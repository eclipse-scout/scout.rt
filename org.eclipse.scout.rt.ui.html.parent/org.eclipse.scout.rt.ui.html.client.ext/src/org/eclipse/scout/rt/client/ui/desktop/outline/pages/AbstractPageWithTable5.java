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
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.action.menu.TableMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.ITable5;
import org.eclipse.scout.rt.client.ui.basic.tree.ITreeNode;
import org.eclipse.scout.rt.shared.ui.menu.AbstractMenu5;

public class AbstractPageWithTable5<T extends ITable5> extends AbstractPageWithTable<T> implements IPage5 {

  @Override
  protected void initConfig() {
    super.initConfig();
    //FIXME how to do this properly? Das menu dürfte auch nicht auf den Baum synchronisiert werden
    if (getTable().getDefaultMenu() == null && !isLeaf()) {
      List<IMenu> tableMenus = getTable().getContextMenu().getChildActions();
      tableMenus.add(0, new DrillDownMenu());
      getTable().getContextMenu().setChildActions(tableMenus);
    }
  }

  @Order(0)
  public class DrillDownMenu extends AbstractMenu5 {
    @Override
    protected String getConfiguredText() {
      return "Anzeigen"; //FIXME CGU translation
    }

    @Override
    protected Set<? extends IMenuType> getConfiguredMenuTypes() {
      return CollectionUtility.hashSet(TableMenuType.SingleSelection);
    }

    //FIXME CGU maybe do this in gui to make it more responsive?
    @Override
    protected void execAction() throws ProcessingException {
      ITreeNode node = null;

      node = getTreeNodeFor(getTable().getSelectedRow());
      if (node != null) {
        //FIXME UIFacade seems wrong...  copied from OutlineMediator
        getOutline().getUIFacade().setNodeSelectedAndExpandedFromUI(node);
      }
    }
  }
}
