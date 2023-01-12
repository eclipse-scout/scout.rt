/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Alignment, Column, Event, PropertyChangeEvent, PropertyEventMap, TableHeaderMenu} from '../../index';

export interface ColumnHeaderMenuOpenEvent<T = Column> extends Event<T> {
  menu: TableHeaderMenu;
}

export interface ColumnHeaderMenuCloseEvent<T = Column> extends Event<T> {
  menu: TableHeaderMenu;
}

export interface ColumnEventMap extends PropertyEventMap {
  'headerMenuOpen': ColumnHeaderMenuOpenEvent;
  'headerMenuClose': ColumnHeaderMenuCloseEvent;
  'propertyChange:autoOptimizeWidth': PropertyChangeEvent<boolean>;
  'propertyChange:compacted': PropertyChangeEvent<boolean>;
  'propertyChange:cssClass': PropertyChangeEvent<string>;
  'propertyChange:displayable': PropertyChangeEvent<boolean>;
  'propertyChange:editable': PropertyChangeEvent<boolean>;
  'propertyChange:headerCssClass': PropertyChangeEvent<string>;
  'propertyChange:headerHtmlEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:headerIconId': PropertyChangeEvent<string>;
  'propertyChange:headerTooltipHtmlEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:headerTooltipText': PropertyChangeEvent<string>;
  'propertyChange:horizontalAlignment': PropertyChangeEvent<Alignment>;
  'propertyChange:mandatory': PropertyChangeEvent<boolean>;
  'propertyChange:maxLength': PropertyChangeEvent<number>;
  'propertyChange:text': PropertyChangeEvent<string>;
  'propertyChange:textWrap': PropertyChangeEvent<boolean>;
  'propertyChange:visible': PropertyChangeEvent<boolean>;
  'propertyChange:width': PropertyChangeEvent<number>;
}
