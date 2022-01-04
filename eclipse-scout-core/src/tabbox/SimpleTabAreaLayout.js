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
import {AbstractLayout, Dimension, graphics, scout, SimpleTabArea, styles, widgets} from '../index';
import $ from 'jquery';

export default class SimpleTabAreaLayout extends AbstractLayout {

  constructor(tabArea) {
    super();
    this.tabArea = tabArea;
    this._$overflowTab = null;
    this._overflowTabsIndizes = [];

    this.tabWidth = null;
    this.tabMinWidth = null;
    this.overflowTabItemWidth = null;
  }

  /**
   * @override AbstractLayout.js
   */
  layout($container) {
    let htmlContainer = this.tabArea.htmlComp,
      containerSize = htmlContainer.size({
        exact: true
      }),
      $tabs = htmlContainer.$comp.children('.simple-tab'),
      numTabs = this.tabArea.getTabs().length,
      smallPrefSize = this.smallPrefSize();

    containerSize = containerSize.subtract(htmlContainer.insets());

    this._initSizes();

    // Reset tabs
    if (this._$overflowTab) {
      this._$overflowTab.remove();
    }
    $tabs.setVisible(true);
    this._overflowTabsIndizes = [];
    widgets.updateFirstLastMarker(this.tabArea.getTabs());

    // All tabs fit in container -> no overflow menu necessary
    if (smallPrefSize.width <= containerSize.width) {
      $container.removeClass('overflown');
      return;
    }

    // Not all tabs fit in container -> put tabs into overflow menu
    $container.addClass('overflown');
    containerSize.width -= this.overflowTabItemWidth;

    // check how many tabs fit into remaining containerSize.width
    let numVisibleTabs = Math.floor(containerSize.width / this.tabMinWidth);
    let numOverflowTabs = numTabs - numVisibleTabs;

    let selectedIndex = 0;
    $tabs.each((i, tab) => {
      if ($(tab).hasClass('selected')) {
        selectedIndex = i;
      }
    });

    // determine visible range
    let rightEnd;
    let leftEnd = selectedIndex - Math.floor(numVisibleTabs / 2);
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
      .appendDiv('simple-overflow-tab-item')
      .on('mousedown', this._onOverflowTabItemMouseDown.bind(this));
    this._$overflowTab.appendDiv('num-tabs').text(numOverflowTabs);

    $tabs.each((i, tab) => {
      if (i < leftEnd || i > rightEnd) {
        $(tab).setVisible(false);
        this._overflowTabsIndizes.push(i);
      }
    });
    widgets.updateFirstLastMarker(this.tabArea.getVisibleTabs());
  }

  smallPrefSize(options = {}) {
    this._initSizes();
    options = $.extend({minTabWidth: this.tabMinWidth}, options);
    return this.preferredLayoutSize(this.tabArea.$container, options);
  }

  preferredLayoutSize($container, options = {}) {
    this._initSizes();
    let minTabWidth = scout.nvl(options.minTabWidth, 0) || scout.nvl(this.tabWidth, 0);
    let numTabs = this.tabArea.getTabs().length;
    let minWidth = numTabs * minTabWidth;
    options = $.extend({useCssSize: true}, options);
    let prefSize = graphics.prefSize(this.tabArea.$container, options);
    if (options.widthHint && this.tabArea.displayStyle === SimpleTabArea.DisplayStyle.SPREAD_EVEN) {
      minWidth = Math.max(options.widthHint, minWidth);
    }
    return new Dimension(minWidth, prefSize.height);
  }

  /**
   * Reads the default sizes from CSS -> the tabs need to specify a width and a min-width.
   * The layout expects all tabs to have the same width.
   */
  _initSizes() {
    if (this.tabWidth != null && this.tabMinWidth != null && this.overflowTabItemWidth != null) {
      return;
    }
    let $tab = this.tabArea.$container.children('.simple-tab').eq(0);
    if ($tab.length === 0) {
      return;
    }
    $tab = $tab.clone().addClass('selected'); // Non selected items have a margin, selected ones don't -> we need to get the width incl. margin
    let tabAreaClasses = this.tabArea.$container.attr('class');
    let tabItemClasses = $tab.attr('class');
    if (this.tabWidth === null) {
      this.tabWidth = styles.getSize([tabAreaClasses, tabItemClasses], 'width', 'width', 0);
    }
    if (this.tabMinWidth === null) {
      this.tabMinWidth = styles.getSize([tabAreaClasses, tabItemClasses], 'min-width', 'minWidth');
    }
    if (this.overflowTabItemWidth === null) {
      this.overflowTabItemWidth = styles.getSize([tabAreaClasses, 'simple-overflow-tab-item'], 'min-width', 'minWidth');
      this.overflowTabItemWidth += styles.getSize([tabAreaClasses, 'simple-overflow-tab-item'], 'margin-left', 'marginLeft');
      this.overflowTabItemWidth += styles.getSize([tabAreaClasses, 'simple-overflow-tab-item'], 'margin-right', 'marginRight');
    }
  }

  _onOverflowTabItemMouseDown(event) {
    let tabArea = this.tabArea;
    let overflowMenus = [];
    let $overflowTabItem = $(event.currentTarget);
    if ($overflowTabItem.data('popup')) {
      $overflowTabItem.data('popup').close();
      return;
    }
    this._overflowTabsIndizes.forEach(i => {
      let tab = this.tabArea.getTabs()[i];
      let menu = scout.create('Menu', {
        parent: this.tabArea,
        text: tab.getMenuText(),
        tab: tab
      });
      menu.on('action', function() {
        $.log.isDebugEnabled() && $.log.debug('(SimpleTabAreaLayout#_onMouseDownOverflow) tab=' + this);
        tabArea.selectTab(this);
      }.bind(tab));
      overflowMenus.push(menu);
    });

    let popup = scout.create('ContextMenuPopup', {
      parent: this.tabArea,
      menuItems: overflowMenus,
      cloneMenuItems: false,
      $anchor: $overflowTabItem,
      closeOnAnchorMouseDown: false
    });
    $overflowTabItem.addClass('selected');
    $overflowTabItem.data('popup', popup);
    popup.one('remove', () => {
      $overflowTabItem.removeClass('selected');
      $overflowTabItem.data('popup', null);
    });
    popup.open();
  }
}
