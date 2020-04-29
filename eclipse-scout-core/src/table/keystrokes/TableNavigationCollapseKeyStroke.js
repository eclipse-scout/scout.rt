/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractTableNavigationKeyStroke, keys} from '../../index';

export default class TableNavigationCollapseKeyStroke extends AbstractTableNavigationKeyStroke {

  constructor(table) {
    super(table);
    this.field = table;
    this.which = [keys.SUBTRACT, keys.LEFT];
    this.renderingHints.text = '-';
    this.renderingHints.$drawingArea = function($drawingArea, event) {
      let row = this.field.selectedRows[0];
      if (row) {
        return row.$row;
      }
    }.bind(this);
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
    if (selectedRow.expanded && selectedRow._expandable) {
      // collapse
      return true;
    }

    return !!selectedRow.parentRow;
  }

  handle(event) {
    let table = this.field,
      selectedRow = this.field.selectedRows[0];
    if (!selectedRow) {
      return;
    }
    if (selectedRow.expanded && selectedRow._expandable) {
      table.collapseRow(selectedRow);
    } else if (selectedRow.parentRow) {
      table.selectRow(selectedRow.parentRow);
      table.selectionHandler.lastActionRow = selectedRow.parentRow;
    }
  }
}
