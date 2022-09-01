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
import {PropertyChangeEvent, TableRowTileMapping, TableTileGridMediator, Tile, TileGridLayoutConfig, WidgetEventMap} from '../index';

export default interface TableTileGridMediatorEventMap extends WidgetEventMap {
  'propertyChange:gridColumnCount': PropertyChangeEvent<number, TableTileGridMediator>;
  'propertyChange:tileGridLayoutConfig': PropertyChangeEvent<TileGridLayoutConfig, TableTileGridMediator>;
  'propertyChange:tileMappings': PropertyChangeEvent<TableRowTileMapping[], TableTileGridMediator>;
  'propertyChange:tiles': PropertyChangeEvent<Tile[], TableTileGridMediator>;
  'propertyChange:withPlaceholders': PropertyChangeEvent<boolean, TableTileGridMediator>;
}
