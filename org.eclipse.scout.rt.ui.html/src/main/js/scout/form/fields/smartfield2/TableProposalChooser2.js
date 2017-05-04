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
  this.trigger('lookupRowSelected', {
    lookupRow: this.getSelectedLookupRow()
  });
};

scout.TableProposalChooser2.prototype.setLookupRows = function(lookupRows) {
  var tableRows = [];
  this.model.deleteAllRows();
  lookupRows.forEach(function(lookupRow) {
    tableRows.push({
      cells: [lookupRow.text],
      lookupRow: lookupRow
    });
  });
  this.model.insertRows(tableRows);
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

