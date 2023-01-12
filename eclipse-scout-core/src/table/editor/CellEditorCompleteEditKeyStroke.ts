/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CellEditorPopup, keys, KeyStroke} from '../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export class CellEditorCompleteEditKeyStroke extends KeyStroke {
  declare field: CellEditorPopup<any>;

  constructor(popup: CellEditorPopup<any>) {
    super();
    this.field = popup;
    this.which = [keys.ENTER];
    this.stopPropagation = true;
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    this.field.completeEdit();
  }
}
