/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {InitModelOf, ObjectModel, ObjectWithType, scout, SomeRequired, Tile, TileGrid, TileGridMoveSupport} from '..';
import $ from 'jquery';

export class TileMoveHandler implements TileMovableModel, ObjectWithType {
  declare model: TileMovableModel;
  declare initModel: SomeRequired<this['model'], '$container'>;
  objectType: string;
  tileGrid: TileGrid;
  $container: JQuery;

  protected _mousemoveHandler: (event: JQuery.MouseDownEvent) => void;

  constructor(model: InitModelOf<TileMoveHandler>) {
    scout.assertParameter('model', model);
    scout.assertParameter('$container', model.$container);
    this.$container = model.$container;
    this.tileGrid = model.tileGrid;

    this._mousemoveHandler = this._onMoveTileMouseDown.bind(this);
  }

  init() {
    this.$container.addClass('movable');
    this.$container.on('mousedown touchstart', this._mousemoveHandler);
  }

  destroy() {
    if (this.$container) {
      this.$container.removeClass('movable');
      this.$container
        .off('mousedown touchstart', this._mousemoveHandler);
    }
  }

  protected _onMoveTileMouseDown(event: JQuery.MouseDownEvent) {
    let $target = $(event.target);
    if ($target.hasClass('resizable-handle')) {
      return;
    }
    let tile = scout.widget($(event.currentTarget)) as Tile;
    // Install move support for each drag operation so that a tile can be dragged even if another one is still finishing dragging
    new TileGridMoveSupport(this.tileGrid).start(event, this.tileGrid.tiles, tile);
  }
}

export interface TileMovableModel extends ObjectModel<TileMoveHandler> {
  $container?: JQuery;
  tileGrid: TileGrid;
}
