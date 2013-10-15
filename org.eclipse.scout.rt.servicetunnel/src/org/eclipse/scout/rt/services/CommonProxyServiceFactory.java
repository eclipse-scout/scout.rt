/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.services;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.servicetunnel.IServiceTunnel;
import org.eclipse.scout.rt.servicetunnel.ServiceTunnelUtility;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.TierState;
import org.eclipse.scout.rt.shared.TierState.Tier;
import org.eclipse.scout.service.CreateServiceImmediatelySchedulingRule;
import org.eclipse.scout.service.IService;
import org.eclipse.scout.service.IServiceFactory;
import org.eclipse.scout.service.ServiceConstants;
import org.eclipse.scout.service.ServiceUtility;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 * Common base class used to create proxy instances for services using a service tunnel.
 * 
 * @author awe (refactoring)
 */
public abstract class CommonProxyServiceFactory<T extends ISession> implements IServiceFactory {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(CommonProxyServiceFactory.class);

  private Bundle m_bundle;
  private final Class<?> m_serviceClass;
  private String m_sessionType;
  private Class<T> m_sessionClass;
  // lazy creation
  private Object m_serviceImpl;
  private final Object m_serviceLock = new Object();

  public CommonProxyServiceFactory(Class<?> serviceClass) {
    if (serviceClass == null) {
      throw new IllegalArgumentException("service type must not be null");
    }
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
    if (isCreateServiceTunnelPossible()) {
      if (TierState.get() == getTier() || TierState.get() == Tier.Undefined) {
        updateInstanceCache(registration);
        if (m_serviceClass.isInterface()) {
          Object service = ServiceTunnelUtility.createProxy(m_serviceClass, createServiceTunnel());
          return service;
        }
        else {
          return m_serviceImpl;
        }
      }
    }
    return ServiceUtility.NULL_SERVICE;
  }

  abstract protected Tier getTier();

  abstract protected Class<T> getDefaultSessionClass();

  abstract protected boolean isCreateServiceTunnelPossible();

  abstract protected IServiceTunnel createServiceTunnel();

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
        Class<T> defaultSessionClass = getDefaultSessionClass();
        if (m_sessionClass == null) {
          if (m_sessionType == null) {
            m_sessionClass = defaultSessionClass;
          }
          else {
            m_sessionClass = (Class<T>) m_bundle.loadClass(m_sessionType);
            if (!defaultSessionClass.isAssignableFrom(m_sessionClass)) {
              throw new IllegalArgumentException("session type must be a subtype of " + defaultSessionClass + ": " + m_sessionType);
            }
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
            m_serviceImpl = createServiceInstance(m_serviceClass);
            if (m_serviceImpl instanceof IService) {
              ((IService) m_serviceImpl).initializeService(registration);
            }
          }
        }
        catch (Throwable t) {
          LOG.error("Failed creating proxy instance for " + m_serviceClass, t);
        }
      }
    }
  }

  final protected Class<T> getSessionClass() {
    return m_sessionClass;
  }

  /**
   * This method creates a new instance from the given service class. The default implementation calls
   * <code>newInstance()</code> on the class instance. Override this method if you must create instances
   * in another way (e.g. by using a dependency injection mechanism).
   * 
   * @param serviceClass
   * @return
   * @throws InstantiationException
   * @throws IllegalAccessException
   */
  protected Object createServiceInstance(Class<?> serviceClass) throws InstantiationException, IllegalAccessException {
    return serviceClass.newInstance();
  }

}
