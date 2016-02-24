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
  this._addModelProperties(['text', 'iconId', 'tooltipText', 'keyStroke', 'enabled', 'selected', 'visible', 'tabbable']);

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

  this.actionKeyStroke = this._createActionKeyStroke();
  this.textVisible = true;
};
scout.inherits(scout.Action, scout.ModelAdapter);

scout.Action.ActionStyle = {
  DEFAULT: 0,
  BUTTON: 1
};

scout.Action.prototype._init = function(model) {
  scout.Action.parent.prototype._init.call(this, model);
  this._syncKeyStroke(this.keyStroke);
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

scout.Action.prototype._renderSelected = function(event) {
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
  if (this.tabbable) {
    this.$container.attr('tabindex', 0);
  } else {
    this.$container.removeAttr('tabindex');
  }
};

scout.Action.prototype._renderHorizontalAlignment = function() {
  // nothing to render, property is only considered by the menubar
};

scout.Action.prototype._renderToggleAction = function() {
  // nop
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
 * @param event
 *          UI event that triggered the action (e.g. 'mouse down'). This argument
 *          may be used by action implementors to check if the action should really
 *          be performed. E.g. the MenuBarPopup uses it to prevent the popup from
 *          being closed again by the same event when it bubbles to other elements.
 * @return {Boolean}
 *          <code>true</code> if the action has been performed or <code>false</code> if it
 *          has not been performed (e.g. when the button is not enabled).
 */
scout.Action.prototype.doAction = function(event) {
  if (!this.prepareDoAction(event)) {
    return false;
  }

  if (this.isToggleAction()) {
    this.setSelected(!this.selected, event);
  } else {
    this.sendDoAction();
  }
  return true;
};

scout.Action.prototype.toggle = function(event) {
  if (this.isToggleAction()) {
    this.setSelected(!this.selected, event);
  }
};

scout.Action.prototype.isToggleAction = function() {
  return this.toggleAction;
};

/**
 * @returns {Boolean} <code>true</code> if the action may be executed, <code>false</code> if it should be ignored.
 */
scout.Action.prototype.prepareDoAction = function(event) {
  if (!this.enabled || !this.visible) {
    return false;
  }

  // This is required for key-stroke actions. When they are triggered on
  // key-down, the active field is still focused and its blur-event is not
  // triggered, which means the acceptInput() is never executed so
  // the executed action works with a wrong value for the active field.
  // --> Same check in Button.doAction()
  if (event && event.target) {
    var $activeElement = $(event.target.ownerDocument.activeElement),
      activeValueField = $activeElement.data('valuefield');
    if (activeValueField === undefined) {
      // try parent, some times the value field is the parent of the input field (e.g. DateField.js)
      activeValueField = $activeElement.parent().data('valuefield');
    }
    if (activeValueField) {
      activeValueField.acceptInput();
    }
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

scout.Action.prototype.setSelected = function(selected, event) {
  if (selected === this.selected) {
    return;
  }
  this._setProperty('selected', selected);
  if (this.rendered) {
    this._renderSelected(event);
  }
  this.sendSelected();
};

scout.Action.prototype.sendSelected = function() {
  this._send('selected', {
    selected: this.selected
  });
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

scout.Action.prototype._createActionKeyStroke = function() {
  return new scout.ActionKeyStroke(this);
};
