scout.Menu = function() {
  scout.Menu.parent.call(this);
  this.childActions = [];
  this._addAdapterProperties('childActions');
  this.popup;
  this.excludedByFilter = false;

  /**
   * This property is true when the menu instance was moved into a overflow-menu
   * when there's not enough space on the screen (see MenuBarLayout.js). When set
   * to true, button style menus must be displayed as regular menus.
   */
  this.overflow = false;
};
scout.inherits(scout.Menu, scout.Action);

/**
 * @override ModelAdapter
 */
scout.Menu.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.Menu.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  keyStrokeContext.registerKeyStroke(new scout.MenuExecKeyStroke(this));
};

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
  this.$container = $parent.appendDiv('menu-item');
  if (this._customCssClasses) {
    this.$container.addClass(this._customCssClasses);
  }

  this.$container
    .on('mousedown', '', this._onMouseEvent.bind(this))
    .on('contextmenu', '', this._onMouseEvent.bind(this))
    .on('click', '', this._onMouseEvent.bind(this));
  if (this.childActions.length > 0 && this.text) {
    this.$container.addClass('has-submenu');
  }

  // when menus with button style are displayed in a overflow-menu,
  // render as regular menu, ignore button styles.
  if (!this.overflow) {
    if (scout.Action.ActionStyle.BUTTON === this.actionStyle) {
      this.$container.addClass('menu-button');
    }
  }
};

scout.Menu.prototype._renderSelected = function() {
  if (this.selected) {
    if (this._doActionTogglesPopup()) {
      this._openPopup();
    }
  } else {
    this._closePopup();
  }
};

scout.Menu.prototype._onMouseEvent = function(event) {
  if (event.which !== 1) {
    return; // Other button than left mouse button --> nop
  }

  // If menu has childActions, a popup should be rendered on click. To create
  // the impression of a faster UI, open the popup already on 'mousedown', not
  // on 'click'. All other actions are handled on 'click'.
  if (event.type === 'mousedown' && this._doActionTogglesPopup()) {
    this.doAction(event);
  } else if ((event.type === 'click' || event.type === 'contextmenu') && !this._doActionTogglesPopup()) {
    this.doAction(event);
  }
};

/**
 * May be overridden if the criteria to open a popup differs
 */
scout.Menu.prototype._doActionTogglesPopup = function() {
  return this.childActions.length > 0;
};

scout.Menu.prototype._renderText = function(text) {
  scout.Menu.parent.prototype._renderText.call(this, text);
  this._updateIconAndTextStyle();
};

scout.Menu.prototype._renderIconId = function() {
  scout.Menu.parent.prototype._renderIconId.call(this);
  this._updateIconAndTextStyle();
};

scout.Menu.prototype.isTabTarget = function() {
  return this.enabled && this.visible && (this.actionStyle === scout.Action.ActionStyle.BUTTON || !this.separator);
};

scout.Menu.prototype._updateIconAndTextStyle = function() {
  var textAndIcon = (this.text && this.text.length > 0 && this.iconId);
  this.$container.toggleClass('menu-textandicon', !! textAndIcon);
};

scout.Menu.prototype._closePopup = function() {
  if (this.popup) {
    this.popup.close();
  }
};

/**
 * @param event
 *          UI event that triggered the popup (e.g. 'mouse clicked'). This argument
 *          is passed to the MenuBarPopup  as 'ignoreEvent'. It prevents the popup
 *          from being closed again by the same event that bubbled to other elements.
 */
scout.Menu.prototype._openPopup = function(event) {
  if (this.popup) {
    // already open
    return;
  }
  this.popup = this._createPopup(event);
  this.popup.render();
  this.popup.on('remove', function(event) {
    this.popup = null;
  }.bind(this));
  // Reason for separating remove and close event:
  // Remove may be called if parent (menubar) gets removed or rebuilt.
  // In that case, we do not want to change the selected state because after rebuilding the popup should still be open
  // In every other case the state of the menu needs to be reseted if the popup closes
  this.popup.on('close', function(event) {
    this.setSelected(false);
  }.bind(this));
};

scout.Menu.prototype._createPopup = function(event) {
  return scout.create(scout.MenuBarPopup, {
    parent: this,
    menu: this,
    ignoreEvent: event,
    openingDirectionX: this.popupOpeningDirectionX,
    openingDirectionY: this.popupOpeningDirectionY
  });
};

scout.Menu.prototype._createActionKeyStroke = function() {
  return new scout.MenuKeyStroke(this);
};

scout.Menu.prototype.isToggleAction = function() {
  return this.childActions.length > 0 || this.toggleAction;
};
