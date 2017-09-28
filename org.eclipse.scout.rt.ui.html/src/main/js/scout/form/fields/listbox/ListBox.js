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
scout.ListBox = function() {
  scout.ListBox.parent.call(this);
  this.gridDataHints.weightY = 1.0;
  this.gridDataHints.h = 2;
  this._addWidgetProperties(['table', 'filterBox']);
};
scout.inherits(scout.ListBox, scout.ValueField);

scout.ListBox.prototype._init = function(model) {
  scout.ListBox.parent.prototype._init.call(this, model);
  if (this.filterBox) {
    this.filterBox.enabledComputed = true; // filter is always enabled
    this.filterBox.recomputeEnabled(true);
  }
};

scout.ListBox.prototype._render = function() {
  this.addContainer(this.$parent, 'list-box');
  this.addLabel();
  this.addMandatoryIndicator();
  this.addStatus();

  this.addFieldContainer(this.$parent.makeDiv());
  var htmlComp = scout.HtmlComponent.install(this.$fieldContainer, this.session);
  htmlComp.setLayout(new scout.ListBoxLayout(this, this.table, this.filterBox));

  if (this.table) {
    this._renderTable();
  }
  if (this.filterBox) {
    this._renderFilterBox();
    this.table.htmlComp.pixelBasedSizing = true;
  }
};

scout.ListBox.prototype._renderTable = function() {
  this.table.render(this.$fieldContainer);
  this.addField(this.table.$container);
  this.$field.addDeviceClass();
};

scout.ListBox.prototype._renderFilterBox = function() {
  this.filterBox.render(this.$fieldContainer);
};
