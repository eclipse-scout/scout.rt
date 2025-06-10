/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, ValueField} from '../../index';

/**
 * The `AcceptInputKeyStroke` is bound to the ENTER key and calls {@link ValueField.acceptInput} on the active value field.
 *
 * The keystroke manager normally calls {@link ValueField.acceptInput} automatically before any keystroke is executed, unless {@link preventInvokeAcceptInputOnActiveValueField} is set to true.
 * This means, this keystroke only needs to be registered if there is no other keystroke registered and pressing ENTER should accept the input.
 */
export class AcceptInputKeyStroke extends KeyStroke {
  declare field: ValueField<any>;

  constructor(field: ValueField<any>) {
    super();
    this.field = field;
    this.which = [keys.ENTER];
    this.renderingHints.render = false;
    this.preventDefault = false;
    this.preventInvokeAcceptInputOnActiveValueField = true; // There is no need to call acceptInput() twice
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.acceptInput();
  }
}
