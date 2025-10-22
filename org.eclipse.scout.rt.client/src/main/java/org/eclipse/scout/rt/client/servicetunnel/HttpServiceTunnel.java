/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.servicetunnel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;

import jakarta.ws.rs.core.Response;

import org.eclipse.scout.rt.client.clientnotification.ClientNotificationDispatcher;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.services.common.perf.IPerformanceAnalyzerService;
import org.eclipse.scout.rt.client.servicetunnel.ServiceTunnelClientConfigProperties.BackendUrlProperty;
import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.IThrowableWithContextInfo;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.opentelemetry.HttpServiceTunnelInstrumenterFactory;
import org.eclipse.scout.rt.shared.servicetunnel.BinaryServiceTunnelContentHandler;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.http.HttpResponse;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;

/**
 * Client-side tunnel used to invoke a service through HTTP (REST).
 */
@ApplicationScoped
public class HttpServiceTunnel {
  private static final Logger LOG = LoggerFactory.getLogger(HttpServiceTunnel.class);

  private final boolean m_active;

  private final Instrumenter<ServiceTunnelRequest, Void> m_instrumenter;
  private boolean m_analyzeNetworkLatency = true;

  public HttpServiceTunnel() {
    m_active = StringUtility.hasText(CONFIG.getPropertyValue(BackendUrlProperty.class));
    m_instrumenter = BEANS.get(HttpServiceTunnelInstrumenterFactory.class).createInstrumenter();
  }

  /**
   * The service tunnel is accessible over the {@link BEANS}. The service tunnel will always be available and indicates
   * readiness for usage with this method.
   *
   * @return true when the service tunnel is ready to get invoked false otherwise.
   */
  public boolean isActive() {
    return m_active;
  }

  /**
   * Execute a {@link ServiceTunnelRequest}, returns the plain {@link HttpResponse} - (executed and) ready to be
   * processed to create a {@link ServiceTunnelResponse}.
   *
   * @param call
   *     the original call
   * @param callData
   *     the data created by the {@link BinaryServiceTunnelContentHandler} used by this tunnel Create url connection and
   *     write post data (if required)
   */
  protected Response executeRequestInternal(ServiceTunnelRequest call, byte[] callData) throws IOException {
    return BEANS.get(ProcessResourceClient.class).call(new ByteArrayInputStream(callData));
  }

  protected Response executeRequest(ServiceTunnelRequest call, byte[] callData) throws IOException {
    Context parentContext = Context.current();

    if (!m_instrumenter.shouldStart(parentContext, call)) {
      return executeRequestInternal(call, callData);
    }

    Context context = m_instrumenter.start(parentContext, call);
    Response response;
    try (Scope ignored = context.makeCurrent()) {
      response = executeRequestInternal(call, callData);
    }
    catch (Throwable t) {
      m_instrumenter.end(context, call, null, t);
      throw t;
    }
    m_instrumenter.end(context, call, null, null);
    return response;
  }

  /**
   * Invoke a remote service through a service tunnel<br>
   * The argument array may contain IHolder values which are updated as OUT parameters when the backend call has
   * completed flags are custom flags not used by the framework itself
   */
  public Object invokeService(Class serviceInterfaceClass, Method operation, Object[] callerArgs) {
    LOG.debug("{}.{}({})", serviceInterfaceClass, operation, callerArgs);
    ServiceTunnelRequest request = createRequest(serviceInterfaceClass, operation, callerArgs);
    interceptRequest(request);
    return invokeService(request);
  }

  public Object invokeService(ServiceTunnelRequest request) {
    final long t0 = System.nanoTime();

    checkAlreadyCancelled(request);
    beforeTunnel(request);
    ServiceTunnelResponse response = tunnel(request);
    afterTunnel(t0, response);

    // Exception handling
    Throwable t = response.getException();
    if (t != null) {
      // Associate the exception with context information about the service call (without arg values due to security reasons).
      RuntimeException serviceException = interceptException(t);
      if (serviceException instanceof PlatformException) {
        ((IThrowableWithContextInfo) serviceException)
            .withContextInfo("remote-service.name", request.getServiceInterfaceClassName())
            .withContextInfo("remote-service.operation", request.getOperation());
      }

      // Combine local and remote stacktraces.
      StackTraceElement[] trace1 = serviceException.getStackTrace();
      StackTraceElement[] trace2 = new Exception().getStackTrace();
      StackTraceElement[] both = new StackTraceElement[trace1.length + trace2.length];
      System.arraycopy(trace1, 0, both, 0, trace1.length);
      System.arraycopy(trace2, 0, both, trace1.length, trace2.length);
      serviceException.setStackTrace(both);
      throw serviceException;
    }
    return response.getData();
  }

  public ServiceTunnelRequest createRequest(Class<?> interfaceClass, Method operation, Object[] args) {
    if (args == null) {
      args = new Object[0];
    }
    return new ServiceTunnelRequest(interfaceClass.getName(), operation.getName(), operation.getParameterTypes(), args);
  }

  protected void interceptRequest(ServiceTunnelRequest request) {
    UserAgent userAgent = UserAgent.CURRENT.get();
    if (userAgent == null) {
      LOG.warn("No UserAgent set on calling context; include default in service-request");
      userAgent = UserAgents.createDefault();
    }
    request.setUserAgent(userAgent.createIdentifier());

    ISession session = ISession.CURRENT.get();
    if (session != null) {
      request.setSessionId(session.getId());
    }
    request.setClientNodeId(NodeId.current());
  }

  /**
   * Creates the {@link Callable} to invoke the remote service operation described by 'serviceRequest'.
   * <p>
   * To enable cancellation, the callable returned must also implement {@link ICancellable}, so that the remote
   * operation can be cancelled once the current {@link RunMonitor} gets cancelled.
   */
  protected RemoteServiceInvocationCallable createRemoteServiceInvocationCallable(ServiceTunnelRequest serviceRequest) {
    return new RemoteServiceInvocationCallable(this, serviceRequest);
  }

  /**
   * Invokes the service operation remotely on server.
   * <p>
   * This method returns, once the current {@link RunMonitor} gets cancelled. When being cancelled, a cancellation
   * request is sent to the server, and the {@link ServiceTunnelResponse} returned contains an
   * {@link ThreadInterruptedError} to indicate cancellation.
   *
   * @return response sent by the server; is never <code>null</code>.
   */
  protected ServiceTunnelResponse tunnel(final ServiceTunnelRequest serviceRequest) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("requestSequence {} {}.{}", serviceRequest.getRequestSequence(), serviceRequest.getServiceInterfaceClassName(), serviceRequest.getOperation());
    }
    final long requestSequence = serviceRequest.getRequestSequence();

    // Create the Callable to be given to the job manager for execution.
    final RemoteServiceInvocationCallable remoteInvocationCallable = createRemoteServiceInvocationCallable(serviceRequest);

    // Register the execution monitor as child monitor of the current monitor so that the service request is cancelled once the current monitor gets cancelled.
    // Invoke the service operation asynchronously (to enable cancellation) and wait until completed or cancelled.
    final IFuture<ServiceTunnelResponse> future = Jobs
        .schedule(remoteInvocationCallable,
            Jobs.newInput().withRunContext(RunContext.CURRENT.get().copy())
                .withName(createServiceRequestName(requestSequence))
                .withExceptionHandling(null, false)); // do not handle uncaught exceptions because typically invoked from within a model job (might cause a deadlock, because ClientExceptionHandler schedules and waits for a model job to visualize the exception).

    try {
      return future.awaitDoneAndGet();
    }
    catch (ThreadInterruptedError e) { // NOSONAR
      future.cancel(true); // Ensure the monitor to be cancelled once this thread is interrupted to cancel the remote call.
      return new ServiceTunnelResponse(new ThreadInterruptedError("UserInterrupted")); // Interruption has precedence over computation result or computation error.
    }
    catch (FutureCancelledError e) { // NOSONAR
      return new ServiceTunnelResponse(new FutureCancelledError("UserInterrupted")); // Cancellation has precedence over computation result or computation error.
    }
  }

  /**
   * This method is called just after the HTTP response is received, but before being processed, and might be used to
   * read and interpret custom HTTP headers.
   */
  protected void interceptHttpResponse(Response httpResponse, ServiceTunnelRequest call) {
    // subclasses may intercept jersey response
  }

  /**
   * Returns the name to decorate the thread's name while executing the service request.
   */
  protected String createServiceRequestName(final long requestSequence) {
    final IFuture<?> currentFuture = IFuture.CURRENT.get();
    final String submitter = (currentFuture != null ? currentFuture.getJobInput().getName() : Thread.currentThread().getName());
    return String.format("Tunneling service request [seq=%s, submitter=%s]", requestSequence, submitter);
  }

  /**
   * see {@link #setAnalyzeNetworkLatency(boolean)} default is true
   */
  public boolean isAnalyzeNetworkLatency() {
    return m_analyzeNetworkLatency;
  }

  /**
   * If true the client notification polling process analyzes network latency to optimize the poll interval in order to
   * save the network. for Experts: constant N is defined as: N=10 Assertion is: pollInterval &gt; N*networkLatency
   * Example: the initial pollInterval is 2000ms and the moving average of the networkLatency reaches 700ms, then the
   * used polling interval will be max(2000ms,N*700ms) -&gt; 7000ms
   */
  public void setAnalyzeNetworkLatency(boolean b) {
    m_analyzeNetworkLatency = b;
  }

  /**
   * Method invoked before the service request is tunneled to the server. Overwrite this method to add additional
   * information to the request.
   */
  protected void beforeTunnel(ServiceTunnelRequest serviceRequest) {
  }

  /**
   * Method invoked after the service request was tunneled. Overwrite this method to add additional information to the
   * response.
   *
   * @param t0
   *     System time before the request has been started (may be used for performance analyzing).
   */
  protected void afterTunnel(long t0, ServiceTunnelResponse serviceResponse) {
    if (isAnalyzeNetworkLatency()) {
      // performance analyzer
      IPerformanceAnalyzerService perf = BEANS.opt(IPerformanceAnalyzerService.class);
      if (perf != null) {
        long totalMillis = (System.nanoTime() - t0) / 1000000L;
        Long execMillis = serviceResponse.getProcessingDuration();
        if (execMillis != null) {
          perf.addNetworkLatencySample(totalMillis - execMillis);
          perf.addServerExecutionTimeSample(execMillis);
        }
        else {
          perf.addNetworkLatencySample(totalMillis);
        }
      }
    }

    // process piggyback client notifications.
    try {
      dispatchClientNotifications(serviceResponse.getNotifications());
    }
    catch (RuntimeException e) {
      LOG.error("Error during processing piggyback client notifictions.", e);
    }
  }

  /**
   * dispatch notifications in a client job and ensure to wait for dispatched notifications
   *
   * @param notifications
   *     the notifications to dispatch
   */
  protected void dispatchClientNotifications(final List<ClientNotificationMessage> notifications) {
    if (CollectionUtility.isEmpty(notifications)) {
      return;
    }
    final IBlockingCondition cond = Jobs.newBlockingCondition(true);
    Jobs.schedule(() -> {
          ClientNotificationDispatcher notificationDispatcher = BEANS.get(ClientNotificationDispatcher.class);
          notificationDispatcher.dispatchNotifications(notifications);
        }, Jobs.newInput()
            .withRunContext(ClientRunContexts.copyCurrent()))
        .whenDone(event -> cond.setBlocking(false), null);
    cond.waitFor();
  }

  /**
   * Will throw a CancellationException if the future is already cancelled.
   *
   * @throws ThreadInterruptedError
   *     if the current thread is cancelled
   */
  protected void checkAlreadyCancelled(ServiceTunnelRequest serviceRequest) {
    final RunMonitor monitor = RunMonitor.CURRENT.get();
    if (monitor != null && monitor.isCancelled()) {
      final StringBuilder cancellationExceptionText = new StringBuilder();
      cancellationExceptionText.append("RunMonitor is already cancelled.");
      if (serviceRequest != null) {
        cancellationExceptionText.append(" (Request was '");
        cancellationExceptionText.append(serviceRequest.getServiceInterfaceClassName());
        cancellationExceptionText.append(".");
        cancellationExceptionText.append(serviceRequest.getOperation());
        cancellationExceptionText.append("(..)')");
      }

      throw new ThreadInterruptedError(cancellationExceptionText.toString());
    }
  }

  /**
   * Method invoked to intercept a service exception before being propagated to the caller.
   * <p>
   * The default implementation translates the {@link Throwable} via {@link DefaultRuntimeExceptionTranslator}.
   */
  protected RuntimeException interceptException(Throwable t) {
    return BEANS.get(DefaultRuntimeExceptionTranslator.class).translate(t);
  }
}
