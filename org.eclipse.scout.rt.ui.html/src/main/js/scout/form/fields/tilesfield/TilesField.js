/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TilesField = function() {
  scout.TilesField.parent.call(this);
  this._addWidgetProperties(['tiles']);
};
scout.inherits(scout.TilesField, scout.FormField);

scout.TilesField.prototype._render = function() {
  this.addContainer(this.$parent, 'tiles-field');
  this.addLabel();
//  this.addMandatoryIndicator(); // FIXME CGU should tiles or container have an indicator?
  this.addStatus();
  if (this.tiles) {
    this._renderTiles();
  }
};

scout.TilesField.prototype._renderTiles = function() {
  this.tiles.render();
  this.addField(this.tiles.$container);
};

scout.TilesField.prototype._removeTiles = function() {
  this.tiles.remove();
  this._removeField();
};
