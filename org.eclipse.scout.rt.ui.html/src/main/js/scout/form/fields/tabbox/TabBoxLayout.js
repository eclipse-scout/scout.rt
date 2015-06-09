scout.TabBoxLayout = function(tabBox) {
  scout.TabBoxLayout.parent.call(this);
  this._tabBox = tabBox;
  this._$ellipsis;
  this._overflowTabItems = [];
};
scout.inherits(scout.TabBoxLayout, scout.AbstractLayout);

scout.TabBoxLayout.prototype.layout = function($container) {
  var containerSize, tabContentSize, htmlTabArea, tabAreaSize,
    htmlContainer = scout.HtmlComponent.get($container),
    $tabArea = this._tabBox._$tabArea,
    $tabContent = this._tabBox._$tabContent,
    htmlTabContent = scout.HtmlComponent.get($tabContent),
    tabAreaHeight = 0;

  containerSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets());

  if ($tabArea.isVisible()) {
    htmlTabArea = scout.HtmlComponent.get($tabArea);
    tabAreaSize = containerSize.subtract(htmlTabArea.getMargins());
    tabAreaHeight = $tabArea.outerHeight(true);
    this._layoutTabArea(htmlTabArea);
  }

  tabContentSize = containerSize.subtract(htmlTabContent.getMargins());
  tabContentSize.height -= tabAreaHeight;

  htmlTabContent.setSize(tabContentSize);
};

scout.TabBoxLayout.prototype._layoutTabArea = function(htmlTabArea) {
  this._destroyEllipsis();
  this._tabBox.rebuildTabs();

  var bounds, rightOuterX,
    tabArea = htmlTabArea.$comp[0],
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
    this._createAndRenderEllipsis(htmlTabArea.$comp);
  }
};


scout.TabBoxLayout.prototype._createAndRenderEllipsis = function($container) {
  this._$ellipsis = $container
    .appendDiv('overflow-tab-item')
    .click(this._onClickEllipsis.bind(this));
};

scout.TabBoxLayout.prototype._destroyEllipsis = function() {
  if (this._$ellipsis) {
    this._$ellipsis.remove();
    this._$ellipsis = null;
  }
};

/**
 * Preferred size of the tab-box aligns every tab-item in a single line, so that each item is visible.
 */
scout.TabBoxLayout.prototype.preferredLayoutSize = function($container) {
  var prefSize,
    htmlContainer = scout.HtmlComponent.get($container),
    $tabArea = this._tabBox._$tabArea,
    $tabContent = this._tabBox._$tabContent,
    htmlTabContent = scout.HtmlComponent.get($tabContent),
    tabAreaSize = new scout.Dimension(),
    tabContentSize = new scout.Dimension();

  tabAreaSize = scout.graphics.getVisibleSize($tabArea, true);
  tabContentSize = htmlTabContent.getPreferredSize()
    .add(htmlContainer.getInsets())
    .add(htmlTabContent.getMargins());

  prefSize = new scout.Dimension(
    Math.max(tabAreaSize.width, tabContentSize.width),
    tabContentSize.height + tabAreaSize.height);

  return prefSize;
};

scout.TabBoxLayout.prototype._onClickEllipsis = function(event) {
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
      $.log.debug('(TabBoxLayout#_onClickEllipsis) tabItem=' + this.tabItem);
      tabBox._selectTab(this.tabItem);
    };
    overflowMenus.push(menu);
  });

  popup = new scout.ContextMenuPopup(this._tabBox.session, overflowMenus, {cloneMenuItems: false});
  popup.render();
  popup.setLocation(new scout.Point(event.pageX, event.pageY));
};
