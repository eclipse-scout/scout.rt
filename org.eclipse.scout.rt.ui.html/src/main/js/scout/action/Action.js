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

scout.Action.prototype.init = function(model, session) {
  scout.Action.parent.prototype.init.call(this, model, session);
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
  this.tooltip = new scout.Tooltip(this._configureTooltip());
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

scout.Action.prototype.doAction = function() {
  if (this.enabled) {
    this.sendDoAction();
  }
};

scout.Action.prototype.sendDoAction = function() {
  var activeValueField = $(document.activeElement).data('valuefield');
  if (activeValueField) {
    activeValueField.displayTextChanged();
  }
  this.beforeSendDoAction();
  this.session.send(this.id, 'doAction');
  this.afterSendDoAction();
};

scout.Action.prototype.sendSelected = function(selected) {
  this.session.send(this.id, 'selected', {
    selected: selected
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
    if (part === 'alternate') {
      this.alt = true;
    } else if (part === 'control') {
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
  if (this.enabled && this.visible) {
    this.sendDoAction();
    if (this.preventDefaultOnEvent) {
      event.preventDefault();
    }
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
  if (!this.drawHint || !this.keyStroke) {
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

