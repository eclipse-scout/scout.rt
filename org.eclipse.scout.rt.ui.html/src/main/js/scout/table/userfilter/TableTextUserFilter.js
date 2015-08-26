scout.TableTextUserFilter = function(table, column) {
  scout.TableTextUserFilter.parent.call(this, table);
  this.filterType = scout.TableTextUserFilter.Type;
};
scout.inherits(scout.TableTextUserFilter, scout.TableUserFilter);

scout.TableTextUserFilter.Type = 'text';

scout.TableTextUserFilter.prototype.createAddFilterEventData = function() {
  var data = scout.ColumnUserFilter.parent.prototype.createAddFilterEventData.call(this);
  return $.extend(data, {
    text: this.text
  });
};

scout.TableTextUserFilter.prototype.createLabel = function() {
  return this.text;
};

scout.TableTextUserFilter.prototype.accept = function($row) {
  var rowText = $row.text().toLowerCase();
  return rowText.indexOf(this.text) > -1;
};
