/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
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
