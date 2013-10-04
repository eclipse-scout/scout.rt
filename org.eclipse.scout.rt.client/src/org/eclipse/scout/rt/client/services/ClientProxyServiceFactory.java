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
package org.eclipse.scout.rt.client.services;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.rt.client.ClientAsyncJob;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.IClientSessionProvider;
import org.eclipse.scout.rt.services.CommonProxyServiceFactory;
import org.eclipse.scout.rt.servicetunnel.IServiceTunnel;
import org.eclipse.scout.rt.shared.TierState.Tier;
import org.eclipse.scout.service.IService;
import org.eclipse.scout.service.ServiceConstants;

/**
 * Service factory handling client proxy services based on a {@link IClientSession}. The service exists once per osgi
 * environment and is
 * cached persistent. The proxy is only available within a {@link Job} that
 * implements {@link IClientSessionProvider} with a compatible {@link IClientSession} type. see {@link ClientJob},
 * {@link ClientSyncJob}, {@link ClientAsyncJob} The proxy is tunneled through the {@link IServiceTunnel} provided on
 * the {@link IClientSession}.
 * <p>
 * The factory supports {@link ServiceConstants#SERVICE_SCOPE} and expects an {@link IClientSession} class
 * <p>
 * Visiblity: ClientJob.getCurrentSession()!=null && FE, see also {@link IService} for details
 * <p>
 * This proxy service factory can be used on an interface (default) where it creates an ad-hoc proxy on each operation
 * call, but it also can be used on an implementation, where it is similar to the {@link ClientServiceFactory} but
 * scopes as a proxy. This is useful when creating "pseudo" proxies as in AccessControlClientProxy etc.
 */
public class ClientProxyServiceFactory extends CommonProxyServiceFactory<IClientSession> {

  public ClientProxyServiceFactory(Class<?> serviceClass) {
    super(serviceClass);
  }

  @Override
  protected Tier getTier() {
    return Tier.FrontEnd;
  }

  @Override
  protected Class<IClientSession> getDefaultSessionClass() {
    return IClientSession.class;
  }

  @Override
  protected boolean isCreateServiceTunnelPossible() {
    return getCurrentSession() != null;
  }

  @Override
  protected IServiceTunnel createServiceTunnel() {
    return getCurrentSession().getServiceTunnel();
  }

  private IClientSession getCurrentSession() {
    return ClientJob.getCurrentSession(getSessionClass());
  }
}
