scout.IconColumn = function() {
  scout.IconColumn.parent.call(this);
};
scout.inherits(scout.IconColumn, scout.Column);

scout.IconColumn.prototype.buildCell = function(row) {
  var $cell = $(scout.IconColumn.parent.prototype.buildCell.call(this, row));
  var value = this.table.cellValue(this, row);
  this._renderValue($cell, value);
  return $cell[0].outerHTML;
};


scout.IconColumn.prototype._renderValue = function($cell, value) {
  var iconChar, $icon;
  if(value){
    if(scout.strings.startsWith(value, "font:")){
      iconChar = value.substr(5);
      $cell.appendSpan('font-icon', iconChar);
    } else {
      $icon = $.make('<img>')
        .attr('src', scout.helpers.dynamicRsourceUrl(this, value)
        .appendTo($cell));
    }
  }
};
