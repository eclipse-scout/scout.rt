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
scout.Tile = function() {
  scout.Tile.parent.call(this);
  this.gridData;
  this.gridDataHints = new scout.GridData();
};
scout.inherits(scout.Tile, scout.Widget);

scout.Tile.prototype._init = function(model) {
  scout.Tile.parent.prototype._init.call(this, model);
  this._setGridDataHints(this.gridDataHints);
  this._setGridData(this.gridData);
};

scout.Tile.prototype._render = function() {
  this.$container = this.$parent.appendDiv('tile');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
};

scout.Tile.prototype._renderProperties = function() {
  scout.Tile.parent.prototype._renderProperties.call(this);
  this._renderGridData();
};

scout.Tile.prototype.setGridDataHints = function(gridData) {
  this.setProperty('gridDataHints', gridData);
};

scout.Tile.prototype._setGridDataHints = function(gridData) {
  if (!gridData) {
    gridData = new scout.GridData();
  }
  this._setProperty('gridDataHints', scout.GridData.ensure(gridData));
  this.parent.invalidateLogicalGrid();
};

scout.Tile.prototype._setGridData = function(gridData) {
  if (!gridData) {
    gridData = new scout.GridData();
  }
  this._setProperty('gridData', scout.GridData.ensure(gridData));
};

scout.Tile.prototype._renderGridData = function() {
  if (this.rendered) {
    var htmlCompParent = this.htmlComp.getParent();
    if (htmlCompParent) { // may be null if $container is detached
      htmlCompParent.invalidateLayoutTree();
    }
  }
};
