scout.CheckBoxColumn = function() {
  scout.CheckBoxColumn.parent.call(this);
};
scout.inherits(scout.CheckBoxColumn, scout.Column);

scout.CheckBoxColumn.CHECKABLE_COLUMN_SIZE = 30;

scout.CheckBoxColumn.prototype.init = function(model, session) {
  this.fixedWidth = true;
  this.guiOnlyCheckBoxColumn = true;
  // Fill in the missing default values
  scout.defaultValues.applyTo(this);
  this.width = scout.CheckBoxColumn.CHECKABLE_COLUMN_SIZE;
  this.objectType = 'CheckBoxColumn';
};

scout.CheckBoxColumn.prototype.buildCell = function(row) {
  var cell = '<div class="table-cell checkable"  style="min-width:' + scout.CheckBoxColumn.CHECKABLE_COLUMN_SIZE +
    'px; max-width:' + scout.CheckBoxColumn.CHECKABLE_COLUMN_SIZE + 'px;"' + scout.device.unselectableAttribute +
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

scout.CheckBoxColumn.prototype.buildHeaderCell = function(header) {
  header.$container.prependDiv('header-resize');
  this.header = header.$container.prependDiv('header-item')
    .css('min-width', scout.CheckBoxColumn.CHECKABLE_COLUMN_SIZE + 'px')
    .css('max-width', scout.CheckBoxColumn.CHECKABLE_COLUMN_SIZE + 'px');
};

scout.CheckBoxColumn.prototype.onMouseUp = function(event, $row) {
  var row = $row.data('row');
  var $target = $(event.target);
  if ($target.is('label') && $target.parent().hasClass('checkable')) {
    this.table.checkRow(row, !row.checked);
  }
};
