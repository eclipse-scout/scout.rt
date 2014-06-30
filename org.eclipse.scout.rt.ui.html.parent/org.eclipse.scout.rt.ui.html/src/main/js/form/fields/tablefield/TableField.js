// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TableField = function() {
  scout.TableField.parent.call(this);
  this._addAdapterProperties(['table']);
};
scout.inherits(scout.TableField, scout.ModelAdapter);

scout.TableField.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(undefined, 'table-field');
  if (this.label) {
    this.$label = this.$container.appendDiv(undefined, 'label');
  }
  this._setTable(this.table);
};

scout.TableField.prototype._setTable = function(table) {
  if (this.isRendered() && table) {
    table.render(this.$container);
  }
};
