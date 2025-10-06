/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FocusTabTargetKeyStroke, keys, scout, TabbableCoordinator, Widget} from '../../index';

export class FocusNextTabTargetKeyStroke extends FocusTabTargetKeyStroke {

  constructor(widget: Widget, tabbableCoordinator: TabbableCoordinator) {
    super(widget, tabbableCoordinator);
    if (scout.isOneOf(tabbableCoordinator.orientation, 'horizontal', 'both')) {
      this.which.push(keys.RIGHT);
    }
    if (scout.isOneOf(tabbableCoordinator.orientation, 'vertical', 'both')) {
      this.which.push(keys.DOWN);
    }
  }

  override handle(event: JQuery.KeyboardEventBase) {
    let tabbableItems = this.tabbableCoordinator.items;
    let $focusedItem = this._$getFocusedItem(event);
    let focusNext = false;

    for (const item of tabbableItems) {
      if (focusNext && item.isTabTarget()) {
        this.tabbableCoordinator.setCurrentItem(item);
        item.focus();
        item.$container.addClass('keyboard-navigation');
        break;
      }
      if ($focusedItem.is(item.$container)) {
        focusNext = true;
      }
    }
  }
}
