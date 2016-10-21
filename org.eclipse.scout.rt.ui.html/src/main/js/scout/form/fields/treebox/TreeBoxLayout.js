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
scout.TreeBoxLayout = function(treeBox, tree, filterBox) {
  scout.TreeBoxLayout.parent.call(this);
  this.tree = tree;
  this.filterBox = filterBox;
  this.treeBox = treeBox;
};
scout.inherits(scout.TreeBoxLayout, scout.AbstractLayout);

scout.TreeBoxLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    size = htmlContainer.getSize(),
    height = size.height;

  if (this.filterBox && this.filterBox.rendered && this.filterBox.$container.isVisible()) {
    height -= scout.HtmlEnvironment.formRowHeight;
  }

  var htmlTree = scout.HtmlComponent.get(this.tree.$container);
  htmlTree.setSize(new scout.Dimension(size.width, height));

  if (this.filterBox && this.filterBox.rendered && this.filterBox.$container.isVisible()) {
    var htmlFilterBox = scout.HtmlComponent.get(this.filterBox.$container);
    htmlFilterBox.setSize(new scout.Dimension(size.width, scout.HtmlEnvironment.formRowHeight));
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
  if (treeBox.$mandatory) {
    width += treeBox.$mandatory.outerWidth(true);
  }
  if (treeBox.$status && treeBox.statusVisible) {
    width += treeBox.$status.outerWidth(true);
  }

  // getSize of tree and size of filterBox
  treeContainer = scout.HtmlComponent.optGet(this.tree.$container);
  if (treeContainer) {
    prefSizeTree = treeContainer.getPreferredSize()
      .add(htmlContainer.getInsets())
      .add(treeContainer.getMargins());
  } else {
    prefSizeTree = this.naturalSize(treeBox);
  }

  filterContainer = scout.HtmlComponent.optGet(this.filterBox.$container);
  if (filterContainer) {
    prefSizeFilterBox = filterContainer.getPreferredSize()
      .add(htmlContainer.getInsets())
      .add(filterContainer.getMargins());
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
