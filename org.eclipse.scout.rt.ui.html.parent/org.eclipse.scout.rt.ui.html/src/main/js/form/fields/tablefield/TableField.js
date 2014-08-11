// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TableField = function() {
  scout.TableField.parent.call(this);
  this._addAdapterProperties(['table']);
};
scout.inherits(scout.TableField, scout.FormField);

scout.TableField.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('', 'table-field');
  this.$label = $('<label>')
    .appendTo(this.$container);

  if (this.table) {
    this.table.render(this.$container);
  }
};
