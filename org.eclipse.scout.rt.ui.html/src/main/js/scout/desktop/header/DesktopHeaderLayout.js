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
scout.DesktopHeaderLayout = function(header) {
  scout.DesktopHeaderLayout.parent.call(this);

  this.TAB_WIDTH_LARGE = 220;
  this.TAB_WIDTH_SMALL = 130;
  this.header = header;
  this.desktop = header.desktop;
  this._$overflowTab;
  this._overflowTabsIndizes = [];
};
scout.inherits(scout.DesktopHeaderLayout, scout.AbstractLayout);

// create a clone to measure pref. width
scout.DesktopHeaderLayout.prototype._toolsWidth = function($tools, cssClasses) {
  var $clone = $tools.clone(),
    $items = $clone.find('.header-tool-item');

  $items
    .removeClass('compact')
    .removeClass('icon-only');

  if (cssClasses) {
    $items.addClass(cssClasses);
  }
  $clone.width('auto').appendTo(this.desktop.$container);
  var toolsWidth = scout.graphics.getSize($clone, true).width;
  $clone.remove();
  return toolsWidth;
};

/**
 * TODO awe: this layout works primarily with DOM elements and jQuery selectors, it would be better
 * to use the model objects instead. This should be considered, when this layout is refactored the next time.
 *
 * @override AbstractLayout.js
 */
scout.DesktopHeaderLayout.prototype.layout = function($container) {
  var $tabs = $container.find('.desktop-view-tabs'),
    $tools = $container.find('.header-tools'),
    contWidth = scout.graphics.getSize($container).width,
    numTabs = this.desktop.viewTabsController.viewTabCount(),
    smallPrefTabsWidth = numTabs * this.TAB_WIDTH_SMALL,
    logoWidth = 0,
    toolsWidth, tabsWidth;

  if (this.header.logo) {
    logoWidth = scout.graphics.getSize(this.header.logo.$container, true).width;
  }

  // reset tabs and tool-items
  if (this._$overflowTab) {
    this._$overflowTab.remove();
  }

  $tabs.find('.desktop-view-tab').setVisible(true);

  $tools.find('.header-tool-item').each(function() {
    var $item = $(this);
    $item.removeClass('compact');
    var dataText = $item.data('item-text');
    if (dataText) {
      var $title = $item.find('.text');
      $title.text(dataText);
    }
  });

  toolsWidth = this._toolsWidth($tools);
  tabsWidth = contWidth - toolsWidth - logoWidth;
  $tools.cssLeft(contWidth - toolsWidth - logoWidth);

  this._overflowTabsIndizes = [];
  var tabWidth;
  if (smallPrefTabsWidth <= tabsWidth) {
    tabWidth = Math.min(this.TAB_WIDTH_LARGE, Math.floor(tabsWidth / numTabs));
    // 2nd - all Tabs fit when they have small size
    $tabs.find('.desktop-view-tab').each(function() {
      $(this).outerWidth(tabWidth);
    });
  } else {

    // 1st try to minimize padding around tool-bar items
    // re-calculate tabsWidth with reduced padding on the tool-bar-items
    $tools.find('.header-tool-item').each(function() {
      $(this).addClass('compact');
    });

    toolsWidth = scout.graphics.getSize($tools, true).width;
    tabsWidth = contWidth - toolsWidth - logoWidth;
    $tools.cssLeft(contWidth - toolsWidth - logoWidth);

    if (smallPrefTabsWidth <= tabsWidth) {
      tabWidth = this.TAB_WIDTH_SMALL;
      $tabs.find('.desktop-view-tab').each(function() {
        $(this).outerWidth(tabWidth);
      });
      return;
    }

    // 2nd remove text from tool-bar items, only show icon
    $tools.find('.header-tool-item').each(function() {
      var $item = $(this),
        $title = $item.find('.text'),
        text = $title.text();
      $title.empty();
      $item.data('item-text', text);
    });

    toolsWidth = scout.graphics.getSize($tools, true).width;
    tabsWidth = contWidth - toolsWidth - logoWidth;
    $tools.cssLeft(contWidth - toolsWidth - logoWidth);

    if (smallPrefTabsWidth <= tabsWidth) {
      tabWidth = this.TAB_WIDTH_SMALL;
      $tabs.find('.desktop-view-tab').each(function() {
        $(this).outerWidth(tabWidth);
      });
      return;
    }

    // Still doesn't fit? Put tabs into overflow menu
    tabsWidth -= 30;

    // check how many tabs fit into remaining tabsWidth
    var numVisibleTabs = Math.floor(tabsWidth / this.TAB_WIDTH_SMALL),
      numOverflowTabs = numTabs - numVisibleTabs;

    var i = 0,
      selectedIndex = 0;
    $tabs.find('.desktop-view-tab').each(function() {
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

    this._$overflowTab = $tabs
      .appendDiv('overflow-tab-item')
      .on('mousedown', this._onMouseDownOverflow.bind(this));
    if (numOverflowTabs > 1) {
      this._$overflowTab.appendDiv('num-tabs').text(numOverflowTabs);
    }

    var that = this;
    tabWidth = this.TAB_WIDTH_SMALL;
    i = 0;
    $tabs.find('.desktop-view-tab').each(function() {
      if (i >= leftEnd && i <= rightEnd) {
        $(this).outerWidth(tabWidth);
      } else {
        $(this).setVisible(false);
        that._overflowTabsIndizes.push(i);
      }
      i++;
    });
  }

  // Make sure open popups are at the correct position after layouting
  this.desktop.actions
    .filter(function(action) {
      return action.selected && action.popup;
    })
    .some(function(action) {
      action.popup.position();
      return true;
    });
};

scout.DesktopHeaderLayout.prototype._onMouseDownOverflow = function(event) {
  var menu, tab, popup, overflowMenus = [],
    desktop = this.desktop;
  this._overflowTabsIndizes.forEach(function(i) {
    // FIXME awe: fix bugs in overflow-menu:
    // - 1. menu schliesst nicht
    // - 2. manchmal verschwindet ein (noch offener) Tab - wenn nur einer sichtbar ist
    // - 3. add selenium tests
    tab = desktop.viewTabsController.viewTabs()[i];
    menu = scout.create('Menu', {
      parent: desktop,
      text: tab.getMenuText(),
      tab: tab
    });
    menu.remoteHandler = function(event) {
      if ('doAction' === event.type) {
        $.log.debug('(DesktopHeaderLayout#_onMouseDownOverflow) tab=' + this);
        desktop.viewTabsController.selectViewTab(this);
      }
    }.bind(tab);
    overflowMenus.push(menu);
  });

  popup = scout.create('ContextMenuPopup', {
    parent: desktop,
    menuItems: overflowMenus,
    cloneMenuItems: false,
    location: {
      x: event.pageX,
      y: event.pageY
    }
  });
  popup.open(undefined, event);
};
