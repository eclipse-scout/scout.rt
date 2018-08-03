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

scout.TreeField.prototype.setTree = function(tree) {
  this.setProperty('tree', tree);
};

scout.TreeField.prototype._setTree = function(tree) {
  this._setProperty('tree', tree);
  if (tree) {
    tree.setScrollTop(this.scrollTop);
  }
};

scout.TreeField.prototype._renderTree = function() {
  if (!this.tree) {
    return;
  }
  this.tree.render();
  this.addField(this.tree.$container);
  this.$field.addDeviceClass();
  this.invalidateLayoutTree();
};

scout.TreeField.prototype._removeTree = function() {
  if (!this.tree) {
    return;
  }
  this.tree.remove();
  this._removeField();
  this.invalidateLayoutTree();
};

/**
 * @override
 */
scout.TreeField.prototype.getDelegateScrollable = function() {
  return this.tree;
};
