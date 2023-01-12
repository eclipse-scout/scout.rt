/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormFieldLayout, TagField} from '../../../index';

export class TagFieldLayout extends FormFieldLayout {
  declare formField: TagField;

  constructor(field: TagField) {
    super(field);
  }

  protected override _$elementForIconLayout(): JQuery {
    // The field container has the border and not the input ($field) element
    return this.formField.$fieldContainer;
  }
}
