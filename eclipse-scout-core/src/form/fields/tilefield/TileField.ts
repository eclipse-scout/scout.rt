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
import {EventDelegator, FormField, LoadingSupport, TileFieldEventMap, TileFieldModel, TileGrid, Widget} from '../../../index';

export default class TileField extends FormField implements TileFieldModel {
  declare model: TileFieldModel;
  declare eventMap: TileFieldEventMap;

  tileGrid: TileGrid;
  eventDelegator: EventDelegator;

  constructor() {
    super();
    this.eventDelegator = null;
    this._addWidgetProperties(['tileGrid']);
  }

  protected override _init(model: TileFieldModel) {
    super._init(model);

    this._setTileGrid(this.tileGrid);
  }

  protected override _createLoadingSupport(): LoadingSupport {
    // Loading is delegated to tileGrid
    return null;
  }

  protected _render() {
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

  setTileGrid(tileGrid: TileGrid) {
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
