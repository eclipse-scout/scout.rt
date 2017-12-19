/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
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
  this._addWidgetProperties(['tileGrid']);
};
scout.inherits(scout.TileField, scout.FormField);

scout.TileField.prototype._render = function() {
  this.addContainer(this.$parent, 'tile-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addStatus();
  if (this.tileGrid) {
    this._renderTileGrid();
  }
};

scout.TileField.prototype._renderTileGrid = function() {
  this.tileGrid.render();
  this.addField(this.tileGrid.$container);
};

scout.TileField.prototype._removeTileGrid = function() {
  this.tileGrid.remove();
  this._removeField();
};
