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
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for {@link AbstractTree}
 */
@RunWith(PlatformTestRunner.class)
public class AbstractTreeTest {

  private P_TreeNode m_node1;
  private P_TreeNode m_node2;
  private P_TreeNode m_subNode1;
  private P_TreeListener m_treeListener;
  private P_Tree m_tree;
  private P_TestMenu m_node1Menu1;
  private P_TestMenu m_subNode1Menu1;
  private P_TestMenu m_subNode1Menu2;

  @Before
  public void setup() {
    m_tree = new P_Tree();
    m_node1 = new P_TreeNode("node1");
    m_node2 = new P_TreeNode("node2");
    m_subNode1 = new P_TreeNode("subNode1");
    m_node1Menu1 = new P_TestMenu("node1Menu1");
    m_node1.setMenus(CollectionUtility.arrayList(m_node1Menu1));
    m_subNode1Menu1 = new P_TestMenu("subNode1Menu1");
    m_subNode1Menu2 = new P_TestMenu("subNode1Menu2");
    m_subNode1.setMenus(CollectionUtility.arrayList(m_subNode1Menu1, m_subNode1Menu2));
    m_tree.addChildNode(m_tree.getRootNode(), m_node1);
    m_tree.addChildNode(m_tree.getRootNode(), m_node2);
    m_tree.addChildNode(m_node2, m_subNode1);
    m_treeListener = new P_TreeListener();
    m_tree.addTreeListener(m_treeListener);
  }

  @Test
  public void testNodeChangedSingleEvents() {

    m_node1.getCellForUpdate().setText("foo");
    assertNotifications(1, 1);

    // expected no notification after setting same value again
    m_node1.getCellForUpdate().setText("foo");
    assertNotifications(1, 1);

    m_node1.getCellForUpdate().setText("foo2");
    assertNotifications(2, 2);

    m_node1.getCellForUpdate().setBackgroundColor("FFFF00");
    m_node1.getCellForUpdate().setForegroundColor("00FF00");
    m_node1.getCellForUpdate().setFont(new FontSpec("Arial", FontSpec.STYLE_BOLD, 7));
    assertNotifications(5, 5);
  }

  @Test
  public void testNodeDropTargetChanged() {
    ITreeNode a = mock(ITreeNode.class);
    ITreeNode b = mock(ITreeNode.class);
    ITreeNode c = mock(ITreeNode.class);

    assertEquals(0, m_tree.m_execDropTargetChangedTimesCalled);

    m_tree.fireNodeDropTargetChanged(a);
    Assert.assertEquals(a, m_tree.m_currentDropNode);
    assertEquals(1, m_tree.m_execDropTargetChangedTimesCalled);

    m_tree.fireNodeDropTargetChanged(b);
    assertEquals(b, m_tree.m_currentDropNode);
    assertEquals(2, m_tree.m_execDropTargetChangedTimesCalled);

    m_tree.fireNodeDropTargetChanged(c);
    assertEquals(c, m_tree.m_currentDropNode);
    assertEquals(3, m_tree.m_execDropTargetChangedTimesCalled);
    m_tree.fireNodeDropTargetChanged(c);
    m_tree.fireNodeDropTargetChanged(c);
    m_tree.fireNodeDropTargetChanged(c);
    m_tree.fireNodeDropTargetChanged(c);
    m_tree.fireNodeDropTargetChanged(c);
    assertEquals(c, m_tree.m_currentDropNode);
    assertEquals(3, m_tree.m_execDropTargetChangedTimesCalled);
  }

  @Test
  public void testNodeChangedBatchEvents() {
    try {
      m_node1.getTree().setTreeChanging(true);
      m_node1.getCellForUpdate().setText("foo");
      // expected no notification after setting same value again
      m_node1.getCellForUpdate().setText("foo");
      m_node1.getCellForUpdate().setText("foo2");
      m_node2.getCellForUpdate().setText("foo2"); // <-- must NOT be coalesced (different node)
      m_node1.getCellForUpdate().setBackgroundColor("FFFF00");
      m_node1.getCellForUpdate().setForegroundColor("00FF00");
      m_node1.getCellForUpdate().setFont(new FontSpec("Arial", FontSpec.STYLE_BOLD, 7));
      m_node1.setEnabled(false); // <-- all other fire NODE_CHANGED event, this fires NODES_UPDATED event
      assertNotifications(0, 0);
    }
    finally {
      m_node1.getTree().setTreeChanging(false);
    }
    // custom check "assertNotification(1, 3)" because different nodes are involved
    assertEquals("wrong number of notifications", 3, m_treeListener.m_notificationCounter);
    assertEquals("wrong number of events", 3, m_treeListener.m_treeEvents.size());
    for (int i = 0; i < m_treeListener.m_treeEvents.size(); i++) {
      TreeEvent e = m_treeListener.m_treeEvents.get(i);
      if (i == 1) {
        Assert.assertSame("expected node to be included in tree event", m_node2, e.getNode());
      }
      else {
        Assert.assertSame("expected node to be included in tree event", m_node1, e.getNode());
      }
    }
  }

  @Test
  public void testInitConfig_DefaultValues() {
    m_tree.initConfig();
    assertTrue(m_tree.isEnabled());
  }

  @Test
  public void testDisposeTree() {
    m_tree.dispose();
    assertDisposed(m_node1, m_node2, m_subNode1, m_node1Menu1, m_subNode1Menu1, m_subNode1Menu2);
  }

  @Test
  public void testDisposeRemovedTreeNodes() {
    m_tree.removeNode(m_subNode1);
    assertNotDisposed(m_node1, m_node2, m_node1Menu1);
    assertDisposed(m_subNode1, m_subNode1Menu1, m_subNode1Menu2);

    m_tree.dispose();
    assertDisposed(m_node1, m_node2, m_subNode1, m_node1Menu1, m_subNode1Menu1, m_subNode1Menu2);
  }

  @Test
  public void testDisposeRemovedTreeNodesRecursive() {
    m_tree.removeNode(m_node2);
    assertNotDisposed(m_node1, m_node1Menu1);
    assertDisposed(m_node2, m_subNode1, m_subNode1Menu1, m_subNode1Menu2);

    m_tree.dispose();
    assertDisposed(m_node1, m_node2, m_subNode1, m_node1Menu1, m_subNode1Menu1, m_subNode1Menu2);
  }

  @Test
  public void testDisposeRemovedTreeNodesInNonAutodiscardTree() {
    m_tree.setAutoDiscardOnDelete(false);
    m_tree.removeNode(m_node2);
    assertNotDisposed(m_node1, m_node2, m_subNode1, m_node1Menu1, m_subNode1Menu1, m_subNode1Menu2);

    m_tree.dispose();
    assertDisposed(m_node1, m_node2, m_subNode1, m_node1Menu1, m_subNode1Menu1, m_subNode1Menu2);
  }

  @Test
  public void testDisposeRemovedTreeNodesInNonAutodiscardTreeExplicitlyClearingDeletedNodes() {
    m_tree.setAutoDiscardOnDelete(false);
    m_tree.removeNode(m_node2);
    assertNotDisposed(m_node1, m_node2, m_subNode1, m_node1Menu1, m_subNode1Menu1, m_subNode1Menu2);

    m_tree.clearDeletedNodes();

    assertNotDisposed(m_node1, m_node1Menu1);
    assertDisposed(m_node2, m_subNode1, m_subNode1Menu1, m_subNode1Menu2);

    m_tree.dispose();
    assertDisposed(m_node1, m_node2, m_subNode1, m_node1Menu1, m_subNode1Menu1, m_subNode1Menu2);
  }

  @Test
  public void testCheckNodes() {
    m_tree.setCheckable(true);
    m_tree.setMultiCheck(false);

    m_tree.setNodeChecked(m_node1, true);
    assertEquals(Collections.singleton(m_node1), m_tree.getCheckedNodes());

    m_tree.setNodeChecked(m_node2, true);
    assertEquals(Collections.singleton(m_node2), m_tree.getCheckedNodes());

    m_tree.setNodeChecked(m_node1, false);
    assertEquals(Collections.singleton(m_node2), m_tree.getCheckedNodes());

    m_tree.setNodeChecked(m_node2, false);
    assertEquals(Collections.emptySet(), m_tree.getCheckedNodes());

    // allow multi check
    m_tree.setMultiCheck(true);
    m_tree.setNodeChecked(m_node1, true);
    m_tree.setNodeChecked(m_node2, true);
    assertEquals(CollectionUtility.hashSet(m_node1, m_node2), m_tree.getCheckedNodes());
  }

  @Test
  public void testHandlingInvisibleChildNodes() {
    m_tree = new P_Tree();
    m_node1 = new P_TreeNode("node1") {
      @Override
      public void loadChildren() {
        getTree().addChildNodes(this, CollectionUtility.arrayList(m_subNode1));
      }
    };

    m_node1.setVisible(false);
    m_node1.setExpanded(true);
    m_tree.setTreeChanging(true);
    m_tree.addChildNode(m_tree.getRootNode(), m_node1);
    List<TreeEvent> events = m_tree.getEventBuffer().consumeAndCoalesceEvents();
    m_tree.setTreeChanging(false);

    assertFalse(containsEvent(events, TreeEvent.TYPE_NODES_INSERTED, m_node1));
    assertFalse(containsEvent(events, TreeEvent.TYPE_NODES_INSERTED, m_subNode1));
    assertFalse(containsEvent(events, TreeEvent.TYPE_NODE_EXPANDED, m_node1));
  }

  protected boolean containsEvent(List<TreeEvent> events, int eventType, ITreeNode node) {
    return events.stream().anyMatch(event -> event.getType() == eventType && event.getNode() == node);
  }

  private static void assertDisposed(ITestDisposable... disposables) {
    for (ITestDisposable disposable : disposables) {
      assertTrue("should be disposed, but is not: " + disposable.getName(), disposable.isDisposed());
    }
  }

  private static void assertNotDisposed(ITestDisposable... disposables) {
    for (ITestDisposable disposable : disposables) {
      assertFalse("should not be disposed, but is: " + disposable.getName(), disposable.isDisposed());
    }
  }

  private void assertNotifications(int expectedNotifications, int expectedEvents) {
    assertEquals("wrong number of notifications", expectedNotifications, m_treeListener.m_notificationCounter);
    assertEquals("wrong number of events", expectedEvents, m_treeListener.m_treeEvents.size());
    for (TreeEvent e : m_treeListener.m_treeEvents) {
      Assert.assertSame("expected node to be included in tree event", m_node1, e.getNode());
    }
  }

  @Test
  public void testExpandCollapse() {
    // A
    // +-B
    // | +-C
    // | | +-D
    // | +-E
    // +-F
    //   +-G
    ITreeNode a = new P_TreeNode("A");
    ITreeNode b = new P_TreeNode("B");
    ITreeNode c = new P_TreeNode("C");
    ITreeNode d = new P_TreeNode("D");
    ITreeNode e = new P_TreeNode("E");
    ITreeNode f = new P_TreeNode("F");
    ITreeNode g = new P_TreeNode("G");
    m_tree.addChildNode(m_tree.getRootNode(), a);
    m_tree.addChildNode(a, b);
    m_tree.addChildNode(a, f);
    m_tree.addChildNode(b, c);
    m_tree.addChildNode(b, e);
    m_tree.addChildNode(c, d);
    m_tree.addChildNode(f, g);

    // In the beginning, everything is collapsed
    assertFalse(a.isExpanded());
    assertFalse(b.isExpanded());
    assertFalse(c.isExpanded());
    assertFalse(d.isExpanded());
    assertFalse(e.isExpanded());
    assertFalse(f.isExpanded());
    assertFalse(g.isExpanded());

    // Expand tree recursively --> all nodes should be expanded
    m_tree.expandAll(m_tree.getRootNode());
    assertTrue(a.isExpanded());
    assertTrue(b.isExpanded());
    assertTrue(c.isExpanded());
    assertTrue(d.isExpanded());
    assertTrue(e.isExpanded());
    assertTrue(f.isExpanded());
    assertTrue(g.isExpanded());

    // Collapse B only --> only B should be collapsed, all other nodes remain expanded
    m_tree.setNodeExpanded(b, false);
    assertTrue(a.isExpanded());
    assertFalse(b.isExpanded());
    assertTrue(c.isExpanded());
    assertTrue(d.isExpanded());
    assertTrue(e.isExpanded());
    assertTrue(f.isExpanded());
    assertTrue(g.isExpanded());

    // Collapse A and the entire subtree recursively --> B and all its child nodes should be collapsed as well
    m_tree.collapseAll(a);
    assertFalse(a.isExpanded());
    assertFalse(b.isExpanded());
    assertFalse(c.isExpanded());
    assertFalse(d.isExpanded());
    assertFalse(e.isExpanded());
    assertFalse(f.isExpanded());
    assertFalse(g.isExpanded());

    // Expand A only --> A is expanded, but all other nodes are still collapsed
    m_tree.setNodeExpanded(a, true);
    assertTrue(a.isExpanded());
    assertFalse(b.isExpanded());
    assertFalse(c.isExpanded());
    assertFalse(d.isExpanded());
    assertFalse(e.isExpanded());
    assertFalse(f.isExpanded());
    assertFalse(g.isExpanded());

    // Expand C, an inner node whose parent is not expanded --> path to root should be expanded automatically
    m_tree.setNodeExpanded(c, true);
    assertTrue(a.isExpanded());
    assertTrue(b.isExpanded());
    assertTrue(c.isExpanded());
    assertFalse(d.isExpanded());
    assertFalse(e.isExpanded());
    assertFalse(f.isExpanded());
    assertFalse(g.isExpanded());
  }

  @Test
  public void testLazyExpandCollapse() {
    // A
    // +-B
    // +-C
    // | +-D
    ITreeNode a = new P_TreeNode("A");
    ITreeNode b = new P_TreeNode("B");
    ITreeNode c = new P_TreeNode("C");
    ITreeNode d = new P_TreeNode("D");
    m_tree.addChildNode(m_tree.getRootNode(), a);
    m_tree.addChildNode(a, b);
    m_tree.addChildNode(a, c);
    m_tree.addChildNode(c, d);

    // In the beginning, everything is collapsed
    assertFalse(a.isExpanded());
    assertFalse(a.isExpandedLazy());
    assertFalse(b.isExpanded());
    assertFalse(b.isExpandedLazy());
    assertFalse(c.isExpanded());
    assertFalse(c.isExpandedLazy());
    assertFalse(d.isExpanded());
    assertFalse(d.isExpandedLazy());

    // Expand A --> only A should be expanded
    m_tree.setNodeExpanded(a, true, false);
    assertTrue(a.isExpanded());
    assertFalse(a.isExpandedLazy());
    assertFalse(b.isExpanded());
    assertFalse(b.isExpandedLazy());
    assertFalse(c.isExpanded());
    assertFalse(c.isExpandedLazy());
    assertFalse(d.isExpanded());
    assertFalse(d.isExpandedLazy());

    // Lazy-Expand D --> A, C and D should be expanded
    m_tree.setNodeExpanded(d, true, true);
    assertTrue(a.isExpanded());
    assertFalse(a.isExpandedLazy());
    assertFalse(b.isExpanded());
    assertFalse(b.isExpandedLazy());
    assertTrue(c.isExpanded());
    assertFalse(c.isExpandedLazy());
    assertTrue(d.isExpanded());
    assertTrue(d.isExpandedLazy());

    // Lazy-Collapse D --> A, C should be expanded
    m_tree.setNodeExpanded(d, false, true);
    assertTrue(a.isExpanded());
    assertFalse(a.isExpandedLazy());
    assertFalse(b.isExpanded());
    assertFalse(b.isExpandedLazy());
    assertTrue(c.isExpanded());
    assertFalse(c.isExpandedLazy());
    assertFalse(d.isExpanded());
    assertTrue(d.isExpandedLazy());

    // Expand D --> A, C and D should be expanded
    m_tree.setNodeExpanded(d, true, false);
    assertTrue(a.isExpanded());
    assertFalse(a.isExpandedLazy());
    assertFalse(b.isExpanded());
    assertFalse(b.isExpandedLazy());
    assertTrue(c.isExpanded());
    assertFalse(c.isExpandedLazy());
    assertTrue(d.isExpanded());
    assertFalse(d.isExpandedLazy());

    // Lazy-Collapse D --> D should be collapsed
    m_tree.setNodeExpanded(d, false, true);
    assertTrue(a.isExpanded());
    assertFalse(a.isExpandedLazy());
    assertFalse(b.isExpanded());
    assertFalse(b.isExpandedLazy());
    assertTrue(c.isExpanded());
    assertFalse(c.isExpandedLazy());
    assertFalse(d.isExpanded());
    assertTrue(d.isExpandedLazy());

    // Lazy-Collapse C --> C and D should be collapsed
    m_tree.setNodeExpanded(c, false, true);
    assertTrue(a.isExpanded());
    assertFalse(a.isExpandedLazy());
    assertFalse(b.isExpanded());
    assertFalse(b.isExpandedLazy());
    assertFalse(c.isExpanded());
    assertTrue(c.isExpandedLazy());
    assertFalse(d.isExpanded());
    assertTrue(d.isExpandedLazy());

    // Collapse C --> C is collapsed
    m_tree.setNodeExpanded(c, false, false);
    assertTrue(a.isExpanded());
    assertFalse(a.isExpandedLazy());
    assertFalse(b.isExpanded());
    assertFalse(b.isExpandedLazy());
    assertFalse(c.isExpanded());
    assertFalse(c.isExpandedLazy());
    assertFalse(d.isExpanded());
    assertTrue(d.isExpandedLazy());

    // Collapse all
    m_tree.collapseAll(a);
    assertFalse(a.isExpanded());
    assertFalse(a.isExpandedLazy());
    assertFalse(b.isExpanded());
    assertFalse(b.isExpandedLazy());
    assertFalse(c.isExpanded());
    assertFalse(c.isExpandedLazy());
    assertFalse(d.isExpanded());
    assertTrue(d.isExpandedLazy());

    // Lazy-Expand B --> A and B are expanded
    m_tree.setNodeExpanded(b, true, true);
    assertTrue(a.isExpanded());
    assertFalse(a.isExpandedLazy());
    assertTrue(b.isExpanded());
    assertTrue(b.isExpandedLazy());
    assertFalse(c.isExpanded());
    assertFalse(c.isExpandedLazy());
    assertFalse(d.isExpanded());
    assertTrue(d.isExpandedLazy());

    // Lazy-Collapse A --> A and B should be collapsed
    m_tree.setNodeExpanded(a, false, true);
    assertFalse(a.isExpanded());
    assertTrue(a.isExpandedLazy());
    assertTrue(b.isExpanded());
    assertTrue(b.isExpandedLazy());
    assertFalse(c.isExpanded());
    assertFalse(c.isExpandedLazy());
    assertFalse(d.isExpanded());
    assertTrue(d.isExpandedLazy());
  }

  public static class P_Tree extends AbstractTree {
    ITreeNode m_currentDropNode;
    int m_execDropTargetChangedTimesCalled;

    @Override
    protected void execDropTargetChanged(ITreeNode node) {
      super.execDropTargetChanged(node);
      m_currentDropNode = node;
      m_execDropTargetChangedTimesCalled++;
    }
  }

  public static class P_TreeListener implements TreeListener {
    int m_notificationCounter = 0;
    ArrayList<TreeEvent> m_treeEvents = new ArrayList<>();

    @Override
    public void treeChanged(TreeEvent e) {
      ++m_notificationCounter;
      handleTreeEvent(e);
    }

    private void handleTreeEvent(TreeEvent e) {
      m_treeEvents.add(e);
    }
  }

  private interface ITestDisposable {
    boolean isDisposed();

    String getName();
  }

  public static class P_TreeNode extends AbstractTreeNode implements ITestDisposable {
    boolean m_disposeInternalCalled = false;
    boolean m_execDisposeCalled = false;

    private String m_name;

    public P_TreeNode(String name) {
      m_name = name;
    }

    @Override
    public String getName() {
      return m_name;
    }

    @Override
    public boolean isDisposed() {
      return m_disposeInternalCalled && m_execDisposeCalled;
    }

    @Override
    protected void execDispose() {
      m_execDisposeCalled = true;
    }

    @Override
    public void disposeInternal() {
      super.disposeInternal();
      m_disposeInternalCalled = true;
    }
  }

  public static class P_TestMenu extends AbstractMenu implements ITestDisposable {
    boolean m_disposed = false;
    private String m_name;

    public P_TestMenu(String name) {
      m_name = name;
    }

    @Override
    public String getName() {
      return m_name;
    }

    @Override
    public boolean isDisposed() {
      return m_disposed;
    }

    @Override
    public void disposeActionInternal() {
      super.disposeActionInternal();
      m_disposed = true;
    }
  }
}
