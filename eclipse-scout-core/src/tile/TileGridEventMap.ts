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
}

export default interface TileGridEventMap extends WidgetEventMap {
  'tileAction': TileActionEvent;
  'tileClick': TileClickEvent;
  'propertyChange:empty': PropertyChangeEvent<boolean, TileGrid>;
  'propertyChange:filteredTiles': PropertyChangeEvent<Tile[], TileGrid>;
  'propertyChange:gridColumnCount': PropertyChangeEvent<number, TileGrid>;
  'propertyChange:layoutConfig': PropertyChangeEvent<TileGridLayoutConfig, TileGrid>;
  'propertyChange:menus': PropertyChangeEvent<Menu[], TileGrid>;
  'propertyChange:multiSelect': PropertyChangeEvent<boolean, TileGrid>;
  'propertyChange:scrollable': PropertyChangeEvent<boolean, TileGrid>;
  'propertyChange:selectable': PropertyChangeEvent<boolean, TileGrid>;
  'propertyChange:selectedTiles': PropertyChangeEvent<Tile[], TileGrid>;
  'propertyChange:textFilterEnabled': PropertyChangeEvent<boolean, TileGrid>;
  'propertyChange:tiles': PropertyChangeEvent<Tile[], TileGrid>;
  'propertyChange:viewRangeSize': PropertyChangeEvent<number, TileGrid>;
  'propertyChange:virtual': PropertyChangeEvent<boolean, TileGrid>;
  'propertyChange:withPlaceholders': PropertyChangeEvent<boolean, TileGrid>;
}
