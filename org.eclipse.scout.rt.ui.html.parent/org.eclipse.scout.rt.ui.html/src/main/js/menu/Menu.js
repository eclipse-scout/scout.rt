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
  this.$container = $parent
    .appendDiv('', 'menu-item')
    .on('click', '', onClicked.bind(this));

  function onClicked(event) {
    if (this.$container.isEnabled()) {
      return;
    }

    if (this.children.length > 0) {
      // TODO cru: mmh, works only for menubar, not for context menu...
      var right = parseFloat(this.parent.$container[0].offsetWidth) - parseFloat(this.$container.position().left) -  parseFloat(this.$container[0].offsetWidth),
        top = this.$container.height() - 3;

      scout.menus.showContextMenu(this.children, this.parent.$container, this.$container, undefined, right, top);
    } else {
      this.sendMenuAction();
    }
  }
};

scout.Menu.prototype._callSetters = function() {
  this._setText(this.text);
  this._setIconId(this.iconId);
  this._setEnabled(this.enabled);
  this._setVisible(this.visible);
};

scout.Menu.prototype._setText = function(text) {
  if (!text) {
    text = '';
  }
  this.$container.text(text);
};


scout.Menu.prototype._setIconId = function(iconId) {
  if (iconId && !this.text) {
    this.$container.attr('data-icon', iconId);
  } else {
    this.$container.removeAttr('data-icon');
  }
};

scout.Menu.prototype._setEnabled = function(enabled) {
  if (enabled) {
    this.$container.removeAttr('disabled');
  } else {
    this.$container.attr('disabled', 'disabled');
  }
};

scout.Menu.prototype._setVisible = function(visible) {
  if (visible) {
    this.$container.show();
  } else {
    this.$container.hide();
  }
};

scout.Menu.prototype.goOffline = function() {
  scout.Menu.parent.prototype.goOffline.call(this);
  this._setEnabled(false);
};

scout.Menu.prototype.goOnline = function() {
  scout.Menu.parent.prototype.goOnline.call(this);
  this._setEnabled(true);
};
