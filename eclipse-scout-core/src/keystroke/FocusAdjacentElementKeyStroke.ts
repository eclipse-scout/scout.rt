/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, Session, Widget} from '../index';

export class FocusAdjacentElementKeyStroke extends KeyStroke {
  session: Session;

  constructor(session: Session, field: Widget) {
    super();
    this.session = session;
    this.field = field;
    this.which = [keys.LEFT, keys.RIGHT];
    this.renderingHints.render = false;
    this.stopPropagation = true;
    this.keyStrokeMode = KeyStroke.Mode.DOWN;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    let activeElement = this.field.$container.activeElement(true),
      $focusableElements = this.field.$container.find(':focusable');

    switch (event.which) { // NOSONAR
      case keys.RIGHT:
        this._handleNext(activeElement, $focusableElements);
        break;
      case keys.LEFT:
        this._handlePrevious(activeElement, $focusableElements);
        break;
    }
  }

  protected _handleNext(activeElement: HTMLElement, $focusableElements: JQuery) {
    let $newFocusElement;
    if (activeElement === $focusableElements.last()[0]) {
      $newFocusElement = $focusableElements.first();
    } else {
      $newFocusElement = $($focusableElements[$focusableElements.index(activeElement) + 1]);
    }
    this.session.focusManager.requestFocus($newFocusElement);
    $newFocusElement.addClass('keyboard-navigation');
  }

  protected _handlePrevious(activeElement: HTMLElement, $focusableElements: JQuery) {
    let $newFocusElement;
    if (activeElement === $focusableElements.first()[0]) {
      $newFocusElement = $focusableElements.last();
    } else {
      $newFocusElement = $($focusableElements[$focusableElements.index(activeElement) - 1]);
    }
    this.session.focusManager.requestFocus($newFocusElement);
    $newFocusElement.addClass('keyboard-navigation');
  }
}
