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
