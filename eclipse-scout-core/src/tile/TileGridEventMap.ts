/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, Menu, PropertyChangeEvent, Tile, TileGrid, TileGridLayoutConfig, WidgetEventMap} from '../index';

export interface TileActionEvent<T = TileGrid> extends Event<T> {
  tile: Tile;
}

export interface TileClickEvent<T = TileGrid> extends Event<T> {
  tile: Tile;
  mouseButton: number;
  originalEvent: JQuery.ClickEvent;
}

export interface TileGridEventMap extends WidgetEventMap {
  'tileAction': TileActionEvent;
  'tileClick': TileClickEvent;
  'layoutDone': Event;
  'layoutAnimationDone': Event;
  'propertyChange:empty': PropertyChangeEvent<boolean>;
  'propertyChange:filteredTiles': PropertyChangeEvent<Tile[]>;
  'propertyChange:gridColumnCount': PropertyChangeEvent<number>;
  'propertyChange:layoutConfig': PropertyChangeEvent<TileGridLayoutConfig>;
  'propertyChange:menus': PropertyChangeEvent<Menu[]>;
  'propertyChange:multiSelect': PropertyChangeEvent<boolean>;
  'propertyChange:scrollable': PropertyChangeEvent<boolean>;
  'propertyChange:selectable': PropertyChangeEvent<boolean>;
  'propertyChange:selectedTiles': PropertyChangeEvent<Tile[]>;
  'propertyChange:textFilterEnabled': PropertyChangeEvent<boolean>;
  'propertyChange:tiles': PropertyChangeEvent<Tile[]>;
  'propertyChange:viewRangeSize': PropertyChangeEvent<number>;
  'propertyChange:virtual': PropertyChangeEvent<boolean>;
  'propertyChange:withPlaceholders': PropertyChangeEvent<boolean>;
}
