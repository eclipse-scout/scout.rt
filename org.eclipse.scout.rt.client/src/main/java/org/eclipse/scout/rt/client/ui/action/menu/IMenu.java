/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.action.menu;

import java.util.Set;

import org.eclipse.scout.rt.client.ui.action.tree.IActionNode;
import org.eclipse.scout.rt.client.ui.basic.table.ITable;
import org.eclipse.scout.rt.client.ui.basic.tree.ITree;
import org.eclipse.scout.rt.client.ui.form.fields.IValueField;

/**
 * Interface for menus that normally appear in the gui on the menubar
 */
public interface IMenu extends IActionNode<IMenu> {

  String PROP_MENU_TYPES = "menuTypes";
  String PROP_PREVENT_DOUBLE_CLICK = "preventDoubleClick";
  String PROP_STACKABLE = "stackable";
  String PROP_SHRINKABLE = "shrinkable";
  String PROP_SUB_MENU_VISIBILITY = "subMenuVisibility";

  /**
   * Sub-menu icon is only visible when the menu has child-actions and a text.
   */
  String SUB_MENU_VISIBILITY_DEFAULT = "default";
  /**
   * Sub-menu icon is only visible when the menu has child-actions and a text or an icon.
   */
  String SUB_MENU_VISIBILITY_TEXT_OR_ICON = "textOrIcon";
  /**
   * Sub-menu icon is visible when the menu has child-actions.
   */
  String SUB_MENU_VISIBILITY_ALWAYS = "always";

  /**
   * Sub-menu icon is never visible.
   */
  String SUB_MENU_VISIBILITY_NEVER = "never";

  /**
   * A menu can have several {@link IMenuType}s each menu type describes a certain usage in a specific context (e.g.
   * {@link ITable}, {@link ITree}, {@link IValueField} ) of the menu.
   *
   * @return all menu types for this menu.
   */
  Set<IMenuType> getMenuTypes();

  boolean isPreventDoubleClick();

  void setPreventDoubleClick(boolean preventDoubleClick);

  boolean isStackable();

  /**
   * A stackable menu will be stacked in a dropdown menu if there is not enough space in the menubar. This property is
   * usually set to false for right aligned menus with only an icon.
   */
  void setStackable(boolean stackable);

  boolean isShrinkable();

  /**
   * A shrinkable menu will be displayed without text but only with its configured icon if there is not enough space in
   * the menubar.
   */
  void setShrinkable(boolean shrinkable);

  String getSubMenuVisibility();

  void setSubMenuVisibility(String subMenuVisibility);

  Object getOwnerValue();

  /**
   * @param newValue
   */
  void handleOwnerValueChanged(Object newValue);
}
