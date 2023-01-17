/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.servicetunnel;

import java.lang.reflect.Method;

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.IThrowableWithContextInfo;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service tunnel is Thread-Safe.
 */
public abstract class AbstractServiceTunnel implements IServiceTunnel {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractServiceTunnel.class);

  @Override
  public Object invokeService(Class<?> serviceInterfaceClass, Method operation, Object[] callerArgs) {
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
   * Method invoked before the service request is tunneled to the server. Overwrite this method to add additional
   * information to the request.
   */
  protected void beforeTunnel(ServiceTunnelRequest serviceRequest) {
  }

  /**
   * Will throw a CancellationException if the future is already cancelled.
   *
   * @throws ThreadInterruptedError
   *           if the current thread is cancelled
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
   * Invokes the service operation remotely on server.
   * <p>
   * This method returns, once the current {@link RunMonitor} gets cancelled. When being cancelled, a cancellation
   * request is sent to the server, and the {@link ServiceTunnelResponse} returned contains an
   * {@link ThreadInterruptedError} to indicate cancellation.
   *
   * @return response sent by the server; is never <code>null</code>.
   */
  protected abstract ServiceTunnelResponse tunnel(final ServiceTunnelRequest serviceRequest);

  /**
   * Method invoked after the service request was tunneled. Overwrite this method to add additional information to the
   * response.
   *
   * @param t0
   *          System time before the request has been started (may be used for performance analyzing).
   */
  protected void afterTunnel(long t0, ServiceTunnelResponse serviceResponse) {
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
