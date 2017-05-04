scout.TableProposalChooser2 = function() {
  scout.TableProposalChooser2.parent.call(this);
};
scout.inherits(scout.TableProposalChooser2, scout.ProposalChooser2);

scout.TableProposalChooser2.prototype._createModel = function() {
  var table = scout.create('Table', {
    parent: this,
    headerVisible: false,
    autoResizeColumns: true,
    multiSelect: false,
    columns: [
      scout.create('Column', {
        index: 0,
        session: this.session
      })
    ]
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
  var tableRows = [];
  this.model.deleteAllRows();
  lookupRows.forEach(function(lookupRow) {
    tableRows.push(this._createTableRow(lookupRow));
  }, this);
  this.model.insertRows(tableRows);
};

/**
 * Creates a table-row for the given lookup-row.
 * @returns {object} table-row model
 */
scout.TableProposalChooser2.prototype._createTableRow = function(lookupRow) {
  var
  cell = scout.create('Cell', {
    text: lookupRow.text
  }),
  row = {
    cells: [cell],
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
  //FIXME [awe] 7.0 - SF2: impl. additionalTableRowData
  if (lookupRow.cssClass) {
    cell.cssClass = lookupRow.cssClass;
  }
  return row;
};

scout.TableProposalChooser2.prototype._renderModel = function() {
  this.model.render(this.$container);

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

scout.TableProposalChooser2.prototype.delegateKeyEvent = function(event) {
  this.model.$container.trigger(event);
};

