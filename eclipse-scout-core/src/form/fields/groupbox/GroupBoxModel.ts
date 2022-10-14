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
  borderDecoration?: GroupBoxBorderDecoration;
  borderVisible?: boolean;
  subLabel?: string;
  scrollable?: boolean;
  expandable?: boolean;
  expanded?: boolean;
  gridColumnCount?: number;
  staticMenus?: (Menu | MenuModel)[];
  selectionKeystroke?: string;
  responsive?: boolean;
}
