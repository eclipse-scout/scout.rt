/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, Table} from '../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export class TableCopyKeyStroke extends KeyStroke {
  declare field: Table;

  constructor(table: Table) {
    super();
    this.field = table;
    this.which = [keys.C];
    this.ctrl = true;
    this.renderingHints.render = false;
    this.inheritAccessibility = false;
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    this.field.exportToClipboard();
  }
}
