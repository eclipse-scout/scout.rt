/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.servicetunnel;

import java.lang.reflect.Method;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.util.concurrent.InterruptedRuntimeException;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service tunnel is Thread-Safe.
 */
public abstract class AbstractServiceTunnel implements IServiceTunnel {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractServiceTunnel.class);

  public AbstractServiceTunnel() {
  }

  @Override
  public Object invokeService(Class serviceInterfaceClass, Method operation, Object[] callerArgs) {
    long t0 = System.nanoTime();
    if (callerArgs == null) {
      callerArgs = new Object[0];
    }
    LOG.debug("{}.{}({})", serviceInterfaceClass, operation, callerArgs);

    ServiceUtility serviceUtility = BEANS.get(ServiceUtility.class);
    Object[] serializableArgs = serviceUtility.filterHolderArguments(callerArgs);
    ServiceTunnelRequest request = createServiceTunnelRequest(serviceInterfaceClass, operation, serializableArgs);
    beforeTunnel(request);
    ServiceTunnelResponse response = tunnel(request);
    afterTunnel(t0, response);

    // Exception handling
    Throwable t = response.getException();
    if (t != null) {
      // Associate the exception with context information about the service call (without arg values due to security reasons).
      RuntimeException serviceException = interceptException(t);
      if (serviceException instanceof PlatformException) {
        ((PlatformException) serviceException)
            .withContextInfo("remote-service.name", serviceInterfaceClass.getSimpleName())
            .withContextInfo("remote-service.operation", operation.getName());
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
    serviceUtility.updateHolderArguments(callerArgs, response.getOutVars(), false);
    return response.getData();
  }

  protected ServiceTunnelRequest createServiceTunnelRequest(Class serviceInterfaceClass, Method operation, Object[] args) {
    UserAgent userAgent = UserAgent.CURRENT.get();
    if (userAgent == null) {
      LOG.warn("No UserAgent set on calling context; include default in service-request");
      userAgent = UserAgent.createDefault();
    }

    // default implementation
    ServiceTunnelRequest call = new ServiceTunnelRequest(serviceInterfaceClass.getName(), operation.getName(), operation.getParameterTypes(), args);
    call.setUserAgent(userAgent.createIdentifier());

    return call;
  }

  /**
   * Method invoked before the service request is tunneled to the server. Overwrite this method to add additional
   * information to the request.
   */
  protected void beforeTunnel(ServiceTunnelRequest serviceRequest) {
  }

  /**
   * Invokes the service operation remotely on server.
   * <p>
   * This method returns, once the current {@link RunMonitor} gets cancelled. When being cancelled, a cancellation
   * request is sent to the server, and the {@link ServiceTunnelResponse} returned contains an
   * {@link InterruptedRuntimeException} to indicate cancellation.
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
   * @param serviceResponse
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
