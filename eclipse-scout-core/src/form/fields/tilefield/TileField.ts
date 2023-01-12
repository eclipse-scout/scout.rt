/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {EventDelegator, FormField, InitModelOf, LoadingSupport, ObjectOrChildModel, TileFieldEventMap, TileFieldModel, TileGrid, Widget} from '../../../index';

export class TileField extends FormField implements TileFieldModel {
  declare model: TileFieldModel;
  declare eventMap: TileFieldEventMap;
  declare self: TileField;

  tileGrid: TileGrid;
  eventDelegator: EventDelegator;

  constructor() {
    super();
    this.eventDelegator = null;
    this._addWidgetProperties(['tileGrid']);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this._setTileGrid(this.tileGrid);
  }

  protected override _createLoadingSupport(): LoadingSupport {
    // Loading is delegated to tileGrid
    return null;
  }

  protected override _render() {
    this.addContainer(this.$parent, 'tile-field');
    this.addLabel();
    this.addMandatoryIndicator();
    this.addStatus();
    if (this.tileGrid) {
      this._renderTileGrid();
    }
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderDropType();
  }

  setTileGrid(tileGrid: ObjectOrChildModel<TileGrid>) {
    this.setProperty('tileGrid', tileGrid);
  }

  protected _setTileGrid(tileGrid: TileGrid) {
    if (this.tileGrid) {
      if (this.eventDelegator) {
        this.eventDelegator.destroy();
        this.eventDelegator = null;
      }
    }
    this._setProperty('tileGrid', tileGrid);
    if (tileGrid) {
      this.eventDelegator = EventDelegator.create(this, tileGrid, {
        delegateProperties: ['loading']
      });
      tileGrid.setLoading(this.loading);
      tileGrid.setScrollTop(this.scrollTop);
    }
  }

  protected _renderTileGrid() {
    if (!this.tileGrid) {
      return;
    }
    this.tileGrid.render();
    this.addField(this.tileGrid.$container);
    this.invalidateLayoutTree();
  }

  protected _removeTileGrid() {
    if (!this.tileGrid) {
      return;
    }
    this.tileGrid.remove();
    this._removeField();
    this.invalidateLayoutTree();
  }

  override getDelegateScrollable(): Widget {
    return this.tileGrid;
  }
}
