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

export default class TableNavigationExpandKeyStroke extends AbstractTableNavigationKeyStroke {

  constructor(table: Table) {
    super(table);
    this.which = [keys.ADD, keys.RIGHT];
    this.renderingHints.text = '+';
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
    return selectedRow._expandable;
  }

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
    let table = this.field,
      selectedRow = table.selectedRows[0];
    if (!selectedRow) {
      return;
    }
    // @ts-ignore
    if (selectedRow._expandable) {
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
