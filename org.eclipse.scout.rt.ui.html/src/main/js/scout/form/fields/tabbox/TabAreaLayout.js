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
scout.TabAreaLayout = function(tabArea) {
  scout.TabAreaLayout.parent.call(this);
  this.tabArea = tabArea;
  this.$ellipsis;
  this.overflowTabs = [];
  this.visibleTabs = [];
  this._tabAreaPropertyChangeHandler = this._onTabAreaPropertyChange.bind(this);

  this.tabArea.on('propertyChange', this._tabAreaPropertyChangeHandler);
  this.tabArea.one('remove', function(){
   this.tabArea.off('propertyChange', this._tabAreaPropertyChangeHandler);
  }.bind(this));
};
scout.inherits(scout.TabAreaLayout, scout.AbstractLayout);

scout.TabAreaLayout.prototype.layout = function($container) {
  var ellipsis = this.tabArea.ellipsis,
    htmlContainer = scout.HtmlComponent.get($container),
    containerSize = htmlContainer.availableSize().subtract(htmlContainer.insets());

  this._ensureCachedBounds();

  // compute visible and overflown tabs
  this.preferredLayoutSize($container, {
    widthHint: containerSize.width
  });

  if (this.overflowTabs.length > 0) {
    ellipsis.setHidden(false);
  }

  this.visibleTabs.forEach(function(tabItem) {
    tabItem.setTabOverflown(false);
  });

  this.overflowTabs.forEach(function(tabItem) {
    tabItem.setTabOverflown(true);
  });

  if (this.overflowTabs.length === 0) {
    ellipsis.setHidden(true);
  }

  ellipsis.setText(this.overflowTabs.length + '');

  // set childActions to empty array to prevent the menuItems from calling remove.
  ellipsis.setChildActions(this.overflowTabs.map(function(tabItem) {
    var menu = scout.create('Menu', {
      parent: ellipsis,
      text: tabItem.label,
      tabItem: tabItem,
      visible: tabItem.visible
    });
    menu.on('action', function(event) {
      $.log.isDebugEnabled() && $.log.debug('(TabAreaLayout#_onClickEllipsis) tabItem=' + tabItem);
      // first close popup to ensure the focus is handled in the correct focus context.
      ellipsis.popup.close();
      this.tabArea.setSelectedTab(tabItem);
      tabItem.focusTab();
    }.bind(this));
    return menu;
  }, this));

  this._layoutSelectionMarker();
};

scout.TabAreaLayout.prototype._layoutSelectionMarker = function() {
  var $selectionMarker = this.tabArea.$selectionMarker,
    selectedTab = this.tabArea.selectedTab,
    selectedItemBounds;

  if (selectedTab) {
    $selectionMarker.setVisible(true);
    selectedItemBounds = scout.graphics.bounds(selectedTab.$tabContainer);
    $selectionMarker.cssLeft(selectedItemBounds.x);
    $selectionMarker.cssWidth(selectedItemBounds.width);
  } else {
    $selectionMarker.setVisible(false);
  }
};

scout.TabAreaLayout.prototype.preferredLayoutSize = function($container, options) {
  var htmlContainer = scout.HtmlComponent.get($container),
    tabItems = this.tabArea.tabItems,
    ellipsis = this.tabArea.ellipsis,
    ellipsisBoundsGross = new scout.Dimension(),
    preferredSize = new scout.Dimension(),
    _widthHintFilter = function(itemWidth, totalWidth, wHint) {
      // escape truthy
      if (wHint === 0 || wHint) {
        return totalWidth + itemWidth <= wHint;
      }
      return true;
    },
    unorderedVisibleTabs = [];

  this.overflowTabs = [];
  this.visibleTabs = [];

  this._ensureCachedBounds();
  ellipsisBoundsGross = ellipsis.htmlComp._cachedPrefSize.add(ellipsis.htmlComp._cachedMargins);

  // add the ellipsis width, it will be removed if no ellipsis is needed later.
  preferredSize.width += ellipsisBoundsGross.width;

  tabItems.filter(function(tabItem) {
    // filter for not active tabs and add the active already to the visible tabs, the active tab is always visible.
    var prefSizeGross;
    if (!tabItem.isVisible()) {
      return false;
    }
    if (tabItem._tabActive) {
      prefSizeGross = tabItem.tabHtmlComp._cachedPrefSize.add(tabItem.tabHtmlComp._cachedMargins);
      preferredSize.width += prefSizeGross.width;
      preferredSize.height = Math.max(preferredSize.height, prefSizeGross.height);
      unorderedVisibleTabs.push(tabItem);
      return false;
    }
    return true;

  }).forEach(function(tabItem, index, items) {
    var prefSizeGross = tabItem.tabHtmlComp._cachedPrefSize.add(tabItem.tabHtmlComp._cachedMargins),
      totalWidth = preferredSize.width;

    if (index === items.length - 1 && this.overflowTabs.length === 0) {
      totalWidth -= ellipsisBoundsGross.width;
    }

    // if one item is already overflowed all following items will be overflowed as well.
    if (this.overflowTabs.length > 0) {
      this.overflowTabs.push(tabItem);
      return;
    }
    if (_widthHintFilter(prefSizeGross.width, totalWidth, options.widthHint)) {
      preferredSize.width += prefSizeGross.width;
      preferredSize.height = Math.max(preferredSize.height, prefSizeGross.height);
      unorderedVisibleTabs.push(tabItem);
    } else {
      this.overflowTabs.push(tabItem);
    }
  }, this);

  if (this.overflowTabs.length === 0) {
    preferredSize.width -= ellipsisBoundsGross.width;
  } else {
    preferredSize.height = Math.max(preferredSize.height, ellipsisBoundsGross.height);
  }
  // well order and push to visible tabs
  tabItems.filter(function(tabItem) {
    if (unorderedVisibleTabs.indexOf(tabItem) >= 0) {
      this.visibleTabs.push(tabItem);
    }
  }, this);

  return preferredSize.add(htmlContainer.insets());
};

scout.TabAreaLayout.prototype.invalidate = function() {
  var allItems = this.tabArea.tabItems.slice();

  this.tabArea.ellipsis.htmlComp._cachedPrefSize = null;
  this.tabArea.ellipsis.htmlComp._cachedMargins = null;
  allItems.forEach(function(item) {
    if (item.tabHtmlComp) {
      item.tabHtmlComp._cachedPrefSize = null;
      item.tabHtmlComp._cachedMargins = null;
    }
  });
};

scout.TabAreaLayout.prototype._ensureCachedBounds = function() {
  var htmlComps = this.tabArea.tabItems.filter(function(item) {
    return item.isVisible();
  }).map(function(item) {
    return item.tabHtmlComp;
  });
  htmlComps.push(this.tabArea.ellipsis.htmlComp);
  var classList;
  htmlComps.forEach(function(htmlComp) {
    if (!htmlComp._cachedPrefSize || !htmlComp._cachedMargins) {
      classList = htmlComp.$comp.attr('class');

      htmlComp.$comp.removeClass('overflown');
      htmlComp.$comp.removeClass('hidden');

      htmlComp._cachedPrefSize = htmlComp.prefSize({
        useCssSize: true,
        exact: true
      });
      htmlComp._cachedMargins = scout.graphics.margins(htmlComp.$comp);
      htmlComp.$comp.attrOrRemove('class', classList);
    }
  });
};

scout.TabAreaLayout.prototype._onTabAreaPropertyChange = function(event) {
  if (event.propertyName === 'selectedTab') {
    this._layoutSelectionMarker();
  }
};

scout.TabAreaLayout.prototype._updateFirstMarker = function() {
  this.tabArea.tabItems.forEach(function(tab, i, tabs) {
    tab.$tabContainer.toggleClass('first', i === 0);
  }, this);
};
