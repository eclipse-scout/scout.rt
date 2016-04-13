/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
var FormSpecHelper = function(session) {
  this.session = session;
};

FormSpecHelper.prototype.createFormWithOneField = function(parentId) {
  var form = this.createFormModel();
  var rootGroupBox = this.createGroupBoxModel();
  var field = this.createFieldModel('StringField');

  form.rootGroupBox = rootGroupBox.id;
  rootGroupBox.mainBox = true;
  form.owner = parentId || this.session.rootAdapter.id;
  rootGroupBox.fields = [field.id];

  return createAdapter(form, this.session, [rootGroupBox, field]);
};

FormSpecHelper.prototype.createGroupBoxWithOneField = function(parentId) {
  var groupBox = this.createGroupBoxModel();
  var field = this.createFieldModel('StringField');

  groupBox.owner = parentId || this.session.rootAdapter.id;
  groupBox.fields = [field.id];

  return createAdapter(groupBox, this.session, [field]);
};

FormSpecHelper.prototype.createFormModel = function() {
  var form = createSimpleModel('Form', this.session);
  // By definition, a Form must have a 'displayParent'. That is why a mocked parent is set.
  form.parent = {
    rendered: true,
    removeChild: function() {},
    setParent: function() {},
    addChild: function() {},
    inFront: function() {
      return true;
    }, // expected API of a 'displayParent'
    glassPaneTargets: function() {
      return [];
    } // expected API of a 'displayParent'
  };
  return form;
};

FormSpecHelper.prototype.createFieldModel = function(objectType) {
  var session = this.session;
  var model = createSimpleModel(objectType || 'StringField', session);
  model.enabled = true;
  model.visible = true;
  return model;
};

FormSpecHelper.prototype.createGroupBoxModel = function() {
  return this.createFieldModel('GroupBox');
};

FormSpecHelper.prototype.createFormXFields = function(x, isModal, parentId) {
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
  form.owner = parentId || this.session.rootAdapter.id;
  fields.push(rootGroupBox);
  return createAdapter(form, this.session, fields);
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
