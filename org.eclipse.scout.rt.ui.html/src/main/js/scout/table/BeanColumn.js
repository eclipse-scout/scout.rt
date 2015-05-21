scout.BeanColumn = function() {
  scout.BeanColumn.parent.call(this);
};
scout.inherits(scout.BeanColumn, scout.Column);

scout.BeanColumn.prototype.buildCell = function(row) {
  var $cell, value;
  $cell = $(scout.BeanColumn.parent.prototype.buildCell.call(this, row));
  // Clear any content (e.g. nbsp due to empty text)
  $cell.empty();

  value = this.table.cellValue(this, row);
  this._renderValue($cell, value);
  return $cell[0].outerHTML;
};

/**
 * Override to render the value.<p>
 * If you have a large table you should consider overriding buildCell instead and create the html as string instead of using jquery.
 */
scout.BeanColumn.prototype._renderValue = function($cell, value) {
  // to be implemented by the subclass
};
