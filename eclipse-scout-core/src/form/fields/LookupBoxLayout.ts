/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Dimension, FormField, HtmlComponent, HtmlCompPrefSizeOptions, HtmlEnvironment, LookupBox, Widget} from '../../index';

export class LookupBoxLayout extends AbstractLayout {
  box: LookupBox<any>;
  structure: Widget;
  filterBox: Widget;

  constructor(box: LookupBox<any>, structure: Widget, filterBox: Widget) {
    super();
    this.box = box;
    this.structure = structure;
    this.filterBox = filterBox;
  }

  override layout($container: JQuery) {
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

  override preferredLayoutSize($container: JQuery, options?: HtmlCompPrefSizeOptions): Dimension {
    options = options || {};
    let prefSizeStructure: Dimension,
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
    let structureContainer = HtmlComponent.optGet(this.structure.$container);
    if (structureContainer) {
      prefSizeStructure = structureContainer.prefSize(options)
        .add(htmlContainer.insets())
        .add(structureContainer.margins());
    } else {
      prefSizeStructure = this.naturalSize(box);
    }

    let prefSizeFilterBox = new Dimension(0, 0);
    if (this.filterBox) {
      let filterContainer = HtmlComponent.optGet(this.filterBox.$container);
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

  naturalSize(formField: FormField): Dimension {
    return new Dimension(formField.$fieldContainer.width(), formField.$fieldContainer.height());
  }
}
