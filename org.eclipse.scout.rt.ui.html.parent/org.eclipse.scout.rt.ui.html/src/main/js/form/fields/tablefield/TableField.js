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
  if (this.table) {
    this.table.render(this.$container);
  }
};

scout.TableField.prototype._setTable = function(table) {
  this.table = table;
  if (this.isRendered() && table) {
    table.render(this.$container.parent());
  }
};

scout.TableField.prototype.onModelPropertyChange = function(event) {
  if (event.table !== undefined) {
    //FIXME CGU verify with AWE: dieses verhalten müsste vom neuen konzept noch berücksichtigt werden
    if (this.table) {
      this.table.remove();
    }
    scout.TableField.parent.prototype.onModelPropertyChange.call(this);
  }
};
