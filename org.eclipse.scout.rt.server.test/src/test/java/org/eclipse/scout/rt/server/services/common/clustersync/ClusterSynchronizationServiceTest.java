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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.server.TestServerSession;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationMessage;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationMessageProperties;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.testing.platform.runner.RunWithSubject;
import org.eclipse.scout.rt.testing.server.runner.RunWithServerSession;
import org.eclipse.scout.rt.testing.server.runner.ServerTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link ClusterSynchronizationService}
 */
@RunWith(ServerTestRunner.class)
@RunWithServerSession(TestServerSession.class)
@RunWithSubject("default")
public class ClusterSynchronizationServiceTest {
  private static final String TEST_NODE = "node";
  private static final String TEST_USER = "user";

  private ClusterNotificationMessage m_message;
  private List<IBean<?>> m_beans = new ArrayList<>();

  @Before
  public void before() {
    m_message = mock(ClusterNotificationMessage.class);
    when(m_message.getProperties()).thenReturn(new ClusterNotificationMessageProperties(TEST_NODE, TEST_USER));

    final IPublishSubscribeMessageService ps = mock(IPublishSubscribeMessageService.class);
    m_beans.add(
        TestingUtility.registerBean(
            new BeanMetaData(IPublishSubscribeMessageService.class).
                initialInstance(ps).
                applicationScoped(true)
            ));
  }

  @After
  public void tearDown() {
    TestingUtility.unregisterBeans(m_beans);
  }

  /**
   * Tests that the statusInfo is updated up on receipt of a message.
   */
  @Test
  public void testReveiveInfoUpdated() throws ProcessingException {
    ClusterSynchronizationService svc = new ClusterSynchronizationService();
    svc.initializeService();
    svc.enable();
    svc.onMessage(m_message);
    IClusterNodeStatusInfo nodeInfo = svc.getStatusInfo();
    assertEquals(1, nodeInfo.getReceivedMessageCount());
    assertEquals(0, nodeInfo.getSentMessageCount());
    assertEquals(TEST_NODE, nodeInfo.getLastChangedOriginNodeId());
  }

  /**
   * Tests that the statusInfo is updated after sending a message.
   */
  @Test
  public void testSendInfoUpdated() throws ProcessingException {
    ClusterSynchronizationService svc = new ClusterSynchronizationService();
    svc.initializeService();
    svc.enable();
    final IClusterNotification notification = mock(IClusterNotification.class);
    svc.publishNotification(notification);
    ITransaction.CURRENT.get().commitPhase1();
    ITransaction.CURRENT.get().commitPhase2();

    IClusterNodeStatusInfo nodeInfo = svc.getStatusInfo();
    assertEquals(0, nodeInfo.getReceivedMessageCount());
    assertEquals(1, nodeInfo.getSentMessageCount());
    assertEquals(svc.getNodeId(), nodeInfo.getLastChangedOriginNodeId());
    assertEquals("default", nodeInfo.getLastChangedUserId());
  }
}
