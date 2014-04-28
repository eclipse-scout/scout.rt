// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.TableField = function(session, model) {
  this.model = model;
  this.session = session;

  if (session && model) {
    this.session.widgetMap[model.table.id] = this;
  }
};

scout.TableField.prototype.render = function ($parent) {
  this._$container = $parent.appendDiv();
  this.table = new scout.Table(this.session, this.model.table);
  this.table.attach(this._$container);
};

scout.TableField.prototype.onModelCreate = function(event) {
  if (event.objectType == "Table") {
    if (this.table) {
      this.table.detach();
    }
    this.table = new scout.Table(this.session, event);
    this.table.attach();
  } else {
    $.log("Widget creation not handled for object type '" + event.objectType + "'.");
  }
};


scout.TableField.prototype._onModelTableChanged = function (tableId) {
  this.table.detach();
  this.table= this.session.widgetMap[tableId];
  this.table.attach(this._$container);
};

scout.TableField.prototype.onModelPropertyChange = function(event) {
  if (event.tableId !== undefined) {
    this._onModelTableChanged(event.tableId);
  }
};
