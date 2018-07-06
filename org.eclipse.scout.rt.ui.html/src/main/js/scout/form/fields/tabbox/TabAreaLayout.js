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
  this.tabArea.one('remove', function() {
    this.tabArea.off('propertyChange', this._tabAreaPropertyChangeHandler);
  }.bind(this));
};
scout.inherits(scout.TabAreaLayout, scout.AbstractLayout);

scout.TabAreaLayout.prototype.layout = function($container) {
  var ellipsis = this.tabArea.ellipsis,
    htmlContainer = scout.HtmlComponent.get($container),
    containerSize = htmlContainer.availableSize().subtract(htmlContainer.insets());

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
  var htmlComp = scout.HtmlComponent.get($container),
    prefSize = new scout.Dimension(0, 0),
    prefWidth = Number.MAX_VALUE,
    visibleTabItems = this.tabArea.tabItems.filter(function(tabItem) {
      return tabItem.isVisible();
    }),
    overflowableIndexes = visibleTabItems.map(function(tabItem, index) {
      if (tabItem._tabActive) {
        return -1;
      }
      return index;
    }).filter(function(index) {
      return index >= 0;
    });

  this.overflowTabs = [];

  //consider avoid falsy 0 in tabboxes a 0 withHint will be used to calculate the minimum width
  if (options.widthHint === 0 || options.widthHint) {
    prefWidth = options.widthHint - htmlComp.insets().horizontal();
  }

  // shortcut for minimum size.
  if (prefWidth <= 0) {
    return this._minSize(visibleTabItems).add(htmlComp.insets());
  }

  var overflowIndex = -1;
  this._setFirstLastMarker(visibleTabItems);
  prefSize = this._prefSize(visibleTabItems);
  while (prefSize.width > prefWidth && overflowableIndexes.length > 0) {
    overflowIndex = overflowableIndexes.splice(-1)[0];
    this.overflowTabs.splice(0, 0, visibleTabItems[overflowIndex]);
    visibleTabItems.splice(overflowIndex, 1);
    this._setFirstLastMarker(visibleTabItems);
    prefSize = this._prefSize(visibleTabItems);
  }

  this.visibleTabs = visibleTabItems;
  return prefSize.add(htmlComp.insets());
};

scout.TabAreaLayout.prototype._minSize = function(tabItems) {
  var visibleTabItems = [],
    prefSize;
  this.overflowTabs = tabItems.filter(function(tabItem) {
    if (tabItem._tabActive) {
      visibleTabItems.push(tabItem);
      return false;
    }
    return true;
  }, this);

  this.visibleTabs = visibleTabItems;
  this._setFirstLastMarker(visibleTabItems);
  prefSize = this._prefSize(visibleTabItems);

  return prefSize;
};

scout.TabAreaLayout.prototype._prefSize = function(tabItems, considerEllipsis) {
  var prefSize = tabItems.map(function(tabItem) {
      return this._tabItemSize(tabItem.tabHtmlComp);
    }, this).reduce(function(prefSize, itemSize) {
      prefSize.height = Math.max(prefSize.height, itemSize.height);
      prefSize.width += itemSize.width;
      return prefSize;
    }, new scout.Dimension(0, 0)),
    ellipsisSize = new scout.Dimension(0, 0);

  considerEllipsis = scout.nvl(considerEllipsis, this.overflowTabs.length > 0);
  if (considerEllipsis) {
    ellipsisSize = this._tabItemSize(this.tabArea.ellipsis.htmlComp);
    prefSize.height = Math.max(prefSize.height, ellipsisSize.height);
    prefSize.width += ellipsisSize.width;
  }
  return prefSize;
};

scout.TabAreaLayout.prototype._setFirstLastMarker = function(tabItems, considerEllipsis) {
  considerEllipsis = scout.nvl(considerEllipsis, this.overflowTabs.length > 0);

  // reset
  this.tabArea.tabItems.forEach(function(tabItem) {
    tabItem.tabHtmlComp.$comp.removeClass('first last');
  });
  this.tabArea.ellipsis.$container.removeClass('first last');

  // set first and last
  if (tabItems.length > 0) {
    tabItems[0].tabHtmlComp.$comp.addClass('first');
    if (considerEllipsis) {
      this.tabArea.ellipsis.$container.addClass('last');
    } else {
      tabItems[tabItems.length - 1].tabHtmlComp.$comp.addClass('last');
    }
  }
};

scout.TabAreaLayout.prototype._tabItemSize = function(htmlComp) {
  var prefSize,
    classList = htmlComp.$comp.attr('class');

  htmlComp.$comp.removeClass('overflown');
  htmlComp.$comp.removeClass('hidden');

  prefSize = htmlComp.prefSize({
    useCssSize: true,
    exact: true
  }).add(scout.graphics.margins(htmlComp.$comp));

  htmlComp.$comp.attrOrRemove('class', classList);
  return prefSize;
};

scout.TabAreaLayout.prototype._onTabAreaPropertyChange = function(event) {
  if (event.propertyName === 'selectedTab') {
    this._layoutSelectionMarker();
  }
};
