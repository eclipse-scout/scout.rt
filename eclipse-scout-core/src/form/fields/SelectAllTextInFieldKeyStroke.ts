/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormField, keys, KeyStroke} from '../../index';

/**
 * Selects all text in the field when pressing ctrl-a rather than selecting all text of the document.
 */
export class SelectAllTextInFieldKeyStroke extends KeyStroke {
  declare field: FormField;

  constructor(widget: FormField) {
    super();
    this.field = widget;
    this.ctrl = true;
    this.which = [keys.A];
    this.stopPropagation = true;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    let widget = this.field;
    let selection = widget.$container.window(true).getSelection();
    let rangeCount = selection.rangeCount;
    if (rangeCount !== 1) {
      return;
    }
    let range = selection.getRangeAt(0);
    let ancestor = range.commonAncestorContainer as HTMLElement;
    if (widget.$label.isOrHas(ancestor)) {
      // If the cursor is in the label, only select the label
      widget.$label.selectAllText();
    } else if (widget.$field.isOrHas(ancestor)) {
      // If the cursor is in the field, only select the field
      widget.$field.selectAllText();
    } else {
      // Select label and field if the selection intersects with both elements
      widget.$container.selectAllText();
    }
  }
}
