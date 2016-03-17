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
  var containerSize, menuBarSize, formTop,
    htmlContainer = this.page.htmlComp,
    $title = this.page.$node.children('.text'),
    titleHeight = 0,
    menuBar = this.outline.detailMenuBar,
    menuBarHeight = 0,
    htmlMenuBar = menuBar.htmlComp;

  containerSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets());

  if (menuBar.visible) {
    menuBarHeight = htmlMenuBar.getPreferredSize().height;
    menuBarSize = new scout.Dimension(containerSize.width, menuBarHeight)
      .subtract(htmlMenuBar.getMargins());
    htmlMenuBar.setSize(menuBarSize);
  }

  if (this.outline.detailContent) {
    titleHeight = $title.outerHeight(true);
    this.outline.detailContent.htmlComp.setSize(new scout.Dimension(containerSize.width, containerSize.height - titleHeight - menuBarHeight));
  }
};

scout.PageLayout.prototype.preferredLayoutSize = function($container) {
  var prefSize,
    htmlContainer = this.page.htmlComp,
    formPrefSize = new scout.Dimension(),
    $title = this.page.$node.children('.text'),
    titleHeight = 0,
    menuBar = this.outline.detailMenuBar,
    menuBarPrefSize= new scout.Dimension(),
    htmlMenuBar = menuBar.htmlComp;

  titleHeight = $title.outerHeight(true);
  if (menuBar.visible) {
    menuBarPrefSize = htmlMenuBar.getPreferredSize();
  }
  if (this.outline.detailContent) {
    formPrefSize = this.outline.detailContent.htmlComp.getPreferredSize();
  }

  prefSize = new scout.Dimension(Math.max(formPrefSize.width, menuBarPrefSize.width), titleHeight + menuBarPrefSize.height + formPrefSize.height);
  prefSize = prefSize.add(htmlContainer.getInsets());
  return prefSize;
};
