/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
