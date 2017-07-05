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
  if (!this._ellipsisBounds) {
    this._createAndRenderEllipsis($container);
    this._ellipsisBounds = scout.graphics.bounds(this._$ellipsis, {
      includeMargin: true
    });
  }

  this._destroyEllipsis();
  this._tabBox.rebuildTabs();

  var bounds,
    tabArea = $container[0],
    clientWidth = tabArea.clientWidth - scout.graphics.getInsets($container).horizontal(),
    scrollWidth = tabArea.scrollWidth,
    menuBar = this._tabBox.menuBar,
    $status = this._tabBox.$status,
    statusWidth = 0,
    statusPosition = this._tabBox.statusPosition;

  // If tab area contains a menubar, less space is available
  clientWidth -= scout.graphics.getSize(menuBar.$container, true).width;

  if (statusPosition === scout.FormField.StatusPosition.TOP) {
    // Status on top means it is inside the tab area
    if ($status && $status.isVisible()) {
      statusWidth = $status.outerWidth(true);
    }
    if (statusWidth > 0) {
      clientWidth -= statusWidth;
      // Status is on the right of the menuBar
      menuBar.$container.cssRight(statusWidth);
    }
  }

  this._overflowTabs = [];
  if (clientWidth < scrollWidth) {

    // determine visible range (at least selected tab must be visible)
    var i, tab, numTabs,
      tabs = [], // tabs that are visible by model
      tabBounds = [], // bounds of visible tabs
      visibleTabs = [], // tabs that are visible by model and visible in the UI (= not in overflow)
      selectedTab = -1; // points to index of selected tab (in array of UI visible tabs)

    // reduce list to tab-items that are visible by model
    for (i = 0; i < this._tabBox.tabItems.length; i++) {
      tab = this._tabBox.tabItems[i];
      if (tab.visible) {
        bounds = scout.graphics.bounds(tab.$tabContainer, {
          includeMargin: true
        });
        tabs.push(tab);
        tabBounds.push(bounds);
        // cannot use selectedTab property of TabBox, it points to the wrong index
        // since this layout only works with visible tabs
        if (tab._tabActive) {
          selectedTab = tabs.length - 1;
        }
      }
    }
    numTabs = tabs.length;

    if (selectedTab >= 0) {
      // if we have too few space to even display the selected tab, only render the selected tab
      visibleTabs.push(selectedTab);
      bounds = tabBounds[selectedTab];

      if (clientWidth > bounds.width) {
        // in case of overflow, place selected tab at the left-most position...
        var horizontalInsets = scout.graphics.getInsets($container).horizontal();

        var viewWidth = bounds.width,
          delta = bounds.x - horizontalInsets, // delta used to start from x=0
          overflow = false;

        // when viewWidth doesn't fit into clientWidth anymore, abort always
        // expand to the right until the last tab is reached...
        if (selectedTab < numTabs - 1) {
          for (i = selectedTab + 1; i < numTabs; i++) {
            bounds = tabBounds[i];
            viewWidth = bounds.x - delta + bounds.width + this._ellipsisBounds.width;
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
        tab = tabs[i];
        if (visibleTabs.indexOf(i) === -1) {
          $.log.debug('Overflow tab=' + tab);
          this._overflowTabs.push(tab);
          tab.removeTab();
        }
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
  var menu, popup,
    overflowMenus = [],
    tabBox = this._tabBox;
  this._overflowTabs.forEach(function(tabItem) {
    menu = scout.create('Menu', {
      parent: tabBox,
      text: tabItem.label,
      tabItem: tabItem,
      visible: tabItem.visible
    });
    menu.on('action', function(event) {
      $.log.debug('(TabAreaLayout#_onClickEllipsis) tabItem=' + tabItem);
      tabBox.setSelectedTab(tabItem);
        popup.one('remove', function(event) {
          tabItem.session.focusManager.requestFocus(tabItem.$tabContainer);
      });
    });
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
