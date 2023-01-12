/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  arrays, CellModel, ContextMenuPopup, DateFormat, DateRange, dates, EnumObject, Event, graphics, HtmlComponent, InitModelOf, JsonDateRange, KeyStrokeContext, Menu, MenuBar, menus as menuUtil, objects, PlannerEventMap, PlannerHeader,
  PlannerHeaderDisplayModeClickEvent, PlannerLayout, PlannerMenuItemsOrder, PlannerModel, Range, scout, scrollbars, strings, styles, tooltips, TooltipSupport, Widget, YearPanel, YearPanelDateSelectEvent
} from '../index';
import $ from 'jquery';

export class Planner extends Widget implements PlannerModel {
  declare model: PlannerModel;
  declare eventMap: PlannerEventMap;
  declare self: Planner;

  activityMap: Record<string, PlannerActivity>;
  activitySelectable: boolean;
  availableDisplayModes: PlannerDisplayMode[];
  displayMode: PlannerDisplayMode;
  displayModeOptions: Record<PlannerDisplayMode, PlannerDisplayModeOptions>;
  headerVisible: boolean;
  label: string;
  resources: PlannerResource[];
  resourceMap: Record<string, PlannerResource>;
  selectionMode: PlannerSelectionMode;
  selectionRange: DateRange;
  selectedResources: PlannerResource[];
  viewRange: DateRange;
  startRange: DateRange;
  lastRange: DateRange;
  beginScale: number;
  endScale: number;
  selectedActivity: PlannerActivity;
  menus: Menu[];
  startRow: PlannerResource;
  lastRow: PlannerResource;
  menuBar: MenuBar;
  header: PlannerHeader;
  yearPanel: YearPanel;

  /** scale calculator */
  transformLeft: (t: number) => number;
  transformWidth: (t0: number, t1: number) => number;
  yearPanelVisible: boolean;
  $range: JQuery;
  $modes: JQuery;
  $selector: JQuery;
  $grid: JQuery;
  $highlight: JQuery;
  $timeline: JQuery;
  $timelineLarge: JQuery;
  $timelineSmall: JQuery;
  $scaleTitle: JQuery;
  $scale: JQuery;

  protected _resourceTitleWidth: number;
  protected _rangeSelectionStarted: boolean;
  protected _tooltipSupport: TooltipSupport;
  protected _gridScrollHandler: (event: JQuery.ScrollEvent<HTMLDivElement>) => void;
  protected _cellMousemoveHandler: (event: JQuery.MouseMoveEvent<Document>) => void;
  protected _resizeMousemoveHandler: (event: JQuery.MouseMoveEvent<Document>) => void;

  constructor() {
    super();

    this.activityMap = {};
    this.activitySelectable = false;
    this.availableDisplayModes = [];
    this.displayMode = null;
    // @ts-expect-error
    this.displayModeOptions = {};
    this.headerVisible = true;
    this.label = null;
    this.resources = [];
    this.resourceMap = {};
    this.selectionMode = Planner.SelectionMode.MULTI_RANGE;
    this.selectionRange = new DateRange();
    this.selectedResources = [];
    this.viewRange = new DateRange();
    this.selectedActivity = null;
    this.startRow = null;
    this.lastRow = null;

    this._resourceTitleWidth = 20;
    this._rangeSelectionStarted = false;

    // main elements
    this.$container = null;
    this.$range = null;
    this.$modes = null;
    this.$grid = null;

    this.transformLeft = t => t;
    this.transformWidth = (t0, t1) => (t1 - t0);

    this.yearPanelVisible = false;
    this._addWidgetProperties(['menus']);
  }

  static Direction = {
    BACKWARD: -1,
    FORWARD: 1
  } as const;

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
  } as const;

  static SelectionMode = {
    NONE: 0,
    SINGLE_RANGE: 1,
    MULTI_RANGE: 2
  } as const;

  static RANGE_SELECTION_MOVE_THRESHOLD = 10;

  static MenuTypes = {
    Activity: 'Planner.Activity',
    EmptySpace: 'Planner.EmptySpace',
    Range: 'Planner.Range',
    Resource: 'Planner.Resource'
  } as const;

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.yearPanel = scout.create(YearPanel, {
      parent: this,
      alwaysSelectFirstDay: true
    });
    this.yearPanel.on('dateSelect', this._onYearPanelDateSelect.bind(this));
    this.header = scout.create(PlannerHeader, {
      parent: this
    });
    this.header.on('todayClick', this._onTodayClick.bind(this));
    this.header.on('yearClick', this._onYearClick.bind(this));
    this.header.on('previousClick', this._onPreviousClick.bind(this));
    this.header.on('nextClick', this._onNextClick.bind(this));
    this.header.on('displayModeClick', this._onDisplayModeClick.bind(this));
    this.menuBar = scout.create(MenuBar, {
      parent: this,
      position: MenuBar.Position.BOTTOM,
      menuOrder: new PlannerMenuItemsOrder(this.session, 'Planner'),
      cssClass: 'bounded'
    });
    for (let i = 0; i < this.resources.length; i++) {
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

  protected _initResource(resource: PlannerResource) {
    resource.activities.forEach(activity => this._initActivity(activity));
    this.resourceMap[resource.id] = resource;
  }

  protected _initActivity(activity: PlannerActivity) {
    activity.beginTime = dates.parseJsonDate(activity.beginTime as string);
    activity.endTime = dates.parseJsonDate(activity.endTime as string);
    this.activityMap[activity.id] = activity;
  }

  protected override _render() {
    // basics, layout etc.
    this.$container = this.$parent.appendDiv('planner');
    let layout = new PlannerLayout(this);
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(layout);

    // main elements
    this.header.render();
    this.yearPanel.render();
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
      text: function($comp: JQuery) {
        let activity = this.activityById($comp.attr('data-id'));
        if (activity) {
          return activity.tooltipText;
        }
        return undefined;
      }.bind(this)
    });

    this._installScrollbars();
    this._gridScrollHandler = this._onGridScroll.bind(this);
    this.$grid.on('scroll', this._gridScrollHandler);
  }

  protected override _renderProperties() {
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

  override get$Scrollable(): JQuery {
    return this.$grid;
  }

  /* -- basics, events -------------------------------------------- */

  protected _onPreviousClick(event: Event<PlannerHeader>) {
    this._navigateDate(Planner.Direction.BACKWARD);
  }

  protected _onNextClick(event: Event<PlannerHeader>) {
    this._navigateDate(Planner.Direction.FORWARD);
  }

  protected _navigateDate(direction: PlannerDirection) {
    let viewRange = new DateRange(this.viewRange.from, this.viewRange.to),
      displayMode = Planner.DisplayMode;

    if (this.displayMode === displayMode.DAY) {
      viewRange.from = dates.shift(this.viewRange.from, 0, 0, direction);
      viewRange.to = dates.shift(this.viewRange.to, 0, 0, direction);
    } else if (scout.isOneOf(this.displayMode, displayMode.WEEK, displayMode.WORK_WEEK)) {
      viewRange.from = dates.shift(this.viewRange.from, 0, 0, direction * 7);
      viewRange.from = dates.ensureMonday(viewRange.from, -1 * direction);
      viewRange.to = dates.shift(this.viewRange.to, 0, 0, direction * 7);
    } else if (scout.isOneOf(this.displayMode, displayMode.MONTH, displayMode.CALENDAR_WEEK)) {
      viewRange.from = dates.shift(this.viewRange.from, 0, direction, 0);
      viewRange.from = dates.ensureMonday(viewRange.from, -1 * direction);
      viewRange.to = dates.shift(this.viewRange.to, 0, direction, 0);
    } else if (this.displayMode === displayMode.YEAR) {
      viewRange.from = dates.shift(this.viewRange.from, 0, 3 * direction, 0);
      viewRange.to = dates.shift(this.viewRange.to, 0, 3 * direction, 0);
    }

    this.setViewRange(viewRange);
  }

  protected _onTodayClick(event: Event<PlannerHeader>) {
    let today = this._today(),
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
      today = new Date(year, month, date - day); // set day to Monday
    }

    this.setViewRangeFrom(today);
  }

  protected _today(): Date {
    return new Date();
  }

  protected _onDisplayModeClick(event: PlannerHeaderDisplayModeClickEvent) {
    let displayMode = event.displayMode;
    this.setDisplayMode(displayMode);
  }

  protected _onYearClick(event: Event<PlannerHeader>) {
    this.setYearPanelVisible(!this.yearPanelVisible);
  }

  protected _onYearPanelDateSelect(event: YearPanelDateSelectEvent) {
    this.setViewRangeFrom(event.date);
  }

  protected _onResourceTitleMouseDown(event: JQuery.MouseDownEvent<HTMLDivElement>) {
    let $resource = $(event.target).parent();
    if ($resource.isSelected()) {
      if (event.which === 3 || event.which === 1 && event.ctrlKey) {
        // Right click on an already selected resource must not clear the selection -> context menu will be opened
        return;
      }
    }
    this.startRow = $resource.data('resource') as PlannerResource;
    this.lastRow = this.startRow;
    this._select();
  }

  protected _onResourceTitleContextMenu(event: JQuery.ContextMenuEvent) {
    this._showContextMenu(event, Planner.MenuTypes.Resource);
  }

  protected _onRangeSelectorContextMenu(event: JQuery.ContextMenuEvent) {
    this._showContextMenu(event, Planner.MenuTypes.Range);
  }

  protected _onActivityContextMenu(event: JQuery.ContextMenuEvent) {
    this._showContextMenu(event, Planner.MenuTypes.Activity);
  }

  protected _showContextMenu(event: JQuery.ContextMenuEvent, allowedType: string) {
    event.preventDefault();
    event.stopPropagation();
    let func = function func(event: JQuery.ContextMenuEvent, allowedType: string) {
      if (!this.rendered || !this.attached) { // check needed because function is called asynchronously
        return;
      }
      let filteredMenus: Menu[] = this._filterMenus([allowedType], true);
      let $part = $(event.currentTarget);
      if (filteredMenus.length === 0) {
        return; // at least one menu item must be visible
      }
      let popup = scout.create(ContextMenuPopup, {
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

  protected _onGridScroll(event: JQuery.ScrollEvent<HTMLDivElement>) {
    this._reconcileScrollPos();
  }

  protected _reconcileScrollPos() {
    // When scrolling horizontally scroll scale as well
    let scrollLeft = this.$grid.scrollLeft();
    this.$scale.scrollLeft(scrollLeft);
  }

  protected _renderRange() {
    if (!this.viewRange.from || !this.viewRange.to) {
      return;
    }
    let text: string,
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
    $('.planner-select', this.header.$range).text(text);
  }

  protected _renderScale() {
    if (!this.viewRange.from || !this.viewRange.to) {
      return;
    }
    let that = this,
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
    let $smallScaleItems = this.$timelineSmall.children('.scale-item');
    let $largeScaleItems = this.$timelineLarge.children('.scale-item');
    let width = 100 / $smallScaleItems.length;
    $largeScaleItems.each(function() {
      let $scaleItem = $(this);
      let $largeGridLine = that.$grid.prependDiv('planner-large-scale-item-line');
      $scaleItem.css('width', $scaleItem.data('count') * width + '%')
        .data('scale-item-line', $largeGridLine);
      $scaleItem.prependDiv('planner-large-scale-item-line')
        .css('left', 0);
    });
    $smallScaleItems.each(function(index) {
      let $scaleItem = $(this);
      $scaleItem.css('width', width + '%');
      if (!$scaleItem.data('first')) {
        let $smallGridLine = that.$grid.prependDiv('planner-small-scale-item-line');
        $scaleItem.data('scale-item-line', $smallGridLine);
        $scaleItem.prependDiv('planner-small-scale-item-line')
          .css('left', 0);
      }
    });

    // find transfer function
    let dateFrom = this.$timelineSmall.children().first().data('date-from') as Date;
    this.beginScale = dateFrom.valueOf();
    let dateTo = this.$timelineSmall.children().last().data('date-to') as Date;
    this.endScale = dateTo.valueOf();

    if (scout.isOneOf(this.displayMode, displayMode.WORK_WEEK, displayMode.WEEK)) {
      let options = this.displayModeOptions[this.displayMode];
      let interval = options.interval;
      let firstHourOfDay = options.firstHourOfDay;
      let lastHourOfDay = options.lastHourOfDay;

      this.transformLeft = ((begin: Date, end: Date, firstHour: number, lastHour: number, interval: number) => t => {
        let newDate = new Date(t);
        begin = new Date(begin);
        end = new Date(end);
        let fullRangeMillis = end.valueOf() - begin.valueOf();
        // remove day component from range for scaling
        let dayDiffTBegin = dates.compareDays(newDate, begin);
        let dayDIffEndBegin = dates.compareDays(end, begin);
        let dayComponentMillis = dayDiffTBegin * 3600000 * 24;
        let rangeScaling = (24 / (lastHour - firstHour + 1));
        // re-add day component
        let dayOffset = dayDiffTBegin / dayDIffEndBegin;

        if (newDate.getHours() < firstHour) {
          // If newDate is in the morning before the first hour of day, return 00:00 of this day
          return (rangeScaling / fullRangeMillis + dayOffset) * 100;
        }
        if (newDate.getHours() > lastHour) {
          // If newDate is in the evening after the last hour of day, return 00:00 of next day
          dayOffset = (dayDiffTBegin + 1) / dayDIffEndBegin;
          return (rangeScaling / fullRangeMillis + dayOffset) * 100;
        }

        return ((newDate.valueOf() - (begin.valueOf() + firstHour * 3600000) - dayComponentMillis) * rangeScaling / fullRangeMillis + dayOffset) * 100;
      })(this.viewRange.from, this.viewRange.to, firstHourOfDay, lastHourOfDay, interval);

      this.transformWidth = ((begin: Date, end: Date, firstHour: number, lastHour: number, interval: number) => (function(t0, t1) {
        let fullRangeMillis = end.valueOf() - begin.valueOf();
        let selectionRange = new Range(t0, t1);
        let hiddenRanges = this._findHiddenRangesInWeekMode();
        let selectedRangeMillis = selectionRange.subtractAll(hiddenRanges)
          .reduce((acc, range) => acc + range.size(), 0);
        let rangeScaling = (24 / (lastHour - firstHour + 1));
        return (selectedRangeMillis * rangeScaling) / fullRangeMillis * 100;
      }))(this.viewRange.from, this.viewRange.to, firstHourOfDay, lastHourOfDay, interval);
    } else {
      this.transformLeft = ((begin, end) => t => (t - begin) / (end - begin) * 100)(this.beginScale, this.endScale);
      this.transformWidth = ((begin, end) => (t0, t1) => (t1 - t0) / (end - begin) * 100)(this.beginScale, this.endScale);
    }
  }

  /**
   * Returns every hidden range of the view range created by first and last our of day.
   */
  protected _findHiddenRangesInWeekMode(): Range[] {
    if (!scout.isOneOf(this.displayMode, Planner.DisplayMode.WORK_WEEK, Planner.DisplayMode.WEEK)) {
      return [];
    }
    let ranges: Range[] = [];
    let options = this.displayModeOptions[this.displayMode];
    let firstHourOfDay = options.firstHourOfDay;
    let lastHourOfDay = options.lastHourOfDay;
    let currentDate = new Date(this.viewRange.from.valueOf());
    while (currentDate < this.viewRange.to) {
      // Start of day range
      let hiddenRange = new Range(new Date(currentDate.valueOf()).valueOf(), dates.shiftTime(currentDate, firstHourOfDay).valueOf());
      if (hiddenRange.size() > 0) {
        ranges.push(hiddenRange);
      }
      // End of day range
      hiddenRange = new Range(dates.shiftTime(currentDate, lastHourOfDay + 1).valueOf(), dates.shiftTime(currentDate, 24).valueOf());
      if (hiddenRange.size() > 0) {
        ranges.push(hiddenRange);
      }
      currentDate.setHours(0);
      currentDate.setMinutes(0);
      currentDate.setDate(currentDate.getDate() + 1);
    }
    return ranges;
  }

  protected _renderDayScale() {
    let newLargeGroup: boolean, $divLarge: JQuery, $divSmall: JQuery, first = true;
    let loop = new Date(this.viewRange.from.valueOf());
    let options = this.displayModeOptions[this.displayMode];
    let interval = options.interval;
    let labelPeriod = options.labelPeriod;
    let firstHourOfDay = options.firstHourOfDay;
    let lastHourOfDay = options.lastHourOfDay;

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

  protected _renderWeekScale() {
    let newLargeGroup: boolean, $divLarge: JQuery, $divSmall: JQuery, first = true;
    let loop = new Date(this.viewRange.from.valueOf());
    let options = this.displayModeOptions[this.displayMode];
    let interval = options.interval;
    let labelPeriod = options.labelPeriod;
    let firstHourOfDay = options.firstHourOfDay;
    let lastHourOfDay = options.lastHourOfDay;

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

  protected _renderMonthScale() {
    let newLargeGroup: boolean, $divLarge: JQuery, $divSmall: JQuery, first = true;
    let loop = new Date(this.viewRange.from.valueOf());
    let options = this.displayModeOptions[this.displayMode];
    let labelPeriod = options.labelPeriod;

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

  protected _renderCalendarWeekScale() {
    let newLargeGroup: boolean, $divLarge: JQuery, $divSmall: JQuery, first: boolean | number = true;
    let loop = new Date(this.viewRange.from.valueOf());
    let options = this.displayModeOptions[this.displayMode];
    let labelPeriod = options.labelPeriod;

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
        .appendDiv('scale-item', dates.weekInYear(loop) + '')
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

  protected _renderYearScale() {
    let newLargeGroup: boolean, $divLarge: JQuery, $divSmall: JQuery, first = true;
    let loop = new Date(this.viewRange.from.valueOf());
    let options = this.displayModeOptions[this.displayMode];
    let labelPeriod = options.labelPeriod;

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

  protected _incrementTimelineScaleItems($largeComp: JQuery, $smallComp: JQuery, newDate: Date, newLargeGroup: boolean) {
    $largeComp.data('count', $largeComp.data('count') + 1);
    $smallComp.data('date-to', new Date(newDate.valueOf()))
      .data('first', newLargeGroup);
  }

  /* -- scale events --------------------------------------------------- */

  protected _scaleTooltipText($scale: JQuery): string {
    let toText = ' ' + this.session.text('ui.to') + ' ',
      from = new Date($scale.data('date-from').valueOf()),
      to = new Date($scale.data('date-to').valueOf() - 1);

    if (from.getMonth() === to.getMonth()) {
      return this._dateFormat(from, 'd.') + toText + this._dateFormat(to, 'd. MMMM yyyy');
    } else if (from.getFullYear() === to.getFullYear()) {
      return this._dateFormat(from, 'd. MMMM') + toText + this._dateFormat(to, 'd. MMMM yyyy');
    }
    return this._dateFormat(from, 'd. MMMM yyyy') + toText + this._dateFormat(to, 'd. MMMM yyyy');
  }

  /* --  render resources, activities --------------------------------- */

  protected _removeAllResources() {
    this.resources.forEach(resource => resource.$resource.remove());
  }

  protected _renderResources(resources?: PlannerResource[]) {
    let resource: PlannerResource, resourcesHtml = '';

    resources = resources || this.resources;
    for (let i = 0; i < resources.length; i++) {
      resource = resources[i];
      resourcesHtml += this._buildResourceHtml(resource);
    }

    // Append resources to grid
    $(resourcesHtml).appendTo(this.$grid);

    // Match resources
    this.$grid.children('.planner-resource').each((index, element) => {
      let $element = $(element);
      resource = this.resourceById($element.attr('data-id'));
      this._linkResource($element, resource);
      this._linkActivitiesForResource(resource);
    });
  }

  protected _linkResource($resource: JQuery, resource: PlannerResource) {
    $resource.data('resource', resource);
    resource.$resource = $resource;
    resource.$cells = $resource.children('.resource-cells');
  }

  protected _linkActivity($activity: JQuery, activity: PlannerActivity) {
    $activity.data('activity', activity);
    activity.$activity = $activity;
  }

  protected _rerenderActivities(resources?: PlannerResource[]) {
    resources = resources || this.resources;
    resources.forEach(resource => {
      this._removeActivitiesForResource(resource);
      this._renderActivitiesForResource(resource);
    });
  }

  protected _buildResourceHtml(resource: PlannerResource): string {
    let resourceHtml = '<div class="planner-resource" data-id="' + resource.id + '">';
    resourceHtml += '<div class="resource-title">' + strings.encode(resource.resourceCell.text || '') + '</div>';
    resourceHtml += '<div class="resource-cells">' + this._buildActivitiesHtml(resource) + '</div>';
    resourceHtml += '</div>';
    return resourceHtml;
  }

  protected _renderActivitiesForResource(resource: PlannerResource) {
    resource.$cells.html(this._buildActivitiesHtml(resource));
    this._linkActivitiesForResource(resource);
  }

  protected _linkActivitiesForResource(resource: PlannerResource) {
    resource.$cells.children('.planner-activity').each((index, element) => {
      let $element = $(element);
      let activity = this.activityById($element.attr('data-id'));
      this._linkActivity($element, activity);
    });
  }

  protected _buildActivitiesHtml(resource: PlannerResource): string {
    let activitiesHtml = '';
    resource.activities.forEach(activity => {
      if (activity.beginTime.valueOf() >= this.endScale || activity.endTime.valueOf() <= this.beginScale) {
        // don't add activities which are not in the view range
        return;
      }
      activitiesHtml += this._buildActivityHtml(activity);
    });
    return activitiesHtml;
  }

  protected _removeActivitiesForResource(resource: PlannerResource) {
    resource.activities.forEach(activity => {
      if (activity.$activity) {
        activity.$activity.remove();
        activity.$activity = null;
      }
    });
  }

  protected _buildActivityHtml(activity: PlannerActivity): string {
    let level = 100 - Math.min(activity.level * 100, 100),
      backgroundColor = styles.modelToCssColor(activity.backgroundColor),
      foregroundColor = styles.modelToCssColor(activity.foregroundColor),
      levelColor = styles.modelToCssColor(activity.levelColor),
      beginTime = activity.beginTime as Date,
      endTime = activity.endTime as Date,
      begin = beginTime.valueOf(),
      end = endTime.valueOf();

    // Make sure activity fits into scale
    begin = Math.max(begin, this.beginScale);
    end = Math.min(end, this.endScale);

    let activityCssClass = 'planner-activity' + (activity.cssClass ? (' ' + activity.cssClass) : '');
    let activityStyle = 'left: ' + 'calc(' + this.transformLeft(begin) + '% + 2px);';
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
    let activityLevelCssClass = 'planner-activity-level' + (activity.cssClass ? (' ' + activity.cssClass) : '');
    let activityEmptyColor = styles.get(activityLevelCssClass, 'background-color').backgroundColor;
    activityStyle += ' background-image: ' + 'linear-gradient(to bottom, ' + activityEmptyColor + ' 0%, ' + activityEmptyColor + ' ' + level + '%, transparent ' + level + '%, transparent 100% );';

    let activityHtml = '<div';
    activityHtml += ' class="' + activityCssClass + '"';
    activityHtml += ' style="' + activityStyle + '"';
    activityHtml += ' data-id="' + activity.id + '"';
    activityHtml += '>' + strings.encode(activity.text || '') + '</div>';
    return activityHtml;
  }

  /* -- selector -------------------------------------------------- */

  protected _onCellMouseDown(event: JQuery.MouseDownEvent<HTMLDivElement>) {
    let $activity,
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

  protected _startRangeSelection(pageX: number, pageY: number) {
    // init selector
    this.startRow = this._findRow(pageY);
    this.lastRow = this.startRow;

    // find range on scale
    this.startRange = this._findScale(pageX);
    this.lastRange = this.startRange;

    // draw
    this._select();
    this._rangeSelectionStarted = true;
  }

  /**
   * @returns true if the range selection may be started, false if not
   */
  protected _prepareRangeSelectionByMousemove(mousedownEvent: JQuery.MouseDownEvent<HTMLDivElement>, mousemoveEvent: JQuery.MouseMoveEvent<Document>): boolean {
    let moveX = mousedownEvent.pageX - mousemoveEvent.pageX;
    let moveY = mousedownEvent.pageY - mousemoveEvent.pageY;
    let moveThreshold = Planner.RANGE_SELECTION_MOVE_THRESHOLD;
    if (Math.abs(moveX) >= moveThreshold) {
      // Accept if x movement is big enough
      return true;
    }
    let mousedownRow = this._findRow(mousedownEvent.pageY);
    let mousemoveRow = this._findRow(mousemoveEvent.pageY);
    // Accept if y movement is big enough AND the row changed. No need to switch into range selection mode if cursor is still on the same row
    return Math.abs(moveY) >= moveThreshold && this.selectionMode === Planner.SelectionMode.MULTI_RANGE && mousedownRow !== mousemoveRow;
  }

  protected _onCellMousemove(mousedownEvent: JQuery.MouseDownEvent<HTMLDivElement>, event: JQuery.MouseMoveEvent<Document>) {
    if (this.selectedActivity && !this._rangeSelectionStarted) {
      // If an activity was selected, switch to range selection if the user moves the mouse
      if (!this._prepareRangeSelectionByMousemove(mousedownEvent, event)) {
        return;
      }
      this._startRangeSelection(mousedownEvent.pageX, mousedownEvent.pageY);
    }

    let lastRow = this._findRow(event.pageY);
    if (lastRow) {
      this.lastRow = lastRow;
    }
    let lastRange = this._findScale(event.pageX);
    if (lastRange) {
      this.lastRange = lastRange;
    }

    this._select();
  }

  protected _onResizeMouseDown(event: JQuery.MouseDownEvent): boolean {
    let swap: DateRange,
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

  protected _onResizeMousemove(event: JQuery.MouseMoveEvent) {
    if (!this.rendered) {
      // planner may be removed in the meantime
      return;
    }
    let lastRange = this._findScale(event.pageX);
    if (lastRange) {
      this.lastRange = lastRange;
    }
    this._select();
  }

  protected _onDocumentMouseUp(event: JQuery.MouseUpEvent<Document>) {
    let $target = $(event.target);
    $target.body().removeClass('col-resize');
    if (this._cellMousemoveHandler) {
      $target.document().off('mousemove', this._cellMousemoveHandler);
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

  protected _select() {
    if (!this.startRow || !this.lastRow) {
      return;
    }
    let rangeSelected = !!(this.startRange && this.lastRange);
    let $startRow = this.startRow.$resource,
      $lastRow = this.lastRow.$resource;

    // in case of single selection
    if (this.selectionMode === Planner.SelectionMode.SINGLE_RANGE) {
      this.lastRow = this.startRow;
      $lastRow = this.startRow.$resource;
    }

    // select rows
    let $upperRow = ($startRow[0].offsetTop <= $lastRow[0].offsetTop) ? $startRow : $lastRow,
      $lowerRow = ($startRow[0].offsetTop > $lastRow[0].offsetTop) ? $startRow : $lastRow,
      resources = $('.planner-resource', this.$grid).toArray(),
      top = $upperRow[0].offsetTop,
      low = $lowerRow[0].offsetTop;

    for (let r = resources.length - 1; r >= 0; r--) {
      let row = resources[r];
      if ((row.offsetTop < top && row.offsetTop < low) || (row.offsetTop > top && row.offsetTop > low)) {
        resources.splice(r, 1);
      }
    }

    this.selectResources(resources.map(i => $(i).data('resource')));
    this.selectActivity(null);

    if (rangeSelected) {
      // left and width
      let from = Math.min(this.lastRange.from.valueOf(), this.startRange.from.valueOf()),
        to = Math.max(this.lastRange.to.valueOf(), this.startRange.to.valueOf());

      let selectionRange = new DateRange(new Date(from), new Date(to));
      selectionRange = this._adjustSelectionRange(selectionRange);
      this.selectRange(selectionRange);
    }
  }

  protected _adjustSelectionRange(range: DateRange): DateRange {
    let from = range.from.getTime();
    let to = range.to.getTime();

    // Ensure minimum size of selection range (interval is in minutes)
    let minSelectionDuration = 0;
    let options = this.displayModeOptions[this.displayMode];
    if (options.interval > 0 && options.minSelectionIntervalCount > 0) {
      minSelectionDuration = options.minSelectionIntervalCount * options.interval * 60000;
    }
    let lastHourOfDay = options.lastHourOfDay;
    let endOfDay = dates.shiftTime(dates.trunc(range.from), lastHourOfDay + 1);
    let viewRange = this._visibleViewRange();
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

  protected _findRow(y: number): PlannerResource {
    let $row,
      gridBounds = graphics.offsetBounds(this.$grid),
      x = gridBounds.x + 10;

    y = Math.min(Math.max(y, 0), gridBounds.y + gridBounds.height - 1);
    $row = this.$container.elementFromPoint(x, y, '.planner-resource');
    if ($row.length > 0) {
      return $row.data('resource');
    }
    return null;
  }

  protected _findScale(x: number): DateRange {
    let $scaleItem,
      gridBounds = graphics.offsetBounds(this.$grid),
      y = this.$scale.offset().top + this.$scale.height() * 0.75;

    x = Math.min(Math.max(x, 0), gridBounds.x + gridBounds.width - 1);
    $scaleItem = this.$container.elementFromPoint(x, y, '.scale-item');
    if ($scaleItem.length > 0) {
      return new DateRange($scaleItem.data('date-from').valueOf(), $scaleItem.data('date-to').valueOf());
    }
    return null;
  }

  protected _findScaleByFromDate(from: Date): DateRange {
    return this._findScaleByFunction((i, elem) => {
      let $scaleItem = $(elem);
      if ($scaleItem.data('date-from').getTime() === from.getTime()) {
        return true;
      }
    });
  }

  protected _findScaleByToDate(to: Date): DateRange {
    return this._findScaleByFunction((i, elem) => {
      let $scaleItem = $(elem);
      if ($scaleItem.data('date-to').getTime() === to.getTime()) {
        return true;
      }
    });
  }

  protected _findScaleByFunction(func: (index: number, element: HTMLElement) => boolean): DateRange {
    let $scaleItem = this.$timelineSmall.children('.scale-item').filter(func);
    if (!$scaleItem.length) {
      return null;
    }
    return new DateRange($scaleItem.data('date-from').valueOf(), $scaleItem.data('date-to').valueOf());
  }

  /**
   * @returns the visible view range (the difference to this.viewRange is that first and last hourOfDay are considered).
   */
  protected _visibleViewRange(): DateRange {
    let $items = this.$timelineSmall.children('.scale-item');
    return new DateRange($items.first().data('date-from'), $items.last().data('date-to'));
  }

  /* -- helper ---------------------------------------------------- */

  protected _dateFormat(date: Date, pattern: string): string {
    let d = new Date(date.valueOf()),
      dateFormat = new DateFormat(this.session.locale, pattern);

    return dateFormat.format(d);
  }

  protected _renderViewRange() {
    this._renderRange();
    this._renderScale();
    this.invalidateLayoutTree();
  }

  protected _renderHeaderVisible() {
    this.header.setVisible(this.headerVisible);
    this.invalidateLayoutTree();
  }

  protected _renderYearPanelVisible(animated: boolean) {
    let yearPanelWidth;
    if (this.yearPanelVisible) {
      this.yearPanel.renderContent();
    }

    // show or hide year panel
    $('.calendar-toggle-year', this.$modes).select(this.yearPanelVisible);
    if (this.yearPanelVisible) {
      yearPanelWidth = 210;
    } else {
      yearPanelWidth = 0;
    }
    this.yearPanel.$container.animate({
      width: yearPanelWidth
    }, {
      duration: animated ? 500 : 0,
      progress: this._onYearPanelWidthChange.bind(this),
      complete: this._afterYearPanelWidthChange.bind(this)
    });
  }

  protected _onYearPanelWidthChange() {
    if (!this.yearPanel.$container) {
      // If container has been removed in the meantime (e.g. user navigates away while animation is in progress)
      return;
    }
    let yearPanelWidth = this.yearPanel.$container.outerWidth();
    this.$grid.css('width', 'calc(100% - ' + yearPanelWidth + 'px)');
    this.$scale.css('width', 'calc(100% - ' + yearPanelWidth + 'px)');
    this.revalidateLayout();
  }

  protected _afterYearPanelWidthChange() {
    if (!this.yearPanelVisible) {
      this.yearPanel.removeContent();
    }
  }

  protected _setMenus(menus: Menu[]) {
    this.updateKeyStrokes(menus, this.menus);
    this._setProperty('menus', menus);
    this._updateMenuBar();
  }

  protected _updateMenuBar() {
    let menuItems = this._filterMenus([Planner.MenuTypes.EmptySpace, Planner.MenuTypes.Resource, Planner.MenuTypes.Activity, Planner.MenuTypes.Range], false, true);
    this.menuBar.setMenuItems(menuItems);
  }

  protected _filterMenus(allowedTypes: string[], onlyVisible: boolean, enableDisableKeyStrokes?: boolean): Menu[] {
    allowedTypes = allowedTypes || [];
    if (allowedTypes.indexOf(Planner.MenuTypes.Resource) > -1 && this.selectedResources.length === 0) {
      arrays.remove(allowedTypes, Planner.MenuTypes.Resource);
    }
    if (allowedTypes.indexOf(Planner.MenuTypes.Activity) > -1 && !this.selectedActivity) {
      arrays.remove(allowedTypes, Planner.MenuTypes.Activity);
    }
    if (allowedTypes.indexOf(Planner.MenuTypes.Range) > -1 && !this.selectionRange.from && !this.selectionRange.to) {
      arrays.remove(allowedTypes, Planner.MenuTypes.Range);
    }
    return menuUtil.filter(this.menus, allowedTypes, {onlyVisible, enableDisableKeyStrokes});
  }

  setDisplayModeOptions(
    displayModeOptions: Partial<Record<PlannerDisplayMode, PlannerDisplayModeOptions>>
  ) {
    this.setProperty('displayModeOptions', displayModeOptions);
  }

  protected _setDisplayModeOptions(displayModeOptions: Record<PlannerDisplayMode, PlannerDisplayModeOptions>) {
    if (displayModeOptions) {
      this._adjustHours(displayModeOptions);
    }
    this.displayModeOptions = displayModeOptions;
  }

  /**
   * Make sure configured our is between 0 and 23.
   */
  protected _adjustHours(optionsMap: Record<PlannerDisplayMode, PlannerDisplayModeOptions>) {
    objects.values(optionsMap).forEach((options: PlannerDisplayModeOptions) => {
      if (options.firstHourOfDay) {
        options.firstHourOfDay = validHour(options.firstHourOfDay);
      }
      if (options.lastHourOfDay) {
        options.lastHourOfDay = validHour(options.lastHourOfDay);
      }
    });

    function validHour(hour: number): number {
      if (hour < 0) {
        return 0;
      }
      if (hour > 23) {
        return 23;
      }
      return hour;
    }
  }

  protected _renderDisplayModeOptions() {
    this._renderRange();
    this._renderScale();
    this._rerenderActivities(); // required in case first/lastHourOfDay changes
    this._select(); // adjust selection if minSelectionIntervalCount has changed
    this.invalidateLayoutTree();
  }

  protected _renderAvailableDisplayModes() {
    // done by PlannerHeader.js
  }

  protected _renderDisplayMode() {
    // done by PlannerHeader.js
  }

  protected _setViewRange(viewRange: DateRange | JsonDateRange) {
    viewRange = DateRange.ensure(viewRange);
    this._setProperty('viewRange', viewRange);
    this.yearPanel.setViewRange(this.viewRange);
    this.yearPanel.selectDate(this.viewRange.from);
  }

  protected _setDisplayMode(displayMode: PlannerDisplayMode) {
    this._setProperty('displayMode', displayMode);
    this.yearPanel.setDisplayMode(this.displayMode);
    this.header.setDisplayMode(this.displayMode);
  }

  protected _setAvailableDisplayModes(availableDisplayModes: PlannerDisplayMode[]) {
    this._setProperty('availableDisplayModes', availableDisplayModes);
    this.header.setAvailableDisplayModes(this.availableDisplayModes);
  }

  protected _setSelectionRange(selectionRange: DateRange | JsonDateRange) {
    selectionRange = DateRange.ensure(selectionRange);
    this._setProperty('selectionRange', selectionRange);
    this.session.onRequestsDone(this._updateMenuBar.bind(this));
  }

  protected _setSelectedResources(selectedResources: PlannerResource[] | string[]) {
    if (typeof selectedResources[0] === 'string') {
      selectedResources = this.resourcesByIds(selectedResources as string[]);
    }
    if (this.rendered) {
      this._removeSelectedResources();
    }
    this._setProperty('selectedResources', selectedResources);
    this.session.onRequestsDone(this._updateMenuBar.bind(this));
  }

  protected _removeSelectedResources() {
    this.selectedResources.forEach(resource => resource.$resource.select(false));
  }

  protected _renderSelectedResources() {
    this.selectedResources.forEach(resource => resource.$resource.select(true));
  }

  protected _renderActivitySelectable() {
    if (this.selectedActivity && this.selectedActivity.$activity) {
      this.selectedActivity.$activity.toggleClass('selected', this.activitySelectable);
    }
  }

  protected _renderSelectionMode() {
    if (this.selectionMode === Planner.SelectionMode.NONE) {
      if (this.$selector) {
        this.$selector.remove();
        this.$highlight.remove();
      }
    } else {
      this._renderSelectionRange();
    }
  }

  protected _renderSelectionRange() {
    let fromDate = this.selectionRange.from,
      toDate = this.selectionRange.to,
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
    let $startRow = startRow.$resource;
    let $lastRow = lastRow.$resource;

    // Make sure selection fits into scale
    let from = Math.max(fromDate.valueOf(), this.beginScale);
    let to = Math.min(toDate.valueOf(), this.endScale);

    // top and height
    let $parent = ($startRow[0].offsetTop <= $lastRow[0].offsetTop) ? $startRow : $lastRow;
    this.$selector = $parent.children('.resource-cells').appendDiv('selector');
    this.$selector.cssHeight($startRow.outerHeight() + Math.abs($lastRow[0].offsetTop - $startRow[0].offsetTop));
    let $selectorResizeLeft = this.$selector.appendDiv('selector-resize-left').on('mousedown', this._onResizeMouseDown.bind(this));
    let $selectorResizeRight = this.$selector.appendDiv('selector-resize-right').on('mousedown', this._onResizeMouseDown.bind(this));
    this.$selector
      .css('left', 'calc(' + this.transformLeft(from) + '% - ' + $selectorResizeLeft.cssWidth() + 'px)')
      .css('width', 'calc(' + this.transformWidth(from, to) + '% + ' + ($selectorResizeLeft.cssWidth() + $selectorResizeRight.cssWidth()) + 'px)')
      .on('contextmenu', this._onRangeSelectorContextMenu.bind(this));

    // colorize scale
    this.$highlight = this.$timelineSmall.prependDiv('highlight');

    let left = this.$selector.cssLeft() + $selectorResizeLeft.cssWidth() + this.$scaleTitle.cssWidth();
    let width = this.$selector.cssWidth() - ($selectorResizeLeft.cssWidth() + $selectorResizeRight.cssWidth());
    this.$highlight
      .cssLeft(left)
      .cssWidth(width);
  }

  protected _setSelectedActivity(selectedActivity: PlannerActivity | string) {
    if (typeof selectedActivity === 'string') {
      selectedActivity = this.activityById(selectedActivity);
    }
    if (this.rendered) {
      this._removeSelectedActivity();
    }
    this._setProperty('selectedActivity', selectedActivity);
    this.session.onRequestsDone(this._updateMenuBar.bind(this));
  }

  protected _removeSelectedActivity() {
    if (this.selectedActivity && this.selectedActivity.$activity) {
      this.selectedActivity.$activity.removeClass('selected');
    }
  }

  protected _renderSelectedActivity() {
    if (this.selectedActivity && this.selectedActivity.$activity) {
      this.selectedActivity.$activity.addClass('selected');
    }
  }

  protected _renderLabel() {
    let label = this.label || '';
    if (this.$scaleTitle) {
      this.$scaleTitle.text(label);
    }
  }

  resourcesByIds(ids: string[]): PlannerResource[] {
    return ids.map(this.resourceById.bind(this));
  }

  activityById(id: string): PlannerActivity {
    return this.activityMap[id];
  }

  resourceById(id: string): PlannerResource {
    return this.resourceMap[id];
  }

  setDisplayMode(displayMode: PlannerDisplayMode) {
    this.setProperty('displayMode', displayMode);
    this.startRange = null;
    this.lastRange = null;
  }

  layoutYearPanel() {
    if (this.yearPanelVisible) {
      scrollbars.update(this.yearPanel.$yearList);
      this.yearPanel._scrollYear();
    }
  }

  setYearPanelVisible(visible: boolean) {
    if (this.yearPanelVisible === visible) {
      return;
    }
    this._setProperty('yearPanelVisible', visible);
    if (this.rendered) {
      this._renderYearPanelVisible(true);
    }
  }

  setViewRangeFrom(date: Date) {
    let daysDiff = dates.compareDays(this.viewRange.to, this.viewRange.from);
    let viewRange = new DateRange(this.viewRange.from, this.viewRange.to);

    viewRange.from = date;
    viewRange.to = dates.shift(date, 0, 0, daysDiff);
    this.setViewRange(viewRange);
  }

  setViewRange(viewRange: DateRange) {
    if (this.viewRange === viewRange) {
      return;
    }
    this._setViewRange(viewRange);

    if (this.rendered) {
      this._renderViewRange();
      this._rerenderActivities();
      this._renderSelectedActivity();
      this.validateLayoutTree(); // Layouting is required for adjusting the scroll position
      this._reconcileScrollPos();
    }
  }

  selectRange(range: DateRange) {
    if (range && range.equals(this.selectionRange)) {
      return;
    }
    this.setProperty('selectionRange', range);
  }

  selectActivity(activity: PlannerActivity | string) {
    this.setProperty('selectedActivity', activity);
  }

  selectResources(resources: PlannerResource[] | PlannerResource) {
    let resourcesArray = arrays.ensure(resources);
    if (arrays.equals(resourcesArray, this.selectedResources)) {
      return;
    }

    // Make a copy so that original array stays untouched
    resourcesArray = resourcesArray.slice();
    this.setProperty('selectedResources', resourcesArray);
    this.trigger('resourcesSelected', {
      resources: resourcesArray
    });

    if (this.rendered) {
      // Render selection range as well for the case if selectedRange does not change but selected resources do
      this._renderSelectionRange();
    }
  }

  /**
   * Returns true if a deselection happened. False if the given resources were not selected at all.
   */
  deselectResources(resources: PlannerResource[] | PlannerResource): boolean {
    let deselected = false;
    resources = arrays.ensure(resources);
    let selectedResources = this.selectedResources.slice(); // copy
    if (arrays.removeAll(selectedResources, resources)) {
      this.selectResources(selectedResources);
      deselected = true;
    }
    return deselected;
  }

  insertResources(resources: PlannerResource[]) {
    // Update model
    resources.forEach(resource => {
      this._initResource(resource);
      // Always insert new rows at the end, if the order is wrong a rowOrderChanged event will follow
      this.resources.push(resource);
    });

    // Update HTML
    if (this.rendered) {
      this._renderResources(resources);
      this.invalidateLayoutTree();
    }
  }

  deleteResources(resources: PlannerResource[]) {
    if (this.deselectResources(resources)) {
      this.selectRange(new DateRange());
    }
    resources.forEach(resource => {
      // Update model
      arrays.remove(this.resources, resource);
      delete this.resourceMap[resource.id];

      resource.activities.forEach(activity => delete this.activityMap[activity.id]);

      // Update HTML
      if (this.rendered) {
        resource.$resource.remove();
        delete resource.$resource;
      }
    });

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

  updateResources(resources: PlannerResource[]) {
    resources.forEach(updatedResource => {
      let oldResource = this.resourceMap[updatedResource.id];
      if (!oldResource) {
        throw new Error('Update event received for non existing resource. ResourceId: ' + updatedResource.id);
      }

      // Replace old resource
      this._initResource(updatedResource);
      arrays.replace(this.resources, oldResource, updatedResource);
      arrays.replace(this.selectedResources, oldResource, updatedResource);

      // Replace old $resource
      if (this.rendered && oldResource.$resource) {
        let $updatedResource = $(this._buildResourceHtml(updatedResource));
        oldResource.$resource.replaceWith($updatedResource);
        $updatedResource.css('min-width', oldResource.$resource.css('min-width'));
        this._linkResource($updatedResource, updatedResource);
        this._linkActivitiesForResource(updatedResource);
      }
    });
  }
}

export type PlannerDisplayMode = EnumObject<typeof Planner.DisplayMode>;
export type PlannerDirection = EnumObject<typeof Planner.Direction>;
export type PlannerSelectionMode = EnumObject<typeof Planner.SelectionMode>;
export type PlannerMenuTypes = EnumObject<typeof Planner.MenuTypes>;

export interface PlannerActivity {
  id: string;
  beginTime: string | Date;
  endTime: string | Date;
  text?: string;
  backgroundColor?: string;
  foregroundColor?: string;
  level?: number;
  levelColor?: string;
  tooltipText?: string;
  cssClass?: string;
  $activity?: JQuery;
}

export interface PlannerResource {
  id: string;
  resourceCell: CellModel<any>;
  activities: PlannerActivity[];
  $resource?: JQuery;
  $cells?: JQuery;
}

export interface PlannerDisplayModeOptions {
  interval?: number;
  firstHourOfDay?: number;
  lastHourOfDay?: number;
  labelPeriod?: number;
  minSelectionIntervalCount?: number;
}
