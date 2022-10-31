/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FormField, FormFieldModel, LogicalGridLayoutConfig, Menu, MenuModel, Notification, NotificationModel} from '../../../index';
import {MenuBarEllipsisPosition} from '../../../menu/menubar/MenuBar';
import {GroupBoxBorderDecoration, GroupBoxMenuBarPosition} from './GroupBox';
import {RefModel} from '../../../types';
import {LogicalGridLayoutConfigModel} from '../../../layout/logicalgrid/LogicalGridLayoutConfig';

export default interface GroupBoxModel extends FormFieldModel {
  fields?: (FormField | RefModel<FormFieldModel>)[];
  menuBarVisible?: boolean;
  menuBarPosition?: GroupBoxMenuBarPosition;
  menuBarEllipsisPosition?: MenuBarEllipsisPosition;
  notification?: Notification | RefModel<NotificationModel>;
  bodyLayoutConfig?: LogicalGridLayoutConfig | LogicalGridLayoutConfigModel;
  /**
   * Only has an effect if the border is visible which can be controlled using {@link borderVisible}.
   */
  borderDecoration?: GroupBoxBorderDecoration;
  borderVisible?: boolean;
  /**
   * Configures the text of the sub label. The sub label is usually displayed below the label.
   */
  subLabel?: string;
  /**
   * Configures whether this group box should be scrollable in vertical direction.
   * By default, this value is null which means a main-box of a form is scrollable, while all other boxes are false.
   * If you want a non-main-box to be scrollable, you have to set this groupbox to scrollable while setting the main-box to scrollable=false.
   */
  scrollable?: boolean;
  /**
   * Configures whether this group box should be expandable or not.
   * This property depends on the border decoration which can be configured using {@link borderDecoration}.
   */
  expandable?: boolean;
  expanded?: boolean;
  /**
   * Configures the number of columns used in this group box.
   * A typical {@link FormField} inside a group box spans one column. This behavior can be changed by setting {@link FormField.gridData.w}.
   * Default is 2 columns.
   */
  gridColumnCount?: number;
  staticMenus?: (Menu | MenuModel)[];
  /**
   * Configures the keystroke to select this group box (inside a tab-box).
   * If the groupbox is not inside a tab-box, this configured selection keyStroke will be ignored.
   */
  selectionKeystroke?: string;
  /**
   * If the property is set to true, the content of the group box will be adjusted to ensure best readability, when the width of the group box is less than its preferred size.
   * If the property is set to null, it will be true if the group box is the main box in a form and false otherwise.
   * The default is null which means all main-boxes are responsive and all others are not.
   */
  responsive?: boolean;
}
