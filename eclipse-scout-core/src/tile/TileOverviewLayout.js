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
import {AbstractLayout, graphics, HtmlComponent, Dimension} from '../index';

export default class TileOverviewLayout extends AbstractLayout {

  constructor(tileOverview) {
    super();
    this.tileOverview = tileOverview;
  }

  layout($container) {
    var htmlContainer = this.tileOverview.htmlComp;
    var pageTileGrid = this.tileOverview.pageTileGrid;
    var $content = this.tileOverview.$content;

    var containerSize = htmlContainer.availableSize()
      .subtract(htmlContainer.insets());
    var contentSize = containerSize.subtract(graphics.insets($content, {
      includeMargin: true
    }));

    // layout group-box and menu-bar (optional)
    var htmlRootGb = this._htmlRootGroupBox();
    if (htmlRootGb) {
      var rootGbSize = containerSize.subtract(htmlRootGb.margins());
      var rootGbPrefSize = htmlRootGb.prefSize();
      htmlRootGb.setSize(new Dimension(rootGbSize.width, rootGbPrefSize.height));
    }

    // layout tile-grid
    var htmlTileGrid = pageTileGrid.htmlComp;
    var tilesPrefSize = pageTileGrid.htmlComp.prefSize({
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
    var $rootGroupBox = this.tileOverview.$container.children('.root-group-box');
    if ($rootGroupBox.length) {
      return HtmlComponent.get($rootGroupBox);
    }
    return null;
  }
}
