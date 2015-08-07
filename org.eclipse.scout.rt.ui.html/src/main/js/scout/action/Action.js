scout.Action = function() {
  this._hoverBound = false;
  scout.Action.parent.call(this);

  // keyStroke
  this.keyStroke;
  this.keyStrokeKeyPart;
  this.ctrl = false;
  this.alt = false;
  this.shift = false;
  this.bubbleUp = false;
  this.drawHint = true;
  this.preventDefaultOnEvent = true;
  this.stopImmediate = true;

  /**
   * This property decides whether or not the tabindex attribute is set in the DOM.
   */
  this.tabbable = false;

  /**
   * Supported action styles are:
   * - default: regular menu-look, also used in overflow menus
   * - taskbar: as used in the task bar
   * - button: menu looks like a button
   */
  this.actionStyle = scout.Action.ActionStyle.DEFAULT;
};
scout.inherits(scout.Action, scout.ModelAdapter);

scout.Action.ActionStyle = {
  DEFAULT: 0,
  BUTTON: 1,
  TOGGLE: 2,
  TASK_BAR: 3
};

scout.Action.prototype._renderProperties = function() {
  scout.Action.parent.prototype._renderProperties.call(this);

  this._renderText(this.text);
  this._renderIconId(this.iconId);
  this._renderTooltipText(this.tooltipText);
  this._renderKeyStroke(this.keyStroke);
  this._renderEnabled(this.enabled);
  this._renderSelected(this.selected);
  this._renderVisible(this.visible);
  this._renderTabbable();
};

scout.Action.prototype._init = function(model, session) {
  scout.Action.parent.prototype._init.call(this, model, session);
  this.initKeyStrokeParts();
};

scout.Action.prototype._remove = function() {
  this._hoverBound = false;
  scout.Action.parent.prototype._remove.call(this);
};

scout.Action.prototype._renderText = function(text) {
  text = text || '';
  this.$container.text(text);
};

scout.Action.prototype._renderIconId = function(iconId) {
  iconId = iconId || '';
  this.$container.icon(iconId);
};

scout.Action.prototype._renderEnabled = function(enabled) {
  this.$container.setEnabled(enabled);
};

scout.Action.prototype._renderVisible = function(visible) {
  this.$container.setVisible(visible);
};

scout.Action.prototype._renderSelected = function(selected) {
  this.$container.select(selected);
};

scout.Action.prototype._renderKeyStroke = function(keyStroke) {
  if (keyStroke === undefined) {
    this.$container.removeAttr('data-shortcut');
  } else {
    this.$container.attr('data-shortcut', keyStroke);
  }
};

scout.Action.prototype._renderTooltipText = function(tooltipText) {
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
  this.tooltip = new scout.Tooltip(this.session, this._configureTooltip());
  this.tooltip.render();
};

scout.Action.prototype._configureTooltip = function() {
  return {
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

  if (this.actionStyle === scout.Action.ActionStyle.TOGGLE) {
    this.setSelected(!this.selected);
  } else {
    this.sendDoAction();
  }
  return true;
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
  this.remoteHandler(this.id, 'doAction');
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
  this.selected = selected;
  if (this.rendered) {
    this._renderSelected(this.selected);
  }
  this.sendSelected();
};

scout.Action.prototype.sendSelected = function() {
  this.session.send(this.id, 'selected', {
    selected: this.selected
  });
};

// KeyStrokes

scout.Action.prototype.ignore = function(event) {
  return false;
};

scout.Action._syncKeyStroke = function(keyStroke) {
  // When model's 'keystroke' property changes, also update keystroke parts
  this.keyStroke = keyStroke;
  this.initKeyStrokeParts();
};

scout.Action.prototype.initKeyStrokeParts = function() {
  this.keyStrokeKeyPart = undefined;
  if (!this.keyStroke) {
    return;
  }
  var keyStrokeParts = this.keyStroke.split('-');
  for (var i = 0; i < keyStrokeParts.length; i++) {
    var part = keyStrokeParts[i];
    // see org.eclipse.scout.rt.client.ui.action.keystroke.KeyStrokeNormalizer
    if (part === 'alternate' || part === 'alt') {
      this.alt = true;
    } else if (part === 'control' || part ==='ctrl') {
      this.ctrl = true;
    } else if (part === 'shift') {
      this.shift = true;
    } else {
      this.keyStrokeKeyPart = scout.keys[part.toUpperCase()];
    }
  }
};

scout.Action.prototype.accept = function(event) {
  if (this.ignore(event)) {
    return false;
  }
  if (event && event.ctrlKey === this.ctrl && event.altKey === this.alt && event.shiftKey === this.shift && event.which === this.keyStrokeKeyPart) {
    return true;
  }
  return false;
};

scout.Action.prototype.setTabbable = function(tabbable) {
  this.tabbable = tabbable;
  if (this.rendered) {
    this._renderTabbable();
  }
};

scout.Action.prototype.handle = function(event) {
  var actionPerformed = this.doAction(event);
  if (actionPerformed && this.preventDefaultOnEvent) {
    event.preventDefault();
  }
};

scout.Action.prototype.checkAndDrawKeyBox = function($container, drawedKeys) {
  if (drawedKeys[this.keyStrokeName()]) {
    return;
  }
  if (this.drawHint) {
    this._drawKeyBox($container);
  }
  drawedKeys[this.keyStrokeName()] = true;
};

scout.Action.prototype._drawKeyBox = function($container) {
  if (!this.drawHint || !this.keyStroke || !this.visible || !this.enabled || !this.rendered) {
    return;
  }
  var keyBoxText = scout.codesToKeys[this.keyStrokeKeyPart];
  scout.keyStrokeBox.drawSingleKeyBoxItem(4, keyBoxText, $container, this.ctrl, this.alt, this.shift);
};

scout.Action.prototype.removeKeyBox = function($container) {
  $('.key-box', $container).remove();
  $('.key-box-additional', $container).remove();
};

scout.Action.prototype.keyStrokeName = function() {
  var name = this.ctrl ? 'ctrl+' : '';
  name += this.alt ? 'alt+' : '';
  name += this.shift ? 'shift+' : '';
  return name + this.keyStrokeKeyPart;
};
