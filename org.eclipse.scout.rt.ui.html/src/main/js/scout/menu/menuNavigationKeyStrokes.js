scout.menuNavigationKeyStrokes = {

  registerKeyStrokes: function(keyStrokeContext, popup, menuItemClass) {
    keyStrokeContext.registerKeyStroke([
      new scout.MenuNavigationUpKeyStroke(popup, menuItemClass),
      new scout.MenuNavigationDownKeyStroke(popup, menuItemClass),
      new scout.MenuNavigationExecKeyStroke(popup, menuItemClass)
    ]);
  },

  _findMenuItems: function(popup, menuItemClass) {
    return {
      $all: popup.$body.find('.' + menuItemClass),
      $selected: popup.$body.find('.' + menuItemClass + '.selected')
    };
  },

  _changeSelection: function($oldItem, $newItem) {
    if ($newItem.length === 0) {
      // do not change selection
      return;
    } else {
      $newItem.select(true).focus();
    }
    if ($oldItem.length > 0) {
      $oldItem.select(false);
    }
  }
};

/**
 * MenuNavigationUpKeyStroke
 */
scout.MenuNavigationUpKeyStroke = function(popup, menuItemClass) {
  scout.MenuNavigationUpKeyStroke.parent.call(this);
  this._menuItemClass = menuItemClass;
  this.field = popup;
  this.which = [scout.keys.UP];
  this.renderingHints.render = false;
};
scout.inherits(scout.MenuNavigationUpKeyStroke, scout.KeyStroke);

scout.MenuNavigationUpKeyStroke.prototype.handle = function(event) {
  var menuItems = scout.menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass);
  if (menuItems.$selected.length > 0) {
    scout.menuNavigationKeyStrokes._changeSelection(menuItems.$selected, menuItems.$selected.prevAll(':visible').first());
  } else {
    scout.menuNavigationKeyStrokes._changeSelection(menuItems.$selected, menuItems.$all.first());
  }
};

/**
 * MenuNavigationDownKeyStroke
 */
scout.MenuNavigationDownKeyStroke = function(popup, menuItemClass) {
  scout.MenuNavigationDownKeyStroke.parent.call(this);
  this._menuItemClass = menuItemClass;
  this.field = popup;
  this.which = [scout.keys.DOWN];
  this.renderingHints.render = false;
};
scout.inherits(scout.MenuNavigationDownKeyStroke, scout.KeyStroke);

scout.MenuNavigationDownKeyStroke.prototype.handle = function(event) {
  var menuItems = scout.menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass);
  if (menuItems.$selected.length > 0) {
    scout.menuNavigationKeyStrokes._changeSelection(menuItems.$selected, menuItems.$selected.nextAll(':visible').first());
  } else {
    scout.menuNavigationKeyStrokes._changeSelection(menuItems.$selected, menuItems.$all.first());
  }
};

/**
 * MenuNavigationExecKeyStroke
 */
scout.MenuNavigationExecKeyStroke = function(popup, menuItemClass) {
  scout.MenuNavigationExecKeyStroke.parent.call(this);
  this._menuItemClass = menuItemClass;
  this.field = popup;
  this.which = [scout.keys.ENTER, scout.keys.SPACE];
  this.renderingHints.render = false;
};
scout.inherits(scout.MenuNavigationExecKeyStroke, scout.KeyStroke);

scout.MenuNavigationExecKeyStroke.prototype.handle = function(event) {
  var menuItems = scout.menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass);
  ['mousedown', 'mouseup', 'click'].forEach(function(eventType) {
    menuItems.$selected.trigger({
      type: eventType,
      which: 1
    });
  }); // simulate left-mouse click (full click event sequence in order, see scout.Menu.prototype._onMouseEvent)
};
