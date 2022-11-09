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
