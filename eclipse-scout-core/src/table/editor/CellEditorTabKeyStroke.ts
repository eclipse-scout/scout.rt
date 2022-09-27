/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {keys, KeyStroke} from '../../index';

export default class CellEditorTabKeyStroke extends KeyStroke {

  constructor(popup) {
    super();
    this.field = popup;
    this.which = [keys.TAB];
    this.shift = undefined; // to tab forward and backward
  }

  _accept(event) {
    let accepted = super._accept(event);
    return accepted && !this.field.isCompleteCellEditRequested(); // Make sure events (complete, prepare) don't get sent twice since it will lead to exceptions. This may happen if user presses and holds the tab key.
  }

  handle(event) {
    let pos,
      backwards = event.shiftKey,
      table = this.field.table,
      column = this.field.column,
      row = this.field.row;

    this.field.completeEdit()
      .then(() => {
        pos = table.nextEditableCellPos(column, row, backwards);
        if (pos) {
          table.prepareCellEdit(pos.column, pos.row);
        }
      });
  }
}
