scout.MenuControlKeyStrokes = function(popup) {
  scout.MenuControlKeyStrokes.parent.call(this);
  this.drawHint = true;
  this._popup = popup;
  this.initKeyStrokeParts();
};
scout.inherits(scout.MenuControlKeyStrokes, scout.KeyStroke);
/**
 * @Override scout.KeyStroke
 */
scout.MenuControlKeyStrokes.prototype.handle = function(event) {
  var $menuItems = this._popup.$body.find('.menu-item');
  var $selectedMenuItem = this._popup.$body.find('.menu-item.selected');
  var keycode = event.which;
  var $newSelection;
  if (keycode === scout.keys.ESC) {
    this._popup.remove();
  }

  if ((keycode === scout.keys.SPACE || keycode === scout.keys.ENTER) && $selectedMenuItem.length > 0) {
    $selectedMenuItem.trigger('click');
  }
  // up: move up
  if (keycode === scout.keys.UP) {
    if ($selectedMenuItem.length > 0) {
      $newSelection = $selectedMenuItem.first().prev();
    } else {
      $newSelection = $menuItems.first();
    }
    this._changeSelection($selectedMenuItem, $newSelection);
  }

  // down: move down
  if (keycode === scout.keys.DOWN) {
    if ($selectedMenuItem.length > 0) {
      $newSelection = $selectedMenuItem.first().next();
    } else {
      $newSelection = $menuItems.first();
    }
    this._changeSelection($selectedMenuItem, $newSelection);
  }
  event.preventDefault();
};

scout.MenuControlKeyStrokes.prototype._changeSelection = function($oldItem, $newItem) {
  if ($newItem.length === 0) {
    //do not change selection
    return;
  } else {
    $newItem.addClass('selected');
  }

  if ($oldItem.length > 0) {
    $oldItem.removeClass('selected');
  }
};
/**
 * @Override scout.KeyStroke
 */
scout.MenuControlKeyStrokes.prototype._drawKeyBox = function($container, drawedKeys) {

};

/**
 * @Override scout.KeyStroke
 */
scout.MenuControlKeyStrokes.prototype.checkAndDrawKeyBox = function($container, drawedKeys) {
  this._drawKeyBox($container, drawedKeys);
};
/**
 * @Override scout.KeyStroke
 */
scout.MenuControlKeyStrokes.prototype.accept = function(event) {
  return event &&
    $.inArray(event.which, [scout.keys.UP, scout.keys.DOWN, scout.keys.SPACE, scout.keys.ENTER, scout.keys.ESC]) >= 0 &&
    event.ctrlKey === this.ctrl &&
    event.altKey === this.alt &&
    event.shiftKey === this.shift;
};
