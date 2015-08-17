scout.ColumnUserTableFilter = function() {
  scout.ColumnUserTableFilter.parent.call(this);
  this.filterType = 'column';
  this.selectedValues = [];
};
scout.inherits(scout.ColumnUserTableFilter, scout.UserTableFilter);

scout.ColumnUserTableFilter.prototype.init = function(model, session) {
  scout.ColumnUserTableFilter.parent.prototype.init.call(this, model, session);

  this.calculateCube();
};

scout.ColumnUserTableFilter.prototype.calculateCube = function() {
  var group = (this.column.type === 'date') ? scout.ChartTableControlMatrix.DateGroup.YEAR : -1;
  this.matrix = new scout.ChartTableControlMatrix(this.table, this.session),
  this.xAxis = this.matrix.addAxis(this.column, group);
  this.matrix.calculateCube();
  this.availableValues = [];
  this.xAxis.forEach(function(key) {
    this.availableValues.push(this.xAxis.format(key));
  }, this);
};

scout.ColumnUserTableFilter.prototype.createAddFilterEventData = function() {
  var data = scout.ColumnUserTableFilter.parent.prototype.createAddFilterEventData.call(this);
  return $.extend(data, {
    columnId: this.column.id,
    selectedValues: this.selectedValues
  });
};

scout.ColumnUserTableFilter.prototype.createRemoveFilterEventData = function() {
  var data = scout.ColumnUserTableFilter.parent.prototype.createRemoveFilterEventData.call(this);
  return $.extend(data, {
    columnId: this.column.id
  });
};

scout.ColumnUserTableFilter.prototype.createLabel = function() {
  return this.column.text || '';
};

scout.ColumnUserTableFilter.prototype.createKey = function() {
  return this.column.id;
};

scout.ColumnUserTableFilter.prototype.accept = function($row) {
  var row = $row.data('row'),
    cellValue = this.table.cellValue(this.column, row),
    normCellValue = this.xAxis.norm(cellValue);
  return (this.selectedValues.indexOf(this.xAxis.format(normCellValue)) > -1);
};
