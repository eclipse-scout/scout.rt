/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, ScoutKeyboardEvent, SmartField} from '../../../index';

/**
 * Closes the popup without accepting the proposal
 */
export class SmartFieldCancelKeyStroke extends KeyStroke {
  declare field: SmartField<any>;

  constructor(field: SmartField<any>) {
    super();
    this.field = field;
    this.which = [keys.ESC];
    this.stopPropagation = true;
    this.preventInvokeAcceptInputOnActiveValueField = true;

    this.renderingHints.$drawingArea = function($drawingArea, event) {
      return this.field.$fieldContainer;
    }.bind(this);
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }
    if (!this.field.popup) {
      return false;
    }
    return true;
  }

  override handle(event: JQuery.KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    this.field.closePopup();
    this.field.resetDisplayText();
  }
}
