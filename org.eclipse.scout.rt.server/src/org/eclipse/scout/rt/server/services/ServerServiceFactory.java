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
package org.eclipse.scout.rt.server.services;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.IServerSessionProvider;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.server.ThreadContext;
import org.eclipse.scout.rt.shared.OfflineState;
import org.eclipse.scout.rt.shared.TierState;
import org.eclipse.scout.rt.shared.TierState.Tier;
import org.eclipse.scout.service.CreateServiceImmediatelySchedulingRule;
import org.eclipse.scout.service.IService;
import org.eclipse.scout.service.IService2;
import org.eclipse.scout.service.IServiceFactory;
import org.eclipse.scout.service.ServiceConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 * Service factory handling server services based on a {@link IServerSession}.
 * The service exists once per osgi environment and is cached persistent. The
 * service is only available within a {@link Job} that implements {@link IServerSessionProvider} with a compatible
 * {@link IServerSession} type
 * or within a thread with {@link ThreadContext#get(Class)}!=null see {@link ServerJob}
 * <p>
 * The factory supports {@link ServiceConstants#SERVICE_SCOPE} with an {@link IServerSession} class.<br>
 * The factory supports {@link ServiceConstants#SERVICE_CREATE_IMMEDIATELY}
 * <p>
 * Visiblity: ServerJob.getCurrentSession()!=null && (BE || OFF), see also {@link IService} for details
 */
public class ServerServiceFactory implements IServiceFactory {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServerServiceFactory.class);

  private Bundle m_bundle;
  private Class<?> m_serviceClass;
  private String m_sessionType;
  private Class<? extends IServerSession> m_sessionClass;
  // lazy creation
  private Object m_service;
  private Object m_serviceLock = new Object();

  public ServerServiceFactory(Class<?> serviceClass) {
    if (serviceClass == null) throw new IllegalArgumentException("service type must not be null");
    if (serviceClass.isInterface()) throw new IllegalArgumentException("service type must not be an interface: " + serviceClass);
    m_serviceClass = serviceClass;
  }

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

  public Object getService(Bundle bundle, ServiceRegistration registration) {
    updateClassCache(registration);
    IServerSession session = ServerJob.getCurrentSession(m_sessionClass);
    if (session != null) {
      if (TierState.get() == Tier.BackEnd || TierState.get() == Tier.Undefined || OfflineState.isOfflineInCurrentThread()) {
        updateInstanceCache(registration);
        return m_service;
      }
    }
    return null;
  }

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
            m_sessionClass = IServerSession.class;
          }
          else {
            m_sessionClass = (Class<? extends IServerSession>) m_bundle.loadClass(m_sessionType);
            if (!IServerSession.class.isAssignableFrom(m_sessionClass)) throw new IllegalArgumentException("session type must be a subtype of " + IServerSession.class + ": " + m_sessionType);
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
          if (m_service instanceof IService2) {
            ((IService2) m_service).initializeService(registration);
          }
          else if (m_service instanceof IService) {
            ((IService) m_service).initializeService();
          }
        }
        catch (Throwable t) {
          LOG.error("Failed creating instance of " + m_serviceClass, t);
        }
      }
    }
  }

}
