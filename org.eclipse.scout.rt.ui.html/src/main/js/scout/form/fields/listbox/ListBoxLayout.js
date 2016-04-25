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
scout.ListBoxLayout = function(listBox, table, filterBox) {
  scout.ListBoxLayout.parent.call(this);
  this.table = table;
  this.filterBox = filterBox;
  this.listBox = listBox;
};
scout.inherits(scout.ListBoxLayout, scout.AbstractLayout);

scout.ListBoxLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    size = htmlContainer.getSize(),
    height = size.height,
    minHeight = 0,
    heightCalculated;

  this.pixelBasedSizing = true;

  if (this.filterBox && this.filterBox.rendered && this.filterBox.$container.isVisible()) {
    heightCalculated = scout.HtmlComponent.get(this.filterBox.$container).getPreferredSize().height ;
    height -= heightCalculated;
  }
  height = Math.max(height, 20);
  var htmlTable = scout.HtmlComponent.get(this.table.$container);
  htmlTable.pixelBasedSizing = true;
  htmlTable.setSize(new scout.Dimension(size.width, height));

  if (this.filterBox && this.filterBox.rendered && this.filterBox.$container.isVisible()) {
    var htmlFilterBox = scout.HtmlComponent.get(this.filterBox.$container);
    htmlFilterBox.setSize(new scout.Dimension(size.width, Math.max(minHeight, heightCalculated)));
  }
};

scout.ListBoxLayout.prototype.preferredLayoutSize = function($container) {
  var prefSizeTable, prefSizeFilterBox, tableContainer, filterContainer,
    width = 0,
    htmlContainer = scout.HtmlComponent.get($container),
    height = scout.HtmlEnvironment.formRowHeight,
    listBox = this.listBox;
  if (listBox.$label && listBox.labelVisible) {
    width += scout.HtmlEnvironment.fieldLabelWidth;
  }
  if (listBox.$mandatory) {
    width += listBox.$mandatory.outerWidth(true);
  }
  if (listBox.$status && listBox.statusVisible) {
    width += listBox.$status.outerWidth(true);
  }

  // getSize of table and size of filterBox
  tableContainer = scout.HtmlComponent.optGet(this.table.$container);
  if (tableContainer) {
    prefSizeTable = tableContainer.getPreferredSize()
      .add(htmlContainer.getInsets())
      .add(tableContainer.getMargins());
  } else {
    prefSizeTable = this.naturalSize(listBox);
  }

  filterContainer = scout.HtmlComponent.optGet(this.filterBox.$container);
  if (filterContainer) {
    prefSizeFilterBox = filterContainer.getPreferredSize()
      .add(htmlContainer.getInsets())
      .add(filterContainer.getMargins());
  } else {
    prefSizeTable = this.naturalSize(listBox);
  }

  width += Math.max(prefSizeTable.width, prefSizeFilterBox.width);
  height = Math.max(height, prefSizeTable.height + prefSizeFilterBox.height);

  return new scout.Dimension(width, height);

};

scout.ListBoxLayout.prototype.naturalSize = function(formField) {
  return new scout.Dimension(formField.$fieldContainer.width(), formField.$fieldContainer.height());
};
