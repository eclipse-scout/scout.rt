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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientAsyncJob;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.IClientSessionProvider;
import org.eclipse.scout.rt.client.servicetunnel.IServiceTunnel;
import org.eclipse.scout.rt.client.servicetunnel.ServiceTunnelUtility;
import org.eclipse.scout.rt.shared.TierState;
import org.eclipse.scout.rt.shared.TierState.Tier;
import org.eclipse.scout.service.CreateServiceImmediatelySchedulingRule;
import org.eclipse.scout.service.IService;
import org.eclipse.scout.service.IService2;
import org.eclipse.scout.service.IServiceFactory;
import org.eclipse.scout.service.ServiceConstants;
import org.eclipse.scout.service.ServiceUtility;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

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
public class ClientProxyServiceFactory implements IServiceFactory {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientProxyServiceFactory.class);

  private Bundle m_bundle;
  private final Class<?> m_serviceClass;
  private String m_sessionType;
  private Class<? extends IClientSession> m_sessionClass;
  // lazy creation
  private Object m_serviceImpl;
  private final Object m_serviceLock = new Object();

  public ClientProxyServiceFactory(Class<?> serviceClass) {
    if (serviceClass == null) throw new IllegalArgumentException("service type must not be null");
    m_serviceClass = serviceClass;
  }

  @Override
  public void serviceRegistered(final ServiceRegistration registration) throws Throwable {
    if (!m_serviceClass.isInterface()) {
      Boolean createImmediately = (Boolean) registration.getReference().getProperty(ServiceConstants.SERVICE_CREATE_IMMEDIATELY);
      if (createImmediately != null && createImmediately) {
        Job job = new Job("create service " + m_serviceClass.getSimpleName()) {
          @Override
          protected IStatus run(IProgressMonitor monitor) {
            updateClassCache(registration);
            updateInstanceCache(registration);
            return Status.OK_STATUS;
          }
        };
        job.setRule(new CreateServiceImmediatelySchedulingRule());
        job.schedule();
      }
    }
  }

  @Override
  public Object getService(Bundle bundle, ServiceRegistration registration) {
    updateClassCache(registration);
    IClientSession session = ClientJob.getCurrentSession(m_sessionClass);
    if (session != null) {
      if (TierState.get() == Tier.FrontEnd || TierState.get() == Tier.Undefined) {
        updateInstanceCache(registration);
        if (m_serviceClass.isInterface()) {
          Object service = ServiceTunnelUtility.createProxy(m_serviceClass, session.getServiceTunnel());
          return service;
        }
        else {
          return m_serviceImpl;
        }
      }
    }
    return ServiceUtility.NULL_SERVICE;
  }

  @Override
  public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
  }

  @SuppressWarnings("unchecked")
  private void updateClassCache(ServiceRegistration registration) {
    synchronized (m_serviceLock) {
      if (m_bundle == null) {
        m_bundle = registration.getReference().getBundle();
      }
      if (m_sessionType == null) {
        m_sessionType = (String) registration.getReference().getProperty(ServiceConstants.SERVICE_SCOPE);
      }
      try {
        if (m_sessionClass == null) {
          if (m_sessionType == null) {
            m_sessionClass = IClientSession.class;
          }
          else {
            m_sessionClass = (Class<? extends IClientSession>) m_bundle.loadClass(m_sessionType);
            if (!IClientSession.class.isAssignableFrom(m_sessionClass)) throw new IllegalArgumentException("session type must be a subtype of " + IClientSession.class + ": " + m_sessionType);
          }
        }
      }
      catch (Throwable t) {
        LOG.error("Failed creating proxy class for " + m_serviceClass, t);
      }
    }
  }

  private void updateInstanceCache(ServiceRegistration registration) {
    synchronized (m_serviceLock) {
      if (m_serviceImpl == null) {
        try {
          if (!m_serviceClass.isInterface()) {
            m_serviceImpl = m_serviceClass.newInstance();
            if (m_serviceImpl instanceof IService2) {
              ((IService2) m_serviceImpl).initializeService(registration);
            }
            else if (m_serviceImpl instanceof IService) {
              ((IService) m_serviceImpl).initializeService();
            }

          }
        }
        catch (Throwable t) {
          LOG.error("Failed creating proxy instance for " + m_serviceClass, t);
        }
      }
    }
  }
}
