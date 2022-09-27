/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {AbstractLayout, graphics, HtmlComponent, MenuBarLayout, Planner, scout, scrollbars} from '../index';
import $ from 'jquery';

export default class PlannerLayout extends AbstractLayout {

  constructor(planner) {
    super();
    this.planner = planner;
  }

  layout($container) {
    let menuBarSize,
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
      htmlMenuBar = HtmlComponent.get(menuBar.$container),
      htmlContainer = this.planner.htmlComp,
      containerSize = htmlContainer.availableSize()
        .subtract(htmlContainer.insets());

    if (menuBar.$container.isVisible()) {
      menuBarSize = MenuBarLayout.size(htmlMenuBar, containerSize);
      htmlMenuBar.setSize(menuBarSize);
      menuBarHeight = menuBarSize.height;
      if (menuBar.position === 'top') {
        scaleTop += menuBarHeight;
      }
    }

    if ($header.isVisible()) {
      scaleTop += graphics.size($header).height;
    }
    $scale.css('top', scaleTop);
    gridTop += scaleTop + graphics.size($scale).height;
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
    scrollbars.update(this.planner.$grid, true);
    this.planner.layoutYearPanel();
  }

  /**
   * Min width is necessary for horizontal scrollbar
   */
  _updateMinWidth() {
    let minWidth = this._minWidth(),
      $scaleTitle = this.planner.$scaleTitle,
      $timeline = this.planner.$timeline;

    if (!$timeline) {
      // May be null if no view range is rendered
      return;
    }

    $timeline.css('min-width', minWidth);
    minWidth += $scaleTitle.outerWidth(true);
    this.planner.resources.forEach(resource => {
      resource.$resource.css('min-width', minWidth);
    });
  }

  /**
   * Positions the scale lines and set to correct height
   */
  _layoutScaleLines() {
    let height, $smallScaleItems, $largeScaleItems, scrollLeft,
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
      let $scaleItemLine = $(this).data('scale-item-line');
      $scaleItemLine.cssHeight(0);
    });
    $smallScaleItems.each(function() {
      let $scaleItemLine = $(this).data('scale-item-line');
      if ($scaleItemLine) {
        $scaleItemLine.cssHeight(0);
      }
    });
    // also make sure there is no scrollbar anymore which could influence scrollHeight
    scrollbars.reset(this.planner.$grid);

    // Loop again and update height and left
    height = this.planner.$grid[0].scrollHeight;
    scrollLeft = this.planner.$scale[0].scrollLeft;
    $largeScaleItems.each(function() {
      let $scaleItem = $(this),
        $scaleItemLine = $scaleItem.data('scale-item-line');
      $scaleItemLine.cssLeft(scrollLeft + $scaleItem.position().left)
        .cssHeight(height);
    });
    $smallScaleItems.each(function() {
      let $scaleItem = $(this),
        $scaleItemLine = $scaleItem.data('scale-item-line');
      if ($scaleItemLine) {
        $scaleItemLine.cssLeft(scrollLeft + $scaleItem.position().left)
          .cssHeight(height);
      }
    });
  }

  _minWidth() {
    let $scaleItemsLarge = this.planner.$timelineLarge.children('.scale-item'),
      $scaleItemsSmall = this.planner.$timelineSmall.children('.scale-item'),
      numScaleItemsLarge = $scaleItemsLarge.length,
      numScaleItemsSmall = $scaleItemsSmall.length,
      displayMode = Planner.DisplayMode,
      cellInsets = graphics.insets($scaleItemsSmall, {
        includeBorder: false
      }),
      minWidth = numScaleItemsSmall * cellInsets.horizontal(); // no matter what, this width must never be deceeded

    if (this.planner.displayMode === displayMode.DAY) {
      return Math.max(minWidth, numScaleItemsLarge * 52);
    }
    if (scout.isOneOf(this.planner.displayMode, displayMode.WORK_WEEK, displayMode.WEEK)) {
      return Math.max(minWidth, numScaleItemsLarge * 160);
    }
    if (this.planner.displayMode === displayMode.MONTH) {
      return Math.max(minWidth, numScaleItemsSmall * 23);
    }
    if (this.planner.displayMode === displayMode.CALENDAR_WEEK) {
      return Math.max(minWidth, numScaleItemsSmall * 23);
    }
    if (this.planner.displayMode === displayMode.YEAR) {
      return Math.max(minWidth, numScaleItemsSmall * 90);
    }
  }
}
