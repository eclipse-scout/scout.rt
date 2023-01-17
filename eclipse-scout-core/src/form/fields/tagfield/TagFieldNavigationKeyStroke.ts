/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Alignment, keys, KeyStroke, ScoutKeyboardEvent, TagBar, TagFieldKeyStrokeAdapter} from '../../../index';
import $ from 'jquery';

/**
 * @param fieldAdapter acts as an interface so we can use the same key-stroke for TagField and TagBarOverflowPopup.
 *
 */
export class TagFieldNavigationKeyStroke extends KeyStroke {
  fieldAdapter: TagFieldKeyStrokeAdapter;

  constructor(fieldAdapter: TagFieldKeyStrokeAdapter) {
    super();
    this.fieldAdapter = fieldAdapter;
    this.which = [keys.LEFT, keys.RIGHT];
    this.preventDefault = false;
    this.preventInvokeAcceptInputOnActiveValueField = true;
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }
    return this.fieldAdapter.enabled();
  }

  override handle(event: JQuery.KeyboardEventBase) {
    if (event.which === keys.LEFT) {
      this._focusTagElement(-1);
    } else if (event.which === keys.RIGHT) {
      this._focusTagElement(1);
    }
  }

  protected _focusTagElement(direction: Alignment) {
    let UNDEFINED = -1;
    let INPUT = -2;

    // find overflow-icon and all tag-elements
    let $focusTargets = TagBar.findFocusableTagElements(this.fieldAdapter.$container());
    let numTargets = $focusTargets.length;
    if (numTargets === 0) {
      return;
    }

    // check which element has the focus
    let focusIndex = UNDEFINED;
    $focusTargets.each(function(index) {
      let $element = $(this);
      if ($element.hasClass('focused')) {
        focusIndex = index;
      }
    });

    if (focusIndex === UNDEFINED) {
      // no tag-elements focused currently
      if (direction === -1) {
        focusIndex = numTargets - 1;
      }
    } else {
      let nextFocusIndex = focusIndex + direction;
      if (nextFocusIndex >= numTargets) {
        focusIndex = INPUT;
      } else if (nextFocusIndex < 0) {
        focusIndex = UNDEFINED;
      } else {
        TagBar.unfocusTagElement($focusTargets.eq(focusIndex));
        focusIndex = nextFocusIndex;
      }
    }

    if (focusIndex === UNDEFINED) {
      // leave focus untouched
    } else if (focusIndex === INPUT) {
      this.fieldAdapter.focus();
    } else {
      TagBar.focusTagElement($focusTargets.eq(focusIndex));
    }
  }
}
