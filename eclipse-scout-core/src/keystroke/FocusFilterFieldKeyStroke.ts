/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Key, keys, KeyStroke, scout, ScoutKeyboardEvent, Widget} from '../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

/**
 * Keystroke to move the cursor into filter field.
 *
 * Hint: This keystroke is not implemented as RangeKeyStroke.js because:
 *       a) the accepted keys are not rendered on F1, but a condensed 'a-z' instead;
 *       b) there is no need to evaluate a concrete key's propagation status when being rendered (because of (a))
 *
 */
export class FocusFilterFieldKeyStroke extends KeyStroke {
  virtualKeyStrokeWhich: string;

  constructor(field: Widget) {
    super();
    this.field = field;
    this.renderingHints.$drawingArea = ($drawingArea: JQuery, event: ScoutKeyboardEvent & { _$filterInput?: JQuery<HTMLInputElement> }) => event._$filterInput;
    this.virtualKeyStrokeWhich = 'a-Z;a-z;0-9';
    this.preventDefault = false; // false so that the key is inserted into the search field.
    this.keyStrokeMode = KeyStroke.Mode.DOWN;
    this.stopPropagation = true;
    this.inheritAccessibility = false;
  }

  /**
   * @override KeyStroke.js
   */
  protected override _accept(event: ScoutKeyboardEvent & { _$filterInput?: JQuery<HTMLInputElement> }): boolean {
    if (!this._isKeyStrokeInRange(event)) {
      return false;
    }

    let $filterInput = this.field.$container.data('filter-field') as JQuery<HTMLInputElement>;
    if (!$filterInput || !$filterInput.length || !$filterInput.is(':focusable')) {
      return false;
    }

    let $activeElement = this.field.$container.activeElement();
    if ($activeElement[0] !== $filterInput[0]) {
      event._$filterInput = $filterInput;
      this._isKeyStrokeInRange(event);
      return true;
    }
    return false;
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement> & { _$filterInput?: JQuery<HTMLInputElement> }) {
    let $filterInput = event._$filterInput;

    // Focus the field and move cursor to the end.
    if (this.field.session.focusManager.requestFocus($filterInput)) {
      $filterInput.focus();

      let length = scout.nvl($filterInput.val(), '').length;
      $filterInput[0].setSelectionRange(length, length);
    }
  }

  /**
   * Returns a virtual key to represent this keystroke.
   */
  override keys(): Key[] {
    // @ts-expect-error
    return [new Key(this, this.virtualKeyStrokeWhich)];
  }

  override renderKeyBox($drawingArea: JQuery, event: ScoutKeyboardEvent & { _$filterInput?: JQuery<HTMLInputElement> }): JQuery {
    let $filterInput = event._$filterInput;
    let filterInputPosition = $filterInput.position();
    let left = filterInputPosition.left + $filterInput.cssMarginLeft() + 4;
    $filterInput.beforeDiv('key-box char', 'a - z')
      .toggleClass('disabled', !this.enabledByFilter)
      .cssLeft(left);
    return $filterInput.parent();
  }

  protected _isKeyStrokeInRange(event: ScoutKeyboardEvent): boolean {
    // @ts-expect-error
    if (event.which === this.virtualKeyStrokeWhich) {
      return true; // the event has this keystroke's 'virtual which part' in case it is rendered.
    }

    if (event.altKey || event.ctrlKey) {
      return false;
    }
    return (event.which >= keys.A && event.which <= keys.Z) ||
      (event.which >= keys['0'] && event.which <= keys['9']) ||
      (event.which >= keys.NUMPAD_0 && event.which <= keys.NUMPAD_9);
  }
}
