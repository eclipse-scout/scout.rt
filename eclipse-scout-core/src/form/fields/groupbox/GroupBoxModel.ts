/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormField, FormFieldModel, GroupBoxBorderDecoration, GroupBoxMenuBarPosition, LogicalGridLayoutConfig, MenuBarEllipsisPosition, Notification, ObjectOrChildModel, ObjectOrModel} from '../../../index';

export interface GroupBoxModel extends FormFieldModel {
  /**
   * The fields to be displayed inside the group box.
   *
   * The fields are arranged by the {@link logicalGrid} based on their order and {@link FormFieldModel.gridDataHints}.
   */
  fields?: ObjectOrChildModel<FormField>[];
  /**
   * Defines whether the menu bar should be visible if there are menus
   *
   * Default is true.
   */
  menuBarVisible?: boolean;
  /**
   * Defines the position of the menu bar containing the {@link menus}.
   *
   * Default is {@link GroupBoxMenuBarPosition.AUTO} which means the UI will decide where it should be.
   */
  menuBarPosition?: GroupBoxMenuBarPosition;
  /**
   * Defines whether the {@link EllipsisMenu} should be shown on the left or right of the menu bar.
   * The ellipsis menu contains all menus that don't fit into the menu bar and is created automatically. If all menus fit, the ellipsis menu won't be shown.
   *
   * Default is {@link MenuBarEllipsisPosition.RIGHT}
   */
  menuBarEllipsisPosition?: MenuBarEllipsisPosition;
  /**
   * A {@link Notification} can be used to show an important message displayed prominently on top of the fields
   */
  notification?: ObjectOrChildModel<Notification>;
  /**
   * Configures layouting hints for the logical grid like the {@link LogicalGridLayoutConfig.rowHeight} or {@link LogicalGridLayoutConfig.columnWidth}
   * or the gaps between the cells ({@link LogicalGridLayoutConfig.hgap}, {@link LogicalGridLayoutConfig.vgap}).
   *
   * By default, an empty {@link LogicalGridLayoutConfig} is used which means the hints are read from CSS.
   */
  bodyLayoutConfig?: ObjectOrModel<LogicalGridLayoutConfig>;
  /**
   * Controls the space around the group box.
   *
   * @see borderVisible.
   */
  borderDecoration?: GroupBoxBorderDecoration;
  /**
   * Defines whether the padding on top and bottom of the group box should be visible.
   *
   * By default, the padding is invisible for root group boxes ({@link mainBox} set to true), unless {@link borderDecoration} is set to {@link borderDecoration.EMPTY} explicitly.
   * Otherwise it is visible.
   */
  borderVisible?: boolean;
  /**
   * Configures the text of the sub label. The sub label is usually displayed below the label.
   */
  subLabel?: string;
  /**
   * Configures whether this group box should be scrollable in vertical direction.
   *
   * By default, this value is null which means a main-box of a form is scrollable, while all other boxes are false.
   * If you want a non-main-box to be scrollable, you have to set this group box to scrollable while setting the main-box to scrollable=false.
   */
  scrollable?: boolean;
  /**
   * Configures whether the group box can be expanded by the user using a expand icon in the group box header.
   *
   * Default is false.
   *
   * @see expanded
   */
  expandable?: boolean;
  /**
   * Defines whether the group box is currently expanded.
   *
   * Default is true.
   *
   * @see expandable
   */
  expanded?: boolean;
  /**
   * Configures the number of columns used in this group box.
   *
   * A typical {@link FormField} inside a group box spans one column. This behavior can be changed by setting {@link FormField.gridData.w}.
   *
   * Default is 2 columns.
   */
  gridColumnCount?: number;
  /**
   * Configures whether this group box should be responsive.
   *
   * - If the property is set to true, the content of the group box will be adjusted to ensure the best readability, when the width of the group box is less than its preferred size.
   * - If the property is set to null, it will be true if the group box is the main box in a form and false otherwise.
   *
   * The default is null which means all main-boxes are responsive and all others are not.
   */
  responsive?: boolean;
}
