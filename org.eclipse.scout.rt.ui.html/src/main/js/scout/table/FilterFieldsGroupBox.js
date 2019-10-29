/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.FilterFieldsGroupBox = function() {
  scout.FilterFieldsGroupBox.parent.call(this);
  this.gridColumnCount = 1;
  this.cssClass = 'filter-fields';
};
scout.inherits(scout.FilterFieldsGroupBox, scout.GroupBox);

scout.FilterFieldsGroupBox.prototype._init = function(model) {
  scout.FilterFieldsGroupBox.parent.prototype._init.call(this, model);
  this.filter.addFilterFields(this);
};

/**
 * @override GroupBox.js
 */
scout.FilterFieldsGroupBox.prototype._renderProperties = function($parent) {
  scout.FilterFieldsGroupBox.parent.prototype._renderProperties.call(this, $parent);
  this.filter.modifyFilterFields();
};

scout.FilterFieldsGroupBox.prototype.addFilterField = function(objectType, text) {
  var field = scout.create(objectType, {
    parent: this,
    label: this.session.text(text),
    statusVisible: false,
    labelWidthInPixel: 50,
    maxLength: 100,
    updateDisplayTextOnModify: true
  });
  this.addField0(field);
  return field;
};

// Info from awe, cgu: Added '0' to the name to avoid temporarily to avoid naming conflict with FormField#addField
// This should be refactored in a future release
scout.FilterFieldsGroupBox.prototype.addField0 = function(field) {
  this.fields.push(field);
  this._prepareFields();
};
