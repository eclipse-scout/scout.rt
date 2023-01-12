/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FileChooserField, HAlign, keys, KeyStroke} from '../../../index';

export class FileChooserFieldDeleteKeyStroke extends KeyStroke {
  declare field: FileChooserField;

  constructor(field: FileChooserField) {
    super();
    this.field = field;
    this.which = [keys.DELETE];
    this.stopPropagation = true;
    this.renderingHints.offset = 25;
    this.renderingHints.hAlign = HAlign.RIGHT;
    this.renderingHints.$drawingArea = ($drawingArea, event) => this.field.$fieldContainer;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.clear();
  }
}
