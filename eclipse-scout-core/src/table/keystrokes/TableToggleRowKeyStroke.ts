/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, ScoutKeyboardEvent, Table} from '../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export class TableToggleRowKeyStroke extends KeyStroke {
  declare field: Table;

  constructor(table: Table) {
    super();
    this.field = table;
    this.which = [keys.SPACE];
    this.stopPropagation = true;
    this.renderingHints.render = false;
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    let accepted = super._accept(event);
    return accepted &&
      this.field.checkable &&
      this.field.selectedRows.length > 0;
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    let selectedRows = this.field.selectedRows.filter(row => row.enabled);
    // Toggle checked state to 'true', except if every row is already checked
    let checked = selectedRows.some(row => !row.checked);
    selectedRows.forEach(row => this.field.checkRow(row, checked));
  }
}
