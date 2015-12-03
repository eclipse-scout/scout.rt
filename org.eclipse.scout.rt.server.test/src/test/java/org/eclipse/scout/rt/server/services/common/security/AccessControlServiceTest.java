/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.List;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.internal.BeanInstanceUtil;
import org.eclipse.scout.rt.server.TestServerSession;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationProperties.MaxNotificationBlockingTimeOut;
import org.eclipse.scout.rt.server.services.common.security.fixture.TestPermission1;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.cache.InvalidateCacheNotification;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.shared.security.RemoteServiceAccessPermission;
import org.eclipse.scout.rt.shared.services.common.security.AbstractAccessControlService;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.services.common.security.UserIdAccessControlService;
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
   * Tests that a client notification of {@link InvalidateCacheNotification} is sent when the cache is cleared:
   * {@link IAccessControlService#clearCache()}
   */
  @Test
  public void testClientNotificationSentForClearCache() throws ProcessingException {
    m_accessControlService.clearCache();
    commit();

    assertSingleNotification();
  }

  /**
   * Tests that a client notification of {@link InvalidateCacheNotification} is sent when the cache is cleared:
   * {@link IAccessControlService#clearCacheOfCurrentUser()}
   */
  @Test
  public void testClientNotificationSentForClearCacheOfCurrentUser() throws ProcessingException {
    m_accessControlService.clearCacheOfCurrentUser();
    commit();

    assertSingleNotification();
  }

  private void commit() {
    ITransaction.CURRENT.get().commitPhase2();
  }

  private void assertSingleNotification() {
    final List<ClientNotificationMessage> notifications = getNotifications();
    assertEquals(1, notifications.size());
    assertEquals(InvalidateCacheNotification.class, notifications.get(0).getNotification().getClass());
  }

  private List<ClientNotificationMessage> getNotifications() {
    return BEANS.get(IClientNotificationService.class).getNotifications("testNode");
  }

  /**
   * An access control service with {@link TestPermission1} for testing
   */
  @IgnoreBean
  private static class TestAccessControlService extends UserIdAccessControlService {

    @Override
    protected PermissionCollection execLoadPermissions(String userId) {
      Permissions permissions = new Permissions();
      permissions.add(new TestPermission1());
      permissions.add(new RemoteServiceAccessPermission("*.shared.*", "*"));
      return permissions;
    }
  }

  class TestBlockingProperty extends MaxNotificationBlockingTimeOut {
    @Override
    protected Integer getDefaultValue() {
      return 1;
    }
  }
}
