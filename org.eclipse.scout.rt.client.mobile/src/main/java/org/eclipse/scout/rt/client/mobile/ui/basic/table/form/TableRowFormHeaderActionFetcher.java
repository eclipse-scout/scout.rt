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
package org.eclipse.scout.rt.client.mobile.ui.basic.table.form;

import java.util.List;
import java.util.Set;

import org.eclipse.scout.rt.client.mobile.ui.form.AbstractMobileAction;
import org.eclipse.scout.rt.client.mobile.ui.form.FormHeaderActionFetcher;
import org.eclipse.scout.rt.client.mobile.ui.form.IMobileAction;
import org.eclipse.scout.rt.client.ui.action.ActionUtility;
import org.eclipse.scout.rt.client.ui.action.IActionFilter;
import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.action.menu.IMenuType;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.form.IForm;

/**
 * Additionally fetches the actions of the table row and places them on the right side.
 */
public class TableRowFormHeaderActionFetcher extends FormHeaderActionFetcher {

  private ITable m_table;

  public TableRowFormHeaderActionFetcher(IForm form, ITable table) {
    super(form);

    m_table = table;
  }

  public ITable getTable() {
    return m_table;
  }

  @Override
  public List<IMenu> fetch() {
    List<IMenu> headerActions = super.fetch();
    Set<? extends IMenuType> currentMenuTypes = getTable().getContextMenu().getCurrentMenuTypes();
    IActionFilter actionFilter = ActionUtility.createMenuFilterMenuTypes(currentMenuTypes, true);
    List<IMenu> tableRowActions = ActionUtility.getActions(getTable().getMenus(), actionFilter);
    for (IMenu action : tableRowActions) {
      AbstractMobileAction.setHorizontalAlignment(action, IMobileAction.HORIZONTAL_ALIGNMENT_RIGHT);
    }
    headerActions.addAll(0, tableRowActions);

    return headerActions;
  }

}
