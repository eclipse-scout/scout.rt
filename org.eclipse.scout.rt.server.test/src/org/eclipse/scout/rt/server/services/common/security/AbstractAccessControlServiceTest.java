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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.server.ThreadContext;
import org.eclipse.scout.rt.server.services.common.clientnotification.ClientNotificationQueueEvent;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationFilter;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.server.services.common.clientnotification.SingleUserFilter;
import org.eclipse.scout.rt.server.services.common.security.fixture.TestAccessControlService;
import org.eclipse.scout.rt.server.services.common.security.fixture.TestClientNotificationQueueListener;
import org.eclipse.scout.rt.shared.services.common.security.AccessControlChangedNotification;
import org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner;
import org.eclipse.scout.service.SERVICES;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link AbstractAccessControlService}
 */
@RunWith(ScoutServerTestRunner.class)
public class AbstractAccessControlServiceTest {
  AbstractAccessControlService m_accessControlService;
  TestClientNotificationQueueListener m_listener;

  @Before
  public void setup() {
    m_accessControlService = new TestAccessControlService();
    m_accessControlService.initializeService(null);
    m_listener = new TestClientNotificationQueueListener();
    SERVICES.getService(IClientNotificationService.class).addClientNotificationQueueListener(m_listener);
  }

  /**
   * Tests that a client notification of {@link AccessControlChangedNotification} is sent when the cach is cleared:
   * {@link AbstractAccessControlService#clearCacheOfUserIds(String...)}
   */
  @Test
  public void testClientNotificationSentForClearCache() {
    final String testUser = "testuser";
    Set<String> testUsers = Collections.singleton(testUser);
    clearCache(testUsers);

    List<ClientNotificationQueueEvent> eventList = m_listener.getEventList();
    assertEquals("No client notification received.", eventList.size(), 1);

    ClientNotificationQueueEvent clientNotificationQueueEvent = eventList.get(0);
    assertEquals("Incorrect ClientNotification Type", AccessControlChangedNotification.class, clientNotificationQueueEvent.getNotification().getClass());
    IClientNotificationFilter filter = clientNotificationQueueEvent.getFilter();
    assertEquals("Incorrect Notification filter type", SingleUserFilter.class, filter.getClass());
    assertEquals("UserId in client notification filter is incorrect", testUser, ((SingleUserFilter) filter).getUserId());
  }

  /**
   * Tests that a client notification of {@link AccessControlChangedNotification} is sent when the cache is cleared (no
   * users): {@link AbstractAccessControlService#clearCacheOfUserIds(String...)}
   */
  @Test
  public void testClientNotificationSentForClearCacheNoUsers() {
    clearCache(Collections.<String> emptySet());
    verifyNoNotificationsSent();
  }

  private void clearCache(Collection<String> testUsers) {
    m_accessControlService.clearCacheOfUserIds(testUsers);
    ThreadContext.getTransaction().commitPhase2();
  }

  private void verifyNoNotificationsSent() {
    assertEquals("No client notification received.", m_listener.getEventList().size(), 0);
  }

}
