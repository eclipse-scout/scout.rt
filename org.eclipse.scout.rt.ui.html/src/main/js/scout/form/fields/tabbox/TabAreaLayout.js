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
scout.TabAreaLayout = function(tabBox) {
  scout.TabAreaLayout.parent.call(this);
  this._tabBox = tabBox;
  this._$ellipsis;
  this._overflowTabs = [];
};
scout.inherits(scout.TabAreaLayout, scout.AbstractLayout);

scout.TabAreaLayout.prototype.layout = function($container) {
  this._destroyEllipsis();
  this._tabBox.rebuildTabs();

  var bounds,
    tabArea = $container[0],
    clientWidth = tabArea.clientWidth,
    scrollWidth = tabArea.scrollWidth;

  // If tab area contains a menubar, less space is available
  var menubar = $container.children('.menubar')[0];
  if (menubar) {
    clientWidth -= menubar.clientWidth;
  }

  this._overflowTabs = [];
  if (clientWidth < scrollWidth) {

    // determine visible range (at least selected tab must be visible)
    var i, tabItem,
      numTabs = this._tabBox.tabItems.length,
      selectedTab = this._tabBox.selectedTab,
      tabBounds = [],
      visibleTabs = [];
    for (i = 0; i < numTabs; i++) {
      tabItem = this._tabBox.tabItems[i];
      bounds = scout.graphics.bounds(tabItem.$tabContainer, true, true);
      tabBounds.push(bounds);
    }

    // if we have too few space to even display the selected tab, only render the selected tab
    visibleTabs.push(selectedTab);
    bounds = tabBounds[selectedTab];

    if (clientWidth > bounds.width) {
      // in case of overflow, place selected tab at the left-most position...
      var viewWidth = bounds.width,
        delta = bounds.x, // delta used to start from x=0
        leftMostTab = selectedTab,
        rightMostTab = selectedTab,
        overflow = false;

      // when viewWidth doesn't fit into clientWidth anymore, abort always
      // expand to the right until the last tab is reached...
      if (selectedTab < numTabs - 1) {
        for (i = selectedTab + 1; i < numTabs; i++) {
          bounds = tabBounds[i];
          viewWidth = bounds.x - delta + bounds.width;
          if (viewWidth < clientWidth) {
            visibleTabs.push(i);
          } else {
            overflow = true;
          }
        }
      }

      // than expand to the left until the first tab is reached
      if (!overflow && selectedTab > 0) {
        for (i = selectedTab - 1; i >= 0; i--) {
          bounds = tabBounds[i];
          if (viewWidth + delta - bounds.x < clientWidth) {
            visibleTabs.push(i);
          }
        }
      }
    }

    // remove all tabs which aren't visible
    for (i = 0; i < numTabs; i++) {
      tabItem = this._tabBox.tabItems[i];
      if (visibleTabs.indexOf(i) === -1) {
        $.log.debug('Overflow tabItem=' + tabItem);
        this._overflowTabs.push(tabItem);
        tabItem.removeTab();
      }
    }
  }

  if (this._overflowTabs.length > 0) {
    this._createAndRenderEllipsis($container);
  }
};

scout.TabAreaLayout.prototype._createAndRenderEllipsis = function($container) {
  this._$ellipsis = $container
    .appendDiv('overflow-tab-item')
    .click(this._onClickEllipsis.bind(this));
};

scout.TabAreaLayout.prototype._destroyEllipsis = function() {
  if (this._$ellipsis) {
    this._$ellipsis.remove();
    this._$ellipsis = null;
  }
};

scout.TabAreaLayout.prototype._onClickEllipsis = function(event) {
  var menu, popup, localSession,
    overflowMenus = [],
    tabBox = this._tabBox;
  this._overflowTabs.forEach(function(tabItem) {
    menu = scout.create('Menu', {
      parent: tabBox,
      text: scout.strings.removeAmpersand(tabItem.label),
      tabItem: tabItem,
      enabled: tabItem.enabled,
      visible: tabItem.visible
    });
    menu.remoteHandler = function(event) {
      if ('doAction' === event.type) {
        $.log.debug('(TabAreaLayout#_onClickEllipsis) tabItem=' + tabItem);
        tabBox._selectTab(tabItem);
      }
    };
    overflowMenus.push(menu);
  });

  popup = scout.create('ContextMenuPopup', {
    parent: tabBox,
    menuItems: overflowMenus,
    cloneMenuItems: false,
    location: {
      x: event.pageX,
      y: event.pageY
    }
  });
  popup.open();
};
