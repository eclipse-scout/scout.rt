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
scout.ViewButton = function() {
  scout.ViewButton.parent.call(this);
  this._breadcrumbEnabled = false;
};
scout.inherits(scout.ViewButton, scout.Action);

scout.ViewButton.prototype._render = function($parent) {
  if (this._isMenu()) {
    this._renderAsMenuItem($parent);
  } else {
    this._renderAsTab($parent);
  }
};

scout.ViewButton.prototype._isMenu = function() {
  return this.displayStyle === 'MENU';
};

scout.ViewButton.prototype._isTab = function() {
  return this.displayStyle === 'TAB';
};

scout.ViewButton.prototype._renderAsMenuItem = function($parent) {
  this.$container = $parent.appendDiv('view-menu-item')
    .on('click', this._onMouseEvent.bind(this));
};

scout.ViewButton.prototype._renderAsTab = function($parent) {
  this.$container = $parent.appendDiv('view-button-tab')
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

scout.ViewButton.prototype.last = function() {
  this.$container.addClass('last');
};

scout.ViewButton.prototype.setBreadcrumbEnabled = function(enabled) {
  this._breadcrumbEnabled = enabled;
  this._renderText();
  this._renderSelected();
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
    $drawingArea.find('.key-box').css('left', leftKeyBox + 'px');
  }
};

scout.ViewButtonActionKeyStroke.prototype.renderKeyBox = function($drawingArea, event) {
  if (this.field._isMenu()) {
    this.renderingHints.hAlign = scout.hAlign.RIGHT;
  }
  return scout.ViewButtonActionKeyStroke.parent.prototype.renderKeyBox.call(this, $drawingArea, event);
};
