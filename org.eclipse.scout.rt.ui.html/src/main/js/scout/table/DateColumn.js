scout.DateColumn = function() {
  scout.DateColumn.parent.call(this);
};
scout.inherits(scout.DateColumn, scout.Column);

scout.DateColumn.prototype.init = function(model, session) {
  scout.DateColumn.parent.prototype.init.call(this, model, session);

  this.groupFormatFormatter = new scout.DateFormat(this.session.locale, this.groupFormat);
};

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

scout.DateColumn.prototype.cellTextForGrouping = function(row) {
  if (this.groupFormat === undefined || this.groupFormat === this.format) {
    // fallback/shortcut, if no groupFormat defined or groupFormat equals format use cellText
    return this.table.cellText(this, row);
  }

  if (this.groupFormatFormatter) {
    var val = this.table.cellValue(this, row);
    return this.groupFormatFormatter.format(val);
  }

  return this.table.cellText(this, row);
};
