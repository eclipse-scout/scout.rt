/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.services.common.clustersync;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationMessage;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationProperties;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link ClusterNodeStatusInfo}
 */
public class ClusterNodeStatusInfoTest {
  private static final NodeId TEST_NODE = NodeId.of("node");
  private static final String TEST_USER = "user";
  private ClusterNotificationMessage m_message;

  @Before
  public void setup() {
    m_message = mock(ClusterNotificationMessage.class);
    when(m_message.getProperties()).thenReturn(new ClusterNotificationProperties(TEST_NODE, TEST_USER));
  }

  @Test
  public void testReceiveStatus() {
    ClusterNodeStatusInfo info = new ClusterNodeStatusInfo();
    info.updateReceiveStatus(m_message);
    IClusterNodeStatusInfo current = info.getStatus();
    assertEquals(1L, current.getReceivedMessageCount());
    assertEquals(0L, current.getSentMessageCount());
    assertEquals(TEST_NODE, current.getLastChangedOriginNodeId());
    assertEquals(TEST_USER, current.getLastChangedUserId());
    assertNotNull(current.getLastChangedDate());
  }

  @Test
  public void testSentStatus() {
    ClusterNodeStatusInfo info = new ClusterNodeStatusInfo();
    info.updateSentStatus(m_message);
    IClusterNodeStatusInfo current = info.getStatus();
    assertEquals(0L, current.getReceivedMessageCount());
    assertEquals(1L, current.getSentMessageCount());
    assertEquals(TEST_NODE, current.getLastChangedOriginNodeId());
    assertEquals(TEST_USER, current.getLastChangedUserId());
    assertNotNull(current.getLastChangedDate());
  }
}
