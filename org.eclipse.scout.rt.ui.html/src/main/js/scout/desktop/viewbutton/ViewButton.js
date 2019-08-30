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
scout.ViewButton = function() {
  scout.ViewButton.parent.call(this);
  this.showTooltipWhenSelected = false;
  this.displayStyle = 'TAB';
  this._renderedAsMenu = false;
};
scout.inherits(scout.ViewButton, scout.Action);

scout.ViewButton.prototype.renderAsMenuItem = function($parent) {
  this._renderedAsMenu = true;
  scout.ViewButton.parent.prototype.render.call(this, $parent);
};
scout.ViewButton.prototype.renderAsTab = function($parent) {
  this._renderedAsMenu = false;
  scout.ViewButton.parent.prototype.render.call(this, $parent);
};

scout.ViewButton.prototype._render = function() {
  if (this._renderedAsMenu) {
    this._renderAsMenuItem();
  } else {
    this._renderAsTab();
  }
};

scout.ViewButton.prototype._renderAsMenuItem = function() {
  this.$container = this.$parent.appendDiv('view-menu-item')
    .on('click', this._onMouseEvent.bind(this));
};

scout.ViewButton.prototype._renderAsTab = function() {
  this.$container = this.$parent.appendDiv('view-button-tab')
    .on('mousedown', this._onMouseEvent.bind(this));
};

/**
 * @override Action.js
 */
scout.ViewButton.prototype._renderText = function() {
  if (this._renderedAsMenu) {
    scout.ViewButton.parent.prototype._renderText.call(this);
  }
};

scout.ViewButton.prototype.setDisplayStyle = function(displayStyle) {
  this.setProperty('displayStyle', displayStyle);
};

scout.ViewButton.prototype.last = function() {
  this.$container.addClass('last');
};

scout.ViewButton.prototype.tab = function() {
  this.$container.addClass('view-tab');
};

scout.ViewButton.prototype._onMouseEvent = function(event) {
  this.doAction();
};

/**
 * @override Action.js
 */
scout.ViewButton.prototype._createActionKeyStroke = function() {
  return new scout.ViewButtonActionKeyStroke(this);
};
