/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.PageLayout = function(outline, page) {
  scout.PageLayout.parent.call(this);
  this.outline = outline;
  this.page = page;
};
scout.inherits(scout.PageLayout, scout.AbstractLayout);

scout.PageLayout.prototype.layout = function($container) {
  var containerSize, detailMenuBarSize,
    htmlContainer = this.page.htmlComp,
    $text = this.page.$text,
    $icon = this.page.$icon(),
    titleHeight = 0,
    nodeMenuBar = this.outline.nodeMenuBar,
    nodeMenuBarWidth = 0,
    detailMenuBar = this.outline.detailMenuBar,
    detailMenuBarHeight = 0;

  containerSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets());

  if (nodeMenuBar.visible) {
    nodeMenuBarWidth = nodeMenuBar.htmlComp.getPreferredSize().width;
    $text.cssWidth(containerSize.width - nodeMenuBarWidth);
  }

  if (detailMenuBar.visible) {
    detailMenuBarHeight = detailMenuBar.htmlComp.getPreferredSize().height;
    detailMenuBarSize = new scout.Dimension(containerSize.width, detailMenuBarHeight)
      .subtract(detailMenuBar.htmlComp.getMargins());
    detailMenuBar.htmlComp.setSize(detailMenuBarSize);
  }

  if (this.outline.detailContent) {
    titleHeight = Math.max($text.outerHeight(true), $icon.outerHeight(true));
    this.outline.detailContent.htmlComp.setSize(new scout.Dimension(containerSize.width, containerSize.height - titleHeight - detailMenuBarHeight));
  }
};

scout.PageLayout.prototype.preferredLayoutSize = function($container) {
  var prefSize, containerSize, textHeight, iconHeight,
    htmlContainer = this.page.htmlComp,
    detailContentPrefSize = new scout.Dimension(),
    $text = this.page.$text,
    $icon = this.page.$icon(),
    titlePrefHeight = 0,
    detailMenuBar = this.outline.detailMenuBar,
    detailMenuBarPrefSize = new scout.Dimension(),
    nodeMenuBar = this.outline.nodeMenuBar,
    nodeMenuBarWidth = 0;

  containerSize = htmlContainer.getSize()
    .subtract(htmlContainer.getInsets());

  if (nodeMenuBar.visible) {
    nodeMenuBarWidth = nodeMenuBar.htmlComp.getPreferredSize().width;
  }

  // needs a width to be able to calculate the pref height -> container width needs to be correct already
  textHeight = scout.graphics.prefSize($text, {
    includeMargin: true,
    widthHint: containerSize.width - nodeMenuBarWidth
  }).height;
  iconHeight = $icon.outerHeight(true);
  titlePrefHeight = Math.max(textHeight, iconHeight);

  if (detailMenuBar.visible) {
    detailMenuBarPrefSize = detailMenuBar.htmlComp.getPreferredSize();
  }
  if (this.outline.detailContent) {
    // Table row detail may contain wrapped text as well, but since it uses the full width there is no need to give a width hint
    detailContentPrefSize = this.outline.detailContent.htmlComp.getPreferredSize();
  }

  prefSize = new scout.Dimension(Math.max(detailContentPrefSize.width, detailMenuBarPrefSize.width), titlePrefHeight + detailMenuBarPrefSize.height + detailContentPrefSize.height);
  prefSize = prefSize.add(htmlContainer.getInsets());
  return prefSize;
};
