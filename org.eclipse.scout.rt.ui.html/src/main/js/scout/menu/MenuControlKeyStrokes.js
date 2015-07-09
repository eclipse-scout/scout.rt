scout.MenuControlKeyStrokes = function(popup, menuItemClass) {
  scout.MenuControlKeyStrokes.parent.call(this);
  this._popup = popup;
  this._menuItemClass = menuItemClass;
  this.drawHint = true;
  this.initKeyStrokeParts();
};
scout.inherits(scout.MenuControlKeyStrokes, scout.KeyStroke);

/**
 * @override scout.KeyStroke
 */
scout.MenuControlKeyStrokes.prototype.handle = function(event) {
  var $newSelection,
    keycode = event.which,
    $menuItems = this._popup.$body.find('.' + this._menuItemClass),
    $selectedMenuItem = this._popup.$body.find('.' + this._menuItemClass + '.selected');

  if ((keycode === scout.keys.SPACE || keycode === scout.keys.ENTER) && $selectedMenuItem.length > 0) {
    $selectedMenuItem.trigger('click');
  }
  // move up
  if (keycode === scout.keys.UP) {
    if ($selectedMenuItem.length > 0) {
      $newSelection = $selectedMenuItem.prevAll(':visible').first();
    } else {
      $newSelection = $menuItems.first();
    }
    this._changeSelection($selectedMenuItem, $newSelection);
  }

  // move down
  if (keycode === scout.keys.DOWN) {
    if ($selectedMenuItem.length > 0) {
      $newSelection = $selectedMenuItem.nextAll(':visible').first();
    } else {
      $newSelection = $menuItems.first();
    }
    this._changeSelection($selectedMenuItem, $newSelection);
  }
  event.preventDefault();
};

scout.MenuControlKeyStrokes.prototype._changeSelection = function($oldItem, $newItem) {
  if ($newItem.length === 0) {
    // do not change selection
    return;
  } else {
    $newItem.select(true).focus();
  }
  if ($oldItem.length > 0) {
    $oldItem.select(false);
  }
};

/**
 * @override scout.KeyStroke
 */
scout.MenuControlKeyStrokes.prototype._drawKeyBox = function($container, drawedKeys) {
  // NOP
};

/**
 * @override scout.KeyStroke
 */
scout.MenuControlKeyStrokes.prototype.checkAndDrawKeyBox = function($container, drawedKeys) {
  this._drawKeyBox($container, drawedKeys);
};

/**
 * @override scout.KeyStroke
 */
scout.MenuControlKeyStrokes.prototype.accept = function(event) {
  return event &&
    $.inArray(event.which, [scout.keys.UP, scout.keys.DOWN, scout.keys.SPACE, scout.keys.ENTER]) >= 0 &&
    event.ctrlKey === this.ctrl &&
    event.altKey === this.alt &&
    event.shiftKey === this.shift;
};
