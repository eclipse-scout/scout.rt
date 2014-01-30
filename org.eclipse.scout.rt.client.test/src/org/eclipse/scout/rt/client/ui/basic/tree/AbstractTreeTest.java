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

import java.util.ArrayList;

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

  @Before
  public void setup() {
    P_Tree tree = new P_Tree();
    m_node = new AbstractTreeNode() {
    };
    tree.addChildNode(tree.getRootNode(), m_node);
    m_treeListener = new P_TreeListener();
    tree.addTreeListener(m_treeListener);
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
      if (e.getType() == TreeEvent.TYPE_NODE_CHANGED) {
        m_treeEvents.add(e);
      }
    }

    @Override
    public void treeChangedBatch(TreeEvent[] batch) {
      ++m_notificationCounter;
      for (TreeEvent e : batch) {
        handleTreeEvent(e);
      }
    }
  }

}
