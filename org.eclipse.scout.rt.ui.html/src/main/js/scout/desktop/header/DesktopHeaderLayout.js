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
    containerSize = htmlContainer.size(),
    toolBox = this.header.toolBox,
    viewButtonBox = this.header.viewButtonBox,
    tabArea = this.header.tabArea,
    smallTabsPrefSize = tabArea.htmlComp.layout.smallPrefSize(),
    tabsPrefSize = tabArea.htmlComp.prefSize(),
    tabsWidth = 0, // = available width for tabs
    logoWidth = 0,
    viewButtonBoxWidth = 0,
    toolBoxWidth = 0;

  containerSize = containerSize.subtract(htmlContainer.insets());

  if (this.header.logo) {
    logoWidth = scout.graphics.size(this.header.logo.$container, true).width;
  }

  if (viewButtonBox) {
    viewButtonBoxPrefSize = viewButtonBox.htmlComp.prefSize();
    viewButtonBoxWidth = viewButtonBoxPrefSize.width;
    viewButtonBox.htmlComp.setSize(viewButtonBoxPrefSize.subtract(viewButtonBox.htmlComp.margins()));
  }
  tabArea.htmlComp.$comp.cssLeft(viewButtonBoxWidth);

  if (toolBox) {
    toolBoxPrefSize = toolBox.htmlComp.prefSize();
    toolBoxWidth = toolBoxPrefSize.width;
    setToolBoxSize();
    setToolBoxLocation();
  }

  tabsWidth = calcTabsWidth();
  if (tabsWidth >= smallTabsPrefSize.width) {
    // All tabs fit when they have small size -> use available size but max the pref size -> prefSize = size of maximumtabs if tabs use their large (max) size
    tabsWidth = Math.min(tabsPrefSize.width, tabsWidth);
    tabArea.htmlComp.setSize(new scout.Dimension(tabsWidth, tabsPrefSize.height));
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
  if (tabsWidth >= smallTabsPrefSize.width) {
    tabArea.htmlComp.setSize(smallTabsPrefSize);
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
  tabsWidth = Math.min(smallTabsPrefSize.width, tabsWidth);
  // Ensure minimum with for the the overflow menu - expect if there are no tabs at all (in that case ensure min width of 0)
  tabsWidth = Math.max(tabsWidth, (tabArea.tabs.length ? scout.SimpleTabAreaLayout.OVERFLOW_MENU_WIDTH : 0));
  setTabsSize();

  // 3rd if only the overflow menu is shown make toolBox smaller so that ellipsis may be displayed
  if (toolBox && tabsWidth <= scout.SimpleTabAreaLayout.OVERFLOW_MENU_WIDTH) {
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
    tabArea.htmlComp.setSize(new scout.Dimension(tabsWidth, tabsPrefSize.height));
  }

  function setToolBoxSize() {
    toolBox.htmlComp.setSize(new scout.Dimension(toolBoxWidth, toolBoxPrefSize.height).subtract(toolBox.htmlComp.margins()));
  }

  function setToolBoxLocation() {
    toolBox.htmlComp.$comp.cssLeft(containerSize.width - toolBoxWidth - logoWidth);
  }
};
