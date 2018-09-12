/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TileOutlineOverviewLayout = function(tileOutlineOverview) {
  scout.TileOutlineOverviewLayout.parent.call(this);
  this.tileOutlineOverview = tileOutlineOverview;
};
scout.inherits(scout.TileOutlineOverviewLayout, scout.AbstractLayout);

scout.TileOutlineOverviewLayout.prototype.layout = function($container) {
  var htmlContainer = this.tileOutlineOverview.htmlComp;
  var pageTileGrid = this.tileOutlineOverview.pageTileGrid;
  var $content = this.tileOutlineOverview.$content;
  var contentSize = htmlContainer.availableSize()
    .subtract(htmlContainer.insets())
    .subtract(scout.graphics.insets($content, {
      includeMargin: true
    }));

  var htmlTileGrid = pageTileGrid.htmlComp;
  var tilesPrefSize = pageTileGrid.htmlComp.prefSize({
    widthHint: contentSize.width
  });
  htmlTileGrid.setSize(tilesPrefSize);
};
