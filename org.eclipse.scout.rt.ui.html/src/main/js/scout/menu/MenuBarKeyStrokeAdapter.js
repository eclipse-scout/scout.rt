scout.MenuBarKeyStrokeAdapter = function(menuBar) {
  scout.MenuBarKeyStrokeAdapter.parent.call(this, menuBar);

  this.keyStrokes.push(new scout.MenuBarLeftKeyStroke(menuBar));
  this.keyStrokes.push(new scout.MenuBarRightKeyStroke(menuBar));
  if (this._field && this._field.menuItems && this._field.menuItems.length > 0 && this._field.menuItems[0].session) {
    this._uiSessionId = this._field.menuItems[0].session.uiSessionId;
  }
};
scout.inherits(scout.MenuBarKeyStrokeAdapter, scout.AbstractKeyStrokeAdapter);

scout.MenuBarLeftKeyStroke = function(menuBar, keyStroke) {
  scout.MenuBarLeftKeyStroke.parent.call(this);
  this.drawHint = true;
  this.keyStroke = 'LEFT';
  this.menuBar = menuBar;
  this.initKeyStrokeParts();
  this.bubbleUp = false;
};
scout.inherits(scout.MenuBarLeftKeyStroke, scout.KeyStroke);
/**
 * @Override scout.KeyStroke
 */
scout.MenuBarLeftKeyStroke.prototype.handle = function(event) {
  var menuItems = this.menuBar.menuItems;
  var $menuItemFocused = this.menuBar.$container.find(':focus');
  var lastValidItem, elementToFocus;

  for (var i = 0; i < menuItems.length; i++) {
    var actualItem = menuItems[i];
    actualItem.$container.removeAttr('tabindex');
    if ($menuItemFocused[0] === actualItem.$container[0] || (actualItem.$field && $menuItemFocused[0] === actualItem.$field[0])) {
      if (lastValidItem) {
        elementToFocus = lastValidItem;
      }
      break;
    }
    if ((actualItem instanceof scout.Button || (actualItem instanceof scout.Menu && !actualItem.separator)) && actualItem.visible && actualItem.enabled) {
      lastValidItem = actualItem;
    }
  }
  if (elementToFocus) {
    if (elementToFocus instanceof scout.Button) {
      elementToFocus.$field.attr('tabindex', 0);
      elementToFocus.$field.focus();
    } else {
      elementToFocus.$container.attr('tabindex', 0);
      elementToFocus.$container.focus();
    }
  } else {
    $menuItemFocused.attr('tabindex', 0);
  }
};

scout.MenuBarLeftKeyStroke.prototype._drawKeyBox = function($container) {

};

scout.MenuBarRightKeyStroke = function(menuBar, keyStroke) {
  scout.MenuBarRightKeyStroke.parent.call(this);
  this.drawHint = true;
  this.keyStroke = 'RIGHT';
  this.menuBar = menuBar;
  this.initKeyStrokeParts();
  this.bubbleUp = false;
};
scout.inherits(scout.MenuBarRightKeyStroke, scout.KeyStroke);
/**
 * @Override scout.KeyStroke
 */
scout.MenuBarRightKeyStroke.prototype.handle = function(event) {
  var menuItems = this.menuBar.menuItems;
  var $menuItemFocused = this.menuBar.$container.find(':focus');
  var focusNext = false,
    elementToFocus;

  for (var i = 0; i < menuItems.length; i++) {
    var actualItem = menuItems[i];
    actualItem.$container.removeAttr('tabindex');
    if (focusNext && (actualItem instanceof scout.Button || (actualItem instanceof scout.Menu && !actualItem.separator)) && actualItem.visible && actualItem.enabled) {
      focusNext = false;
      elementToFocus = actualItem;
      break;
    }
    if ($menuItemFocused[0] === actualItem.$container[0] || (actualItem.$field && $menuItemFocused[0] === actualItem.$field[0])) {
      focusNext = true;
    }
  }
  if (elementToFocus) {
    if (elementToFocus instanceof scout.Button) {
      elementToFocus.$field.attr('tabindex', 0);
      elementToFocus.$field.focus();
    } else {
      elementToFocus.$container.attr('tabindex', 0);
      elementToFocus.$container.focus();
    }
  } else {
    $menuItemFocused.attr('tabindex', 0);
  }
};

scout.MenuBarRightKeyStroke.prototype._drawKeyBox = function($container) {

};
