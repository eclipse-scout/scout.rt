scout.TabAreaLayout = function(tabBox) {
  scout.TabAreaLayout.parent.call(this);
  this._tabBox = tabBox;
  this._$ellipsis;
  this._overflowTabItems = [];
};
scout.inherits(scout.TabAreaLayout, scout.AbstractLayout);

scout.TabAreaLayout.prototype.layout = function($container) {
  this._destroyEllipsis();
  this._tabBox.rebuildTabs();

  var bounds,
    tabArea = $container[0],
    clientWidth = tabArea.clientWidth,
    scrollWidth = tabArea.scrollWidth;

  this._overflowTabItems = [];
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
      // 1. when oldTab is unknown, place selected tab at the left-most position
      var
        viewWidth = bounds.width,
        delta = bounds.x, // delta used to start from x=0
        leftMostTab = selectedTab,
        rightMostTab = selectedTab,
        overflow = false;

      // when leftEnd + rightEnd do not fit into clientWidth anymore, abort always
      // expand to the right until the last tab is reached
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

      // 2. find place for selected tab depending on the oldTab
      // FIXME AWE: damit das richtig funktioniert, muss auch der _vorher_ selektierte tab bekannt sein
    }

    // FIXME AWE: hier durch visibleTabs loopen
    for (i = 0; i < numTabs; i++) {
      tabItem = this._tabBox.tabItems[i];
      if (visibleTabs.indexOf(i) === -1) {
        $.log.debug('Overflow tabItem=' + tabItem);
        this._overflowTabItems.push(tabItem);
        tabItem.removeTab();
      }
    }
  }

  if (this._overflowTabItems.length > 0) {
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
  this._overflowTabItems.forEach(function(tabItem) {
    menu = tabBox.session.createUiObject({
      objectType: 'Menu',
      text: scout.strings.removeAmpersand(tabItem.label),
      tabItem: tabItem
    });
    menu.sendDoAction = function() {
      $.log.debug('(TabAreaLayout#_onClickEllipsis) tabItem=' + this.tabItem);
      tabBox._selectTab(this.tabItem);
    };
    overflowMenus.push(menu);
  });

  popup = new scout.ContextMenuPopup(this._tabBox.session, overflowMenus, {cloneMenuItems: false});
  popup.render();
  popup.setLocation(new scout.Point(event.pageX, event.pageY));
};


scout.TabAreaLayout.prototype.preferredLayoutSize = function($container) {
  return scout.graphics.getSize($container);
};
