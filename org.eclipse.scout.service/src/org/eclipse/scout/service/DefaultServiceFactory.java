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
package org.eclipse.scout.service;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceRegistration;

/**
 * Service factory handling default osgi services registered in services
 * extension
 * <p>
 * The service exists once per osgi environment and is cached persistent.
 * <p>
 * The factory supports {@link ServiceConstants#SERVICE_CREATE_IMMEDIATELY} and calls
 * {@link IService#initializeService(ServiceRegistration registration)} on {@link IService} instances.
 */
public class DefaultServiceFactory implements IServiceFactory {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DefaultServiceFactory.class);

  private Bundle m_bundle;
  private Class<?> m_serviceClass;
  // lazy creation
  private Object m_service;
  private Object m_serviceLock = new Object();

  public DefaultServiceFactory(Class<?> serviceClass) {
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
    updateInstanceCache(registration);
    return m_service;
  }

  private void updateClassCache(ServiceRegistration registration) {
    synchronized (m_serviceLock) {
      if (m_bundle == null) {
        m_bundle = registration.getReference().getBundle();
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

  @Override
  public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
  }

}
