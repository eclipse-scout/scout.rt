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
scout.TreeField = function() {
  scout.TreeField.parent.call(this);

  this.gridDataHints.weightY = 1.0;
  this.gridDataHints.h = 3;
  this._addWidgetProperties(['tree']);
};
scout.inherits(scout.TreeField, scout.FormField);

scout.TreeField.prototype._render = function() {
  this.addContainer(this.$parent, 'tree-field');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addStatus();
  if (this.tree) {
    this._renderTree();
  }
};

/**
 * Will also be called by model adapter on property change event
 */
scout.TreeField.prototype._renderTree = function() {
  this.tree.render();
  this.addField(this.tree.$container);
  this.$field.addDeviceClass();
};

scout.TreeField.prototype._removeTree = function() {
  this.tree.remove();
  this._removeField();
};
