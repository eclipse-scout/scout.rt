/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.LookupBoxLayout = function(box, structure, filterBox) {
  scout.LookupBoxLayout.parent.call(this);
  this.box = box;
  this.structure = structure;
  this.filterBox = filterBox;
};
scout.inherits(scout.LookupBoxLayout, scout.AbstractLayout);

scout.LookupBoxLayout.prototype.layout = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    size = htmlContainer.size(),
    height = size.height,
    filterBoxHeight;

  if (this.filterBox && this.filterBox.rendered) {
    filterBoxHeight = scout.HtmlComponent.get(this.filterBox.$container).prefSize().height;
    height -= filterBoxHeight;
  }

  height = Math.max(height, 20);
  var htmlStructure = scout.HtmlComponent.get(this.structure.$container);
  htmlStructure.setSize(new scout.Dimension(size.width, height));

  if (this.filterBox && this.filterBox.rendered) {
    var htmlFilterBox = scout.HtmlComponent.get(this.filterBox.$container);
    htmlFilterBox.setSize(new scout.Dimension(size.width, filterBoxHeight));
  }
};

scout.LookupBoxLayout.prototype.preferredLayoutSize = function($container, options) {
  options = options || {};
  var prefSizeStructure, prefSizeFilterBox, structureContainer, filterContainer,
    width = 0,
    htmlContainer = scout.HtmlComponent.get($container),
    height = scout.htmlEnvironment.formRowHeight,
    box = this.box;

  // HeightHint not supported
  options.heightHint = null;

  if (box.$label && box.labelVisible) {
    width += scout.htmlEnvironment.fieldLabelWidth;
  }
  if (box.$mandatory && box.$mandatory.isVisible()) {
    width += box.$mandatory.outerWidth(true);
  }
  if (box.$status && box.statusVisible) {
    width += box.$status.outerWidth(true);
  }

  // size of table and size of filterBox
  structureContainer = scout.HtmlComponent.optGet(this.structure.$container);
  if (structureContainer) {
    prefSizeStructure = structureContainer.prefSize(options)
      .add(htmlContainer.insets())
      .add(structureContainer.margins());
  } else {
    prefSizeStructure = this.naturalSize(box);
  }

  prefSizeFilterBox = new scout.Dimension(0, 0);
  if (this.filterBox) {
    filterContainer = scout.HtmlComponent.optGet(this.filterBox.$container);
    if (filterContainer) {
      prefSizeFilterBox = filterContainer.prefSize(options)
        .add(htmlContainer.insets())
        .add(filterContainer.margins());
    }
  }

  width += Math.max(prefSizeStructure.width, prefSizeFilterBox.width);
  height = Math.max(height, prefSizeStructure.height + prefSizeFilterBox.height);

  return new scout.Dimension(width, height);
};

scout.LookupBoxLayout.prototype.naturalSize = function(formField) {
  return new scout.Dimension(formField.$fieldContainer.width(), formField.$fieldContainer.height());
};
