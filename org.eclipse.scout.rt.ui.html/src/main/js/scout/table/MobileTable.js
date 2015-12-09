/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.MobileTable = function() {
  scout.MobileTable.parent.call(this);
  this.selectionHandler.mouseMoveSelectionEnabled = false;
  this._headerColumns = [];
};
scout.inherits(scout.MobileTable, scout.Table);

/**
 * @override
 */
scout.MobileTable.prototype.init = function(model, session, register) {
  // FIXME cgu: should be done by server, or should we add gui only property to control it? model may set it to true at any time later
  model.headerVisible = false;
  scout.MobileTable.parent.prototype.init.call(this, model, session, register);
};

/**
 * @override
 */
scout.MobileTable.prototype._renderRows = function(rows, startRowIndex) {
  this._headerColumns = this._computeHeaderColumns();
  scout.MobileTable.parent.prototype._renderRows.call(this, rows, startRowIndex);
};

/**
 * @override
 */
scout.MobileTable.prototype._buildRowDiv = function(row, rowSelected, previousRowSelected, followingRowSelected) {
  //TODO [5.2] nbu: selection border
  var rowClass,
    cellContent = '',
    columns = this.columns,
    numColumnsUsed = 0,
    column, value, headerText = '';

  for (var c = 0; c < row.cells.length; c++) {
    column = this.columns[c];
    value = this.cellText(column, row);

    if (c === 0) {
      cellContent += '<p>';
    }

    if (this._headerColumns.indexOf(column) >= 0) {
      headerText += value;
      numColumnsUsed++;
    } else {
      if (this._isColumnNameNecessary(columns[c])) {
        cellContent += columns[c].text + ': ';
      }
      cellContent += value;
      numColumnsUsed++;
      if (c < row.cells.length - 1) {
        cellContent += '<br/>';
      }
    }

    if (c === row.cells.length - 1) {
      cellContent += '</p>';
    }
  }
  if (headerText) {
    cellContent = '<h3>' + headerText + '</h3>' + cellContent;
  }

  rowClass = 'table-row ';
  if (numColumnsUsed === 1) {
    rowClass += 'table-row-single ';
  }
  if (this.selectedRows.indexOf(row) > -1) {
    rowClass += 'selected ';
  }

  return '<div id="' + row.id + '" class="' + rowClass + '">' + cellContent + '</div>';
};

scout.MobileTable.prototype._computeHeaderColumns = function() {
  var columns = this.columns,
    column,
    headerColumns = [],
    i;

  for (i = 0; i < columns.length; i++) {
    column = columns[i];

    if (column.summary) {
      headerColumns.push(column);
    }
  }

  if (headerColumns.length > 0) {
    return headerColumns;
  }

  for (i = 0; i < columns.length; i++) {
    column = columns[i];

    if (column.visible) { // FIXME cgu: also check for other criterias (checkboxcolum, see AbstractRowSummaryColumn);
      headerColumns.push(column);
      return headerColumns;
    }
  }

  return headerColumns;
};

scout.MobileTable.prototype._isColumnNameNecessary = function(column) {
  return true;
};
