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
scout.FormSpecHelper = function(session) {
  this.session = session;
};

scout.FormSpecHelper.prototype.createViewWithOneField = function(parent) {
  var form = this.createFormWithOneField(parent);
  form.displayHint = scout.Form.DisplayHint.VIEW;
  return form;
};

scout.FormSpecHelper.prototype.createFormWithOneField = function(parent) {
  parent = scout.nvl(parent, this.session.desktop);
  var form = scout.create('Form', {parent: parent});
  var rootGroupBox = this.createGroupBoxWithFields(form, true, 1);
  form.rootGroupBox = rootGroupBox;
  return form;
};

scout.FormSpecHelper.prototype.createGroupBoxWithOneField = function(parent, mainBox, numFields) {
  return this.createGroupBoxWithFields(parent, false, 1);
};

scout.FormSpecHelper.prototype.createGroupBoxWithFields = function(parent, mainBox, numFields) {
  parent = scout.nvl(parent, this.session.desktop);
  mainBox = scout.nvl(mainBox, false);
  numFields = scout.nvl(numFields, 1);
  var
    fields = [],
    groupBox = scout.create('GroupBox', {
    parent: parent,
    mainBox: scout.nvl(mainBox, false)});
  for (var i = 0; i < numFields; i++) {
    fields.push(scout.create('StringField', {parent: groupBox}));
  }
  groupBox.setProperty('fields', fields);
  return groupBox;
};

scout.FormSpecHelper.prototype.createRadioButtonGroup = function(parent, numRadioButtons) {
  parent = scout.nvl(parent, this.session.desktop);
  numRadioButtons = scout.nvl(numRadioButtons, 2);
  var
    fields = [],
    radioButtonGroup = scout.create('RadioButtonGroup', {parent: parent});

  for (var i = 0; i < numRadioButtons; i++) {
    fields.push(scout.create('RadioButton', {parent: radioButtonGroup}));
  }
  radioButtonGroup.setProperty('formFields', fields);
  radioButtonGroup.setProperty('radioButtons', fields);
  return radioButtonGroup;
};

scout.FormSpecHelper.prototype.createFormWithFields = function(parent, isModal, numFields) {
  parent = scout.nvl(parent, this.session.desktop);
  var form = scout.create('Form', {
    parent: parent,
    displayHint: isModal ? 'dialog' : 'view'});
  var rootGroupBox = this.createGroupBoxWithFields(form, true, numFields);
  form._syncRootGroupBox(rootGroupBox);
  return form;
};

scout.FormSpecHelper.prototype.createFieldModel = function(objectType, parent, modelProperties) {
  parent = scout.nvl(parent, this.session.desktop);
  var model = createSimpleModel(objectType || 'StringField', this.session);
  model.parent = parent;
  if (modelProperties) {
    $.extend(model, modelProperties);
  }
  return model;
};

scout.FormSpecHelper.prototype.createField = function(objectType, parent, modelProperties) {
  return scout.create(objectType, this.createFieldModel(objectType, parent, modelProperties));
};
