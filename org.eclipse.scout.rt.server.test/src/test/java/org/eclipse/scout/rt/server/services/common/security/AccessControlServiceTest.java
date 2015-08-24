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
package org.eclipse.scout.rt.server.services.common.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.security.Permissions;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.internal.BeanInstanceUtil;
import org.eclipse.scout.rt.server.TestServerSession;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationProperties.MaxNotificationBlockingTimeOut;
import org.eclipse.scout.rt.server.services.common.security.fixture.TestPermission1;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.shared.security.RemoteServiceAccessPermission;
import org.eclipse.scout.rt.shared.services.common.security.AccessControlChangedNotification;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.services.common.security.ResetAccessControlChangedNotification;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link AbstractAccessControlService}
 */
@RunWith(ServerTestRunner.class)
@RunWithServerSession(TestServerSession.class)
@RunWithSubject("john")
public class AccessControlServiceTest {
  private TestAccessControlService m_accessControlService;
  private List<IBean<?>> m_registerServices;
  private static final String TEST_USER = "testuser";

  @Before
  public void setup() {
    m_accessControlService = BeanInstanceUtil.create(TestAccessControlService.class);

    //Register this IAccessControlService with an higher priority than AllAccessControlService registered in CustomServerTestEnvironment
    m_registerServices = TestingUtility.registerBeans(
        new BeanMetaData(IAccessControlService.class).withInitialInstance(m_accessControlService).withApplicationScoped(true),
        new BeanMetaData(MaxNotificationBlockingTimeOut.class).withInitialInstance(new TestBlockingProperty()).withApplicationScoped(true));

    //register test session
    final IClientNotificationService registry = BEANS.get(IClientNotificationService.class);
    registry.registerSession("testNode", "testSession", "testuser");
  }

  @After
  public void after() {
    TestingUtility.unregisterBeans(m_registerServices);
  }

  /**
   * Tests that a client notification of {@link AccessControlChangedNotification} is sent when permissions are loaded
   * {@link AbstractAccessControlService#getPermissions()}
   */
  @Test
  @RunWithSubject("testuser")
  public void testClientNotificationSentForGetPermissions() {
    m_accessControlService.getPermissions();
    commit();

    assertSingleNotificationWithPermissions();
  }

  /**
   * Tests that a client notification of {@link ResetAccessControlChangedNotification} is sent when the cache is
   * cleared: {@link AbstractAccessControlService#clearCache()}
   */
  @Test
  public void testClientNotificationSentForClearCache() {
    m_accessControlService.clearCache();
    commit();

    assertSingleNotification(ResetAccessControlChangedNotification.class);
  }

  /**
   * Tests that no client notification at all is sent when the cache is cleared:
   * {@link AbstractAccessControlService#clearCacheNoFire()}
   */
  @Test
  public void testClientNotificationSentForClearCacheNoFire() {
    m_accessControlService.callClearCacheNoFire();
    commit();

    assertNoNotification();
  }

  /**
   * Tests that a client notification of {@link AccessControlChangedNotification} is sent when the cache is cleared:
   * {@link AbstractAccessControlService#clearCacheOfUserIds(Collection)}
   */
  @Test
  public void testClientNotificationSentForClearCacheOfUserIds() {
    Set<String> testUsers = Collections.singleton(TEST_USER);
    m_accessControlService.clearCacheOfUserIds(testUsers);
    commit();

    assertSingleNotification(AccessControlChangedNotification.class);
  }

  /**
   * Tests that no client notification at all is sent when the cache is cleared:
   * {@link AbstractAccessControlService#clearCacheOfUserIdsNoFire(Collection)}
   */
  @Test
  public void testClientNotificationSentForClearCacheOfUserIdsNoFire() {
    Set<String> testUsers = Collections.singleton(TEST_USER);
    m_accessControlService.callClearCacheOfUserIdsNoFire(testUsers);
    commit();

    assertNoNotification();
  }

  /**
   * Tests that no client notification at all is sent when the cache is cleared (no users):
   * {@link AbstractAccessControlService#clearCacheOfUserIds(Collection)}
   */
  @Test
  public void testClientNotificationSentForClearCacheNoUsers() {
    m_accessControlService.clearCacheOfUserIds(Collections.<String> emptySet());
    commit();
    assertNoNotification();
  }

  private void commit() {
    ITransaction.CURRENT.get().commitPhase2();
  }

  private void assertSingleNotificationWithPermissions() {
    final List<ClientNotificationMessage> notifications = getNotifications();
    assertEquals(1, notifications.size());
    assertTrue(notifications.get(0).getNotification() instanceof AccessControlChangedNotification);
    AccessControlChangedNotification n = (AccessControlChangedNotification) notifications.get(0).getNotification();
    assertEquals(m_accessControlService.getPermissions(), n.getPermissions());
  }

  private void assertSingleNotification(Class<? extends Serializable> notificationClass) {
    final List<ClientNotificationMessage> notifications = getNotifications();
    assertEquals(1, notifications.size());
    assertEquals(notificationClass, notifications.get(0).getNotification().getClass());
  }

  private void assertNoNotification() {
    final List<ClientNotificationMessage> notifications = getNotifications();
    assertTrue(notifications.isEmpty());
  }

  private List<ClientNotificationMessage> getNotifications() {
    return BEANS.get(IClientNotificationService.class).getNotifications("testNode");
  }

  /**
   * An access control service with {@link TestPermission1} for testing
   */
  private static class TestAccessControlService extends AbstractAccessControlService {
    /**
     * Loads a test permission
     */
    @Override
    protected Permissions execLoadPermissions() {
      Permissions permissions = new Permissions();
      permissions.add(new TestPermission1());
      permissions.add(new RemoteServiceAccessPermission("*.shared.*", "*"));
      return permissions;
    }

    public void callClearCacheNoFire() {
      clearCacheNoFire();
    }

    public void callClearCacheOfUserIdsNoFire(Collection<String> userIds) {
      clearCacheOfUserIdsNoFire(userIds);
    }
  }

  class TestBlockingProperty extends MaxNotificationBlockingTimeOut {
    @Override
    protected Integer getDefaultValue() {
      return 1;
    }
  }
}
