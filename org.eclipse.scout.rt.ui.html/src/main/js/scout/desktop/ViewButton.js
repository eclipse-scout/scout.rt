// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.ViewButton = function() {
  scout.ViewButton.parent.call(this);
  this.$title;
  this._onMouseEvent = this.doAction.bind(this);
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
    .data('tooltipText', function() { return this.text; }.bind(this));

  this.$title = this.$container.appendSpan('view-button-tab-title');
};

/**
 * @override Action.js
 */
scout.ViewButton.prototype._renderText = function(text) {
  if (this._isMenu()) {
    scout.ViewButton.parent.prototype._renderText.call(this, text);
  } else {
    this.$title.text(this.selected ? text : '');
  }
};

/**
 * @override Action.js
 */
scout.ViewButton.prototype._renderSelected = function(selected) {
  scout.ViewButton.parent.prototype._renderSelected.call(this, selected);
  if (this._isTab()) {
    if (this.selected) {
      scout.tooltips.uninstall(this.$container);
      this.$title.text(this.text);
    } else {
      scout.tooltips.install(this.$container, this.session, {text: this.text});
      this.$title.text('');
    }
  }
};

/**
 * Use a default icon, when view-tab doesn't define one.
 * @override Action.js
 */
scout.ViewButton.prototype._renderIconId = function(iconId) {
  if (this._isTab()) {
    this.$container.icon(this.getIconId(iconId));
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

scout.ViewButton.prototype._onClick = function() {
  this.doAction();
};

scout.ViewButton.prototype.last = function() {
  this.$container.addClass('last');
};

/**
 * @override Action.js
 */
scout.ViewButton.prototype._drawKeyBox = function($container) {
  scout.ViewButton.parent.prototype._drawKeyBox.call(this,$container);
  if(this.iconId){
    var wIcon = this.$container.width();
    var wKeybox = this.$container.find('.key-box').outerWidth();
    var containerPadding = Number(this.$container.css('padding-left').replace('px', ''));
    var leftKeyBox = wIcon/2 - wKeybox/2 + containerPadding;
    this.$container.find('.key-box').css('left', leftKeyBox+'px');
  }
};
