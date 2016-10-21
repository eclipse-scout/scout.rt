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
scout.TreeBox = function() {
  scout.TreeBox.parent.call(this);
  this._addAdapterProperties(['tree', 'filterBox']);
};
scout.inherits(scout.TreeBox, scout.ValueField);

scout.TreeBox.prototype._render = function($parent) {
  this.addContainer($parent, 'tree-box');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addStatus();

  this.addFieldContainer($parent.makeDiv());
  var htmlComp = scout.HtmlComponent.install(this.$fieldContainer, this.session);
  htmlComp.setLayout(new scout.TreeBoxLayout(this, this.tree, this.filterBox));

  if (this.tree) {
    this._renderTree();
  }
  if (this.filterBox) {
    // TODO [5.2] bsh: Tree | Only render when filter active
    this._renderFilterBox();
    this.tree.htmlComp.pixelBasedSizing = true;
  }
};

scout.TreeBox.prototype._renderTree = function($fieldContainer) {
  this.tree.render(this.$fieldContainer);
  this.addField(this.tree.$container);
};

scout.TreeBox.prototype._renderFilterBox = function($fieldContainer) {
  this.filterBox.render(this.$fieldContainer);
};
