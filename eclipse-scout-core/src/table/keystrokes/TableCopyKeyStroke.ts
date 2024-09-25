/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, ScoutKeyboardEvent, Table} from '../../index';

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

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    if (this.field.footer?.$controlContainer?.has(event.target).length) {
      // Allow text copy inside control container (labels, iframes etc.)
      return false;
    }
    return super._accept(event);
  }

  override handle(event: JQuery.KeyboardEventBase) {
    this.field.exportToClipboard();
  }
}
