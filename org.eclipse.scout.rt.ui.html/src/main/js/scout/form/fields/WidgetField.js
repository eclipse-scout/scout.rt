/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.WidgetField = function() {
  scout.WidgetField.parent.call(this);

  this.scrollable = true;

  this._addWidgetProperties(['fieldWidget']);
};
scout.inherits(scout.WidgetField, scout.FormField);

scout.WidgetField.prototype._init = function(model) {
  scout.WidgetField.parent.prototype._init.call(this, model);
};

scout.WidgetField.prototype._render = function() {
  this.addContainer(this.$parent, 'widget-field', new scout.WidgetFieldLayout(this));
  this.addLabel();
  this.addMandatoryIndicator();
  this.addStatus();
};

scout.WidgetField.prototype._renderProperties = function() {
  scout.WidgetField.parent.prototype._renderProperties.call(this);
  this._renderFieldWidget();
  this._renderScrollable();
};

scout.WidgetField.prototype.setFieldWidget = function(fieldWidget) {
  this.setProperty('fieldWidget', fieldWidget);
};

scout.WidgetField.prototype._renderFieldWidget = function() {
  if (!this.fieldWidget) {
    return;
  }
  this.fieldWidget.render();
  this.addField(this.fieldWidget.$container);
  this.invalidateLayoutTree();
};

scout.WidgetField.prototype._removeFieldWidget = function() {
  if (!this.fieldWidget) {
    return;
  }
  this.fieldWidget.remove();
  this._removeField();
  this.invalidateLayoutTree();
};

scout.WidgetField.prototype.setScrollable = function(scrollable) {
  this.setProperty('scrollable', scrollable);
};

scout.WidgetField.prototype._renderScrollable = function() {
  this._uninstallScrollbars();
  if (this.scrollable) {
    this._installScrollbars();
  }
};

/**
 * @override
 */
scout.WidgetField.prototype.get$Scrollable = function() {
  return this.$fieldContainer;
};
