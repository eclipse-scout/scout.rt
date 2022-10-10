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
import {AbstractLayout, Dimension, graphics, SimpleTabArea, SimpleTabAreaLayout} from '../../index';

export default class DesktopHeaderLayout extends AbstractLayout {

  constructor(header) {
    super();
    this.header = header;
    this.desktop = header.desktop;
  }

  /**
   * @override AbstractLayout.js
   */
  layout($container) {
    let viewButtonBoxPrefSize, toolBoxPrefSize, tabsPrefSize, smallTabsPrefSize,
      htmlContainer = this.header.htmlComp,
      containerSize = htmlContainer.size(),
      toolBox = this.header.toolBox,
      viewButtonBox = this.header.viewButtonBox,
      tabArea = this.header.tabArea,
      tabsWidth = 0, // = available width for tabs
      logoWidth = 0,
      viewButtonBoxWidth = 0,
      toolBoxWidth = 0;

    containerSize = containerSize.subtract(htmlContainer.insets());

    if (this.header.logo) {
      logoWidth = graphics.size(this.header.logo.$container, true).width;
    }

    if (viewButtonBox) {
      viewButtonBoxPrefSize = viewButtonBox.htmlComp.prefSize(true);
      viewButtonBoxWidth = viewButtonBoxPrefSize.width;
      viewButtonBox.htmlComp.setSize(viewButtonBoxPrefSize.subtract(viewButtonBox.htmlComp.margins()));
    }
    tabArea.htmlComp.$comp.cssLeft(viewButtonBoxWidth);

    if (toolBox) {
      toolBoxPrefSize = toolBox.htmlComp.prefSize(true);
      toolBoxWidth = toolBoxPrefSize.width;
      setToolBoxSize();
      setToolBoxLocation();
    }

    tabsWidth = calcTabsWidth();
    smallTabsPrefSize = getTabsSmallPrefSize();
    tabsPrefSize = tabArea.htmlComp.prefSize({widthHint: tabsWidth, includeMargin: true});
    if (tabsWidth >= smallTabsPrefSize.width) {
      // All tabs fit when they have small size -> use available size but max the pref size -> prefSize = size of maximum tabs if tabs use their large (max) size
      tabsWidth = Math.min(tabsPrefSize.width, tabsWidth);
      setTabsSize();
      return;
    }

    // 1st try to minimize padding around tool-bar items -> compact mode
    if (toolBox) {
      toolBoxPrefSize = getToolBoxCompactPrefSize();
      toolBoxWidth = toolBoxPrefSize.width;
      setToolBoxSize();
      setToolBoxLocation();
    }

    tabsWidth = calcTabsWidth();
    if (tabsWidth >= smallTabsPrefSize.width) {
      setTabsSize();
      return;
    }

    // 2nd remove text from tool-bar items, only show icon
    if (toolBox) {
      toolBoxPrefSize = getToolBoxShrinkPrefSize();
      toolBoxWidth = toolBoxPrefSize.width;
      setToolBoxSize();
      setToolBoxLocation();
    }

    tabsWidth = calcTabsWidth();
    if (tabArea.displayStyle !== SimpleTabArea.DisplayStyle.SPREAD_EVEN) {
      tabsWidth = Math.min(smallTabsPrefSize.width, tabsWidth);
    }
    // Ensure minimum width for the the overflow menu - expect if there are no tabs at all (in that case ensure min width of 0)
    let overflowTabItemWidth = tabArea.htmlComp.layout.overflowTabItemWidth;
    tabsWidth = Math.max(tabsWidth, (tabArea.tabs.length ? overflowTabItemWidth : 0));
    setTabsSize();

    // 3rd if only the overflow menu is shown make toolBox smaller so that ellipsis may be displayed
    if (toolBox && tabsWidth <= overflowTabItemWidth) {
      // layout toolBox, now an ellipsis menu may be shown
      toolBoxWidth = containerSize.width - tabsWidth - logoWidth - viewButtonBoxWidth;
      setToolBoxSize();
      setToolBoxLocation();
    }

    function calcTabsWidth() {
      return containerSize.width - toolBoxWidth - logoWidth - viewButtonBoxWidth;
    }

    function setTabsSize() {
      tabArea.htmlComp.setSize(new Dimension(tabsWidth, tabsPrefSize.height).subtract(tabArea.htmlComp.margins()));
    }

    function getTabsSmallPrefSize() {
      return tabArea.htmlComp.layout.smallPrefSize({widthHint: tabsWidth}).add(tabArea.htmlComp.margins());
    }

    function setToolBoxSize() {
      toolBox.htmlComp.setSize(new Dimension(toolBoxWidth, toolBoxPrefSize.height).subtract(toolBox.htmlComp.margins()));
    }

    function getToolBoxCompactPrefSize() {
      return toolBox.htmlComp.layout.compactPrefSize().add(toolBox.htmlComp.margins());
    }

    function getToolBoxShrinkPrefSize() {
      return toolBox.htmlComp.layout.shrinkPrefSize().add(toolBox.htmlComp.margins());
    }

    function getToolBoxActualPrefSize() {
      return toolBox.htmlComp.layout.actualPrefSize().add(toolBox.htmlComp.margins());
    }

    function setToolBoxLocation() {
      toolBox.htmlComp.$comp.cssLeft(containerSize.width - toolBoxWidth - logoWidth);
    }
  }
}
