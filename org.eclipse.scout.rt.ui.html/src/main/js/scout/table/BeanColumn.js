scout.BeanColumn = function() {
  scout.BeanColumn.parent.call(this);
};
scout.inherits(scout.BeanColumn, scout.Column);

scout.BeanColumn.prototype.buildCell = function(row) {
  var $cell, value;
  $cell = $(scout.BeanColumn.parent.prototype.buildCell.call(this, row));
  // Clear any content (e.g. nbsp due to empty text)
  $cell.empty();
  $cell.removeClass('empty');

  value = this.table.cellValue(this, row);
  this._renderValue($cell, value);
  if (scout.device.tableAdditionalDivRequired) {
    $cell.html('<div class="width-fix" style="max-width: ' + (this.width - this.table.cellHorizontalPadding - 2 /* unknown IE9 extra space */) + 'px; ' + '">' + $cell.html() + '</div>');
  }
  return $cell[0].outerHTML;
};

/**
 * Override to render the value.<p>
 * If you have a large table you should consider overriding buildCell instead and create the html as string instead of using jquery.
 */
scout.BeanColumn.prototype._renderValue = function($cell, value) {
  // to be implemented by the subclass
};
