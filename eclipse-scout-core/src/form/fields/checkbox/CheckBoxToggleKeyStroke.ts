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
import {CheckBoxField, HAlign, keys, KeyStroke} from '../../../index';

export default class CheckBoxToggleKeyStroke extends KeyStroke {
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
