/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {fields, FormField, FormFieldStyle, widgets} from '../../index';

export abstract class CompositeField extends FormField {
  /**
   * @returns an array of child-fields.
   */
  abstract getFields(): FormField[];

  /**
   * Sets the given fieldStyle recursively on all fields of the composite field.
   */
  override setFieldStyle(fieldStyle: FormFieldStyle) {
    this.getFields().forEach(field => field.setFieldStyle(fieldStyle));
    super.setFieldStyle(fieldStyle);
  }

  override activate() {
    fields.activateFirstField(this, this.getFields());
  }

  override getFocusableElement(): HTMLElement | JQuery {
    let field = widgets.findFirstFocusableWidget(this.getFields(), this);
    if (field) {
      return field.getFocusableElement();
    }
    return null;
  }
}
