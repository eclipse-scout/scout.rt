/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TableProposalChooser = function() {
  scout.TableProposalChooser.parent.call(this);
};
scout.inherits(scout.TableProposalChooser, scout.ProposalChooser);

scout.TableProposalChooser.prototype._createModel = function() {
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
};

scout.TableProposalChooser.prototype._createColumn = function() {
  return scout.create('Column', {
    session: this.session,
    width: scout.Column.NARROW_MIN_WIDTH,
    horizontalAlignment: this.smartField.gridData.horizontalAlignment
  });
};

scout.TableProposalChooser.prototype._createColumnForDescriptor = function(descriptor) {
  var width = scout.Column.NARROW_MIN_WIDTH;
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
    visible: scout.nvl(descriptor.visible, true)
  });
  return column;
};

scout.TableProposalChooser.prototype._createTable = function(columns, headerVisible) {
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
};

scout.TableProposalChooser.prototype._onRowClick = function(event) {
  var row = event.row;
  if (!row || !row.enabled) {
    return;
  }
  this.setBusy(true);
  this.triggerLookupRowSelected(row);
};

scout.TableProposalChooser.prototype.selectedRow = function() {
  return this.model.selectedRow();
};

scout.TableProposalChooser.prototype.setLookupResult = function(result) {
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
};

scout.TableProposalChooser.prototype.trySelectCurrentValue = function() {
  var currentValue = this.smartField.getValueForSelection();
  if (scout.objects.isNullOrUndefined(currentValue)) {
    return;
  }
  var tableRow = scout.arrays.find(this.model.rows, function(row) {
    return row.lookupRow.key === currentValue;
  });
  if (tableRow) {
    this.model.selectRow(tableRow);
  }
};

scout.TableProposalChooser.prototype.selectFirstLookupRow = function() {
  if (this.model.rows.length) {
    this.model.selectRow(this.model.rows[0]);
  }
};

scout.TableProposalChooser.prototype.clearSelection = function() {
  this.model.deselectAll();
};


scout.TableProposalChooser.prototype.clearLookupRows = function() {
  this.model.removeAllRows();
};

/**
 * Creates a table-row for the given lookup-row.
 *
 * @returns {object} table-row model
 */
scout.TableProposalChooser.prototype._createTableRow = function(lookupRow, multipleColumns) {
  var row = scout.lookupField.createTableRow(lookupRow);
  if (multipleColumns && lookupRow.additionalTableRowData) {
    scout.arrays.pushAll(row.cells, this._transformTableRowData(lookupRow.additionalTableRowData));
  }
  return row;
};

scout.TableProposalChooser.prototype._renderModel = function() {
  this.model.setVirtual(this.smartField.virtual());
  this.model.render();

  // Make sure table never gets the focus, but looks focused
  this.model.$container.setTabbable(false);
  this.model.$container.addClass('focused');
};

scout.TableProposalChooser.prototype.getSelectedLookupRow = function() {
  var selectedRow = this.model.selectedRow();
  if (!selectedRow) {
    return null;
  }
  return selectedRow.lookupRow;
};

/**
 * Takes the TableRowData bean and the infos provided by the column descriptors to create an
 * array of additional values in the correct order, as defined by the descriptors.
 */
scout.TableProposalChooser.prototype._transformTableRowData = function(tableRowData) {
  var descriptors = this.smartField.columnDescriptors;
  var cells = [];
  descriptors.forEach(function(desc) {
    if (desc.propertyName) { // default column descriptor (first column) has propertyName null
      cells.push(scout.create('Cell', {
        text: tableRowData[desc.propertyName]
      }));
    }
  });
  return cells;
};
