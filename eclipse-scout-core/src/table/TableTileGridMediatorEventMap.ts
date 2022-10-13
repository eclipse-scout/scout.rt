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
import {PropertyChangeEvent, TableRowTileMapping, Tile, TileGridLayoutConfig, WidgetEventMap} from '../index';

export default interface TableTileGridMediatorEventMap extends WidgetEventMap {
  'propertyChange:gridColumnCount': PropertyChangeEvent<number>;
  'propertyChange:tileGridLayoutConfig': PropertyChangeEvent<TileGridLayoutConfig>;
  'propertyChange:tileMappings': PropertyChangeEvent<TableRowTileMapping[]>;
  'propertyChange:tiles': PropertyChangeEvent<Tile[]>;
  'propertyChange:withPlaceholders': PropertyChangeEvent<boolean>;
}
