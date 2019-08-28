/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.TabBoxHeaderLayout = function(tabBoxHeader) {
  scout.TabBoxHeaderLayout.parent.call(this);
  this.tabBoxHeader = tabBoxHeader;

  this._initDefaults();

  this.htmlPropertyChangeHandler = this._onHtmlEnvironmenPropertyChange.bind(this);
  scout.HtmlEnvironment.on('propertyChange', this.htmlPropertyChangeHandler);
  this.tabBoxHeader.one('remove', function() {
    scout.HtmlEnvironment.off('propertyChange', this.htmlPropertyChangeHandler);
  }.bind(this));
};
scout.inherits(scout.TabBoxHeaderLayout, scout.AbstractLayout);

scout.TabBoxHeaderLayout.prototype._initDefaults = function() {
  this.fieldStatusWidth = scout.HtmlEnvironment.fieldStatusWidth;
};

scout.TabBoxHeaderLayout.prototype._onHtmlEnvironmenPropertyChange = function() {
  this._initDefaults();
  this.tabBoxHeader.invalidateLayoutTree();
};

scout.TabBoxHeaderLayout.prototype.layout = function($container) { //
  var htmlContainer = scout.HtmlComponent.get($container),
    tabArea = this.tabBoxHeader.tabArea,
    tabAreaMargins = tabArea.htmlComp.margins(),
    tabAreaPrefSize,
    menuBar = this.tabBoxHeader.menuBar,
    menuBarMargins = menuBar.htmlComp.margins(),
    menuBarMinumumSize,
    $status = this.tabBoxHeader.tabBox.$status,
    statusSizeLarge = new scout.Dimension(),
    insets = htmlContainer.insets(),
    containerSize = htmlContainer.availableSize({
      exact: true
    }).subtract(htmlContainer.insets()),
    clientArea = new scout.Rectangle(insets.left, insets.top, containerSize.width, containerSize.height),
    left = clientArea.x;

  menuBarMinumumSize = menuBar.htmlComp.prefSize({
    widthHint: 0
  });

  if (this.tabBoxHeader.tabBox.statusPosition === scout.FormField.StatusPosition.TOP && $status && $status.isVisible()) {
    statusSizeLarge.height = $status.outerHeight(true);
    statusSizeLarge.width = this.fieldStatusWidth + scout.graphics.margins($status).horizontal();
  }

  tabAreaPrefSize = tabArea.htmlComp.prefSize({
    widthHint: clientArea.width - menuBarMinumumSize.width - menuBarMargins.horizontal() - statusSizeLarge.width,
    exact: false
  });

  // layout tabItemsBar
  tabArea.htmlComp.setBounds(new scout.Rectangle(
    clientArea.x + tabAreaMargins.left,
    insets.top + tabAreaMargins.top,
    tabAreaPrefSize.width,
    clientArea.height - tabAreaMargins.vertical()
  ));

  menuBar.htmlComp.layout.collapsed = tabArea.htmlComp.layout.overflowTabs.length > 0;
  // layout menuBar
  menuBar.htmlComp.setBounds(new scout.Rectangle(
    left + tabAreaPrefSize.width + tabAreaMargins.horizontal() + menuBarMargins.left,
    insets.top + menuBarMargins.top,
    clientArea.width - tabAreaPrefSize.width - tabAreaMargins.horizontal() - menuBarMargins.horizontal() - statusSizeLarge.width,
    clientArea.height - menuBarMargins.vertical()
  ));

  // layout status
  if (this.tabBoxHeader.tabBox.statusPosition === scout.FormField.StatusPosition.TOP && $status && $status.isVisible()) {
    $status.cssWidth(this.fieldStatusWidth)
      .cssRight(insets.left)
      .cssHeight(clientArea.height - scout.graphics.margins($status).vertical())
      .cssLineHeight(clientArea.height - scout.graphics.margins($status).vertical());
  }

};

scout.TabBoxHeaderLayout.prototype.preferredLayoutSize = function($container, options) {
  var htmlContainer = scout.HtmlComponent.get($container),
    insets = htmlContainer.insets(),
    wHint = (options.widthHint || htmlContainer.availableSize().width) - htmlContainer.insets().horizontal(),
    prefSize = new scout.Dimension(),
    $status = this.tabBoxHeader.tabBox.$status,
    statusSizeLarge = new scout.Dimension(),
    tabArea = this.tabBoxHeader.tabArea,
    tabAreaMargins = tabArea.htmlComp.margins(),
    tabAreaPrefSize,
    menuBar = this.tabBoxHeader.menuBar,
    menuBarMargins = menuBar.htmlComp.margins(),
    menuBarMinumumSize,
    menuBarPrefSize;

  menuBarMinumumSize = menuBar.htmlComp.prefSize({
    widthHint: 0
  });

  if (this.tabBoxHeader.tabBox.statusPosition === scout.FormField.StatusPosition.TOP && $status && $status.isVisible()) {
    statusSizeLarge.height = $status.outerHeight(true);
    statusSizeLarge.width = this.fieldStatusWidth + scout.graphics.margins($status).horizontal();

    prefSize.width += statusSizeLarge.width;
    prefSize.height = Math.max(prefSize.height, statusSizeLarge.height);
  }

  tabAreaPrefSize = tabArea.htmlComp.prefSize({
    widthHint: wHint - menuBarMinumumSize.width - menuBarMargins.horizontal() - tabAreaMargins.horizontal() - statusSizeLarge.width
  });

  prefSize.width += tabAreaPrefSize.width + tabAreaMargins.horizontal();
  prefSize.height = Math.max(prefSize.height, tabAreaPrefSize.height + tabAreaMargins.vertical());

  menuBarPrefSize = menuBar.htmlComp.prefSize({
    widthHint: wHint - tabAreaPrefSize.width - tabAreaMargins.horizontal() - menuBarMargins.horizontal() - statusSizeLarge.width
  });

  prefSize.width += menuBarPrefSize.width + menuBarMargins.horizontal();
  prefSize.height = Math.max(prefSize.height, menuBarPrefSize.height + menuBarMargins.vertical());

  return prefSize.add(insets);
};
