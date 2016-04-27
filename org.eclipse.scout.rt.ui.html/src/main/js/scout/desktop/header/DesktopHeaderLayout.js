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
  var viewButtonBoxPrefSize, toolBarPrefSize,
    htmlContainer = this.header.htmlComp,
    containerSize = htmlContainer.getSize(),
    toolBar = this.header.toolBar,
    viewButtonBox = this.header.viewButtonBox,
    viewTabBox = this.header.viewTabBox,
    smallTabsPrefSize = viewTabBox.htmlComp.layout.smallPrefSize(),
    tabsPrefSize = viewTabBox.htmlComp.getPreferredSize(),
    tabsWidth = 0,
    logoWidth = 0,
    viewButtonBoxWidth = 0,
    toolBarWidth = 0;

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

  if (toolBar) {
    toolBarPrefSize = toolBar.htmlComp.getPreferredSize();
    toolBarWidth = toolBarPrefSize.width;
    setToolBarSize();
    setToolBarLocation();
  }

  tabsWidth = calcTabsWidth();
  if (smallTabsPrefSize.width <= tabsWidth) {
    // All tabs fit when they have small size -> use available size but max the pref size -> prefSize = size of maximumtabs if tabs use their large (max) size
    tabsWidth = Math.min(tabsPrefSize.width, tabsWidth);
    viewTabBox.htmlComp.setSize(new scout.Dimension(tabsWidth, tabsPrefSize.height));
    return;
  }

  // 1st try to minimize padding around tool-bar items -> compact mode
  if (toolBar) {
    toolBarPrefSize = toolBar.htmlComp.layout.compactPrefSize();
    toolBarWidth = toolBarPrefSize.width;
    setToolBarSize();
    setToolBarLocation();
  }

  tabsWidth = calcTabsWidth();
  if (smallTabsPrefSize.width <= tabsWidth) {
      viewTabBox.htmlComp.setSize(smallTabsPrefSize);
    setTabsSize();
    return;
  }

  // 2nd remove text from tool-bar items, only show icon
  if (toolBar) {
    toolBarPrefSize = toolBar.htmlComp.layout.shrinkPrefSize();
    toolBarWidth = toolBarPrefSize.width;
    setToolBarSize();
    setToolBarLocation();
  }

  tabsWidth = calcTabsWidth();
  tabsWidth = Math.max(Math.min(smallTabsPrefSize.width, tabsWidth), scout.ViewTabAreaLayout.OVERFLOW_MENU_WIDTH);
  setTabsSize();

  // 3rd if only the overflow menu is shown make toolBar smaller so that ellipsis may be displayed
  if (toolBar && tabsWidth <= scout.ViewTabAreaLayout.OVERFLOW_MENU_WIDTH) {
    // layout toolBar, now an ellipsis menu may be shown
    toolBarWidth = containerSize.width - tabsWidth - logoWidth - viewButtonBoxWidth;
    setToolBarSize();

    // update size of the toolBar again with the actual width to make it correctly right aligned
    toolBarWidth = toolBar.htmlComp.layout.actualPrefSize().width;
    setToolBarSize();
    setToolBarLocation();
  }

  function calcTabsWidth() {
    return containerSize.width - toolBarWidth - logoWidth - viewButtonBoxWidth;
  }

  function setTabsSize() {
    viewTabBox.htmlComp.setSize(new scout.Dimension(tabsWidth, tabsPrefSize.height));
  }

  function setToolBarSize() {
    toolBar.htmlComp.setSize(new scout.Dimension(toolBarWidth, toolBarPrefSize.height).subtract(toolBar.htmlComp.getMargins()));
  }

  function setToolBarLocation() {
    toolBar.htmlComp.$comp.cssLeft(containerSize.width - toolBarWidth - logoWidth);
  }
};
