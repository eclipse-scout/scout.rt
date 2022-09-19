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
import {MenuExecByNumberKeyStroke, MenuNavigationDownKeyStroke, MenuNavigationExecKeyStroke, MenuNavigationUpKeyStroke, SubCloseKeyStroke} from '../index';

export function registerKeyStrokes(keyStrokeContext, popup, menuItemClass) {
  keyStrokeContext.registerKeyStroke([
    new MenuNavigationUpKeyStroke(popup, menuItemClass),
    new MenuNavigationDownKeyStroke(popup, menuItemClass),
    new MenuNavigationExecKeyStroke(popup, menuItemClass),
    new MenuExecByNumberKeyStroke(popup, menuItemClass),
    new SubCloseKeyStroke(popup, menuItemClass)
  ]);
}

export function _findMenuItems(popup, menuItemClass) {
  return {
    $all: popup.$body.find('.' + menuItemClass),
    $allVisible: popup.$body.find('.' + menuItemClass + ':visible'),
    $selected: popup.$body.find('.' + menuItemClass + '.selected')
  };
}

export default {
  registerKeyStrokes,
  _findMenuItems
};
