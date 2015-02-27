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
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.ICallable;
import org.eclipse.scout.commons.serialization.SerializationUtility;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.commons.cache.IClientIdentificationService;
import org.eclipse.scout.rt.server.job.IServerJobManager;
import org.eclipse.scout.rt.server.job.ServerJobInput;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache;
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
      // Create the job-input on behalf of which the server-job is run.
      ServerJobInput input = ServerJobInput.empty();
      input.name("OfflineServiceCall");
      input.id(String.valueOf(request.getRequestSequence())); // to cancel server jobs and associated transactions.
      input.subject(Subject.getSubject(AccessController.getContext()));
      input.locale(request.getLocale());
      input.userAgent(UserAgent.createByIdentifier(request.getUserAgent()));
      input.session(provideServerSession(input.copy()));

      input = interceptServerJobInput(input);

      final IServiceTunnelResponse response = invokeServiceInServerJob(input, request);
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
   * Override this method to intercept the {@link ServerJobInput} used to run server jobs. The default implementation
   * simply returns the given input.
   */
  protected ServerJobInput interceptServerJobInput(final ServerJobInput input) {
    return input;
  }

  /**
   * Method invoked to provide a new or cached {@link IServerSession} for the current request.
   *
   * @param input
   *          context information about the ongoing request.
   * @return {@link IServerSession}; must not be <code>null</code>.
   */
  protected IServerSession provideServerSession(final ServerJobInput input) throws ProcessingException {
    final IServerSession serverSession = OBJ.one(ServerSessionProviderWithCache.class).provide(input);
    serverSession.setIdInternal(SERVICES.getService(IClientIdentificationService.class).getClientId(input.getServletRequest(), input.getServletResponse()));
    return serverSession;
  }

  /**
   * Method invoked to delegate the request to the 'process service' on behalf of a server job.
   *
   * @param input
   *          input to be used to run the server job with current context information set.
   * @param serviceTunnelRequest
   *          describes the service to be invoked.
   * @return {@link IServiceTunnelResponse} response sent back to the caller.
   */
  protected IServiceTunnelResponse invokeServiceInServerJob(final ServerJobInput input, final IServiceTunnelRequest serviceTunnelRequest) throws ProcessingException {
    return OBJ.one(IServerJobManager.class).schedule(new ICallable<IServiceTunnelResponse>() {

      @Override
      public IServiceTunnelResponse call() throws Exception {
        return invokeService(serviceTunnelRequest);
      }
    }, input).get(); // schedule the service-request to enable cancellation by 'ServerProcessingCancelService.cancel(requestSequence)'
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
