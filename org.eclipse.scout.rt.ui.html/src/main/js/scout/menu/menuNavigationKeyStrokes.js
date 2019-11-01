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
import {MenuNavigationUpKeyStroke} from '../index';
import {MenuNavigationExecKeyStroke} from '../index';
import {MenuExecByNumberKeyStroke} from '../index';
import {MenuNavigationDownKeyStroke} from '../index';
import {SubCloseKeyStroke} from '../index';



export function registerKeyStrokes(keyStrokeContext, popup, menuItemClass) {
  keyStrokeContext.registerKeyStroke([
    new MenuNavigationUpKeyStroke(popup, menuItemClass),
    new MenuNavigationDownKeyStroke(popup, menuItemClass),
    new MenuNavigationExecKeyStroke(popup, menuItemClass),
    new MenuExecByNumberKeyStroke(popup, menuItemClass),
    new SubCloseKeyStroke(popup, menuItemClass)
  ]);
}

//private
 export function _findMenuItems(popup, menuItemClass) {
  return {
    $all: popup.$body.find('.' + menuItemClass),
    $allVisible: popup.$body.find('.' + menuItemClass + ':visible'),
    $selected: popup.$body.find('.' + menuItemClass + '.selected')
  };
}

//private
 export function _changeSelection($oldItem, $newItem) {
  if ($newItem.length === 0) {
    // do not change selection
    return;
  } else {
    $newItem.select(true).focus();
    if (field.updateNextToSelected) {
      field.updateNextToSelected(_menuItemClass, $newItem);
    }
  }
  if ($oldItem.length > 0) {
    $oldItem.select(false);
  }
}

export default {
  registerKeyStrokes
};
