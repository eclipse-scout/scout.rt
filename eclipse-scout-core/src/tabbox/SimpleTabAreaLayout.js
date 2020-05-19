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
import {AbstractLayout, Dimension, graphics, scout, SimpleTabArea} from '../index';
import $ from 'jquery';

export default class SimpleTabAreaLayout extends AbstractLayout {

  constructor(tabArea) {
    super();
    this.tabArea = tabArea;
    this._$overflowTab = null;
    this._overflowTabsIndizes = [];
  }

  static TAB_WIDTH_LARGE = 220;
  static TAB_WIDTH_SMALL = 130;
  static OVERFLOW_MENU_WIDTH = 30;

  /**
   * @override AbstractLayout.js
   */
  layout($container) {
    let tabWidth,
      htmlContainer = this.tabArea.htmlComp,
      containerSize = htmlContainer.size({
        exact: true
      }),
      $tabs = htmlContainer.$comp.find('.simple-tab'),
      numTabs = this.tabArea.getTabs().length,
      smallPrefSize = this.smallPrefSize();

    containerSize = containerSize.subtract(htmlContainer.insets());

    // reset tabs and tool-items
    if (this._$overflowTab) {
      this._$overflowTab.remove();
    }

    $tabs.setVisible(true);
    this._overflowTabsIndizes = [];

    // All tabs in container
    if (smallPrefSize.width <= containerSize.width) {
      if (this.tabArea.displayStyle === SimpleTabArea.DisplayStyle.SPREAD_EVEN) {
        tabWidth = containerSize.width / numTabs;
      } else {
        tabWidth = Math.min(SimpleTabAreaLayout.TAB_WIDTH_LARGE, containerSize.width / numTabs);
      }
      // 2nd - all Tabs fit when they have small size
      $tabs.each(function() {
        $(this).outerWidth(tabWidth);
      });
      $container.removeClass('overflown');
      return;
    }

    // Not all tabs fit in container -> put tabs into overflow menu
    $container.addClass('overflown');
    containerSize.width -= SimpleTabAreaLayout.OVERFLOW_MENU_WIDTH;

    // check how many tabs fit into remaining containerSize.width
    let numVisibleTabs = Math.floor(containerSize.width / SimpleTabAreaLayout.TAB_WIDTH_SMALL),
      numOverflowTabs = numTabs - numVisibleTabs;

    let i = 0,
      selectedIndex = 0;
    $tabs.each(function() {
      if ($(this).hasClass('selected')) {
        selectedIndex = i;
      }
      i++;
    });

    // determine visible range
    let rightEnd, leftEnd = selectedIndex - Math.floor(numVisibleTabs / 2);
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
      .on('mousedown', this._onMouseDownOverflow.bind(this));
    this._$overflowTab.appendDiv('num-tabs').text(numOverflowTabs);

    let that = this;
    if (this.tabArea.displayStyle === SimpleTabArea.DisplayStyle.SPREAD_EVEN) {
      // Distribute equally. This actually acts as max-width
      tabWidth = containerSize.width / numVisibleTabs;
    } else {
      tabWidth = SimpleTabAreaLayout.TAB_WIDTH_SMALL;
    }
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
  }

  smallPrefSize(options = {}) {
    options = $.extend({minTabWidth: SimpleTabAreaLayout.TAB_WIDTH_SMALL}, options);
    return this.preferredLayoutSize(this.tabArea.$container, options);
  }

  preferredLayoutSize($container, options = {}) {
    let minTabWidth = options.minTabWidth || SimpleTabAreaLayout.TAB_WIDTH_LARGE;
    let numTabs = this.tabArea.getTabs().length;
    let minWidth = numTabs * minTabWidth;
    options = $.extend({useCssSize: true}, options);
    let prefSize = graphics.prefSize(this.tabArea.$container, options);
    if (options.widthHint && this.tabArea.displayStyle === SimpleTabArea.DisplayStyle.SPREAD_EVEN) {
      minWidth = Math.max(options.widthHint, minWidth);
    }
    return new Dimension(minWidth, prefSize.height);
  }

  _onMouseDownOverflow(event) {
    let menu, tab, popup,
      tabArea = this.tabArea,
      overflowMenus = [];

    this._overflowTabsIndizes.forEach(function(i) {
      tab = this.tabArea.getTabs()[i];
      menu = scout.create('Menu', {
        parent: this.tabArea,
        text: tab.getMenuText(),
        tab: tab
      });
      menu.on('action', function() {
        $.log.isDebugEnabled() && $.log.debug('(SimpleTabAreaLayout#_onMouseDownOverflow) tab=' + this);
        tabArea.selectTab(this);
      }.bind(tab));
      overflowMenus.push(menu);
    }, this);

    popup = scout.create('ContextMenuPopup', {
      parent: this.tabArea,
      menuItems: overflowMenus,
      cloneMenuItems: false,
      location: {
        x: event.pageX,
        y: event.pageY
      }
    });
    popup.open();
  }
}
