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
package org.eclipse.scout.rt.client.ui.action.menu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.fixture.OwnerValueCapturingMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.ITreeContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.internal.TreeContextMenu;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTree;
import org.eclipse.scout.rt.client.ui.basic.tree.AbstractTreeNode;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link TreeContextMenu}
 */
@RunWith(PlatformTestRunner.class)
public class TreeMenuTest {

  private OwnerValueCapturingMenu m_multi;
  private OwnerValueCapturingMenu m_single;
  private OwnerValueCapturingMenu m_emptySpace;
  private OwnerValueCapturingMenu m_singleEmpty;
  private OwnerValueCapturingMenu m_all;

  @Before
  public void before() {
    m_multi = new OwnerValueCapturingMenu(TreeMenuType.MultiSelection);
    m_single = new OwnerValueCapturingMenu(TreeMenuType.SingleSelection);
    m_emptySpace = new OwnerValueCapturingMenu(TreeMenuType.EmptySpace);
    m_singleEmpty = new OwnerValueCapturingMenu(TreeMenuType.EmptySpace, TreeMenuType.SingleSelection);
    m_all = new OwnerValueCapturingMenu(TreeMenuType.values());
  }

  /**
   * Tests that {@link AbstractMenu#execOwnerValueChanged} is only called only single-selection menus, if a single node
   * is selected.
   */
  @Test
  public void testOwnerValueOnSingleSelection() {
    final ContextMenuTree tree = createContextMenuTree();
    addTestMenus(tree);
    tree.selectFirstNode();

    assertOwnerValueChange(m_single, 1);
    assertOwnerValueChange(m_all, 1);
    assertOwnerValueChange(m_singleEmpty, 1);
    assertNoOwnerValueChange(m_multi);
    assertNoOwnerValueChange(m_emptySpace);
  }

  /**
   * Tests that {@link AbstractMenu#execOwnerValueChanged} is only called only multi-selection menus, if multiple nodes
   * are selected.
   */
  @Test
  public void testOwnerValueOnMultiSelection() {
    final ContextMenuTree tree = createContextMenuTree();
    addTestMenus(tree);
    tree.setMultiSelect(true);
    tree.selectNodes(tree.getRootNode().getChildNodes(), true);

    assertOwnerValueChange(m_multi, 2);
    assertOwnerValueChange(m_all, 2);
    assertNoOwnerValueChange(m_singleEmpty);
    assertNoOwnerValueChange(m_single);
    assertNoOwnerValueChange(m_emptySpace);
  }

  /**
   * Tests that {@link AbstractMenu#execOwnerValueChanged} is only called only empty space menus, if no nodes are
   * selected.
   */
  @Test
  public void testEmptySpaceSelection() {
    final ContextMenuTree tree = createContextMenuTree();
    tree.selectNode(tree.getRootNode());
    addTestMenus(tree);
    tree.deselectNode(tree.getRootNode());

    assertOwnerValueChange(m_emptySpace, 0);
    assertOwnerValueChange(m_all, 0);
    assertOwnerValueChange(m_singleEmpty, 0);
    assertNoOwnerValueChange(m_single);
    assertNoOwnerValueChange(m_multi);
  }

  private void assertOwnerValueChange(OwnerValueCapturingMenu menu, int rows) {
    assertEquals(1, menu.getCount());
    assertTrue("Owner should be a collection of 2 rows " + menu.getLastOwnerValue().getClass(), menu.getLastOwnerValue() instanceof Collection);
    assertEquals(rows, ((Collection) menu.getLastOwnerValue()).size());
  }

  private void assertNoOwnerValueChange(OwnerValueCapturingMenu menu) {
    assertEquals(0, menu.getCount());
  }

  private void addTestMenus(ContextMenuTree tree) {
    List<IMenu> menus = new ArrayList<>();
    menus.add(m_emptySpace);
    menus.add(m_single);
    menus.add(m_multi);
    menus.add(m_all);
    menus.add(m_singleEmpty);
    tree.setContextMenuInternal(new TreeContextMenu(tree, menus));
  }

  private ContextMenuTree createContextMenuTree() {
    final ContextMenuTree tree = new ContextMenuTree();
    tree.addChildNode(tree.getRootNode(), new AbstractTreeNode() {
    });
    tree.addChildNode(tree.getRootNode(), new AbstractTreeNode() {
    });
    tree.getRootNode().setExpanded(true);
    return tree;
  }

  private static class ContextMenuTree extends AbstractTree {

    @Override
    public void setContextMenuInternal(ITreeContextMenu contextMenu) {
      super.setContextMenuInternal(contextMenu);
    }
  }

}
