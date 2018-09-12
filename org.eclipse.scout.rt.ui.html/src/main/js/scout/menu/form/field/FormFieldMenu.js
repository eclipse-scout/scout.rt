/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.FormFieldMenu = function() {
  scout.FormFieldMenu.parent.call(this);
  this._addWidgetProperties('field');
};
scout.inherits(scout.FormFieldMenu, scout.Menu);

scout.FormFieldMenu.prototype._render = function() {
  this.$container = this.$parent.appendDiv('menu-item');
  this.$container.addClass('form-field-menu');
  if (this.uiCssClass) {
    this.$container.addClass(this.uiCssClass);
  }
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.ColumnLayout({
    stretch: false
  }));
};

scout.FormFieldMenu.prototype._renderProperties = function() {
  scout.FormFieldMenu.parent.prototype._renderProperties.call(this);
  this._renderField();
};

/**
 * Override
 */
scout.FormFieldMenu.prototype._renderText = function(text) {
  scout.FormFieldMenu.parent.prototype._renderText.call(this, text);
  if (this.$text) {
    this.field.$container.insertAfter(this.$text);
  }
};

scout.FormFieldMenu.prototype.setField = function(field) {
  this.setProperty('field', field);
};

scout.FormFieldMenu.prototype._renderField = function() {
  if (this.field) {
    this.field.render(this.$container);
    this.formFieldMenu = true;
    if (this.field.gridDataHints) {
      this.field.gridData = scout.GridData.createFromHints(this.field, 1);
    }
    var layoutData = new scout.LogicalGridData(this.field);
    layoutData.validate();
    this.field.setLayoutData(layoutData);
    this.field.$container.addClass('content');
  }
};

scout.FormFieldMenu.prototype._removeField = function() {
  if (this.field) {
    this.field.remove();
  }
};

scout.FormFieldMenu.prototype.clone = function(model, options) {
  var clone = scout.FormFieldMenu.parent.prototype.clone.call(this, model, options);
  this._deepCloneProperties(clone, ['field'], options);
  return clone;
};

scout.FormFieldMenu.prototype.isTabTarget = function() {
  return false;
};

scout.FormFieldMenu.prototype._renderOverflown = function() {
  scout.FormFieldMenu.parent.prototype._renderOverflown.call(this);
  this.field._hideStatusMessage();
};
