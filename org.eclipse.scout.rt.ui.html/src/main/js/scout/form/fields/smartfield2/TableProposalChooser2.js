/**
 * This is a wrapper / interface around a table or tree, used to select a lookup row.
 *
 * @param {scout.SmartField2} smartField
 * @param {function} onLookupRowSelected callback to function called when lookup row is selected
 */
scout.TableProposalChooser2 = function(smartField, onLookupRowSelected) {
  this.smartField = smartField;
  this.table = scout.create('Table', {
    parent: smartField,
    headerVisible: false,
    autoResizeColumns: true,
    multiSelect: false,
    columns: [
      scout.create('Column', {
        index: 0,
        session: smartField.session
      })
    ]
  });

  this.table.on('rowClicked', function(event) {
    onLookupRowSelected(this.getSelectedLookupRow());
  }.bind(this));
};

scout.TableProposalChooser2.prototype.setLookupRows = function(lookupRows) {
  var tableRows = [];
  this.table.deleteAllRows();
  lookupRows.forEach(function(lookupRow) {
    tableRows.push({
      cells: [lookupRow.text],
      lookupRow: lookupRow
    });
  });
  this.table.insertRows(tableRows);
};

scout.TableProposalChooser2.prototype.render = function($parent) {
  this.table.render($parent);

  // Make sure table never gets the focus, but looks focused
  this.table.$container.setTabbable(false);
  this.table.$container.addClass('focused');
};

scout.TableProposalChooser2.prototype.getSelectedLookupRow = function() {
  var selectedRow = this.table.selectedRows[0];
  if (!selectedRow) {
    return null;
  }
  return selectedRow.lookupRow;
};

scout.TableProposalChooser2.prototype.delegateKeyEvent = function(event) {
  this.table.$container.trigger(event);
};

