scout.Action = function() {
  this._hoverBound = false;
  scout.Action.parent.call(this);

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
};
scout.inherits(scout.Action, scout.ModelAdapter);

scout.Action.ActionStyle = {
  DEFAULT: 0,
  BUTTON: 1
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

scout.Action.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.Action.parent.prototype._initKeyStrokeContext(this, keyStrokeContext);
  this.actionKeyStroke.parseAndSetKeyStroke(this.keyStroke);
};

scout.Action.prototype._remove = function() {
  this._hoverBound = false;
  this._removeText();
  this._removeTooltip();
  scout.Action.parent.prototype._remove.call(this);
};

scout.Action.prototype._renderText = function() {
  var text = this.text || '';
  if (text) {
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
  enabled = scout.helpers.nvl(enabled, this.enabled);
  this.$container.setEnabled(enabled);
};

scout.Action.prototype._renderVisible = function() {
  this.$container.setVisible(this.visible);
};

scout.Action.prototype._renderSelected = function() {
  this.$container.select(this.selected);
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
  var tooltipText = this.tooltipText;
  if (tooltipText && !this._hoverBound) {
    this.$container.hover(this._onHoverIn.bind(this), this._onHoverOut.bind(this));
    this._hoverBound = true;
  } else if (!tooltipText && this.hoverBound) {
    this.$container.off('mouseenter mouseleave');
    this._hoverBound = false;
  }
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

scout.Action.prototype._onHoverIn = function() {
  // Don't show tooltip if action is selected or not enabled
  if (this.enabled && !this.selected) {
    this._showTooltip();
  }
};

scout.Action.prototype._onHoverOut = function() {
  this._removeTooltip();
};

scout.Action.prototype._showTooltip = function() {
  this.tooltip = scout.create(scout.Tooltip, this._configureTooltip());
  this.tooltip.render();
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

scout.Action.prototype._removeTooltip = function() {
  if (this.tooltip) {
    this.tooltip.remove();
    this.tooltip = null;
  }
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
scout.Action.prototype.prepareDoAction = function(event) {
  if (!this.enabled || !this.visible) {
    return false;
  }

  // This is required for key-stroke actions. When they are triggered on
  // key-down, the active field is still focused and its blur-event is not
  // triggered, which means the acceptInput() is never executed so
  // the executed action works with a wrong value for the active field.
  // --> Same check in Button.doAction()
  var activeValueField = $(document.activeElement).data('valuefield');
  if (activeValueField === undefined) {
    // try parent, some times the value field is the parent of the input field (e.g. DateField.js)
    activeValueField = $(document.activeElement).parent().data('valuefield');
  }
  if (activeValueField) {
    activeValueField.acceptInput();
  }
  return true;
};

scout.Action.prototype.sendDoAction = function() {
  this.beforeSendDoAction();
  this.remoteHandler(this.originalId(), 'doAction');
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

scout.Action.prototype.setSelected = function(selected) {
  if (selected === this.selected) {
    return;
  }
  this.selected = selected;
  if (this.rendered) {
    this._renderSelected();
  }
  this.sendSelected();
};

scout.Action.prototype.sendSelected = function() {
  this.remoteHandler(this.originalId(), 'selected', {
    selected: this.selected
  });
};

scout.Action._syncKeyStroke = function(keyStroke) {
  this.keyStroke = keyStroke;
  this.actionKeyStroke.parseAndSetKeyStroke(keyStroke);
};

scout.Action.prototype.setTabbable = function(tabbable) {
  this.tabbable = tabbable;
  if (this.rendered) {
    this._renderTabbable();
  }
};

scout.Action.prototype._createActionKeyStroke = function() {
  return new scout.ActionKeyStroke(this);
};
