/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.clustersync;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.server.ServerJobService;
import org.eclipse.scout.rt.server.ThreadContext;
import org.eclipse.scout.rt.server.internal.Activator;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationMessage;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationMessageProperties;
import org.eclipse.scout.rt.testing.server.runner.ScoutServerTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.ServiceRegistration;

/**
 * Tests for {@link ClusterSynchronizationService}
 */
@RunWith(ScoutServerTestRunner.class)
public class ClusterSynchronizationServiceTest {
  private static final String TEST_NODE = "node";
  private static final String TEST_USER = "user";
  private ClusterNotificationMessage m_message;
  private List<ServiceRegistration> m_regs;

  @Before
  public void setup() {
    m_message = mock(ClusterNotificationMessage.class);
    when(m_message.getProperties()).thenReturn(new ClusterNotificationMessageProperties(TEST_NODE, TEST_USER));
    final IPublishSubscribeMessageService ps = mock(IPublishSubscribeMessageService.class);
    ServerJobService js = new ServerJobService();
    js.setServerSessionClassName("org.eclipse.scout.rt.server.TestServerSession");
    m_regs = TestingUtility.registerServices(Activator.getDefault().getBundle(), 9000, ps, js);
  }

  @After
  public void tearDown() {
    TestingUtility.unregisterServices(m_regs);
  }

  /**
   * Tests that the statusInfo is updated up on receipt of a message.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testReveiveInfoUpdated() throws ProcessingException {
    ClusterSynchronizationService svc = new ClusterSynchronizationService();
    svc.initializeService(null);
    svc.enable();
    svc.onMessage(m_message);
    ClusterNodeStatusInfo nodeInfo = svc.getClusterNodeStatusInfo();
    assertEquals(1, nodeInfo.getReceivedMessageCount());
    assertEquals(0, nodeInfo.getSentMessageCount());
    assertEquals(TEST_NODE, nodeInfo.getLastChangedOriginNodeId());
  }

  /**
   * Tests that the statusInfo is updated after sending a message.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testSendInfoUpdated() throws ProcessingException {
    ClusterSynchronizationService svc = new ClusterSynchronizationService();
    svc.initializeService(null);
    svc.enable();
    final IClusterNotification notification = mock(IClusterNotification.class);
    svc.publishNotification(notification);
    ThreadContext.getTransaction().commitPhase1();
    ThreadContext.getTransaction().commitPhase2();

    ClusterNodeStatusInfo nodeInfo = svc.getClusterNodeStatusInfo();
    assertEquals(0, nodeInfo.getReceivedMessageCount());
    assertEquals(1, nodeInfo.getSentMessageCount());
    assertEquals(svc.getNodeId(), nodeInfo.getLastChangedOriginNodeId());
  }
}
