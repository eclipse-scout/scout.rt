scout.PlannerLayout = function(planner) {
  scout.PlannerLayout.parent.call(this);
  this.planner = planner;
};
scout.inherits(scout.PlannerLayout, scout.AbstractLayout);

scout.PlannerLayout.prototype.layout = function($container) {
  var $header = this.planner._header.$container,
    $scale = this.planner.$scale,
    $grid = this.planner.$grid,
    menuBar = this.planner.menuBar,
    $yearContainer = this.planner._yearPanel.$container,
    menuBarHeight = 0,
    gridHeight = 0,
    yearContainerHeight = 0,
    gridTop = 0,
    scaleTop = 0;

  if (menuBar.$container.isVisible()) {
    var htmlMenuBar = scout.HtmlComponent.get(menuBar.$container),
    menuBarSize = htmlMenuBar.getPreferredSize();
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

  $yearContainer.css('height', 'calc(100% - '+ yearContainerHeight + 'px)');
  $grid.css('height', 'calc(100% - '+ gridHeight + 'px)');

  this._updateMinWidth();
  this._layoutScaleLines();
  scout.scrollbars.update(this.planner.$grid);
  this.planner.layoutYearPanel();
};

/**
 * Min width is necessary for horizontal scrollbar
 */
scout.PlannerLayout.prototype._updateMinWidth = function() {
  var minWidth = this._minWidth(),
    $scale = this.planner.$scale,
    $scaleTitle = this.planner.$scaleTitle,
    $timeline = this.planner.$timeline;

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
  var height,
    $smallScaleItems = this.planner.$timelineSmall.children('.scale-item'),
    $largeScaleItems = this.planner.$timelineLarge.children('.scale-item');

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
  $largeScaleItems.each(function() {
    var $scaleItem = $(this),
      $scaleItemLine = $scaleItem.data('scale-item-line');
    $scaleItemLine.cssLeft($scaleItem.position().left)
      .cssHeight(height);
  });
  $smallScaleItems.each(function() {
    var $scaleItem = $(this),
      $scaleItemLine = $scaleItem.data('scale-item-line');
    if ($scaleItemLine) {
      $scaleItemLine.cssLeft($scaleItem.position().left)
        .cssHeight(height);
    }
  });
};

scout.PlannerLayout.prototype._minWidth = function() {
  var $scale = this.planner.$scale,
    $scaleItems = $scale.find('.timeline-large').children('.scale-item'),
    numScaleItems = $scaleItems.length,
    DISPLAY_MODE = scout.Planner.DisplayMode;

  var scaleItemMinWidth = 0;
  if (this.planner.displayMode === DISPLAY_MODE.DAY) {
    scaleItemMinWidth = 52;
  } else if ((this.planner.displayMode === DISPLAY_MODE.WORK) || (this.planner.displayMode === DISPLAY_MODE.WEEK)) {
    scaleItemMinWidth = 160;
  } else if (this.planner.displayMode === DISPLAY_MODE.MONTH) {
    scaleItemMinWidth = 450;
  } else if (this.planner.displayMode === DISPLAY_MODE.CALENDAR_WEEK) {
    scaleItemMinWidth = 88;
  } else if (this.planner.displayMode === DISPLAY_MODE.YEAR) {
    scaleItemMinWidth = 550;
  }

  return numScaleItems * scaleItemMinWidth;
};
