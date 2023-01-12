/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, KeyStroke, ScoutKeyboardEvent, Table, TableCellPosition} from '../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export class TableStartCellEditKeyStroke extends KeyStroke {
  declare field: Table;

  constructor(table: Table) {
    super();
    this.field = table;
    this.ctrl = true;
    this.which = [keys.ENTER];
    this.stopPropagation = true;
    this.renderingHints.$drawingArea = ($drawingArea, event: ScoutKeyboardEvent & { _editPosition?: TableCellPosition }) => {
      let editPosition = event._editPosition,
        columnIndex = this.field.visibleColumns().indexOf(editPosition.column);
      if (columnIndex === 0) {
        // Other key strokes like PageDown, Home etc. are displayed in the row -> make sure the cell edit key stroke will be displayed next to the other ones
        return editPosition.row.$row;
      }
      return this.field.$cell(columnIndex, editPosition.row.$row);
    };
  }

  protected override _accept(event: ScoutKeyboardEvent & { _editPosition?: TableCellPosition }): boolean {
    let accepted = super._accept(event);
    if (!accepted) {
      return false;
    }

    if (this.field.cellEditorPopup) {
      // Already open
      return false;
    }

    let selectedRows = this.field.selectedRows;
    if (!selectedRows.length) {
      return false;
    }

    let position = this.field.nextEditableCellPosForRow(0, selectedRows[0]);
    if (position) {
      event._editPosition = position;
      return true;
    }
    return false;
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement> & { _editPosition?: TableCellPosition }) {
    let editPosition = event._editPosition;
    this.field.prepareCellEdit(editPosition.column, editPosition.row, true);
  }
}
