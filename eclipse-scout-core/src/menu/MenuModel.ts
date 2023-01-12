/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ActionModel, Menu, MenuFilter, ObjectOrChildModel, PopupAlignment, SubMenuVisibility} from '../index';

export interface MenuModel extends ActionModel {
  childActions?: ObjectOrChildModel<Menu>[];
  /**
   * A menu type provides a way to control the visibility of a menu for common use cases.
   * For example: a menu should be displayed only if an item is selected or a value present.
   *
   * Menu types are context specific and interpreted by the menu container (e.g. by a table or form field).
   *
   * Please refer to the respective container for the available menu types and their functions.
   */
  menuTypes?: string[];
  /**
   * Whether the menu should get a look that stands out so the user notices it (call-to-action button).
   * In a {@link MenuBar}, only one menu / button can have that look. Ideally, that button is mapped to the `enter` keystroke as well.
   *
   * That is why the property will be set automatically by the {@link MenuBar} and does not need to be set explicitly.
   * To set the property, the menu bar searches the first menu with an `enter` keystroke and makes that menu the default menu, unless `defaultMenu` is explicitly set to `false`.
   * If a menu is explicitly set to `true`, that menu will be the default menu regardless of the keystroke.
   *
   * Default is null which means the menu bar will consider it as default menu.
   */
  defaultMenu?: boolean;
  popupHorizontalAlignment?: PopupAlignment;
  popupVerticalAlignment?: PopupAlignment;
  /**
   * Configures whether the menu should be stackable.
   * A stackable menu will be stacked in a dropdown menu if there is not enough
   * space in the menubar. This property is usually set to false for right aligned menus with only an icon.
   *
   * Default is true.
   */
  stackable?: boolean;
  /**
   * Default is false
   */
  separator?: boolean;
  /**
   * Configures whether the menu should be shrinkable.
   * Shrinking means that the text will be removed and only the icon kept when there is not enough space in the menubar.
   *
   * Default is false.
   */
  shrinkable?: boolean;
  /**
   * Configures the behavior when the sub-menu icon is visible.
   * A menu must have child-actions in order to display the sub-menu icon.
   * By default, the sub-menu icon is shown only when the menu has text. You may change that behavior by configuring another visibility.
   *
   * Default is {@link Menu.SubMenuVisibility.DEFAULT}.
   */
  subMenuVisibility?: SubMenuVisibility;
  menuFilter?: MenuFilter;
}
