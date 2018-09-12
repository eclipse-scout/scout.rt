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
scout.Tile = function() {
  scout.Tile.parent.call(this);
  this.displayStyle = scout.Tile.DisplayStyle.DEFAULT;
  this.filterAccepted = true;
  this.gridData = null;
  this.gridDataHints = new scout.GridData();
  this.colorScheme = null;
  this.selected = false;
  this.selectable = false;
};
scout.inherits(scout.Tile, scout.Widget);

// These constants need to correspond to the IDs defined in TileColorScheme.java
scout.Tile.ColorSchemeId = {
  DEFAULT: 'default',
  ALTERNATIVE: 'alternative',
  RAINBOW: 'rainbow'
};

scout.Tile.DisplayStyle = {
  DEFAULT: 'default',
  PLAIN: 'plain'
};

/**
 * @override
 */
scout.Tile.prototype._createLoadingSupport = function() {
  return new scout.LoadingSupport({
    widget: this
  });
};

scout.Tile.prototype._init = function(model) {
  scout.Tile.parent.prototype._init.call(this, model);
  this._setGridDataHints(this.gridDataHints);
  this._setColorScheme(this.colorScheme);
  this._setSelectable(this.selectable);
};

scout.Tile.prototype._render = function() {
  this.$container = this.$parent.appendDiv('tile');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.SingleLayout());
};

scout.Tile.prototype._renderProperties = function() {
  scout.Tile.parent.prototype._renderProperties.call(this);
  this._renderGridDataHints();
  this._renderColorScheme();
  this._renderSelectable();
  this._renderSelected();
  this._renderDisplayStyle();
};

scout.Tile.prototype._postRender = function() {
  this.$container.addClass('tile');
  // Make sure prefSize returns the size the tile has after the animation even if it is called while the animation runs
  // Otherwise the tile may have the wrong size after making a tile with useUiHeight = true visible
  this.htmlComp.layout.animateClasses = ['animate-visible', 'animate-invisible'];
};

scout.Tile.prototype._renderDisplayStyle = function() {
  this.$container.toggleClass('default-tile', this.displayStyle === scout.Tile.DisplayStyle.DEFAULT);
};

scout.Tile.prototype.setGridDataHints = function(gridData) {
  this.setProperty('gridDataHints', gridData);
};

scout.Tile.prototype._setGridDataHints = function(gridData) {
  if (!gridData) {
    gridData = new scout.GridData();
  }
  this._setProperty('gridDataHints', scout.GridData.ensure(gridData));
};

scout.Tile.prototype._renderGridDataHints = function() {
  this.parent.invalidateLogicalGrid();
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
    if (scout.strings.startsWith(colorScheme, scout.Tile.ColorSchemeId.RAINBOW)) {
      colorSchemeObj.scheme = scout.Tile.ColorSchemeId.RAINBOW;
    }
    colorSchemeObj.inverted = scout.strings.endsWith(colorScheme, '-inverted');
  }
  return colorSchemeObj;
};

scout.Tile.prototype._renderColorScheme = function() {
  this.$container.toggleClass('color-alternative', (this.colorScheme.scheme === scout.Tile.ColorSchemeId.ALTERNATIVE));
  this.$container.toggleClass('color-rainbow', (this.colorScheme.scheme === scout.Tile.ColorSchemeId.RAINBOW));
  this.$container.toggleClass('inverted', this.colorScheme.inverted);
};

scout.Tile.prototype.setSelected = function(selected) {
  if (selected && !this.selectable) {
    return;
  }
  this.setProperty('selected', selected);
};

scout.Tile.prototype._renderSelected = function() {
  this.$container.toggleClass('selected', this.selected);
};

scout.Tile.prototype.setSelectable = function(selectable) {
  this.setProperty('selectable', selectable);
};

scout.Tile.prototype._setSelectable = function(selectable) {
  this._setProperty('selectable', selectable);
  if (!this.selectable) {
    this.setSelected(false);
  }
};

scout.Tile.prototype._renderSelectable = function() {
  this.$container.toggleClass('selectable', this.selectable);
};

scout.Tile.prototype.setFilterAccepted = function(filterAccepted) {
  this.setProperty('filterAccepted', filterAccepted);
};

scout.Tile.prototype._renderFilterAccepted = function() {
  this._renderVisible();
};

scout.Tile.prototype._renderVisible = function() {
  if (this.rendering) {
    this.$container.setVisible(this.isVisible());
    return;
  }
  if (!this.isVisible()) {
    // Remove animate-visible first to show correct animation even if tile is made invisible while visible animation is still in progress
    // It is also necessary if the container is made invisible before the animation is finished because animationEnd won't fire in that case
    // which means that animate-invisible is still on the element and will trigger the (wrong) animation when container is made visible again
    this._beforeAnimateVisible();
    this.$container.removeClass('animate-visible');
    this.$container.addClassForAnimation('animate-invisible');
    this.$container.oneAnimationEnd(function() {
      // Make the element invisible after the animation (but only if visibility has not changed again in the meantime)
      this.$container.setVisible(this.isVisible());
    }.bind(this));
  } else {
    this.$container.setVisible(true);
    this._beforeAnimateVisible();
    this.$container.removeClass('animate-invisible');
    this.$container.addClassForAnimation('animate-visible');
  }
  this.invalidateParentLogicalGrid();
};

/**
 * Override this function to do something before the visibility animation starts.
 * Check the isVisible() function if you must distinct between visible/invisible.
 * You can use this function if your tile uses a programmed layout and you need
 * the size of the tile, without the effects from the animation.
 */
scout.Tile.prototype._beforeAnimateVisible = function() {
  // NOP
};

/**
 * @override
 */
scout.Tile.prototype.isVisible = function() {
  return this.visible && this.filterAccepted;
};
