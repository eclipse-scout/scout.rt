scout.Form = function(model, session) {
  scout.Form.parent.call(this, model, session);
  this._$title;
};

scout.inherits(scout.Form, scout.ModelAdapter);

scout.Form.prototype._render = function($parent) {
  this.$container = $parent.appendDiv(undefined, 'form');
  // TODO AWE: append form title section (including ! ? and progress indicator)
  this._$title = this.$container.appendDiv(undefined, 'form-title', this.model.title);

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

  if (this.model.displayHint == 'dialog') {
    this.$container.addClass('dialog-form');

    // TODO AWE: (form) hacky hack: add something to close the form
    var closeButton = $('<button>Close [X]</button>');
    closeButton.prependTo(this.$container);
    var that = this;
    closeButton.on('click', function() {
      that.session.send('formClosing', that.model.id);
    });
  }

};

scout.Form.prototype.detach = function() {
  scout.Form.parent.prototype.detach.call(this);

  var i, formFieldModel, formField;
  for (i = 0; i < this.model.formFields.length; i++) {
    formFieldModel = this.model.formFields[i];
    formField = this.session.widgetMap[formFieldModel.id];
    if(formField && formField.dispose) {
      formField.dispose();
    }
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
