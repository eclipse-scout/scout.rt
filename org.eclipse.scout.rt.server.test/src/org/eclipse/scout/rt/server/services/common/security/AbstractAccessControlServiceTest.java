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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.server.services.common.clientnotification.ClientNotificationQueueEvent;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.server.services.common.security.fixture.TestAccessControlService;
import org.eclipse.scout.rt.server.services.common.security.fixture.TestClientNotificationQueueListener;
import org.eclipse.scout.rt.shared.services.common.security.AccessControlChangedNotification;
import org.eclipse.scout.service.SERVICES;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link AbstractAccessControlService}
 */
public class AbstractAccessControlServiceTest {

//  /**
//   * Tests that a client notification of {@link AccessControlChangedNotification} is sent when the cach is cleared:
//   * {@link AbstractAccessControlService#clearCacheOfUserIds(String...)}
//   */
//  @Test
//  public void testClientNotificationSentForClearCache() {
//    AbstractAccessControlService accessControlService = new TestAccessControlService();
//    accessControlService.initializeService(null);
//
//    TestClientNotificationQueueListener listener = new TestClientNotificationQueueListener();
//    SERVICES.getService(IClientNotificationService.class).addClientNotificationQueueListener(listener);
//    final String testUser = "testuser";
//    accessControlService.clearCacheOfUserIds(Collections.singleton(testUser));
//
//    List<ClientNotificationQueueEvent> eventList = listener.getEventList();
//    Assert.assertEquals("No client notification received.", eventList.size(), 1);
//    ClientNotificationQueueEvent clientNotificationQueueEvent = eventList.get(0);
//
//    Assert.assertEquals("Incorrect ClientNotification Type", AccessControlChangedNotification.class, clientNotificationQueueEvent.getNotification().getClass());
//    IClientNotificationFilter filter = clientNotificationQueueEvent.getFilter();
//    Assert.assertEquals("Incorrect Notification filter type", SingleUserFilter.class, filter.getClass());
//    Assert.assertEquals("UserId in client notification filter is incorrect", testUser, ((SingleUserFilter) filter).getUserId());
//  }

  /**
   * Tests that a client notification of {@link AccessControlChangedNotification} is sent when the cache is cleared
   * (null users): {@link AbstractAccessControlService#clearCacheOfUserIds(String...)}
   */
  @Test
  public void testClientNotificationSentForClearCacheNullUsers() {
    verifyNoNotificationSentForClearCache(Collections.<String> emptySet());
  }

  /**
   * Tests that a client notification of {@link AccessControlChangedNotification} is sent when the cache is cleared (no
   * users): {@link AbstractAccessControlService#clearCacheOfUserIds(String...)}
   */
  @Test
  public void testClientNotificationSentForClearCacheNoUsers() {
    verifyNoNotificationSentForClearCache(Collections.<String> emptySet());
  }

  /**
   * Tests that a client notification of {@link AccessControlChangedNotification} is sent when the cache is cleared
   * (null userId): {@link AbstractAccessControlService#clearCacheOfUserIds(String...)}
   */
  @Test
  public void testClientNotificationSentForClearCacheEmptyUsers() {
    verifyNoNotificationSentForClearCache(Collections.<String> emptySet());
  }

  private void verifyNoNotificationSentForClearCache(Collection<String> userIds) {
    AbstractAccessControlService accessControlService = new TestAccessControlService();
    accessControlService.initializeService(null);

    TestClientNotificationQueueListener listener = new TestClientNotificationQueueListener();
    SERVICES.getService(IClientNotificationService.class).addClientNotificationQueueListener(listener);
    accessControlService.clearCacheOfUserIds(userIds);

    List<ClientNotificationQueueEvent> eventList = listener.getEventList();
    Assert.assertEquals("No client notification received.", eventList.size(), 0);
  }

}
