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
import {KeyStroke} from '../../index';
import {keys} from '../../index';

export default class TableStartCellEditKeyStroke extends KeyStroke {

constructor(table) {
  super();
  this.field = table;
  this.ctrl = true;
  this.which = [keys.ENTER];
  this.stopPropagation = true;
  this.renderingHints.$drawingArea = function($drawingArea, event) {
    var editPosition = event._editPosition,
      columnIndex = this.field.visibleColumns().indexOf(editPosition.column);
    if (columnIndex === 0) {
      // Other key strokes like PageDown, Home etc. are displayed in the row -> make sure the cell edit key stroke will be displayed next to the other ones
      return editPosition.row.$row;
    }
    return this.field.$cell(columnIndex, editPosition.row.$row);
  }.bind(this);
}


_accept(event) {
  var accepted = super._accept( event);
  if (!accepted) {
    return false;
  }

  if (this.field.cellEditorPopup) {
    // Already open
    return false;
  }

  var selectedRows = this.field.selectedRows;
  if (!selectedRows.length) {
    return false;
  }

  var position = this.field.nextEditableCellPosForRow(0, selectedRows[0]);
  if (position) {
    event._editPosition = position;
    return true;
  } else {
    return false;
  }
}

handle(event) {
  var editPosition = event._editPosition;
  this.field.prepareCellEdit(editPosition.column, editPosition.row, true);
}
}
