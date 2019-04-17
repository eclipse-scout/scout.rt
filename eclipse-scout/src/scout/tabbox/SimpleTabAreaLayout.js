import AbstractLayout from '../layout/AbstractLayout';
import Dimension from '../util/Dimension';
import * as scout from '../scout';
import * as graphics from '../util/graphics';

export default class SimpleTabAreaLayout extends AbstractLayout {

  constructor(tabArea) {
    super();
    this.tabArea = tabArea;
    this._$overflowTab;
    this._overflowTabsIndizes = [];
  }

  /**
   * @override AbstractLayout.js
   */
  layout($container) {
    var tabWidth,
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
      tabWidth = Math.min(TAB_WIDTH_LARGE, containerSize.width / numTabs);
      // 2nd - all Tabs fit when they have small size
      $tabs.each(function() {
        $(this).outerWidth(tabWidth);
      });
      return;
    }

    // Not all tabs fit in container -> put tabs into overflow menu
    containerSize.width -= OVERFLOW_MENU_WIDTH;

    // check how many tabs fit into remaining containerSize.width
    var numVisibleTabs = Math.floor(containerSize.width / TAB_WIDTH_SMALL),
      numOverflowTabs = numTabs - numVisibleTabs;

    var i = 0,
      selectedIndex = 0;
    $tabs.each(function() {
      if ($(this).hasClass('selected')) {
        selectedIndex = i;
      }
      i++;
    });

    // determine visible range
    var rightEnd, leftEnd = selectedIndex - Math.floor(numVisibleTabs / 2);
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

    var that = this;
    tabWidth = TAB_WIDTH_SMALL;
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
  };

  smallPrefSize() {
    var numTabs = this.tabArea.getTabs().length;
    return new Dimension(numTabs * TAB_WIDTH_SMALL, this.tabArea.htmlComp.$comp.outerHeight(true));
  };

  preferredLayoutSize($container) {
    var numTabs = this.tabArea.getTabs().length;
    return new Dimension(numTabs * TAB_WIDTH_LARGE, graphics.prefSize(this.tabArea.htmlComp.$comp, {
      includeMargin: true,
      useCssSize: true
    }).height);
  };

  _onMouseDownOverflow(event) {
    var menu, tab, popup,
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
  };

}

export const TAB_WIDTH_LARGE = 220;
export const TAB_WIDTH_SMALL = 130;
export const OVERFLOW_MENU_WIDTH = 30;
