/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.shared.data.basic.FontSpec;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link AbstractTree}
 */
public class AbstractTreeTest {

  private AbstractTreeNode m_node;
  private P_TreeListener m_treeListener;
  private P_Tree m_tree;

  @Before
  public void setup() {
    m_tree = new P_Tree();
    m_node = new AbstractTreeNode() {
    };
    m_tree.addChildNode(m_tree.getRootNode(), m_node);
    m_treeListener = new P_TreeListener();
    m_tree.addTreeListener(m_treeListener);
  }

  @Test
  public void testNodeChangedSingleEvents() {

    m_node.getCellForUpdate().setText("foo");
    assertNotifications(1, 1);

    // expected no notification after setting same value again
    m_node.getCellForUpdate().setText("foo");
    assertNotifications(1, 1);

    m_node.getCellForUpdate().setText("foo2");
    assertNotifications(2, 2);

    m_node.getCellForUpdate().setBackgroundColor("FFFF00");
    m_node.getCellForUpdate().setForegroundColor("00FF00");
    m_node.getCellForUpdate().setFont(new FontSpec("Arial", FontSpec.STYLE_BOLD, 7));
    assertNotifications(5, 5);
  }

  @Test
  public void testNodeDropTargetChanged() throws ProcessingException {
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
      m_node.getTree().setTreeChanging(true);
      m_node.getCellForUpdate().setText("foo");
      // expected no notification after setting same value again
      m_node.getCellForUpdate().setText("foo");
      m_node.getCellForUpdate().setText("foo2");
      m_node.getCellForUpdate().setBackgroundColor("FFFF00");
      m_node.getCellForUpdate().setForegroundColor("00FF00");
      m_node.getCellForUpdate().setFont(new FontSpec("Arial", FontSpec.STYLE_BOLD, 7));
      assertNotifications(0, 0);
    }
    finally {
      m_node.getTree().setTreeChanging(false);
    }
    assertNotifications(1, 5);

  }

  private void assertNotifications(int expectedNotifications, int expectedEvents) {
    assertEquals("wrong number of notifications", expectedNotifications, m_treeListener.m_notificationCounter);
    assertEquals("wrong number of events", expectedEvents, m_treeListener.m_treeEvents.size());
    for (TreeEvent e : m_treeListener.m_treeEvents) {
      Assert.assertSame("expected node to be included in tree event", m_node, e.getNode());
    }
  }

  public static class P_Tree extends AbstractTree {
    ITreeNode m_currentDropNode;
    int m_execDropTargetChangedTimesCalled;

    @Override
    protected void execDropTargetChanged(ITreeNode node) throws ProcessingException {
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

}
