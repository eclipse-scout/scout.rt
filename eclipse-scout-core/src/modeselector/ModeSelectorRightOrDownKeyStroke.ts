/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, ModeSelector} from '../index';
import $ from 'jquery';

export class ModeSelectorRightOrDownKeyStroke extends KeyStroke {
  declare field: ModeSelector<any>;

  constructor(modeSelector: ModeSelector<any>) {
    super();
    this.field = modeSelector;
    this.which = [keys.RIGHT, keys.DOWN];
    this.renderingHints.render = false;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    let fieldBefore,
      focusedMode = $(event.target).data('mode');

    // Find the mode that received the keystroke, then find the next selectable mode and select it
    this.field.modes.some(mode => {
      if (fieldBefore && mode.enabledComputed && mode.visible) {
        mode.setSelected(true);
        mode.focus();
        mode.$container.addClass('keyboard-navigation');
        return true;
      }
      // do not check for enabled here, we also want the keystroke to work if the focused mode was disabled dynamically
      if (mode === focusedMode) {
        fieldBefore = mode;
      }
      return false;
    }, this);
  }
}
