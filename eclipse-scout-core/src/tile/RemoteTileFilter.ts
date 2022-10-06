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
export default class RemoteTileFilter {

  constructor(model) {
    model = model || {};
    this.synthetic = true;
    this.tileMap = {};
    if (model.tileIds) {
      this.setTileIds(model.tileIds);
    }
  }

  setTileIds(tileIds) {
    this.tileMap = {};
    tileIds.forEach(function(tileId) {
      this.tileMap[tileId] = tileId;
    }, this);
  }

  accept(tile) {
    return !!this.tileMap[tile.id];
  }
}
