scout.DateColumn = function() {
  scout.DateColumn.parent.call(this);
};
scout.inherits(scout.DateColumn, scout.Column);

/**
 * Converts the value which is a json date to a javascript date.
 */
scout.DateColumn.prototype.initCell = function(cell) {
  cell = scout.DateColumn.parent.prototype.initCell.call(this, cell);
  if (cell.value !== undefined) {
    cell.value = scout.dates.parseJsonDate(cell.value);
  }
  return cell;
};
