/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
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
  var pageTiles = this.tileOutlineOverview.pageTiles;
  var $content = this.tileOutlineOverview.$content;
  var contentSize = htmlContainer.availableSize()
    .subtract(htmlContainer.insets())
    .subtract(scout.graphics.insets($content, {
      includeMargin: true
    }));

  var htmlTiles = pageTiles.htmlComp;
  var tilesSize = contentSize.subtract(htmlTiles.margins());
  var tilesPrefSize = pageTiles.htmlComp.layout.prefSizeForWidth(tilesSize.width);
  htmlTiles.setSize(tilesPrefSize);
  this._positionContent();
};

/**
 * Positions the content in the middle of the container
 */
scout.TileOutlineOverviewLayout.prototype._positionContent = function() {
  var contentTop, contentLeft,
    $container = this.tileOutlineOverview.$container,
    $content = this.tileOutlineOverview.$content,
    $title = this.tileOutlineOverview.$title,
    containerSize = scout.graphics.size($container),
    contentSize = scout.graphics.size($content, true),
    titleSize = scout.graphics.size($title, true);

  contentTop = containerSize.height / 2 - (contentSize.height + titleSize.height) / 2;
  contentTop = Math.max(contentTop, 0);
  contentLeft = containerSize.width / 2 - contentSize.width / 2;
  contentLeft = Math.max(contentLeft, 0);
  $content.cssTop(contentTop);
  $content.cssLeft(contentLeft);
};
