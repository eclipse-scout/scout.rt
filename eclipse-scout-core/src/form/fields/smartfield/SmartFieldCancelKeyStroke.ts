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
