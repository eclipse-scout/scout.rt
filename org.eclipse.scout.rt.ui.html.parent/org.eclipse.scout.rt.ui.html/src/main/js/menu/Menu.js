scout.Menu = function() {
  scout.Menu.parent.call(this);
  this.childMenus = [];
  this._addAdapterProperties('childMenus');
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
  if (this.children.length > 0) {
    var popup = new scout.PopupMenuItem($(event.target));
    popup.render();
    scout.menus.appendMenuItems(popup, this.children);
    popup.alignTo();
  } else {
    this.sendDoAction();
  }
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
