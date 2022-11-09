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
import {CellEditorPopup, keys, KeyStroke, ScoutKeyboardEvent} from '../../index';
import KeyboardEventBase = JQuery.KeyboardEventBase;

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

  override handle(event: KeyboardEventBase<HTMLElement, undefined, HTMLElement, HTMLElement>) {
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
