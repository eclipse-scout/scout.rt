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
scout.BreadcrumbBarField = function() {
  scout.BreadcrumbBarField.parent.call(this);

  this._addWidgetProperties(['breadcrumbBar']);
};
scout.inherits(scout.BreadcrumbBarField, scout.FormField);

scout.BreadcrumbBarField.prototype._render = function() {
  this.addContainer(this.$parent, 'breadcrumb-bar-field');
  if (this.breadcrumbBar) {
    this._renderBreadcrumbBar();
  }
};

// Will also be called by model adapter on property change event
scout.BreadcrumbBarField.prototype._renderBreadcrumbBar = function() {
  this.breadcrumbBar.render();
  this.addLabel();
  this.addMandatoryIndicator();
  this.addField(this.breadcrumbBar.$container);
  this.addStatus();
};

scout.BreadcrumbBarField.prototype.setBreadcrumbBar = function(breadcrumbBar) {
  this._setBreadcrumbBar(breadcrumbBar);
};

scout.BreadcrumbBarField.prototype.setBreadcrumbItems = function(breadcrumbItems) {
  this.breadcrumbBar.setBreadcrumbItems(breadcrumbItems);
};

scout.BreadcrumbBarField.prototype._removeBreadcrumbBar = function() {
  this.breadcrumbBar.remove();
  this._removeField();
};
