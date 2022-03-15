/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, Dimension, graphics, HtmlComponent, scout, TabArea} from '../../../index';
import $ from 'jquery';

export default class TabAreaLayout extends AbstractLayout {

  /**
   * @param {TabArea} tabArea
   */
  constructor(tabArea) {
    super();
    this.tabArea = tabArea;
    this.overflowTabs = [];
    this.visibleTabs = [];
    this._tabAreaPropertyChangeHandler = this._onTabAreaPropertyChange.bind(this);

    this.tabArea.on('propertyChange', this._tabAreaPropertyChangeHandler);
    this.tabArea.one('remove', () => {
      this.tabArea.off('propertyChange', this._tabAreaPropertyChangeHandler);
    });
  }

  layout($container) {
    let htmlContainer = HtmlComponent.get($container),
      containerSize = htmlContainer.availableSize().subtract(htmlContainer.insets());

    // compute visible and overflown tabs
    this.preferredLayoutSize($container, {
      widthHint: containerSize.width
    });
    this._layoutSelectionMarker();
  }

  _layoutSelectionMarker() {
    let $selectionMarker = this.tabArea.$selectionMarker,
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

  _updateEllipsis() {
    let ellipsis = this.tabArea.ellipsis;
    ellipsis.setHidden(this.overflowTabs.length < 1);
    ellipsis.setText(this.overflowTabs.length + '');
    this.visibleTabs.forEach(tabItem => tabItem.setOverflown(false));
    this.overflowTabs.forEach(tabItem => tabItem.setOverflown(true));

    ellipsis.setChildActions(this.overflowTabs.map(tab => {
      let menu = scout.create('Menu', {
        parent: ellipsis,
        text: tab.label,
        tab: tab,
        visible: tab.visible
      });
      menu.on('action', event => {
        $.log.isDebugEnabled() && $.log.debug('(TabAreaLayout#_onClickEllipsis) tabItem=' + tab);
        // first close popup to ensure the focus is handled in the correct focus context.
        ellipsis.popup.close();
        tab.select();
        tab.focus();
      });
      return menu;
    }, this));
  }

  preferredLayoutSize($container, options) {
    let htmlComp = HtmlComponent.get($container),
      prefSize = new Dimension(0, 0),
      prefWidth = Number.MAX_VALUE;
    this.visibleTabs = this.tabArea.visibleTabs();
    let overflowableIndexes = this.visibleTabs.map((tabItem, index) => {
      if (tabItem.selected) {
        return -1;
      }
      return index;
    }).filter(index => index >= 0);

    this.overflowTabs = [];

    // consider avoid falsy 0 in tab-boxes a 0 withHint will be used to calculate the minimum width
    if (options.widthHint === 0 || options.widthHint) {
      prefWidth = options.widthHint - htmlComp.insets().horizontal();
    }

    // shortcut for minimum size.
    if (prefWidth <= 0) {
      return this._minSize(this.visibleTabs).add(htmlComp.insets());
    }

    this._setFirstLastMarker(this.visibleTabs);
    this._updateEllipsis();
    prefSize = this._prefSize(this.visibleTabs);

    while (prefSize.width > prefWidth && overflowableIndexes.length > 0) {
      let overflowIndex = overflowableIndexes.splice(-1)[0];
      this.overflowTabs.splice(0, 0, this.visibleTabs[overflowIndex]);
      this.visibleTabs.splice(overflowIndex, 1);
      this._setFirstLastMarker(this.visibleTabs);
      this._updateEllipsis(); // update ellipsis here already so that the prefSize on the next line is correct
      prefSize = this._prefSize(this.visibleTabs);
    }

    // Use the total available space if spreading tabs evenly.
    if (this.tabArea.displayStyle === TabArea.DisplayStyle.SPREAD_EVEN) {
      return graphics.prefSize($container, options);
    }
    return graphics.exactPrefSize(prefSize.add(htmlComp.insets()), options);
  }

  _minSize(tabItems) {
    let visibleTabItems = [];
    this.overflowTabs = tabItems.filter(tabItem => {
      if (tabItem.selected) {
        visibleTabItems.push(tabItem);
        return false;
      }
      return true;
    }, this);

    this.visibleTabs = visibleTabItems;
    this._setFirstLastMarker(visibleTabItems);
    return this._prefSize(visibleTabItems);
  }

  _prefSize(tabItems, considerEllipsis) {
    let prefSize = tabItems
      .map(tabItem => this._tabItemSize(tabItem))
      .reduce((prefSize, itemSize) => {
        prefSize.height = Math.max(prefSize.height, itemSize.height);
        prefSize.width += itemSize.width;
        return prefSize;
      }, new Dimension(0, 0));

    considerEllipsis = scout.nvl(considerEllipsis, this.overflowTabs.length > 0);
    if (considerEllipsis) {
      let ellipsisSize = this._tabItemSize(this.tabArea.ellipsis);
      prefSize.height = Math.max(prefSize.height, ellipsisSize.height);
      prefSize.width += ellipsisSize.width;
    }
    return prefSize;
  }

  _setFirstLastMarker(tabItems, considerEllipsis) {
    considerEllipsis = scout.nvl(considerEllipsis, this.overflowTabs.length > 0);

    // reset
    this.tabArea.tabs.forEach(tabItem => {
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

  _tabItemSize(item) {
    let htmlComp = item.htmlComp, prefSize,
      classList = htmlComp.$comp.attr('class');

    // temporarily revert display style to default. otherwise the pref size of the tab item will be the size of the container.
    if (this.tabArea.displayStyle === TabArea.DisplayStyle.SPREAD_EVEN) {
      this.tabArea.$container.removeClass('spread-even');
    }

    htmlComp.$comp.removeClass('overflown');
    htmlComp.$comp.removeClass('hidden');

    prefSize = htmlComp.prefSize({
      exact: true
    }).add(graphics.margins(htmlComp.$comp));
    if (item.fieldStatus && item.fieldStatus.htmlComp) {
      let statusOverflownAndHidden = item.overflown && !item.fieldStatus.visible;
      if (statusOverflownAndHidden) {
        // overflown tabs have no fieldStatus: explicitly set to visible so that the real consumed space can be computed
        item.fieldStatus.setVisible(true);
      }
      let statusWidth = item.fieldStatus.htmlComp.prefSize({includeMargin: true}).width;
      if (statusOverflownAndHidden) {
        // restore
        item.fieldStatus.setVisible(false);
      }
      prefSize.width += statusWidth;
    }

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
