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
import {AbstractLayout, Dimension, HtmlComponent, HtmlEnvironment} from '../../index';

export default class LookupBoxLayout extends AbstractLayout {

  constructor(box, structure, filterBox) {
    super();
    this.box = box;
    this.structure = structure;
    this.filterBox = filterBox;
  }

  layout($container) {
    let htmlContainer = HtmlComponent.get($container),
      size = htmlContainer.size(),
      height = size.height,
      filterBoxHeight;

    if (this.filterBox && this.filterBox.rendered) {
      filterBoxHeight = HtmlComponent.get(this.filterBox.$container).prefSize().height;
      height -= filterBoxHeight;
    }

    height = Math.max(height, 20);
    let htmlStructure = HtmlComponent.get(this.structure.$container);
    htmlStructure.setSize(new Dimension(size.width, height));

    if (this.filterBox && this.filterBox.rendered) {
      let htmlFilterBox = HtmlComponent.get(this.filterBox.$container);
      htmlFilterBox.setSize(new Dimension(size.width, filterBoxHeight));
    }
  }

  preferredLayoutSize($container, options) {
    options = options || {};
    let prefSizeStructure, prefSizeFilterBox, structureContainer, filterContainer,
      width = 0,
      htmlContainer = HtmlComponent.get($container),
      height = HtmlEnvironment.get().formRowHeight,
      box = this.box;

    // HeightHint not supported
    options.heightHint = null;

    if (box.$label && box.labelVisible) {
      width += HtmlEnvironment.get().fieldLabelWidth;
    }
    if (box.$mandatory && box.$mandatory.isVisible()) {
      width += box.$mandatory.outerWidth(true);
    }
    if (box.$status && box.statusVisible) {
      width += box.$status.outerWidth(true);
    }

    // size of table and size of filterBox
    structureContainer = HtmlComponent.optGet(this.structure.$container);
    if (structureContainer) {
      prefSizeStructure = structureContainer.prefSize(options)
        .add(htmlContainer.insets())
        .add(structureContainer.margins());
    } else {
      prefSizeStructure = this.naturalSize(box);
    }

    prefSizeFilterBox = new Dimension(0, 0);
    if (this.filterBox) {
      filterContainer = HtmlComponent.optGet(this.filterBox.$container);
      if (filterContainer) {
        prefSizeFilterBox = filterContainer.prefSize(options)
          .add(htmlContainer.insets())
          .add(filterContainer.margins());
      }
    }

    width += Math.max(prefSizeStructure.width, prefSizeFilterBox.width);
    height = Math.max(height, prefSizeStructure.height + prefSizeFilterBox.height);

    return new Dimension(width, height);
  }

  naturalSize(formField) {
    return new Dimension(formField.$fieldContainer.width(), formField.$fieldContainer.height());
  }
}
