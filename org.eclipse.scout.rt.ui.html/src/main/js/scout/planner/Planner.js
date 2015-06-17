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
  this.$year;
  this.$grid;

  // scale calculator
  this.transformLeft = function(t) {
    return t;
  };
  this.transformWidth = function(t) {
    return t;
  };

  // additional modes; should be stored in model
  this.showYearPanel = false;
  this._addAdapterProperties(['menus']);
};
scout.inherits(scout.Planner, scout.ModelAdapter);

scout.Planner.Direction = {
  BACKWARD: -1,
  FORWARD: 1
};

scout.Planner.DisplayMode = {
  DAY:  1,
  WEEK:  2,
  MONTH:  3,
  WORK:  4,
  CALENDAR_WEEK:  5,
  YEAR:  6
};

scout.Planner.SelectionMode = {
  NONE: 0,
  ACTIVITY: 1,
  SINGLE_RANGE: 2,
  MULTI_RANGE: 3
};

scout.Planner.prototype.init = function(model, session) {
  scout.Planner.parent.prototype.init.call(this, model, session);
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
  this.$year = this.$container.appendDiv('planner-year-container').appendDiv('planner-year');
  this.$grid = this.$container.appendDiv('planner-grid').mousedown(this._onCellMousedown.bind(this));
  this.$scale = this.$container.appendDiv('planner-scale');
  this.menuBar.render(this.$container);

  scout.scrollbars.install(this.$grid);
  this.session.detachHelper.pushScrollable(this.$grid);

  // header contains all controls
  this.$range = this.$header.appendDiv('planner-range');
  this.$range.appendDiv('planner-minus').click(this._onClickPrevious.bind(this));
  this.$range.appendDiv('planner-plus').click(this._onClickNext.bind(this));
  this.$range.appendDiv('planner-select');

  // and modes
  this.$commands = this.$header.appendDiv('planner-commands');
  this._renderAvailableDisplayModes();

  this._updateModel();
  this._updateScreen();
};

scout.Planner.prototype._renderProperties = function() {
  scout.Planner.parent.prototype._renderProperties.call(this);

  //TODO CGU/CRU vermutlich nicht nötig, da in updateScreen gemacht
  //this._renderSelectedResources();
  // render with setTimeout because the planner needs to be layouted first
  setTimeout(this._renderSelectionRange.bind(this));
  this._renderDisplayMode();
  this._renderHeaderVisible();
  this._renderMenus();
};

/* -- basics, events -------------------------------------------- */

scout.Planner.prototype._onClickPrevious = function(event) {
  this._navigateDate(scout.Planner.Direction.BACKWARD);
};

scout.Planner.prototype._onClickNext = function(event) {
  this._navigateDate(scout.Planner.Direction.FORWARD);
};

scout.Planner.prototype._navigateDate = function(direction) {
  var DISPLAY_MODE = scout.Planner.DisplayMode;
  if (this.displayMode == DISPLAY_MODE.DAY) {
    this.viewRange.from = scout.dates.shift(this.viewRange.from, 0, 0, direction);
    this.viewRange.to = scout.dates.shift(this.viewRange.to, 0, 0, direction);
  } else if (this.displayMode == DISPLAY_MODE.WEEK || this.displayMode == DISPLAY_MODE.WORK) {
    this.viewRange.from = scout.dates.shift(this.viewRange.from, 0, 0, direction * 7);
    this.viewRange.to = scout.dates.shift(this.viewRange.to, 0, 0, direction * 7);
  } else if (this.displayMode == DISPLAY_MODE.MONTH) {
    this.viewRange.from = scout.dates.shift(this.viewRange.from, 0, direction, 0);
    this.viewRange.to = scout.dates.shift(this.viewRange.to, 0, direction, 0);
  } else if (this.displayMode == DISPLAY_MODE.CALENDAR_WEEK) {
    this.viewRange.from = scout.dates.shift(this.viewRange.from, 0, direction, 0);
    this.viewRange.to = scout.dates.shift(this.viewRange.to, 0, direction, 0);
  } else if (this.displayMode == DISPLAY_MODE.YEAR) {
    this.viewRange.from = scout.dates.shift(this.viewRange.from, 0, 3 * direction, 0);
    this.viewRange.to = scout.dates.shift(this.viewRange.to, 0, 3 * direction, 0);
  }

  this._updateModel();
  this._updateScreen();
};

scout.Planner.prototype._onClickToday = function(event) {
  // new selected date
  this.selected = new Date();

  // update calendar
  this._updateModel();
  this._updateScreen();
};

scout.Planner.prototype._onClickDisplayMode = function(event) {
  var displayMode = $(event.target).data('mode');
  this.setDisplayMode(displayMode);
};

scout.Planner.prototype._onClickYear = function(event) {
  // set flag
  this.showYearPanel = !this.showYearPanel;

  // update screen
  this._updateScreen();
};

scout.Planner.prototype._onRangeSelectorContextMenu = function(event) {
  this._showContextMenu(event, 'Planner.Range');
};

scout.Planner.prototype._onResourceContextMenu = function(event) {
  //FIXME CGU only show contextmenu for selected resource
  this._showContextMenu(event, 'Planner.Resource');
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

/* --  set display mode and range ------------------------------------- */

scout.Planner.prototype._updateModel = function() {};

scout.Planner.prototype._updateScreen = function() {
  // clear before draw
  if (this.rendered) {
    this._removeAllResources();
  }

  // update
  this._layoutRange();
  this._layoutScale();

  this._renderResources();
  this._renderSelectedResources();

  // if year shown and changed, redraw year
  if (this.showYearPanel) {
    this.$year.empty();
    this.drawYear();

    this.$year.parent().animateAVCSD('width', 270);
    this.$grid.animateAVCSD('width', '-=270', function() {
      this.$grid.css('width', 'calc(100% - 270px)');
    }.bind(this));
    this.$scale.animateAVCSD('width', '-=270', function() {
      this.$scale.css('width', 'calc(100% - 270px)');
    }.bind(this));
  }
  if (this.$year.parent().width() !== 0) {
    this.$year.parent().animateAVCSD('width', 0);
    this.$grid.animateAVCSD('width', '100%');
    this.$scale.animateAVCSD('width', '100%');
  }
  this.$year.parent().setVisible(this.showYearPanel);

  // color year
  this.colorYear();
};

scout.Planner.prototype._layoutRange = function() {
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
scout.Planner.prototype._layoutScale = function() {
  var $timelineLarge, $timelineSmall, loop, $divLarge, $divSmall, width,
    DISPLAY_MODE = scout.Planner.DisplayMode;

  // empty scale
  this.$scale.empty();

  // append main elements
  this.$scale.appendDiv('planner-scale-title', "Titel");
  $timelineLarge = this.$scale.appendDiv('timeline-large');
  $timelineSmall = this.$scale.appendDiv('timeline-small');

  // fill timeline large depending on mode
  // TODO: depending on screen size: smaller or large representation
  // TODO: change to shift
  if (this.displayMode === DISPLAY_MODE.DAY) {
    loop = new Date(this.viewRange.from.valueOf());

    // from start to end
    while (loop < this.viewRange.to) {
      if ((loop.getMinutes() === 0) || (loop.valueOf() == this.viewRange.from.valueOf())) {
        $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'HH')).data('count', 0);
      }

      $divSmall = $timelineSmall
        .appendDiv('scale-item', this._dateFormat(loop, ':mm'))
        .data('date-from', new Date(loop.valueOf()));

      loop.setMinutes(loop.getMinutes() + 30);
      $divSmall.data('date-to', new Date(loop.valueOf()));

      $divLarge.data('count', $divLarge.data('count') + 1);
    }
  } else if ((this.displayMode === DISPLAY_MODE.WORK) || (this.displayMode === DISPLAY_MODE.WEEK)) {
    loop = new Date(this.viewRange.from.valueOf());

    // from start to end
    while (loop < this.viewRange.to) {
      if ((loop.getHours() === 0) || (loop.valueOf() == this.viewRange.from.valueOf())) {
        if ((loop.getMonth() === 0) || (loop.valueOf() == this.viewRange.from.valueOf())) {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'd. MMMM yyyy')).data('count', 0);
        } else if (loop.getDate() === 1) {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'd. MMM')).data('count', 0);
        } else {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'd.')).data('count', 0);
        }
      }

      $divSmall = $timelineSmall
        .appendDiv('scale-item', this._dateFormat(loop, 'HH:mm'))
        .data('date-from', new Date(loop.valueOf()));

      loop.setHours(loop.getHours() + 6);
      $divSmall.data('date-to', new Date(loop.valueOf()));

      $divLarge.data('count', $divLarge.data('count') + 1);
    }

  } else if (this.displayMode === DISPLAY_MODE.MONTH) {
    loop = new Date(this.viewRange.from.valueOf());

    // from start to end
    while (loop < this.viewRange.to) {
      if ((loop.getDate() == 1) || (loop.valueOf() == this.viewRange.from.valueOf())) {
        if ((loop.getMonth() === 0) || (loop.valueOf() == this.viewRange.from.valueOf())) {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'MMMM yyyy')).data('count', 0);
        } else {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'MMMM')).data('count', 0);
        }
      }

      $divSmall = $timelineSmall
        .appendDiv('scale-item', this._dateFormat(loop, 'dd'))
        .data('date-from', new Date(loop.valueOf()));

      loop.setDate(loop.getDate() + 1);
      $divSmall.data('date-to', new Date(loop.valueOf()));

      $divLarge.data('count', $divLarge.data('count') + 1);
    }

  } else if (this.displayMode === DISPLAY_MODE.CALENDAR_WEEK) {
    loop = new Date(this.viewRange.from.valueOf());

    // from start to end
    while (loop < this.viewRange.to) {
      if ((loop.getDate() < 8) || (loop.valueOf() == this.viewRange.from.valueOf())) {
        if ((loop.getMonth() === 0) || (loop.valueOf() == this.viewRange.from.valueOf())) {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'MMMM yyyy')).data('count', 0);
        } else {
          $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'MMMM')).data('count', 0);
        }
      }

      $divSmall = $timelineSmall
        .appendDiv('scale-item', scout.dates.weekInYear(loop))
        .data('date-from', new Date(loop.valueOf()))
        .mouseenter(this._onScaleHoverIn.bind(this))
        .mouseleave(this._onScaleHoverOut.bind(this));

      loop.setDate(loop.getDate() + 7);
      $divSmall.data('date-to', new Date(loop.valueOf()));

      $divLarge.data('count', $divLarge.data('count') + 1);
    }

  } else if (this.displayMode === DISPLAY_MODE.YEAR) {
    loop = new Date(this.viewRange.from.valueOf());

    // from start to end
    while (loop < this.viewRange.to) {
      if ((loop.getMonth() === 0) || (loop.valueOf() == this.viewRange.from.valueOf())) {
        $divLarge = $timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'yyyy')).data('count', 0);
      }

      $divSmall = $timelineSmall
        .appendDiv('scale-item', this._dateFormat(loop, 'MMMM'))
        .data('date-from', new Date(loop.valueOf()));

      loop.setMonth(loop.getMonth() + 1);
      $divSmall.data('date-to', new Date(loop.valueOf()));

      $divLarge.data('count', $divLarge.data('count') + 1);
    }
  }

  // set sizes
  width = 100 / $timelineSmall.children().length;
  $timelineLarge.children().each(function() {
    $(this).css('width', $(this).data('count') * width + '%');
  });
  $timelineSmall.children().css('width', width + '%');

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
    .text(resource.resourceCell.text)
    .on('contextmenu', this._onResourceContextMenu.bind(this));
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
    .css('width', 'calc(' + this.transformWidth(end - begin) + '% - 4px')
    .on('contextmenu', this._onActivityContextMenu.bind(this));

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
    if (event.which === 3 || event.which === 1 && event.ctrlKey) {
      // don't select, context menu will be opened
      return;
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
  if (this.startRow === null || this.lastRow === null || this.startRange === null || this.lastRange === null) {
    return;
  }
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
  }), false);

  // left and width
  var from = Math.min(this.lastRange.from, this.startRange.from),
    to = Math.max(this.lastRange.to, this.startRange.to);
  var selectionRange = {
    from: new Date(from),
    to: new Date(to)
  };

  this.selectRange(selectionRange, !whileSelecting);
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

/* -- year, draw and color ---------------------------------------- */

scout.Planner.prototype.drawYear = function() {
  // init vars
  var year = this.viewRange.from.getFullYear(),
    first, $month, day, $day;

  // set title
  var $title = this.$year.appendDiv('year-title').data('year', year);

  // append 3 years
  $title.appendDiv('year-title-item', year - 1)
    .data('year-diff', -1)
    .click(this._onYearClick.bind(this));

  $title.appendDiv('year-title-item selected', year);

  $title.appendDiv('year-title-item', year + 1)
    .data('year-diff', +1)
    .click(this._onYearClick.bind(this));

  // add months and days
  for (var month = 0; month < 12; month++) {
    first = new Date(year, month, 1);
    $month = this.$year.appendDiv('year-month').attr('data-title', this._dateFormat(first, 'MMMM'));
    for (var d = 1; d <= 31; d++) {
      day = new Date(year, month, d);

      // stop if day is already out of range
      if (day.getMonth() !== month) {
        break;
      }

      // add div per day
      $day = $month.appendDiv('year-day', d).data('date', day);

      // first day has margin depending on weekday
      if (d === 1) {
        $day.css('margin-left', ((day.getDay() + 6) % 7) * $day.outerWidth());
      }
    }
  }

  // bind events for days divs
  $('.year-day', this.$year)
    .click(this._onYearDayClick.bind(this))
    .hover(this._onYearHoverIn.bind(this), this._onYearHoverOut.bind(this));
};

scout.Planner.prototype.colorYear = function() {
  // color is only needed if visible
  if (!this.showYearPanel) {
    return;
  }
  /*
  // remove color information
  $('.year-day.year-range, .year-day.year-range-day', this.$year).removeClass('year-range year-range-day');

  // loop all days and colorize based on range and selected
  var that = this,
    $day, date;

  $('.year-day', this.$year).each(function() {
    $day = $(this);
    date = $day.data('date');

    if (that.displayMode !== that.DAY && date >= that.start && date <= that.end) {
      $day.addClass('year-range');
    }

    if (scout.dates.isSameDay(date, that.selected)) {
      $day.addClass('year-range-day');
    }
  });*/
};

/* -- year, events ---------------------------------------- */

scout.Planner.prototype._onYearClick = function(event) {
  // prepare calculation
  var diff = $(event.target).data('year-diff'),
    year = this.selected.getFullYear(),
    month = this.selected.getMonth(),
    date = this.selected.getDate();

  // find new selected date
  this.selected = new Date(year + diff, month, date);

  // update calendar
  this._updateModel();
  this._updateScreen();
};

scout.Planner.prototype._onYearDayClick = function(event) {
  // new selected day
  this.selected = $('.year-hover-day', this.$year).data('date');

  // update calendar
  this._updateModel();
  this._updateScreen();
};

scout.Planner.prototype._onYearHoverIn = function(event) {
  // init vars
  var startHover, endHover, $day2, date2,
    $day = $(event.target),
    date1 = $day.data('date'),
    year = date1.getFullYear(),
    month = date1.getMonth(),
    date = date1.getDate(),
    day = (date1.getDay() + 6) % 7,
    DISPLAY_MODE = scout.Planner.DisplayMode;

  // find hover based on mode
  if (this.displayMode === DISPLAY_MODE.DAY) {
    startHover = new Date(year, month, date);
    endHover = new Date(year, month, date);
  } else if (this.displayMode === DISPLAY_MODE.WEEK) {
    startHover = new Date(year, month, date - day);
    endHover = new Date(year, month, date - day + 6);
  } else if (this.displayMode === DISPLAY_MODE.MONTH) {
    startHover = new Date(year, month, 1);
    endHover = new Date(year, month + 1, 0);
  } else if (this.displayMode === DISPLAY_MODE.WORK) {
    startHover = new Date(year, month, date - day);
    endHover = new Date(year, month, date - day + 4);

    // in case of work week: selected date has to be opart of range
    if (date1 > endHover) {
      date1 = endHover;
    }
  }

  // loop days and colorize based on hover start and hover end
  $('.year-day', this.$year).each(function() {
    $day2 = $(this);
    date2 = $day2.data('date');

    if (date2 >= startHover && date2 <= endHover) {
      $day2.addClass('year-hover');
    } else {
      $day2.removeClass('year-hover');
    }

    if (scout.dates.isSameDay(date1, date2)) {
      $day2.addClass('year-hover-day');
    }
  });
};

scout.Planner.prototype._onYearHoverOut = function(event) {
  // remove all hover effects
  $('.year-day.year-hover, .year-day.year-hover-day', this.$year).removeClass('year-hover year-hover-day');
};

/* -- helper ---------------------------------------------------- */

scout.Planner.prototype._dateFormat = function(date, pattern) {
  var d = new Date(date.valueOf()),
    dateFormat = new scout.DateFormat(this.session.locale, pattern);

  return dateFormat.format(d);
};

scout.Planner.prototype._renderViewRange = function() {
  //FIXME CGU/CRU always redraw whole screen? what if several properties change?
  this._updateScreen();
};

scout.Planner.prototype._renderHeaderVisible = function() {
  this.$header.setVisible(this.headerVisible);
  this.invalidateTree();
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

  if (!startRow || !lastRow) {
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

scout.Planner.prototype._renderDrawSections = function() {};

scout.Planner.prototype._resourcesByIds = function(ids) {
  return ids.map(this._resourceById.bind(this));
};

scout.Planner.prototype._resourceById = function(id) {
  return this.resourceMap[id];
};

scout.Planner.prototype.setDisplayMode = function(displayMode) {
  this.displayMode = displayMode;
  this._sendSetDisplayMode(displayMode);

  //FIXME CGU/CRU currently only triggered by server, because end time is not known here
  //  this._updateScreen();
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
    this.htmlComp.invalidateTree();
  }
};

scout.Planner.prototype._deleteResources = function(resources) {
  resources.forEach(function(resource) {
    // Update model
    scout.arrays.remove(this.resources, resource);
    delete this.resourcesMap[resource.id];

    // Update HTML
    if (this.rendered) {
      resource.$resource.remove();
      delete resource.$resource;
    }
  }.bind(this));
};

scout.Planner.prototype._deleteAllResources = function() {
  // Update HTML
  if (this.rendered) {
    this._removeAllResources();
    this.htmlComp.invalidateTree();
  }

  // Update model
  this.resources = [];
  this.resourcesMap = {};
};

scout.Planner.prototype._sendSetDisplayMode = function(displayMode) {
  this.session.send(this.id, 'setDisplayMode', {
    displayMode: displayMode
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
