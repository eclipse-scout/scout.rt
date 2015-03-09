scout.Column = function() {
};

scout.Column.prototype.init = function(model, session) {
  this.session = session;

  // Copy all properties from model to this
  $.extend(this, model);

  // Fill in the missing default values
  scout.defaultValues.applyTo(this);
};

scout.Column.prototype.buildCell = function(row) {
  var style, value, tooltipText, tooltip;
  style = this.table.cellStyle(this, row);
  value = this.table.cellText(this, row);
  tooltipText = this.table.cellTooltipText(this, row);
  tooltip = (!scout.strings.hasText(tooltipText) ? '' : ' title="' + tooltipText + '"');

  return '<div class="table-cell" style="' + style + '"' + tooltip + scout.device.unselectableAttribute + '>' + value + '</div>';
};

scout.Column.prototype.onMouseUp = function(event, $row) {
  // May be implemented by subclasses
};
