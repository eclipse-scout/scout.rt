/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormFieldLayout, graphics, Rectangle, StringField} from '../../../index';

export class StringFieldLayout extends FormFieldLayout {

  constructor(stringField: StringField) {
    super(stringField);
  }

  protected override _layoutClearIcon(formField: StringField, fieldBounds: Rectangle, right: number, top: number) {
    if (formField.$icon && formField.$icon.isVisible()) {
      right += graphics.prefSize(formField.$icon, true).width;
    }
    super._layoutClearIcon(formField, fieldBounds, right, top);
  }
}
