/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.TabBoxLayout = function(tabBox) {
  scout.TabBoxLayout.parent.call(this);
  this._tabBox = tabBox;

  this._initDefaults();

  this.htmlPropertyChangeHandler = this._onHtmlEnvironmenPropertyChange.bind(this);
  scout.HtmlEnvironment.on('propertyChange', this.htmlPropertyChangeHandler);
  this._tabBox.one('remove', function() {
    scout.HtmlEnvironment.off('propertyChange', this.htmlPropertyChangeHandler);
  }.bind(this));
};
scout.inherits(scout.TabBoxLayout, scout.AbstractLayout);

scout.TabBoxLayout.prototype._initDefaults = function() {
  this._statusWidth = scout.HtmlEnvironment.fieldStatusWidth;
};

scout.TabBoxLayout.prototype._onHtmlEnvironmenPropertyChange = function() {
  this._initDefaults();
  this._tabBox.invalidateLayoutTree();
};

scout.TabBoxLayout.prototype.layout = function($container) {
  var containerSize, tabContentSize, tabAreaMargins, innerTabAreaSize,
    htmlContainer = scout.HtmlComponent.get($container),
    htmlTabContent = scout.HtmlComponent.get(this._tabBox._$tabContent),
    htmlTabArea = scout.HtmlComponent.get(this._tabBox.header.$container),
    tabAreaWidthHint = 0,
    tabAreaSize = new scout.Dimension(),
    tooltip = this._tabBox._tooltip(),
    $status = this._tabBox.$status,
    statusPosition = this._tabBox.statusPosition;

  containerSize = htmlContainer.availableSize()
    .subtract(htmlContainer.insets());

  if (htmlTabArea.isVisible()) {
    tabAreaMargins = htmlTabArea.margins();
    tabAreaWidthHint = containerSize.subtract(tabAreaMargins).width;
    if ($status && $status.isVisible()) {
      this._layoutStatus();
      if (statusPosition === scout.FormField.StatusPosition.DEFAULT) {
        tabAreaWidthHint -= (this._statusWidth + scout.graphics.margins($status).horizontal());
      }
    }
    innerTabAreaSize = htmlTabArea.prefSize({
      widthHint: tabAreaWidthHint
    });

    if ($status && $status.isVisible()) {
      this._layoutStatus(innerTabAreaSize.height);
    }

    innerTabAreaSize.width = tabAreaWidthHint;
    htmlTabArea.setSize(innerTabAreaSize);
    tabAreaSize = innerTabAreaSize.add(tabAreaMargins);
  }

  tabContentSize = containerSize.subtract(htmlTabContent.margins());
  tabContentSize.height -= tabAreaSize.height;
  htmlTabContent.setSize(tabContentSize);

  // Make sure tooltip is at correct position after layouting, if there is one
  if (tooltip && tooltip.rendered) {
    tooltip.position();
  }
};

scout.TabBoxLayout.prototype._layoutStatus = function(height) {
  var htmlContainer = this._tabBox.htmlComp,
    containerPadding = htmlContainer.insets({
      includeBorder: false
    }),
    top = containerPadding.top,
    right = containerPadding.right,
    $tabArea = this._tabBox.header.$container,
    $status = this._tabBox.$status,
    statusMargins = scout.graphics.margins($status),
    statusTop = top,
    statusPosition = this._tabBox.statusPosition,
    statusHeight = height - statusMargins.vertical();

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
scout.TabBoxLayout.prototype.preferredLayoutSize = function($container, options) {
  options = options || {};
  var htmlContainer = scout.HtmlComponent.get($container),
    htmlTabContent = scout.HtmlComponent.get(this._tabBox._$tabContent),
    htmlTabArea = scout.HtmlComponent.get(this._tabBox.header.$container),
    tabAreaSize = new scout.Dimension(),
    tabContentSize = new scout.Dimension(),
    $status = this._tabBox.$status,
    statusPosition = this._tabBox.statusPosition,
    headerWidthHint = htmlContainer.availableSize().subtract(htmlContainer.insets()).width;

  // HeightHint not supported
  options.heightHint = null;

  if (htmlTabArea.isVisible()) {
    if ($status && $status.isVisible()) {
      if (statusPosition === scout.FormField.StatusPosition.DEFAULT) {
        headerWidthHint -= $status.outerWidth(true);
      }
    }
    tabAreaSize = htmlTabArea.prefSize({
        widthHint: headerWidthHint
      })
      .add(htmlTabArea.margins());
  }

  tabContentSize = htmlTabContent.prefSize(options)
    .add(htmlContainer.insets())
    .add(htmlTabContent.margins());

  return new scout.Dimension(
    Math.max(tabAreaSize.width, tabContentSize.width),
    tabContentSize.height + tabAreaSize.height);
};
