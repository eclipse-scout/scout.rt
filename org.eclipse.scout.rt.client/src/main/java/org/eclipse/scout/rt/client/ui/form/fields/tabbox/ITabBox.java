/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.form.fields.tabbox;

import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.root.IFormFieldContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;

public interface ITabBox extends ICompositeField {

  /**
   * {@link IGroupBox}
   */
  String PROP_SELECTED_TAB = "selectedTab";

  String PROP_MARK_STRATEGY = "markStrategy";

  String PROP_TAB_AREA_STYLE = "tabAreaStyle";

  /**
   * Define the default mark strategy of the tabs. This means the marker is set on the tabs if at least one field is not
   * empty.
   */
  int MARK_STRATEGY_EMPTY = 0;

  /**
   * Define a optional mark strategy of the tabs. This means the marker is set on the tabs if at least one field is
   * marked as "save needed".
   */
  int MARK_STRATEGY_SAVE_NEEDED = 1;

  /**
   * Aligns the tabs on the left.
   */
  String TAB_AREA_STYLE_DEFAULT = "default";

  /**
   * Spreads the tabs evenly over the available space. If there isn't enough space, the tabs still move to the overflow
   * menu.
   */
  String TAB_AREA_STYLE_SPREAD_EVEN = "spreadEven";

  /*
   * Runtime
   */
  List<IGroupBox> getGroupBoxes();

  IGroupBox getSelectedTab();

  void setSelectedTab(IGroupBox box);

  int getMarkStrategy();

  void setMarkStrategy(int markStrategy);

  @Override
  IFormFieldContextMenu getContextMenu();

  String getTabAreaStyle();

  void setTabAreaStyle(String tabAreaStyle);

  ITabBoxUIFacade getUIFacade();
}
