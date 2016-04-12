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
  this._layout($container);

  // Make sure open popups are at the correct position after layouting
  this.desktop.actions
    .filter(function(action) {
      return action.selected && action.popup;
    })
    .some(function(action) {
      action.popup.position();
      return true;
    });
};

scout.DesktopHeaderLayout.prototype._layout = function($container) {
  var viewButtonsPrefSize, toolBarPrefSize,
    htmlContainer = this.header.htmlComp,
    containerSize = htmlContainer.getSize(),
    toolBar = this.header.toolBar,
    viewButtons = this.header.viewButtons,
    viewTabs = this.header.viewTabs,
    smallTabsPrefSize = viewTabs.htmlComp.layout.smallPrefSize(),
    tabsPrefSize = viewTabs.htmlComp.getPreferredSize(),
    tabsWidth = 0,
    logoWidth = 0,
    viewButtonsWidth = 0,
    toolBarWidth = 0;

  containerSize = containerSize.subtract(htmlContainer.getInsets());

  if (this.header.logo) {
    logoWidth = scout.graphics.getSize(this.header.logo.$container, true).width;
  }

  if (viewButtons) {
    viewButtonsPrefSize = viewButtons.htmlComp.getPreferredSize();
    viewButtonsWidth = viewButtonsPrefSize.width;
    viewButtons.htmlComp.setSize(viewButtonsPrefSize.subtract(viewButtons.htmlComp.getMargins()));
  }
  viewTabs.htmlComp.$comp.cssLeft(viewButtonsWidth);

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
    setTabsSize();
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
    tabsWidth = smallTabsPrefSize.width;
    setTabsSize();
    return;
  }

  // 2nd remove text from tool-bar items, only show icon
  if (toolBar) {
    toolBarPrefSize = toolBar.htmlComp.layout.smallPrefSize();
    toolBarWidth = toolBarPrefSize.width;
    setToolBarSize();
    setToolBarLocation();
  }

  tabsWidth = calcTabsWidth();
  tabsWidth = Math.max(Math.min(smallTabsPrefSize.width, tabsWidth), scout.DesktopViewTabsLayout.OVERFLOW_MENU_WIDTH);
  setTabsSize();

  // 3rd if only the overflow menu is shown make toolBar smaller so that ellipsis may be displayed
  if (toolBar && tabsWidth <= scout.DesktopViewTabsLayout.OVERFLOW_MENU_WIDTH) {
    // layout toolBar, now an ellipsis menu may be shown
    toolBarWidth = containerSize.width - tabsWidth - logoWidth - viewButtonsWidth;
    setToolBarSize();

    // update size of the toolBar again with the actual width to make it correctly right aligned
    toolBarWidth = toolBar.htmlComp.layout.actualSize().width;
    setToolBarSize();
    setToolBarLocation();
  }

  function calcTabsWidth() {
    return containerSize.width - toolBarWidth - logoWidth - viewButtonsWidth;
  }

  function setTabsSize() {
    viewTabs.htmlComp.setSize(new scout.Dimension(tabsWidth, tabsPrefSize.height).subtract(viewTabs.htmlComp.getMargins()));
  }

  function setToolBarSize() {
    toolBar.htmlComp.setSize(new scout.Dimension(toolBarWidth, toolBarPrefSize.height).subtract(toolBar.htmlComp.getMargins()));
  }

  function setToolBarLocation() {
    toolBar.htmlComp.$comp.cssLeft(containerSize.width - toolBarWidth - logoWidth);
  }
};
