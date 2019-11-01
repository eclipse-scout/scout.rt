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
import {ProposalChooser} from '../../../index';
import {Column} from '../../../index';
import {lookupField} from '../../../index';
import {scout} from '../../../index';
import {objects} from '../../../index';
import {arrays} from '../../../index';

export default class TableProposalChooser extends ProposalChooser {

constructor() {
  super();
}


_createModel() {
  var headerVisible = false,
    columns = [],
    descriptors = this.smartField.columnDescriptors;

  if (descriptors) {
    descriptors.forEach(function(descriptor, index) {
      headerVisible = headerVisible || !!descriptor.text;
      columns.push(this._createColumnForDescriptor(descriptor));
    }, this);
  } else {
    columns.push(this._createColumn());
  }

  var table = this._createTable(columns, headerVisible);
  table.on('rowClick', this._onRowClick.bind(this));

  return table;
}

_createColumn() {
  return scout.create('Column', {
    session: this.session,
    width: Column.NARROW_MIN_WIDTH,
    horizontalAlignment: this.smartField.gridData.horizontalAlignment
  });
}

_createColumnForDescriptor(descriptor) {
  var width = Column.NARROW_MIN_WIDTH;
  if (descriptor.width && descriptor.width > 0) { // 0 = default
    width = descriptor.width;
  }
  var column = scout.create('Column', {
    session: this.session,
    text: descriptor.text,
    cssClass: scout.nvl(descriptor.cssClass, null),
    width: width, // needs to be passed here to make sure initialWidth is also set, if set using setWidth() autoResizeColumn won't work because initalWidth would still be NARROW_MIN_WIDTH
    autoOptimizeWidth: scout.nvl(descriptor.autoOptimizeWidth, false),
    fixedWidth: scout.nvl(descriptor.fixedWidth, false),
    fixedPosition: scout.nvl(descriptor.fixedPosition, false),
    horizontalAlignment: scout.nvl(descriptor.horizontalAlignment, this.smartField.gridData.horizontalAlignment),
    visible: scout.nvl(descriptor.visible, true),
    htmlEnabled: scout.nvl(descriptor.htmlEnabled, false)
  });
  return column;
}

_createTable(columns, headerVisible) {
  return scout.create('Table', {
    parent: this,
    headerVisible: headerVisible,
    autoResizeColumns: true,
    multiSelect: false,
    multilineText: true,
    scrollToSelection: true,
    columns: columns,
    headerMenusEnabled: false
  });
}

_onRowClick(event) {
  var row = event.row;
  if (!row || !row.enabled) {
    return;
  }
  this.setBusy(true);
  this.triggerLookupRowSelected(row);
}

selectedRow() {
  return this.model.selectedRow();
}

setLookupResult(result) {
  var
    tableRows = [],
    lookupRows = result.lookupRows,
    multipleColumns = !!this.smartField.columnDescriptors;

  this.model.deleteAllRows();
  lookupRows.forEach(function(lookupRow) {
    tableRows.push(this._createTableRow(lookupRow, multipleColumns));
  }, this);
  this.model.insertRows(tableRows);

  this._selectProposal(result, tableRows);
}

trySelectCurrentValue() {
  var currentValue = this.smartField.getValueForSelection();
  if (objects.isNullOrUndefined(currentValue)) {
    return;
  }
  var tableRow = arrays.find(this.model.rows, function(row) {
    return row.lookupRow.key === currentValue;
  });
  if (tableRow) {
    this.model.selectRow(tableRow);
  }
}

selectFirstLookupRow() {
  if (this.model.rows.length) {
    this.model.selectRow(this.model.rows[0]);
  }
}

clearSelection() {
  this.model.deselectAll();
}

clearLookupRows() {
  this.model.removeAllRows();
}

/**
 * Creates a table-row for the given lookup-row.
 *
 * @returns {object} table-row model
 */
_createTableRow(lookupRow, multipleColumns) {
  var row = lookupField.createTableRow(lookupRow, multipleColumns);
  if (multipleColumns) {
    arrays.pushAll(row.cells, this._transformTableRowData(lookupRow, lookupRow.additionalTableRowData));
  }
  return row;
}

_renderModel() {
  this.model.setVirtual(this.smartField.virtual());
  this.model.render();

  // Make sure table never gets the focus, but looks focused
  this.model.$container.setTabbable(false);
  this.model.$container.addClass('focused');
}

getSelectedLookupRow() {
  var selectedRow = this.model.selectedRow();
  if (!selectedRow) {
    return null;
  }
  return selectedRow.lookupRow;
}

/**
 * Takes the TableRowData bean and the infos provided by the column descriptors to create an
 * array of additional values in the correct order, as defined by the descriptors.
 */
_transformTableRowData(lookupRow, tableRowData) {
  var descriptors = this.smartField.columnDescriptors;
  var cells = [];
  descriptors.forEach(function(desc) {
    cells.push(lookupField.createTableCell(lookupRow, desc, tableRowData));
  });
  return cells;
}
}
