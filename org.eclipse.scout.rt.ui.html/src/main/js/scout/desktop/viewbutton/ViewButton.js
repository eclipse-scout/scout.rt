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
scout.ViewButton = function() {
  scout.ViewButton.parent.call(this);
  this.inBackground = false;
};
scout.inherits(scout.ViewButton, scout.Action);

scout.ViewButton.prototype._render = function() {
  if (this._isMenu()) {
    this._renderAsMenuItem();
  } else {
    this._renderAsTab();
  }
};

scout.ViewButton.prototype._renderProperties = function() {
  scout.ViewButton.parent.prototype._renderProperties.call(this);

  this._renderInBackground();
};

scout.ViewButton.prototype._isMenu = function() {
  return this.displayStyle === 'MENU';
};

scout.ViewButton.prototype._isTab = function() {
  return this.displayStyle === 'TAB';
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
  if (this._isMenu()) {
    scout.ViewButton.parent.prototype._renderText.call(this);
  }
};

/**
 * Use a default icon, when view-tab doesn't define one.
 * @override Action.js
 */
scout.ViewButton.prototype._renderIconId = function() {
  if (this._isTab()) {
    this.$container.icon(this.iconId);
  }
};

scout.ViewButton.prototype._renderInBackground = function() {
  this.$container.toggleClass('in-background', this.inBackground);
};

scout.ViewButton.prototype.last = function() {
  this.$container.addClass('last');
};

scout.ViewButton.prototype.sendToBack = function() {
  this.inBackground = true;
  if (this.rendered) {
    this._renderInBackground();
  }
};

scout.ViewButton.prototype.bringToFront = function() {
  this.inBackground = false;
  if (this.rendered) {
    this._renderInBackground();
  }
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

/**
 * ViewButtonActionKeyStroke
 */
scout.ViewButtonActionKeyStroke = function(action) {
  scout.ViewButtonActionKeyStroke.parent.call(this, action);

};
scout.inherits(scout.ViewButtonActionKeyStroke, scout.ActionKeyStroke);

scout.ViewButtonActionKeyStroke.prototype._postRenderKeyBox = function($drawingArea) {
  if (this.field.iconId && this.field._isTab()) {
    var width = $drawingArea.outerWidth();
    var wKeybox = $drawingArea.find('.key-box').outerWidth();
    var leftKeyBox = width / 2 - wKeybox / 2;
    $drawingArea.find('.key-box').cssLeft(leftKeyBox);
  }
};

scout.ViewButtonActionKeyStroke.prototype.renderKeyBox = function($drawingArea, event) {
  if (this.field._isMenu()) {
    this.renderingHints.hAlign = scout.hAlign.RIGHT;
  }
  return scout.ViewButtonActionKeyStroke.parent.prototype.renderKeyBox.call(this, $drawingArea, event);
};
