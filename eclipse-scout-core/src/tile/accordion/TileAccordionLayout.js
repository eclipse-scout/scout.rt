/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import {AccordionLayout, HtmlComponent} from '../../index';

export default class TileAccordionLayout extends AccordionLayout {

  constructor(tileAccordion, options) {
    super(options);
    this.tileAccordion = tileAccordion;
  }

  layout($container) {
    let previousGroupHeights = this.tileAccordion.groups
      .map(group => group.body)
      .map(tileGrid => this._getTileGridHeight(tileGrid));

    super.layout($container);
    this._updateFilterFieldMaxWidth($container);

    this.tileAccordion.groups
      .map(group => group.body)
      .forEach((tileGrid, index) => this._updateTileGridViewPort(tileGrid, previousGroupHeights[index]));
  }

  _updateTileGridViewPort(tileGrid, previousHeight) {
    if (!tileGrid.rendered || !tileGrid.htmlComp || previousHeight <= 0) {
      return;
    }

    let newHeight = this._getTileGridHeight(tileGrid);
    if (previousHeight === newHeight && tileGrid.virtual) {
      // The viewPort of the virtual tileGrid has not been updated as no layout update was done for the grid because its height is unchanged.
      // But as there might be more space available in the accordion now (its height might have changed), enforce a viewPort update to ensure all necessary tiles are rendered.
      tileGrid.setViewRangeSize(tileGrid.calculateViewRangeSize(), false);
      tileGrid.htmlComp.layout.updateViewPort();
    }
  }

  _getTileGridHeight(tileGrid) {
    if (!tileGrid) {
      return 0;
    }
    let htmlComp = tileGrid.htmlComp;
    if (!htmlComp) {
      return 0;
    }
    let size = tileGrid.htmlComp.sizeCached;
    if (!size) {
      return 0;
    }
    return size.height;
  }

  _updateFilterFieldMaxWidth($container) {
    let htmlComp = HtmlComponent.get($container),
      width = htmlComp.availableSize().subtract(htmlComp.insets()).width;
    this.tileAccordion.$filterFieldContainer.css('--filter-field-max-width', (width * 0.6) + 'px');
  }
}
