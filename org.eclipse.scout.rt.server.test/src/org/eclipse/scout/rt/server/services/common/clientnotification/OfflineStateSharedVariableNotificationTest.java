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
package org.eclipse.scout.rt.server.services.common.clientnotification;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.server.AbstractServerSession;
import org.eclipse.scout.rt.shared.OfflineState;
import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;
import org.eclipse.scout.rt.testing.server.Activator;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.ServiceRegistration;

/**
 * This class tests the behavior of sent client notifications when
 * the shared variable map changes on the server side.<br/>
 * {@code SharedContextChangedNotification}s should only be sent if
 * the default offline state matches the offline state of the current thread.
 * 
 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=379721">Bugzilla 379721</a>
 */
public class OfflineStateSharedVariableNotificationTest {

  private TestServerSession m_serverSession;
  private List<ServiceRegistration> m_registrationList;

  @Before
  public void setUp() {
    OfflineState.setOfflineDefault(false);
    OfflineState.setOfflineInCurrentThread(null);
    m_serverSession = new TestServerSession();
    m_registrationList = TestingUtility.registerServices(Activator.getDefault().getBundle(), 1000, new MockClientNotificationService());
  }

  @After
  public void tearDown() {
    OfflineState.setOfflineDefault(false);
    OfflineState.setOfflineInCurrentThread(null);
    m_serverSession = null;
    TestingUtility.unregisterServices(m_registrationList);
    m_registrationList = null;
  }

  /**
   * Tests shared variable map notifications with following setup:
   * <ul>
   * <li>Offline default = true</li>
   * <li>Offline current thread = null</li>
   * </ul>
   * A client notification should be sent in this case.
   */
  @Test
  public void testDefaultOffline() {
    IClientNotificationService svc = SERVICES.getService(IClientNotificationService.class);

    assertNull("No existing shared variable expected.", m_serverSession.getMySharedVariable());
    assertTrue("No existing client notifications expected.", svc.getNextNotifications(0).length == 0);

    OfflineState.setOfflineDefault(true);
    OfflineState.setOfflineInCurrentThread(null);
    m_serverSession.setMySharedVariable(new Object());

    assertNotNull("Shared variable should be set.", m_serverSession.getMySharedVariable());
    assertTrue("At least one client notification expected.", svc.getNextNotifications(0).length > 0);
  }

  /**
   * Tests shared variable map notifications with following setup:
   * <ul>
   * <li>Offline default = true</li>
   * <li>Offline current thread = true</li>
   * </ul>
   * A client notification should be sent in this case.
   */
  @Test
  public void testDefaultOfflineThreadOffline() {
    IClientNotificationService svc = SERVICES.getService(IClientNotificationService.class);

    assertNull("No existing shared variable expected.", m_serverSession.getMySharedVariable());
    assertTrue("No existing client notifications expected.", svc.getNextNotifications(0).length == 0);

    OfflineState.setOfflineDefault(true);
    OfflineState.setOfflineInCurrentThread(true);
    m_serverSession.setMySharedVariable(new Object());

    assertNotNull("Shared variable should be set.", m_serverSession.getMySharedVariable());
    assertTrue("At least one client notification expected.", svc.getNextNotifications(0).length > 0);
  }

  /**
   * Tests shared variable map notifications with following setup:
   * <ul>
   * <li>Offline default = true</li>
   * <li>Offline current thread = false</li>
   * </ul>
   * No client notification should be sent in this case.
   */
  @Test
  public void testDefaultOfflineThreadOnline() {
    IClientNotificationService svc = SERVICES.getService(IClientNotificationService.class);

    assertNull("No existing shared variable expected.", m_serverSession.getMySharedVariable());
    assertTrue("No existing client notifications expected.", svc.getNextNotifications(0).length == 0);

    OfflineState.setOfflineDefault(true);
    OfflineState.setOfflineInCurrentThread(false);
    m_serverSession.setMySharedVariable(new Object());

    assertNotNull("Shared variable should be set.", m_serverSession.getMySharedVariable());
    assertTrue("No new client notification expected.", svc.getNextNotifications(0).length == 0);
  }

  /**
   * Tests shared variable map notifications with following setup:
   * <ul>
   * <li>Offline default = false</li>
   * <li>Offline current thread = null</li>
   * </ul>
   * A client notification should be sent in this case.
   */
  @Test
  public void testDefaultOnline() {
    IClientNotificationService svc = SERVICES.getService(IClientNotificationService.class);

    assertNull("No existing shared variable expected.", m_serverSession.getMySharedVariable());
    assertTrue("No existing client notifications expected.", svc.getNextNotifications(0).length == 0);

    OfflineState.setOfflineDefault(false);
    OfflineState.setOfflineInCurrentThread(null);
    m_serverSession.setMySharedVariable(new Object());

    assertNotNull("Shared variable should be set.", m_serverSession.getMySharedVariable());
    assertTrue("At least one client notification expected.", svc.getNextNotifications(0).length > 0);
  }

  /**
   * Tests shared variable map notifications with following setup:
   * <ul>
   * <li>Offline default = false</li>
   * <li>Offline current thread = false</li>
   * </ul>
   * A client notification should be sent in this case.
   */
  @Test
  public void testDefaultOnlineThreadOnline() {
    IClientNotificationService svc = SERVICES.getService(IClientNotificationService.class);

    assertNull("No existing shared variable expected.", m_serverSession.getMySharedVariable());
    assertTrue("No existing client notifications expected.", svc.getNextNotifications(0).length == 0);

    OfflineState.setOfflineDefault(false);
    OfflineState.setOfflineInCurrentThread(false);
    m_serverSession.setMySharedVariable(new Object());

    assertNotNull("Shared variable should be set.", m_serverSession.getMySharedVariable());
    assertTrue("At least one client notification expected.", svc.getNextNotifications(0).length > 0);
  }

  /**
   * Tests shared variable map notifications with following setup:
   * <ul>
   * <li>Offline default = false</li>
   * <li>Offline current thread = true</li>
   * </ul>
   * No client notification should be sent in this case.
   */
  @Test
  public void testDefaultOnlineThreadOffline() {
    IClientNotificationService svc = SERVICES.getService(IClientNotificationService.class);

    assertNull("No existing shared variable expected.", m_serverSession.getMySharedVariable());
    assertTrue("No existing client notifications expected.", svc.getNextNotifications(0).length == 0);

    OfflineState.setOfflineDefault(false);
    OfflineState.setOfflineInCurrentThread(true);
    m_serverSession.setMySharedVariable(new Object());

    assertNotNull("Shared variable should be set.", m_serverSession.getMySharedVariable());
    assertTrue("No new client notification expected.", svc.getNextNotifications(0).length == 0);
  }

  /**
   * A server session with access to a shared variable for testing purposes.
   */
  private static class TestServerSession extends AbstractServerSession {
    private static final String KEY_MY_SHARED_VARIABLE = "mySharedVariable";

    public TestServerSession() {
      // make sure initConfig is called in super class constructor
      super(true);
    }

    public void setMySharedVariable(Object value) {
      setSharedContextVariable(KEY_MY_SHARED_VARIABLE, Object.class, value);
    }

    public Object getMySharedVariable() {
      return getSharedContextVariable(KEY_MY_SHARED_VARIABLE, Object.class);
    }
  }

  /**
   * A client notification service for testing purposes.
   */
  private static class MockClientNotificationService extends AbstractService implements IClientNotificationService {
    private List<IClientNotification> m_notifications;

    public MockClientNotificationService() {
      m_notifications = new LinkedList<IClientNotification>();
    }

    @Override
    public IClientNotification[] getNextNotifications(long blockingTimeout) {
      IClientNotification[] result = m_notifications.toArray(new IClientNotification[m_notifications.size()]);
      m_notifications.clear();
      return result;
    }

    @Override
    public void putNotification(IClientNotification notification, IClientNotificationFilter filter) {
      m_notifications.add(notification);
    }

    @Override
    public void addClientNotificationQueueListener(IClientNotificationQueueListener listener) {
      throw new AssertionError("Should not be called during this test");
    }

    @Override
    public void removeClientNotificationQueueListener(IClientNotificationQueueListener listener) {
      throw new AssertionError("Should not be called during this test");
    }

  }
}
