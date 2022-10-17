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
import {keys, KeyStroke} from '../../index';
import CellEditorPopup from './CellEditorPopup';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export default class CellEditorCancelEditKeyStroke extends KeyStroke {
  declare field: CellEditorPopup<any>;

  constructor(popup: CellEditorPopup<any>) {
    super();
    this.field = popup;
    this.which = [keys.ESC];
    this.stopPropagation = true;
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    this.field.cancelEdit();
  }
}
