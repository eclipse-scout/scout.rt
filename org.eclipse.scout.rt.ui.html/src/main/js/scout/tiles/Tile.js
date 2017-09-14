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
  this.colorScheme = null;
};
scout.inherits(scout.Tile, scout.Widget);

// These constants need to correspond to the IDs defined in TileColorScheme.java
scout.Tile.ColorSchemeId = {
  DEFAULT: 'default',
  ALTERNATIVE: 'alternative'
};

scout.Tile.prototype._init = function(model) {
  scout.Tile.parent.prototype._init.call(this, model);
  this._setGridDataHints(this.gridDataHints);
  this._setGridData(this.gridData);
  this._setColorScheme(this.colorScheme);
};

scout.Tile.prototype._render = function() {
  this.$container = this.$parent.appendDiv('tile');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.SingleLayout());
};

scout.Tile.prototype._renderProperties = function() {
  scout.Tile.parent.prototype._renderProperties.call(this);
  this._renderGridData();
  this._renderColorScheme();
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

scout.Tile.prototype.setColorScheme = function(colorScheme) {
  this.setProperty('colorScheme', colorScheme);
};

scout.Tile.prototype._setColorScheme = function(colorScheme) {
  var defaultScheme = {
    scheme: scout.Tile.ColorSchemeId.DEFAULT,
    inverted: false
  };
  colorScheme = this._ensureColorScheme(colorScheme);
  colorScheme = $.extend({}, defaultScheme, colorScheme);
  this._setProperty('colorScheme', colorScheme);
};

/**
 * ColorScheme may be a string -> convert to an object
 */
scout.Tile.prototype._ensureColorScheme = function(colorScheme) {
  if (typeof colorScheme === 'object') {
    return colorScheme;
  }
  var colorSchemeObj = {};
  if (typeof colorScheme === 'string') {
    // Split up colorScheme in two individual parts ("scheme" and "inverted").
    // This information is then used when rendering the color scheme.
    if (scout.strings.startsWith(colorScheme, scout.Tile.ColorSchemeId.ALTERNATIVE)) {
      colorSchemeObj.scheme = scout.Tile.ColorSchemeId.ALTERNATIVE;
    }
    if (scout.strings.endsWith(colorScheme, '-inverted')) {
      colorSchemeObj.inverted = true;
    }
  }
  return colorSchemeObj;
};

scout.Tile.prototype._renderColorScheme = function() {
  this.$container.toggleClass('color-alternative', (this.colorScheme.scheme === scout.Tile.ColorSchemeId.ALTERNATIVE));
  this.$container.toggleClass('inverted', this.colorScheme.inverted);
};
