scout.CheckBoxColumn = function() {
  scout.CheckBoxColumn.parent.call(this);
};
scout.inherits(scout.CheckBoxColumn, scout.Column);

scout.CheckBoxColumn.CHECKABLE_COLUMN_SIZE = 40;

scout.CheckBoxColumn.prototype.buildCell = function(row) {
  var cell = '<div class="table-cell checkable-col"  style="min-width:' + scout.CheckBoxColumn.CHECKABLE_COLUMN_SIZE +
    'px; max-width:' + scout.Table.CHECKABLE_COLUMN_SIZE + 'px;"' + scout.device.unselectableAttribute +
    '><input type="checkbox" id="' + row.id + '-checkable" ';
  if (row.checked) {
    cell += ' checked="checked" ';
  }
  if (!row.enabled) {
    cell += ' disabled="disabled" ';
  }
  cell += '/><label for="' + row.id + '-checkable">&nbsp;</label></div>';

  return cell;
};
