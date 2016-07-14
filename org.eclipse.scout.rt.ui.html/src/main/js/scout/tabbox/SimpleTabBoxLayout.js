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
scout.SimpleTabBoxLayout = function(tabBox) {
  scout.SimpleTabBoxLayout.parent.call(this);
  this.tabBox = tabBox;
};
scout.inherits(scout.SimpleTabBoxLayout, scout.AbstractLayout);

scout.SimpleTabBoxLayout.prototype.layout = function($container) {
  var containerSize, viewContentSize,
    htmlContainer = scout.HtmlComponent.get($container),

    htmlViewContent = scout.HtmlComponent.get(this.tabBox.$viewContent),
    tabAreaSize ;

  containerSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets());

  tabAreaSize = this._layoutTabArea(containerSize);

  viewContentSize = containerSize.subtract(htmlViewContent.getMargins());
  viewContentSize.height -= tabAreaSize.height;
  htmlViewContent.setSize(viewContentSize);

};

/**
 *
 * @param containerSize
 * @returns {@link {@link scout.Dimension}} used of the tab area
 */
scout.SimpleTabBoxLayout.prototype._layoutTabArea = function(containerSize) {
  if (!this.tabBox.rendered) {
    return new scout.Dimension(0,0);
  }
  // exprected the tab area is layouted dynamically only
  var htmlViewTabs = scout.HtmlComponent.get(this.tabBox.$tabArea),
    prefSize = htmlViewTabs.getPreferredSize(),
    margins = htmlViewTabs.getMargins();
  var size = new scout.Dimension(containerSize.width, prefSize.height + margins.top + margins.bottom);
  htmlViewTabs.setSize(size);
  return  size;
};

/**
 * Preferred size of the tab-box aligns every tab-item in a single line, so that each item is visible.
 */
scout.SimpleTabBoxLayout.prototype.preferredLayoutSize = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlViewContent = scout.HtmlComponent.get(this.tabBox.$viewContent),
    htmlViewTabs = scout.HtmlComponent.get(this.tabBox.$tabArea),
    viewTabsSize = new scout.Dimension(),
    viewContentSize = new scout.Dimension();

  if (htmlViewTabs.isVisible()) {
    viewTabsSize = htmlViewTabs.getPreferredSize()
      .add(htmlViewTabs.getMargins());
  }

  viewContentSize = htmlViewContent.getPreferredSize()
    .add(htmlContainer.getInsets())
    .add(htmlViewContent.getMargins());

  return new scout.Dimension(
    Math.max(viewTabsSize.width, viewContentSize.width),
    viewContentSize.height + viewTabsSize.height);
};
