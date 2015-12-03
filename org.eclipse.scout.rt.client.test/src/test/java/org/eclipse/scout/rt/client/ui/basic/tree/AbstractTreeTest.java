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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.rt.client.extension.ui.basic.tree.AbstractTreeNodeExtension;
import org.eclipse.scout.rt.client.extension.ui.basic.tree.TreeNodeChains.TreeNodeDisposeChain;
import org.eclipse.scout.rt.client.ui.action.menu.AbstractMenu;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.eclipse.scout.rt.shared.extension.IExtensionRegistry;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.After;
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
    BEANS.get(IExtensionRegistry.class).register(TreeNodeExtension.class);
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

  @After
  public void tearDown() {
    BEANS.get(IExtensionRegistry.class).deregister(TreeNodeExtension.class);
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
    assertEquals("wrong number of notifications", 1, m_treeListener.m_notificationCounter);
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
  public void testInitConfig_DefaultValues() throws Exception {
    m_tree.initConfig();
    assertTrue(m_tree.isEnabled());
  }

  @Test
  public void testDisposeTree() {
    m_tree.disposeTree();
    assertDisposed(m_node1, m_node2, m_subNode1, m_node1Menu1, m_subNode1Menu1, m_subNode1Menu2);
  }

  @Test
  public void testDisposeRemovedTreeNodes() {
    m_tree.removeNode(m_subNode1);
    assertNotDisposed(m_node1, m_node2, m_node1Menu1);
    assertDisposed(m_subNode1, m_subNode1Menu1, m_subNode1Menu2);

    m_tree.disposeTree();
    assertDisposed(m_node1, m_node2, m_subNode1, m_node1Menu1, m_subNode1Menu1, m_subNode1Menu2);
  }

  @Test
  public void testDisposeRemovedTreeNodesRecursive() {
    m_tree.removeNode(m_node2);
    assertNotDisposed(m_node1, m_node1Menu1);
    assertDisposed(m_node2, m_subNode1, m_subNode1Menu1, m_subNode1Menu2);

    m_tree.disposeTree();
    assertDisposed(m_node1, m_node2, m_subNode1, m_node1Menu1, m_subNode1Menu1, m_subNode1Menu2);
  }

  @Test
  public void testDisposeRemovedTreeNodesInNonAutodiscardTree() {
    m_tree.setAutoDiscardOnDelete(false);
    m_tree.removeNode(m_node2);
    assertNotDisposed(m_node1, m_node2, m_subNode1, m_node1Menu1, m_subNode1Menu1, m_subNode1Menu2);

    m_tree.disposeTree();
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

    m_tree.disposeTree();
    assertDisposed(m_node1, m_node2, m_subNode1, m_node1Menu1, m_subNode1Menu1, m_subNode1Menu2);
  }

  private static void assertDisposed(ITestDisposable... disposables) {
    for (ITestDisposable disposable : disposables) {
      assertTrue("should be desposed, but is not: " + disposable.getName(), disposable.isDisposed());
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
    ArrayList<TreeEvent> m_treeEvents = new ArrayList<TreeEvent>();

    @Override
    public void treeChanged(TreeEvent e) {
      ++m_notificationCounter;
      handleTreeEvent(e);
    }

    private void handleTreeEvent(TreeEvent e) {
      m_treeEvents.add(e);
    }

    @Override
    public void treeChangedBatch(List<? extends TreeEvent> batch) {
      ++m_notificationCounter;
      for (TreeEvent e : batch) {
        handleTreeEvent(e);
      }
    }
  }

  private static interface ITestDisposable {
    boolean isDisposed();

    String getName();

  }

  public static class TreeNodeExtension extends AbstractTreeNodeExtension<P_TreeNode> {

    public TreeNodeExtension(P_TreeNode owner) {
      super(owner);
    }

    @Override
    public void execDispose(TreeNodeDisposeChain chain) {
      chain.execDispose();
      getOwner().additionalDispose();
    }
  }

  public static class P_TreeNode extends AbstractTreeNode implements ITestDisposable {
    boolean m_disposeInternalCalled = false;
    boolean m_execDisposeCalled = false;
    boolean m_additionalDisposeCalled = false;

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
      return m_disposeInternalCalled && m_execDisposeCalled && m_additionalDisposeCalled;
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

    public void additionalDispose() {
      m_additionalDisposeCalled = true;
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
    public void disposeInternal() {
      super.disposeInternal();
      m_disposed = true;
    }
  }

}
