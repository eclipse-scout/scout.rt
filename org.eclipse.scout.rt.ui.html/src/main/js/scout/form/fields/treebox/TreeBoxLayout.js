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
scout.TreeBoxLayout = function(treeBox, tree, filterBox) {
  scout.TreeBoxLayout.parent.call(this);
  this.tree = tree;
  this.filterBox = filterBox;
  this.treeBox = treeBox;
};
scout.inherits(scout.TreeBoxLayout, scout.AbstractLayout);

scout.TreeBoxLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    size = htmlContainer.size(),
    height = size.height,
    filterBoxHeight;

  if (this.filterBox && this.filterBox.rendered && this.filterBox.$container.isVisible()) {
    filterBoxHeight = scout.HtmlComponent.get(this.filterBox.$container).prefSize().height;
    height -= filterBoxHeight;
  }

  height = Math.max(height, 20);
  var htmlTree = scout.HtmlComponent.get(this.tree.$container);
  htmlTree.setSize(new scout.Dimension(size.width, height));

  if (this.filterBox && this.filterBox.rendered && this.filterBox.$container.isVisible()) {
    var htmlFilterBox = scout.HtmlComponent.get(this.filterBox.$container);
    htmlFilterBox.setSize(new scout.Dimension(size.width, filterBoxHeight));
  }
};

scout.TreeBoxLayout.prototype.preferredLayoutSize = function($container) {
  var prefSizeTree, prefSizeFilterBox, treeContainer, filterContainer,
    width = 0,
    htmlContainer = scout.HtmlComponent.get($container),
    height = scout.HtmlEnvironment.formRowHeight,
    treeBox = this.treeBox;
  if (treeBox.$label && treeBox.labelVisible) {
    width += scout.HtmlEnvironment.fieldLabelWidth;
  }
  if (treeBox.$mandatory && treeBox.$mandatory.isVisible()) {
    width += treeBox.$mandatory.outerWidth(true);
  }
  if (treeBox.$status && treeBox.statusVisible) {
    width += treeBox.$status.outerWidth(true);
  }

  // size of tree and size of filterBox
  treeContainer = scout.HtmlComponent.optGet(this.tree.$container);
  if (treeContainer) {
    prefSizeTree = treeContainer.prefSize()
      .add(htmlContainer.insets())
      .add(treeContainer.margins());
  } else {
    prefSizeTree = this.naturalSize(treeBox);
  }

  filterContainer = scout.HtmlComponent.optGet(this.filterBox.$container);
  if (filterContainer) {
    prefSizeFilterBox = filterContainer.prefSize()
      .add(htmlContainer.insets())
      .add(filterContainer.margins());
  } else {
    prefSizeTree = this.naturalSize(treeBox);
  }

  width += Math.max(prefSizeTree.width, prefSizeFilterBox.width);
  height = Math.max(height, prefSizeTree.height + prefSizeFilterBox.height);

  return new scout.Dimension(width, height);

};

scout.TreeBoxLayout.prototype.naturalSize = function(formField) {
  return new scout.Dimension(formField.$fieldContainer.width(), formField.$fieldContainer.height());
};
