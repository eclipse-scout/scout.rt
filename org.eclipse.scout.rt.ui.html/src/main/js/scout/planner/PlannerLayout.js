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
scout.PlannerLayout = function(planner) {
  scout.PlannerLayout.parent.call(this);
  this.planner = planner;
};
scout.inherits(scout.PlannerLayout, scout.AbstractLayout);

scout.PlannerLayout.prototype.layout = function($container) {
  var menuBarSize,
    $header = this.planner._header.$container,
    $scale = this.planner.$scale,
    $grid = this.planner.$grid,
    menuBar = this.planner.menuBar,
    $yearContainer = this.planner._yearPanel.$container,
    menuBarHeight = 0,
    gridHeight = 0,
    yearContainerHeight = 0,
    gridTop = 0,
    scaleTop = 0,
    htmlMenuBar = scout.HtmlComponent.get(menuBar.$container),
    htmlContainer = this.planner.htmlComp,
    containerSize = htmlContainer.getAvailableSize()
    .subtract(htmlContainer.getInsets());

  if (menuBar.$container.isVisible()) {
    menuBarSize = scout.MenuBarLayout.size(htmlMenuBar, containerSize);
    htmlMenuBar.setSize(menuBarSize);
    menuBarHeight = menuBarSize.height;
    if (menuBar.position === 'top') {
      scaleTop += menuBarHeight;
    }
  }

  if ($header.isVisible()) {
    scaleTop += scout.graphics.getSize($header).height;
  }
  $scale.css('top', scaleTop);
  gridTop += scaleTop + scout.graphics.getSize($scale).height;
  $grid.css('top', gridTop);

  yearContainerHeight = scaleTop + $yearContainer.cssMarginY();
  gridHeight = gridTop + $grid.cssMarginY();

  if (menuBar.$container.isVisible() && menuBar.position === 'bottom') {
    yearContainerHeight += menuBarHeight;
    gridHeight += menuBarHeight;
  }

  $yearContainer.css('height', 'calc(100% - ' + yearContainerHeight + 'px)');
  $grid.css('height', 'calc(100% - ' + gridHeight + 'px)');

  this._updateMinWidth();
  this._layoutScaleLines();
  // immediate update to prevent flickering, due to reset in layoutScaleLines
  scout.scrollbars.update(this.planner.$grid, true);
  this.planner.layoutYearPanel();
};

/**
 * Min width is necessary for horizontal scrollbar
 */
scout.PlannerLayout.prototype._updateMinWidth = function() {
  var minWidth = this._minWidth(),
    $scaleTitle = this.planner.$scaleTitle,
    $timeline = this.planner.$timeline;

  if (!$timeline) {
    // May be null if no view range is rendered
    return;
  }

  $timeline.css('min-width', minWidth);
  minWidth += $scaleTitle.outerWidth(true);
  this.planner.resources.forEach(function(resource) {
    resource.$resource.css('min-width', minWidth);
  });
};

/**
 * Positions the scale lines and set to correct height
 */
scout.PlannerLayout.prototype._layoutScaleLines = function() {
  var height, $smallScaleItems, $largeScaleItems, scrollLeft,
    $timelineSmall = this.planner.$timelineSmall,
    $timelineLarge = this.planner.$timelineLarge;

  if (!$timelineSmall) {
    // May be null if no view range is rendered
    return;
  }

  $smallScaleItems = $timelineSmall.children('.scale-item');
  $largeScaleItems = $timelineLarge.children('.scale-item');

  // First loop through every item and set height to 0 in order to get the correct scrollHeight
  $largeScaleItems.each(function() {
    var $scaleItemLine = $(this).data('scale-item-line');
    $scaleItemLine.cssHeight(0);
  });
  $smallScaleItems.each(function() {
    var $scaleItemLine = $(this).data('scale-item-line');
    if ($scaleItemLine) {
      $scaleItemLine.cssHeight(0);
    }
  });
  // also make sure there is no scrollbar anymore which could influence scrollHeight
  scout.scrollbars.reset(this.planner.$grid);

  // Loop again and update height and left
  height = this.planner.$grid[0].scrollHeight;
  scrollLeft = this.planner.$scale[0].scrollLeft;
  $largeScaleItems.each(function() {
    var $scaleItem = $(this),
      $scaleItemLine = $scaleItem.data('scale-item-line');
    $scaleItemLine.cssLeft(scrollLeft + $scaleItem.position().left)
      .cssHeight(height);
  });
  $smallScaleItems.each(function() {
    var $scaleItem = $(this),
      $scaleItemLine = $scaleItem.data('scale-item-line');
    if ($scaleItemLine) {
      $scaleItemLine.cssLeft(scrollLeft + $scaleItem.position().left)
        .cssHeight(height);
    }
  });
};

scout.PlannerLayout.prototype._minWidth = function() {
  var $scaleItemsLarge = this.planner.$timelineLarge.children('.scale-item'),
    $scaleItemsSmall = this.planner.$timelineSmall.children('.scale-item'),
    numScaleItemsLarge = $scaleItemsLarge.length,
    numScaleItemsSmall = $scaleItemsSmall.length,
    displayMode = scout.Planner.DisplayMode;

  if (this.planner.displayMode === displayMode.DAY) {
    return numScaleItemsLarge * 52;
  } if (scout.isOneOf(this.planner.displayMode, displayMode.WORK_WEEK, displayMode.WEEK)) {
    return numScaleItemsLarge * 160;
  } if (this.planner.displayMode === displayMode.MONTH) {
    return numScaleItemsSmall * 23;
  } if (this.planner.displayMode === displayMode.CALENDAR_WEEK) {
    return numScaleItemsSmall * 23;
  } if (this.planner.displayMode === displayMode.YEAR) {
    return numScaleItemsSmall * 90;
  }
};
