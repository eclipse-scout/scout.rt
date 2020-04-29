/*
 * Copyright (c) 2014-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, Dimension, graphics, HtmlComponent} from '../index';

export default class TileOverviewLayout extends AbstractLayout {

  constructor(tileOverview) {
    super();
    this.tileOverview = tileOverview;
  }

  layout($container) {
    let htmlContainer = this.tileOverview.htmlComp;
    let pageTileGrid = this.tileOverview.pageTileGrid;
    let $content = this.tileOverview.$content;

    let containerSize = htmlContainer.availableSize()
      .subtract(htmlContainer.insets());
    let contentSize = containerSize.subtract(graphics.insets($content, {
      includeMargin: true
    }));

    // layout group-box and menu-bar (optional)
    let htmlRootGb = this._htmlRootGroupBox();
    if (htmlRootGb) {
      let rootGbSize = containerSize.subtract(htmlRootGb.margins());
      let rootGbPrefSize = htmlRootGb.prefSize();
      htmlRootGb.setSize(new Dimension(rootGbSize.width, rootGbPrefSize.height));
    }

    // layout tile-grid
    let htmlTileGrid = pageTileGrid.htmlComp;
    let tilesPrefSize = pageTileGrid.htmlComp.prefSize({
      widthHint: contentSize.width
    });
    htmlTileGrid.setSize(tilesPrefSize);
  }

  /**
   * May return null when there is no root group box, which is the case when this layout is used by OutlineOverview.js.
   * TileOverviewForm.js has always a group-box and this group-box needs to be layouted.
   * @returns {*}
   */
  _htmlRootGroupBox() {
    let $rootGroupBox = this.tileOverview.$container.children('.root-group-box');
    if ($rootGroupBox.length) {
      return HtmlComponent.get($rootGroupBox);
    }
    return null;
  }
}
