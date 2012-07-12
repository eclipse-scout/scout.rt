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
package org.eclipse.scout.rt.ui.rap.mobile.form.fields.treefield;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.scout.rt.client.mobile.ui.action.ActionButtonBarUtility;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeAdapter;
import org.eclipse.scout.rt.client.ui.basic.tree.TreeEvent;
import org.eclipse.scout.rt.client.ui.form.fields.treefield.ITreeField;
import org.eclipse.scout.rt.ui.rap.LogicalGridData;
import org.eclipse.scout.rt.ui.rap.RwtMenuUtility;
import org.eclipse.scout.rt.ui.rap.form.fields.LogicalGridDataBuilder;
import org.eclipse.scout.rt.ui.rap.mobile.action.AbstractRwtScoutActionBar;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @since 3.8.0
 */
public class RwtScoutTreeActionBar extends AbstractRwtScoutActionBar<ITreeField> {
  private P_TreeNodeSelectionListener m_nodeSelectionListener;
  private ITree m_tree;

  public RwtScoutTreeActionBar() {
    setMenuOpeningDirection(SWT.UP);
  }

  @Override
  protected void initLayout(Composite container) {
    super.initLayout(container);

    LogicalGridData tableGridData = LogicalGridDataBuilder.createField(getScoutObject().getGridData());
    LogicalGridData gd = new LogicalGridData();
    gd.gridx = tableGridData.gridx;
    gd.gridy = tableGridData.gridy + tableGridData.gridh;
    gd.gridw = tableGridData.gridw;
    gd.topInset = 0;
    gd.gridh = 1;
    if (getHeightHint() != null) {
      gd.heightHint = getHeightHint();
    }
    else {
      gd.useUiHeight = true;
    }
    gd.weightx = tableGridData.weightx;
    gd.weighty = 0.0;
    gd.fillHorizontal = true;
    container.setLayoutData(gd);
  }

  @Override
  protected void collectMenusForLeftButtonBar(List<IMenu> menuList) {
    ITree tree = getScoutObject().getTree();
    if (tree == null) {
      return;
    }

    IMenu[] emptySpaceMenus = RwtMenuUtility.collectEmptySpaceMenus(tree, getUiEnvironment());
    if (emptySpaceMenus != null) {
      menuList.addAll(Arrays.asList(emptySpaceMenus));
    }

    IMenu[] rowMenus = RwtMenuUtility.collectNodeMenus(tree, getUiEnvironment());
    if (rowMenus != null) {
      List<IMenu> rowMenuList = new LinkedList<IMenu>(Arrays.asList(rowMenus));

      ActionButtonBarUtility.distributeRowActions(menuList, emptySpaceMenus, rowMenuList);

      //Add remaining row menus
      menuList.addAll(rowMenuList);
    }
  }

  @Override
  protected void collectMenusForRightButtonBar(List<IMenu> menuList) {
  }

  @Override
  protected void attachScout() {
    super.attachScout();

    m_tree = getScoutObject().getTree();

    addRowSelectionListener(m_tree);
  }

  @Override
  protected void detachScout() {
    super.detachScout();

    removeRowSelectionListener(m_tree);

    m_tree = null;
  }

  private void addRowSelectionListener(ITree tree) {
    if (m_nodeSelectionListener != null || tree == null) {
      return;
    }

    m_nodeSelectionListener = new P_TreeNodeSelectionListener();
    tree.addTreeListener(m_nodeSelectionListener);
  }

  private void removeRowSelectionListener(ITree tree) {
    if (m_nodeSelectionListener == null || tree == null) {
      return;
    }

    tree.removeTreeListener(m_nodeSelectionListener);
    m_nodeSelectionListener = null;
  }

  @Override
  protected void handleScoutPropertyChange(String name, Object newValue) {
    super.handleScoutPropertyChange(name, newValue);
    if (name.equals(ITreeField.PROP_TREE)) {
      removeRowSelectionListener(m_tree);

      m_tree = (ITree) newValue;

      addRowSelectionListener(m_tree);
    }
  }

  private class P_TreeNodeSelectionListener extends TreeAdapter {

    @Override
    public void treeChanged(TreeEvent e) {
      if (e.getType() == TreeEvent.TYPE_NODES_SELECTED) {
        rowSelected();
      }
    }

    private void rowSelected() {
      rebuildContentFromScout();
    }

  }

}
