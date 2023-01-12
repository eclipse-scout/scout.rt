/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {AccordionLayout, HtmlComponent, RowLayoutOptions, TileAccordion, TileGrid, TileGridLayout} from '../../index';

export class TileAccordionLayout extends AccordionLayout {
  tileAccordion: TileAccordion;

  constructor(tileAccordion: TileAccordion, options?: RowLayoutOptions) {
    super(options);
    this.tileAccordion = tileAccordion;
  }

  override layout($container: JQuery) {
    let previousGroupHeights = this.tileAccordion.groups
      .map(group => group.body)
      .map(tileGrid => this._getTileGridHeight(tileGrid));

    super.layout($container);
    this._updateFilterFieldMaxWidth($container);

    this.tileAccordion.groups
      .map(group => group.body)
      .forEach((tileGrid, index) => this._updateTileGridViewPort(tileGrid, previousGroupHeights[index]));
  }

  protected _updateTileGridViewPort(tileGrid: TileGrid, previousHeight: number) {
    if (!tileGrid.rendered || !tileGrid.htmlComp || previousHeight <= 0) {
      return;
    }

    let newHeight = this._getTileGridHeight(tileGrid);
    if (previousHeight === newHeight && tileGrid.virtual) {
      // The viewPort of the virtual tileGrid has not been updated as no layout update was done for the grid because its height is unchanged.
      // But as there might be more space available in the accordion now (its height might have changed), enforce a viewPort update to ensure all necessary tiles are rendered.
      tileGrid.setViewRangeSize(tileGrid.calculateViewRangeSize(), false);
      (tileGrid.htmlComp.layout as TileGridLayout).updateViewPort();
    }
  }

  protected _getTileGridHeight(tileGrid: TileGrid): number {
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

  protected _updateFilterFieldMaxWidth($container: JQuery) {
    let htmlComp = HtmlComponent.get($container),
      width = htmlComp.availableSize().subtract(htmlComp.insets()).width;
    this.tileAccordion.$filterFieldContainer.css('--filter-field-max-width', (width * 0.6) + 'px');
  }
}
