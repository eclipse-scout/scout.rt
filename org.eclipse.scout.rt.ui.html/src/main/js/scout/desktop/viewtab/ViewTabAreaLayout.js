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
scout.ViewTabAreaLayout = function(viewTabs) {
  scout.ViewTabAreaLayout.parent.call(this);

  this.viewTabs = viewTabs;
  this._$overflowTab;
  this._overflowTabsIndizes = [];
};
scout.inherits(scout.ViewTabAreaLayout, scout.AbstractLayout);

scout.ViewTabAreaLayout.TAB_WIDTH_LARGE = 220;
scout.ViewTabAreaLayout.TAB_WIDTH_SMALL = 130;
scout.ViewTabAreaLayout.OVERFLOW_MENU_WIDTH = 30;

/**
 * @override AbstractLayout.js
 */
scout.ViewTabAreaLayout.prototype.layout = function($container) {
  var tabWidth,
    htmlContainer = this.viewTabs.htmlComp,
    containerSize = htmlContainer.getSize(),
    $tabs = htmlContainer.$comp.find('.desktop-view-tab'),
    numTabs = this.viewTabs.getViewTabs().length,
    smallPrefSize = this.smallPrefSize();

  containerSize = containerSize.subtract(htmlContainer.getInsets());

  // reset tabs and tool-items
  if (this._$overflowTab) {
    this._$overflowTab.remove();
  }

  $tabs.setVisible(true);
  this._overflowTabsIndizes = [];

  // All tabs in container
  if (smallPrefSize.width <= containerSize.width) {
    tabWidth = Math.min(scout.ViewTabAreaLayout.TAB_WIDTH_LARGE, Math.floor(containerSize.width / numTabs));
    // 2nd - all Tabs fit when they have small size
    $tabs.each(function() {
      $(this).outerWidth(tabWidth);
    });
    return;
  }

  // Not all tabs fit in container -> put tabs into overflow menu
  containerSize.width -= scout.ViewTabAreaLayout.OVERFLOW_MENU_WIDTH;

  // check how many tabs fit into remaining containerSize.width
  var numVisibleTabs = Math.floor(containerSize.width / scout.ViewTabAreaLayout.TAB_WIDTH_SMALL),
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
  tabWidth = scout.ViewTabAreaLayout.TAB_WIDTH_SMALL;
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

scout.ViewTabAreaLayout.prototype.smallPrefSize = function() {
  var numTabs = this.viewTabs.getViewTabs().length;
  return new scout.Dimension(numTabs * scout.ViewTabAreaLayout.TAB_WIDTH_SMALL, this.viewTabs.htmlComp.$comp.outerHeight(true));
};

scout.ViewTabAreaLayout.prototype.preferredLayoutSize = function($container) {
  var numTabs = this.viewTabs.getViewTabs().length;
  return new scout.Dimension(numTabs *  scout.ViewTabAreaLayout.TAB_WIDTH_LARGE, scout.graphics.prefSize(this.viewTabs.htmlComp.$comp, true,true).height);
};

scout.ViewTabAreaLayout.prototype._onMouseDownOverflow = function(event) {
  var menu, tab, popup,
    viewTabs = this.viewTabs,
    overflowMenus = [];

  this._overflowTabsIndizes.forEach(function(i) {
    // FIXME awe: fix bugs in overflow-menu:
    // - 1. menu schliesst nicht
    // - 2. manchmal verschwindet ein (noch offener) Tab - wenn nur einer sichtbar ist
    // - 3. add selenium tests
    tab = this.viewTabs.getViewTabs()[i];
    menu = scout.create('Menu', {
      parent: this.viewTabs,
      text: tab.getMenuText(),
      tab: tab
    });
    menu.remoteHandler = function(event) {
      if ('doAction' === event.type) {
        $.log.debug('(ViewTabAreaLayout#_onMouseDownOverflow) tab=' + this);
        viewTabs.selectViewTab(this);
      }
    }.bind(tab);
    overflowMenus.push(menu);
  }, this);

  popup = scout.create('ContextMenuPopup', {
    parent: this.viewTabs,
    menuItems: overflowMenus,
    cloneMenuItems: false,
    location: {
      x: event.pageX,
      y: event.pageY
    }
  });
  popup.open();
};
