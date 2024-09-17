/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractTableNavigationKeyStroke, ScoutKeyboardEvent, Table} from '../../index';

export class TableNavigationExpandKeyStroke extends AbstractTableNavigationKeyStroke {

  constructor(table: Table, key: number, displayText: string) {
    super(table);
    this.which = [key];
    this.renderingHints.text = displayText;
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      let row = this.field.selectedRows[0];
      if (row) {
        return row.$row;
      }
    };
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }
    let selectedRow = this.field.selectedRows[0];
    if (!selectedRow) {
      return false;
    }
    return selectedRow.expandable;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    let table = this.field,
      selectedRow = table.selectedRows[0];
    if (!selectedRow) {
      return;
    }
    if (selectedRow.expandable) {
      if (selectedRow.expanded) {
        // select first child
        let visibleChildRows = table.visibleChildRows(selectedRow);
        table.selectRow(visibleChildRows[0]);
        table.selectionHandler.lastActionRow = visibleChildRows[0];
      } else {
        table.expandRow(selectedRow);
      }
      if (!table.isFocused()) {
        table.focus();
      }
    }
  }
}
