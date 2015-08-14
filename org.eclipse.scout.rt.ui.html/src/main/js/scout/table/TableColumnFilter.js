scout.TableColumnFilter = function(table, column) {
  this.table = table;
  this.column = column;
  this.label = column.text || '';
  this.selectedValues = [];

  this.calculateCube();
};

scout.TableColumnFilter.prototype.calculateCube = function() {
  var group = (this.column.type === 'date') ? scout.ChartTableControlMatrix.DateGroup.YEAR : -1;
  this.matrix = new scout.ChartTableControlMatrix(this.table, this.table.session),
  this.xAxis = this.matrix.addAxis(this.column, group);
  this.matrix.calculateCube();
  this.availableValues = [];
  this.xAxis.forEach(function(key) {
    this.availableValues.push(this.xAxis.format(key));
  }, this);
};

scout.TableColumnFilter.prototype.accept = function($row) {
  var row = $row.data('row'),
    cellValue = this.table.cellValue(this.column, row),
    normCellValue = this.xAxis.norm(cellValue);
  return (this.selectedValues.indexOf(this.xAxis.format(normCellValue)) > -1);
};
