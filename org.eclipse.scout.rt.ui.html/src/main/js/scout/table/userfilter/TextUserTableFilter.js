scout.TextUserTableFilter = function(table, column) {
  scout.TextUserTableFilter.parent.call(this, table);
  this.filterType = scout.TextUserTableFilter.Type;
};
scout.inherits(scout.TextUserTableFilter, scout.UserTableFilter);

scout.TextUserTableFilter.Type = 'text';

scout.TextUserTableFilter.prototype.createAddFilterEventData = function() {
  var data = scout.ColumnUserTableFilter.parent.prototype.createAddFilterEventData.call(this);
  return $.extend(data, {
    text: this.text
  });
};

scout.TextUserTableFilter.prototype.createLabel = function() {
  return this.text;
};

scout.TextUserTableFilter.prototype.accept = function($row) {
  var rowText = $row.text().toLowerCase();
  return rowText.indexOf(this.text) > -1;
};
