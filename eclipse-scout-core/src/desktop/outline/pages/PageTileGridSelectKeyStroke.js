/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {keys, RangeKeyStroke, TileButton} from '../../../index';

export default class PageTileGridSelectKeyStroke extends RangeKeyStroke {

  constructor(pageTileGrid) {
    super();
    this.field = pageTileGrid;

    // range [1..9]
    this.registerRange(
      keys['1'], // range from
      () => {
        return keys[Math.min(this._tiles().length, 9)]; // range to
      }
    );

    // rendering hints
    this.renderingHints.$drawingArea = function($drawingArea, event) {
      let index = event.which - keys['1'];
      let tiles = this._tiles();
      if (index < tiles.length && tiles[index].tileWidget instanceof TileButton) {
        return tiles[index].tileWidget.$fieldContainer;
      }
      return null;
    }.bind(this);
  }

  /**
   * @override
   */
  _accept(event) {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }

    if (this.field && this.field.outline && this.field.outline.inBackground) {
      return false;
    }

    let index = keys.codesToKeys[event.which] - 1;
    let tiles = this._tiles();

    if (index < tiles.length && tiles[index].tileWidget instanceof TileButton) {
      event._$element = tiles[index].$container;
      if (event._$element) {
        return true;
      }
    }
    return false;
  }

  /**
   * @override
   */
  handle(event) {
    let tile = event._$element.data('widget');
    tile.tileWidget.doAction();
  }

  _tiles() {
    return this.field.tiles;
  }
}
