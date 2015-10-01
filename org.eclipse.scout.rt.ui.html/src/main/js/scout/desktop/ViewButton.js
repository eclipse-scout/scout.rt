// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.ViewButton = function() {
  scout.ViewButton.parent.call(this);
  this.$title;
  this._onMouseEvent = this.doAction.bind(this);
  this._breadcrumbEnabled = false;
};
scout.inherits(scout.ViewButton, scout.Action);

scout.ViewButton.prototype._render = function($parent) {
  if (this._isMenu()) {
    this._renderAsMenu($parent);
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

scout.ViewButton.prototype._renderAsMenu = function($parent) {
  this.$container = $parent.appendDiv('view-button-menu')
    .on('click', this._onMouseEvent);
};

scout.ViewButton.prototype._renderAsTab = function($parent) {
  this.$container = $parent.appendDiv('view-button-tab')
    .on('mousedown', this._onMouseEvent)
    .data('tooltipText', function() {
      return this.text;
    }.bind(this));

  this.$title = this.$container.appendSpan('view-button-tab-title');
};

/**
 * @override Action.js
 */
scout.ViewButton.prototype._renderText = function() {
  if (this._isMenu()) {
    scout.ViewButton.parent.prototype._renderText.call(this);
  } else {
    this.$title.css('display', this._breadcrumbEnabled || this._isTab() ? 'none' : '');
  }
};

/**
 * @override Action.js
 */
scout.ViewButton.prototype._renderSelected = function() {
  scout.ViewButton.parent.prototype._renderSelected.call(this);
  if (this._isTab()) {
      scout.tooltips.install(this.$container, {
        parent: this,
        text: this.text
      });
    }
};

/**
 * Use a default icon, when view-tab doesn't define one.
 * @override Action.js
 */
scout.ViewButton.prototype._renderIconId = function() {
  if (this._isTab()) {
    this.$container.icon(this.getIconId(this.iconId));
  }
};

/**
 * Returns a default-icon when no icon is set.
 */
scout.ViewButton.prototype.getIconId = function(iconId) {
  if (arguments.length === 0) {
    iconId = this.iconId;
  }
  if (!scout.strings.hasText(iconId)) {
    iconId = scout.icons.OUTLINE;
  }
  return iconId;
};

scout.ViewButton.prototype.last = function() {
  this.$container.addClass('last');
};

scout.ViewButton.prototype.setBreadcrumbEnabled = function(enabled) {
  this._breadcrumbEnabled = enabled;
  this._renderText();
  this._renderSelected();
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
  if (this.field.iconId) {
    var wIcon = $drawingArea.width();
    var wKeybox = $drawingArea.find('.key-box').outerWidth();
    var containerPadding = Number($drawingArea.css('padding-left').replace('px', ''));
    var leftKeyBox = wIcon / 2 - wKeybox / 2 + containerPadding;
    $drawingArea.find('.key-box').css('left', leftKeyBox + 'px');
  }
};
