/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {ButtonTile, keys, PageTileGrid, RangeKeyStroke, ScoutKeyboardEvent, TileButton} from '../../../index';

export class PageTileGridSelectKeyStroke extends RangeKeyStroke {
  declare field: PageTileGrid;

  constructor(pageTileGrid: PageTileGrid) {
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
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      let index = event.which - keys['1'];
      let tiles = this._tiles();
      if (index < tiles.length && tiles[index].tileWidget instanceof TileButton) {
        let tileWidget = tiles[index].tileWidget as TileButton;
        return tileWidget.$fieldContainer;
      }
      return null;
    };
  }

  protected override _accept(event: ScoutKeyboardEvent & { _$element?: JQuery }): boolean {
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

  override handle(event: JQuery.KeyboardEventBase & { _$element?: JQuery }) {
    let tile = event._$element.data('widget') as ButtonTile;
    let tileWidget = tile.tileWidget as TileButton;
    tileWidget.doAction();
  }

  protected _tiles(): ButtonTile[] {
    return this.field.tiles;
  }
}
