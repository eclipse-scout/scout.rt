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
package org.eclipse.scout.rt.client.ui.desktop.outline.pages;

import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.ITable5;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.desktop.outline.IOutline5;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineEvent;
import org.eclipse.scout.rt.client.ui.desktop.outline.OutlineNavigation;

public abstract class AbstractPageWithTable5<T extends ITable5> extends AbstractPageWithTable<T> implements IPage5 {

  private boolean m_detailFormVisible = true;

  @Override
  protected void initConfig() {
    super.initConfig();
    getTable().setTableStatusVisible(true);
  }

  @Override
  public void setTreeInternal(ITree tree, boolean includeSubtree) {
    super.setTreeInternal(tree, includeSubtree);

    // FIXME CGU: how to do this properly? Das menu dürfte auch nicht auf den Baum synchronisiert werden
    if (getOutline() != null && getTable().getDefaultMenu() == null && !isLeaf()) {
      List<IMenu> tableMenus = getTable().getContextMenu().getChildActions();
      tableMenus.add(0, OutlineNavigation.createUp(getOutline()));
      tableMenus.add(1, OutlineNavigation.createDown(getOutline()));
      getTable().getContextMenu().setChildActions(tableMenus);
    }
  }

  // TODO AWE: (scout) gemeinsame basis klasse für page5, copy/paste für detailForm|TableVisible entfernen

  @Override
  public boolean isDetailFormVisible() {
    return m_detailFormVisible;
  }

  @Override
  public void setDetailFormVisible(boolean visible) {
    boolean oldVisible = m_detailFormVisible;
    if (oldVisible != visible) {
      m_detailFormVisible = visible;
      fireOutlineEvent(OutlineEvent.TYPE_PAGE_CHANGED);
    }
  }

  @Override
  public void setTableVisible(boolean visible) {
    boolean oldVisible = isTableVisible();
    if (oldVisible != visible) {
      super.setTableVisible(visible);
      fireOutlineEvent(OutlineEvent.TYPE_PAGE_CHANGED);
    }
  }

  /**
   * Note: set*Visible methods are called by initConfig(), at this point getTree() is still null.
   */
  private void fireOutlineEvent(int eventType) {
    if (getTree() != null) {
      ((IOutline5) getOutline()).fireOutlineEvent(new OutlineEvent(getTree(), eventType, this));
    }
  }

}
