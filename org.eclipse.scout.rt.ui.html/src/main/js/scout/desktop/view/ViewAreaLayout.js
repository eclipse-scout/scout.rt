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
scout.ViewAreaLayout = function(viewArea) {
  scout.ViewAreaLayout.parent.call(this);
  this.viewArea = viewArea;
};
scout.inherits(scout.ViewAreaLayout, scout.AbstractLayout);

scout.ViewAreaLayout.prototype.layout = function($container) {
  var containerSize, viewContentSize, viewTabsMargins, innerViewTabsSize,
    htmlContainer = scout.HtmlComponent.get($container),
    htmlViewTabs = scout.HtmlComponent.get(this.viewArea.$viewTabArea),
    htmlViewContent = scout.HtmlComponent.get(this.viewArea.$viewContent),
    viewTabsWidth = 0,
    viewTabsHeight = 0,
    viewTabsSize = new scout.Dimension();
//    $status = this._tabBox.$status,
//    statusPosition = this._tabBox.statusPosition;

  containerSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets());

  if (htmlViewTabs.isVisible()) {
    viewTabsMargins = htmlViewTabs.getMargins();
    viewTabsHeight = htmlViewTabs.getPreferredSize().height;
    viewTabsWidth = containerSize.subtract(viewTabsMargins).width;
//    if ($status && $status.isVisible()) {
//      this._layoutStatus();
//      if (statusPosition === scout.FormField.STATUS_POSITION_DEFAULT) {
//        viewTabsWidth -= $status.outerWidth(true);
//      }
//    }
    innerViewTabsSize = new scout.Dimension(viewTabsWidth, viewTabsHeight);
    htmlViewTabs.setSize(innerViewTabsSize);
    viewTabsSize = innerViewTabsSize.add(viewTabsMargins);
  }

  viewContentSize = containerSize.subtract(htmlViewContent.getMargins());
  viewContentSize.height -= viewTabsSize.height;
  htmlViewContent.setSize(viewContentSize);
};

//scout.ViewAreaLayout.prototype._layoutStatus = function() {
//  var htmlContainer = this._tabBox.htmlComp,
//    containerPadding = htmlContainer.getInsets({
//      includeBorder: false
//    }),
//    top = containerPadding.top,
//    right = containerPadding.right,
//    $tabArea = this._tabBox._$tabArea,
//    tabAreaInnerHeight = $tabArea.innerHeight();
//    $status = this._tabBox.$status,
//    statusMargins = scout.graphics.getMargins($status),
//    statusTop = top,
//    statusPosition = this._tabBox.statusPosition,
//    statusHeight = tabAreaInnerHeight - statusMargins.vertical();
//
//  if (statusPosition === scout.FormField.STATUS_POSITION_DEFAULT) {
//    statusTop += $tabArea.cssMarginTop();
//  } else {
//    statusHeight -= $status.cssBorderWidthY(); // status has a transparent border to align icon with text
//  }
//
//  $status.cssWidth(this._statusWidth)
//    .cssTop(statusTop)
//    .cssRight(right)
//    .cssHeight(statusHeight)
//    .cssLineHeight(statusHeight);
//};

/**
 * Preferred size of the tab-box aligns every tab-item in a single line, so that each item is visible.
 */
scout.ViewAreaLayout.prototype.preferredLayoutSize = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlViewContent = scout.HtmlComponent.get(this._tabBox._$tabContent),
    htmlViewTabs = scout.HtmlComponent.get(this._tabBox._$tabArea),
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
