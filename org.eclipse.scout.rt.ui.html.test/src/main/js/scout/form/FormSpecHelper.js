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

scout.FormSpecHelper.prototype.createViewWithOneField = function(model) {
  var form = this.createFormWithOneField(model);
  form.displayHint = scout.Form.DisplayHint.VIEW;
  return form;
};

scout.FormSpecHelper.prototype.createFormWithOneField = function(model) {
  var defaults = {
    parent: this.session.desktop
  };
  model = $.extend({}, defaults, model);
  var form = scout.create('Form', model);
  var rootGroupBox = this.createGroupBoxWithFields(form, 1);
  form._setRootGroupBox(rootGroupBox);
  return form;
};

scout.FormSpecHelper.prototype.createGroupBoxWithOneField = function(parent, numFields) {
  return this.createGroupBoxWithFields(parent, 1);
};

scout.FormSpecHelper.prototype.createGroupBoxWithFields = function(parent, numFields) {
  parent = scout.nvl(parent, this.session.desktop);
  numFields = scout.nvl(numFields, 1);
  var
    fields = [],
    groupBox = scout.create('GroupBox', {
      parent: parent
    });
  for (var i = 0; i < numFields; i++) {
    fields.push(scout.create('StringField', {
      parent: groupBox
    }));
  }
  groupBox.setProperty('fields', fields);
  return groupBox;
};

scout.FormSpecHelper.prototype.createRadioButtonGroup = function(parent, numRadioButtons) {
  parent = scout.nvl(parent, this.session.desktop);
  numRadioButtons = scout.nvl(numRadioButtons, 2);
  var
    fields = [],
    radioButtonGroup = scout.create('RadioButtonGroup', {
      parent: parent
    });

  for (var i = 0; i < numRadioButtons; i++) {
    fields.push(scout.create('RadioButton', {
      parent: radioButtonGroup
    }));
  }
  radioButtonGroup.setProperty('formFields', fields);
  radioButtonGroup.setProperty('radioButtons', fields);
  return radioButtonGroup;
};

scout.FormSpecHelper.prototype.createFormWithFields = function(parent, isModal, numFields) {
  parent = scout.nvl(parent, this.session.desktop);
  var form = scout.create('Form', {
    parent: parent,
    displayHint: isModal ? 'dialog' : 'view'
  });
  var rootGroupBox = this.createGroupBoxWithFields(form, numFields);
  form._setRootGroupBox(rootGroupBox);
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
  parent = parent || this.session.desktop;
  return scout.create(objectType, this.createFieldModel(objectType, parent, modelProperties));
};
