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
scout.TableProposalChooser2 = function() {
  scout.TableProposalChooser2.parent.call(this);
};
scout.inherits(scout.TableProposalChooser2, scout.ProposalChooser2);

scout.TableProposalChooser2.prototype._createModel = function() {
  var headerVisible, column,
    columns = [],
    descriptors = this.smartField.columnDescriptors,
    autoResize = true;

  if (descriptors) {
    headerVisible = true;
    descriptors.forEach(function(descriptor, index) {
      column = scout.create('Column', {
        index: index,
        session: this.session,
        text: descriptor.text
      });

      // if at least one of the descriptors defines a width, we set autoResize to false
      if (descriptor.width) {
        autoResize = false;
        column.width = descriptor.width;
      }

      columns.push(column);
    }, this);
  } else {
    headerVisible = false;
    columns.push(scout.create('Column', {
      index: 0,
      session: this.session
    }));
  }

  var table = scout.create('Table', {
    parent: this,
    headerVisible: headerVisible,
    autoResizeColumns: autoResize,
    multiSelect: false,
    scrollToSelection: true,
    columns: columns
  });

  table.on('rowClicked', this._onRowClicked.bind(this));

  return table;
};

scout.TableProposalChooser2.prototype._onRowClicked = function(event) {
  this.setBusy(true);
  this.triggerLookupRowSelected(event.row);
};

scout.TableProposalChooser2.prototype.triggerLookupRowSelected = function(row) {
  row = row || this.model.selectedRow();
  if (!row || !row.enabled) {
    return;
  }
  this.trigger('lookupRowSelected', {
    lookupRow: row.lookupRow
  });
};

scout.TableProposalChooser2.prototype.setLookupResult = function(result) {
  var
    tableRows = [],
    lookupRows = result.lookupRows,
    multipleColumns = !!this.smartField.columnDescriptors;

  this.model.deleteAllRows();
  lookupRows.forEach(function(lookupRow) {
    tableRows.push(this._createTableRow(lookupRow, multipleColumns));
  }, this);
  this.model.insertRows(tableRows);

  if (result.browse) {
    this.trySelectCurrentValue();
  } else if (tableRows.length === 1) {
    this.selectFirstLookupRow();
  }
};

scout.TableProposalChooser2.prototype.trySelectCurrentValue = function() {
  var currentValue = this.smartField.value;
  if (scout.objects.isNullOrUndefined(currentValue)) {
    return;
  }
  var tableRow = this.model.rows.find(function(row) {
    return row.lookupRow.key === currentValue;
  });
  if (tableRow) {
    this.model.selectRow(tableRow);
  }
};

scout.TableProposalChooser2.prototype.selectFirstLookupRow = function() {
  if (this.model.rows.length) {
    this.model.selectRow(this.model.rows[0]);
  }
};

scout.TableProposalChooser2.prototype.clearLookupRows = function() {
  this.model._removeAllRows(); // FIXME [awe] 7.0 - ask C.GU make "public"
};

/**
 * Creates a table-row for the given lookup-row.
 * @returns {object} table-row model
 */
scout.TableProposalChooser2.prototype._createTableRow = function(lookupRow, multipleColumns) {
  var
    cell = scout.create('Cell', {
      text: lookupRow.text
    }),
    cells = [cell],
    row = {
      cells: cells,
      lookupRow: lookupRow
    };

  if (lookupRow.iconId) {
    cell.iconId = lookupRow.iconId;
  }
  if (lookupRow.tooltipText) {
    cell.tooltipText = lookupRow.tooltipText;
  }
  if (lookupRow.backgroundColor) {
    cell.backgroundColor = lookupRow.backgroundColor;
  }
  if (lookupRow.foregroundColor) {
    cell.foregroundColor = lookupRow.foregroundColor;
  }
  if (lookupRow.font) {
    cell.font = lookupRow.font;
  }
  if (lookupRow.enabled === false) {
    row.enabled = false;
  }
  // FIXME [awe] 7.0 - SF2: impl. parentKey mapping / Tree
  if (lookupRow.active === false) {
    row.active = false;
  }
  if (lookupRow.cssClass) {
    cell.cssClass = lookupRow.cssClass;
  }

  if (multipleColumns && lookupRow.additionalTableRowData) {
    scout.arrays.pushAll(cells, this._transformTableRowData(lookupRow.additionalTableRowData));
  }

  return row;
};

scout.TableProposalChooser2.prototype._renderModel = function() {
  this.model.setVirtual(this.smartField.virtual());
  this.model.render();

  // Make sure table never gets the focus, but looks focused
  this.model.$container.setTabbable(false);
  this.model.$container.addClass('focused');
};

scout.TableProposalChooser2.prototype.getSelectedLookupRow = function() {
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
scout.TableProposalChooser2.prototype._transformTableRowData = function(tableRowData) {
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




