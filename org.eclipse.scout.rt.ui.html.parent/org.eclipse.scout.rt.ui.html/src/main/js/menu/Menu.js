scout.Menu = function() {
  scout.Menu.parent.call(this);
  this.childMenus = [];
  this._addAdapterProperties('childMenus');
  this.popup = undefined;
};
scout.inherits(scout.Menu, scout.Action);

scout.Menu.prototype.sendAboutToShow = function(event) {
  this.session.send(this.id, 'aboutToShow');
};

scout.Menu.prototype._render = function($parent) {
  if (this.separator) {
    this._renderSeparator($parent);
  } else {
    this._renderItem($parent);
  }
};

scout.Menu.prototype._renderSeparator = function($parent) {
  this.$container = $parent.appendDiv('menu-separator');
};

scout.Menu.prototype._renderItem = function($parent) {
  this.$container = $parent
    .appendDiv('menu-item')
    .on('click', '', onClicked.bind(this));

  if ('taskbar' === this.menuStyle) {
    this.$container.addClass('taskbar');
  }
  if (this.childMenus.length > 0 && this.text) {
    this.$container.addClass('has-submenu');
  }

  function onClicked(event) {
    if (!this.$container.isEnabled()) {
      return;
    }
    this._onMenuClicked(event);
  }
};

scout.Menu.prototype._onMenuClicked = function(event) {
  this.doAction($(event.target));
};

scout.Menu.prototype._renderText = function(text) {
  scout.Menu.parent.prototype._renderText.call(this, text);
  this._updateIconAndTextStyle();
};

scout.Menu.prototype._renderIconId = function(iconId) {
  scout.Menu.parent.prototype._renderIconId.call(this, iconId);
  this._updateIconAndTextStyle();
};

scout.Menu.prototype._updateIconAndTextStyle = function() {
  var textAndIcon = (this.text && this.text.length > 0 && this.iconId);
  this.$container.toggleClass('menu-textandicon', !! textAndIcon);
};

scout.Menu.prototype.doAction = function($target) {
  if (this.children.length > 0) {
    this.popup = new scout.PopupMenuItem($target);
    this.popup.render();
    scout.menus.appendMenuItems(this.popup, this.children);
    this.popup.alignTo();
  } else {
    this.sendDoAction();
  }
};

scout.Menu.prototype.handle = function(event) {
  if (this.popup && this.popup.$container) {
    return;
  }
  this.doAction(this.$container);
  if (this.preventDefaultOnEvent) {
    event.preventDefault();
  }
};

scout.Menu.prototype.ignore = function(event) {
  return this.popup && this.popup.$container;
};

scout.Menu.prototype._drawKeyBox = function($container) {
  scout.Menu.parent.prototype._drawKeyBox.call(this, $container);
};
