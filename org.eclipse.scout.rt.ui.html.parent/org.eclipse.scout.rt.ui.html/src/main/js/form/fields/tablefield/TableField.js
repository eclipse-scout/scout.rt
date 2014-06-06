// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TableField = function() {
  scout.TableField.parent.call(this);
};
scout.inherits(scout.TableField, scout.ModelAdapter);

scout.TableField.prototype.init = function(model, session) {
  scout.TableField.parent.prototype.init.call(this, model, session);
  this.table = this.session.getOrCreateModelAdapter(model.table, this);
};

scout.TableField.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(undefined, 'table-field');
  if (this.label) {
    this.$label = this.$container.appendDiv(undefined, 'label');
  }
  if (this.table) {
    this.table.render(this.$container);
  }
};

scout.TableField.prototype.setTable = function(table) {
  this.table = this.updateModelAdapterAndRender(table, this);
};

scout.TableField.prototype.dispose = function() {
  if (this.table) {
    this.table.dispose();
  }
};

scout.TableField.prototype.onModelPropertyChange = function(event) {
  if (event.table !== undefined) {
    this.setTable(event.table);
  }
};
