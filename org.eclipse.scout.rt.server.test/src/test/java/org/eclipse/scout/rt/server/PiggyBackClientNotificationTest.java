/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;
import org.eclipse.scout.rt.shared.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelRequest;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelResponse;
import org.eclipse.scout.rt.testing.platform.mock.BeanMock;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;

/**
 * Tests that transactional client notifications are transferred with the response.
 *
 * @author jgu
 * @see ServiceTunnelServlet
 */
@RunWith(PlatformTestRunner.class)
public class PiggyBackClientNotificationTest {

  @BeanMock
  IPingService m_pingSvc;

  @Before
  public void before() {
    BEANS.get(IClientNotificationService.class).registerNode(NodeId.of("test"));
    when(m_pingSvc.ping(any(String.class))).thenAnswer((Answer<String>) invocation -> {
      BEANS.get(ClientNotificationRegistry.class).putTransactionalForAllSessions("testNotification");
      return "pong";
    });
  }

  @Test
  public void testPiggyBack() {
    ServiceTunnelServlet s = new ServiceTunnelServlet();
    Class[] parameterTypes = new Class[]{String.class};
    Object[] args = new Object[]{"test"};
    ServiceTunnelRequest req = new ServiceTunnelRequest(IPingService.class.getName(), "ping", parameterTypes, args);
    req.setClientNodeId(NodeId.of("testNodeId"));
    ServiceTunnelResponse res = s.doPost(req);
    assertEquals("pong", res.getData());
    assertNull(res.getException());
    assertEquals(1, res.getNotifications().size());
    assertEquals("testNotification", res.getNotifications().get(0).getNotification());
  }

}
