// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.MobileTable = function(session, model) {
  this.base(session, model);

  this.config.contextMenuEnabled = false;

  //FIXME should be done by server, or should we add gui only property to control it? model may set it to true at any time later
  this.model.headerVisible = false;

  this._headerColumns = [];
};
scout.MobileTable.inheritsFrom(scout.Table);

/**
 * @override
 */
scout.MobileTable.prototype._drawData = function(startRow) {
  this._headerColumns = this._computeHeaderColumns();

  this.base.prototype._drawData.call(this, startRow);
};

/**
 * @override
 */
scout.MobileTable.prototype._buildRowDiv = function(row, index) {
  var rowClass = 'table-row ',
    table = this.model,
    column, value, headerText = "";

  if (this.model.selectedRowIds && this.model.selectedRowIds.indexOf(row.id) > -1) {
    rowClass += 'row-selected ';
  }

  var cellContent = "";
  var columns = this.model.columns;
  for (var c = 0; c < row.cells.length; c++) {
    column = this.model.columns[c];
    value = this.getText(c, index);

    if (c === 0) {
      cellContent += "<p>";
    }

    if (this._headerColumns.indexOf(column) >= 0) {
      headerText += value;
    } else {
      if (this._isColumnNameNecessary(columns[c])) {
        cellContent += columns[c].text + ": ";
      }
      cellContent += value;
      if (c < row.cells.length - 1) {
        cellContent += '<br/>';
      }
    }

    if (c === row.cells.length - 1) {
      cellContent += "</p>";
    }
  }
  if (headerText) {
    cellContent = '<h3>' + headerText + '</h3>' + cellContent;
  }

  return '<div id="' + row.id + '" class="' + rowClass + '">' + cellContent + '</div>';
};

scout.MobileTable.prototype._computeHeaderColumns = function() {
  var columns = this.model.columns,
    column,
    headerColumns = [], i;

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

    if (column.visible) {//FIXME also check for other criterias (checkboxcolum, see AbstractRowSummaryColumn);
      headerColumns.push(column);
      return headerColumns;
    }
  }

  return headerColumns;
};

scout.MobileTable.prototype._isColumnNameNecessary = function(column) {
  return true;
};
