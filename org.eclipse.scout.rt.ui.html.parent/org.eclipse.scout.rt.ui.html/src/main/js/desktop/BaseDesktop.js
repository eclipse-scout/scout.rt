// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.BaseDesktop = function() {
  scout.BaseDesktop.parent.call(this);
  this.taskbar;
  this.modalDialogStack = [];
};
scout.inherits(scout.BaseDesktop, scout.ModelAdapter);

scout.BaseDesktop.prototype.init = function(model, session) {
  scout.BaseDesktop.parent.prototype.init.call(this, model, session);

  this.forms = this.session.getOrCreateModelAdapters(this.model.forms, this);
};

scout.BaseDesktop.prototype._render = function($parent) {
  //this.$entryPoint.addClass('desktop'); //FIXME desktop elements use ids,
  // maybe better change to class to support multiple session divs with multiple
  // desktops

  var i, form;
  for (i = 0; i < this.forms.length; i++) {
    this._addForm(this.forms[i]);
  }
};

scout.BaseDesktop.prototype._addForm = function(form) {
  var added = false;
  if (form.model.displayHint == "view") {
    form.attach(this._resolveViewContainer(form));
    added = true;
  } else if (form.model.displayHint == "dialog") {
    var previousForm;
    if (form.model.modal && this.modalDialogStack.length > 0) {
      previousForm = this.modalDialogStack[this.modalDialogStack.length - 1];
      previousForm.disable();
    }
    this.modalDialogStack.push(form);

    form.attach(this.$parent);

    if (this.taskbar) {
      if (previousForm) {
        this.taskbar.formDisabled(previousForm);
      }
      this.taskbar.formAdded(form);
    }
  } else {
    $.log("Form displayHint not handled: '" + form.model.displayHint + "'.");
  }
};

scout.BaseDesktop.prototype._removeForm = function(form) {
  if (form) {
    form.detach();

    if (form.model.displayHint === "dialog") {
      scout.arrays.remove(this.modalDialogStack, form);
      var previousForm = this.modalDialogStack[this.modalDialogStack.length - 1];
      if (previousForm) {
        previousForm.enable();
      }

      if (this.taskbar) {
        if (previousForm) {
          this.taskbar.formEnabled(previousForm);
        }
        this.taskbar.formRemoved(form);
      }
    }
  }
};

scout.BaseDesktop.prototype.activateForm = function(form) {
  if (form) {
    if (form.model.displayHint === "dialog") {
      //re attach it at the end
      form.attach(this.$parent);

      if (this.taskbar) {
        this.taskbar.formActivated(form);
      }
    }

  }
};

scout.BaseDesktop.prototype.onModelPropertyChange = function() {};

scout.BaseDesktop.prototype.onModelCreate = function(event) {
  if (event.objectType == "Form") {
    var form = this.session.objectFactory.create(event);
    this._addForm(form);
  } else {
    $.log("Widget creation not handled for object type '" + event.objectType + "'.");
  }
};

scout.BaseDesktop.prototype.onModelAction = function(event) {
  var form;

  if (event.type_ == 'formAdded') {
    form = this.session.modelAdapterRegistry[event.formId];
    this._addForm(form);
  } else if (event.type_ == 'formRemoved') {
    form = this.session.modelAdapterRegistry[event.formId];
    this._removeForm(form);
  } else if (event.type_ == 'formEnsureVisible') {
    form = this.session.modelAdapterRegistry[event.formId];
    this.activateForm(form);
  } else if (event.type_ == 'formRemoved') {
    form = this.session.modelAdapterRegistry[event.formId];
    this._removeForm(form);
  } else {
    $.log("Model event not handled. Widget: Desktop. Event: " + event.type_ + ".");
  }
};
