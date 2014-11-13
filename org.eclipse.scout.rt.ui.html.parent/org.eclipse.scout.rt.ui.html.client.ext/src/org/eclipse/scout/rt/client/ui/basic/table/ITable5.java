package org.eclipse.scout.rt.client.ui.basic.table;

import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.IMenu;
import org.eclipse.scout.rt.client.ui.basic.table.columns.IColumn;
import org.eclipse.scout.rt.client.ui.basic.table.control.ITableControl;

/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/

public interface ITable5 extends ITable {
  String PROP_TABLE_STATUS_VISIBLE = "tableStatusVisible";
  String PROP_MENU_BAR_POSITION = "menubarPosition";

  public static final String MENUBAR_POSITION_BOTTOM = "bottom";

  List<ITableControl> getControls();

//FIXME move to ui facade.
  void fireTableReloadFromUI();

  void fireSortColumnRemovedFromUI(IColumn<?> column);

  Class<? extends IMenu> getDefaultMenu();

  boolean isTableStatusVisible();

  void setTableStatusVisible(boolean b);

  String getMenubarPosition();

  void setMenubarPosition(String position);
}
