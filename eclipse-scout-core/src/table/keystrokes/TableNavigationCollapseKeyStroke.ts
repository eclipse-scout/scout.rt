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

export class TableNavigationCollapseKeyStroke extends AbstractTableNavigationKeyStroke {

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
    if (selectedRow.expanded && selectedRow.expandable) {
      // collapse
      return true;
    }

    return !!selectedRow.parentRow;
  }

  override handle(event: JQuery.KeyboardEventBase) {
    let selectedRow = this.field.selectedRows[0];
    if (!selectedRow) {
      return;
    }
    let table = this.field;
    let focus = false;
    if (selectedRow.expanded && selectedRow.expandable) {
      table.collapseRow(selectedRow);
      focus = true;
    } else if (selectedRow.parentRow) {
      table.selectRow(selectedRow.parentRow);
      table.selectionHandler.lastActionRow = selectedRow.parentRow;
      focus = true;
    }
    if (focus && !table.isFocused()) {
      table.focus();
    }
  }
}
