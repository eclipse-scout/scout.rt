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
  scout.PageLayout.parent.call(this, outline);
  this.outline = outline;
  this.page = page;
};
scout.inherits(scout.PageLayout, scout.AbstractLayout);

scout.PageLayout.prototype.layout = function($container) {
  var containerSize, detailMenuBarSize, formTop,
    htmlContainer = this.page.htmlComp,
    $text = this.page.$node.children('.text'),
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
    titleHeight = $text.outerHeight(true);
    this.outline.detailContent.htmlComp.setSize(new scout.Dimension(containerSize.width, containerSize.height - titleHeight - detailMenuBarHeight));
  }
};

scout.PageLayout.prototype.preferredLayoutSize = function($container) {
  var prefSize,
    htmlContainer = this.page.htmlComp,
    formPrefSize = new scout.Dimension(),
    $text = this.page.$node.children('.text'),
    titleHeight = 0,
    detailMenuBar = this.outline.detailMenuBar,
    detailMenuBarPrefSize = new scout.Dimension();

  titleHeight = scout.graphics.prefSize($text, true).height;
  if (detailMenuBar.visible) {
    detailMenuBarPrefSize = detailMenuBar.htmlComp.getPreferredSize();
  }
  if (this.outline.detailContent) {
    formPrefSize = this.outline.detailContent.htmlComp.getPreferredSize();
  }

  prefSize = new scout.Dimension(Math.max(formPrefSize.width, detailMenuBarPrefSize.width), titleHeight + detailMenuBarPrefSize.height + formPrefSize.height);
  prefSize = prefSize.add(htmlContainer.getInsets());
  return prefSize;
};
