/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.offline;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.serialization.SerializationUtility;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.ExceptionTranslator;
import org.eclipse.scout.rt.platform.service.AbstractService;
import org.eclipse.scout.rt.platform.service.ServiceUtility;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.RunMonitorCancelRegistry;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache;
import org.eclipse.scout.rt.shared.services.common.offline.IOfflineDispatcherService;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.ui.UserAgent;

public class OfflineDispatcherService extends AbstractService implements IOfflineDispatcherService {

  @Override
  public IServiceTunnelResponse dispatch(final ServiceTunnelRequest serviceRequest) {
    try {
      // Enable global cancellation of the service request.
      RunMonitor runMonitor = BEANS.get(RunMonitor.class);

      ServerRunContext serverRunContext = ServerRunContexts.copyCurrent();
      serverRunContext.offline(true);
      serverRunContext.locale(serviceRequest.getLocale());
      serverRunContext.userAgent(UserAgent.createByIdentifier(serviceRequest.getUserAgent()));
      serverRunContext.runMonitor(runMonitor);
      serverRunContext.session(provideServerSession(serverRunContext.copy()), true);

      IServerSession session = serverRunContext.session();
      long requestId = serviceRequest.getRequestSequence();

      BEANS.get(RunMonitorCancelRegistry.class).register(session, requestId, runMonitor);
      try {
        IServiceTunnelResponse serviceResponse = invokeService(serverRunContext, serviceRequest);
        return (serviceResponse != null ? serviceResponse : new ServiceTunnelResponse(null, null, new InterruptedException("Result from handler was null")));
      }
      finally {
        BEANS.get(RunMonitorCancelRegistry.class).unregister(session, requestId);
      }
    }
    catch (final Exception e) {
      return new ServiceTunnelResponse(null, null, e);
    }
  }

  /**
   * Method invoked to provide a new or cached {@link IServerSession} for the current request.
   */
  protected IServerSession provideServerSession(final ServerRunContext serverRunContext) throws ProcessingException {
    return BEANS.get(ServerSessionProviderWithCache.class).provide(serverRunContext);
  }

  /**
   * Method invoked to delegate the request to the 'process service'.
   */
  protected IServiceTunnelResponse invokeService(final ServerRunContext serverRunContext, final ServiceTunnelRequest serviceTunnelRequest) throws Exception {
    return serverRunContext.call(new Callable<IServiceTunnelResponse>() {

      @Override
      public IServiceTunnelResponse call() throws Exception {
        final Class<?> serviceClass = SerializationUtility.getClassLoader().loadClass(serviceTunnelRequest.getServiceInterfaceClassName());
        final Object service = Assertions.assertNotNull(BEANS.get(serviceClass), "service not found in service registry: %s", serviceClass);
        final Method serviceOperation = ServiceUtility.getServiceOperation(serviceClass, serviceTunnelRequest.getOperation(), serviceTunnelRequest.getParameterTypes());

        final Object[] serviceArgs = serviceTunnelRequest.getArgs();
        final Object result = ServiceUtility.invoke(serviceOperation, service, serviceArgs);
        final Object[] outParams = ServiceUtility.extractHolderArguments(serviceArgs);

        final ServiceTunnelResponse serviceResponse = new ServiceTunnelResponse(result, outParams, null);

        return serviceResponse;
      }
    }, BEANS.get(ExceptionTranslator.class));
  }
}
