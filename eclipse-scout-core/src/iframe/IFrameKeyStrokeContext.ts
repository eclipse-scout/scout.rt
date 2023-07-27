/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {InputFieldKeyStrokeContext, keys, KeyStrokeContext, KeyStrokeMode, keyStrokeModifier, PropagatedEvent, scout, ScoutKeyboardEvent, VirtualKeyStrokeEvent} from '../index';
import $ from 'jquery';

/**
 * Makes sure that certain keys are not propagated to the iframe container because they are needed by the elements inside the iframe
 * and must not trigger a keystroke outside the iframe.
 */
export class IFrameKeyStrokeContext extends KeyStrokeContext {

  protected override _applyPropagationFlags(event: ScoutKeyboardEvent) {
    if (event.which === keys.TAB) {
      // Don't propagate TAB keystrokes otherwise it would break tabbing inside the document.
      event.stopPropagation();
      return;
    }

    let $target = $(this._resolveTarget(event));

    // Don't propagate enter and space if a button is focused inside the iframe
    let inputKeyStrokeContext = new InputFieldKeyStrokeContext();
    if (inputKeyStrokeContext.isButton($target)) {
      let modifierBitMask = keyStrokeModifier.toModifierBitMask(event);
      if (modifierBitMask === keyStrokeModifier.NONE && scout.isOneOf(event.which, [keys.ENTER, keys.SPACE])) {
        event.stopPropagation();
      }
      return;
    }

    // Don't propagate navigation keys if an input or textarea is focused inside the iframe
    if (inputKeyStrokeContext.isInput($target)) {
      let vrEvent = new VirtualKeyStrokeEvent(event.which, event.ctrlKey, event.altKey, event.shiftKey, event.type as KeyStrokeMode, $target[0]);
      inputKeyStrokeContext.setMultiline($target.is('textarea'));
      inputKeyStrokeContext.toggleStopPropagationKeys(keyStrokeModifier.NONE, [keys.ENTER, keys.SPACE], $target.is('textarea'));
      inputKeyStrokeContext.accept(vrEvent);
      if (vrEvent.isPropagationStopped()) {
        event.stopPropagation();
      }
    }
  }

  /**
   * @returns the actual target that triggered the event inside the iframe
   */
  protected _resolveTarget(event: ScoutKeyboardEvent): HTMLElement {
    return (event.originalEvent as KeyboardEvent & PropagatedEvent)?.sourceEvent?.target as HTMLElement;
  }
}
