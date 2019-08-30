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
scout.Action = function() {
  scout.Action.parent.call(this);

  this.actionStyle = scout.Action.ActionStyle.DEFAULT;
  this.compact = false;
  this.iconId = null;
  this.horizontalAlignment = -1;
  this.keyStroke = null;
  this.keyStrokeFirePolicy = scout.Action.KeyStrokeFirePolicy.ACCESSIBLE_ONLY;
  this.selected = false;
  /**
   * This property decides whether or not the tabindex attribute is set in the DOM.
   */
  this.tabbable = false;
  this.text = null;
  /**
   * Supported action styles are:
   * - default: regular menu-look, also used in overflow menus
   * - button: menu looks like a button
   */
  this.textVisible = true;
  this.toggleAction = false;
  this.tooltipText = null;
  this.showTooltipWhenSelected = true;

  this._addCloneProperties(['actionStyle', 'horizontalAlignment', 'iconId', 'selected', 'tabbable', 'text', 'tooltipText', 'toggleAction']);
};
scout.inherits(scout.Action, scout.Widget);

scout.Action.ActionStyle = {
  DEFAULT: 0,
  BUTTON: 1
};

scout.Action.KeyStrokeFirePolicy = {
  ACCESSIBLE_ONLY: 0,
  ALWAYS: 1
};

/**
 * @override
 */
scout.Action.prototype._createKeyStrokeContext = function() {
  return new scout.KeyStrokeContext();
};

scout.Action.prototype._init = function(model) {
  scout.Action.parent.prototype._init.call(this, model);
  this.actionKeyStroke = this._createActionKeyStroke();
  this.resolveConsts([{
    property: 'actionStyle',
    constType: scout.Action.ActionStyle
  }, {
    property: 'keyStrokeFirePolicy',
    constType: scout.Action.KeyStrokeFirePolicy
  }]);
  this.resolveTextKeys(['text', 'tooltipText']);
  this.resolveIconIds(['iconId']);
  this._setKeyStroke(this.keyStroke);
};

scout.Action.prototype._render = function() {
  this.$container = this.$parent.appendDiv('action')
    .on('click', this._onClick.bind(this));
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(this._createLayout());
};

scout.Action.prototype._createLayout = function() {
  return new scout.NullLayout();
};

scout.Action.prototype._renderProperties = function() {
  scout.Action.parent.prototype._renderProperties.call(this);

  this._renderText();
  this._renderIconId();
  this._renderTooltipText();
  this._renderKeyStroke();
  this._renderSelected();
  this._renderTabbable();
  this._renderCompact();
};

scout.Action.prototype._remove = function() {
  this._removeText();
  this._removeIconId();
  scout.Action.parent.prototype._remove.call(this);
};

scout.Action.prototype.setText = function(text) {
  this.setProperty('text', text);
};

scout.Action.prototype._renderText = function() {
  var text = this.text || '';
  if (text && this.textVisible) {
    if (!this.$text) {
      // Create a separate text element to so that setting the text does not remove the icon
      this.$text = this.$container.appendSpan('content text');
      scout.HtmlComponent.install(this.$text, this.session);
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

scout.Action.prototype.setIconId = function(iconId) {
  this.setProperty('iconId', iconId);
};

scout.Action.prototype._renderIconId = function() {
  var iconId = this.iconId || '';
  // If the icon is an image (and not a font icon), the scout.Icon class will invalidate the layout when the image has loaded
  if (!iconId) {
    this._removeIconId();
    return;
  }
  if (this.icon) {
    this.icon.setIconDesc(iconId);
    return;
  }
  this.icon = scout.create('Icon', {
    parent: this,
    iconDesc: iconId,
    prepend: true
  });
  this.icon.one('destroy', function() {
    this.icon = null;
  }.bind(this));
  this.icon.render();
};

scout.Action.prototype.get$Icon = function() {
  if (this.icon) {
    return this.icon.$container;
  }
  return $();
};

scout.Action.prototype._removeIconId = function() {
  if (this.icon) {
    this.icon.destroy();
  }
};

/**
 * @override
 */
scout.Action.prototype._renderEnabled = function() {
  scout.Action.parent.prototype._renderEnabled.call(this);
  if (this.rendered) { // No need to do this during initial rendering
    this._updateTooltip();
    this._renderTabbable();
  }
};

scout.Action.prototype.setTooltipText = function(tooltipText) {
  this.setProperty('tooltipText', tooltipText);
};

scout.Action.prototype._renderTooltipText = function() {
  this._updateTooltip();
};

/**
 * Installs or uninstalls tooltip based on tooltipText, selected and enabledComputed.
 */
scout.Action.prototype._updateTooltip = function() {
  if (this._shouldInstallTooltip()) {
    scout.tooltips.install(this.$container, this._configureTooltip());
  } else {
    scout.tooltips.uninstall(this.$container);
  }
};

scout.Action.prototype._shouldInstallTooltip = function() {
  var show = this.tooltipText && this.enabledComputed;
  if (!this.showTooltipWhenSelected && this.selected) {
    show = false;
  }
  return show;
};

scout.Action.prototype._renderTabbable = function() {
  this.$container.setTabbable(this.tabbable && this.enabled && !scout.device.supportsTouch());
};

scout.Action.prototype._renderCompact = function() {
  this.$container.toggleClass('compact', this.compact);
  this.invalidateLayoutTree();
};

scout.Action.prototype.setTooltipPosition = function(position) {
  this.setProperty('tooltipPosition', position);
};

scout.Action.prototype._configureTooltip = function() {
  return {
    parent: this,
    text: this.tooltipText,
    $anchor: this.$container,
    arrowPosition: 50,
    arrowPositionUnit: '%',
    tooltipPosition: this.tooltipPosition
  };
};

/**
 * @return {Boolean}
 *          <code>true</code> if the action has been performed or <code>false</code> if it
 *          has not been performed (e.g. when the button is not enabledComputed).
 */
scout.Action.prototype.doAction = function() {
  if (!this.prepareDoAction()) {
    return false;
  }

  if (this.isToggleAction()) {
    this.setSelected(!this.selected);
  }
  this._doAction();
  return true;
};

scout.Action.prototype.toggle = function() {
  if (this.isToggleAction()) {
    this.setSelected(!this.selected);
  }
};

scout.Action.prototype.setToggleAction = function(toggleAction) {
  this.setProperty('toggleAction', toggleAction);
};

scout.Action.prototype.isToggleAction = function() {
  return this.toggleAction;
};

/**
 * @returns {Boolean} <code>true</code> if the action may be executed, <code>false</code> if it should be ignored.
 */
scout.Action.prototype.prepareDoAction = function() {
  if (!this.enabledComputed || !this.visible) {
    return false;
  }

  return true;
};

scout.Action.prototype._doAction = function() {
  this.trigger('action');
};

scout.Action.prototype.setSelected = function(selected) {
  this.setProperty('selected', selected);
};

scout.Action.prototype._renderSelected = function() {
  this.$container.toggleClass('selected', this.selected);
  if (this.rendered) { // prevent unnecessary tooltip updates during initial rendering
    this._updateTooltip();
  }
};

scout.Action.prototype.setKeyStroke = function(keyStroke) {
  this.setProperty('keyStroke', keyStroke);
};

scout.Action.prototype._setKeyStroke = function(keyStroke) {
  this.actionKeyStroke.parseAndSetKeyStroke(keyStroke);
  this._setProperty('keyStroke', keyStroke);
};

scout.Action.prototype._renderKeyStroke = function() {
  var keyStroke = this.keyStroke;
  if (keyStroke === undefined) {
    this.$container.removeAttr('data-shortcut');
  } else {
    this.$container.attr('data-shortcut', keyStroke);
  }
};

scout.Action.prototype.setTabbable = function(tabbable) {
  this.setProperty('tabbable', tabbable);
};

scout.Action.prototype.setTextVisible = function(textVisible) {
  if (this.textVisible === textVisible) {
    return;
  }
  this._setProperty('textVisible', textVisible);
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

scout.Action.prototype.setHorizontalAlignment = function(horizontalAlignment) {
  this.setProperty('horizontalAlignment', horizontalAlignment);
};

scout.Action.prototype._createActionKeyStroke = function() {
  return new scout.ActionKeyStroke(this);
};

scout.Action.prototype._allowMouseEvent = function(event) {
  if (event.which !== 1) {
    return false; // Other button than left mouse button --> nop
  }
  if (event.type === 'click' && event.detail > 1 && this.preventDoubleClick) {
    return false; // More than one consecutive click --> nop
  }
  return true;
};

scout.Action.prototype._onClick = function(event) {
  if (!this._allowMouseEvent(event)) {
    return;
  }

  // When the action is clicked the user wants to execute the action and not see the tooltip -> cancel the task
  // If it is already displayed it will stay
  scout.tooltips.cancel(this.$container);

  this.doAction();
};
