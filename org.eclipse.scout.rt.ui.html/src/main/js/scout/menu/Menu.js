scout.Menu = function() {
  scout.Menu.parent.call(this);
  this.childActions = [];
  this._addAdapterProperties('childActions');
  this.popup = undefined; // FIXME AWE: use this for different style?
  this.keyStrokeAdapter;
};
scout.inherits(scout.Menu, scout.Action);

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
  if ('taskbar' === this.menuStyle) {
    this.$container = $parent
      .appendDiv('taskbar-tool-item');
  } else {
    this.$container = $parent
      .appendDiv('menu-item')
      .attr('tabindex', 0);
  }
  this.$container.on('click', '', onClicked.bind(this));
  if (this.childActions.length > 0 && this.text) {
    this.$container.addClass('has-submenu');
  }
  if (this.visible && this.enabled) {
    this._registerKeyStrokeAdapter();
  }

  // --- Helper functions ---

  function onClicked(event) {
    if (!this.$container.isEnabled()) {
      return;
    }
    this._onMenuClicked(event);
  }
};

scout.Menu.prototype._onMenuClicked = function(event) {
  if (this.$container.isEnabled()) {
    this.doAction($(event.target));
  }
};

scout.Menu.prototype._renderEnabled = function(enabled) {
  scout.Menu.parent.prototype._renderEnabled.call(this, enabled);
  if (enabled) {
    this._registerKeyStrokeAdapter();
  } else {
    this._unregisterKeyStrokeAdapter();
  }
};

scout.Menu.prototype._renderVisible = function(enabled) {
  scout.Menu.parent.prototype._renderVisible.call(this, enabled);
  if (enabled) {
    this._registerKeyStrokeAdapter();
  } else {
    this._unregisterKeyStrokeAdapter();
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
  if ('taskbar' === this.menuStyle) {
    return;
  }
  var textAndIcon = (this.text && this.text.length > 0 && this.iconId);
  this.$container.toggleClass('menu-textandicon', !! textAndIcon);
};

scout.Menu.prototype.doAction = function($target) {
  if (this.childActions.length > 0) {
    this.popup = new scout.MenuBarPopup(this, this.session);
    this.popup.render();
  } else {
    this.sendDoAction();
  }
};

scout.Menu.prototype.handle = function(event) {
  this.doAction(this.$container);
  if (this.preventDefaultOnEvent) {
    event.preventDefault();
  }
};

scout.Menu.prototype._drawKeyBox = function($container) {
  scout.Menu.parent.prototype._drawKeyBox.call(this, $container);
};

scout.Menu.prototype._registerKeyStrokeAdapter = function() {
  if (!this.keyStrokeAdapter) {
    this.keyStrokeAdapter = new scout.MenuKeyStrokeAdapter(this);
  }
  scout.keyStrokeManager.installAdapter(this.$container, this.keyStrokeAdapter);
};

scout.Menu.prototype._unregisterKeyStrokeAdapter = function() {
  if (this.keyStrokeAdapter) {
    scout.keyStrokeManager.uninstallAdapter(this.keyStrokeAdapter);
  }
};
