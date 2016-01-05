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
      if(this.field.updateNextToSelected){
        this.field.updateNextToSelected(this._menuItemClass, $newItem);
      }
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
    scout.menuNavigationKeyStrokes._changeSelection.call(this, menuItems.$selected, menuItems.$selected.prevAll(':visible').first());
  } else {
    scout.menuNavigationKeyStrokes._changeSelection.call(this, menuItems.$selected, menuItems.$allVisible.last());
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
    scout.menuNavigationKeyStrokes._changeSelection.call(this, menuItems.$selected, menuItems.$selected.nextAll(':visible').first());
  } else {
    scout.menuNavigationKeyStrokes._changeSelection.call(this, menuItems.$selected, menuItems.$allVisible.first());
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
  this._simulateLeftClickOnItems(scout.menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass).$selected);
};

scout.MenuNavigationExecKeyStroke.prototype._accept = function(event) {
  var accepted = scout.MenuNavigationExecKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted || this.field.bodyAnimating) {
    return false;
  }
  return accepted;
};

scout.MenuNavigationExecKeyStroke.prototype._simulateLeftClickOnItems = function($menuItems) {
  ['mousedown', 'mouseup', 'click'].forEach(function(eventType) {
    $menuItems.trigger({
      type: eventType,
      which: 1
    });
  }); // simulate left-mouse click (full click event sequence in order, see scout.Menu.prototype._onMouseEvent)
};

/**
 * SubCloseKeyStroke
 */
scout.SubCloseKeyStroke = function(popup, menuItemClass) {
  scout.SubCloseKeyStroke.parent.call(this, popup, menuItemClass);
  this._menuItemClass = menuItemClass;
  this.field = popup;
  this.which = [scout.keys.BACKSPACE];
  this.renderingHints.render = true;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return event._$element;
  }.bind(this);
};

scout.inherits(scout.SubCloseKeyStroke, scout.MenuNavigationExecKeyStroke);

scout.SubCloseKeyStroke.prototype._accept = function(event) {
  var accepted = scout.MenuExecByNumberKeyStroke.parent.prototype._accept.call(this, event);
  if (!accepted) {
    return false;
  }

  var menuItems = scout.menuNavigationKeyStrokes._findMenuItems(this.field, this._menuItemClass + '.expanded');

  if (menuItems.$all.length > 0) {
    event._$element = menuItems.$all;
    return true;
  }
  return false;
};

scout.SubCloseKeyStroke.prototype.handle = function(event) {
  if (event._$element) {
    this._simulateLeftClickOnItems(event._$element);
  }
};

/**
 * MenuExecByNumberKeyStroke
 */
scout.MenuExecByNumberKeyStroke = function(popup, menuItemClass) {
  scout.MenuExecByNumberKeyStroke.parent.call(this, popup, menuItemClass);
  this._menuItemClass = menuItemClass;
  this.field = popup;
  this.which = [scout.keys[1], scout.keys[2], scout.keys[3], scout.keys[4], scout.keys[5], scout.keys[6], scout.keys[7], scout.keys[8], scout.keys[9]];
  this.renderingHints.render = true;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    return event._$element;
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
  event._$element = menuItems.$allVisible.eq(index - 1);

  if (event._$element) {
    return true;
  }
  return false;
};

scout.MenuExecByNumberKeyStroke.prototype.handle = function(event) {
  if (event._$element) {
    this._simulateLeftClickOnItems(event._$element);
  }
};
