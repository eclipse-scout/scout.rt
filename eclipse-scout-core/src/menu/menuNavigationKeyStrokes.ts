/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ContextMenuPopup, KeyStrokeContext, MenuExecByNumberKeyStroke, MenuNavigationDownKeyStroke, MenuNavigationExecKeyStroke, MenuNavigationUpKeyStroke, SubCloseKeyStroke} from '../index';

export const menuNavigationKeyStrokes = {
  registerKeyStrokes(keyStrokeContext: KeyStrokeContext, popup: ContextMenuPopup, menuItemClass: string) {
    keyStrokeContext.registerKeyStrokes([
      new MenuNavigationUpKeyStroke(popup, menuItemClass),
      new MenuNavigationDownKeyStroke(popup, menuItemClass),
      new MenuNavigationExecKeyStroke(popup, menuItemClass),
      new MenuExecByNumberKeyStroke(popup, menuItemClass),
      new SubCloseKeyStroke(popup, menuItemClass)
    ]);
  },

  /** @internal */
  _findMenuItems(popup: ContextMenuPopup, menuItemClass: string): { $all: JQuery; $allVisible: JQuery; $selected: JQuery } {
    return {
      $all: popup.$body.find('.' + menuItemClass),
      $allVisible: popup.$body.find('.' + menuItemClass + ':visible'),
      $selected: popup.$body.find('.' + menuItemClass + '.selected')
    };
  }
};
