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
package org.eclipse.scout.rt.client.ui.basic.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link TreeEventBuffer}
 */
public class TreeEventBufferTest {

  private TreeEventBuffer m_testBuffer;
  private Map<String, ITreeNode> m_mockNodes;

  @Before
  public void setup() {
    m_testBuffer = new TreeEventBuffer();
    m_mockNodes = new HashMap<>();
  }

  /**
   * Some events should not be coalesced: selected, updated, row_action.
   */
  @Test
  public void testNoCoalesce() {
    final TreeEvent e1 = mockEvent(TreeEvent.TYPE_NODE_ACTION, 1);
    final TreeEvent e2 = mockEvent(TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED, 1);
    final TreeEvent e3 = mockEvent(TreeEvent.TYPE_NODES_DRAG_REQUEST, 1);
    m_testBuffer.add(e1);
    m_testBuffer.add(e2);
    m_testBuffer.add(e3);
    final List<TreeEvent> coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(3, coalesced.size());
    assertSame(e1, coalesced.get(0));
    assertSame(e2, coalesced.get(1));
    assertSame(e3, coalesced.get(2));
  }

  /**
   * Only the last selection event must be kept.
   */
  @Test
  public void testSelections() {
    final TreeEvent e1 = mockEvent(TreeEvent.TYPE_NODES_SELECTED, 1);
    final TreeEvent e2 = mockEvent(TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED, 1);
    final TreeEvent e3 = mockEvent(TreeEvent.TYPE_NODES_SELECTED, 1);
    final TreeEvent e4 = mockEvent(TreeEvent.TYPE_NODES_SELECTED, 1);
    m_testBuffer.add(e1);
    m_testBuffer.add(e2);
    m_testBuffer.add(e3);
    m_testBuffer.add(e4);
    final List<TreeEvent> coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(2, coalesced.size());
    assertSame(e2, coalesced.get(0));
    assertSame(e4, coalesced.get(1));
  }

  /**
   * Consecutive update events are coalesced
   */
  @Test
  public void testUpdateCoalesce() {
    final TreeEvent e1 = mockEvent(TreeEvent.TYPE_NODES_UPDATED, "A", "B", "C");
    final TreeEvent e2 = mockEvent(TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED, "C", "B", "A");
    final TreeEvent e3 = mockEvent(TreeEvent.TYPE_NODES_UPDATED, "B");
    final TreeEvent e4 = mockEvent(TreeEvent.TYPE_NODES_UPDATED, "C", "B", "D");
    m_testBuffer.add(e1);
    m_testBuffer.add(e2);
    m_testBuffer.add(e3);
    m_testBuffer.add(e4);
    final List<TreeEvent> coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(3, coalesced.size());
    assertEquals(TreeEvent.TYPE_NODES_UPDATED, coalesced.get(0).getType());
    assertEquals(3, coalesced.get(0).getNodes().size());
    assertEquals(TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED, coalesced.get(1).getType());
    assertEquals(3, coalesced.get(1).getNodes().size());
    assertEquals(TreeEvent.TYPE_NODES_UPDATED, coalesced.get(2).getType());
    assertEquals(3, coalesced.get(2).getNodes().size());
  }

  /**
   * Updates are merged into insert
   */
  @Test
  public void testUpdateMergedIntoInsert() {
    final TreeEvent e1 = mockEvent(TreeEvent.TYPE_NODES_INSERTED, "A", "B", "C");
    final TreeEvent e2 = mockEvent(TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED, "C", "B", "A");
    final TreeEvent e3 = mockEvent(TreeEvent.TYPE_NODES_UPDATED, "B");
    final TreeEvent e4 = mockEvent(TreeEvent.TYPE_NODES_UPDATED, "C", "D");
    m_testBuffer.add(e1);
    m_testBuffer.add(e2);
    m_testBuffer.add(e3);
    m_testBuffer.add(e4);
    final List<TreeEvent> coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(3, coalesced.size());
    assertEquals(TreeEvent.TYPE_NODES_INSERTED, coalesced.get(0).getType());
    assertEquals(3, coalesced.get(0).getNodes().size());
    assertEquals(TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED, coalesced.get(1).getType());
    assertEquals(3, coalesced.get(1).getNodes().size());
    assertEquals(TreeEvent.TYPE_NODES_UPDATED, coalesced.get(2).getType());
    assertEquals(1, coalesced.get(2).getNodes().size());
  }

  /**
   * Insert/Update/Delete => cleared
   */
  @Test
  public void testInsertUpdateDeleteInSameRequest() {
    final TreeEvent e1 = mockEvent(TreeEvent.TYPE_NODES_INSERTED, "A", "B", "C");
    final TreeEvent e2 = mockEvent(TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED, "C", "B", "A");
    final TreeEvent e3 = mockEvent(TreeEvent.TYPE_NODES_UPDATED, "B");
    final TreeEvent e4 = mockEvent(TreeEvent.TYPE_NODES_DELETED, "A", "D", "B", "C");
    m_testBuffer.add(e1);
    m_testBuffer.add(e2);
    m_testBuffer.add(e3);
    m_testBuffer.add(e4);
    final List<TreeEvent> coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(1, coalesced.size());
    assertEquals(TreeEvent.TYPE_NODES_DELETED, coalesced.get(0).getType());
    assertEquals(1, coalesced.get(0).getNodes().size());
  }

  @SuppressWarnings("unused")
  private TreeEvent mockEvent(int type) {
    return mockEvent(type, 0);
  }

  private TreeEvent mockEvent(int type, int nodeCount) {
    List<ITreeNode> nodes = null;
    if (nodeCount > 0) {
      nodes = new ArrayList<>();
      for (int i = 0; i < nodeCount; i++) {
        nodes.add(mockNode("N" + i));
      }
    }
    return new TreeEvent(mock(ITree.class), type, nodes);
  }

  private TreeEvent mockEvent(int type, String... nodeIds) {
    return new TreeEvent(mock(ITree.class), type, mockNodes(nodeIds));
  }

  private List<ITreeNode> mockNodes(String... nodeIds) {
    if (nodeIds == null) {
      return null;
    }
    List<ITreeNode> rows = new ArrayList<>();
    for (String nodeId : nodeIds) {
      rows.add(mockNode(nodeId));
    }
    return rows;
  }

  private ITreeNode mockNode(String nodeId) {
    ITreeNode node = m_mockNodes.get(nodeId);
    if (node != null) {
      return node;
    }
    // Create a new
    node = mock(ITreeNode.class);
    when(node.getNodeId()).thenReturn(nodeId);
    m_mockNodes.put(nodeId, node);
    return node;
  }
}
