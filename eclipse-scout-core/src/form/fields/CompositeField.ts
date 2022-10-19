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
import {fields, FormField, TreeVisitResult, widgets} from '../../index';
import {FormFieldStyle} from './FormField';

export default abstract class CompositeField extends FormField {

  /**
   * @returns an array of child-fields.
   */
  abstract getFields(): FormField[];

  override visitFields(visitor: (field: FormField) => TreeVisitResult | void): TreeVisitResult | void {
    let treeVisitResult = super.visitFields(visitor);
    if (treeVisitResult === TreeVisitResult.TERMINATE) {
      return TreeVisitResult.TERMINATE;
    }
    if (treeVisitResult === TreeVisitResult.SKIP_SUBTREE) {
      return TreeVisitResult.CONTINUE;
    }

    let fields = this.getFields();
    for (let i = 0; i < fields.length; i++) {
      let field = fields[i];
      treeVisitResult = field.visitFields(visitor);
      if (treeVisitResult === TreeVisitResult.TERMINATE) {
        return TreeVisitResult.TERMINATE;
      }
    }
  }

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
