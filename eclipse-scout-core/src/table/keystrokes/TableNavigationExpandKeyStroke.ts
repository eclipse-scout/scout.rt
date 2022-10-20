/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractTableNavigationKeyStroke, keys} from '../../index';

export default class TableNavigationExpandKeyStroke extends AbstractTableNavigationKeyStroke {

  constructor(table) {
    super(table);
    this.field = table;
    this.which = [keys.ADD, keys.RIGHT];
    this.renderingHints.text = '+';
    this.renderingHints.$drawingArea = ($drawingArea, event) => {
      let row = this.field.selectedRows[0];
      if (row) {
        return row.$row;
      }
    };
  }

  _accept(event) {
    let accepted = super._accept(event),
      selectedRow = this.field.selectedRows[0];
    if (!accepted) {
      return false;
    }
    if (!selectedRow) {
      return false;
    }
    return selectedRow._expandable;
  }

  handle(event) {
    let table = this.field,
      selectedRow = this.field.selectedRows[0],
      visibleChildRows;
    if (!selectedRow) {
      return;
    }
    if (selectedRow._expandable) {
      if (selectedRow.expanded) {
        // select first child
        visibleChildRows = this.field.visibleChildRows(selectedRow);
        table.selectRow(visibleChildRows[0]);
        table.selectionHandler.lastActionRow = visibleChildRows[0];
      } else {
        // expand
        table.expandRow(selectedRow);
      }
      if (!table.isFocused()) {
        table.focus();
      }
    }
  }
}
