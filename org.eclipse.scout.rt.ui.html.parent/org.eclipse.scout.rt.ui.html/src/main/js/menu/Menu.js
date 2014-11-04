scout.Menu = function() {
  scout.Menu.parent.call(this);
  this.childMenus = [];
  this._addAdapterProperties('childMenus');
};
scout.inherits(scout.Menu, scout.ModelAdapter);

scout.Menu.EVENT_ABOUT_TO_SHOW = 'aboutToShow';
scout.Menu.EVENT_MENU_ACTION = 'menuAction';

scout.Menu.prototype.sendAboutToShow = function(event) {
  this.session.send(scout.Menu.EVENT_ABOUT_TO_SHOW, this.id);
};

scout.Menu.prototype.sendMenuAction = function(event) {
  this.session.send(scout.Menu.EVENT_MENU_ACTION, this.id);
};

scout.Menu.prototype._render = function($parent) {
  var text = this.text;
  if (this.hasButtonStyle()) {
    this.$container = $('<button>').
      appendTo($parent).
      addClass('menu-button').
      on('click', '', onClicked.bind(this));
    if (scout.Button.SYSTEM_TYPE.OK === this.systemType) {
      this.$container.
        addClass('default-button').
        addClass('last');
    }
  } else {
    this.$container = $parent.
       appendDIV('menu-item').
       on('click', '', onClicked.bind(this));
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

scout.Menu.prototype.hasButtonStyle = function() {
  return scout.menus.checkType(this, ["Form.System"]);
};

scout.Menu.prototype._onMenuClicked = function(event) {
  if (this.children.length > 0) {
    var popup = new scout.PopupMenuItem($(event.target));
    popup.render();
    scout.menus.appendMenuItems(popup, this.children);
    popup.alignTo();
  } else {
    this.sendMenuAction();
  }
};

scout.Menu.prototype._renderProperties = function() {
  this._renderText(this.text);
  this._renderIconId(this.iconId);
  this._renderEnabled(this.enabled);
  this._renderVisible(this.visible);
};

scout.Menu.prototype._renderText = function(text) {
  if (!text) {
    text = '';
  }
  this.$container.text(text);
  this._updateIconAndTextStyle();
};

scout.Menu.prototype._updateIconAndTextStyle = function() {
  var textAndIcon = (this.text && this.text.length > 0 && this.iconId);
  this.$container.toggleClass('menu-textandicon', !!textAndIcon);
};

scout.Menu.prototype._renderIconId = function(iconId) {
  this.$container.icon(iconId);
  this._updateIconAndTextStyle();
};

scout.Menu.prototype._renderEnabled = function(enabled) {
  this.$container.setEnabled(enabled);
};

scout.Menu.prototype._renderVisible = function(visible) {
  this.$container.setVisible(visible);
};

scout.Menu.prototype.goOffline = function() {
  scout.Menu.parent.prototype.goOffline.call(this);
  this._renderEnabled(false);
};

scout.Menu.prototype.goOnline = function() {
  scout.Menu.parent.prototype.goOnline.call(this);
  this._renderEnabled(true);
};
