/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.TabBoxLayout = function(tabBox) {
  scout.TabBoxLayout.parent.call(this);
  this._tabBox = tabBox;
  this._statusWidth = scout.HtmlEnvironment.fieldStatusWidth;
};
scout.inherits(scout.TabBoxLayout, scout.AbstractLayout);

scout.TabBoxLayout.prototype.layout = function($container) {
  var containerSize, tabContentSize, tabAreaMargins, innerTabAreaSize,
    htmlContainer = scout.HtmlComponent.get($container),
    htmlTabContent = scout.HtmlComponent.get(this._tabBox._$tabContent),
    htmlTabArea = scout.HtmlComponent.get(this._tabBox._$tabArea),
    tabAreaWidth = 0,
    tabAreaHeight = 0,
    tabAreaSize = new scout.Dimension(),
    $status = this._tabBox.$status,
    statusPosition = this._tabBox.statusPosition;

  containerSize = htmlContainer.availableSize()
    .subtract(htmlContainer.insets());

  if (htmlTabArea.isVisible()) {
    tabAreaMargins = htmlTabArea.margins();
    tabAreaHeight = htmlTabArea.prefSize().height;
    tabAreaWidth = containerSize.subtract(tabAreaMargins).width;
    if ($status && $status.isVisible()) {
      this._layoutStatus();
      if (statusPosition === scout.FormField.StatusPosition.DEFAULT) {
        tabAreaWidth -= $status.outerWidth(true);
      }
    }
    innerTabAreaSize = new scout.Dimension(tabAreaWidth, tabAreaHeight);
    htmlTabArea.setSize(innerTabAreaSize);
    tabAreaSize = innerTabAreaSize.add(tabAreaMargins);
  }

  tabContentSize = containerSize.subtract(htmlTabContent.margins());
  tabContentSize.height -= tabAreaSize.height;
  htmlTabContent.setSize(tabContentSize);
};

scout.TabBoxLayout.prototype._layoutStatus = function() {
  var htmlContainer = this._tabBox.htmlComp,
    containerPadding = htmlContainer.insets({
      includeBorder: false
    }),
    top = containerPadding.top,
    right = containerPadding.right,
    $tabArea = this._tabBox._$tabArea,
    tabAreaInnerHeight = $tabArea.innerHeight(),
    $status = this._tabBox.$status,
    statusMargins = scout.graphics.margins($status),
    statusTop = top,
    statusPosition = this._tabBox.statusPosition,
    statusHeight = tabAreaInnerHeight - statusMargins.vertical();

  if (statusPosition === scout.FormField.StatusPosition.DEFAULT) {
    statusTop += $tabArea.cssMarginTop();
  } else {
    statusHeight -= $status.cssBorderWidthY(); // status has a transparent border to align icon with text
  }

  $status.cssWidth(this._statusWidth)
    .cssTop(statusTop)
    .cssRight(right)
    .cssHeight(statusHeight)
    .cssLineHeight(statusHeight);
};

/**
 * Preferred size of the tab-box aligns every tab-item in a single line, so that each item is visible.
 */
scout.TabBoxLayout.prototype.preferredLayoutSize = function($container) {
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlTabContent = scout.HtmlComponent.get(this._tabBox._$tabContent),
    htmlTabArea = scout.HtmlComponent.get(this._tabBox._$tabArea),
    tabAreaSize = new scout.Dimension(),
    tabContentSize = new scout.Dimension();

  if (htmlTabArea.isVisible()) {
    tabAreaSize = htmlTabArea.prefSize()
      .add(htmlTabArea.margins());
  }

  tabContentSize = htmlTabContent.prefSize()
    .add(htmlContainer.insets())
    .add(htmlTabContent.margins());

  return new scout.Dimension(
    Math.max(tabAreaSize.width, tabContentSize.width),
    tabContentSize.height + tabAreaSize.height);
};
