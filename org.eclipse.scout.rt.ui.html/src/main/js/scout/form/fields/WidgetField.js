/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.WidgetField = function() {
  scout.WidgetField.parent.call(this);
  this._addWidgetProperties(['fieldWidget']);
};
scout.inherits(scout.WidgetField, scout.FormField);

scout.WidgetField.prototype._init = function(model) {
  scout.WidgetField.parent.prototype._init.call(this, model);
};

scout.WidgetField.prototype._render = function() {
  this.addContainer(this.$parent, 'widget-field', new scout.SingleLayout());
  this.addLabel();
  this.addMandatoryIndicator();
  this.addStatus();
  this._renderFieldWidget();

};
scout.WidgetField.prototype.setFieldWidget = function(fieldWidget) {
  this.setProperty('fieldWidget', fieldWidget);
};

scout.WidgetField.prototype._renderFieldWidget = function() {
  if (this.fieldWidget) {
    this.fieldWidget.render();
  }
};

scout.WidgetField.prototype._removeFieldWidget = function() {
  this.fieldWidget.remove();
};
