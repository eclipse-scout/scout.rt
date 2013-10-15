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
import org.eclipse.scout.service.CreateServiceImmediatelySchedulingRule;
import org.eclipse.scout.service.IService;
import org.eclipse.scout.service.IServiceFactory;
import org.eclipse.scout.service.ServiceConstants;
import org.eclipse.scout.service.ServiceUtility;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 * Service factory handling client services based on a {@link IClientSession}.
 * The service exists once per osgi environment and is cached persistent. The
 * service is only available within a {@link Job} that implements {@link IClientSessionProvider} with a compatible
 * {@link IClientSession} type.
 * see {@link ClientJob}, {@link ClientSyncJob}, {@link ClientAsyncJob}
 * <p>
 * The factory supports {@link ServiceConstants#SERVICE_SCOPE} with an {@link IClientSession} class.<br>
 * The factory supports {@link ServiceConstants#SERVICE_CREATE_IMMEDIATELY}
 * <p>
 * Visiblity: ClientJob.getCurrentSession()!=null, see also {@link IService} for details
 */
public class ClientServiceFactory implements IServiceFactory {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientServiceFactory.class);

  private Bundle m_bundle;
  private final Class<?> m_serviceClass;
  private String m_sessionType;
  private Class<? extends IClientSession> m_sessionClass;
  // lazy creation
  private Object m_service;
  private final Object m_serviceLock = new Object();

  public ClientServiceFactory(Class<?> serviceClass) {
    if (serviceClass == null) {
      throw new IllegalArgumentException("service type must not be null");
    }
    if (serviceClass.isInterface()) {
      throw new IllegalArgumentException("service type must not be an interface: " + serviceClass);
    }
    m_serviceClass = serviceClass;
  }

  @Override
  public void serviceRegistered(final ServiceRegistration registration) throws Throwable {
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

  @Override
  public Object getService(Bundle bundle, ServiceRegistration registration) {
    updateClassCache(registration);
    IClientSession session = ClientJob.getCurrentSession(m_sessionClass);
    if (session != null) {
      updateInstanceCache(registration);
      return m_service;
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
            if (!IClientSession.class.isAssignableFrom(m_sessionClass)) {
              throw new IllegalArgumentException("session type must be a subtype of " + IClientSession.class + ": " + m_sessionType);
            }
          }
        }
      }
      catch (Throwable t) {
        LOG.error("Failed creating class of " + m_serviceClass, t);
      }
    }
  }

  private void updateInstanceCache(ServiceRegistration registration) {
    synchronized (m_serviceLock) {
      if (m_service == null) {
        try {
          m_service = m_serviceClass.newInstance();
          if (m_service instanceof IService) {
            ((IService) m_service).initializeService(registration);
          }
        }
        catch (Throwable t) {
          LOG.error("Failed creating instance of " + m_serviceClass, t);
        }
      }
    }
  }
}
