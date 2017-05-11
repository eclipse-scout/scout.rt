scout.TableProposalChooser2 = function() {
  scout.TableProposalChooser2.parent.call(this);
};
scout.inherits(scout.TableProposalChooser2, scout.ProposalChooser2);

scout.TableProposalChooser2.prototype._createModel = function() {
  var headerVisible, column,
    columns = [],
    descriptors = this._smartField().columnDescriptors,
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
    columns: columns
  });

  table.on('rowClicked', this._triggerLookupRowSelected.bind(this));

  return table;
};

scout.TableProposalChooser2.prototype._triggerLookupRowSelected = function(event) {
  if (!event.row.enabled) {
    return;
  }
  this.trigger('lookupRowSelected', {
    lookupRow: this.getSelectedLookupRow()
  });
};

scout.TableProposalChooser2.prototype.setLookupRows = function(lookupRows) {
  var tableRows = [],
    multipleColumns = !!this._smartField().columnDescriptors;

  this.model.deleteAllRows();
  lookupRows.forEach(function(lookupRow) {
    tableRows.push(this._createTableRow(lookupRow, multipleColumns));
  }, this);
  this.model.insertRows(tableRows);
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
  this.model.setVirtual(this._smartField().virtual());
  this.model.render();

  // Make sure table never gets the focus, but looks focused
  this.model.$container.setTabbable(false);
  this.model.$container.addClass('focused');
};

scout.TableProposalChooser2.prototype.getSelectedLookupRow = function() {
  var selectedRow = this.model.selectedRows[0];
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
  var descriptors = this._smartField().columnDescriptors;
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




