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
package org.eclipse.scout.rt.client.ui.form.fields.tabbox;

import java.util.List;

import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenu;
import org.eclipse.scout.rt.client.ui.action.menu.root.IContextMenuOwner;
import org.eclipse.scout.rt.client.ui.action.menu.root.IFormFieldContextMenu;
import org.eclipse.scout.rt.client.ui.form.fields.ICompositeField;
import org.eclipse.scout.rt.client.ui.form.fields.groupbox.IGroupBox;

public interface ITabBox extends ICompositeField, IContextMenuOwner {

  /**
   * {@link IGroupBox}
   */
  String PROP_SELECTED_TAB = "selectedTab";

  String PROP_MARK_STRATEGY = "markStrategy";

  /**
   * {@link IContextMenu}
   */
  String PROP_CONTEXT_MENU = "contextMenu";

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

  ITabBoxUIFacade getUIFacade();
}
