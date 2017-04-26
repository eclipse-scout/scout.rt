/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.menuNavigationKeyStrokes = {

  registerKeyStrokes: function(keyStrokeContext, popup, menuItemClass) {
    keyStrokeContext.registerKeyStroke([
      new scout.MenuNavigationUpKeyStroke(popup, menuItemClass),
      new scout.MenuNavigationDownKeyStroke(popup, menuItemClass),
      new scout.MenuNavigationExecKeyStroke(popup, menuItemClass),
      new scout.MenuExecByNumberKeyStroke(popup, menuItemClass),
      new scout.SubCloseKeyStroke(popup, menuItemClass)
    ]);
  },

  _findMenuItems: function(popup, menuItemClass) {
    return {
      $all: popup.$body.find('.' + menuItemClass),
      $allVisible: popup.$body.find('.' + menuItemClass + ':visible'),
      $selected: popup.$body.find('.' + menuItemClass + '.selected')
    };
  },

  _changeSelection: function($oldItem, $newItem) {
    if ($newItem.length === 0) {
      // do not change selection
      return;
    } else {
      $newItem.select(true).focus();
      if (this.field.updateNextToSelected) {
        this.field.updateNextToSelected(this._menuItemClass, $newItem);
      }
    }
    if ($oldItem.length > 0) {
      $oldItem.select(false);
    }
  }
};

/**
 * MenuNavigationKeyStroke
 */
scout.MenuNavigationKeyStroke = function(popup) {
  scout.MenuNavigationKeyStroke.parent.call(this);
  this.field = popup;
};
scout.inherits(scout.MenuNavigationKeyStroke, scout.KeyStroke);

scout.MenuNavigationKeyStroke.prototype._accept = function(event) {
  var accepted = scout.MenuNavigationKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted || this.field.bodyAnimating) {
    return false;
  }
  return accepted;
};

/**
 * MenuNavigationUpKeyStroke
 */
scout.MenuNavigationUpKeyStroke = function(popup, menuItemClass) {
  scout.MenuNavigationUpKeyStroke.parent.call(this, popup);
  this._menuItemClass = menuItemClass;
  this.which = [scout.keys.UP];
  this.renderingHints.render = false;
};
scout.inherits(scout.MenuNavigationUpKeyStroke, scout.MenuNavigationKeyStroke);

scout.MenuNavigationUpKeyStroke.prototype.handle = function(event) {
  var menuItems = scout.menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass);
  if (menuItems.$selected.length > 0) {
    scout.menuNavigationKeyStrokes._changeSelection.call(this, menuItems.$selected, menuItems.$selected.prevAll(':visible').first());
  } else {
    scout.menuNavigationKeyStrokes._changeSelection.call(this, menuItems.$selected, menuItems.$allVisible.last());
  }
};

/**
 * MenuNavigationDownKeyStroke
 */
scout.MenuNavigationDownKeyStroke = function(popup, menuItemClass) {
  scout.MenuNavigationDownKeyStroke.parent.call(this, popup);
  this._menuItemClass = menuItemClass;
  this.which = [scout.keys.DOWN];
  this.renderingHints.render = false;
};
scout.inherits(scout.MenuNavigationDownKeyStroke, scout.MenuNavigationKeyStroke);

scout.MenuNavigationDownKeyStroke.prototype.handle = function(event) {
  var menuItems = scout.menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass);
  if (menuItems.$selected.length > 0) {
    scout.menuNavigationKeyStrokes._changeSelection.call(this, menuItems.$selected, menuItems.$selected.nextAll(':visible').first());
  } else {
    scout.menuNavigationKeyStrokes._changeSelection.call(this, menuItems.$selected, menuItems.$allVisible.first());
  }
};

/**
 * MenuNavigationExecKeyStroke
 */
scout.MenuNavigationExecKeyStroke = function(popup, menuItemClass) {
  scout.MenuNavigationExecKeyStroke.parent.call(this, popup);
  this._menuItemClass = menuItemClass;
  this.stopImmediatePropagation = true;
  this.which = [scout.keys.ENTER, scout.keys.SPACE];
  this.renderingHints.render = false;
};
scout.inherits(scout.MenuNavigationExecKeyStroke, scout.MenuNavigationKeyStroke);

scout.MenuNavigationExecKeyStroke.prototype.handle = function(event) {
  var $menuItem = scout.menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass).$selected;
  $menuItem.data('widget').doAction();
};

/**
 * SubCloseKeyStroke
 */
scout.SubCloseKeyStroke = function(popup, menuItemClass) {
  scout.SubCloseKeyStroke.parent.call(this, popup, menuItemClass);
  this._menuItemClass = menuItemClass;
  this.which = [scout.keys.BACKSPACE];
  this.renderingHints.render = true;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return event.$menuItem;
  }.bind(this);
};

scout.inherits(scout.SubCloseKeyStroke, scout.MenuNavigationExecKeyStroke);

scout.SubCloseKeyStroke.prototype._accept = function(event) {
  var accepted = scout.SubCloseKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }

  var menuItems = scout.menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass + '.expanded');
  if (menuItems.$all.length > 0) {
    event.$menuItem = menuItems.$all;
    return true;
  }
  return false;
};

scout.SubCloseKeyStroke.prototype.handle = function(event) {
  if (event.$menuItem) {
    event.$menuItem.data('widget').doAction();
  }
};

/**
 * MenuExecByNumberKeyStroke
 */
scout.MenuExecByNumberKeyStroke = function(popup, menuItemClass) {
  scout.MenuExecByNumberKeyStroke.parent.call(this, popup, menuItemClass);
  this._menuItemClass = menuItemClass;
  this.which = [scout.keys[1], scout.keys[2], scout.keys[3], scout.keys[4], scout.keys[5], scout.keys[6], scout.keys[7], scout.keys[8], scout.keys[9]];
  this.renderingHints.render = true;
  this.renderingHints.hAlign = scout.hAlign.RIGHT;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return event.$menuItem;
  }.bind(this);
};
scout.inherits(scout.MenuExecByNumberKeyStroke, scout.MenuNavigationExecKeyStroke);

scout.MenuExecByNumberKeyStroke.prototype._accept = function(event) {
  var accepted = scout.MenuExecByNumberKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }

  var menuItems = scout.menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass);
  var index = scout.codesToKeys[event.which];
  event.$menuItem = menuItems.$allVisible.eq(index - 1);
  if (event.$menuItem) {
    return true;
  }
  return false;
};

scout.MenuExecByNumberKeyStroke.prototype.handle = function(event) {
  if (event.$menuItem) {
    event.$menuItem.data('widget').doAction();
  }
};
