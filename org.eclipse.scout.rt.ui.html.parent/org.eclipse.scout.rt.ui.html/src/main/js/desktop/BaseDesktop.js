// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.BaseDesktop = function(model, session) {
  this.session = session;
  this.model = model;
  this.tree;
  if (session && model) {
    this.session.widgetMap[model.id] = this;
  }
};

scout.BaseDesktop.prototype.attach = function($parent) {
  this.$parent = $parent;
  this._render($parent); //FIXME CGU inherit from modeladapter
};

scout.BaseDesktop.prototype._render = function($parent) {
  //this.$entryPoint.addClass('desktop'); //FIXME desktop elements use ids,
  // maybe better change to class to support multiple session divs with multiple
  // desktops

  var form, i, formModel;
  if (this.model.forms) {
    for (i = 0; i < this.model.forms.length; i++) {
      formModel = this.model.forms[i];
      form = this.session.widgetMap[formModel.id];
      if (!form) {
        form = this.session.objectFactory.create(formModel);
      }
      this._attachForm(form);
    }
  }
};

scout.BaseDesktop.prototype._attachForm = function(form) {
  if (form.model.displayHint == "view") {
    form.attach(this._resolveViewContainer(form));
  } else if (form.model.displayHint == "dialog") {
    form.attach(this.$parent);
  } else {
    $.log("Form displayHint not handled: '" + form.model.displayHint + "'.");
  }
};

scout.BaseDesktop.prototype._removeForm = function(form) {
  if (form) {
    form.detach();
  }
};

scout.BaseDesktop.prototype.onModelPropertyChange = function() {};

scout.BaseDesktop.prototype.onModelCreate = function(event) {
  if (event.objectType == "Outline") {
    this.tree.onOutlineCreated(event);
  } else if (event.objectType == "Form") {
    var form = this.session.objectFactory.create(event);
    this._attachForm(form);
  } else {
    $.log("Widget creation not handled for object type '" + event.objectType + "'.");
  }
};

scout.BaseDesktop.prototype.onModelAction = function(event) {
  var form;

  if (event.type_ == 'outlineChanged') {
    this.tree.onOutlineChanged(event.outlineId);
  } else if (event.type_ == 'formAdded') {
    form = this.session.widgetMap[event.formId];
    this._attachForm(form);
  } else if (event.type_ == 'formRemoved') {
    form = this.session.widgetMap[event.formId];
    this._removeForm(form);
  } else {
    $.log("Model event not handled. Widget: Desktop. Event: " + event.type_ + ".");
  }
};
