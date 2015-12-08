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
scout.DesktopTabBarLayout = function(desktop) {
  scout.DesktopTabBarLayout.parent.call(this);

  this.TAB_WIDTH_LARGE = 220;
  this.TAB_WIDTH_SMALL = 130;
  this._desktop = desktop;
  this._$overflowTab;
  this._overflowTabsIndizes = [];
};
scout.inherits(scout.DesktopTabBarLayout, scout.AbstractLayout);

// create a clone to measure pref. width
scout.DesktopTabBarLayout.prototype._toolsWidth = function($tools, cssClasses) {
  var $clone = $tools.clone(),
    $items = $clone.find('.taskbar-tool-item');

  $items
    .removeClass('min-padding')
    .removeClass('icon-only');

  if (cssClasses) {
    $items.addClass(cssClasses);
  }
  $clone.width('auto').appendTo(this._desktop.session.$entryPoint);
  var toolsWidth = scout.graphics.getSize($clone, true).width;
  $clone.remove();
  return toolsWidth;
};

/**
 * @override AbstractLayout.js
 */
scout.DesktopTabBarLayout.prototype.layout = function($container) {
  var $tabs = $container.find('.desktop-view-tabs'),
    $tools = $container.find('.taskbar-tools'),
    contWidth = scout.graphics.getSize($container).width,
    numTabs = this._desktop.viewTabsController.viewTabCount(),
    largePrefTabsWidth = numTabs * this.TAB_WIDTH_LARGE,
    smallPrefTabsWidth = numTabs * this.TAB_WIDTH_SMALL,
    logoWidth = 0,
    toolsWidth, tabsWidth;

  if (this._desktop.session.uiUseTaskbarLogo) {
    var $logo = $container.find('.taskbar-logo');
    logoWidth = scout.graphics.getSize($logo, true).width;
  }

  // reset tabs and tool-items
  if (this._$overflowTab) {
    this._$overflowTab.remove();
  }

  $tabs.find('.desktop-view-tab').setVisible(true);

  $tools.find('.taskbar-tool-item').each(function() {
    var $item = $(this);
    $item.removeClass('min-padding');
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
    $tools.find('.taskbar-tool-item').each(function() {
      $(this).addClass('min-padding');
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
    $tools.find('.taskbar-tool-item').each(function() {
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

    // FIXME AWE: display correct range of tabs (around visible tab)
    // FIXME AWE: tabs have no 'selected' state, this must be added together with activeForm on model Desktop
    // Never put selected tab into overflow
    var i = 0,
      selectedIndex, tab;
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
};

scout.DesktopTabBarLayout.prototype._onMouseDownOverflow = function(event) {
  var menu, tab, text, popup, overflowMenus = [],
    desktop = this._desktop,
    that = this;
  this._overflowTabsIndizes.forEach(function(i) {
    // FIXME AWE: fix bugs in overflow-menu:
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
        $.log.debug('(DesktopTaskBarLayout#_onMouseDownOverflow) tab=' + this);
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
