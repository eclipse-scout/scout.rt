/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CellEditorPopup, keys, KeyStroke} from '../../index';

export class CellEditorCancelEditKeyStroke extends KeyStroke {
  declare field: CellEditorPopup<any>;

  constructor(popup: CellEditorPopup<any>) {
    super();
    this.field = popup;
    this.which = [keys.ESC];
    this.stopPropagation = true;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.cancelEdit();
  }
}
