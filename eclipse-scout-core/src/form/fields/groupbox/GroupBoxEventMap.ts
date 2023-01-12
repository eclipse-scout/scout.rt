/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormField, FormFieldEventMap, GroupBoxBorderDecoration, GroupBoxMenuBarPosition, LogicalGridLayoutConfig, Menu, MenuBarEllipsisPosition, PropertyChangeEvent} from '../../../index';

export interface GroupBoxEventMap extends FormFieldEventMap {
  'propertyChange:bodyLayoutConfig': PropertyChangeEvent<LogicalGridLayoutConfig>;
  'propertyChange:borderDecoration': PropertyChangeEvent<GroupBoxBorderDecoration>;
  'propertyChange:borderVisible': PropertyChangeEvent<boolean>;
  'propertyChange:expandable': PropertyChangeEvent<boolean>;
  'propertyChange:expanded': PropertyChangeEvent<boolean>;
  'propertyChange:fields': PropertyChangeEvent<FormField[]>;
  'propertyChange:gridColumnCount': PropertyChangeEvent<number>;
  'propertyChange:mainBox': PropertyChangeEvent<boolean>;
  'propertyChange:menuBarEllipsisPosition': PropertyChangeEvent<MenuBarEllipsisPosition>;
  'propertyChange:menuBarPosition': PropertyChangeEvent<GroupBoxMenuBarPosition>;
  'propertyChange:menuBarVisible': PropertyChangeEvent<boolean>;
  'propertyChange:notification': PropertyChangeEvent<Notification>;
  'propertyChange:responsive': PropertyChangeEvent<boolean>;
  'propertyChange:scrollable': PropertyChangeEvent<boolean>;
  'propertyChange:staticMenus': PropertyChangeEvent<Menu[]>;
  'propertyChange:subLabel': PropertyChangeEvent<string>;
}
