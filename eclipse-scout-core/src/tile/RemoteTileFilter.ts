/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
