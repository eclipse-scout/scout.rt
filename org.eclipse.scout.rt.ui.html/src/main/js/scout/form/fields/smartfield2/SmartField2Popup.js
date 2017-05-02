scout.SmartField2Popup = function() {
  scout.SmartField2Popup.parent.call(this);
  this.lookupRows = [];
  this.animateRemoval = true;
};
scout.inherits(scout.SmartField2Popup, scout.Popup);

scout.SmartField2Popup.prototype._init = function(model) {
  scout.SmartField2Popup.parent.prototype._init.call(this, model);
  this.list = scout.create('Table', {
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
  this.list.on('rowClicked', this._onListRowClicked.bind(this));
};

/**
 * @override
 */
scout.SmartField2Popup.prototype._createLayout = function() {
  return new scout.SmartField2PopupLayout(this, this.list);
};

scout.SmartField2Popup.prototype._render = function($parent) {
  scout.SmartField2Popup.parent.prototype._render.call(this, $parent);
  this.$container.addClass('dropdown-popup');
  this.list.render(this.$container);

  // Make sure table never gets the focus, but looks focused
  this.list.$container.setTabbable(false);
  this.list.$container.addClass('focused');
};

scout.SmartField2Popup.prototype.setLookupRows = function(lookupRows) {
  this.lookupRows = lookupRows;
  this.list.deleteAllRows();

  var rows = [];
  this.lookupRows.forEach(function(lookupRow) {
    var row = {
      cells: [lookupRow.text],
      lookupRow: lookupRow
    };
    rows.push(row);
  }, this);

  this.list.insertRows(rows);
};

scout.SmartField2Popup.prototype.getSelectedLookupRow = function() {
  var selectedRow = this.list.selectedRows[0];
  if (!selectedRow) {
    return null;
  }
  return selectedRow.lookupRow;
};

/**
 * Delegates the key event to the table.
 */
scout.SmartField2Popup.prototype.delegateKeyEvent = function(event) {
  event.originalEvent.smartFieldEvent = true;
  this.list.$container.trigger(event);
};

scout.SmartField2Popup.prototype._onListRowClicked = function(event) {
  this.trigger('select', {
    lookupRow: this.getSelectedLookupRow()
  });
};
