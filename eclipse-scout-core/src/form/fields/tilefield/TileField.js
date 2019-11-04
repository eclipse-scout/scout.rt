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
import {EventDelegator, FormField} from '../../../index';

export default class TileField extends FormField {

  constructor() {
    super();
    this.eventDelegator = null;
    this._addWidgetProperties(['tileGrid']);
  }

  _init(model) {
    super._init(model);

    this._setTileGrid(this.tileGrid);
  }

  /**
   * @override
   */
  _createLoadingSupport() {
    // Loading is delegated to tileGrid
    return null;
  }

  _render() {
    this.addContainer(this.$parent, 'tile-field');
    this.addLabel();
    this.addMandatoryIndicator();
    this.addStatus();
    if (this.tileGrid) {
      this._renderTileGrid();
    }
  }

  _renderProperties() {
    super._renderProperties();
    this._renderDropType();
  }

  setTileGrid(tileGrid) {
    this.setProperty('tileGrid', tileGrid);
  }

  _setTileGrid(tileGrid) {
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

  _renderTileGrid() {
    if (!this.tileGrid) {
      return;
    }
    this.tileGrid.render();
    this.addField(this.tileGrid.$container);
    this.invalidateLayoutTree();
  }

  _removeTileGrid() {
    if (!this.tileGrid) {
      return;
    }
    this.tileGrid.remove();
    this._removeField();
    this.invalidateLayoutTree();
  }

  /**
   * @override
   */
  getDelegateScrollable() {
    return this.tileGrid;
  }
}
