/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TileField = function() {
  scout.TileField.parent.call(this);
  this.eventDelegator = null;
  this._addWidgetProperties(['tileGrid']);
};
scout.inherits(scout.TileField, scout.FormField);

scout.TileField.prototype._init = function(model) {
  scout.TileField.parent.prototype._init.call(this, model);

  this._setTileGrid(this.tileGrid);
};

/**
 * @override
 */
scout.TileField.prototype._createLoadingSupport = function() {
  // Loading is delegated to tileGrid
  return null;
};

scout.TileField.prototype._render = function() {
  this.addContainer(this.$parent, 'tile-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addStatus();
  if (this.tileGrid) {
    this._renderTileGrid();
  }
};

scout.TileField.prototype._renderProperties = function() {
  scout.TileField.parent.prototype._renderProperties.call(this);
  this._renderDropType();
};

scout.TileField.prototype.setTileGrid = function(tileGrid) {
  this.setProperty('tileGrid', tileGrid);
};

scout.TileField.prototype._setTileGrid = function(tileGrid) {
  if (this.tileGrid) {
    if (this.eventDelegator) {
      this.eventDelegator.destroy();
      this.eventDelegator = null;
    }
  }
  this._setProperty('tileGrid', tileGrid);
  if (tileGrid) {
    this.eventDelegator = scout.EventDelegator.create(this, tileGrid, {
      delegateProperties: ['loading']
    });
    tileGrid.setLoading(this.loading);
    tileGrid.setScrollTop(this.scrollTop);
  }
};

scout.TileField.prototype._renderTileGrid = function() {
  if (!this.tileGrid) {
    return;
  }
  this.tileGrid.render();
  this.addField(this.tileGrid.$container);
  this.invalidateLayoutTree();
};

scout.TileField.prototype._removeTileGrid = function() {
  if (!this.tileGrid) {
    return;
  }
  this.tileGrid.remove();
  this._removeField();
  this.invalidateLayoutTree();
};

/**
 * @override
 */
scout.TileField.prototype.getDelegateScrollable = function() {
  return this.tileGrid;
};
