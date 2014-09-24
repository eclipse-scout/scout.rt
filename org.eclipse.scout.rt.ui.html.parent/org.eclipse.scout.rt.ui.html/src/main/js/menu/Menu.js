scout.Menu = function() {
  scout.Menu.parent.call(this);
  this.childMenus = [];
  this._addAdapterProperties('childMenus');

  var that = this;
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
  this.$container = $parent
    .appendDIV('menu-item')
    .on('click', '', onClicked.bind(this));

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
    //TODO cru: work in progress
    if (this.$container.hasClass('menu-right')) {
      var right = parseFloat(this.parent.$container[0].offsetWidth) - parseFloat(this.$container.position().left) -  parseFloat(this.$container[0].offsetWidth),
        top = this.$container.height() - 7;
      scout.menus.showContextMenu(this.children, this.parent.$container, this.$container, undefined, right, top, false, true);
    } else {
      var left = parseFloat(this.$container.position().left) + 8;
      scout.menus.showContextMenu(this.children, this.parent.$container, this.$container, left, undefined, 8, true, false);
    }
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
};


scout.Menu.prototype._renderIconId = function(iconId) {
  this.$container.icon(iconId);
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
