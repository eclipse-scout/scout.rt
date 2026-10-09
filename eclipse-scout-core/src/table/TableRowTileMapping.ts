/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {PropertyChangeEvent, TableRowTileMappingModel, Tile, Widget, WidgetEventMap} from '../index';

export class TableRowTileMapping extends Widget implements TableRowTileMappingModel {
  declare model: TableRowTileMappingModel;
  declare eventMap: TableRowTileMappingEventMap;
  declare self: TableRowTileMapping;

  tableRow: string;
  tile: Tile;

  constructor() {
    super();
    this.tableRow = null;
    this.tile = null;
    this._addWidgetProperties(['tile']);
  }

  setTile(tile: Tile) {
    this.setProperty('tile', tile);
  }
}

export interface TableRowTileMappingEventMap extends WidgetEventMap {
  'propertyChange:tile': PropertyChangeEvent<Tile>;
}
