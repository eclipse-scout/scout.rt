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
package org.eclipse.scout.rt.client.ui.action.menu;

import java.util.Set;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

/**
 * Interface for menus that normally appear in the gui on the menubar
 */
public interface IMenu extends IActionNode<IMenu> {
  String PROP_MENU_TYPES = "propMenuTypes";

  /**
   * A menu can have several {@link IMenuType}s each menu type describes a certain usage in a specific context (e.g.
   * {@link ITable}, {@link ITree}, {@link IValueField} ) of the menu.
   *
   * @return all menu types for this menu.
   */
  Set<IMenuType> getMenuTypes();

  /**
   * called from the UI before the menu is displayed.
   *
   * @see AbstractMenu#execAboutToShow()
   */
  void aboutToShow();

  /**
   * @param newValue
   * @throws ProcessingException
   */
  void handleOwnerValueChanged(Object newValue) throws ProcessingException;

  /**
   * action is chosen on a single selected item
   *
   * @deprecated will be removed with V 5.0 use {@link AbstractMenu#execOwnerValueChanged(Object)} instead
   */
  @Deprecated
  boolean isSingleSelectionAction();

  /**
   * @deprecated will be removed with V 5.0 use {@link AbstractMenu#execOwnerValueChanged(Object)} instead
   */
  @Deprecated
  void setSingleSelectionAction(boolean b);

  /**
   * action is chosen on any of multiple (>=2) selected items
   *
   * @deprecated will be removed with V 5.0 use {@link AbstractMenu#execOwnerValueChanged(Object)} instead
   */
  @Deprecated
  boolean isMultiSelectionAction();

  /**
   * @deprecated will be removed with V 5.0 use {@link AbstractMenu#execOwnerValueChanged(Object)} instead
   */
  @Deprecated
  void setMultiSelectionAction(boolean b);

  /**
   * action is chosen on empty space (not on items)
   *
   * @deprecated will be removed with V 5.0 use {@link AbstractMenu#execOwnerValueChanged(Object)} instead
   */
  @Deprecated
  boolean isEmptySpaceAction();

  /**
   * @deprecated will be removed with V 5.0 use {@link AbstractMenu#execOwnerValueChanged(Object)} instead
   */
  @Deprecated
  void setEmptySpaceAction(boolean b);

}
