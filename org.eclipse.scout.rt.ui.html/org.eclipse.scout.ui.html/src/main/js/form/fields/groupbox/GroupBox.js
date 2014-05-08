// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.GroupBox = function(session, model) {
  this.base(session, model);
};
scout.GroupBox.inheritsFrom(scout.ModelAdapter);

scout.GroupBox.prototype._render = function($parent) {
  this.$container = $parent.appendDiv();

  var formField, i, formFieldModel;

  for (i = 0; i < this.model.formFields.length; i++) {
    formFieldModel = this.model.formFields[i];
    formField = this.session.widgetMap[formFieldModel.id];
    if (!formField) {
      formField = this.session.objectFactory.create(formFieldModel);
    }
    formField.attach(this.$container);
  }
};

scout.GroupBox.prototype._onModelBorderVisibleChanged = function(borderVisible) {

};

scout.GroupBox.prototype.onModelPropertyChange = function(event) {
  if (event.borderVisible !== undefined) {
    this._onModelBorderVisibleChanged(event.borderVisible);
  }
};
