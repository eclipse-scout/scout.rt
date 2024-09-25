/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {CellEditorPopup, keys, KeyStroke, ScoutKeyboardEvent} from '../../index';

export class CellEditorTabKeyStroke extends KeyStroke {
  declare field: CellEditorPopup<any>;

  constructor(popup: CellEditorPopup<any>) {
    super();
    this.field = popup;
    this.which = [keys.TAB];
    this.shift = undefined; // to tab forward and backward
  }

  protected override _accept(event: ScoutKeyboardEvent): boolean {
    let accepted = super._accept(event);
    return accepted && !this.field.isCompleteCellEditRequested(); // Make sure events (complete, prepare) don't get sent twice since it will lead to exceptions. This may happen if user presses and holds the tab key.
  }

  override handle(event: JQuery.KeyboardEventBase) {
    let backwards = event.shiftKey,
      table = this.field.table,
      column = this.field.column,
      row = this.field.row;

    this.field.completeEdit()
      .then(() => {
        let pos = table.nextEditableCellPos(column, row, backwards);
        if (pos) {
          table.prepareCellEdit(pos.column, pos.row);
        }
      });
  }
}
