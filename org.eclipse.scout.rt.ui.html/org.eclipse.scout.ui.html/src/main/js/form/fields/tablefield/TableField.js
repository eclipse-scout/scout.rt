// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TableField = function(session, model) {
  this.base(session, model);
};
scout.TableField.inheritsFrom(scout.ModelAdapter);

scout.TableField.prototype._render = function($parent) {
  this.$container = $parent.appendDiv();
  if (this.model.label) {
    this.$label = $container.makeDiv(undefined, 'label');
  }

  this.table = this.session.widgetMap[this.model.table];
  if (!this.table) {
    this.table = this.session.objectFactory.create(this.model.table);
  }
  this.table.attach(this.$container);
};

scout.TableField.prototype.onModelCreate = function(event) {
  if (event.objectType == "Table") {
    if (this.table) {
      this.table.detach();
    }
    this.table = this.session.objectFactory.create(this.session, event);
    this.table.attach();
  }
  else {
    $.log("Widget creation not handled for object type '" + event.objectType + "'.");
  }
};

scout.TableField.prototype._onModelTableChanged = function(tableId) {
  this.table.detach();
  this.table = this.session.widgetMap[tableId];
  this.table.attach(this._$container);
};

scout.TableField.prototype.onModelPropertyChange = function(event) {
  if (event.tableId !== undefined) {
    this._onModelTableChanged(event.tableId);
  }
};
