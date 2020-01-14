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
import {AbstractLayout, graphics} from '../index';

export default class TileOverviewLayout extends AbstractLayout {

  constructor(tileOverview) {
    super();
    this.tileOverview = tileOverview;
  }

  layout($container) {
    var htmlContainer = this.tileOverview.htmlComp;
    var pageTileGrid = this.tileOverview.pageTileGrid;
    var $content = this.tileOverview.$content;
    var contentSize = htmlContainer.availableSize()
      .subtract(htmlContainer.insets())
      .subtract(graphics.insets($content, {
        includeMargin: true
      }));

    var htmlTileGrid = pageTileGrid.htmlComp;
    var tilesPrefSize = pageTileGrid.htmlComp.prefSize({
      widthHint: contentSize.width
    });
    htmlTileGrid.setSize(tilesPrefSize);
  }
}
