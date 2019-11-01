/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout} from '../../../index';
import {HtmlComponent} from '../../../index';
import {scout} from '../../../index';
import {TabArea} from '../../../index';
import {graphics} from '../../../index';
import {Dimension} from '../../../index';
import * as $ from 'jquery';

export default class TabAreaLayout extends AbstractLayout {

constructor(tabArea) {
  super();
  this.tabArea = tabArea;
  this.overflowTabs = [];
  this.visibleTabs = [];
  this._tabAreaPropertyChangeHandler = this._onTabAreaPropertyChange.bind(this);

  this.tabArea.on('propertyChange', this._tabAreaPropertyChangeHandler);
  this.tabArea.one('remove', function() {
    this.tabArea.off('propertyChange', this._tabAreaPropertyChangeHandler);
  }.bind(this));
}


layout($container) {
  var ellipsis = this.tabArea.ellipsis,
    htmlContainer = HtmlComponent.get($container),
    containerSize = htmlContainer.availableSize().subtract(htmlContainer.insets());

  // compute visible and overflown tabs
  this.preferredLayoutSize($container, {
    widthHint: containerSize.width
  });

  if (this.overflowTabs.length > 0) {
    ellipsis.setHidden(false);
  }

  this.visibleTabs.forEach(function(tabItem) {
    tabItem.setOverflown(false);
  });

  this.overflowTabs.forEach(function(tabItem) {
    tabItem.setOverflown(true);
  });

  if (this.overflowTabs.length === 0) {
    ellipsis.setHidden(true);
  }

  ellipsis.setText(this.overflowTabs.length + '');

  ellipsis.setChildActions(this.overflowTabs.map(function(tab) {
    var menu = scout.create('Menu', {
      parent: ellipsis,
      text: tab.label,
      tab: tab,
      visible: tab.visible
    });
    menu.on('action', function(event) {
      $.log.isDebugEnabled() && $.log.debug('(TabAreaLayout#_onClickEllipsis) tabItem=' + tab);
      // first close popup to ensure the focus is handled in the correct focus context.
      ellipsis.popup.close();
      tab.select();
      tab.focus();
    }.bind(this));
    return menu;
  }, this));

  this._layoutSelectionMarker();
}

_layoutSelectionMarker() {
  var $selectionMarker = this.tabArea.$selectionMarker,
    selectedTab = this.tabArea.selectedTab,
    selectedItemBounds;

  if (selectedTab) {
    $selectionMarker.setVisible(true);
    selectedItemBounds = graphics.bounds(selectedTab.$container);
    $selectionMarker.cssLeft(selectedItemBounds.x);
    $selectionMarker.cssWidth(selectedItemBounds.width);
  } else {
    $selectionMarker.setVisible(false);
  }
}

preferredLayoutSize($container, options) {
  var htmlComp = HtmlComponent.get($container),
    prefSize = new Dimension(0, 0),
    prefWidth = Number.MAX_VALUE,
    visibleTabItems = this.tabArea.tabs.filter(function(tabItem) {
      return tabItem.isVisible();
    }),
    overflowableIndexes = visibleTabItems.map(function(tabItem, index) {
      if (tabItem.selected) {
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

  // Use the total available space if spreading tabs evenly.
  if (this.tabArea.displayStyle === TabArea.DisplayStyle.SPREAD_EVEN) {
    return graphics.prefSize($container, options);
  }

  return graphics.exactPrefSize(prefSize.add(htmlComp.insets()), options);
}

_minSize(tabItems) {
  var visibleTabItems = [],
    prefSize;
  this.overflowTabs = tabItems.filter(function(tabItem) {
    if (tabItem.selected) {
      visibleTabItems.push(tabItem);
      return false;
    }
    return true;
  }, this);

  this.visibleTabs = visibleTabItems;
  this._setFirstLastMarker(visibleTabItems);
  prefSize = this._prefSize(visibleTabItems);

  return prefSize;
}

_prefSize(tabItems, considerEllipsis) {
  var prefSize = tabItems.map(function(tabItem) {
      return this._tabItemSize(tabItem.htmlComp);
    }, this).reduce(function(prefSize, itemSize) {
      prefSize.height = Math.max(prefSize.height, itemSize.height);
      prefSize.width += itemSize.width;
      return prefSize;
    }, new Dimension(0, 0)),
    ellipsisSize = new Dimension(0, 0);

  considerEllipsis = scout.nvl(considerEllipsis, this.overflowTabs.length > 0);
  if (considerEllipsis) {
    ellipsisSize = this._tabItemSize(this.tabArea.ellipsis.htmlComp);
    prefSize.height = Math.max(prefSize.height, ellipsisSize.height);
    prefSize.width += ellipsisSize.width;
  }
  return prefSize;
}

_setFirstLastMarker(tabItems, considerEllipsis) {
  considerEllipsis = scout.nvl(considerEllipsis, this.overflowTabs.length > 0);

  // reset
  this.tabArea.tabs.forEach(function(tabItem) {
    tabItem.htmlComp.$comp.removeClass('first last');
  });
  this.tabArea.ellipsis.$container.removeClass('first last');

  // set first and last
  if (tabItems.length > 0) {
    tabItems[0].$container.addClass('first');
    if (considerEllipsis) {
      this.tabArea.ellipsis.$container.addClass('last');
    } else {
      tabItems[tabItems.length - 1].$container.addClass('last');
    }
  }
}

_tabItemSize(htmlComp) {
  var prefSize,
    classList = htmlComp.$comp.attr('class');

  // temporarly revert display style to default. otherwise the pref size of the tab item will be the size of the container.
  if (this.tabArea.displayStyle === TabArea.DisplayStyle.SPREAD_EVEN) {
    this.tabArea.$container.removeClass('spread-even');
  }

  htmlComp.$comp.removeClass('overflown');
  htmlComp.$comp.removeClass('hidden');

  prefSize = htmlComp.prefSize({
    useCssSize: true,
    exact: true
  }).add(graphics.margins(htmlComp.$comp));

  htmlComp.$comp.attrOrRemove('class', classList);

  if (this.tabArea.displayStyle === TabArea.DisplayStyle.SPREAD_EVEN) {
    this.tabArea.$container.addClass('spread-even');
  }
  return prefSize;
}

_onTabAreaPropertyChange(event) {
  if (event.propertyName === 'selectedTab') {
    this._layoutSelectionMarker();
  }
}
}
