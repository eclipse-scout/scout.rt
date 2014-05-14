scout.Form = function(session, model) {
  this.base(session, model);
  this._$title;
};

scout.Form.inheritsFrom(scout.ModelAdapter);

scout.Form.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(undefined, 'form');
  // TODO AWE: append form title section (including ! ? and progress indicator)
  this._$title = this.$container.appendDiv(undefined, 'form-title', 'TODO: Form title');

  // TODO AWE: copy/paste from GroupBox.js
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

scout.Form.prototype.onModelCreate = function() {
};

scout.Form.prototype.onModelAction = function(event) {
  if (event.type_ == 'formClosed') {
    this.dispose();
  }
  else {
    $.log("Model event not handled. Widget: Form. Event: " + event.type_ + ".");
  }
};
