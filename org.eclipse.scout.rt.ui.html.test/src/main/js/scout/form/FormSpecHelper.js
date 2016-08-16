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

// FIXME [awe] 6.1 jasmine - DEPRECATED, remove method -> use *2 and rename
scout.FormSpecHelper.prototype.createViewWithOneField = function(parentId) {
  var form = this.createFormWithOneField(parentId);
  form.displayHint = scout.Form.DisplayHint.VIEW;
  return form;
};

// FIXME [awe] 6.1 jasmine - DEPRECATED, remove method -> use *2 and rename
scout.FormSpecHelper.prototype.createFormWithOneField = function(parentId) {
  var form = this.createFormModel();
  var rootGroupBox = this.createFieldModel('GroupBox');
  var field = this.createFieldModel('StringField');

  form.rootGroupBox = rootGroupBox;
  rootGroupBox.mainBox = true;
  rootGroupBox.fields = [field];

  return scout.create(form);
};

scout.FormSpecHelper.prototype.createFormWithOneField2 = function(parent) {
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

scout.FormSpecHelper.prototype.createFormWithFields = function(parent, isModal, numFields) {
  parent = scout.nvl(parent, this.session.desktop);
  var form = scout.create('Form', {
    parent: parent,
    displayHint: isModal ? 'dialog' : 'view'});
  var rootGroupBox = this.createGroupBoxWithFields(form, true, numFields);
  form.setRootGroupBox(rootGroupBox);
  return form;
};

// FIXME [awe] 6.1 jasmine - DEPRECATED, remove method
scout.FormSpecHelper.prototype.createFormModel = function() {
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


/**
 * Converts the given adapterDataArray into a map of adapterData and registers the adapterData in the Session.
 * Only use this function when your tests requires to have a remote adapter. In that case create widget and
 * remote adapter with Session#getOrCreateWidget().
 *
 * @param adapterDataArray
 */
scout.FormSpecHelper.prototype.registerAdapterData = function(adapterDataArray) {
  var adapterDataMap = this.mapAdapterData(adapterDataArray);
  this.session._copyAdapterData(adapterDataMap);
};

/**
 * Converts the given adapaterDataArray into a map of adapterData where the key
 * is the adapterData.id and the value is the adapterData itself.
 */
scout.FormSpecHelper.prototype.mapAdapterData = function(adapterDataArray) {
  var adapterDataMap = {};
  adapterDataArray = scout.arrays.ensure(adapterDataArray);
  adapterDataArray.forEach(function(adapterData) {
    adapterDataMap[adapterData.id] = adapterData;
  });
  return adapterDataMap;
};
