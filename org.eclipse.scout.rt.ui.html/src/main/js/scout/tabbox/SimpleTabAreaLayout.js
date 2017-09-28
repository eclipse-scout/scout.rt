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
scout.SimpleTabAreaLayout = function(tabArea) {
  scout.SimpleTabAreaLayout.parent.call(this);
  this.tabArea = tabArea;
  this._$overflowTab;
  this._overflowTabsIndizes = [];
};
scout.inherits(scout.SimpleTabAreaLayout, scout.AbstractLayout);

scout.SimpleTabAreaLayout.TAB_WIDTH_LARGE = 220;
scout.SimpleTabAreaLayout.TAB_WIDTH_SMALL = 130;
scout.SimpleTabAreaLayout.OVERFLOW_MENU_WIDTH = 30;

/**
 * @override AbstractLayout.js
 */
scout.SimpleTabAreaLayout.prototype.layout = function($container) {
  var tabWidth,
    htmlContainer = this.tabArea.htmlComp,
    containerSize = htmlContainer.size(),
    $tabs = htmlContainer.$comp.find('.simple-tab'),
    numTabs = this.tabArea.getTabs().length,
    smallPrefSize = this.smallPrefSize();

  containerSize = containerSize.subtract(htmlContainer.insets());

  // reset tabs and tool-items
  if (this._$overflowTab) {
    this._$overflowTab.remove();
  }

  $tabs.setVisible(true);
  this._overflowTabsIndizes = [];

  // All tabs in container
  if (smallPrefSize.width <= containerSize.width) {
    tabWidth = Math.min(scout.SimpleTabAreaLayout.TAB_WIDTH_LARGE, Math.floor(containerSize.width / numTabs));
    // 2nd - all Tabs fit when they have small size
    $tabs.each(function() {
      $(this).outerWidth(tabWidth);
    });
    return;
  }

  // Not all tabs fit in container -> put tabs into overflow menu
  containerSize.width -= scout.SimpleTabAreaLayout.OVERFLOW_MENU_WIDTH;

  // check how many tabs fit into remaining containerSize.width
  var numVisibleTabs = Math.floor(containerSize.width / scout.SimpleTabAreaLayout.TAB_WIDTH_SMALL),
    numOverflowTabs = numTabs - numVisibleTabs;

  var i = 0,
    selectedIndex = 0;
  $tabs.each(function() {
    if ($(this).hasClass('selected')) {
      selectedIndex = i;
    }
    i++;
  });

  // determine visible range
  var rightEnd, leftEnd = selectedIndex - Math.floor(numVisibleTabs / 2);
  if (leftEnd < 0) {
    leftEnd = 0;
    rightEnd = numVisibleTabs - 1;
  } else {
    rightEnd = leftEnd + numVisibleTabs - 1;
    if (rightEnd > numTabs - 1) {
      rightEnd = numTabs - 1;
      leftEnd = rightEnd - numVisibleTabs + 1;
    }
  }

  this._$overflowTab = htmlContainer.$comp
    .appendDiv('overflow-tab-item')
    .on('mousedown', this._onMouseDownOverflow.bind(this));
  if (numOverflowTabs > 1) {
    this._$overflowTab.appendDiv('num-tabs').text(numOverflowTabs);
  }

  var that = this;
  tabWidth = scout.SimpleTabAreaLayout.TAB_WIDTH_SMALL;
  i = 0;
  $tabs.each(function() {
    if (i >= leftEnd && i <= rightEnd) {
      $(this).outerWidth(tabWidth);
    } else {
      $(this).setVisible(false);
      that._overflowTabsIndizes.push(i);
    }
    i++;
  });
};

scout.SimpleTabAreaLayout.prototype.smallPrefSize = function() {
  var numTabs = this.tabArea.getTabs().length;
  return new scout.Dimension(numTabs * scout.SimpleTabAreaLayout.TAB_WIDTH_SMALL, this.tabArea.htmlComp.$comp.outerHeight(true));
};

scout.SimpleTabAreaLayout.prototype.preferredLayoutSize = function($container) {
  var numTabs = this.tabArea.getTabs().length;
  return new scout.Dimension(numTabs * scout.SimpleTabAreaLayout.TAB_WIDTH_LARGE, scout.graphics.prefSize(this.tabArea.htmlComp.$comp, {
    includeMargin: true,
    useCssSize: true
  }).height);
};

scout.SimpleTabAreaLayout.prototype._onMouseDownOverflow = function(event) {
  var menu, tab, popup,
    tabArea = this.tabArea,
    overflowMenus = [];

  this._overflowTabsIndizes.forEach(function(i) {
    tab = this.tabArea.getTabs()[i];
    menu = scout.create('Menu', {
      parent: this.tabArea,
      text: tab.getMenuText(),
      tab: tab
    });
    menu.on('action', function() {
      $.log.debug('(SimpleTabAreaLayout#_onMouseDownOverflow) tab=' + this);
      tabArea.selectTab(this);
    }.bind(tab));
    overflowMenus.push(menu);
  }, this);

  popup = scout.create('ContextMenuPopup', {
    parent: this.tabArea,
    menuItems: overflowMenus,
    cloneMenuItems: false,
    location: {
      x: event.pageX,
      y: event.pageY
    }
  });
  popup.open();
};
