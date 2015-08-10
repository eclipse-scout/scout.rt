scout.Menu = function() {
  scout.Menu.parent.call(this);
  this.childActions = [];
  this._addAdapterProperties('childActions');
  this.popup;
  this.keyStrokeAdapter;

  /**
   * This property is true when the menu instance was moved into a overflow-menu
   * when there's not enough space on the screen (see MenuBarLayout.js). When set
   * to true, button style menus must be displayed as regular menus.
   */
  this.overflow = false;
};
scout.inherits(scout.Menu, scout.Action);

scout.Menu.prototype._render = function($parent) {
  if (this.separator) {
    this._renderSeparator($parent);
  } else {
    this._renderItem($parent);
  }
  this.$container.unfocusable();
};

scout.Menu.prototype._renderSeparator = function($parent) {
  this.$container = $parent.appendDiv('menu-separator');
};

scout.Menu.prototype._renderItem = function($parent) {
  if (scout.Action.ActionStyle.TASK_BAR === this.actionStyle) {
    this.$container = $parent.appendDiv('taskbar-tool-item');
  } else {
    this.$container = $parent.appendDiv('menu-item');
  }

  this.$container
    .on('mousedown', '', this._onMouseEvent.bind(this))
    .on('contextmenu', '', this._onMouseEvent.bind(this))
    .on('click', '', this._onMouseEvent.bind(this));
  if (this.childActions.length > 0 && this.text) {
    this.$container.addClass('has-submenu');
  }
  if (this.visible && this.enabled) {
    this._registerKeyStrokeAdapter();
  }

  // when menus with button style are displayed in a overflow-menu,
  // render as regular menu, ignore button styles.
  if (!this.overflow) {
    if (scout.Action.ActionStyle.BUTTON === this.actionStyle ||
      scout.Action.ActionStyle.TOGGLE === this.actionStyle) {
      this.$container.addClass('menu-button');
    }
  }
};

scout.Menu.prototype._onMouseEvent = function(event) {
  if (event.which !== 1) {
    return; // Other button than left mouse button --> nop
  }

  // If menu has childActions, a popup should be rendered on click. To create
  // the impression of a faster UI, open the popup already on 'mousedown', not
  // on 'click'. All other actions are handeld on 'click'.
  var hasChildActions = (this.childActions.length > 0);
  if (event.type === 'mousedown' && hasChildActions) {
    this.doAction(event);
  } else if ((event.type === 'click' || event.type ==='contextmenu') && !hasChildActions) {
    this.doAction(event);
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

scout.Menu.prototype._renderVisible = function(visible) {
  scout.Menu.parent.prototype._renderVisible.call(this, visible);
  if (visible) {
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

scout.Menu.prototype.isTabTarget = function() {
  return this.enabled && this.visible && (this.actionStyle === scout.Action.ActionStyle.BUTTON || !this.separator);
};

scout.Menu.prototype._updateIconAndTextStyle = function() {
  if (scout.Action.ActionStyle.TASK_BAR !== this.actionStyle) {
    var textAndIcon = (this.text && this.text.length > 0 && this.iconId);
    this.$container.toggleClass('menu-textandicon', !! textAndIcon);
  }
};

scout.Menu.prototype.togglePopup = function(event) {
  if (this.popup) {
    this.popup.close(event);
  } else {
    this.popup = this._openPopup(event);
    this.popup.on('close', function(event) {
      this.popup = null;
    }.bind(this));
  }
};

/**
 * @param event
 *          UI event that triggered the popup (e.g. 'mouse clicked'). This argument
 *          is passed to the MenuBarPopup  as 'ignoreEvent'. It prevents the popup
 *          from being closed again by the same event that bubbled to other elements.
 */
scout.Menu.prototype._openPopup = function(event) {
  var popup = new scout.MenuBarPopup(this, this.session, {
    ignoreEvent: event
  });
  popup.render();
  return popup;
};

/**
 * @override Action.js
 */
scout.Menu.prototype.doAction = function(event) {
  if (this.childActions.length) {
    // Special handling if menu has childActions
    if (this.prepareDoAction()) {
      this.togglePopup(event);
      return true;
    } else {
      return false;
    }
  }
  // Default action handling
  return scout.Menu.parent.prototype.doAction.call(this, event);
};

scout.Menu.prototype._drawKeyBox = function($container) {
  scout.Menu.parent.prototype._drawKeyBox.call(this, this.$container);
};

scout.Menu.prototype._registerKeyStrokeAdapter = function() {
  this.keyStrokeAdapter = this.keyStrokeAdapter || new scout.MenuKeyStrokeAdapter(this);
  scout.keyStrokeUtils.installAdapter(this.session, this.keyStrokeAdapter, this.$container);
};

scout.Menu.prototype._unregisterKeyStrokeAdapter = function() {
  scout.keyStrokeUtils.uninstallAdapter(this.keyStrokeAdapter);
};
