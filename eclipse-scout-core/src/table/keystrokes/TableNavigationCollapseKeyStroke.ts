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
import {AbstractTableNavigationKeyStroke, keys, ScoutKeyboardEvent, Table} from '../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

export default class TableNavigationCollapseKeyStroke extends AbstractTableNavigationKeyStroke {

  constructor(table: Table) {
    super(table);
    this.which = [keys.SUBTRACT, keys.LEFT];
    this.renderingHints.text = '-';
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
    // @ts-ignore
    if (selectedRow.expanded && selectedRow._expandable) {
      // collapse
      return true;
    }

    return !!selectedRow.parentRow;
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    let selectedRow = this.field.selectedRows[0];
    if (!selectedRow) {
      return;
    }
    let table = this.field;
    let focus = false;
    // @ts-ignore
    if (selectedRow.expanded && selectedRow._expandable) {
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
