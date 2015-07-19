var FormSpecHelper = function(session) {
  this.session = session;
};

FormSpecHelper.prototype.createFormWithOneField = function(session, parentId) {
  var form = this.createFormModel();
  var rootGroupBox = this.createGroupBoxModel();
  var field = this.createFieldModel();

  form.rootGroupBox = rootGroupBox.id;
  rootGroupBox.mainBox = true;
  form.owner = parentId || session.rootAdapter.id;
  rootGroupBox.fields = [field.id];

  return createAdapter(form, session, [rootGroupBox, field]);
};

FormSpecHelper.prototype.createFormModel = function() {
  var form = createSimpleModel('Form');
  // By definition, a Form must have a 'displayParent'. That is why a mocked parent is set.
  form.parent = {
      rendered: true,
      removeChild: function() {},
      addChild: function() {},
      inFront: function() { return true; }, // expected API of a 'displayParent'
      glassPaneTargets: function() { return []; } // expected API of a 'displayParent'
    };
  return form;
};

FormSpecHelper.prototype.createFieldModel = function(objectType) {
  var model = createSimpleModel(objectType || 'StringField');
  model.enabled = true;
  model.visible = true;

  return model;
};

FormSpecHelper.prototype.createGroupBoxModel = function() {
  return this.createFieldModel('GroupBox');
};

FormSpecHelper.prototype.createFormXFields = function(x, session, isModal, parentId) {
  var form = isModal ? this.createFormModelWithDisplayHint('dialog') : this.createFormModelWithDisplayHint('view');
  var rootGroupBox = this.createGroupBoxModel();
  var fields = [];
  var fieldIds = [];
  var field;
  for (var i = 0; i < x; i++) {
    field = this.createFieldModel();
    fields.push(field);
    fieldIds.push(field.id);
  }
  rootGroupBox.fields = fieldIds;
  form.rootGroupBox = rootGroupBox.id;
  form.owner = parentId || session.rootAdapter.id;
  fields.push(rootGroupBox);
  return createAdapter(form, session, fields);
};

FormSpecHelper.prototype.createFormModelWithDisplayHint = function(displayHint) {
  var model = this.createFormModel();
  model.displayHint = displayHint;
  return model;
};

/**
 * Creates an adapter with rootAdapter as owner.
 * Expects model.fields to be set, creates an adapter for each field.
 * Also replaces model.fields with the ids of the fields.
 */
FormSpecHelper.prototype.createCompositeField = function(session, model) {
  var fields = model.fields || [];
  model.fields = [];
  fields.forEach(function(field) {
    field.owner = model.id;
    model.fields.push(field.id);
  });
  model.owner = session.rootAdapter.id;
  return createAdapter(model, session, fields);
};
