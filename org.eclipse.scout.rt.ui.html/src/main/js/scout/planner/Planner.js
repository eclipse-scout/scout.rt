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
import {MenuBar} from '../index';
import {dates} from '../index';
import {menus as menus_1} from '../index';
import {objects} from '../index';
import {HtmlComponent} from '../index';
import {KeyStrokeContext} from '../index';
import * as $ from 'jquery';
import {scout} from '../index';
import {TooltipSupport} from '../index';
import {graphics} from '../index';
import {defaultValues} from '../index';
import {scrollbars} from '../index';
import {DateFormat} from '../index';
import {tooltips} from '../index';
import {Range} from '../index';
import {strings} from '../index';
import {DateRange} from '../index';
import {PlannerMenuItemsOrder} from '../index';
import {PlannerLayout} from '../index';
import {Widget} from '../index';
import {styles} from '../index';
import {arrays} from '../index';

export default class Planner extends Widget {

constructor() {
  super();

  this.activityMap = [];
  this.activitySelectable = false;
  this.availableDisplayModes = [];
  this.displayMode;
  this.displayModeOptions = {};
  this.headerVisible = true;
  this.label;
  this.resources = [];
  this.resourceMap = [];
  this.selectionMode = Planner.SelectionMode.MULTI_RANGE;
  this.selectionRange = new DateRange();
  this.selectedResources = [];
  this.viewRange = {};

  // visual
  this._resourceTitleWidth = 20;
  this._rangeSelectionStarted = false;
  this.startRow;
  this.lastRow;

  // main elements
  this.$container;
  this.$range;
  this.$modes;
  this.$grid;

  // scale calculator
  this.transformLeft = function(t) {
    return t;
  };
  this.transformWidth = function(t0, t1) {
    return (t1 - t0);
  };

  this.yearPanelVisible = false;
  this._addWidgetProperties(['menus']);
}


static Direction = {
  BACKWARD: -1,
  FORWARD: 1
};

/**
 * Enum providing display-modes for planner (extends calendar).
 * @see IPlannerDisplayMode.java
 */
static DisplayMode = {
  DAY: 1,
  WEEK: 2,
  MONTH: 3,
  WORK_WEEK: 4,
  CALENDAR_WEEK: 5,
  YEAR: 6
};

static SelectionMode = {
  NONE: 0,
  SINGLE_RANGE: 1,
  MULTI_RANGE: 2
};

static RANGE_SELECTION_MOVE_THRESHOLD = 10;

/**
 * @override
 */
_createKeyStrokeContext() {
  return new KeyStrokeContext();
}

_init(model) {
  super._init( model);
  this._yearPanel = scout.create('YearPanel', {
    parent: this,
    alwaysSelectFirstDay: true
  });
  this._yearPanel.on('dateSelect', this._onYearPanelDateSelect.bind(this));
  this._header = scout.create('PlannerHeader', {
    parent: this
  });
  this._header.on('todayClick', this._onTodayClick.bind(this));
  this._header.on('yearClick', this._onYearClick.bind(this));
  this._header.on('previousClick', this._onPreviousClick.bind(this));
  this._header.on('nextClick', this._onNextClick.bind(this));
  this._header.on('displayModeClick', this._onDisplayModeClick.bind(this));
  this.menuBar = scout.create('MenuBar', {
    parent: this,
    position: MenuBar.Position.BOTTOM,
    menuOrder: new PlannerMenuItemsOrder(this.session, 'Planner')
  });
  for (var i = 0; i < this.resources.length; i++) {
    this._initResource(this.resources[i]);
  }
  this._setDisplayMode(this.displayMode);
  this._setAvailableDisplayModes(this.availableDisplayModes);
  this._setViewRange(this.viewRange);
  this._setSelectedResources(this.selectedResources);
  this._setSelectedActivity(this.selectedActivity);
  this._setSelectionRange(this.selectionRange);
  this._setMenus(this.menus);
  this._setDisplayModeOptions(this.displayModeOptions);

  this._tooltipSupport = new TooltipSupport({
    parent: this,
    arrowPosition: 50
  });
}

_initResource(resource) {
  defaultValues.applyTo(resource, 'Resource');
  resource.activities.forEach(function(activity) {
    this._initActivity(activity);
  }, this);
  this.resourceMap[resource.id] = resource;
}

_initActivity(activity) {
  activity.beginTime = dates.parseJsonDate(activity.beginTime);
  activity.endTime = dates.parseJsonDate(activity.endTime);
  defaultValues.applyTo(activity, 'Activity');
  this.activityMap[activity.id] = activity;
}

_render() {
  // basics, layout etc.
  this.$container = this.$parent.appendDiv('planner');
  var layout = new PlannerLayout(this);
  this.htmlComp = HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(layout);

  // main elements
  this._header.render();
  this._yearPanel.render();
  this.$grid = this.$container.appendDiv('planner-grid')
    .on('mousedown', '.resource-cells', this._onCellMouseDown.bind(this))
    .on('mousedown', '.resource-title', this._onResourceTitleMouseDown.bind(this))
    .on('contextmenu', '.resource-title', this._onResourceTitleContextMenu.bind(this))
    .on('contextmenu', '.planner-activity', this._onActivityContextMenu.bind(this));
  this.$scale = this.$container.appendDiv('planner-scale');
  this.menuBar.render();

  tooltips.install(this.$grid, {
    parent: this,
    selector: '.planner-activity',
    text: function($comp) {
      if (this._activityById($comp.attr('data-id'))) {
        return this._activityById($comp.attr('data-id')).tooltipText;
      } else {
        return undefined;
      }
    }.bind(this)
  });

  this._installScrollbars();
  this._gridScrollHandler = this._onGridScroll.bind(this);
  this.$grid.on('scroll', this._gridScrollHandler);
}

_renderProperties() {
  super._renderProperties();

  this._renderViewRange();
  this._renderHeaderVisible();
  this._renderYearPanelVisible(false);
  this._renderResources();
  this._renderSelectedActivity();
  this._renderSelectedResources();
  // render with setTimeout because the planner needs to be layouted first
  setTimeout(this._renderSelectionRange.bind(this));
}

/**
 * @override
 */
get$Scrollable() {
  return this.$grid;
}

/* -- basics, events -------------------------------------------- */

_onPreviousClick(event) {
  this._navigateDate(Planner.Direction.BACKWARD);
}

_onNextClick(event) {
  this._navigateDate(Planner.Direction.FORWARD);
}

_navigateDate(direction) {
  var viewRange = new DateRange(this.viewRange.from, this.viewRange.to),
    displayMode = Planner.DisplayMode;

  if (this.displayMode === displayMode.DAY) {
    viewRange.from = dates.shift(this.viewRange.from, 0, 0, direction);
    viewRange.to = dates.shift(this.viewRange.to, 0, 0, direction);
  } else if (scout.isOneOf(this.displayMode, displayMode.WEEK, displayMode.WORK_WEEK)) {
    viewRange.from = dates.shift(this.viewRange.from, 0, 0, direction * 7);
    viewRange.from = dates.ensureMonday(viewRange.from, -1 * direction);
    viewRange.to = dates.shift(this.viewRange.to, 0, 0, direction * 7);
  } else if (this.displayMode === displayMode.MONTH) {
    viewRange.from = dates.shift(this.viewRange.from, 0, direction, 0);
    viewRange.from = dates.ensureMonday(viewRange.from, -1 * direction);
    viewRange.to = dates.shift(this.viewRange.to, 0, direction, 0);
  } else if (this.displayMode === displayMode.CALENDAR_WEEK) {
    viewRange.from = dates.shift(this.viewRange.from, 0, direction, 0);
    viewRange.from = dates.ensureMonday(viewRange.from, -1 * direction);
    viewRange.to = dates.shift(this.viewRange.to, 0, direction, 0);
  } else if (this.displayMode === displayMode.YEAR) {
    viewRange.from = dates.shift(this.viewRange.from, 0, 3 * direction, 0);
    viewRange.to = dates.shift(this.viewRange.to, 0, 3 * direction, 0);
  }

  this.setViewRange(viewRange);
}

_onTodayClick(event) {
  var today = new Date(),
    year = today.getFullYear(),
    month = today.getMonth(),
    date = today.getDate(),
    day = (today.getDay() + 6) % 7,
    displayMode = Planner.DisplayMode;

  if (this.displayMode === displayMode.DAY) {
    today = new Date(year, month, date);
  } else if (this.displayMode === displayMode.YEAR) {
    today = new Date(year, month, 1);
  } else {
    today = new Date(year, month, date - day);
  }

  this.setViewRangeFrom(today);
}

_onDisplayModeClick(event) {
  var displayMode = event.displayMode;
  this.setDisplayMode(displayMode);
}

_onYearClick(event) {
  this.setYearPanelVisible(!this.yearPanelVisible);
}

_onYearPanelDateSelect(event) {
  this.setViewRangeFrom(event.date);
}

_onResourceTitleMouseDown(event) {
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
}

_onResourceTitleContextMenu(event) {
  this._showContextMenu(event, 'Planner.Resource');
}

_onRangeSelectorContextMenu(event) {
  this._showContextMenu(event, 'Planner.Range');
}

_onActivityContextMenu(event) {
  this._showContextMenu(event, 'Planner.Activity');
}

_showContextMenu(event, allowedType) {
  event.preventDefault();
  event.stopPropagation();
  var func = function func(event, allowedType) {
    if (!this.rendered || !this.attached) { // check needed because function is called asynchronously
      return;
    }
    var filteredMenus = this._filterMenus([allowedType], true),
      $part = $(event.currentTarget);
    if (filteredMenus.length === 0) {
      return; // at least one menu item must be visible
    }
    var popup = scout.create('ContextMenuPopup', {
      parent: this,
      menuItems: filteredMenus,
      location: {
        x: event.pageX,
        y: event.pageY
      },
      $anchor: $part
    });
    popup.open();
  }.bind(this);

  this.session.onRequestsDone(func, event, allowedType);
}

_onGridScroll() {
  this._reconcileScrollPos();
}

_reconcileScrollPos() {
  // When scrolling horizontally scroll scale as well
  var scrollLeft = this.$grid.scrollLeft();
  this.$scale.scrollLeft(scrollLeft);
}

_renderRange() {
  if (!this.viewRange.from || !this.viewRange.to) {
    return;
  }
  var text,
    toDate = new Date(this.viewRange.to.valueOf() - 1),
    toText = this.session.text('ui.to'),
    displayMode = Planner.DisplayMode;

  // find range text
  if (dates.isSameDay(this.viewRange.from, toDate)) {
    text = this._dateFormat(this.viewRange.from, 'd. MMMM yyyy');
  } else if (this.viewRange.from.getMonth() === toDate.getMonth() && this.viewRange.from.getFullYear() === toDate.getFullYear()) {
    text = strings.join(' ', this._dateFormat(this.viewRange.from, 'd.'), toText, this._dateFormat(toDate, 'd. MMMM yyyy'));
  } else if (this.viewRange.from.getFullYear() === toDate.getFullYear()) {
    if (this.displayMode === displayMode.YEAR) {
      text = strings.join(' ', this._dateFormat(this.viewRange.from, 'MMMM'), toText, this._dateFormat(toDate, 'MMMM yyyy'));
    } else {
      text = strings.join(' ', this._dateFormat(this.viewRange.from, 'd.  MMMM'), toText, this._dateFormat(toDate, 'd. MMMM yyyy'));
    }
  } else {
    if (this.displayMode === displayMode.YEAR) {
      text = strings.join(' ', this._dateFormat(this.viewRange.from, 'MMMM yyyy'), toText, this._dateFormat(toDate, 'MMMM yyyy'));
    } else {
      text = strings.join(' ', this._dateFormat(this.viewRange.from, 'd.  MMMM yyyy'), toText, this._dateFormat(toDate, 'd. MMMM yyyy'));
    }
  }

  // set text
  $('.planner-select', this._header.$range).text(text);
}

_renderScale() {
  if (!this.viewRange.from || !this.viewRange.to) {
    return;
  }
  var width,
    that = this,
    displayMode = Planner.DisplayMode;

  // empty scale
  this.$scale.empty();
  this.$grid.children('.planner-small-scale-item-line').remove();
  this.$grid.children('.planner-large-scale-item-line').remove();

  // append main elements
  this.$scaleTitle = this.$scale.appendDiv('planner-scale-title');
  this._renderLabel();
  this.$timeline = this.$scale.appendDiv('timeline');
  this.$timelineLarge = this.$timeline.appendDiv('timeline-large');
  this.$timelineSmall = this.$timeline.appendDiv('timeline-small');

  // fill timeline large depending on mode
  if (this.displayMode === displayMode.DAY) {
    this._renderDayScale();
  } else if (scout.isOneOf(this.displayMode, displayMode.WORK_WEEK, displayMode.WEEK)) {
    this._renderWeekScale();
  } else if (this.displayMode === displayMode.MONTH) {
    this._renderMonthScale();
  } else if (this.displayMode === displayMode.CALENDAR_WEEK) {
    this._renderCalendarWeekScale();
  } else if (this.displayMode === displayMode.YEAR) {
    this._renderYearScale();
  }

  // set sizes and append scale lines
  var $smallScaleItems = this.$timelineSmall.children('.scale-item');
  var $largeScaleItems = this.$timelineLarge.children('.scale-item');
  width = 100 / $smallScaleItems.length;
  $largeScaleItems.each(function() {
    var $scaleItem = $(this);
    var $largeGridLine = that.$grid.prependDiv('planner-large-scale-item-line');
    $scaleItem.css('width', $scaleItem.data('count') * width + '%')
      .data('scale-item-line', $largeGridLine);
    $scaleItem.prependDiv('planner-large-scale-item-line')
      .css('left', 0);
  });
  $smallScaleItems.each(function(index) {
    var $scaleItem = $(this);
    $scaleItem.css('width', width + '%');
    if (!$scaleItem.data('first')) {
      var $smallGridLine = that.$grid.prependDiv('planner-small-scale-item-line');
      $scaleItem.data('scale-item-line', $smallGridLine);
      $scaleItem.prependDiv('planner-small-scale-item-line')
        .css('left', 0);
    }
  });

  // find transfer function
  this.beginScale = this.$timelineSmall.children().first().data('date-from').valueOf();
  this.endScale = this.$timelineSmall.children().last().data('date-to').valueOf();

  if (scout.isOneOf(this.displayMode, displayMode.WORK_WEEK, displayMode.WEEK)) {
    var options = this.displayModeOptions[this.displayMode];
    var interval = options.interval;
    var firstHourOfDay = options.firstHourOfDay;
    var lastHourOfDay = options.lastHourOfDay;

    this.transformLeft = function(begin, end, firstHour, lastHour, interval) {
      return function(t) {
        t = new Date(t);
        begin = new Date(begin);
        end = new Date(end);
        var fullRangeMillis = end - begin;
        // remove day component from range for scaling
        var dayDiffTBegin = dates.compareDays(t, begin);
        var dayDIffEndBegin = dates.compareDays(end, begin);
        var dayComponentMillis = dayDiffTBegin * 3600000 * 24;
        var rangeScaling = (24 / (lastHour - firstHour + 1));
        // re-add day component
        var dayOffset = dayDiffTBegin / dayDIffEndBegin;

        if (t.getHours() < firstHour) {
          // If t is in the morning before the first hour of day, return 00:00 of this day
          return (rangeScaling / fullRangeMillis + dayOffset) * 100;
        }
        if (t.getHours() > lastHour) {
          // If t is in the evening after the last hour of day, return 00:00 of next day
          dayOffset = (dayDiffTBegin + 1) / dayDIffEndBegin;
          return (rangeScaling / fullRangeMillis + dayOffset) * 100;
        }

        return ((t.valueOf() - (begin.valueOf() + firstHour * 3600000) - dayComponentMillis) * rangeScaling / fullRangeMillis + dayOffset) * 100;
      };
    }(this.viewRange.from, this.viewRange.to, firstHourOfDay, lastHourOfDay, interval);

    this.transformWidth = function(begin, end, firstHour, lastHour, interval) {
      return function(t0, t1) {
        var fullRangeMillis = end - begin;
        var selectionRange = new Range(new Date(t0), new Date(t1));
        var hiddenRanges = this._findHiddenRangesInWeekMode();
        var selectedRangeMillis = selectionRange.subtractAll(hiddenRanges).reduce(function(acc, range) {
          return acc + range.size();
        }, 0);
        var rangeScaling = (24 / (lastHour - firstHour + 1));
        return (selectedRangeMillis * rangeScaling) / fullRangeMillis * 100;
      };
    }(this.viewRange.from, this.viewRange.to, firstHourOfDay, lastHourOfDay, interval);
  } else {
    this.transformLeft = function(begin, end) {
      return function(t) {
        return (t - begin) / (end - begin) * 100;
      };
    }(this.beginScale, this.endScale);

    this.transformWidth = function(begin, end) {
      return function(t0, t1) {
        return (t1 - t0) / (end - begin) * 100;
      };
    }(this.beginScale, this.endScale);
  }
}

/**
 * Returns every hidden range of the view range created by first and last our of day.
 */
_findHiddenRangesInWeekMode() {
  if (!scout.isOneOf(this.displayMode, Planner.DisplayMode.WORK_WEEK, Planner.DisplayMode.WEEK)) {
    return [];
  }
  var ranges = [];
  var options = this.displayModeOptions[this.displayMode];
  var firstHourOfDay = options.firstHourOfDay;
  var lastHourOfDay = options.lastHourOfDay;
  var currentDate = new Date(this.viewRange.from.valueOf());
  var hiddenRange;
  while (currentDate < this.viewRange.to) {
    // Start of day range
    hiddenRange = new Range(new Date(currentDate.valueOf()), dates.shiftTime(currentDate, firstHourOfDay));
    if (hiddenRange.size() > 0) {
      ranges.push(hiddenRange);
    }
    // End of day range
    hiddenRange = new Range(dates.shiftTime(currentDate, lastHourOfDay + 1), dates.shiftTime(currentDate, 24));
    if (hiddenRange.size() > 0) {
      ranges.push(hiddenRange);
    }
    currentDate.setHours(0);
    currentDate.setMinutes(0);
    currentDate.setDate(currentDate.getDate() + 1);
  }
  return ranges;
}

_renderDayScale() {
  var newLargeGroup, $divLarge, $divSmall,
    first = true;
  var loop = new Date(this.viewRange.from.valueOf());

  var options = this.displayModeOptions[this.displayMode];
  var interval = options.interval;
  var labelPeriod = options.labelPeriod;
  var firstHourOfDay = options.firstHourOfDay;
  var lastHourOfDay = options.lastHourOfDay;

  // cap interval to day range
  interval = Math.min(interval, 60 * (lastHourOfDay - firstHourOfDay + 1));

  // from start to end
  while (loop < this.viewRange.to) {
    if (loop.getHours() >= firstHourOfDay && loop.getHours() <= lastHourOfDay) {
      newLargeGroup = false;
      if (loop.getMinutes() === 0 || first) {
        $divLarge = this.$timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'HH')).data('count', 0);
        newLargeGroup = true;
      }

      $divSmall = this.$timelineSmall
        .appendDiv('scale-item', this._dateFormat(loop, ':mm'))
        .data('date-from', new Date(loop.valueOf()));

      // hide label
      if ((loop.getMinutes() % (interval * labelPeriod)) !== 0) {
        $divSmall.addClass('label-invisible');
      }

      // increase variables
      loop = dates.shiftTime(loop, 0, interval, 0);
      this._incrementTimelineScaleItems($divLarge, $divSmall, loop, newLargeGroup);
      first = false;
    } else {
      loop = dates.shiftTime(loop, 0, interval, 0);
    }
  }
}

_renderWeekScale() {
  var newLargeGroup, $divLarge, $divSmall,
    first = true;
  var loop = new Date(this.viewRange.from.valueOf());

  var options = this.displayModeOptions[this.displayMode];
  var interval = options.interval;
  var labelPeriod = options.labelPeriod;
  var firstHourOfDay = options.firstHourOfDay;
  var lastHourOfDay = options.lastHourOfDay;

  // cap interval to day range
  interval = Math.min(interval, 60 * (lastHourOfDay - firstHourOfDay + 1));

  // from start to end
  while (loop < this.viewRange.to) {
    newLargeGroup = false;
    if (loop.getHours() < firstHourOfDay) {
      loop.setHours(firstHourOfDay);
    }

    if (loop.getHours() === firstHourOfDay && loop.getMinutes() === 0 || first) {
      if (loop.getMonth() === 0 || first) {
        $divLarge = this.$timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'd. MMMM yyyy')).data('count', 0);
      } else if (loop.getDate() === 1) {
        $divLarge = this.$timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'd. MMMM')).data('count', 0);
      } else {
        $divLarge = this.$timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'd.')).data('count', 0);
      }
      newLargeGroup = true;
    }

    $divSmall = this.$timelineSmall
      .appendDiv('scale-item', this._dateFormat(loop, 'HH:mm'))
      .data('date-from', new Date(loop.valueOf()));

    // hide label
    if (((loop.getHours() - firstHourOfDay) * 60 + loop.getMinutes()) % (interval * labelPeriod) !== 0) {
      $divSmall.addClass('label-invisible');
    }

    // increase variables
    loop = dates.shiftTime(loop, 0, interval, 0, 0);
    this._incrementTimelineScaleItems($divLarge, $divSmall, loop, newLargeGroup);
    first = false;

    if (loop.getHours() > lastHourOfDay) {
      // jump to next day
      loop.setHours(0);
      loop.setMinutes(0);
      loop.setDate(loop.getDate() + 1);
    }
  }
}

_renderMonthScale() {
  var newLargeGroup, $divLarge, $divSmall,
    first = true;
  var loop = new Date(this.viewRange.from.valueOf());

  var options = this.displayModeOptions[this.displayMode];
  var labelPeriod = options.labelPeriod;

  // from start to end
  while (loop < this.viewRange.to) {
    newLargeGroup = false;
    if (loop.getDate() === 1 || first) {
      if (loop.getMonth() === 0 || first) {
        $divLarge = this.$timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'MMMM yyyy')).data('count', 0);
      } else {
        $divLarge = this.$timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'MMMM')).data('count', 0);
      }
      newLargeGroup = true;
    }

    $divSmall = this.$timelineSmall
      .appendDiv('scale-item', this._dateFormat(loop, 'dd'))
      .data('date-from', new Date(loop.valueOf()));

    // hide label
    if (loop.getDate() % labelPeriod !== 0) {
      $divSmall.addClass('label-invisible');
    }

    loop = dates.shift(loop, 0, 0, 1);
    this._incrementTimelineScaleItems($divLarge, $divSmall, loop, newLargeGroup);
    first = false;
  }
}

_renderCalendarWeekScale() {
  var newLargeGroup, $divLarge, $divSmall,
    first = true;
  var loop = new Date(this.viewRange.from.valueOf());

  var options = this.displayModeOptions[this.displayMode];
  var labelPeriod = options.labelPeriod;

  // from start to end
  while (loop < this.viewRange.to) {
    newLargeGroup = false;
    if (loop.getDate() < 8 || first === true) {
      if (loop.getMonth() === 0 || first === true) {
        if (loop.getDate() > 11) {
          $divLarge = this.$timelineLarge.appendDiv('scale-item').html('&nbsp;').data('count', 0);
          first = 2;
        } else {
          $divLarge = this.$timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'MMMM yyyy')).data('count', 0);
          first = false;
        }
      } else {
        if (first === 2) {
          $divLarge = this.$timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'MMMM yyyy')).data('count', 0);
          first = false;
        } else {
          $divLarge = this.$timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'MMMM')).data('count', 0);
        }
      }
      newLargeGroup = true;
    }

    $divSmall = this.$timelineSmall
      .appendDiv('scale-item', dates.weekInYear(loop))
      .data('date-from', new Date(loop.valueOf()))
      .data('tooltipText', this._scaleTooltipText.bind(this));
    this._tooltipSupport.install($divSmall);

    // hide label
    if (dates.weekInYear(loop) % labelPeriod !== 0) {
      $divSmall.addClass('label-invisible');
    }

    loop.setDate(loop.getDate() + 7);
    this._incrementTimelineScaleItems($divLarge, $divSmall, loop, newLargeGroup);
  }
}

_renderYearScale() {
  var newLargeGroup, $divLarge, $divSmall,
    first = true;
  var loop = new Date(this.viewRange.from.valueOf());

  var options = this.displayModeOptions[this.displayMode];
  var labelPeriod = options.labelPeriod;

  // from start to end
  while (loop < this.viewRange.to) {
    newLargeGroup = false;
    if (loop.getMonth() === 0 || first) {
      $divLarge = this.$timelineLarge.appendDiv('scale-item', this._dateFormat(loop, 'yyyy')).data('count', 0);
      newLargeGroup = true;
    }

    $divSmall = this.$timelineSmall
      .appendDiv('scale-item', this._dateFormat(loop, 'MMMM'))
      .data('date-from', new Date(loop.valueOf()));

    // hide label
    if (loop.getMonth() % labelPeriod !== 0) {
      $divSmall.addClass('label-invisible');
    }

    loop = dates.shift(loop, 0, 1, 0);
    this._incrementTimelineScaleItems($divLarge, $divSmall, loop, newLargeGroup);
    first = false;
  }
}

_incrementTimelineScaleItems($largeComp, $smallComp, newDate, newLargeGroup) {
  $largeComp.data('count', $largeComp.data('count') + 1);

  $smallComp.data('date-to', new Date(newDate.valueOf()))
    .data('first', newLargeGroup);
}

/* -- scale events --------------------------------------------------- */

_scaleTooltipText($scale) {
  var toText = ' ' + this.session.text('ui.to') + ' ',
    from = new Date($scale.data('date-from').valueOf()),
    to = new Date($scale.data('date-to').valueOf() - 1);

  if (from.getMonth() === to.getMonth()) {
    return this._dateFormat(from, 'd.') + toText + this._dateFormat(to, 'd. MMMM yyyy');
  } else if (from.getFullYear() === to.getFullYear()) {
    return this._dateFormat(from, 'd. MMMM') + toText + this._dateFormat(to, 'd. MMMM yyyy');
  } else {
    return this._dateFormat(from, 'd. MMMM yyyy') + toText + this._dateFormat(to, 'd. MMMM yyyy');
  }
}

/* --  render resources, activities --------------------------------- */

_removeAllResources() {
  this.resources.forEach(function(resource) {
    resource.$resource.remove();
  });
}

_renderResources(resources) {
  var i, resource,
    resourcesHtml = '';

  resources = resources || this.resources;
  for (i = 0; i < resources.length; i++) {
    resource = resources[i];
    resourcesHtml += this._buildResourceHtml(resource, this.$grid);
  }

  // Append resources to grid
  $(resourcesHtml).appendTo(this.$grid);

  // Match resources
  this.$grid.children('.planner-resource').each(function(index, element) {
    var $element = $(element);
    resource = this._resourceById($element.attr('data-id'));
    this._linkResource($element, resource);
    this._linkActivitiesForResource(resource);
  }.bind(this));
}

_linkResource($resource, resource) {
  $resource.data('resource', resource);
  resource.$resource = $resource;
  resource.$cells = $resource.children('.resource-cells');
}

_linkActivity($activity, activity) {
  $activity.data('activity', activity);
  activity.$activity = $activity;
}

_rerenderActivities(resources) {
  resources = resources || this.resources;
  resources.forEach(function(resource) {
    this._removeActivititesForResource(resource);
    this._renderActivititesForResource(resource);
  }, this);
}

_buildResourceHtml(resource) {
  var resourceHtml = '<div class="planner-resource" data-id="' + resource.id + '">';
  resourceHtml += '<div class="resource-title">' + strings.encode(resource.resourceCell.text || '') + '</div>';
  resourceHtml += '<div class="resource-cells">' + this._buildActivitiesHtml(resource) + '</div>';
  resourceHtml += '</div>';
  return resourceHtml;
}

_renderActivititesForResource(resource) {
  resource.$cells.html(this._buildActivitiesHtml(resource));
  this._linkActivitiesForResource(resource);
}

_linkActivitiesForResource(resource) {
  resource.$cells.children('.planner-activity').each(function(index, element) {
    var $element = $(element);
    var activity = this._activityById($element.attr('data-id'));
    this._linkActivity($element, activity);
  }.bind(this));
}

_buildActivitiesHtml(resource) {
  var activitiesHtml = '';
  resource.activities.forEach(function(activity) {
    if (activity.beginTime.valueOf() >= this.endScale ||
      activity.endTime.valueOf() <= this.beginScale) {
      // don't add activities which are not in the view range
      return;
    }
    activitiesHtml += this._buildActivityHtml(activity);
  }, this);
  return activitiesHtml;
}

_removeActivititesForResource(resource) {
  resource.activities.forEach(function(activity) {
    if (activity.$activity) {
      activity.$activity.remove();
      activity.$activity = null;
    }
  }, this);
}

_buildActivityHtml(activity) {
  var level = 100 - Math.min(activity.level * 100, 100),
    backgroundColor = styles.modelToCssColor(activity.backgroundColor),
    foregroundColor = styles.modelToCssColor(activity.foregroundColor),
    levelColor = styles.modelToCssColor(activity.levelColor),
    begin = activity.beginTime.valueOf(),
    end = activity.endTime.valueOf();

  // Make sure activity fits into scale
  begin = Math.max(begin, this.beginScale);
  end = Math.min(end, this.endScale);

  var activityCssClass = 'planner-activity' + (activity.cssClass ? (' ' + activity.cssClass) : '');
  var activityStyle = 'left: ' + 'calc(' + this.transformLeft(begin) + '% + 2px);';
  activityStyle += ' width: ' + 'calc(' + this.transformWidth(begin, end) + '% - 4px);';

  if (levelColor) {
    activityStyle += ' background-color: ' + levelColor + ';';
    activityStyle += ' border-color: ' + levelColor + ';';
  }
  if (!levelColor && backgroundColor) {
    activityStyle += ' background-color: ' + backgroundColor + ';';
    activityStyle += ' border-color: ' + backgroundColor + ';';
  }
  if (foregroundColor) {
    activityStyle += ' color: ' + foregroundColor + ';';
  }

  // The background-color represents the fill level and not the image. This makes it easier to change the color using a css class
  // In order to change the background rather than the fill, use the planner-activity-level css class
  var activityLevelCssClass = 'planner-activity-level' + (activity.cssClass ? (' ' + activity.cssClass) : '');
  var activityEmptyColor = styles.get(activityLevelCssClass, 'background-color').backgroundColor;
  activityStyle += ' background-image: ' + 'linear-gradient(to bottom, ' + activityEmptyColor + ' 0%, ' + activityEmptyColor + ' ' + level + '%, transparent ' + level + '%, transparent 100% );';

  var activityHtml = '<div';
  activityHtml += ' class="' + activityCssClass + '"';
  activityHtml += ' style="' + activityStyle + '"';
  activityHtml += ' data-id="' + activity.id + '"';
  activityHtml += '>' + strings.encode(activity.text || '') + '</div>';
  return activityHtml;
}

/* -- selector -------------------------------------------------- */

_onCellMouseDown(event) {
  var $activity,
    $resource,
    $target = $(event.target),
    selectionMode = Planner.SelectionMode,
    opensContextMenu = (event.which === 3 || event.which === 1 && event.ctrlKey);

  if (this.activitySelectable) {
    if (!opensContextMenu && this.$selector) {
      // Hide selector otherwise activity may not be resolved (elementFromPoint would return the $selector)
      // This allows selecting an activity which is inside a selection range
      this.$selector.hide();
    }
    $activity = this.$grid.elementFromPoint(event.pageX, event.pageY);
    if (!opensContextMenu && this.$selector) {
      this.$selector.show();
    }
    if ($activity.hasClass('planner-activity')) {
      $resource = $activity.parent().parent();
      this.selectResources([$resource.data('resource')]);
      this.selectActivity($activity.data('activity'));
      this.selectRange(new DateRange());
    } else {
      this.selectActivity(null);
    }
  } else {
    this.selectActivity(null);
  }

  if (this.selectionMode === selectionMode.NONE) {
    return;
  }

  if ($target.hasClass('selector') && opensContextMenu) {
    // Right click on the selector must not clear the selection -> context menu will be opened
    return;
  }

  if (!this.selectedActivity) {
    // If not an activity was selected, start immediately, otherwise start as soon the mouse moves
    this._startRangeSelection(event.pageX, event.pageY);
  }

  // add event handlers
  this._cellMousemoveHandler = this._onCellMousemove.bind(this, event);
  $target.document()
    .on('mousemove', this._cellMousemoveHandler)
    .one('mouseup', this._onDocumentMouseUp.bind(this));
}

_startRangeSelection(pageX, pageY) {
  // init selector
  this.startRow = this._findRow(pageY);
  this.lastRow = this.startRow;

  // find range on scale
  this.startRange = this._findScale(pageX);
  this.lastRange = this.startRange;

  // draw
  this._select(true);
  this._rangeSelectionStarted = true;
}

/**
 * @returns true if the range selection may be started, false if not
 */
_prepareRangeSelectionByMousemove(mousedownEvent, mousemoveEvent) {
  var moveX = mousedownEvent.pageX - mousemoveEvent.pageX;
  var moveY = mousedownEvent.pageY - mousemoveEvent.pageY;
  var moveThreshold = Planner.RANGE_SELECTION_MOVE_THRESHOLD;
  if (Math.abs(moveX) >= moveThreshold) {
    // Accept if x movement is big enough
    return true;
  }
  var mousedownRow = this._findRow(mousedownEvent.pageY);
  var mousemoveRow = this._findRow(mousemoveEvent.pageY);
  if (Math.abs(moveY) >= moveThreshold && this.selectionMode === Planner.SelectionMode.MULTI_RANGE && mousedownRow !== mousemoveRow) {
    // Accept if y movement is big enough AND the row changed. No need to switch into range selection mode if cursor is still on the same row
    return true;
  }
  return false;
}

_onCellMousemove(mousedownEvent, event) {
  if (this.selectedActivity && !this._rangeSelectionStarted) {
    // If an activity was selected, switch to range selection if the user moves the mouse
    if (!this._prepareRangeSelectionByMousemove(mousedownEvent, event)) {
      return;
    }
    this._startRangeSelection(mousedownEvent.pageX, mousedownEvent.pageY);
  }

  var lastRow = this._findRow(event.pageY);
  if (lastRow) {
    this.lastRow = lastRow;
  }
  var lastRange = this._findScale(event.pageX);
  if (lastRange) {
    this.lastRange = lastRange;
  }

  this._select(true);
}

_onResizeMouseDown(event) {
  var swap,
    $target = $(event.target);

  this.startRow = this.selectedResources[0];
  this.lastRow = this.selectedResources[this.selectedResources.length - 1];

  // Get the start and last range based on the clicked resize handle. If the ranges cannot be determined it likely means that the selectionRange is out of the current viewRange or dayRange (set by firstHourOfDay, lastHourOfDay)
  this.startRange = scout.nvl(this._findScaleByFromDate(this.selectionRange.from), new Range(this.selectionRange.from.getTime(), this.selectionRange.from.getTime()));
  this.lastRange = scout.nvl(this._findScaleByToDate(this.selectionRange.to), new Range(this.selectionRange.to.getTime(), this.selectionRange.to.getTime()));

  // Swap start and last range if resize-left is clicked
  if ($target.hasClass('selector-resize-left')) {
    swap = this.startRange;
    this.startRange = this.lastRange;
    this.lastRange = swap;
  }

  $target.body().addClass('col-resize');

  this._resizeMousemoveHandler = this._onResizeMousemove.bind(this);
  $target.document()
    .on('mousemove', this._resizeMousemoveHandler)
    .one('mouseup', this._onDocumentMouseUp.bind(this));

  return false;
}

_onResizeMousemove(event) {
  if (!this.rendered) {
    // planner may be removed in the meantime
    return;
  }
  var lastRange = this._findScale(event.pageX);
  if (lastRange) {
    this.lastRange = lastRange;
  }
  this._select(true);
}

_onDocumentMouseUp(event) {
  var $target = $(event.target);
  $target.body().removeClass('col-resize');
  if (this._cellMousemoveHandler) {
    $target.document().off('mousemove', this._documentMousemoveHandler);
    this._cellMousemoveHandler = null;
  }
  if (this._resizeMousemoveHandler) {
    $target.document().off('mousemove', this._resizeMousemoveHandler);
    this._resizeMousemoveHandler = null;
  }
  if (!this._rangeSelectionStarted) {
    // Range selection has not been initiated -> don't call select()
    return;
  }
  this._rangeSelectionStarted = false;
  if (this.rendered) {
    this._select();
  }
}

_select(whileSelecting) {
  if (!this.startRow || !this.lastRow) {
    return;
  }
  var rangeSelected = !!(this.startRange && this.lastRange);
  var $startRow = this.startRow.$resource,
    $lastRow = this.lastRow.$resource;

  // in case of single selection
  if (this.selectionMode === Planner.SelectionMode.SINGLE_RANGE) {
    this.lastRow = this.startRow;
    $lastRow = this.startRow.$resource;
  }

  // select rows
  var $upperRow = ($startRow[0].offsetTop <= $lastRow[0].offsetTop) ? $startRow : $lastRow,
    $lowerRow = ($startRow[0].offsetTop > $lastRow[0].offsetTop) ? $startRow : $lastRow,
    resources = $('.planner-resource', this.$grid).toArray(),
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
  }), !whileSelecting);
  this.selectActivity(null);

  if (rangeSelected) {
    // left and width
    var from = Math.min(this.lastRange.from, this.startRange.from),
      to = Math.max(this.lastRange.to, this.startRange.to);

    var selectionRange = new DateRange(new Date(from), new Date(to));
    selectionRange = this._adjustSelectionRange(selectionRange);
    this.selectRange(selectionRange, !whileSelecting);
  }
}

_adjustSelectionRange(range) {
  var from = range.from.getTime();
  var to = range.to.getTime();

  // Ensure minimum size of selection range (interval is in minutes)
  var minSelectionDuration = 0;
  var options = this.displayModeOptions[this.displayMode];
  if (options.interval > 0 && options.minSelectionIntervalCount > 0) {
    minSelectionDuration = options.minSelectionIntervalCount * options.interval * 60000;
  }
  var lastHourOfDay = options.lastHourOfDay;
  var endOfDay = dates.shiftTime(dates.trunc(range.from), lastHourOfDay + 1);
  var viewRange = this._visibleViewRange();
  if (this.lastRange.from < this.startRange.from) {
    // Selecting to left
    if (to - minSelectionDuration >= viewRange.from.getTime()) {
      // extend to left side
      from = Math.min(from, to - minSelectionDuration);
    } else {
      // extend to right side if from would be smaller than the minimum date (left border)
      from = viewRange.from.getTime();
      to = Math.max(to, Math.min(from + minSelectionDuration, viewRange.to.getTime()));
    }
  } else {
    // Selecting to right
    if (from + minSelectionDuration <= viewRange.to.getTime()) {
      // extend to right side
      to = Math.max(to, Math.max(from + minSelectionDuration, viewRange.from.getTime()));
      if (to >= endOfDay.getTime() && new Range(from, to).size() === minSelectionDuration) {
        // extend to left side when clicking at the end of a day
        to = endOfDay.getTime();
        from = Math.min(from, to - minSelectionDuration);
      }
    } else {
      // extend to left side if to would be greater than the maximum date (right border)
      to = viewRange.to.getTime();
      from = Math.min(from, to - minSelectionDuration);
    }
  }
  return new DateRange(new Date(from), new Date(to));
}

_findRow(y) {
  var $row,
    gridBounds = graphics.offsetBounds(this.$grid),
    x = gridBounds.x + 10;

  y = Math.min(Math.max(y, 0), gridBounds.y + gridBounds.height - 1);
  $row = this.$container.elementFromPoint(x, y, '.planner-resource');
  if ($row.length > 0) {
    return $row.data('resource');
  }
  return null;
}

_findScale(x) {
  var $scaleItem,
    gridBounds = graphics.offsetBounds(this.$grid),
    y = this.$scale.offset().top + this.$scale.height() * 0.75;

  x = Math.min(Math.max(x, 0), gridBounds.x + gridBounds.width - 1);
  $scaleItem = this.$container.elementFromPoint(x, y, '.scale-item');
  if ($scaleItem.length > 0) {
    return new DateRange($scaleItem.data('date-from').valueOf(), $scaleItem.data('date-to').valueOf());
  }
  return null;
}

_findScaleByFromDate(from) {
  return this._findScaleByFunction(function(i, elem) {
    var $scaleItem = $(elem);
    if ($scaleItem.data('date-from').getTime() === from.getTime()) {
      return true;
    }
  });
}

_findScaleByToDate(to) {
  return this._findScaleByFunction(function(i, elem) {
    var $scaleItem = $(elem);
    if ($scaleItem.data('date-to').getTime() === to.getTime()) {
      return true;
    }
  });
}

_findScaleByFunction(func) {
  var $scaleItem = this.$timelineSmall.children('.scale-item').filter(func);
  if (!$scaleItem.length) {
    return null;
  }
  return new DateRange($scaleItem.data('date-from').valueOf(), $scaleItem.data('date-to').valueOf());
}

/**
 * @returns the visible view range (the difference to this.viewRange is that first and last hourOfDay are considered).
 */
_visibleViewRange() {
  var $items = this.$timelineSmall.children('.scale-item');
  return new Range($items.first().data('date-from'), $items.last().data('date-to'));
}

/* -- helper ---------------------------------------------------- */

_dateFormat(date, pattern) {
  var d = new Date(date.valueOf()),
    dateFormat = new DateFormat(this.session.locale, pattern);

  return dateFormat.format(d);
}

_renderViewRange() {
  this._renderRange();
  this._renderScale();
  this.invalidateLayoutTree();
}

_renderHeaderVisible() {
  this._header.setVisible(this.headerVisible);
  this.invalidateLayoutTree();
}

_renderYearPanelVisible(animated) {
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
    duration: animated ? 500 : 0,
    progress: this._onYearPanelWidthChange.bind(this),
    complete: this._afterYearPanelWidthChange.bind(this)
  });
}

_onYearPanelWidthChange() {
  if (!this._yearPanel.$container) {
    // If container has been removed in the meantime (e.g. user navigates away while animation is in progress)
    return;
  }
  var yearPanelWidth = this._yearPanel.$container.outerWidth();
  this.$grid.css('width', 'calc(100% - ' + yearPanelWidth + 'px)');
  this.$scale.css('width', 'calc(100% - ' + yearPanelWidth + 'px)');
  this.revalidateLayout();
}

_afterYearPanelWidthChange() {
  if (!this.yearPanelVisible) {
    this._yearPanel.removeContent();
  }
}

_setMenus(menus) {
  this.updateKeyStrokes(menus, this.menus);
  this._setProperty('menus', menus);
  this._updateMenuBar();
}

_updateMenuBar() {
  var menuItems = this._filterMenus(['Planner.EmptySpace', 'Planner.Resource', 'Planner.Activity', 'Planner.Range'], false, true);
  this.menuBar.setMenuItems(menuItems);
}

_removeMenus() {
  // menubar takes care about removal
}

_filterMenus(allowedTypes, onlyVisible, enableDisableKeyStroke) {
  allowedTypes = allowedTypes || [];
  if (allowedTypes.indexOf('Planner.Resource') > -1 && this.selectedResources.length === 0) {
    arrays.remove(allowedTypes, 'Planner.Resource');
  }
  if (allowedTypes.indexOf('Planner.Activity') > -1 && !this.selectedActivity) {
    arrays.remove(allowedTypes, 'Planner.Activity');
  }
  if (allowedTypes.indexOf('Planner.Range') > -1 && !this.selectionRange.from && !this.selectionRange.to) {
    arrays.remove(allowedTypes, 'Planner.Range');
  }
  return menus_1.filter(this.menus, allowedTypes, onlyVisible, enableDisableKeyStroke);
}

setDisplayModeOptions(displayModeOptions) {
  this.setProperty('displayModeOptions', displayModeOptions);
}

_setDisplayModeOptions(displayModeOptions) {
  if (displayModeOptions) {
    this._adjustHours(displayModeOptions);
  }
  this.displayModeOptions = displayModeOptions;
}

/**
 * Make sure configured our is between 0 and 23.
 */
_adjustHours(optionsMap) {
  objects.values(optionsMap).forEach(function(options) {
    if (options.firstHourOfDay) {
      options.firstHourOfDay = validHour(options.firstHourOfDay);
    }
    if (options.lastHourOfDay) {
      options.lastHourOfDay = validHour(options.lastHourOfDay);
    }
  });

  function validHour(hour) {
    if (hour < 0) {
      return 0;
    }
    if (hour > 23) {
      return 23;
    }
    return hour;
  }
}

_renderDisplayModeOptions() {
  this._renderRange();
  this._renderScale();
  this._select(); // adjust selection if minSelectionIntervalCount has changed
  this.invalidateLayoutTree();
}

_renderAvailableDisplayModes() {
  // done by PlannerHeader.js
}

_renderDisplayMode() {
  // done by PlannerHeader.js
}

_setViewRange(viewRange) {
  viewRange = DateRange.ensure(viewRange);
  this._setProperty('viewRange', viewRange);
  this._yearPanel.setViewRange(this.viewRange);
  this._yearPanel.selectDate(this.viewRange.from);
}

_setDisplayMode(displayMode) {
  this._setProperty('displayMode', displayMode);
  this._yearPanel.setDisplayMode(this.displayMode);
  this._header.setDisplayMode(this.displayMode);
}

_setAvailableDisplayModes(availableDisplayModes) {
  this._setProperty('availableDisplayModes', availableDisplayModes);
  this._header.setAvailableDisplayModes(this.availableDisplayModes);
}

_setSelectionRange(selectionRange) {
  selectionRange = DateRange.ensure(selectionRange);
  this._setProperty('selectionRange', selectionRange);
  this.session.onRequestsDone(this._updateMenuBar.bind(this));
}

_setSelectedResources(selectedResources) {
  if (typeof selectedResources[0] === 'string') {
    selectedResources = this._resourcesByIds(selectedResources);
  }
  if (this.rendered) {
    this._removeSelectedResources();
  }
  this._setProperty('selectedResources', selectedResources);
  this.session.onRequestsDone(this._updateMenuBar.bind(this));
}

_removeSelectedResources() {
  this.selectedResources.forEach(function(resource) {
    resource.$resource.select(false);
  });
}

_renderSelectedResources() {
  this.selectedResources.forEach(function(resource) {
    resource.$resource.select(true);
  });
}

_renderActivitySelectable() {
  if (this.selectedActivity && this.selectedActivity.$activity) {
    this.selectedActivity.$activity.toggleClass('selected', this.activitySelectable);
  }
}

_renderSelectionMode() {
  if (this.selectionMode === Planner.SelectionMode.NONE) {
    if (this.$selector) {
      this.$selector.remove();
      this.$highlight.remove();
    }
  } else {
    this._renderSelectionRange();
  }
}

_renderSelectionRange() {
  var $startRow, $lastRow,
    from = this.selectionRange.from,
    to = this.selectionRange.to,
    startRow = this.selectedResources[0],
    lastRow = this.selectedResources[this.selectedResources.length - 1];

  // remove old selector
  if (this.$selector) {
    this.$selector.remove();
    this.$highlight.remove();
  }

  if (!startRow || !lastRow || !this.selectionRange.from || !this.selectionRange.to) {
    return;
  }
  $startRow = startRow.$resource;
  $lastRow = lastRow.$resource;

  // Make sure selection fits into scale
  from = Math.max(from, this.beginScale);
  to = Math.min(to, this.endScale);

  // top and height
  var $parent = ($startRow[0].offsetTop <= $lastRow[0].offsetTop) ? $startRow : $lastRow;
  this.$selector = $parent.children('.resource-cells').appendDiv('selector');
  this.$selector.cssHeight($startRow.outerHeight() + Math.abs($lastRow[0].offsetTop - $startRow[0].offsetTop));
  var $selectorResizeLeft = this.$selector.appendDiv('selector-resize-left').mousedown(this._onResizeMouseDown.bind(this));
  var $selectorResizeRight = this.$selector.appendDiv('selector-resize-right').mousedown(this._onResizeMouseDown.bind(this));
  this.$selector
    .css('left', 'calc(' + this.transformLeft(from) + '% - ' + $selectorResizeLeft.cssWidth() + 'px)')
    .css('width', 'calc(' + this.transformWidth(from, to) + '% + ' + ($selectorResizeLeft.cssWidth() + $selectorResizeRight.cssWidth()) + 'px)')
    .on('contextmenu', this._onRangeSelectorContextMenu.bind(this));

  // colorize scale
  this.$highlight = this.$timelineSmall.prependDiv('highlight');

  var left = this.$selector.cssLeft() + $selectorResizeLeft.cssWidth() + this.$scaleTitle.cssWidth();
  var width = this.$selector.cssWidth() - ($selectorResizeLeft.cssWidth() + $selectorResizeRight.cssWidth());
  this.$highlight
    .cssLeft(left)
    .cssWidth(width);
}

_setSelectedActivity(selectedActivity) {
  if (typeof selectedActivity === 'string') {
    selectedActivity = this._activityById(selectedActivity);
  }
  if (this.rendered) {
    this._removeSelectedActivity();
  }
  this._setProperty('selectedActivity', selectedActivity);
  this.session.onRequestsDone(this._updateMenuBar.bind(this));
}

_removeSelectedActivity() {
  if (this.selectedActivity && this.selectedActivity.$activity) {
    this.selectedActivity.$activity.removeClass('selected');
  }
}

_renderSelectedActivity() {
  if (this.selectedActivity && this.selectedActivity.$activity) {
    this.selectedActivity.$activity.addClass('selected');
  }
}

_renderLabel() {
  var label = this.label || '';
  if (this.$scaleTitle) {
    this.$scaleTitle.text(label);
  }
}

_resourcesByIds(ids) {
  return ids.map(this._resourceById.bind(this));
}

_activityById(id) {
  return this.activityMap[id];
}

_resourceById(id) {
  return this.resourceMap[id];
}

setDisplayMode(displayMode) {
  this.setProperty('displayMode', displayMode);
  this.startRange = null;
  this.lastRange = null;
}

layoutYearPanel() {
  if (this.yearPanelVisible) {
    scrollbars.update(this._yearPanel.$yearList);
    this._yearPanel._scrollYear();
  }
}

setYearPanelVisible(visible) {
  if (this.yearPanelVisible === visible) {
    return;
  }
  this._setProperty('yearPanelVisible', visible);
  if (this.rendered) {
    this._renderYearPanelVisible(true);
  }
}

setViewRangeFrom(date) {
  var diff = this.viewRange.to.getTime() - this.viewRange.from.getTime(),
    viewRange = new DateRange(this.viewRange.from, this.viewRange.to);

  viewRange.from = date;
  viewRange.to = new Date(date.getTime() + diff);
  this.setViewRange(viewRange);
}

setViewRange(viewRange) {
  if (this.viewRange === viewRange) {
    return;
  }
  this._setViewRange(viewRange);

  if (this.rendered) {
    this._renderViewRange();
    this._rerenderActivities();
    this._renderSelectedActivity();
    this.validateLayoutTree();
  }
}

selectRange(range) {
  if (range && range.equals(this.selectionRange)) {
    return;
  }
  this.setProperty('selectionRange', range);
}

selectActivity(activity) {
  this.setProperty('selectedActivity', activity);
}

selectResources(resources) {
  if (arrays.equals(resources, this.selectedResources)) {
    return;
  }

  resources = arrays.ensure(resources);
  // Make a copy so that original array stays untouched
  resources = resources.slice();
  this.setProperty('selectedResources', resources);
  this.trigger('resourcesSelected', {
    resources: resources
  });

  if (this.rendered) {
    // Render selection range as well for the case if selectedRange does not change but selected resources do
    this._renderSelectionRange();
  }
}

/**
 * Returns true if a deselection happened. False if the given resources were not selected at all.
 */
deselectResources(resources) {
  var deselected = false;
  resources = arrays.ensure(resources);
  var selectedResources = this.selectedResources.slice(); // copy
  if (arrays.removeAll(selectedResources, resources)) {
    this.selectResources(selectedResources);
    deselected = true;
  }
  return deselected;
}

insertResources(resources) {
  // Update model
  resources.forEach(function(resource) {
    this._initResource(resource);
    // Always insert new rows at the end, if the order is wrong a rowOrderChanged event will follow
    this.resources.push(resource);
  }.bind(this));

  // Update HTML
  if (this.rendered) {
    this._renderResources(resources);
    this.invalidateLayoutTree();
  }
}

deleteResources(resources) {
  if (this.deselectResources(resources)) {
    this.selectRange(new DateRange());
  }
  resources.forEach(function(resource) {
    // Update model
    arrays.remove(this.resources, resource);
    delete this.resourceMap[resource.id];

    resource.activities.forEach(function(activity) {
      delete this.activityMap[activity.id];
    }.bind(this));

    // Update HTML
    if (this.rendered) {
      resource.$resource.remove();
      delete resource.$resource;
    }
  }.bind(this));

  this.invalidateLayoutTree();
}

deleteAllResources() {
  // Update HTML
  if (this.rendered) {
    this._removeAllResources();
    this.invalidateLayoutTree();
  }

  // Update model
  this.resources = [];
  this.resourceMap = {};
  this.activityMap = {};
  this.selectResources([]);
  this.selectRange(new DateRange());
}

updateResources(resources) {
  resources.forEach(function(updatedResource) {
    var oldResource = this.resourceMap[updatedResource.id];
    if (!oldResource) {
      throw new Error('Update event received for non existing resource. ResourceId: ' + updatedResource.id);
    }

    // Replace old resource
    this._initResource(updatedResource);
    arrays.replace(this.resources, oldResource, updatedResource);
    arrays.replace(this.selectedResources, oldResource, updatedResource);

    // Replace old $resource
    if (this.rendered && oldResource.$resource) {
      var $updatedResource = $(this._buildResourceHtml(updatedResource));
      oldResource.$resource.replaceWith($updatedResource);
      $updatedResource.css('min-width', oldResource.$resource.css('min-width'));
      this._linkResource($updatedResource, updatedResource);
      this._linkActivitiesForResource(updatedResource);
    }
  }.bind(this));
}
}
