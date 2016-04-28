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
scout.DesktopHeaderLayout = function(header) {
  scout.DesktopHeaderLayout.parent.call(this);

  this.header = header;
  this.desktop = header.desktop;
};
scout.inherits(scout.DesktopHeaderLayout, scout.AbstractLayout);

/**
 * @override AbstractLayout.js
 */
scout.DesktopHeaderLayout.prototype.layout = function($container) {
  var viewButtonBoxPrefSize, toolBoxPrefSize,
    htmlContainer = this.header.htmlComp,
    containerSize = htmlContainer.getSize(),
    toolBox = this.header.toolBox,
    viewButtonBox = this.header.viewButtonBox,
    viewTabBox = this.header.viewTabBox,
    smallTabsPrefSize = viewTabBox.htmlComp.layout.smallPrefSize(),
    tabsPrefSize = viewTabBox.htmlComp.getPreferredSize(),
    tabsWidth = 0,
    logoWidth = 0,
    viewButtonBoxWidth = 0,
    toolBoxWidth = 0;

  containerSize = containerSize.subtract(htmlContainer.getInsets());

  if (this.header.logo) {
    logoWidth = scout.graphics.getSize(this.header.logo.$container, true).width;
  }

  if (viewButtonBox) {
    viewButtonBoxPrefSize = viewButtonBox.htmlComp.getPreferredSize();
    viewButtonBoxWidth = viewButtonBoxPrefSize.width;
    viewButtonBox.htmlComp.setSize(viewButtonBoxPrefSize.subtract(viewButtonBox.htmlComp.getMargins()));
  }
  viewTabBox.htmlComp.$comp.cssLeft(viewButtonBoxWidth);

  if (toolBox) {
    toolBoxPrefSize = toolBox.htmlComp.getPreferredSize();
    toolBoxWidth = toolBoxPrefSize.width;
    setToolBoxSize();
    setToolBoxLocation();
  }

  tabsWidth = calcTabsWidth();
  if (smallTabsPrefSize.width <= tabsWidth) {
    // All tabs fit when they have small size -> use available size but max the pref size -> prefSize = size of maximumtabs if tabs use their large (max) size
    tabsWidth = Math.min(tabsPrefSize.width, tabsWidth);
    viewTabBox.htmlComp.setSize(new scout.Dimension(tabsWidth, tabsPrefSize.height));
    return;
  }

  // 1st try to minimize padding around tool-bar items -> compact mode
  if (toolBox) {
    toolBoxPrefSize = toolBox.htmlComp.layout.compactPrefSize();
    toolBoxWidth = toolBoxPrefSize.width;
    setToolBoxSize();
    setToolBoxLocation();
  }

  tabsWidth = calcTabsWidth();
  if (smallTabsPrefSize.width <= tabsWidth) {
      viewTabBox.htmlComp.setSize(smallTabsPrefSize);
    setTabsSize();
    return;
  }

  // 2nd remove text from tool-bar items, only show icon
  if (toolBox) {
    toolBoxPrefSize = toolBox.htmlComp.layout.shrinkPrefSize();
    toolBoxWidth = toolBoxPrefSize.width;
    setToolBoxSize();
    setToolBoxLocation();
  }

  tabsWidth = calcTabsWidth();
  tabsWidth = Math.max(Math.min(smallTabsPrefSize.width, tabsWidth), scout.ViewTabAreaLayout.OVERFLOW_MENU_WIDTH);
  setTabsSize();

  // 3rd if only the overflow menu is shown make toolBox smaller so that ellipsis may be displayed
  if (toolBox && tabsWidth <= scout.ViewTabAreaLayout.OVERFLOW_MENU_WIDTH) {
    // layout toolBox, now an ellipsis menu may be shown
    toolBoxWidth = containerSize.width - tabsWidth - logoWidth - viewButtonBoxWidth;
    setToolBoxSize();

    // update size of the toolBox again with the actual width to make it correctly right aligned
    toolBoxWidth = toolBox.htmlComp.layout.actualPrefSize().width;
    setToolBoxSize();
    setToolBoxLocation();
  }

  function calcTabsWidth() {
    return containerSize.width - toolBoxWidth - logoWidth - viewButtonBoxWidth;
  }

  function setTabsSize() {
    viewTabBox.htmlComp.setSize(new scout.Dimension(tabsWidth, tabsPrefSize.height));
  }

  function setToolBoxSize() {
    toolBox.htmlComp.setSize(new scout.Dimension(toolBoxWidth, toolBoxPrefSize.height).subtract(toolBox.htmlComp.getMargins()));
  }

  function setToolBoxLocation() {
    toolBox.htmlComp.$comp.cssLeft(containerSize.width - toolBoxWidth - logoWidth);
  }
};
