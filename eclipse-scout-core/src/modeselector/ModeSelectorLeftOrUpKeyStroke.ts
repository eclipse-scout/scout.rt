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

export class ModeSelectorLeftOrUpKeyStroke extends KeyStroke {
  declare field: ModeSelector<any>;

  constructor(modeSelector: ModeSelector<any>) {
    super();
    this.field = modeSelector;
    this.which = [keys.LEFT, keys.UP];
    this.renderingHints.render = false;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    let targetMode,
      focusedButton = $(event.target).data('mode');

    // continuously shift the target mode through the selectable modes to the right,
    // until the next mode is the mode that received the keystroke.
    // Then select that target mode.
    this.field.modes.some(mode => {
      if (targetMode && mode === focusedButton) {
        targetMode.setSelected(true);
        targetMode.focus();
        targetMode.$container.addClass('keyboard-navigation');
        return true;
      }
      if (mode.enabledComputed && mode.visible) {
        targetMode = mode;
      }
      return false;
    });
  }
}
