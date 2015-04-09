var FormSpecHelper = function(session) {
  this.session = session;
};

FormSpecHelper.prototype.createFormWithOneField = function(session, parentId) {
  var form = this.createFormModel();
  var rootGroupBox = this.createGroupBoxModel();
  var field = this.createFieldModel();

  form.rootGroupBox = rootGroupBox.id;
  form.owner = parentId || session.rootAdapter.id;
  rootGroupBox.fields = [field.id];

  return createAdapter(form, session, [rootGroupBox, field]);
};

FormSpecHelper.prototype.createFormModel = function() {
  return createSimpleModel('Form');
};

FormSpecHelper.prototype.createFieldModel = function(objectType) {
  var model = createSimpleModel(objectType || 'StringField');
  $.extend(model, {
    'enabled': true,
    'visible': true
  });

  return model;
};

FormSpecHelper.prototype.createGroupBoxModel = function() {
  return this.createFieldModel('GroupBox');
};
