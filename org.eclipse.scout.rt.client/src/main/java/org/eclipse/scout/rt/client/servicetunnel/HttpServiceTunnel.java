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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import org.eclipse.scout.rt.client.clientnotification.ClientNotificationDispatcher;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.services.common.perf.IPerformanceAnalyzerService;
import org.eclipse.scout.rt.client.servicetunnel.ServiceTunnelClientConfigProperties.BackendUrlProperty;
import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.IThrowableWithContextInfo;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.exception.RemoteSystemUnavailableException;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
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

  protected Response executeRequest(ServiceTunnelRequest call, byte[] callData) throws IOException {
    Context parentContext = Context.current();

    if (!m_instrumenter.shouldStart(parentContext, call)) {
      return BEANS.get(ProcessResourceClient.class).call(new ByteArrayInputStream(callData));
    }

    Context context = m_instrumenter.start(parentContext, call);
    Response response;
    try (Scope ignored = context.makeCurrent()) {
      response = BEANS.get(ProcessResourceClient.class).call(new ByteArrayInputStream(callData));
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
   * Invokes the service operation remotely on server.
   * <p>
   * This method returns, once the current {@link RunMonitor} gets cancelled. When being cancelled, a cancellation
   * request is sent to the server, and the {@link ServiceTunnelResponse} returned contains an
   * {@link ThreadInterruptedError} to indicate cancellation.
   *
   * @return response sent by the server; is never <code>null</code>; in case of an error the response may as well contain an exception
   */
  protected ServiceTunnelResponse tunnel(final ServiceTunnelRequest serviceRequest) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("requestSequence {} {}.{}", serviceRequest.getRequestSequence(), serviceRequest.getServiceInterfaceClassName(), serviceRequest.getOperation());
    }

    BinaryServiceTunnelContentHandler contentHandler = BEANS.get(BinaryServiceTunnelContentHandler.class);
    long nBytes = 0;
    final long tStart = LOG.isDebugEnabled() ? System.nanoTime() : 0L;

    try {
      final ByteArrayOutputStream requestMessage = new ByteArrayOutputStream();
      contentHandler.writeRequest(requestMessage, serviceRequest);
      requestMessage.close();
      final byte[] requestData = requestMessage.toByteArray();
      nBytes = requestData.length;

      Response response = executeRequest(serviceRequest, requestData);  // response will be closed by #readResponse
      try {
        interceptHttpResponse(response, serviceRequest);
        return readResponse(response, contentHandler);
      }
      catch (Throwable t) {
        response.close(); // close response in case of exception
        throw t;
      }
    }
    catch (RemoteSystemUnavailableException exception) {
      Throwable cause = exception.getCause();
      if (cause instanceof WebApplicationException) {
        int status = ((WebApplicationException) cause).getResponse().getStatus();
        return new ServiceTunnelResponse(new HttpServiceTunnelException(status, "Service tunnel request failed with status code {}", status));
      }
      return new ServiceTunnelResponse(new HttpServiceTunnelException("Service tunnel request failed", exception));
    }
    catch (ClassNotFoundException | IOException e) {
      if (Thread.currentThread().isInterrupted()) {
        LOG.debug("Ignoring IOException for interrupted thread.", e);
        return new ServiceTunnelResponse(new ThreadInterruptedError("Thread is interrupted.", e));
      }
      else if (RunMonitor.CURRENT.get().isCancelled()) {
        LOG.debug("Ignoring IOException for cancelled thread.", e);
        return new ServiceTunnelResponse(new FutureCancelledError("RunMonitor is cancelled.", e));
      }
      return new ServiceTunnelResponse(e);
    }
    finally {
      if (LOG.isDebugEnabled()) {
        final long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - tStart);
        LOG.debug("TIME {}.{} {}ms {} bytes", serviceRequest.getServiceInterfaceClassName(), serviceRequest.getOperation(), elapsedMillis, nBytes);
      }
    }
  }

  /**
   * Read {@link Response} and parse into {@link ServiceTunnelResponse}
   * This method is responsible to close the given REST {@code response}.
   */
  protected ServiceTunnelResponse readResponse(Response response, BinaryServiceTunnelContentHandler contentHandler) throws IOException, ClassNotFoundException {
    try (response; InputStream in = response.readEntity(InputStream.class)) {
      // Receive the response.
      ServiceTunnelResponse serviceTunnelResponse = contentHandler.readResponse(in);
      if (serviceTunnelResponse == null) {
        return new ServiceTunnelResponse(new ProcessingException("Response contains no content")
            .withContextInfo("http-status", "{} {}", response.getStatus(), response.getStatusInfo())
            .withContextInfo("http-headers", response.getHeaders() + ""));
      }
      return serviceTunnelResponse;
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
