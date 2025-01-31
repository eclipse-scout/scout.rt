/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.basic.tree;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineEventBuffer;
import org.eclipse.scout.rt.platform.BEANS;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link OutlineEventBuffer}
 */
public class OutlineEventBufferTest {

  private OutlineEventBuffer m_testBuffer;
  private Map<String, ITreeNode> m_mockNodes;

  @Before
  public void setup() {
    m_testBuffer = BEANS.get(OutlineEventBuffer.class);
    m_mockNodes = new HashMap<>();
  }

  /**
   * Only the last selection event must be kept.
   */
  @Test
  public void testSelections() {
    final OutlineEvent e1 = new OutlineEvent(mock(IOutline.class), OutlineEvent.TYPE_PAGE_CHANGED, mockNode("A"), true);
    final OutlineEvent e2 = new OutlineEvent(mock(IOutline.class), OutlineEvent.TYPE_PAGE_CHANGED, mockNode("A"), true);
    m_testBuffer.add(e1);
    m_testBuffer.add(e2);
    final List<TreeEvent> coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(1, coalesced.size());
    assertSame(e1, coalesced.get(0));
  }

  private ITreeNode mockNode(String nodeId) {
    ITreeNode node = m_mockNodes.get(nodeId);
    if (node != null) {
      return node;
    }
    // Create a new
    node = mock(ITreeNode.class, "MockNode[" + nodeId + "]");
    when(node.getNodeId()).thenReturn(nodeId);
    m_mockNodes.put(nodeId, node);
    return node;
  }
}
