/*******************************************************************************
 * Copyright (c) 2017 BSI Business Systems Integration AG.
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
  this.$container.addClass('menu-field');
  if (this.uiCssClass) {
    this.$container.addClass(this.uiCssClass);
  }
  this.field.render(this.$container);
  this.field.$container.addClass('content');

  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.ColumnLayout());
};

scout.FormFieldMenu.prototype._renderText = function(text) {
  scout.FormFieldMenu.parent.prototype._renderText.call(this, text);
  if (this.$text) {
    this.field.$container.insertAfter(this.$text);
  }
};

scout.FormFieldMenu.prototype.setField = function(field) {
  this.setProperty('field', field);
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
