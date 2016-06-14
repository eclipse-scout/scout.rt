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
scout.Action = function() {
  scout.Action.parent.call(this);
  this._addModelProperties(['tabbable']);

  /**
   * This property decides whether or not the tabindex attribute is set in the DOM.
   */
  this.tabbable = false;

  /**
   * Supported action styles are:
   * - default: regular menu-look, also used in overflow menus
   * - button: menu looks like a button
   */
  this.actionStyle = scout.Action.ActionStyle.DEFAULT;
  this.textVisible = true;
  this.compact = false;
};
scout.inherits(scout.Action, scout.ModelAdapter);

scout.Action.ActionStyle = {
  DEFAULT: 0,
  BUTTON: 1
};

scout.Action.prototype._init = function(model) {
  scout.Action.parent.prototype._init.call(this, model);
  this.actionKeyStroke = this._createActionKeyStroke();
  this._syncKeyStroke(this.keyStroke);
  this._syncSelected(this.selected);
};

scout.Action.prototype._renderProperties = function() {
  scout.Action.parent.prototype._renderProperties.call(this);

  this._renderText();
  this._renderIconId();
  this._renderTooltipText();
  this._renderKeyStroke();
  this._renderEnabled();
  this._renderSelected();
  this._renderVisible();
  this._renderTabbable();
  this._renderCompact();
  this._renderCssClass();
};

scout.Action.prototype._remove = function() {
  this._removeText();
  scout.Action.parent.prototype._remove.call(this);
};

scout.Action.prototype._renderText = function() {
  var text = this.text || '';
  if (text && this.textVisible) {
    if (!this.$text) {
      // Create a separate text element to so that setting the text does not remove the icon
      this.$text = this.$container.appendSpan('text');
    }
    this.$text.text(text);
  } else {
    this._removeText();
  }
};

scout.Action.prototype._removeText = function() {
  if (this.$text) {
    this.$text.remove();
    this.$text = null;
  }
};

scout.Action.prototype._renderIconId = function() {
  var iconId = this.iconId || '';
  this.$container.icon(iconId);
};

scout.Action.prototype._renderEnabled = function(enabled) {
  enabled = scout.nvl(enabled, this.enabled);
  this.$container.setEnabled(enabled);
  this._updateTooltip();
};

scout.Action.prototype._renderVisible = function() {
  this.$container.setVisible(this.visible);
};

scout.Action.prototype._renderSelected = function() {
  this.$container.select(this.selected);
  this._updateTooltip();
};

scout.Action.prototype._renderKeyStroke = function() {
  var keyStroke = this.keyStroke;
  if (keyStroke === undefined) {
    this.$container.removeAttr('data-shortcut');
  } else {
    this.$container.attr('data-shortcut', keyStroke);
  }
};

scout.Action.prototype._renderTooltipText = function() {
  this._updateTooltip();
};

/**
 * Installs or uninstalls tooltip based on tooltipText, selected and enabled.
 */
scout.Action.prototype._updateTooltip = function() {
  if (this._shouldInstallTooltip()) {
    scout.tooltips.install(this.$container, this._configureTooltip());
  } else {
    scout.tooltips.uninstall(this.$container);
  }
};

scout.Action.prototype._shouldInstallTooltip = function() {
  return this.tooltipText && !this.selected && this.enabled;
};

scout.Action.prototype._renderTabbable = function() {
  this.$container.setTabbable(this.tabbable && !scout.device.supportsTouch());
};

scout.Action.prototype._renderHorizontalAlignment = function() {
  // nothing to render, property is only considered by the menubar
};

scout.Action.prototype._renderCssClass = function(cssClass, oldCssClass) {
  cssClass = cssClass || this.cssClass;
  this.$container.removeClass(oldCssClass)
    .addClass(cssClass);
};


scout.Action.prototype._renderToggleAction = function() {
  // nop
};

scout.Action.prototype._renderCompact = function() {
  this.$container.toggleClass('compact', this.compact);
  this.invalidateLayoutTree();
};

scout.Action.prototype._configureTooltip = function() {
  return {
    parent: this,
    text: this.tooltipText,
    $anchor: this.$container,
    arrowPosition: 50,
    arrowPositionUnit: '%',
    position: this.tooltipPosition
  };
};

scout.Action.prototype._goOffline = function() {
  this._renderEnabled(false);
};

scout.Action.prototype._goOnline = function() {
  this._renderEnabled(true);
};

/**
 * @return {Boolean}
 *          <code>true</code> if the action has been performed or <code>false</code> if it
 *          has not been performed (e.g. when the button is not enabled).
 */
scout.Action.prototype.doAction = function() {
  if (!this.prepareDoAction()) {
    return false;
  }

  if (this.isToggleAction()) {
    this.setSelected(!this.selected);
  } else {
    this.sendDoAction();
  }
  return true;
};

scout.Action.prototype.toggle = function() {
  if (this.isToggleAction()) {
    this.setSelected(!this.selected);
  }
};

scout.Action.prototype.isToggleAction = function() {
  return this.toggleAction;
};

/**
 * @returns {Boolean} <code>true</code> if the action may be executed, <code>false</code> if it should be ignored.
 */
scout.Action.prototype.prepareDoAction = function() {
  if (!this.enabled || !this.visible) {
    return false;
  }

  return true;
};

scout.Action.prototype.sendDoAction = function() {
  this.beforeSendDoAction();
  this._send('doAction');
  this.afterSendDoAction();
};

/**
 * Override this method to do something before 'doAction' is sent to the server.
 * The default impl. does nothing.
 */
scout.Action.prototype.beforeSendDoAction = function() {
  // NOP
};

/**
 * Override this method to do something after 'doAction' has been sent to the server.
 * The default impl. does nothing.
 */
scout.Action.prototype.afterSendDoAction = function() {
  // NOP
};

scout.Action.prototype.setSelected = function(selected, notifyServer) {
  if (selected === this.selected) {
    return;
  }
  this._setProperty('selected', selected);
  if (scout.nvl(notifyServer, true)) {
    this.sendSelected();
  }
  if (this.rendered) {
    this._renderSelected();
  }
};

scout.Action.prototype.sendSelected = function() {
  this._send('selected', {
    selected: this.selected
  });
};

scout.Action.prototype._syncSelected = function(selected) {
  this.setSelected(selected, false);
  return false;
};

scout.Action.prototype._syncKeyStroke = function(keyStroke) {
  this.keyStroke = keyStroke;
  this.actionKeyStroke.parseAndSetKeyStroke(keyStroke);
};

scout.Action.prototype.setTabbable = function(tabbable) {
  this.tabbable = tabbable;
  if (this.rendered) {
    this._renderTabbable();
  }
};

scout.Action.prototype.setTextVisible = function(textVisible) {
  this.textVisible = textVisible;
  if (this.rendered) {
    this._renderText();
  }
};

scout.Action.prototype.setCompact = function(compact) {
  if (this.compact === compact) {
    return;
  }
  this.compact = compact;
  if (this.rendered) {
    this._renderCompact();
  }
};

scout.Action.prototype._createActionKeyStroke = function() {
  return new scout.ActionKeyStroke(this);
};
