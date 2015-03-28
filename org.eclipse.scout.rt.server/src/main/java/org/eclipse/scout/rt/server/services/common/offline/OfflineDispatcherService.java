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
import java.security.AccessController;

import javax.security.auth.Subject;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.serialization.SerializationUtility;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.commons.cache.IClientIdentificationService;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.job.ServerJobs;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.server.session.ServerSessionProvider;
import org.eclipse.scout.rt.shared.OfflineState;
import org.eclipse.scout.rt.shared.services.common.offline.IOfflineDispatcherService;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.IServiceTunnelResponse;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;
import org.eclipse.scout.service.ServiceUtility;

public class OfflineDispatcherService extends AbstractService implements IOfflineDispatcherService {

  @Override
  public IServiceTunnelResponse dispatch(final IServiceTunnelRequest request) {
    try {
      ServerRunContext runContext = ServerRunContexts.empty();
      runContext.subject(Subject.getSubject(AccessController.getContext()));
      runContext.locale(request.getLocale());
      runContext.userAgent(UserAgent.createByIdentifier(request.getUserAgent()));
      runContext.session(provideServerSession(runContext.copy()));

      runContext = interceptRunContext(runContext);

      final IServiceTunnelResponse response = invokeService(runContext, request);
      if (response != null) {
        return response;
      }
      else {
        return new ServiceTunnelResponse(null, null, new InterruptedException("Result from handler was null"));
      }
    }
    catch (final Exception e) {
      return new ServiceTunnelResponse(null, null, e);
    }
  }

  /**
   * Override this method to intercept the {@link ServerRunContext} used to process a request. The default
   * implementation simply returns the given <code>runContext</code>.
   */
  protected ServerRunContext interceptRunContext(final ServerRunContext runContext) {
    return runContext;
  }

  /**
   * Method invoked to provide a new or cached {@link IServerSession} for the current request.
   *
   * @param runContext
   *          <code>RunContext</code> with information about the ongoing request.
   * @return {@link IServerSession}; must not be <code>null</code>.
   */
  protected IServerSession provideServerSession(final ServerRunContext runContext) throws ProcessingException {
    final IServerSession serverSession = OBJ.get(ServerSessionProvider.class).provide(runContext);
    serverSession.setIdInternal(SERVICES.getService(IClientIdentificationService.class).getClientId(runContext.servletRequest(), runContext.servletResponse()));
    return serverSession;
  }

  /**
   * Method invoked to delegate the request to the 'process service' on behalf of a server job.
   *
   * @param runContext
   *          <code>RunContext</code> with information about the ongoing request to be used to invoke the service.
   * @param serviceTunnelRequest
   *          describes the service to be invoked.
   * @return {@link IServiceTunnelResponse} response sent back to the caller.
   */
  protected IServiceTunnelResponse invokeService(final ServerRunContext runContext, final IServiceTunnelRequest serviceTunnelRequest) throws ProcessingException {
    JobInput jobInput = ServerJobs.newInput(runContext);
    jobInput.name("OfflineServiceCall");
    jobInput.id(String.valueOf(serviceTunnelRequest.getRequestSequence())); // to cancel server jobs and associated transactions.

    return ServerJobs.schedule(new ICallable<IServiceTunnelResponse>() {

      @Override
      public IServiceTunnelResponse call() throws Exception {
        return invokeService(serviceTunnelRequest);
      }
    }, jobInput).awaitDoneAndGet(); // schedule the service-request to enable cancellation by 'ServerProcessingCancelService.cancel(requestSequence)'
  }

  /**
   * Method invoked to delegate the request to the 'process service'.
   */
  @Internal
  protected IServiceTunnelResponse invokeService(final IServiceTunnelRequest serviceTunnelRequest) throws ProcessingException, ClassNotFoundException {
    OfflineState.setOfflineInCurrentThread(true);

    final Class<?> serviceClass = SerializationUtility.getClassLoader().loadClass(serviceTunnelRequest.getServiceInterfaceClassName());
    final Object service = Assertions.assertNotNull(SERVICES.getService(serviceClass), "service not found in service registry: %s", serviceClass);
    final Method serviceOperation = ServiceUtility.getServiceOperation(serviceClass, serviceTunnelRequest.getOperation(), serviceTunnelRequest.getParameterTypes());

    final Object[] serviceArgs = serviceTunnelRequest.getArgs();
    final Object result = ServiceUtility.invoke(serviceOperation, service, serviceArgs);
    final Object[] outParams = ServiceUtility.extractHolderArguments(serviceArgs);

    final ServiceTunnelResponse serviceResponse = new ServiceTunnelResponse(result, outParams, null);

    // add accumulated client notifications as side-payload
    serviceResponse.setClientNotifications(SERVICES.getService(IClientNotificationService.class).getNextNotifications(0));
    return serviceResponse;
  }
}
