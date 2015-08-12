scout.TableColumnFilter = function(table, column) {
  var matrix = new scout.ChartTableControlMatrix(table, table.session),
    group = (column.type === 'date') ? scout.ChartTableControlMatrix.DateGroup.YEAR : -1;

  this.table = table;
  this.column = column;
  this.label = column.text || '';
  this.availableValues = [];
  this.selectedValues = [];
  this.xAxis = matrix.addAxis(column, group);
  matrix.calculateCube();
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
