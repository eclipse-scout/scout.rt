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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Tests for {@link TreeEventBuffer}
 */
public class TreeEventBufferTest {

  /**
   * each test should finish within 1000ms running on a workstation. running on jenkins / CI, due to unpredictable load
   * of the system, they might need substantially more time to complete. therefore this timout value is extracted to
   * constant and can be changed to a lower value when running locally / within ide.
   */
  private static final int TIMEOUT_VALUE = 20000; //ms

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
    assertEquals(3, coalesced.get(0).getNodeCount());
    assertEquals(TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED, coalesced.get(1).getType());
    assertEquals(3, coalesced.get(1).getNodeCount());
    assertEquals(TreeEvent.TYPE_NODES_UPDATED, coalesced.get(2).getType());
    assertEquals(4, coalesced.get(2).getNodeCount());
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
    assertEquals(1, coalesced.get(1).getNodeCount());
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
    assertEquals(1, coalesced.get(0).getNodeCount());
    assertEquals(TreeEvent.TYPE_NODE_CHANGED, coalesced.get(1).getType());
    assertEquals(1, coalesced.get(1).getNodeCount());
    assertEquals(TreeEvent.TYPE_NODE_CHANGED, coalesced.get(2).getType());
    assertEquals(1, coalesced.get(2).getNodeCount());
    assertEquals(TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED, coalesced.get(3).getType());
    assertEquals(3, coalesced.get(3).getNodeCount());
    assertEquals(TreeEvent.TYPE_NODE_CHANGED, coalesced.get(4).getType());
    assertEquals(1, coalesced.get(4).getNodeCount());
    assertEquals(TreeEvent.TYPE_NODE_CHANGED, coalesced.get(5).getType());
    assertEquals(1, coalesced.get(5).getNodeCount());
    assertEquals(TreeEvent.TYPE_NODE_CHANGED, coalesced.get(6).getType());
    assertEquals(1, coalesced.get(6).getNodeCount());
    assertEquals(TreeEvent.TYPE_NODE_CHANGED, coalesced.get(7).getType());
    assertEquals(1, coalesced.get(7).getNodeCount());
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
    assertEquals(3, coalesced.get(0).getNodeCount());
    assertEquals(TreeEvent.TYPE_CHILD_NODE_ORDER_CHANGED, coalesced.get(1).getType());
    assertEquals(3, coalesced.get(1).getNodeCount());
    assertEquals(TreeEvent.TYPE_NODES_UPDATED, coalesced.get(2).getType());
    assertEquals(1, coalesced.get(2).getNodeCount());
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
    assertEquals(1, coalesced.get(0).getNodeCount());
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
    assertEquals(3, coalesced.get(0).getNodeCount());

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
    assertEquals(3, coalesced.get(0).getNodeCount());
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

    Set<ITreeNode> allCollectedNodes = new HashSet<>();
    allCollectedNodes.add(nodeA);
    nodeA.collectChildNodes(allCollectedNodes, true);
    assertEquals(7, allCollectedNodes.size());
    assertTrue(CollectionUtility.equalsCollection(allCollectedNodes, allNodes));
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
    assertEquals(1, coalesced.get(0).getNodeCount());

    assertType(TreeEvent.TYPE_NODES_UPDATED, coalesced, 1);

    assertType(TreeEvent.TYPE_NODES_CHECKED, coalesced, 2);
    assertEquals(nodeB, coalesced.get(2).getCommonParentNode());
    assertEquals(1, coalesced.get(2).getNodeCount());

    assertType(TreeEvent.TYPE_NODES_CHECKED, coalesced, 3);
    assertEquals(nodeC, coalesced.get(3).getCommonParentNode());
    assertEquals(1, coalesced.get(3).getNodeCount());

    assertType(TreeEvent.TYPE_NODES_CHECKED, coalesced, 4);
    assertEquals(nodeA, coalesced.get(4).getCommonParentNode());
    assertEquals(2, coalesced.get(4).getNodeCount());
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

  @Test
  public void testCoalesceSameTypeCheckOrderSingleNodeEvents() {
    final int nodeCount = 10;
    List<ITreeNode> nodes = new ArrayList<>(nodeCount);
    LinkedList<TreeEvent> events = new LinkedList<>();
    for (int i = 0; i < nodeCount; i++) {
      ITreeNode node = mockNode(String.valueOf(i));
      nodes.add(node);
      events.add(mockEvent(TreeEvent.TYPE_NODES_INSERTED, node));
    }

    assertEquals(nodeCount, events.size());
    m_testBuffer.coalesceSameType(events);

    assertEquals(1, events.size());
    assertEquals(nodes, events.get(0).getNodes());
  }

  @Test
  public void testCoalesceSameTypeCheckOrderMultipleNodesEvents() {
    final int nodeCount = 10;
    List<ITreeNode> nodes = new ArrayList<>(nodeCount);
    LinkedList<TreeEvent> events = new LinkedList<>();
    for (int i = 0; i < nodeCount; i++) {
      ITreeNode node1 = mockNode(String.valueOf(2 * i));
      ITreeNode node2 = mockNode(String.valueOf(2 * i + 1));
      nodes.add(node1);
      nodes.add(node2);
      events.add(mockEvent(TreeEvent.TYPE_NODES_INSERTED, node1, node2));
    }

    assertEquals(nodeCount, events.size());
    m_testBuffer.coalesceSameType(events);

    assertEquals(1, events.size());
    assertEquals(nodes, events.get(0).getNodes());
  }

  @Test
  public void testIgnoreExpandAfterInsert() throws Exception {
    testIgnoreAfterInsert(TreeEvent.TYPE_NODE_EXPANDED);
  }

  @Test
  public void testIgnoreExpandRecursiveAfterInsert() throws Exception {
    testIgnoreAfterInsert(TreeEvent.TYPE_NODE_EXPANDED_RECURSIVE);
  }

  @Test
  public void testIgnoreCollapsedAfterInsert() throws Exception {
    testIgnoreAfterInsert(TreeEvent.TYPE_NODE_COLLAPSED);
  }

  @Test
  public void testIgnoreCollapsedRecursiveAfterInsert() throws Exception {
    testIgnoreAfterInsert(TreeEvent.TYPE_NODE_COLLAPSED_RECURSIVE);
  }

  @Test
  public void testIgnoreChangedAfterInsert() throws Exception {
    testIgnoreAfterInsert(TreeEvent.TYPE_NODE_CHANGED);
  }

  @Test
  public void testIgnoreUpdatedAfterInsert() throws Exception {
    testIgnoreAfterInsert(TreeEvent.TYPE_NODES_UPDATED);
  }

  @Test
  public void testIgnoreInsertedAfterInsert() throws Exception {
    testIgnoreAfterInsert(TreeEvent.TYPE_NODES_INSERTED);
  }

  private void testIgnoreAfterInsert(int eventType) {
    LinkedList<TreeEvent> treeEvents = new LinkedList<>();
    ITreeNode parentA = mockNode("parent");
    treeEvents.add(mockEvent(TreeEvent.TYPE_NODES_INSERTED, mockNode("child", parentA)));
    treeEvents.add(mockEvent(eventType, mockNode("child", parentA)));
    m_testBuffer.coalesce(treeEvents);
    assertEquals(1, treeEvents.size());
  }

  /**
   * TYPE_NODES_CHECKED must not be ignored after insert, because it might be necessary to say "no node is checked"
   */
  @Test
  public void testNotIgnoreCheckedAfterInsert() throws Exception {
    LinkedList<TreeEvent> treeEvents = new LinkedList<>();
    ITreeNode parentA = mockNode("parent");
    treeEvents.add(mockEvent(TreeEvent.TYPE_NODES_INSERTED, mockNode("child", parentA)));
    treeEvents.add(mockEvent(TreeEvent.TYPE_NODES_CHECKED, mockNode("child", parentA)));
    m_testBuffer.coalesce(treeEvents);
    assertEquals(2, treeEvents.size());
  }

  @Test(timeout = TIMEOUT_VALUE)
  public void testCoalesceSameTypeWithManyInsertEvents() throws Exception {
    final int eventCount = 10000;
    ITreeNode parentA = mockNode("parentA");
    LinkedList<TreeEvent> treeEvents = new LinkedList<>();
    for (int i = 0; i < eventCount; i++) {
      treeEvents.add(mockEvent(TreeEvent.TYPE_NODES_INSERTED, mockNode(String.valueOf(i), parentA)));
    }

    assertEquals(eventCount, treeEvents.size());
    m_testBuffer.coalesceSameType(treeEvents);
    assertEquals(1, treeEvents.size());
    assertEquals(eventCount, CollectionUtility.firstElement(treeEvents).getNodeCount());
  }

  @Test(timeout = TIMEOUT_VALUE)
  public void testCoalesceSameTypeWithManyInsertHavingDifferentParentsEvents() throws Exception {
    final int eventCount = 10000;
    LinkedList<TreeEvent> treeEvents = new LinkedList<>();
    ITreeNode parentA = mockNode("parentA");
    ITreeNode parentB = mockNode("parentB");
    for (int i = 0; i < eventCount; i++) {
      treeEvents.add(mockEvent(TreeEvent.TYPE_NODES_INSERTED, mockNode(String.valueOf(2 * i), parentA)));
      treeEvents.add(mockEvent(TreeEvent.TYPE_NODES_INSERTED, mockNode(String.valueOf(2 * i + 1), parentB)));
    }

    assertEquals(2 * eventCount, treeEvents.size());
    m_testBuffer.coalesceSameType(treeEvents);
    assertEquals(2, treeEvents.size());

    TreeEvent firstEvent = treeEvents.get(0);
    TreeEvent secondEvent = treeEvents.get(1);
    assertEquals(eventCount, firstEvent.getNodeCount());
    assertEquals(eventCount, secondEvent.getNodeCount());

    assertSame(parentA, firstEvent.getCommonParentNode());
    assertSame(parentB, secondEvent.getCommonParentNode());
  }

  @Test(timeout = TIMEOUT_VALUE)
  public void testCoalesceSameTypeWithManyInsertUpdateEvents() throws Exception {
    final int eventCount = 10000;
    LinkedList<TreeEvent> treeEvents = new LinkedList<>();
    ITreeNode parentA = mockNode("parentA");
    for (int i = 0; i < eventCount; i++) {
      ITreeNode node = mockNode(String.valueOf(i), parentA);
      treeEvents.add(mockEvent(TreeEvent.TYPE_NODES_INSERTED, node));
      treeEvents.add(mockEvent(TreeEvent.TYPE_NODES_UPDATED, node));
    }

    assertEquals(2 * eventCount, treeEvents.size());
    m_testBuffer.coalesceSameType(treeEvents);
    assertEquals(2 * eventCount, treeEvents.size());
  }

  @Test(timeout = TIMEOUT_VALUE)
  public void testCoalesceSameTypeWithManyInsertInsertUpdateUpdateEvents() throws Exception {
    final int eventCount = 10000;
    LinkedList<TreeEvent> events = new LinkedList<>();
    ITreeNode parentA = mockNode("parentA");
    for (int i = 0; i < eventCount; i++) {
      ITreeNode node1 = mockNode(String.valueOf(2 * i), parentA);
      ITreeNode node2 = mockNode(String.valueOf(2 * i + 1), parentA);
      events.add(mockEvent(TreeEvent.TYPE_NODES_INSERTED, node1));
      events.add(mockEvent(TreeEvent.TYPE_NODES_INSERTED, node2));
      events.add(mockEvent(TreeEvent.TYPE_NODES_UPDATED, node1));
      events.add(mockEvent(TreeEvent.TYPE_NODES_UPDATED, node2));
    }

    assertEquals(4 * eventCount, events.size());
    m_testBuffer.coalesceSameType(events);
    assertEquals(2 * eventCount, events.size());
  }

  @Test(timeout = TIMEOUT_VALUE)
  public void testRemoveNodesContainedInPreviousEventsWithManyInsertAndUpdateEvents() throws Exception {
    final int eventCount = 10000;
    LinkedList<TreeEvent> events = new LinkedList<>();
    ITreeNode parentA = mockNode("parentA");

    for (int i = 0; i < eventCount; i++) {
      events.add(mockEvent(TreeEvent.TYPE_NODES_INSERTED, mockNode(String.valueOf(i), parentA)));
    }
    for (int i = 0; i < eventCount; i++) {
      events.add(mockEvent(TreeEvent.TYPE_NODES_UPDATED, mockNode(String.valueOf(i), parentA)));
    }
    assertEquals(2 * eventCount, events.size());

    m_testBuffer.removeNodesContainedInPreviousInsertEvents(events, CollectionUtility.hashSet(TreeEvent.TYPE_NODES_UPDATED, TreeEvent.TYPE_NODE_CHANGED, TreeEvent.TYPE_NODES_INSERTED));
    assertEquals(2 * eventCount, events.size());
    int i = 0;
    for (TreeEvent event : events) {
      int expectedNodeCount = i < eventCount ? 1 : 0;
      assertEquals(expectedNodeCount, event.getNodeCount());
      i++;
    }
  }

  @Test(timeout = TIMEOUT_VALUE)
  public void testRemoveIdenticalEventsWithManyInsertEvents() throws Exception {
    final int eventCount = 10000;
    LinkedList<TreeEvent> events = new LinkedList<>();
    for (int i = 0; i < eventCount; i++) {
      events.add(mockEvent(TreeEvent.TYPE_NODES_INSERTED, String.valueOf(i)));
    }
    for (int i = eventCount - 1; i >= 0; i--) {
      events.add(mockEvent(TreeEvent.TYPE_NODES_INSERTED, String.valueOf(i)));
    }
    assertEquals(2 * eventCount, events.size());
    m_testBuffer.removeIdenticalEvents(events);
    assertEquals(eventCount, events.size());
  }

  @Test(timeout = TIMEOUT_VALUE)
  public void testRemoveIdenticalEventsWithManyInsertAndDeleteEvents() throws Exception {
    final int eventCount = 10000;
    LinkedList<TreeEvent> events = new LinkedList<>();
    for (int i = 0; i < eventCount; i++) {
      events.add(mockEvent(TreeEvent.TYPE_NODES_INSERTED, String.valueOf(i)));
      events.add(mockEvent(TreeEvent.TYPE_NODES_DELETED, String.valueOf(i)));
    }
    assertEquals(2 * eventCount, events.size());
    m_testBuffer.removeIdenticalEvents(events);
    assertEquals(2 * eventCount, events.size());
  }

  @Test(timeout = TIMEOUT_VALUE)
  public void testRemoveObsoleteWithManyNodesInsertAndDelete() throws Exception {
    final int eventCount = 10000;
    LinkedList<TreeEvent> events = new LinkedList<>();
    String[] nodeIds = new String[eventCount];
    for (int i = 0; i < eventCount; i++) {
      nodeIds[i] = String.valueOf(i);
    }
    events.add(mockEvent(TreeEvent.TYPE_NODES_INSERTED, nodeIds));
    events.add(mockEvent(TreeEvent.TYPE_NODES_DELETED, nodeIds));

    assertEquals(2, events.size());
    m_testBuffer.removeObsolete(events);
    assertEquals(2, events.size());

    for (TreeEvent event : events) {
      assertFalse(event.hasNodes());
    }
  }

  @Test(timeout = TIMEOUT_VALUE)
  public void testRemoveObsoleteWithManyNodesPairwiseInsertAndDelete() throws Exception {
    final int eventCount = 10000;
    LinkedList<TreeEvent> events = new LinkedList<>();
    for (int i = 0; i < eventCount; i++) {
      events.add(mockEvent(TreeEvent.TYPE_NODES_INSERTED, String.valueOf(i)));
      events.add(mockEvent(TreeEvent.TYPE_NODES_DELETED, String.valueOf(i)));
    }

    assertEquals(2 * eventCount, events.size());
    m_testBuffer.removeObsolete(events);
    assertEquals(2 * eventCount, events.size());

    for (TreeEvent event : events) {
      assertFalse(event.hasNodes());
    }
  }

  @Test(timeout = TIMEOUT_VALUE)
  public void testRemoveObsoleteWithManyUpdateAndOneDeleteAllNodesEvent() throws Exception {
    final int eventCount = 10000;
    LinkedList<TreeEvent> events = new LinkedList<>();
    String[] nodeIds = new String[eventCount];
    for (int i = 0; i < eventCount; i++) {
      nodeIds[i] = String.valueOf(i);
      events.add(mockEvent(TreeEvent.TYPE_NODES_UPDATED, String.valueOf(i)));
    }
    events.add(mockEvent(TreeEvent.TYPE_NODES_DELETED, nodeIds));

    assertEquals(eventCount + 1, events.size());
    m_testBuffer.removeObsolete(events);
    assertEquals(eventCount + 1, events.size());

    int i = 0;
    for (TreeEvent event : events) {
      if (i < eventCount) {
        assertFalse(event.hasNodes());
      }
      else {
        assertTrue(event.hasNodes());
      }
      i++;
    }
  }

  @Test(timeout = TIMEOUT_VALUE)
  public void testRemoveObsoleteWithManyNodesUpdateAndDelete() throws Exception {
    final int eventCount = 10000;
    LinkedList<TreeEvent> events = new LinkedList<>();
    String[] nodeIds = new String[eventCount];
    for (int i = 0; i < eventCount; i++) {
      nodeIds[i] = String.valueOf(i);
    }
    events.add(mockEvent(TreeEvent.TYPE_NODES_UPDATED, nodeIds));
    events.add(mockEvent(TreeEvent.TYPE_NODES_DELETED, nodeIds));

    assertEquals(2, events.size());
    m_testBuffer.removeObsolete(events);
    assertEquals(2, events.size());

    assertEquals(0, events.get(0).getNodeCount());
    assertEquals(eventCount, events.get(1).getNodeCount());
  }

  @Test(expected = AssertionException.class)
  public void testTreeEventMergerNullInitilaEvent() {
    new TreeEventBuffer.TreeEventMerger(null);
  }

  @Test
  public void testTreeEventMerger() {
    ITreeNode nodeA = mockNode("a");
    ITreeNode nodeB = mockNode("b");
    TreeEvent initialEvent = mockEvent(TreeEvent.TYPE_NODE_CHANGED, nodeA, nodeB);
    TreeEventBuffer.TreeEventMerger eventMerger = new TreeEventBuffer.TreeEventMerger(initialEvent);

    // add first event
    ITreeNode nodeC = mockNode("c");
    TreeEvent e1 = mockEvent(TreeEvent.TYPE_NODE_CHANGED, Arrays.asList(nodeA, nodeB, nodeC));
    eventMerger.merge(e1);

    // add second, empty event
    TreeEvent e2 = mockEvent(TreeEvent.TYPE_NODE_CHANGED, new String[0]);
    eventMerger.merge(e2);

    // add third event
    ITreeNode nodeD = mockNode("d");
    ITreeNode nodeE = mockNode("e");
    TreeEvent e3 = mockEvent(TreeEvent.TYPE_NODE_CHANGED, Arrays.asList(nodeD, nodeE));
    eventMerger.merge(e3);

    eventMerger.complete();
    assertEquals(Arrays.asList(nodeD, nodeE, nodeC, nodeA, nodeB), initialEvent.getNodes());
    assertNull(initialEvent.getCommonParentNode());

    // invoke complete a second time has no effect
    eventMerger.complete();
    assertEquals(Arrays.asList(nodeD, nodeE, nodeC, nodeA, nodeB), initialEvent.getNodes());
    assertNull(initialEvent.getCommonParentNode());
  }

  @Test
  public void testTreeEventMergerCompleteWithoutMerge() {
    ITreeNode nodeA = mockNode("a");
    ITreeNode nodeB = mockNode("b");
    TreeEvent initialEvent = mockEvent(TreeEvent.TYPE_NODE_CHANGED, nodeA, nodeB);
    TreeEventBuffer.TreeEventMerger eventMerger = new TreeEventBuffer.TreeEventMerger(initialEvent);

    eventMerger.complete();

    assertEquals(Arrays.asList(nodeA, nodeB), initialEvent.getNodes());
    assertNull(initialEvent.getCommonParentNode());
  }

  @Test
  public void testTreeEventMergerMergeAfterComplete() {
    TreeEvent initialEvent = mockEvent(TreeEvent.TYPE_NODE_CHANGED, "a", "b");
    TreeEventBuffer.TreeEventMerger eventMerger = new TreeEventBuffer.TreeEventMerger(initialEvent);
    eventMerger.complete();

    TreeEvent e1 = mockEvent(TreeEvent.TYPE_NODE_CHANGED, "c", "d");
    try {
      eventMerger.merge(e1);
      fail("merge after complete must throw an " + IllegalStateException.class.getSimpleName());
    }
    catch (IllegalStateException epxected) {
    }
  }

  private void assertContainsNode(List<TreeEvent> events, int index, ITreeNode... expectedNodes) {
    TreeEvent event = events.get(index);
    assertEquals(expectedNodes.length, event.getNodeCount());
    for (ITreeNode node : expectedNodes) {
      assertTrue(event.containsNode(node));
    }
  }

  private void assertType(int expectedType, List<TreeEvent> events, int index) {
    assertEquals(expectedType, events.get(index).getType());
  }

  private TreeEvent mockEvent(int type, String... nodeIds) {
    return mockEvent(type, mockNodes(nodeIds));
  }

  private TreeEvent mockEvent(int type, ITreeNode... nodes) {
    return mockEvent(type, CollectionUtility.arrayList(nodes));
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
    return mockEvent(parentNode, type, CollectionUtility.arrayList(childNodes));
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
    return mockNode(nodeId, null);
  }

  private ITreeNode mockNode(String nodeId, ITreeNode parentNode) {
    ITreeNode node = m_mockNodes.get(nodeId);
    if (node != null) {
      return node;
    }
    // Create a new
    node = mock(ITreeNode.class, "MockNode[" + nodeId + "]");
    when(node.getNodeId()).thenReturn(nodeId);
    when(node.getParentNode()).thenReturn(parentNode);
    m_mockNodes.put(nodeId, node);
    return node;
  }

  private void installChildNodes(ITreeNode node, ITreeNode... childNodes) {
    final List<ITreeNode> childNodeList = Arrays.asList(childNodes);
    when(node.getChildNodes()).thenReturn(childNodeList);
    when(node.getChildNodeCount()).thenReturn(childNodeList.size());
    doAnswer(new Answer<Void>() {
      @Override
      @SuppressWarnings("unchecked")
      public Void answer(InvocationOnMock invocation) throws Throwable {
        Set collector = invocation.getArgumentAt(0, Set.class);
        collector.addAll(childNodeList);
        boolean recursive = Boolean.valueOf(invocation.getArgumentAt(1, Boolean.class));
        if (recursive) {
          for (ITreeNode childNode : childNodeList) {
            childNode.collectChildNodes(collector, recursive);
          }
        }
        return null;
      }
    }).when(node).collectChildNodes(anySetOf(ITreeNode.class), anyBoolean());
    for (ITreeNode childNode : childNodeList) {
      when(childNode.getParentNode()).thenReturn(node);
    }
  }
}
