/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Tile} from '../index';

export interface RemoteTileFilterOptions {
  tileIds?: string[];
}

export class RemoteTileFilter {
  synthetic: boolean;
  tileMap: Record<string, string>;

  constructor(options?: RemoteTileFilterOptions) {
    options = options || {};
    this.synthetic = true;
    this.tileMap = {};
    if (options.tileIds) {
      this.setTileIds(options.tileIds);
    }
  }

  setTileIds(tileIds: string[]) {
    this.tileMap = {};
    tileIds.forEach(tileId => {
      this.tileMap[tileId] = tileId;
    });
  }

  accept(tile: Tile): boolean {
    return !!this.tileMap[tile.id];
  }
}
