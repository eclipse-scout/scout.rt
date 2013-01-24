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
package org.eclipse.scout.testing.client;

import java.util.HashMap;
import java.util.List;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.session.IClientSessionRegistryService;
import org.eclipse.scout.rt.shared.ui.UserAgent;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.ServiceRegistration;

/**
 * The TestingClientSessionRegistryService is intended to be used within the context of automatic GUI tests.
 * It delegates method calls to a given concrete implementation of IClientSessionRegistryService passed on the
 * constructor. Additionally, the TestingClientSessionRegistryService caches all created client sessions that
 * are needed for GUI tests to reuse the already initialized client session.
 * <p>
 * To use the TestingClientSessionRegistryService, it has to be registered by calling the method
 * {@link #registerTestingClientSessionRegistryService}. For Swing and SWT GUI tests, this is done when the
 * SwingApplication or SwtApplication respectively gets started. In case of RAP GUI tests, this service is registered
 * when the JUnitRAPJob is scheduled. The service is unregistered with
 * {@link #unregisterTestingClientSessionRegistryService} when the Application or the JUnitJob terminates
 * 
 * @since 3.8.1
 */
public class TestingClientSessionRegistryService extends AbstractService implements IClientSessionRegistryService {

  private final HashMap<String, IClientSession> m_cache = new HashMap<String, IClientSession>();
  private final Object m_cacheLock = new Object();

  private final IClientSessionRegistryService m_delegate;

  private List<ServiceRegistration> m_serviceRegistrations;

  public TestingClientSessionRegistryService(IClientSessionRegistryService delegate) {
    m_delegate = delegate;
  }

  public static TestingClientSessionRegistryService registerTestingClientSessionRegistryService() {
    IClientSessionRegistryService delegateClientSessionRegistryService = SERVICES.getService(IClientSessionRegistryService.class);
    TestingClientSessionRegistryService testingClientSessionRegistryService = new TestingClientSessionRegistryService(delegateClientSessionRegistryService);
    List<ServiceRegistration> regs = TestingUtility.registerServices(Activator.getDefault().getBundle(), 1000, testingClientSessionRegistryService);
    testingClientSessionRegistryService.setServiceRegistrations(regs);
    return testingClientSessionRegistryService;
  }

  public static void unregisterTestingClientSessionRegistryService(TestingClientSessionRegistryService service) {
    if (service == null || service.getServiceRegistrations() == null) {
      return;
    }
    TestingUtility.unregisterServices(service.getServiceRegistrations());
  }

  public List<ServiceRegistration> getServiceRegistrations() {
    return m_serviceRegistrations;
  }

  public void setServiceRegistrations(List<ServiceRegistration> serviceRegistrations) {
    m_serviceRegistrations = serviceRegistrations;
  }

  public IClientSessionRegistryService getDelegateService() {
    return m_delegate;
  }

  /**
   * {@inheritDoc} Returns an already cached client session if available. Otherwise, a new ClientSession object
   * is created and put into the cache.
   */
  @Override
  public <T extends IClientSession> T newClientSession(Class<T> clazz, UserAgent userAgent) {
    synchronized (m_cacheLock) {
      @SuppressWarnings("unchecked")
      T clientSession = (T) m_cache.get(clazz.getName());
      if (clientSession != null) {
        return clientSession;
      }
      clientSession = m_delegate.newClientSession(clazz, userAgent);
      m_cache.put(clazz.getName(), clientSession);
      return clientSession;
    }
  }

  /**
   * {@inheritDoc} Returns an already cached client session if available. Otherwise, a new ClientSession object
   * is created and put into the cache.
   */
  @Override
  public <T extends IClientSession> T newClientSession(Class<T> clazz, Subject subject, String virtualSessionId, UserAgent userAgent) {
    synchronized (m_cacheLock) {
      @SuppressWarnings("unchecked")
      T clientSession = (T) m_cache.get(clazz.getName());
      if (clientSession != null) {
        return clientSession;
      }
      clientSession = m_delegate.newClientSession(clazz, subject, virtualSessionId, userAgent);
      m_cache.put(clazz.getName(), clientSession);
      return clientSession;
    }

  }

  @Override
  @Deprecated
  @SuppressWarnings("deprecation")
  public <T extends IClientSession> T getClientSession(Class<T> clazz) {
    return m_delegate.getClientSession(clazz);
  }

  @Override
  @Deprecated
  @SuppressWarnings("deprecation")
  public <T extends IClientSession> T newClientSession(Class<T> clazz, Subject subject, String virtualSessionId) {
    return m_delegate.newClientSession(clazz, subject, virtualSessionId);
  }

}
