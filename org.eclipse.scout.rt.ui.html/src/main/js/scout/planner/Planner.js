// SCOUT GUI
// (c) Copyright 2013-2014, BSI Business Systems Integration AG

scout.Planner = function() {
  scout.Planner.parent.call(this);
  this.resourceMap = [];

  // visual
  this._resourceTitleWidth = 20;

  // tooltip handling
  this._tooltipDelay;

  // main elements
  this.$container;
  this.$header;
  this.$range;
  this.$modes;
  this.$grid;

  // scale calculator
  this.transformLeft = function(t) {
    return t;
  };
  this.transformWidth = function(t) {
    return t;
  };

  // additional modes; should be stored in model
  this.yearPanelVisible = false;
  this._addAdapterProperties(['menus']);
};
scout.inherits(scout.Planner, scout.ModelAdapter);

scout.Planner.Direction = {
  BACKWARD: -1,
  FORWARD: 1
};

scout.Planner.DisplayMode = {
  DAY: 1,
  WEEK: 2,
  MONTH: 3,
  WORK: 4,
  CALENDAR_WEEK: 5,
  YEAR: 6
};

scout.Planner.SelectionMode = {
  NONE: 0,
  ACTIVITY: 1,
  SINGLE_RANGE: 2,
  MULTI_RANGE: 3
};

scout.Planner.prototype.init = function(model, session) {
  scout.Planner.parent.prototype.init.call(this, model, session);
  this._yearPanel = new scout.YearPanel(session);
  this.addChild(this._yearPanel);
  for (var i = 0; i < this.resources.length; i++) {
    this._initResource(this.resources[i]);
  }
  this._syncViewRange(this.viewRange);
  this._syncSelectedResources(this.selectedResources);
  this._syncSelectionRange(this.selectionRange);

  var menuOrder = new scout.PlannerMenuItemsOrder(this.session, this.objectType);
  this.menuBar = new scout.MenuBar(this.session, menuOrder);
  this.menuBar.bottom();
  this.addChild(this.menuBar);
};

scout.Planner.prototype._initResource = function(resource) {
  scout.defaultValues.applyTo(resource, 'Resource');
  scout.defaultValues.applyTo(resource.activities, 'Cell');
  this.resourceMap[resource.id] = resource;
};

scout.Planner.prototype._render = function($parent) {
  //basics, layout etc.
  this._$parent = $parent;
  this.$container = this._$parent.appendDiv('planner');
  var layout = new scout.PlannerLayout(this);
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(layout);
  this.htmlComp.pixelBasedSizing = false;

  // main elements
  this.$header = this.$container.appendDiv('planner-header');
  this._yearPanel.render(this.$container);
  this.$grid = this.$container.appendDiv('planner-grid')
    .on('mousedown', '.resource-cells', this._onCellMousedown.bind(this))
    .on('mousedown', '.resource-title', this._onResourceTitleMousedown.bind(this))
    .on('contextmenu', '.resource-title', this._onResourceTitleContextMenu.bind(this))
    .on('contextmenu', '.activity', this._onActivityContextMenu.bind(this));
  this.$scale = this.$container.appendDiv('planner-scale');
  this.menuBar.render(this.$container);

  scout.scrollbars.install(this.$grid);
  this.session.detachHelper.pushScrollable(this.$grid);
  this._gridScrollHandler = this._onGridScroll.bind(this);
  this.$grid.on('scroll', this._gridScrollHandler);

  // header contains all controls
  this.$range = this.$header.appendDiv('planner-range');
  this.$range.appendDiv('planner-minus').click(this._onClickPrevious.bind(this));
  this.$range.appendDiv('planner-plus').click(this._onClickNext.bind(this));
  this.$range.appendDiv('planner-select');

  // and modes
  this.$commands = this.$header.appendDiv('planner-commands');
  this._renderAvailableDisplayModes();
};

scout.Planner.prototype._renderProperties = function() {
  scout.Planner.parent.prototype._renderProperties.call(this);

  this._renderViewRange();
  this._renderDisplayMode();
  this._renderHeaderVisible();
  this._renderMenus();
  this._renderYearPanelVisible();
  this._renderResources();
  this._renderSelectedResources();
  this._renderLabel();
  // render with setTimeout because the planner needs to be layouted first
  setTimeout(this._renderSelectionRange.bind(this));
};

/* -- basics, events -------------------------------------------- */

scout.Planner.prototype._onClickPrevious = function(event) {
  this._navigateDate(scout.Planner.Direction.BACKWARD);
};

scout.Planner.prototype._onClickNext = function(event) {
  this._navigateDate(scout.Planner.Direction.FORWARD);
};

scout.Planner.prototype._navigateDate = function(direction) {
  var viewRange = this.viewRange,
    DISPLAY_MODE = scout.Planner.DisplayMode;

  if (this.displayMode == DISPLAY_MODE.DAY) {
    viewRange.from = scout.dates.shift(this.viewRange.from, 0, 0, direction);
    viewRange.to = scout.dates.shift(this.viewRange.to, 0, 0, direction);
  } else if (this.displayMode == DISPLAY_MODE.WEEK || this.displayMode == DISPLAY_MODE.WORK) {
    viewRange.from = scout.dates.shift(this.viewRange.from, 0, 0, direction * 7);
    viewRange.to = scout.dates.shift(this.viewRange.to, 0, 0, direction * 7);
  } else if (this.displayMode == DISPLAY_MODE.MONTH) {
    viewRange.from = scout.dates.shift(this.viewRange.from, 0, direction, 0);
    viewRange.to = scout.dates.shift(this.viewRange.to, 0, direction, 0);
  } else if (this.displayMode == DISPLAY_MODE.CALENDAR_WEEK) {
    viewRange.from = scout.dates.shift(this.viewRange.from, 0, direction, 0);
    viewRange.to = scout.dates.shift(this.viewRange.to, 0, direction, 0);
  } else if (this.displayMode == DISPLAY_MODE.YEAR) {
    viewRange.from = scout.dates.shift(this.viewRange.from, 0, 3 * direction, 0);
    viewRange.to = scout.dates.shift(this.viewRange.to, 0, 3 * direction, 0);
  }

  this.setViewRange(viewRange);
};

scout.Planner.prototype._onClickToday = function(event) {
  // new selected date
  this.selected = new Date();
  //FIXME CGU
//  viewRange = this.viewRange;
//  this.setViewRange()
};

scout.Planner.prototype._onClickDisplayMode = function(event) {
  var displayMode = $(event.target).data('mode');
  this.setDisplayMode(displayMode);
};

scout.Planner.prototype._onClickYear = function(event) {
  this.setYearPanelVisible(!this.yearPanelVisible);
};

scout.Planner.prototype._onResourceTitleMousedown = function(event) {
  var $resource = $(event.target).parent();
  if ($resource.isSelected()) {
    if (event.which === 3 || event.which === 1 && event.ctrlKey) {
      // Right click on an already selected resource must not clear the selection -> context menu will be opened
      return;
    }
  }
  this.startRow = $resource.data('resource');
  this.lastRow = this.startRow;
  this._select();
};

scout.Planner.prototype._onResourceTitleContextMenu = function(event) {
  this._showContextMenu(event, 'Planner.Resource');
};

scout.Planner.prototype._onRangeSelectorContextMenu = function(event) {
  this._showContextMenu(event, 'Planner.Range');
};

scout.Planner.prototype._onActivityContextMenu = function(event) {
  this._showContextMenu(event, 'Planner.Activity');
};

scout.Planner.prototype._showContextMenu = function(event, allowedType) {
  event.preventDefault();
  event.stopPropagation();
  var filteredMenus = this._filterMenus([allowedType]),
    popup = new scout.ContextMenuPopup(this.session, filteredMenus),
    $part = $(event.currentTarget),
    x = event.pageX,
    y = event.pageY;
  popup.$anchor = $part;
  popup.render();
  popup.setLocation(new scout.Point(x, y));
};

scout.Planner.prototype._onGridScroll = function() {
  this._reconcileScrollPos();
};

scout.Planner.prototype._reconcileScrollPos = function() {
  // When scrolling horizontally scroll scale as well
  var scrollLeft = this.$grid.scrollLeft();
  this.$scale.scrollLeft(scrollLeft);
};

scout.Planner.prototype._renderRange = function() {
  if (!this.viewRange.from || !this.viewRange.to) {
    return;
  }
  var text,
    toDate = new Date(this.viewRange.to.valueOf() - 1),
    toText = ' bis ',
    DISPLAY_MODE = scout.Planner.DisplayMode;

  // find range text
  if (scout.dates.isSameDay(this.viewRange.from, toDate)) {
    text = this._dateFormat(this.viewRange.from, 'd. MMMM yyyy');
  } else if (this.viewRange.from.getMonth() == toDate.getMonth() && this.viewRange.from.getFullYear() == toDate.getFullYear()) {
    text = this._dateFormat(this.viewRange.from, 'd.') + toText + this._dateFormat(toDate, 'd. MMMM yyyy');
  } else if (this.viewRange.from.getFullYear() === toDate.getFullYear()) {
    if (this.displayMode == DISPLAY_MODE.YEAR) {
      text = this._dateFormat(this.viewRange.from, 'MMMM') + toText + this._dateFormat(toDate, 'MMMM yyyy');
    } else {
      text = this._dateFormat(this.viewRange.from, 'd.  MMMM') + toText + this._dateFormat(toDate, 'd. MMMM yyyy');
    }
  } else {
    if (this.displayMode == DISPLAY_MODE.YEAR) {
      text = this._dateFormat(this.viewRange.from, 'MMMM yyyy') + toText + this._dateFormat(toDate, 'MMMM yyyy');
    } else {
      text = this._dateFormat(this.viewRange.from, 'd.  MMMM yyyy') + toText + this._dateFormat(toDate, 'd. MMMM yyyy');
    }
  }

  // set text
  $('.planner-select', this.$range).text(text);
};

scout.Planner.prototype._renderScale = function() {
  if (!this.viewRange.from || !this.viewRange.to) {
    return;
  }
  var $timeline, $timelineLarge, $timelineSmall, loop, $divLarge, $divSmall, width, newLargeGroup,
    that = this,
    DISPLAY_MODE = scout.Planner.DisplayMode;

  // empty scale
  this.$scale.empty();
  this.$grid.children('.planner-small-scale-item-line').remove();
  this.$grid.children('.planner-large-scale-item-line').remove();

  // append main elements
  this.$scaleTitle = this.$scale.appendDiv('planner-scale-title');
  this.$timeline = this.$scale.appendDiv('timeline');
  this.$timelineLarge = this.$timeline.appendDiv('timeline-large');
  this.$timelineSmall = this.$timeline.appendDiv('timeline-small');
  $timeline = this.$timeline;
  $timelineLarge = this.$timelineLarge;
  $timelineSmall = this.$timelineSmall;

  // fill timeline large depending on mode
  // TODO: depending on screen size: smaller or large representation
  // TODO: change to shift
  if (this.displayMode === DISPLAY_MODE.DAY) {
    loop = new Date(this.viewRange.from.valueOf());

    // from start to end
    while (loop < this.viewRange.to) {
      newLargeGroup = false;
      if ((loop.getMinutes() === 0) || (loop.valueOf() == this.viewRange.from.valueOf())) {
        $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'HH')).data('count', 0);
        newLargeGroup = true;
      }

      $divSmall = $timelineSmall
        .appendDiv('scale-item', this._dateFormat(loop, ':mm'))
        .data('date-from', new Date(loop.valueOf()));

      loop.setMinutes(loop.getMinutes() + 30);
      $divSmall.data('date-to', new Date(loop.valueOf()))
        .data('first', newLargeGroup);

      $divLarge.data('count', $divLarge.data('count') + 1);
    }
  } else if ((this.displayMode === DISPLAY_MODE.WORK) || (this.displayMode === DISPLAY_MODE.WEEK)) {
    loop = new Date(this.viewRange.from.valueOf());

    // from start to end
    while (loop < this.viewRange.to) {
      newLargeGroup = false;
      if ((loop.getHours() === 0) || (loop.valueOf() == this.viewRange.from.valueOf())) {
        if ((loop.getMonth() === 0) || (loop.valueOf() == this.viewRange.from.valueOf())) {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'd. MMMM yyyy')).data('count', 0);
        } else if (loop.getDate() === 1) {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'd. MMM')).data('count', 0);
        } else {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'd.')).data('count', 0);
        }
        newLargeGroup = true;
      }

      $divSmall = $timelineSmall
        .appendDiv('scale-item', this._dateFormat(loop, 'HH:mm'))
        .data('date-from', new Date(loop.valueOf()));

      loop.setHours(loop.getHours() + 6);
      $divSmall.data('date-to', new Date(loop.valueOf()))
        .data('first', newLargeGroup);

      $divLarge.data('count', $divLarge.data('count') + 1);
    }

  } else if (this.displayMode === DISPLAY_MODE.MONTH) {
    loop = new Date(this.viewRange.from.valueOf());

    // from start to end
    while (loop < this.viewRange.to) {
      newLargeGroup = false;
      if ((loop.getDate() == 1) || (loop.valueOf() == this.viewRange.from.valueOf())) {
        if ((loop.getMonth() === 0) || (loop.valueOf() == this.viewRange.from.valueOf())) {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'MMMM yyyy')).data('count', 0);
        } else {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'MMMM')).data('count', 0);
        }
        newLargeGroup = true;
      }

      $divSmall = $timelineSmall
        .appendDiv('scale-item', this._dateFormat(loop, 'dd'))
        .data('date-from', new Date(loop.valueOf()));

      loop.setDate(loop.getDate() + 1);
      $divSmall.data('date-to', new Date(loop.valueOf()))
        .data('first', newLargeGroup);

      $divLarge.data('count', $divLarge.data('count') + 1);
    }

  } else if (this.displayMode === DISPLAY_MODE.CALENDAR_WEEK) {
    loop = new Date(this.viewRange.from.valueOf());

    // from start to end
    while (loop < this.viewRange.to) {
      newLargeGroup = false;
      if ((loop.getDate() < 8) || (loop.valueOf() == this.viewRange.from.valueOf())) {
        if ((loop.getMonth() === 0) || (loop.valueOf() == this.viewRange.from.valueOf())) {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'MMMM yyyy')).data('count', 0);
        } else {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'MMMM')).data('count', 0);
        }
        newLargeGroup = true;
      }

      $divSmall = $timelineSmall
        .appendDiv('scale-item', scout.dates.weekInYear(loop))
        .data('date-from', new Date(loop.valueOf()))
        .mouseenter(this._onScaleHoverIn.bind(this))
        .mouseleave(this._onScaleHoverOut.bind(this));

      loop.setDate(loop.getDate() + 7);
      $divSmall.data('date-to', new Date(loop.valueOf()))
        .data('first', newLargeGroup);

      $divLarge.data('count', $divLarge.data('count') + 1);
    }

  } else if (this.displayMode === DISPLAY_MODE.YEAR) {
    loop = new Date(this.viewRange.from.valueOf());

    // from start to end
    while (loop < this.viewRange.to) {
      newLargeGroup = false;
      if ((loop.getMonth() === 0) || (loop.valueOf() == this.viewRange.from.valueOf())) {
        $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'yyyy')).data('count', 0);
        newLargeGroup = true;
      }

      $divSmall = $timelineSmall
        .appendDiv('scale-item', this._dateFormat(loop, 'MMMM'))
        .data('date-from', new Date(loop.valueOf()));

      loop.setMonth(loop.getMonth() + 1);
      $divSmall.data('date-to', new Date(loop.valueOf()))
        .data('first', newLargeGroup);

      $divLarge.data('count', $divLarge.data('count') + 1);
    }
  }

  // set sizes and append scale lines
  var $smallScaleItems = $timelineSmall.children('.scale-item');
  var $largeScaleItems = $timelineLarge.children('.scale-item');
  width = 100 / $smallScaleItems.length;
  $largeScaleItems.each(function() {
    var $scaleItem = $(this);
    $scaleItem.css('width', $scaleItem.data('count') * width + '%')
      .data('scale-item-line', that.$grid.appendDiv('planner-large-scale-item-line'));
    $scaleItem.appendDiv('planner-large-scale-item-line')
      .css('left', 0);
  });
  $smallScaleItems.each(function(index) {
    var $scaleItem = $(this);
    $scaleItem.css('width', width + '%');
    if (!$scaleItem.data('first')) {
      $scaleItem.data('scale-item-line', that.$grid.appendDiv('planner-small-scale-item-line'));
      $scaleItem.appendDiv('planner-small-scale-item-line')
        .css('left', 0);
    }
  });

  // find transfer function
  var beginScale = $timelineSmall.children().first().data('date-from').valueOf(),
    endScale = $timelineSmall.children().last().data('date-to').valueOf();

  this.transformLeft = function(begin, end) {
    return function(t) {
      return (t - begin) / (end - begin) * 100;
    };
  }(beginScale, endScale);

  this.transformWidth = function(begin, end) {
    return function(t) {
      return t / (end - begin) * 100;
    };
  }(beginScale, endScale);
};

/* -- scale events --------------------------------------------------- */

scout.Planner.prototype._onScaleHoverIn = function(event) {
  this._tooltipDelay = setTimeout(function() {
    var $scale = $(event.currentTarget),
      tooltip,
      text,
      toText = ' bis ',
      from = new Date($scale.data('date-from').valueOf()),
      to = new Date($scale.data('date-to').valueOf());

    if (from.getMonth() == to.getMonth()) {
      text = this._dateFormat(from, 'd.') + toText + this._dateFormat(to, 'd. MMMM yyyy');
    } else if (from.getFullYear() === to.getFullYear()) {
      text = this._dateFormat(from, 'd. MMMM') + toText + this._dateFormat(to, 'd. MMMM yyyy');
    } else {
      text = this._dateFormat(from, 'd. MMMM yyyy') + toText + this._dateFormat(to, 'd. MMMM yyyy');
    }

    tooltip = new scout.Tooltip({
      text: text,
      $anchor: $scale,
      arrowPosition: 50,
      arrowPositionUnit: '%',
      htmlEnabled: true
    });

    $scale.data('tooltip', tooltip);
    tooltip.render();
  }.bind(this), 350);
};

scout.Planner.prototype._onScaleHoverOut = function(event) {
  var $scale = $(event.currentTarget),
    tooltip = $scale.data('tooltip');
  clearTimeout(this._tooltipDelay);
  if (tooltip) {
    tooltip.remove();
    $scale.removeData('tooltip');
  }
};

/* --  render resources, activities --------------------------------- */

scout.Planner.prototype._removeAllResources = function() {
  this.resources.forEach(function(resource) {
    resource.$resource.remove();
  });
};

scout.Planner.prototype._renderResources = function(resources) {
  var i, $resource, resource;

  resources = resources || this.resources;
  for (i = 0; i < resources.length; i++) {
    resource = resources[i];
    $resource = this._build$Resource(resource, this.$grid);
    $resource.data('resource', resource)
      .appendTo(this.$grid);
    resource.$resource = $resource;
  }
};

scout.Planner.prototype._build$Resource = function(resource) {
  var i, $activity,
    $resource = $.makeDiv('resource');
  $resource.appendDiv('resource-title')
    .text(resource.resourceCell.text);
  var $cells = $resource.appendDiv('resource-cells');
  for (i = 0; i < resource.activities.length; i++) {
    $activity = this._build$Activity(resource.activities[i]);
    $activity.appendTo($cells);
  }
  return $resource;
};

scout.Planner.prototype._build$Activity = function(activity) {
  var i,
    $activity = $.makeDiv('activity'),
    level = 100 - Math.min(activity.level * 100, 100),
    levelColor = scout.helpers.modelToCssColor(activity.levelColor),
    begin = scout.dates.parseJsonDate(activity.beginTime).valueOf(),
    end = scout.dates.parseJsonDate(activity.endTime).valueOf();

  $activity.text(activity.text)
    .data('activity', activity)
    .css('left', 'calc(' + this.transformLeft(begin) + '% + 2px)')
    .css('width', 'calc(' + this.transformWidth(end - begin) + '% - 4px');

  if (activity.cssClass) {
    $activity.addClass(activity.cssClass);
  }
  if (levelColor) {
    $activity.css('background-color', levelColor);
    $activity.css('border-color', levelColor);
  }
  // the background-color represents the fill level and not the image. This makes it easier to change the color using a css class
  $activity.css('background-image', 'linear-gradient(to bottom, #fff 0%, #fff ' + level + '%, transparent ' + level + '%, transparent 100% )');

  activity.$activity = $activity;
  return $activity;
};

/* -- selector -------------------------------------------------- */

scout.Planner.prototype._onCellMousedown = function(event) {
  var $activity,
    $resource,
    $target = $(event.target),
    SELECTION_MODE = scout.Planner.SelectionMode;

  if (this.selectionMode == SELECTION_MODE.NONE) {
    return;
  }

  if (this.selectionMode == SELECTION_MODE.ACTIVITY) {
    $activity = $(document.elementFromPoint(event.pageX, event.pageY));

    if ($activity.hasClass('activity')) {
      $('.selected', this.$grid).removeClass('selected');
      $activity.addClass('selected');

      $resource = $activity.parent().parent();
      this.selectResources([$resource.data('resource')]);
    }
  } else {
    if ($target.hasClass('selector')) {
      if (event.which === 3 || event.which === 1 && event.ctrlKey) {
        // Right click on the selector must not clear the selection -> context menu will be opened
        return;
      }
    }

    // init selector
    this.startRow = this._findRow(event.pageY);
    this.lastRow = this.startRow;

    // find range on scale
    this.startRange = this._findScale(event.pageX);
    this.lastRange = this.startRange;

    // draw
    this._select(true);

    // event
    $(document)
      .on('mousemove', this._onCellMousemove.bind(this))
      .one('mouseup', this._onCellMouseup.bind(this));
  }
};

scout.Planner.prototype._onResizeMousedown = function(event) {
  var swap;

  // find range on scale
  if (($(event.target).hasClass('selector-resize-right') && this.startRange.to > this.lastRange.to) ||
    ($(event.target).hasClass('selector-resize-left') && this.startRange.to < this.lastRange.to)) {
    swap = this.startRange;
    this.startRange = this.lastRange;
    this.lastRange = swap;
  }

  $('body').addClass('col-resize');

  $(document)
    .on('mousemove', this._onResizeMousemove.bind(this))
    .one('mouseup', this._onCellMouseup.bind(this));

  return false;

};

scout.Planner.prototype._onCellMousemove = function(event) {
  this.lastRow = this._findRow(event.pageY);
  this.lastRange = this._findScale(event.pageX);

  this._select(true);
};

scout.Planner.prototype._onResizeMousemove = function(event) {
  this.lastRange = this._findScale(event.pageX);

  this._select(true);
};

scout.Planner.prototype._onCellMouseup = function(event) {
  this._select();
  $('body').removeClass('col-resize');
  $(document).off('mousemove');
};

scout.Planner.prototype._select = function(whileSelecting) {
  if (!this.startRow || !this.lastRow) {
    return;
  }
  // If startRange or lastRange are not given, use the existing range selection
  // Happens if the user clicks a resource instead of making a range selection
  if (!this.startRange || !this.lastRange) {
    if (this.selectionRange.from) {
      this.startRange = {};
      this.startRange.from = this.selectionRange.from.getTime();
      this.startRange.to = this.startRange.from;
    }
    if (this.selectionRange.to) {
      this.lastRange = {};
      this.lastRange.from = this.selectionRange.to.getTime();
      this.lastRange.to = this.lastRange.from;
    }
  }
  var rangeSelected = !! (this.startRange && this.lastRange);
  var $startRow = this.startRow.$resource,
    $lastRow = this.lastRow.$resource;

  // in case of single selection
  if (this.selectionMode == scout.Planner.SelectionMode.SINGLE_RANGE) {
    this.lastRow = this.startRow;
  }

  // select rows
  var $upperRow = ($startRow[0].offsetTop <= $lastRow[0].offsetTop) ? $startRow : $lastRow,
    $lowerRow = ($startRow[0].offsetTop > $lastRow[0].offsetTop) ? $startRow : $lastRow,
    resources = $('.resource', this.$grid).toArray(),
    top = $upperRow[0].offsetTop,
    low = $lowerRow[0].offsetTop;

  for (var r = resources.length - 1; r >= 0; r--) {
    var row = resources[r];
    if ((row.offsetTop < top && row.offsetTop < low) || (row.offsetTop > top && row.offsetTop > low)) {
      resources.splice(r, 1);
    }
  }

  this.selectResources(resources.map(function(i) {
    return $(i).data('resource');
  }), !rangeSelected);

  if (rangeSelected) {
    // left and width
    var from = Math.min(this.lastRange.from, this.startRange.from),
      to = Math.max(this.lastRange.to, this.startRange.to);
    var selectionRange = {
      from: new Date(from),
      to: new Date(to)
    };

    this.selectRange(selectionRange, !whileSelecting);
  }
};

scout.Planner.prototype._findRow = function(y) {
  var x = this.$grid.offset().left + 10,
    $row = $(document.elementFromPoint(x, y)).parent();

  if ($row.hasClass('resource')) {
    return $row.data('resource');
  } else {
    return null;
  }
};

scout.Planner.prototype._findScale = function(x) {
  var y = this.$scale.offset().top + this.$scale.height() * 0.75,
    $scale = $(document.elementFromPoint(event.pageX, y));

  if ($scale.data('date-from') !== undefined) {
    return {
      from: $scale.data('date-from').valueOf(),
      to: $scale.data('date-to').valueOf()
    };
  } else {
    return null;
  }
};

/* -- helper ---------------------------------------------------- */

scout.Planner.prototype._dateFormat = function(date, pattern) {
  var d = new Date(date.valueOf()),
    dateFormat = new scout.DateFormat(this.session.locale, pattern);

  return dateFormat.format(d);
};

scout.Planner.prototype._renderViewRange = function() {
  this._renderRange();
  this._renderScale();
  this.invalidateTree();
};

scout.Planner.prototype._renderHeaderVisible = function() {
  this.$header.setVisible(this.headerVisible);
  this.invalidateTree();
};

scout.Planner.prototype._renderYearPanelVisible = function() {
  var yearPanelWidth;
  if (this.yearPanelVisible) {
    this._yearPanel.renderContent();
  }

  // show or hide year panel
  $('.calendar-toggle-year', this.$modes).select(this.yearPanelVisible);
  if (this.yearPanelVisible) {
    yearPanelWidth = 210;
  } else {
    yearPanelWidth = 0;
  }
  this._yearPanel.$container.animate({
    width: yearPanelWidth
  }, {
    duration: 500,
    progress: this._onYearPanelWidthChange.bind(this),
    complete: this._afterYearPanelWidthChange.bind(this)
  });
};

scout.Planner.prototype._onYearPanelWidthChange = function() {
  var yearPanelWidth = this._yearPanel.$container.outerWidth();
  this.$grid.css('width', 'calc(100% - ' + yearPanelWidth + 'px)');
  this.$scale.css('width', 'calc(100% - ' + yearPanelWidth + 'px)');
};

scout.Planner.prototype._afterYearPanelWidthChange = function() {
  if (!this.yearPanelVisible) {
    this._yearPanel.removeContent();
  }
};

scout.Planner.prototype._renderMenus = function() {
  this._updateMenuBar();
};

scout.Planner.prototype._updateMenuBar = function() {
  var menuItems = this._filterMenus(['Planner.EmptySpace', 'Planner.Resource', 'Planner.Activity', 'Planner.Range']);
  this.menuBar.updateItems(menuItems);
};

scout.Planner.prototype._filterMenus = function(allowedTypes) {
  allowedTypes = allowedTypes || [];
  if (allowedTypes.indexOf('Planner.Resource') > -1 && this.selectedResources.length === 0) {
    scout.arrays.remove(allowedTypes, 'Planner.Resource');
  }
  if (allowedTypes.indexOf('Planner.Activity') > -1 && !this.selectedActivity) {
    scout.arrays.remove(allowedTypes, 'Planner.Activity');
  }
  if (allowedTypes.indexOf('Planner.Range') > -1 && !this.selectionRange.from && !this.selectionRange.to) {
    scout.arrays.remove(allowedTypes, 'Planner.Range');
  }
  return scout.menus.filter(this.menus, allowedTypes);
};

scout.Planner.prototype._renderWorkDayCount = function() {};

scout.Planner.prototype._renderWorkDaysOnly = function() {};

scout.Planner.prototype._renderFirstHourOfDay = function() {};

scout.Planner.prototype._renderLastHourOfDay = function() {};

scout.Planner.prototype._renderIntradayInterval = function() {};

scout.Planner.prototype._renderAvailableDisplayModes = function() {
  var DISPLAY_MODE = scout.Planner.DisplayMode;
  this.$commands.empty();

  this.$commands.appendDiv('planner-today').click(this._onClickToday.bind(this));
  this.$commands.appendDiv('planner-separator');
  if (this.availableDisplayModes.indexOf(DISPLAY_MODE.DAY) > -1) {
    this.$commands.appendDiv('planner-mode-day planner-mode').attr('data-mode', DISPLAY_MODE.DAY).click(this._onClickDisplayMode.bind(this));
  }
  if (this.availableDisplayModes.indexOf(DISPLAY_MODE.WORK) > -1) {
    this.$commands.appendDiv('planner-mode-work planner-mode').attr('data-mode', DISPLAY_MODE.WORK).click(this._onClickDisplayMode.bind(this));
  }
  if (this.availableDisplayModes.indexOf(DISPLAY_MODE.WEEK) > -1) {
    this.$commands.appendDiv('planner-mode-week planner-mode').attr('data-mode', DISPLAY_MODE.WEEK).click(this._onClickDisplayMode.bind(this));
  }
  if (this.availableDisplayModes.indexOf(DISPLAY_MODE.MONTH) > -1) {
    this.$commands.appendDiv('planner-mode-month planner-mode').attr('data-mode', DISPLAY_MODE.MONTH).click(this._onClickDisplayMode.bind(this));
  }
  if (this.availableDisplayModes.indexOf(DISPLAY_MODE.CALENDAR_WEEK) > -1) {
    this.$commands.appendDiv('planner-mode-cw planner-mode').attr('data-mode', DISPLAY_MODE.CALENDAR_WEEK).click(this._onClickDisplayMode.bind(this));
  }
  if (this.availableDisplayModes.indexOf(DISPLAY_MODE.YEAR) > -1) {
    this.$commands.appendDiv('planner-mode-year planner-mode').attr('data-mode', DISPLAY_MODE.YEAR).click(this._onClickDisplayMode.bind(this));
  }
  this.$commands.appendDiv('planner-separator');
  this.$commands.appendDiv('planner-toggle-year').click(this._onClickYear.bind(this));
};

scout.Planner.prototype._renderDisplayMode = function() {
  $('.planner-mode', this.$commands).select(false);
  $('[data-mode="' + this.displayMode + '"]', this.$commands).select(true);
};

scout.Planner.prototype._syncViewRange = function(viewRange) {
  this.viewRange = {
    from: scout.dates.create(viewRange.from),
    to: scout.dates.create(viewRange.to)
  };
  this._yearPanel.setViewRange(this.viewRange);
  this._yearPanel.selectDate(this.viewRange.from);
};

scout.Planner.prototype._syncDisplayMode = function(displayMode) {
  this.displayMode = displayMode;
  this._yearPanel.setDisplayMode(this.displayMode);
};

scout.Planner.prototype._syncSelectionRange = function(selectionRange) {
  this.selectionRange = {
    from: scout.dates.create(selectionRange.from),
    to: scout.dates.create(selectionRange.to)
  };
};

scout.Planner.prototype._syncSelectedResources = function(selectedResources) {
  this.selectedResources = this._resourcesByIds(selectedResources);
};

scout.Planner.prototype._renderSelectedResources = function(newIds, oldSelectedResources) {
  if (oldSelectedResources) {
    oldSelectedResources.forEach(function(resource) {
      resource.$resource.select(false);
    });
  }

  this.selectedResources.forEach(function(resource) {
    resource.$resource.select(true);
  });

  // Only call update menubar on property change, not necessary to call it when initializing
  if (this.rendered) {
    this._updateMenuBar();
  }
};

scout.Planner.prototype._renderSelectionRange = function() {
  var $startRow, $lastRow,
    from = this.selectionRange.from,
    to = this.selectionRange.to,
    startRow = this.selectedResources[0],
    lastRow = this.selectedResources[this.selectedResources.length - 1];

  // remove old selector
  if (this.$selector) {
    this.$selector.remove();
  }

  if (!startRow || !lastRow || !this.selectionRange.from || !this.selectionRange.to) {
    return;
  }
  $startRow = startRow.$resource;
  $lastRow = lastRow.$resource;

  // top and height
  var $parent = ($startRow[0].offsetTop <= $lastRow[0].offsetTop) ? $startRow : $lastRow;
  this.$selector = $parent.children('.resource-cells').appendDiv('selector');
  this.$selector.css('height', $startRow.outerHeight() + Math.abs($lastRow[0].offsetTop - $startRow[0].offsetTop));
  this.$selector.appendDiv('selector-resize-left').mousedown(this._onResizeMousedown.bind(this));
  this.$selector.appendDiv('selector-resize-right').mousedown(this._onResizeMousedown.bind(this));
  this.$selector
    .css('left', 'calc(' + this.transformLeft(from) + '% - 6px)')
    .css('width', 'calc(' + this.transformWidth(to - from) + '% + 12px)')
    .on('contextmenu', this._onRangeSelectorContextMenu.bind(this));

  // colorize scale
  $('.selected', this.$scale).removeClass('selected');
  var $scaleItems = $('.timeline-small', this.$scale).children();
  for (var i = 0; i < $scaleItems.length; i++) {
    var $item = $scaleItems.eq(i);
    if ($item.data('date-from') >= from && $item.data('date-to') <= to) {
      $item.addClass('selected');
    }
  }

  // Only call update menubar on property change, not necessary to call it when initializing
  if (this.rendered) {
    this._updateMenuBar();
  }
};

scout.Planner.prototype._renderSelectedActivity = function() {
  // Only call update menubar on property change, not necessary to call it when initializing
  if (this.rendered) {
    this._updateMenuBar();
  }
};

scout.Planner.prototype._renderLabel = function() {
  var label = this.label || '';
  this.$scaleTitle.text(label);
};

scout.Planner.prototype._resourcesByIds = function(ids) {
  return ids.map(this._resourceById.bind(this));
};

scout.Planner.prototype._resourceById = function(id) {
  return this.resourceMap[id];
};

scout.Planner.prototype.setDisplayMode = function(displayMode) {
  this.displayMode = displayMode;
  this._yearPanel.setDisplayMode(displayMode);
  this._sendSetDisplayMode(displayMode);
  if (this.rendered) {
    this._renderDisplayMode();
  }
};

if (this.yearPanelVisible) {
  this._yearPanel.renderContent();
}

scout.Planner.prototype.setYearPanelVisible = function(visible) {
  if (this.yearPanelVisible === visible) {
    return;
  }
  this.yearPanelVisible = visible;
  if (this.rendered) {
    this._renderYearPanelVisible();
  }
};

scout.Planner.prototype.setViewRange = function(viewRange) {
  this.viewRange = viewRange;
  this._yearPanel.setViewRange(viewRange);
  this._yearPanel.selectDate(this.viewRange.from);
  this._sendSetViewRange(viewRange);

  if (this.rendered) {
    this._renderViewRange();
    this.validateLayout();
  }
};

scout.Planner.prototype.selectRange = function(range, notifyServer) {
  notifyServer = notifyServer !== undefined ? notifyServer : true;
  this.selectionRange = range;
  if (notifyServer) {
    this._sendSetSelection();
  }
  if (this.rendered) {
    this._renderSelectionRange();
  }
};

scout.Planner.prototype.selectResources = function(resources, notifyServer) {
  var oldSelection = this.selectedResources;
  notifyServer = notifyServer !== undefined ? notifyServer : true;
  this.selectedResources = resources;
  if (notifyServer) {
    this._sendSetSelection();
  }
  if (this.rendered) {
    this._renderSelectedResources('', oldSelection);
  }
};

/**
 * Returns true if a deselection happened. False if the given resources were not selected at all.
 */
scout.Planner.prototype.deselectResources = function(resources, notifyServer) {
  var deselected = false;
  resources = scout.arrays.ensure(resources);
  notifyServer = notifyServer !== undefined ? notifyServer : true;
  var selectedResources = this.selectedResources.slice(); // copy
  if (scout.arrays.removeAll(selectedResources, resources)) {
    this.selectResources(selectedResources, notifyServer);
    deselected = true;
  }
  return deselected;
};

scout.Planner.prototype._insertResources = function(resources) {
  // Update model
  resources.forEach(function(resource) {
    this._initResource(resource);
    // Always insert new rows at the end, if the order is wrong a rowOrderChange event will follow
    this.resources.push(resource);
  }.bind(this));

  // Update HTML
  if (this.rendered) {
    this._renderResources(resources);
    this.invalidateTree();
  }
};

scout.Planner.prototype._deleteResources = function(resources) {
  if (this.deselectResources(resources, false)) {
    this.selectRange({}, false);
  }
  resources.forEach(function(resource) {
    // Update model
    scout.arrays.remove(this.resources, resource);
    delete this.resourceMap[resource.id];

    // Update HTML
    if (this.rendered) {
      resource.$resource.remove();
      delete resource.$resource;
    }
  }.bind(this));

  this.invalidateTree();
};

scout.Planner.prototype._deleteAllResources = function() {
  // Update HTML
  if (this.rendered) {
    this._removeAllResources();
    this.invalidateTree();
  }

  // Update model
  this.resources = [];
  this.resourceMap = {};
  this.selectResources([], false);
  this.selectRange({}, false);
};

scout.Planner.prototype._sendSetDisplayMode = function(displayMode) {
  this.session.send(this.id, 'setDisplayMode', {
    displayMode: displayMode
  });
};

scout.Planner.prototype._sendSetViewRange = function(viewRange) {
  this.session.send(this.id, 'setViewRange', {
    viewRange: scout.dates.toJsonDateRange(viewRange)
  });
};

scout.Planner.prototype._sendSetSelection = function() {
  var selectionRange = scout.dates.toJsonDateRange(this.selectionRange),
    resourceIds = this.selectedResources.map(function(r) {
      return r.id;
    });
  this.session.send(this.id, 'setSelection', {
    resourceIds: resourceIds,
    selectionRange: selectionRange
  });
};

scout.Planner.prototype._onResourcesInserted = function(resources) {
  this._insertResources(resources);
};

scout.Planner.prototype._onResourcesDeleted = function(resourceIds) {
  var resources = this._resourcesByIds(resourceIds);
  this._deleteResources(resources);
};

scout.Planner.prototype._onAllResourcesDeleted = function() {
  this._deleteAllResources();
};

scout.Planner.prototype._onResourcesUpdated = function(resources) {

};

scout.Planner.prototype._onResourceOrderChanged = function(resourceIds) {

};

scout.Planner.prototype.onModelAction = function(event) {
  if (event.type === 'resourcesInserted') {
    this._onResourcesInserted(event.resources);
  } else if (event.type === 'resourcesDeleted') {
    this._onResourcesDeleted(event.resourceIds);
  } else if (event.type === 'allResourcesDeleted') {
    this._onAllResourcesDeleted();
  } else if (event.type === 'resourceOrderChanged') {
    this._onResourceOrderChanged(event.resourceIds);
  } else if (event.type === 'resourcesUpdated') {
    this._onResourcesUpdated(event.resources);
  } else {
    $.log.warn('Model event not handled. Widget: scout.Planner. Event: ' + event.type + '.');
  }
};
