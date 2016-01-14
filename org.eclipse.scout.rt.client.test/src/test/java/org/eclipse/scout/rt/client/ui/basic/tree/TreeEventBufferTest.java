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
package org.eclipse.scout.rt.client.ui.basic.tree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scout.rt.platform.util.CollectionUtility;

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
    final TreeEvent e1 = mockEvent(TreeEvent.TYPE_NODE_ACTION, "A");
    final TreeEvent e2 = mockEvent(TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED, "A");
    final TreeEvent e3 = mockEvent(TreeEvent.TYPE_NODES_DRAG_REQUEST, "A");
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
    final TreeEvent e1 = mockEvent(TreeEvent.TYPE_NODES_SELECTED, "A");
    final TreeEvent e2 = mockEvent(TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED, "D");
    final TreeEvent e3 = mockEvent(TreeEvent.TYPE_NODES_SELECTED, "B");
    final TreeEvent e4 = mockEvent(TreeEvent.TYPE_NODES_SELECTED, "B");
    m_testBuffer.add(e1);
    m_testBuffer.add(e2);
    m_testBuffer.add(e3);
    m_testBuffer.add(e4);
    final List<TreeEvent> coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(2, coalesced.size());
    assertSame(e2, coalesced.get(0));
    assertSame(e3, coalesced.get(1));
  }

  /**
   * Consecutive update events are coalesced
   */
  @Test
  public void testUpdateCoalesce() {
    final TreeEvent e1 = mockEvent(TreeEvent.TYPE_NODES_UPDATED, "A", "B", "C");
    final TreeEvent e2 = mockEvent(TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED, "C", "B", "A");
    final TreeEvent e3 = mockEvent(TreeEvent.TYPE_NODES_UPDATED, "B", "E");
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
    assertEquals(4, coalesced.get(2).getNodes().size());
  }

  /**
   * Insert[A], Delete[A], Insert[B] has to result in a single Insert[B] (not A!)
   */
  @Test
  public void testInsertCoalesce() {
    // A
    // +-B
    // | +-E
    // |   +-F
    // +-C
    //   +-G
    // +-D
    ITreeNode nodeA = mockNode("A");
    ITreeNode nodeB = mockNode("B");
    ITreeNode nodeC = mockNode("C");
    ITreeNode nodeD = mockNode("D");
    ITreeNode nodeE = mockNode("E");
    ITreeNode nodeF = mockNode("F");
    ITreeNode nodeG = mockNode("G");
    installChildNodes(nodeA, nodeB, nodeC, nodeD);
    installChildNodes(nodeB, nodeE);
    installChildNodes(nodeE, nodeF);
    installChildNodes(nodeC, nodeG);

    final TreeEvent e1 = mockEvent(TreeEvent.TYPE_NODES_INSERTED, nodeE);
    final TreeEvent e2 = mockEvent(nodeA, TreeEvent.TYPE_ALL_CHILD_NODES_DELETED, nodeB, nodeC, nodeD);
    final TreeEvent e3 = mockEvent(TreeEvent.TYPE_NODES_INSERTED, nodeC);

    m_testBuffer.add(e1);
    m_testBuffer.add(e2);
    m_testBuffer.add(e3);
    final List<TreeEvent> coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(2, coalesced.size());
    assertEquals(TreeEvent.TYPE_NODES_INSERTED, coalesced.get(1).getType());
    assertEquals(1, coalesced.get(1).getNodes().size());
  }

  /**
   * Consecutive "node changed" events are coalesced
   */
  @Test
  public void testNodeChangedCoalesce() {
    final TreeEvent e1 = mockEvent(TreeEvent.TYPE_NODE_CHANGED, "A");
    final TreeEvent e2 = mockEvent(TreeEvent.TYPE_NODE_CHANGED, "B");
    final TreeEvent e3 = mockEvent(TreeEvent.TYPE_NODE_CHANGED, "C");
    final TreeEvent e4 = mockEvent(TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED, "C", "B", "A");
    final TreeEvent e5 = mockEvent(TreeEvent.TYPE_NODE_CHANGED, "B");
    final TreeEvent e6 = mockEvent(TreeEvent.TYPE_NODE_CHANGED, "E");
    final TreeEvent e7 = mockEvent(TreeEvent.TYPE_NODE_CHANGED, "C");
    final TreeEvent e8 = mockEvent(TreeEvent.TYPE_NODE_CHANGED, "B"); // B is twice in the list (actually three times, but there is a different event in between)
    final TreeEvent e9 = mockEvent(TreeEvent.TYPE_NODE_CHANGED, "D");
    m_testBuffer.add(e1);
    m_testBuffer.add(e2);
    m_testBuffer.add(e3);
    m_testBuffer.add(e4);
    m_testBuffer.add(e5);
    m_testBuffer.add(e6);
    m_testBuffer.add(e7);
    m_testBuffer.add(e8);
    m_testBuffer.add(e9);
    final List<TreeEvent> coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(8, coalesced.size());
    assertEquals(TreeEvent.TYPE_NODE_CHANGED, coalesced.get(0).getType());
    assertEquals(1, coalesced.get(0).getNodes().size());
    assertEquals(TreeEvent.TYPE_NODE_CHANGED, coalesced.get(1).getType());
    assertEquals(1, coalesced.get(1).getNodes().size());
    assertEquals(TreeEvent.TYPE_NODE_CHANGED, coalesced.get(2).getType());
    assertEquals(1, coalesced.get(2).getNodes().size());
    assertEquals(TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED, coalesced.get(3).getType());
    assertEquals(3, coalesced.get(3).getNodes().size());
    assertEquals(TreeEvent.TYPE_NODE_CHANGED, coalesced.get(4).getType());
    assertEquals(1, coalesced.get(4).getNodes().size());
    assertEquals(TreeEvent.TYPE_NODE_CHANGED, coalesced.get(5).getType());
    assertEquals(1, coalesced.get(5).getNodes().size());
    assertEquals(TreeEvent.TYPE_NODE_CHANGED, coalesced.get(6).getType());
    assertEquals(1, coalesced.get(6).getNodes().size());
    assertEquals(TreeEvent.TYPE_NODE_CHANGED, coalesced.get(7).getType());
    assertEquals(1, coalesced.get(7).getNodes().size());
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

  /**
   * Insert a tree of nodes, and then again a subtree
   */
  @Test
  public void testInsertSameNodesTwice() {
    // A
    // +-B
    // | +-E
    // |   +-F
    // +-C
    //   +-G
    // +-D
    ITreeNode nodeA = mockNode("A");
    ITreeNode nodeB = mockNode("B");
    ITreeNode nodeC = mockNode("C");
    ITreeNode nodeD = mockNode("D");
    ITreeNode nodeE = mockNode("E");
    ITreeNode nodeF = mockNode("F");
    ITreeNode nodeG = mockNode("G");
    ITreeNode nodeH = mockNode("H");
    installChildNodes(nodeA, nodeB, nodeC, nodeD);
    installChildNodes(nodeB, nodeE);
    installChildNodes(nodeE, nodeF);
    installChildNodes(nodeC, nodeG);

    TreeEvent e1 = mockEvent(TreeEvent.TYPE_NODES_INSERTED, nodeA, nodeB, nodeE);
    TreeEvent e2 = mockEvent(TreeEvent.TYPE_NODES_UPDATED, nodeE, nodeH);
    TreeEvent e3 = mockEvent(TreeEvent.TYPE_NODES_INSERTED, nodeB);
    m_testBuffer.add(e1);
    m_testBuffer.add(e2);
    m_testBuffer.add(e3);

    List<TreeEvent> coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(2, coalesced.size()); // e1, e2
  }

  /**
   * Insert some nodes, then delete some children of them --> only insert event should remain
   */
  @Test
  public void testAllChildNodesDeleted() {
    // A
    // +-B
    // | +-E
    // |   +-F
    // +-C
    //   +-G
    // +-D
    ITreeNode nodeA = mockNode("A");
    ITreeNode nodeB = mockNode("B");
    ITreeNode nodeC = mockNode("C");
    ITreeNode nodeD = mockNode("D");
    ITreeNode nodeE = mockNode("E");
    ITreeNode nodeF = mockNode("F");
    ITreeNode nodeG = mockNode("G");
    installChildNodes(nodeA, nodeB, nodeC, nodeD);
    installChildNodes(nodeB, nodeE);
    installChildNodes(nodeE, nodeF);
    installChildNodes(nodeC, nodeG);

    // simulate "all child nodes deleted"
    installChildNodes(nodeB, new ITreeNode[0]);

    // --- Test case 1: ALL_CHILD_NODES_DELETED ----------------------

    TreeEvent e1 = mockEvent(TreeEvent.TYPE_NODES_INSERTED, nodeB, nodeD);
    TreeEvent e2 = mockEvent(nodeB, TreeEvent.TYPE_ALL_CHILD_NODES_DELETED, nodeE);
    TreeEvent e3 = mockEvent(TreeEvent.TYPE_NODES_INSERTED, nodeC);
    m_testBuffer.add(e1);
    m_testBuffer.add(e2);
    m_testBuffer.add(e3);

    List<TreeEvent> coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(1, coalesced.size()); // 1x NODES_INSERTED
    assertEquals(TreeEvent.TYPE_NODES_INSERTED, coalesced.get(0).getType());
    assertEquals(3, coalesced.get(0).getNodes().size());

    // --- Test case 2: NODES_DELETED --------------------------------

    TreeEvent e4 = mockEvent(TreeEvent.TYPE_NODES_INSERTED, nodeB, nodeD);
    TreeEvent e5 = mockEvent(nodeE, TreeEvent.TYPE_NODES_DELETED, nodeF);
    TreeEvent e6 = mockEvent(nodeB, TreeEvent.TYPE_NODES_DELETED, nodeE);
    TreeEvent e7 = mockEvent(TreeEvent.TYPE_NODES_INSERTED, nodeC);
    m_testBuffer.add(e4);
    m_testBuffer.add(e5);
    m_testBuffer.add(e6);
    m_testBuffer.add(e7);

    coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(1, coalesced.size()); // 1x NODES_INSERTED
    assertEquals(TreeEvent.TYPE_NODES_INSERTED, coalesced.get(0).getType());
    assertEquals(3, coalesced.get(0).getNodes().size());
  }

  /**
   * Expanded/Collapsed events ==> If all are collapsed, we don't care about the previous expansion events
   */
  @Test
  public void testCoalesce_Expanded() {
    // A
    // +-B
    // | +-E
    // |   +-F
    // +-C
    //   +-G
    // +-D
    ITreeNode nodeA = mockNode("A");
    ITreeNode nodeB = mockNode("B");
    ITreeNode nodeC = mockNode("C");
    ITreeNode nodeD = mockNode("D");
    ITreeNode nodeE = mockNode("E");
    ITreeNode nodeF = mockNode("F");
    ITreeNode nodeG = mockNode("G");
    installChildNodes(nodeA, nodeB, nodeC, nodeD);
    installChildNodes(nodeB, nodeE);
    installChildNodes(nodeE, nodeF);
    installChildNodes(nodeC, nodeG);

    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODE_COLLAPSED_RECURSIVE, nodeA));
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODE_EXPANDED_RECURSIVE, nodeB));
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODE_EXPANDED, nodeB));
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODE_EXPANDED, nodeE));
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODE_EXPANDED, nodeC));
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODE_COLLAPSED, nodeB));
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODE_EXPANDED, nodeG));
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODE_COLLAPSED_RECURSIVE, nodeA));

    List<TreeEvent> coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(1, coalesced.size());
    assertEquals(TreeEvent.TYPE_NODE_COLLAPSED_RECURSIVE, coalesced.get(0).getType());
  }

  /**
   * We cannot coalesce NODE_EXPANDED, because this is not supported by the UI
   */
  @Test
  public void testCoalesce_NoCoalesceSingleEvents() {
    // A
    // +-B
    // | +-E
    // |   +-F
    // +-C
    //   +-G
    // +-D
    ITreeNode nodeA = mockNode("A");
    ITreeNode nodeB = mockNode("B");
    ITreeNode nodeC = mockNode("C");
    ITreeNode nodeD = mockNode("D");
    ITreeNode nodeE = mockNode("E");
    ITreeNode nodeF = mockNode("F");
    ITreeNode nodeG = mockNode("G");
    installChildNodes(nodeA, nodeB, nodeC, nodeD);
    installChildNodes(nodeB, nodeE);
    installChildNodes(nodeE, nodeF);
    installChildNodes(nodeC, nodeG);

    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODE_EXPANDED, nodeB));
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODE_EXPANDED, nodeE));
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODE_EXPANDED, nodeC));
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODE_COLLAPSED, nodeB));
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODE_EXPANDED, nodeG));

    List<TreeEvent> coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(5, coalesced.size());
  }

  /**
   * Remove deleted nodes from previous events
   */
  @Test
  public void testRemoveDeletedNodesFromPreviousEvents() {
    // A
    // +-B
    // | +-E
    // |   +-F
    // +-C
    //   +-G
    // +-D
    ITreeNode nodeA = mockNode("A");
    ITreeNode nodeB = mockNode("B");
    ITreeNode nodeC = mockNode("C");
    ITreeNode nodeD = mockNode("D");
    ITreeNode nodeE = mockNode("E");
    ITreeNode nodeF = mockNode("F");
    ITreeNode nodeG = mockNode("G");
    installChildNodes(nodeA, nodeB, nodeC, nodeD);
    installChildNodes(nodeB, nodeE);
    installChildNodes(nodeE, nodeF);
    installChildNodes(nodeC, nodeG);

    // NODES_DELETED
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODE_EXPANDED, nodeB));
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODE_EXPANDED, nodeE));
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODES_UPDATED, nodeF));
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODE_COLLAPSED, nodeG));
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODES_DELETED, nodeB, nodeC));

    List<TreeEvent> coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(1, coalesced.size());
    assertEquals(TreeEvent.TYPE_NODES_DELETED, coalesced.get(0).getType());
    assertEquals(2, coalesced.get(0).getChildNodes().size());

    // ALL_CHILD_NODES_DELETED
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODE_EXPANDED, nodeB));
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODE_EXPANDED, nodeE));
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODES_UPDATED, nodeF));
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODE_COLLAPSED, nodeG));
    m_testBuffer.add(mockEvent(nodeA, TreeEvent.TYPE_ALL_CHILD_NODES_DELETED, nodeB, nodeC, nodeD));

    coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(1, coalesced.size());
    assertEquals(TreeEvent.TYPE_ALL_CHILD_NODES_DELETED, coalesced.get(0).getType());
    assertEquals(3, coalesced.get(0).getChildNodes().size());
  }

  /**
   * Test for the utility method "collectAllNodesRec"
   */
  @Test
  public void testCollectAllChildNodesRec() {
    // A
    // +-B
    // | +-E
    // |   +-F
    // +-C
    //   +-G
    // +-D
    ITreeNode nodeA = mockNode("A");
    ITreeNode nodeB = mockNode("B");
    ITreeNode nodeC = mockNode("C");
    ITreeNode nodeD = mockNode("D");
    ITreeNode nodeE = mockNode("E");
    ITreeNode nodeF = mockNode("F");
    ITreeNode nodeG = mockNode("G");
    installChildNodes(nodeA, nodeB, nodeC, nodeD);
    installChildNodes(nodeB, nodeE);
    installChildNodes(nodeE, nodeF);
    installChildNodes(nodeC, nodeG);
    Collection<ITreeNode> allNodes = new ArrayList<ITreeNode>();
    allNodes.add(nodeA);
    allNodes.add(nodeB);
    allNodes.add(nodeC);
    allNodes.add(nodeD);
    allNodes.add(nodeE);
    allNodes.add(nodeF);
    allNodes.add(nodeG);

    Collection<ITreeNode> allCollectedNodes = m_testBuffer.collectAllNodesRec(Collections.singletonList(nodeA));
    assertEquals(7, allCollectedNodes.size());
    assertTrue(CollectionUtility.equalsCollection(allCollectedNodes, allNodes));
  }

  /**
   * Tests that the event buffer uses the resolved nodes instead of the virtual nodes. This is relevant when looping
   * over the children of nodes.
   */
  @Test
  public void testResolveVirtualNodes() {
    // A
    // +- (D) = B
    //          +- E
    // +- (C)
    AbstractTreeNode nodeA = new AbstractTreeNode() {
    };
    AbstractTreeNode nodeB = new AbstractTreeNode() {
    };
    VirtualTreeNode nodeC = new VirtualTreeNode() {
    };
    VirtualTreeNode nodeD = new VirtualTreeNode() {
    };
    VirtualTreeNode nodeE = new VirtualTreeNode() {
    };
    nodeD.setResolvedNode(nodeB);
    nodeA.addChildNodesInternal(0, Collections.singletonList(nodeD), false);
    nodeA.addChildNodesInternal(1, Collections.singletonList(nodeC), false);
    nodeB.addChildNodesInternal(0, Collections.singletonList(nodeE), false);
    nodeA.getCellForUpdate().setText("A");
    nodeB.getCellForUpdate().setText("B");
    nodeC.getCellForUpdate().setText("C (v)");
    nodeD.getCellForUpdate().setText("D (v)");
    nodeE.getCellForUpdate().setText("E");

    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODES_INSERTED, nodeA));
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODES_INSERTED, nodeE));

    List<TreeEvent> coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(1, coalesced.size());
  }

  /**
   * NODES_CHECKED events => coalesce same consecutive types
   */
  @Test
  public void testCoalesce_Checked() {
    // A
    // +-B
    // | +-E
    // |   +-F
    // +-C
    //   +-G
    // +-D
    ITreeNode nodeA = mockNode("A");
    ITreeNode nodeB = mockNode("B");
    ITreeNode nodeC = mockNode("C");
    ITreeNode nodeD = mockNode("D");
    ITreeNode nodeE = mockNode("E");
    ITreeNode nodeF = mockNode("F");
    ITreeNode nodeG = mockNode("G");
    installChildNodes(nodeA, nodeB, nodeC, nodeD);
    installChildNodes(nodeB, nodeE);
    installChildNodes(nodeE, nodeF);
    installChildNodes(nodeC, nodeG);

    m_testBuffer.add(mockEvent(nodeA, TreeEvent.TYPE_NODES_CHECKED, nodeB));
    m_testBuffer.add(mockEvent(TreeEvent.TYPE_NODES_UPDATED, nodeF));
    m_testBuffer.add(mockEvent(nodeA, TreeEvent.TYPE_NODES_CHECKED, nodeD));
    m_testBuffer.add(mockEvent(nodeB, TreeEvent.TYPE_NODES_CHECKED, nodeE));
    m_testBuffer.add(mockEvent(nodeC, TreeEvent.TYPE_NODES_CHECKED, nodeG));
    m_testBuffer.add(mockEvent(nodeA, TreeEvent.TYPE_NODES_CHECKED, nodeC));

    List<TreeEvent> coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(5, coalesced.size());

    assertType(TreeEvent.TYPE_NODES_CHECKED, coalesced, 0);
    assertEquals(nodeA, coalesced.get(0).getCommonParentNode());
    assertEquals(1, coalesced.get(0).getNodes().size());

    assertType(TreeEvent.TYPE_NODES_UPDATED, coalesced, 1);

    assertType(TreeEvent.TYPE_NODES_CHECKED, coalesced, 2);
    assertEquals(nodeB, coalesced.get(2).getCommonParentNode());
    assertEquals(1, coalesced.get(2).getNodes().size());

    assertType(TreeEvent.TYPE_NODES_CHECKED, coalesced, 3);
    assertEquals(nodeC, coalesced.get(3).getCommonParentNode());
    assertEquals(1, coalesced.get(3).getNodes().size());

    assertType(TreeEvent.TYPE_NODES_CHECKED, coalesced, 4);
    assertEquals(nodeA, coalesced.get(4).getCommonParentNode());
    assertEquals(2, coalesced.get(4).getNodes().size());
  }

  /**
   * Tests a case from ticket #167088. Original list of events:
   * <li>TreeEvent[TYPE_NODES_DELETED CPN=node]
   * <li>TreeEvent[TYPE_NODES_INSERTED CPN=node]
   * <li>TreeEvent[TYPE_NODES_DELETED CPN=node]
   * <li>TreeEvent[TYPE_NODES_INSERTED CPN=node]
   * <p>
   * The (wrong) result after coalescing was:
   * </p>
   * <li>TreeEvent[TYPE_NODES_INSERTED CPN=node]
   * <p>
   * The expected result is either the original list of events or a coalesced version with only a single delete and
   * insert event. This problem was caused in the removeObsolete() method, because it also removed the node from the
   * second delete event - this has been corrected in the removeNodesFromPreviousEvents() method.
   * </p>
   *
   * @throws Exception
   */
  @Test
  public void testDeleteInsertCoalesce() throws Exception {
    ITreeNode root = mockNode("Root");
    ITreeNode nodeA = mockNode("A");
    installChildNodes(root, nodeA);

    m_testBuffer.add(mockEvent(root, TreeEvent.TYPE_NODES_DELETED, nodeA));
    m_testBuffer.add(mockEvent(root, TreeEvent.TYPE_NODES_INSERTED, nodeA));
    m_testBuffer.add(mockEvent(root, TreeEvent.TYPE_NODES_DELETED, nodeA));
    m_testBuffer.add(mockEvent(root, TreeEvent.TYPE_NODES_INSERTED, nodeA));

    List<TreeEvent> coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(2, coalesced.size());

    assertType(TreeEvent.TYPE_NODES_DELETED, coalesced, 0);
    assertContainsNode(coalesced, 0, nodeA);
    assertType(TreeEvent.TYPE_NODES_INSERTED, coalesced, 1);
    assertContainsNode(coalesced, 1, nodeA);
  }

  @Test
  public void testDeleteInsertCoalesce_MultipleNodes() throws Exception {
    ITreeNode root = mockNode("Root");
    ITreeNode nodeA = mockNode("A");
    ITreeNode nodeB = mockNode("B");
    installChildNodes(root, nodeA);
    installChildNodes(root, nodeB);

    m_testBuffer.add(mockEvent(root, TreeEvent.TYPE_NODES_DELETED, nodeA, nodeB));
    m_testBuffer.add(mockEvent(root, TreeEvent.TYPE_NODES_INSERTED, nodeB));
    m_testBuffer.add(mockEvent(root, TreeEvent.TYPE_NODES_INSERTED, nodeA));
    m_testBuffer.add(mockEvent(root, TreeEvent.TYPE_NODES_DELETED, nodeB, nodeA));
    m_testBuffer.add(mockEvent(root, TreeEvent.TYPE_NODES_INSERTED, nodeB));
    m_testBuffer.add(mockEvent(root, TreeEvent.TYPE_NODES_INSERTED, nodeA));

    List<TreeEvent> coalesced = m_testBuffer.consumeAndCoalesceEvents();
    assertEquals(2, coalesced.size());

    assertType(TreeEvent.TYPE_NODES_DELETED, coalesced, 0);
    assertContainsNode(coalesced, 0, nodeA, nodeB);
    assertType(TreeEvent.TYPE_NODES_INSERTED, coalesced, 1);
    assertContainsNode(coalesced, 1, nodeA, nodeB);
  }

  private void assertContainsNode(List<TreeEvent> events, int index, ITreeNode... expectedNodes) {
    TreeEvent event = events.get(index);
    assertEquals(expectedNodes.length, event.getNodes().size());
    for (ITreeNode node : expectedNodes) {
      assertTrue(event.getNodes().contains(node));
    }
  }

  private void assertType(int expectedType, List<TreeEvent> events, int index) {
    assertEquals(expectedType, events.get(index).getType());
  }

  private TreeEvent mockEvent(int type, String... nodeIds) {
    return mockEvent(type, mockNodes(nodeIds));
  }

  private TreeEvent mockEvent(int type, ITreeNode... nodes) {
    return mockEvent(type, Arrays.asList(nodes));
  }

  private TreeEvent mockEvent(int type, List<ITreeNode> nodes) {
    if (m_testBuffer.isCommonParentNodeRequired(type)) {
      throw new IllegalStateException("Missing common parent node, use other mock function");
    }
    return new TreeEvent(mock(ITree.class), type, nodes);
  }

  @SuppressWarnings("unused")
  private TreeEvent mockEvent(String parentNodeId, int type, String... childNodeIds) {
    return mockEvent(mockNode(parentNodeId), type, mockNodes(childNodeIds));
  }

  private TreeEvent mockEvent(ITreeNode parentNode, int type, ITreeNode... childNodes) {
    return mockEvent(parentNode, type, Arrays.asList(childNodes));
  }

  private TreeEvent mockEvent(ITreeNode parentNode, int type, List<ITreeNode> childNodes) {
    return new TreeEvent(mock(ITree.class), type, parentNode, childNodes);
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
    node = mock(ITreeNode.class, "MockNode[" + nodeId + "]");
    when(node.getNodeId()).thenReturn(nodeId);
    m_mockNodes.put(nodeId, node);
    return node;
  }

  private void installChildNodes(ITreeNode node, ITreeNode... childNodes) {
    List<ITreeNode> childNodeList = Arrays.asList(childNodes);
    when(node.getChildNodes()).thenReturn(childNodeList);
    when(node.getChildNodeCount()).thenReturn(childNodeList.size());
    for (ITreeNode childNode : childNodeList) {
      when(childNode.getParentNode()).thenReturn(node);
    }
  }

}
