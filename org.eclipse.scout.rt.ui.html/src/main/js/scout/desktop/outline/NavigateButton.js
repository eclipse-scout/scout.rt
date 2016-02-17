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
/**
 * The outline navigation works mostly browser-side. The navigation logic is implemented in JavaScript.
 * When a navigation button is clicked, we process that click browser-side first and send an event to
 * the server which nodes have been selected. We do that for better user experience. In a first attempt
 * the whole navigation logic was on the server, which caused a lag and flickering in the UI.
 *
 * @abstract
 */
scout.NavigateButton = function() {
  scout.NavigateButton.parent.call(this);

  this.node;
  this.outline;
  this._onClickFunc;
  this.selected = false;
  this.visible = true;
  this.enabled = true;
  this.mandatory = false;
  this.actionStyle = scout.Action.ActionStyle.BUTTON;
  /**
   * Additional CSS class to be applied in _render method.
   */
  this._additionalCssClass = '';
};
scout.inherits(scout.NavigateButton, scout.Menu);

scout.NavigateButton.prototype._init = function(options) {
  scout.NavigateButton.parent.prototype._init.call(this, options);

  this.node = options.node;
  this.outline = options.outline;
};

scout.NavigateButton.prototype.destroy = function() {
  if (this.rendered) {
    this.remove();
  }
};

/**
 * @override
 */
scout.NavigateButton.prototype._render = function($parent) {
  if (this._isDetail()) {
    this._onClickFunc = this._setDetailVisible.bind(this);
  } else {
    this._onClickFunc = this._drill.bind(this);
  }
  if (this.overflow) {
    this.text = this.session.text(this._defaultText);
    this.iconId = null;
  } else {
    this.text = null;
    this.iconId = this._defaultIconId;
  }
  this.enabled = this._buttonEnabled();
  scout.NavigateButton.parent.prototype._render.call(this, $parent);
  this.$container.addClass('navigate-button small');
  this.$container.addClass(this._additionalCssClass);
  this.outline.keyStrokeContext.registerKeyStroke(this);
};

/**
 * @override Action.js
 */
scout.NavigateButton.prototype._remove = function() {
  scout.NavigateButton.parent.prototype._remove.call(this);
  this.outline.keyStrokeContext.unregisterKeyStroke(this);
};

scout.NavigateButton.prototype._setDetailVisible = function() {
  var detailVisible = this._toggleDetail();
  $.log.debug('show detail-' + detailVisible ? 'form' : 'table');
  this.node.detailFormVisibleByUi = detailVisible;
  this.outline._updateOutlineNode(this.node, true);
};

/**
 * @override Menu.js
 */
scout.NavigateButton.prototype.doAction = function(event) {
  if (!this.prepareDoAction(event)) {
    return false;
  }
  this._onClickFunc();
  return true;
};

/**
 * Called when enabled state must be re-calculated and probably rendered.
 */
scout.NavigateButton.prototype.updateEnabled = function() {
  this.enabled = this._buttonEnabled();
  if (this.rendered) {
    this._renderEnabled();
  }
};
