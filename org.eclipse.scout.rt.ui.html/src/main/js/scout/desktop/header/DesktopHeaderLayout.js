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

  this.header = header;
  this.desktop = header.desktop;
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
 * @override AbstractLayout.js
 */
scout.DesktopHeaderLayout.prototype.layout = function($container) {
  var viewButtonsSize, toolsWidth, tabsWidth,
    htmlContainer = this.header.htmlComp,
    containerSize = htmlContainer.getSize(),
    $tools = this.header.$toolBar || $(), // toolbar may be invisible and therefore null
    $toolItems = $tools.find('.header-tool-item'),
    viewButtons = this.header.viewButtons,
    viewTabs = this.header.viewTabs,
    smallTabsPrefSize = viewTabs.htmlComp.layout.smallPrefSize(),
    tabsPrefSize = viewTabs.htmlComp.getPreferredSize(),
    logoWidth = 0,
    viewButtonsWidth = 0;

  containerSize = containerSize.subtract(htmlContainer.getInsets());

  if (this.header.logo) {
    logoWidth = scout.graphics.getSize(this.header.logo.$container, true).width;
  }

  // reset tabs and tool-items
  if (this._$overflowTab) {
    this._$overflowTab.remove();
  }

  if (viewButtons) {
    viewButtonsSize = viewButtons.htmlComp.getSize();
    viewButtonsWidth = viewButtonsSize.width;
    viewButtons.htmlComp.setSize(viewButtonsSize.subtract(viewButtons.htmlComp.getMargins()));
  }
  viewTabs.htmlComp.$comp.cssLeft(viewButtonsWidth);

  $toolItems.each(function() {
    var $item = $(this);
    $item.removeClass('compact');
    var dataText = $item.data('item-text');
    if (dataText) {
      var $title = $item.find('.text');
      $title.text(dataText);
    }
  });

  toolsWidth = this._toolsWidth($tools);
  tabsWidth = containerSize.width - toolsWidth - logoWidth - viewButtonsWidth;
  $tools.cssLeft(containerSize.width - toolsWidth - logoWidth);

  this._overflowTabsIndizes = [];
  if (smallTabsPrefSize.width <= tabsWidth) {
    // All tabs fit when they have small size -> use available size but max the pref size -> prefSize = size of maximumtabs if tabs use their large (max) size
    tabsWidth = Math.min(tabsPrefSize.width, tabsWidth);
    viewTabs.htmlComp.setSize(new scout.Dimension(tabsWidth, tabsPrefSize.height));
  } else {

    // 1st try to minimize padding around tool-bar items
    // re-calculate tabsWidth with reduced padding on the tool-bar-items
    $toolItems.each(function() {
      $(this).addClass('compact');
    });

    toolsWidth = scout.graphics.getSize($tools, true).width;
    tabsWidth = containerSize.width - toolsWidth - logoWidth - viewButtonsWidth;
    $tools.cssLeft(containerSize.width - toolsWidth - logoWidth);

    if (smallTabsPrefSize.width <= tabsWidth) {
      viewTabs.htmlComp.setSize(smallTabsPrefSize);
      return;
    }

    // 2nd remove text from tool-bar items, only show icon
    $toolItems.each(function() {
      var $item = $(this),
        $title = $item.find('.text'),
        text = $title.text();
      $title.empty();
      $item.data('item-text', text);
    });

    toolsWidth = scout.graphics.getSize($tools, true).width;
    tabsWidth = containerSize.width - toolsWidth - logoWidth - viewButtonsWidth;
    $tools.cssLeft(containerSize.width - toolsWidth - logoWidth);

    tabsWidth = Math.min(smallTabsPrefSize.width, tabsWidth);
    viewTabs.htmlComp.setSize(new scout.Dimension(tabsWidth, tabsPrefSize.height));
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
