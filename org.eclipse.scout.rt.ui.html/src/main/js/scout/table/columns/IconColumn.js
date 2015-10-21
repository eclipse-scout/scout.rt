scout.IconColumn = function() {
  scout.IconColumn.parent.call(this);
  this.minWidth = scout.Column.NARROW_MIN_WIDTH;
};
scout.inherits(scout.IconColumn, scout.Column);

scout.IconColumn.prototype.buildCell = function(cell, row) {
  cell.iconId = cell.value || cell.iconId;
  return scout.IconColumn.parent.prototype.buildCell.call(this, cell, row);
};
