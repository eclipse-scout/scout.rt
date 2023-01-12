/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CheckBoxField, HAlign, keys, KeyStroke} from '../../../index';

export class CheckBoxToggleKeyStroke extends KeyStroke {
  declare field: CheckBoxField;

  constructor(checkbox: CheckBoxField) {
    super();
    this.field = checkbox;
    this.which = [keys.SPACE];
    this.stopPropagation = true;
    this.stopImmediatePropagation = true;
    this.renderingHints.hAlign = HAlign.LEFT;
    this.renderingHints.$drawingArea = ($drawingArea, event) => this.field.$fieldContainer;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.toggleChecked();
  }
}
