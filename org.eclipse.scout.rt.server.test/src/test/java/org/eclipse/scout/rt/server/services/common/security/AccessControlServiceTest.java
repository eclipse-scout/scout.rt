/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.services.common.security;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.cache.ICacheBuilder;
import org.eclipse.scout.rt.platform.cache.InvalidateCacheNotification;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.internal.BeanInstanceUtil;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.security.AbstractAccessControlService;
import org.eclipse.scout.rt.security.DefaultPermissionCollection;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.security.IPermissionCollection;
import org.eclipse.scout.rt.security.PermissionLevel;
import org.eclipse.scout.rt.server.TestServerSession;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationProperties.MaxNotificationBlockingTimeOut;
import org.eclipse.scout.rt.server.services.common.security.fixture.TestPermission1;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.shared.security.RemoteServiceAccessPermission;
import org.eclipse.scout.rt.shared.session.Sessions;
import org.eclipse.scout.rt.testing.platform.BeanTestingHelper;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
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
    m_accessControlService = BeanInstanceUtil.createBean(TestAccessControlService.class);
    BeanInstanceUtil.initializeBeanInstance(m_accessControlService);

    //Register this IAccessControlService with a higher priority than AllAccessControlService registered in CustomServerTestEnvironment
    m_registerServices = BeanTestingHelper.get().registerBeans(
        new BeanMetaData(IAccessControlService.class).withInitialInstance(m_accessControlService).withApplicationScoped(true),
        new BeanMetaData(MaxNotificationBlockingTimeOut.class).withInitialInstance(new TestBlockingProperty()).withApplicationScoped(true));

    //register test session
    final IClientNotificationService registry = BEANS.get(IClientNotificationService.class);
    registry.registerNode(NodeId.of("testNode"));
  }

  @After
  public void after() {
    BeanTestingHelper.get().unregisterBeans(m_registerServices);
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
    ITransaction transaction = ITransaction.CURRENT.get();
    transaction.commitPhase2();
    transaction.release();
  }

  private void assertSingleNotification() {
    final List<ClientNotificationMessage> notifications = getNotifications();
    assertEquals(1, notifications.size());
    assertEquals(InvalidateCacheNotification.class, notifications.get(0).getNotification().getClass());
  }

  private List<ClientNotificationMessage> getNotifications() {
    return BEANS.get(IClientNotificationService.class).getNotifications(NodeId.of("testNode"));
  }

  /**
   * An access control service with {@link TestPermission1} for testing
   */
  @IgnoreBean
  private static class TestAccessControlService extends AbstractAccessControlService<String> {

    @Override
    protected String getCurrentUserCacheKey() {
      return Sessions.getCurrentUserId();
    }

    @Override
    protected IPermissionCollection execLoadPermissions(String userId) {
      DefaultPermissionCollection permissions = BEANS.get(DefaultPermissionCollection.class);
      permissions.add(new TestPermission1(), PermissionLevel.ALL);
      permissions.add(new RemoteServiceAccessPermission("*.shared.*", "*"), PermissionLevel.ALL);
      permissions.setReadOnly();
      return permissions;
    }

    @Override
    protected ICacheBuilder<String, IPermissionCollection> createCacheBuilder() {
      return super.createCacheBuilder()
          .withCacheId(ACCESS_CONTROL_SERVICE_CACHE_ID + ".for.test")
          .withReplaceIfExists(true);
    }
  }

  @IgnoreBean
  private static class TestBlockingProperty extends MaxNotificationBlockingTimeOut {
    @Override
    public Integer getDefaultValue() {
      return 1;
    }
  }
}
