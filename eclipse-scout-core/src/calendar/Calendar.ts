/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {CalendarComponent, CalendarEventMap, CalendarLayout, CalendarListComponent, CalendarModel, CalendarModesMenu, ContextMenuPopup, DateRange, dates, Device, EnumObject, Event, EventHandler, events, GroupBox, HtmlComponent, KeyStrokeContext, Menu, menus, numbers, objects, Point, PropertyChangeEvent, RoundingMode, scout, scrollbars, strings, ViewportScroller, Widget, YearPanel} from '../index';
import $ from 'jquery';
import {YearPanelDateSelectEvent} from './YearPanelEventMap';
import {EventMapOf, EventModel} from '../events/EventEmitter';
import {JsonDateRange} from '../util/dates';

export type CalendarDisplayMode = EnumObject<typeof Calendar.DisplayMode>;
export type CalendarDirection = EnumObject<typeof Calendar.Direction>;
export type CalendarMoveData = {
  event?: JQuery.MouseEventBase;
  cancel?: () => void;
  cancelled?: boolean;
  unitX?: number;
  unitY?: number;
  logicalX?: number;
  logicalY?: number;
  mode?: string;
  moving?: boolean;
  component?: CalendarComponent;
  containerOffset?: JQuery.Coordinates;
  containerScrollPosition?: Point;
  distance?: Point;
  startCursorPosition?: Point;
  currentCursorPosition?: Point;
  viewportScroller?: ViewportScroller;
  rafId?: number;
  onMove?: (event: JQuery.MouseMoveEvent) => void;
  onUp?: (event: JQuery.MouseUpEvent) => void;
};

export default class Calendar extends Widget implements CalendarModel {
  declare model: CalendarModel;
  declare eventMap: CalendarEventMap;

  monthViewNumberOfWeeks: number;
  numberOfHourDivisions: number;
  widthPerDivision: number;
  heightPerDivision: number;
  startHour: number;
  heightPerHour: number;
  heightPerDay: number;
  spaceBeforeScrollTop: number;
  workDayIndices: number[];
  displayMode: CalendarDisplayMode;
  components: CalendarComponent[];
  selectedComponent: CalendarComponent;
  displayCondensed: boolean;
  loadInProgress: boolean;
  selectedDate: Date;
  showDisplayModeSelection: boolean;
  title: string;
  useOverflowCells: boolean;
  viewRange: DateRange;
  calendarToggleListWidth: number;
  calendarToggleYearWidth: number;
  menuInjectionTarget: GroupBox;
  modesMenu: CalendarModesMenu;
  menus: Menu[];
  needsScrollToStartHour: boolean;

  $header: JQuery<HTMLDivElement>;
  $range: JQuery<HTMLDivElement>;
  $commands: JQuery<HTMLDivElement>;
  $grids: JQuery<HTMLDivElement>;
  $grid: JQuery<HTMLDivElement>;
  $topGrid: JQuery<HTMLDivElement>;
  $list: JQuery<HTMLDivElement>;
  $listTitle: JQuery<HTMLDivElement>;
  $progress: JQuery<HTMLDivElement>;
  $headerRow1: JQuery<HTMLDivElement>;
  $headerRow2: JQuery<HTMLDivElement>;
  $title: JQuery<HTMLDivElement>;
  $select: JQuery<HTMLDivElement>;
  $window: JQuery<Window>;

  /** additional modes; should be stored in model */
  protected _showYearPanel: boolean;
  protected _yearPanel: YearPanel;
  protected _showListPanel: boolean;

  /**
   * The narrow view range is different from the regular view range.
   * It contains only dates that exactly match the requested dates,
   * the regular view range contains also dates from the first and
   * next month. The exact range is not sent to the server.
   */
  protected _exactRange: DateRange;

  /**
   * When the list panel is shown, this list contains the scout.CalenderListComponent
   * items visible on the list.
   */
  protected _listComponents: CalendarListComponent[];

  protected _menuInjectionTargetMenusChangedHandler: EventHandler<PropertyChangeEvent<Menu[], GroupBox>>;

  /** Temporary data structure to store data while mouse actions are handled */
  protected _moveData: CalendarMoveData;

  protected _mouseMoveHandler: (event: JQuery.MouseMoveEvent) => void;
  protected _mouseUpHandler: (event: JQuery.MouseUpEvent) => void;

  constructor() {
    super();

    this.monthViewNumberOfWeeks = 6;
    this.numberOfHourDivisions = this.getConfiguredNumberOfHourDivisions();
    this.heightPerDivision = this.getConfiguredHeightPerDivision();
    this.startHour = this.getConfiguredStartHour();
    this.heightPerHour = this.numberOfHourDivisions * this.heightPerDivision;
    this.heightPerDay = 24 * this.heightPerHour;
    this.spaceBeforeScrollTop = 15;
    this.workDayIndices = [1, 2, 3, 4, 5]; // Workdays: Mon-Fri (Week starts at Sun in JS)
    this.components = [];
    this.displayCondensed = false;
    this.loadInProgress = false;
    this.selectedDate = null;
    this.showDisplayModeSelection = true;
    this.title = null;
    this.useOverflowCells = true;
    this.viewRange = null;
    this.calendarToggleListWidth = 270;
    this.calendarToggleYearWidth = 215;

    // main elements
    this.$container = null;
    this.$header = null;
    this.$range = null;
    this.$commands = null;
    this.$grids = null;
    this.$grid = null;
    this.$topGrid = null;
    this.$list = null;
    this.$progress = null;

    this._showYearPanel = false;
    this._showListPanel = false;

    this._exactRange = null;

    this._listComponents = [];
    this.menuInjectionTarget = null;
    this._menuInjectionTargetMenusChangedHandler = null;

    this._moveData = null;

    this._mouseMoveHandler = this._onMouseMove.bind(this);
    this._mouseUpHandler = this._onMouseUp.bind(this);

    this._addWidgetProperties(['components', 'menus', 'selectedComponent']);
    this._addPreserveOnPropertyChangeProperties(['selectedComponent']);
  }

  override init(model: CalendarModel) {
    super.init(model);
  }

  /**
   * Enum providing display-modes for calender-like components like calendar and planner.
   * @see ICalendarDisplayMode.java
   */
  static DisplayMode = {
    DAY: 1,
    WEEK: 2,
    MONTH: 3,
    WORK_WEEK: 4
  } as const;

  /**
   * Used as a multiplier in date calculations back- and forward (in time).
   */
  static Direction = {
    BACKWARD: -1,
    FORWARD: 1
  } as const;

  getConfiguredNumberOfHourDivisions(): number {
    return 2;
  }

  getConfiguredHeightPerDivision(): number {
    return 30;
  }

  getConfiguredStartHour(): number {
    return 6;
  }

  protected _isDay(): boolean {
    return this.displayMode === Calendar.DisplayMode.DAY;
  }

  protected _isWeek(): boolean {
    return this.displayMode === Calendar.DisplayMode.WEEK;
  }

  protected _isMonth(): boolean {
    return this.displayMode === Calendar.DisplayMode.MONTH;
  }

  protected _isWorkWeek(): boolean {
    return this.displayMode === Calendar.DisplayMode.WORK_WEEK;
  }

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected override _init(model: CalendarModel) {
    super._init(model);
    this._yearPanel = scout.create(YearPanel, {
      parent: this
    });
    this._yearPanel.on('dateSelect', this._onYearPanelDateSelect.bind(this));
    this.modesMenu = scout.create(CalendarModesMenu, {
      parent: this,
      visible: false,
      displayMode: this.displayMode
    });
    this._setSelectedDate(model.selectedDate);
    this._setDisplayMode(model.displayMode);
    this._setMenuInjectionTarget(model.menuInjectionTarget);
    this._exactRange = this._calcExactRange();
    this._yearPanel.setViewRange(this._exactRange);
    this.viewRange = this._calcViewRange();
  }

  setSelectedDate(date: Date | string) {
    this.setProperty('selectedDate', date);
  }

  protected _setSelectedDate(date: Date | string) {
    date = dates.ensure(date);
    this._setProperty('selectedDate', date);
    this._yearPanel.selectDate(this.selectedDate);
  }

  protected _renderSelectedDate() {
    this._updateModel(true, false);
  }

  setDisplayMode(displayMode: CalendarDisplayMode) {
    if (objects.equals(this.displayMode, displayMode)) {
      return;
    }
    let oldDisplayMode = this.displayMode;
    this._setDisplayMode(displayMode);
    if (this.rendered) {
      this._renderDisplayMode(oldDisplayMode);
    }
  }

  protected _setDisplayMode(displayMode: CalendarDisplayMode) {
    this._setProperty('displayMode', displayMode);
    this._yearPanel.setDisplayMode(this.displayMode);
    this.modesMenu.setDisplayMode(displayMode);
    if (this._isWorkWeek()) {
      // change date if selectedDate is on a weekend
      let p = this._dateParts(this.selectedDate, true);
      if (p.day > 4) {
        this.setSelectedDate(new Date(p.year, p.month, p.date - p.day + 4));
      }
    }
  }

  protected _renderDisplayMode(oldDisplayMode?: CalendarDisplayMode) {
    if (this.rendering) {
      // only do it on property changes
      return;
    }
    this._updateModel(false, true);

    // only render if components have another layout
    if (oldDisplayMode === Calendar.DisplayMode.MONTH || this.displayMode === Calendar.DisplayMode.MONTH) {
      this._renderComponents();
      this.needsScrollToStartHour = true;
    }
  }

  protected _setViewRange(viewRange: DateRange | JsonDateRange) {
    viewRange = DateRange.ensure(viewRange);
    this._setProperty('viewRange', viewRange);
  }

  protected _setMenus(menus: Menu[]) {
    if (this._checkMenuInjectionTarget(this.menuInjectionTarget)) {
      let originalMenus = this._removeInjectedMenus(this.menuInjectionTarget, this.menus);
      this.menuInjectionTarget.setMenus(menus.concat(originalMenus));
    }
    this._setProperty('menus', menus);
  }

  protected _setMenuInjectionTarget(menuInjectionTarget: GroupBox | string) {
    if (objects.isString(menuInjectionTarget)) {
      menuInjectionTarget = scout.widget(menuInjectionTarget) as GroupBox;
    }
    // Remove injected menus and installed listener from old injection target
    if (this._checkMenuInjectionTarget(this.menuInjectionTarget)) {
      menuInjectionTarget.off('propertyChange:menus', this._menuInjectionTargetMenusChangedHandler);
      let originalMenus = this._removeInjectedMenus(this.menuInjectionTarget, this.menus);
      this.menuInjectionTarget.setMenus(originalMenus);
    }
    if (this._checkMenuInjectionTarget(menuInjectionTarget)) {
      menuInjectionTarget.setMenus(this.menus.concat(menuInjectionTarget.menus));
      // Listen for menu changes on the injection target. Re inject menus into target if the menus have been altered.
      this._menuInjectionTargetMenusChangedHandler = evt => {
        if (this.menuInjectionTarget.menus.some(element => this.menus.includes(element))) {
          // Menus have already been injected => Do nothing
          return;
        }
        this.menuInjectionTarget.setMenus(this.menus.concat(this.menuInjectionTarget.menus));
      };
      menuInjectionTarget.on('propertyChange:menus', this._menuInjectionTargetMenusChangedHandler);
    }
    this._setProperty('menuInjectionTarget', menuInjectionTarget);
  }

  protected _checkMenuInjectionTarget(menuInjectionTarget: GroupBox): boolean {
    return menuInjectionTarget instanceof GroupBox;
  }

  protected _removeInjectedMenus(menuInjectionTarget: GroupBox, injectedMenus: Menu[]): Menu[] {
    return menuInjectionTarget.menus.filter(element => !injectedMenus.includes(element));
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('calendar');

    let layout = new CalendarLayout(this);
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(layout);
    let isMobile = Device.get().type === Device.Type.MOBILE;

    // main elements
    this.$header = this.$container.appendDiv('calendar-header');
    this.$header.toggleClass('mobile', isMobile);
    this.$headerRow1 = this.$header.appendDiv('calendar-header-row first');
    this.$headerRow2 = this.$header.appendDiv('calendar-header-row last');
    this._yearPanel.render();

    this.$grids = this.$container.appendDiv('calendar-grids');
    this.$topGrid = this.$grids.appendDiv('calendar-top-grid');
    this.$topGrid.toggleClass('mobile', isMobile);
    this.$grid = this.$grids.appendDiv('calendar-grid');
    this.$grid.toggleClass('mobile', isMobile);

    this.$list = this.$container.appendDiv('calendar-list-container').appendDiv('calendar-list');
    this.$listTitle = this.$list.appendDiv('calendar-list-title');

    // header contains range, title and commands. On small screens title will be moved to headerRow2
    this.$range = this.$headerRow1.appendDiv('calendar-range');
    this.$range.appendDiv('calendar-previous')
      .on('click', this._onPreviousClick.bind(this));
    this.$range.appendDiv('calendar-today', this.session.text('ui.CalendarToday'))
      .on('click', this._onTodayClick.bind(this));
    this.$range.appendDiv('calendar-next')
      .on('click', this._onNextClick.bind(this));

    // title
    this.$title = this.$headerRow1.appendDiv('calendar-title');
    this.$select = this.$title.appendDiv('calendar-select');
    this.$progress = this.$title.appendDiv('busyindicator-label');

    // commands
    this.$commands = this.$headerRow1.appendDiv('calendar-commands');
    this.$commands.appendDiv('calendar-mode first', this.session.text('ui.CalendarDay'))
      .attr('data-mode', Calendar.DisplayMode.DAY)
      .on('click', this._onDisplayModeClick.bind(this));
    this.$commands.appendDiv('calendar-mode', this.session.text('ui.CalendarWorkWeek'))
      .attr('data-mode', Calendar.DisplayMode.WORK_WEEK)
      .on('click', this._onDisplayModeClick.bind(this));
    this.$commands.appendDiv('calendar-mode', this.session.text('ui.CalendarWeek'))
      .attr('data-mode', Calendar.DisplayMode.WEEK)
      .on('click', this._onDisplayModeClick.bind(this));
    this.$commands.appendDiv('calendar-mode last', this.session.text('ui.CalendarMonth'))
      .attr('data-mode', Calendar.DisplayMode.MONTH)
      .on('click', this._onDisplayModeClick.bind(this));
    this.modesMenu.render(this.$commands);
    this.$commands.appendDiv('calendar-toggle-year')
      .on('click', this._onYearClick.bind(this));
    this.$commands.appendDiv('calendar-toggle-list')
      .on('click', this._onListClick.bind(this));

    // Append the top grid (day/week views)
    let $weekHeader = this.$topGrid.appendDiv('calendar-week-header');
    $weekHeader.appendDiv('calendar-week-name');
    for (let dayTop = 0; dayTop < 7; dayTop++) {
      $weekHeader.appendDiv('calendar-day-name')
        .data('day', dayTop);
    }

    this.$topGrid.appendDiv('calendar-week-task').attr('data-axis-name', this.session.text('ui.CalendarDay'));
    let $weekTopGridDays = this.$topGrid.appendDiv('calendar-week-allday-container');
    $weekTopGridDays.appendDiv('calendar-week-name');

    let dayContextMenuCallback = this._onDayContextMenu.bind(this);
    for (let dayBottom = 0; dayBottom < 7; dayBottom++) {
      $weekTopGridDays.appendDiv('calendar-day')
        .addClass('calendar-scrollable-components')
        .data('day', dayBottom)
        .on('contextmenu', dayContextMenuCallback);
    }

    for (let w = 1; w < 7; w++) {
      let $w = this.$grid.appendDiv('calendar-week');
      for (let d = 0; d < 8; d++) {
        let $d = $w.appendDiv();
        if (w > 0 && d === 0) {
          $d.addClass('calendar-week-name');
        } else if (w > 0 && d > 0) {
          $d.addClass('calendar-day')
            .data('day', d)
            .data('week', w)
            .on('contextmenu', dayContextMenuCallback);
        }
      }
    }

    // click event on all day and children elements
    let mousedownCallbackWithTime = this._onDayMouseDown.bind(this, true);
    this.$grid.find('.calendar-day').on('mousedown', mousedownCallbackWithTime);
    let mousedownCallback = this._onDayMouseDown.bind(this, false);
    this.$topGrid.find('.calendar-day').on('mousedown', mousedownCallback);

    this.$window = this.$container.window();
    this.$container.on('mousedown touchstart', this._onMouseDown.bind(this));

    this._updateScreen(false, false);
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderComponents();
    this._renderSelectedComponent();
    this._renderLoadInProgress();
    this._renderDisplayMode();
  }

  protected _renderComponents() {
    this.components.sort(this._sortFromTo);
    this.components.forEach(component => component.remove());
    this.components.forEach(component => component.render());
    this._arrangeComponents();
    this._updateListPanel();
  }

  protected _renderSelectedComponent() {
    if (this.selectedComponent) {
      this.selectedComponent.setSelected(true);
    }
  }

  protected _renderLoadInProgress() {
    this.$progress.setVisible(this.loadInProgress);
  }

  updateScrollPosition(animate: boolean) {
    if (!this.rendered) {
      // Execute delayed because table may be not layouted yet
      this.session.layoutValidator.schedulePostValidateFunction(this._updateScrollPosition.bind(this, true, animate));
    } else {
      this._updateScrollPosition(true, animate);
    }
  }

  protected _updateScrollPosition(scrollToInitialTime: boolean, animate: boolean) {
    if (this._isMonth()) {
      this._scrollToSelectedComponent(animate);
    } else {
      if (this.selectedComponent) {
        if (this.selectedComponent.fullDay) {
          this._scrollToSelectedComponent(animate); // scroll top-grid to selected component
          if (scrollToInitialTime) {
            this._scrollToInitialTime(animate); // scroll grid to initial time
          }
        } else {
          let date = dates.parseJsonDate(this.selectedComponent.fromDate);
          let topPercent = this._dayPosition(date.getHours(), date.getMinutes()) / 100;
          let topPos = this.heightPerDay * topPercent;
          scrollbars.scrollTop(this.$grid, topPos - this.spaceBeforeScrollTop, {
            animate: animate
          });
        }
      } else if (scrollToInitialTime) {
        this._scrollToInitialTime(animate);
      }
    }
  }

  protected _scrollToSelectedComponent(animate: boolean) {
    // @ts-ignore
    if (this.selectedComponent && this.selectedComponent._$parts[0] && this.selectedComponent._$parts[0].parent() && this.selectedComponent._$parts[0].isVisible()) {
      // @ts-ignore
      scrollbars.scrollTo(this.selectedComponent._$parts[0].parent(), this.selectedComponent._$parts[0], {
        animate: animate
      });
    }
  }

  protected _scrollToInitialTime(animate: boolean) {
    this.needsScrollToStartHour = false;
    if (!this._isMonth()) {
      if (this.selectedComponent && !this.selectedComponent.fullDay) {
        let date = dates.parseJsonDate(this.selectedComponent.fromDate);
        let topPercent = this._dayPosition(date.getHours(), date.getMinutes()) / 100;
        let topPos = this.heightPerDay * topPercent;
        scrollbars.scrollTop(this.$grid, topPos - this.spaceBeforeScrollTop, {
          animate: animate
        });
      } else {
        let scrollTargetTop = this.heightPerHour * this.startHour;
        scrollbars.scrollTop(this.$grid, scrollTargetTop - this.spaceBeforeScrollTop, {
          animate: animate
        });
      }
    }
  }

  /* -- basics, events -------------------------------------------- */

  protected _onPreviousClick(event: JQuery.ClickEvent<HTMLDivElement>) {
    this._navigateDate(Calendar.Direction.BACKWARD);
  }

  protected _onNextClick(event: JQuery.ClickEvent<HTMLDivElement>) {
    this._navigateDate(Calendar.Direction.FORWARD);
  }

  protected _dateParts(date: Date, modulo?: boolean): { year: number; month: number; date: number; day: number } {
    let parts = {
      year: date.getFullYear(),
      month: date.getMonth(),
      date: date.getDate(),
      day: date.getDay()
    };
    if (modulo) {
      parts.day = (date.getDay() + 6) % 7;
    }
    return parts;
  }

  protected _navigateDate(direction: CalendarDirection) {
    this.selectedDate = this._calcSelectedDate(direction);
    this._updateModel(true, false);
  }

  protected _calcSelectedDate(direction: CalendarDirection): Date {
    // noinspection UnnecessaryLocalVariableJS
    let p = this._dateParts(this.selectedDate),
      dayOperand = direction,
      weekOperand = direction * 7,
      monthOperand = direction;

    if (this._isDay()) {
      return new Date(p.year, p.month, p.date + dayOperand);
    }
    if (this._isWeek() || this._isWorkWeek()) {
      return new Date(p.year, p.month, p.date + weekOperand);
    }
    if (this._isMonth()) {
      return dates.shift(this.selectedDate, 0, monthOperand, 0);
    }
  }

  protected _updateModel(updateTopGrid: boolean, animate: boolean) {
    this._exactRange = this._calcExactRange();
    this._yearPanel.setViewRange(this._exactRange);
    this.viewRange = this._calcViewRange();
    this.trigger('modelChange');
    this._updateScreen(updateTopGrid, animate);
  }

  /**
   * Calculates exact date range of displayed components based on selected-date.
   */
  protected _calcExactRange(): DateRange {
    let from, to,
      p = this._dateParts(this.selectedDate, true);

    if (this._isDay()) {
      from = new Date(p.year, p.month, p.date);
      to = new Date(p.year, p.month, p.date + 1);
    } else if (this._isWeek()) {
      from = new Date(p.year, p.month, p.date - p.day);
      to = new Date(p.year, p.month, p.date - p.day + 6);
    } else if (this._isMonth()) {
      from = new Date(p.year, p.month, 1);
      to = new Date(p.year, p.month + 1, 0);
    } else if (this._isWorkWeek()) {
      from = new Date(p.year, p.month, p.date - p.day);
      to = new Date(p.year, p.month, p.date - p.day + 4);
    } else {
      throw new Error('invalid value for displayMode');
    }

    return new DateRange(from, to);
  }

  /**
   * Calculates the view-range, which is what the user sees in the UI.
   * The view-range is wider than the exact-range in the monthly mode,
   * as it contains also dates from the previous and next month.
   */
  protected _calcViewRange(): DateRange {
    let viewFrom = calcViewFromDate(this._exactRange.from),
      viewTo = calcViewToDate(viewFrom);
    return new DateRange(viewFrom, viewTo);

    function calcViewFromDate(fromDate: Date): Date {
      let i, tmpDate = new Date(fromDate.valueOf());
      for (i = 0; i < 42; i++) {
        tmpDate.setDate(tmpDate.getDate() - 1);
        if ((tmpDate.getDay() === 1) && tmpDate.getMonth() !== fromDate.getMonth()) {
          return tmpDate;
        }
      }
      throw new Error('failed to calc viewFrom date');
    }

    function calcViewToDate(fromDate: Date): Date {
      let i, tmpDate = new Date(fromDate.valueOf());
      for (i = 0; i < 42; i++) {
        tmpDate.setDate(tmpDate.getDate() + 1);
      }
      return tmpDate;
    }
  }

  protected _onTodayClick(event: JQuery.ClickEvent<HTMLDivElement>) {
    this.selectedDate = new Date();
    this._updateModel(true, false);
  }

  protected _onDisplayModeClick(event: JQuery.ClickEvent<HTMLDivElement>) {
    let displayMode = $(event.target).data('mode');
    this.setDisplayMode(displayMode);
  }

  protected _onYearClick(event: JQuery.ClickEvent<HTMLDivElement>) {
    this._showYearPanel = !this._showYearPanel;
    this._updateScreen(true, true);
  }

  protected _onListClick(event: JQuery.ClickEvent<HTMLDivElement>) {
    this._showListPanel = !this._showListPanel;
    this._updateScreen(false, true);
  }

  protected _onDayMouseDown(withTime: boolean, event: JQuery.MouseDownEvent) {
    let selectedDate = new Date($(event.delegateTarget).data('date'));
    if (withTime && (this._isDay() || this._isWeek() || this._isWorkWeek())) {
      // @ts-ignore
      let seconds = Math.floor(event.originalEvent.layerY / this.heightPerDivision) / this.numberOfHourDivisions * 60 * 60;
      if (seconds < 60 * 60 * 24) {
        selectedDate.setSeconds(seconds);
      }
    }
    this._setSelection(selectedDate, null, false);
  }

  /**
   * @param selectedComponent may be null when a day is selected
   */
  protected _setSelection(selectedDate: Date, selectedComponent: CalendarComponent, updateScrollPosition: boolean) {
    let changed = false;

    // selected date
    if (dates.compareDays(this.selectedDate, selectedDate) !== 0) {
      changed = true;
      $('.calendar-day', this.$container).each((index, element) => {
        let $day = $(element),
          date = $day.data('date');
        if (!date || dates.compareDays(date, this.selectedDate) === 0) {
          $day.select(false); // de-select old date
        } else if (dates.compareDays(date, selectedDate) === 0) {
          $day.select(true); // select new date
        }
      });
      this.selectedDate = selectedDate;
    }

    // selected component / part (may be null)
    if (this.selectedComponent !== selectedComponent) {
      changed = true;
      if (this.selectedComponent) {
        this.selectedComponent.setSelected(false);
      }
      if (selectedComponent) {
        selectedComponent.setSelected(true);
      }
      this.selectedComponent = selectedComponent;
    }

    if (changed) {
      this.trigger('selectionChange');
      this._updateListPanel();
      if (updateScrollPosition) {
        this._updateScrollPosition(false, true);
      }
    }

    if (this._showYearPanel) {
      this._yearPanel.selectDate(this.selectedDate);
    }
  }

  /* --  set display mode and range ------------------------------------- */

  protected _updateScreen(updateTopGrid: boolean, animate: boolean) {
    $.log.isInfoEnabled() && $.log.info('(Calendar#_updateScreen)');

    // select mode
    $('.calendar-mode', this.$commands).select(false);
    $('[data-mode="' + this.displayMode + '"]', this.$commands).select(true);

    // remove selected day
    $('.selected', this.$grid).select(false);

    // layout grid
    this.layoutLabel();
    this.layoutSize(animate);
    this.layoutAxis();

    if (this._showYearPanel) {
      this._yearPanel.selectDate(this.selectedDate);
    }

    this._updateListPanel();
    this._updateScrollbars(this.$grid, animate);
    if (updateTopGrid && !this._isMonth()) {
      this._updateTopGrid();
    }
  }

  layoutSize(animate?: boolean) {
    // reset animation sizes
    $('div', this.$container).removeData(['new-width', 'new-height']);

    if (this._isMonth()) {
      this.$topGrid.addClass('calendar-top-grid-short');
      this.$grid.removeClass('calendar-grid-short');
    } else {
      this.$topGrid.removeClass('calendar-top-grid-short');
      this.$grid.addClass('calendar-grid-short');
    }

    // init vars (Selected: Day)
    let $selected = $('.selected', this.$grid),
      $topSelected = $('.selected', this.$topGrid),
      containerW = this.$container.width(),
      gridH = this.$grid.height(),
      gridPaddingX = this.$grid.innerWidth() - this.$grid.width(),
      gridW = containerW - gridPaddingX;

    // show or hide year
    $('.calendar-toggle-year', this.$commands).select(this._showYearPanel);
    if (this._showYearPanel) {
      this._yearPanel.$container.data('new-width', this.calendarToggleYearWidth);
      gridW -= this.calendarToggleYearWidth;
      containerW -= this.calendarToggleYearWidth;
    } else {
      this._yearPanel.$container.data('new-width', 0);
    }

    // show or hide work list
    $('.calendar-toggle-list', this.$commands).select(this._showListPanel);
    if (this._showListPanel) {
      this.$list.parent().data('new-width', this.calendarToggleListWidth);
      gridW -= this.calendarToggleListWidth;
      containerW -= this.calendarToggleListWidth;
    } else {
      this.$list.parent().data('new-width', 0);
    }

    // basic grid width
    this.$grids.data('new-width', containerW);

    let $weeksToHide = $(); // Empty
    let $allWeeks = $('.calendar-week', this.$grid);
    // layout week

    if (this._isDay() || this._isWeek() || this._isWorkWeek()) {
      $allWeeks.removeClass('calendar-week-noborder');
      // Parent of selected (Day) is a week
      let selectedWeek = $selected.parent();
      $weeksToHide = $allWeeks.not(selectedWeek); // Hide all (other) weeks delayed, height will animate to zero
      $weeksToHide.data('new-height', 0);
      $weeksToHide.removeClass('invisible');
      selectedWeek.data('new-height', this.heightPerDay);
      selectedWeek.addClass('calendar-week-noborder');
      selectedWeek.removeClass('hidden invisible'); // Current week must be shown
      $('.calendar-day', selectedWeek).data('new-height', this.heightPerDay);
      // Hide the week-number in the lower grid
      $('.calendar-week-name', this.$grid).addClass('invisible'); // Keep the reserved space
      $('.calendar-week-allday-container', this.$topGrid).removeClass('hidden');
      $('.calendar-week-task', this.$topGrid).removeClass('hidden');
    } else {
      // Month
      let newHeightMonth = gridH / this.monthViewNumberOfWeeks;
      $allWeeks.removeClass('calendar-week-noborder invisible hidden');
      $allWeeks.eq(0).addClass('calendar-week-noborder');
      $allWeeks.data('new-height', newHeightMonth);
      $('.calendar-day', this.$grid).data('new-height', newHeightMonth);
      let $allDays = $('.calendar-week-name', this.$grid);
      $allDays.removeClass('hidden invisible');
      $allDays.data('new-height', newHeightMonth);
      $('.calendar-week-allday-container', this.$topGrid).addClass('hidden');
      $('.calendar-week-task', this.$topGrid).addClass('hidden');
    }

    // layout days
    let contentW = gridW - 45; // gridW - @calendar-week-name-width
    if (this._isDay()) {
      $('.calendar-day-name, .calendar-day', this.$topGrid).data('new-width', 0);
      $('.calendar-day', this.$grid).data('new-width', 0);
      $('.calendar-day-name:nth-child(' + ($topSelected.index() + 1) + ')', this.$topGrid)
        .data('new-width', contentW);
      $('.calendar-day:nth-child(' + ($topSelected.index() + 1) + ')', this.$topGrid).data('new-width', contentW);
      $('.calendar-day:nth-child(' + ($selected.index() + 1) + ')', this.$grid).data('new-width', contentW);
      this.widthPerDivision = contentW;
    } else if (this._isWorkWeek()) {
      this.$topGrid.find('.calendar-day-name').data('new-width', 0);
      this.$grids.find('.calendar-day').data('new-width', 0);
      let newWidthWorkWeek = Math.round(contentW / this.workDayIndices.length);
      this.$topGrid.find('.calendar-day-name').slice(0, 5).data('new-width', newWidthWorkWeek);
      this.$topGrid.find('.calendar-day').slice(0, 5).data('new-width', newWidthWorkWeek);
      $('.calendar-day:nth-child(-n+6)', this.$grid).data('new-width', newWidthWorkWeek);
      this.widthPerDivision = newWidthWorkWeek;
    } else if (this._isMonth() || this._isWeek()) {
      let newWidthMonthOrWeek = Math.round(contentW / 7);
      this.$grids.find('.calendar-day').data('new-width', newWidthMonthOrWeek);
      this.$topGrid.find('.calendar-day-name').data('new-width', newWidthMonthOrWeek);
      this.widthPerDivision = newWidthMonthOrWeek;
    }

    // layout components
    if (this._isMonth()) {
      $('.component-month', this.$grid).each(function() {
        let $comp = $(this),
          $day = $comp.closest('.calendar-day');
        $comp.toggleClass('compact', $day.data('new-width') < CalendarComponent.MONTH_COMPACT_THRESHOLD);
      });
    }

    // animate old to new sizes
    $('div', this.$container).each((i, elem) => {
      let $e = $(elem);
      let w = $e.data('new-width');
      let h = $e.data('new-height');
      $e.stop(false, true);

      if (w !== undefined && w !== $e.outerWidth()) {
        if (animate) {
          let opts: JQuery.EffectsOptions<HTMLElement> = {
            complete: () => this._afterLayout($e, animate)
          };
          if ($e[0] === this.$grids[0]) {
            // Grid contains scroll shadows that should be updated during animation (don't due it always for performance reasons)
            opts.progress = () => this._afterLayout($e, animate);
          }
          $e.animate({width: w}, opts);
        } else {
          $e.css('width', w);
          this._afterLayout($e, animate);
        }
      }
      if (h !== undefined && h !== $e.outerHeight()) {
        if (h > 0) {
          $e.removeClass('hidden');
        }
        if (animate) {
          $e.animateAVCSD('height', h, () => {
            if (h === 0) {
              $e.addClass('hidden');
            }
            this._afterLayout($e, animate);
          });
        } else {
          $e.css('height', h);
          if (h === 0) {
            $e.addClass('hidden');
          }
          this._afterLayout($e, animate);
        }
      }
    });
  }

  protected _afterLayout($parent: JQuery, animate: boolean) {
    this._updateScrollbars($parent, animate);
    this._updateWeekdayNames();
  }

  protected _updateWeekdayNames() {
    // set day-name (based on width of shown column)
    let weekdayWidth = this.$topGrid.width(),
      weekdays;

    if (this._isDay()) {
      weekdayWidth /= 1;
    } else if (this._isWorkWeek()) {
      weekdayWidth /= this.workDayIndices.length;
    } else if (this._isWeek()) {
      weekdayWidth /= 7;
    } else if (this._isMonth()) {
      weekdayWidth /= 7;
    }

    if (weekdayWidth > 90) {
      weekdays = this.session.locale.dateFormat.symbols.weekdaysOrdered;
    } else {
      weekdays = this.session.locale.dateFormat.symbols.weekdaysShortOrdered;
    }

    $('.calendar-day-name', this.$topGrid).each(function(index: number) {
      $(this).attr('data-day-name', weekdays[index]);
    });
  }

  protected _updateScrollbars($parent: JQuery, animate: boolean) {
    let $scrollables = $('.calendar-scrollable-components', $parent);
    $scrollables.each((i, elem) => {
      scrollbars.update($(elem), true);
    });
    this.updateScrollPosition(animate);
  }

  protected _uninstallComponentScrollbars($parent: JQuery) {
    $parent.find('.calendar-scrollable-components').each((i, elem) => {
      scrollbars.uninstall($(elem), this.session);
    });
  }

  protected _updateTopGrid() {
    $('.calendar-component', this.$topGrid).each((index, part) => {
      let component = $(part).data('component');
      if (component) {
        component.remove();
      }
    });
    let allDayComponents = this.components.filter(component => component.fullDay);
    // first remove all components and add them from scratch
    allDayComponents.forEach(component => component.remove());
    allDayComponents.forEach(component => component.render());
    this._updateScrollbars(this.$topGrid, false);
  }

  layoutYearPanel() {
    if (this._showYearPanel) {
      scrollbars.update(this._yearPanel.$yearList);
      // @ts-ignore
      this._yearPanel._scrollYear();
    }
  }

  layoutLabel() {
    let text, $dates, $topGridDates,
      exFrom = this._exactRange.from,
      exTo = this._exactRange.to;

    // set range text
    if (this._isDay()) {
      text = this._format(exFrom, 'd. MMMM yyyy');
    } else if (this._isWorkWeek() || this._isWeek()) {
      let toText = this.session.text('ui.to');
      if (exFrom.getMonth() === exTo.getMonth()) {
        text = strings.join(' ', this._format(exFrom, 'd.'), toText, this._format(exTo, 'd. MMMM yyyy'));
      } else if (exFrom.getFullYear() === exTo.getFullYear()) {
        text = strings.join(' ', this._format(exFrom, 'd. MMMM'), toText, this._format(exTo, 'd. MMMM yyyy'));
      } else {
        text = strings.join(' ', this._format(exFrom, 'd. MMMM yyyy'), toText, this._format(exTo, 'd. MMMM yyyy'));
      }

    } else if (this._isMonth()) {
      text = this._format(exFrom, 'MMMM yyyy');
    }
    this.$select.text(text);

    // prepare to set all day date and mark selected one
    $dates = $('.calendar-day', this.$grid);

    let w, d, cssClass,
      currentMonth = this._exactRange.from.getMonth(),
      date = new Date(this.viewRange.from.valueOf());

    // Main grid: loop all days and set value and class
    for (w = 0; w < this.monthViewNumberOfWeeks; w++) {
      for (d = 0; d < 7; d++) {
        cssClass = '';
        if (this.workDayIndices.indexOf(date.getDay()) === -1) {
          cssClass = date.getMonth() !== currentMonth ? ' weekend-out' : ' weekend';
        } else {
          cssClass = date.getMonth() !== currentMonth ? ' out' : '';
        }
        if (dates.isSameDay(date, new Date())) {
          cssClass += ' now';
        }
        if (dates.isSameDay(date, this.selectedDate)) {
          cssClass += ' selected';
        }
        if (!this._isMonth()) {
          cssClass += ' calendar-no-label'; // If we're not in the month view, number is shown on top
        }

        // adjust position for days between 10 and 19 (because "1" is narrower than "0" or "2")
        if (date.getDate() > 9 && date.getDate() < 20) {
          cssClass += ' center-nice';
        }

        text = this._format(date, 'dd');
        $dates.eq(w * 7 + d)
          .removeClass('weekend-out weekend out selected now calendar-no-label')
          .addClass(cssClass)
          .attr('data-day-name', text)
          .data('date', new Date(date.valueOf()));
        date.setDate(date.getDate() + 1);
      }
    }

    // Top grid: loop days of one calendar week and set value and class
    if (!this._isMonth()) {
      $topGridDates = $('.calendar-day', this.$topGrid);
      // From the view range, find the week we are in
      let exactDate = new Date(this._exactRange.from.valueOf());

      // Find first day of week.
      date = dates.firstDayOfWeek(exactDate, 1);

      for (d = 0; d < 7; d++) {
        cssClass = '';
        if (this.workDayIndices.indexOf(date.getDay()) === -1) {
          cssClass = date.getMonth() !== currentMonth ? ' weekend-out' : ' weekend';
        } else {
          cssClass = date.getMonth() !== currentMonth ? ' out' : '';
        }
        if (dates.isSameDay(date, new Date())) {
          cssClass += ' now';
        }
        if (dates.isSameDay(date, this.selectedDate)) {
          cssClass += ' selected';
        }

        text = this._format(date, 'dd');
        $topGridDates.eq(d)
          .removeClass('weekend-out weekend out selected now')
          .addClass(cssClass)
          .attr('data-day-name', text)
          .data('date', new Date(date.valueOf()));

        date.setDate(date.getDate() + 1);
      }
    }
  }

  layoutAxis() {
    let $e;

    // remove old axis
    $('.calendar-week-axis, .calendar-week-task', this.$grid).remove();

    // set week name
    let session = this.session;

    $('.calendar-week-name', this.$container).each(function(index) {
      if (index > 0) {
        $e = $(this);
        $e.text(session.text('ui.CW', dates.weekInYear($e.next().data('date')) + ''));
      }
    });

    // day schedule
    if (!this._isMonth()) {
      // Parent of selected day: Week
      //    var $parent = $selected.parent();
      let $parent = $('.calendar-week', this.$grid);

      for (let h = 0; h < 24; h++) { // Render lines for each hour
        let paddedHour = ('00' + h).slice(-2);
        let topPos = h * this.heightPerHour;
        $parent.appendDiv('calendar-week-axis hour' + (h === 0 ? ' first' : '')).attr('data-axis-name', paddedHour + ':00').css('top', topPos + 'px');

        for (let m = 1; m < this.numberOfHourDivisions; m++) { // First one rendered above. Start at the next
          topPos += this.heightPerDivision;
          $parent.appendDiv('calendar-week-axis').attr('data-axis-name', '').css('top', topPos + 'px');
        }
      }
    }
  }

  /* -- year events ---------------------------------------- */

  protected _onYearPanelDateSelect(event: YearPanelDateSelectEvent) {
    this.selectedDate = event.date;
    this._updateModel(true, false);
  }

  protected _updateListPanel() {
    if (this._showListPanel) {

      // remove old list-components
      this._listComponents.forEach(listComponent => listComponent.remove());
      this._listComponents = [];
      this._renderListPanel();
    }
  }

  protected override _remove() {
    this._uninstallComponentScrollbars(this.$grid);
    this._uninstallComponentScrollbars(this.$topGrid);

    this.$window
      .off('mousemove touchmove', this._mouseMoveHandler)
      .off('mouseup touchend touchcancel', this._mouseUpHandler);
    this._moveData = null;

    super._remove();
  }

  /**
   * Renders the panel on the left, showing all components of the selected date.
   */
  protected _renderListPanel() {
    let listComponent: CalendarListComponent, components: CalendarComponent[] = [];

    // set title
    this.$listTitle.text(this._format(this.selectedDate, 'd. MMMM yyyy'));

    // find components to display on the list panel
    this.components.forEach(component => {
      if (belongsToSelectedDate.call(this, component)) {
        components.push(component);
      }
    });

    function belongsToSelectedDate(component: CalendarComponent): boolean {
      let selectedDate = dates.trunc(this.selectedDate);
      return dates.compare(selectedDate, component.coveredDaysRange.from) >= 0 &&
        dates.compare(selectedDate, component.coveredDaysRange.to) <= 0;
    }

    components.forEach(component => {
      listComponent = new CalendarListComponent(this.selectedDate, component);
      listComponent.render(this.$list);
      this._listComponents.push(listComponent);
    });
  }

  /* -- components, events-------------------------------------------- */

  protected _selectedComponentChanged(component: CalendarComponent, partDay: Date, updateScrollPosition: boolean) {
    this._setSelection(partDay, component, updateScrollPosition);
  }

  protected _onDayContextMenu(event: JQuery.ContextMenuEvent<HTMLDivElement>) {
    this._showContextMenu(event, 'Calendar.EmptySpace');
  }

  protected _showContextMenu(event: JQuery.ContextMenuEvent<HTMLDivElement>, allowedType: string) {
    event.preventDefault();
    event.stopPropagation();

    let func = function func(event: JQuery.ContextMenuEvent<HTMLDivElement>, allowedType: string) {
      if (!this.rendered || !this.attached) { // check needed because function is called asynchronously
        return;
      }
      let filteredMenus = menus.filter(this.menus, [allowedType], {
          onlyVisible: true
        }),
        $part = $(event.currentTarget);
      if (filteredMenus.length === 0) {
        return;
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

  /* -- components, arrangement------------------------------------ */

  protected _arrangeComponents() {
    let k, j, $day, $allChildren, $children, $scrollableContainer, dayComponents, day;

    let $days = $('.calendar-day', this.$grid);
    // Main (Bottom) grid: Iterate over days
    for (k = 0; k < $days.length; k++) {
      $day = $days.eq(k);
      $children = $day.children('.calendar-component:not(.component-task)');
      $allChildren = $day.children('.calendar-component');
      day = $day.data('date');

      // Remove old element containers
      $scrollableContainer = $day.children('.calendar-scrollable-components');
      if ($scrollableContainer.length > 0) {
        scrollbars.uninstall($scrollableContainer, this.session);
        $scrollableContainer.remove();
      }

      if (this._isMonth() && $allChildren.length > 0) {
        $scrollableContainer = $day.appendDiv('calendar-scrollable-components');

        for (j = 0; j < $allChildren.length; j++) {
          let $child = $allChildren.eq(j);
          // non-tasks (communications) are distributed manually
          // within the parent container in all views except the monthly view.
          if (!this._isMonth() && !$child.hasClass('component-task')) {
            continue;
          }
          $scrollableContainer.append($child);
        }

        scrollbars.install($scrollableContainer, {
          parent: this,
          session: this.session,
          axis: 'y'
        });
      }

      if (this._isMonth() && $children.length > 2) {
        $day.addClass('many-items');
      } else if (!this._isMonth() && $children.length > 1) {
        // logical placement
        dayComponents = this._getComponents($children);
        this._arrange(dayComponents, day);

        // screen placement
        this._arrangeComponentSetPlacement($children, day);
      }
    }

    if (this._isMonth()) {
      this._uninstallScrollbars();
      this._uninstallComponentScrollbars(this.$topGrid);
      this.$grid.removeClass('calendar-scrollable-components');
    } else {
      this.$grid.addClass('calendar-scrollable-components');
      // If we're in the non-month views, the time can scroll. Add scrollbars
      this._installScrollbars({
        parent: this,
        session: this.session,
        axis: 'y'
      });
      this.$topGrid.find('.calendar-scrollable-components').each((i, elem) => {
        let $topDay = $(elem);
        if ($topDay.data('scrollable')) {
          scrollbars.update($topDay);
          return;
        }
        scrollbars.install($topDay, {
          parent: this,
          session: this.session,
          axis: 'y',
          scrollShadow: 'none'
        });
      });
    }
  }

  protected _getComponents($children: JQuery): CalendarComponent[] {
    let i, $child;
    let components = [];
    for (i = 0; i < $children.length; i++) {
      $child = $children.eq(i);
      components.push($child.data('component'));
    }
    return components;
  }

  protected _sort(components: CalendarComponent[]) {
    components.sort(this._sortFromTo);
  }

  /**
   * Arrange components (stack width, stack index) per day
   * */
  protected _arrange(components: CalendarComponent[], day: Date) {
    let i, j, c, r, k,
      columns = [],
      key = day + '';

    // ordered by from, to
    this._sort(components);

    // clear existing placement
    for (i = 0; i < components.length; i++) {
      c = components[i];
      if (!c.stack) {
        c.stack = {};
      }
      c.stack[key] = {};
    }

    for (i = 0; i < components.length; i++) {
      c = components[i];
      r = c.getPartDayPosition(day); // Range [from,to]

      // reduce number of columns, if all components end before this one
      if (columns.length > 0 && this._allEndBefore(columns, r.from, day)) {
        columns = [];
      }

      // replace an component that ends before and can be replaced
      k = this._findReplacableColumn(columns, r.from, day);

      // insert
      if (k >= 0) {
        columns[k] = c;
        c.stack[key].x = k;
      } else {
        columns.push(c);
        c.stack[key].x = columns.length - 1;
      }

      // update stackW
      for (j = 0; j < columns.length; j++) {
        columns[j].stack[key].w = columns.length;
      }
    }
  }

  protected _allEndBefore(columns: CalendarComponent[], pos: number, day: Date): boolean {
    let i;
    for (i = 0; i < columns.length; i++) {
      if (!this._endsBefore(columns[i], pos, day)) {
        return false;
      }
    }
    return true;
  }

  protected _findReplacableColumn(columns: CalendarComponent[], pos: number, day: Date): number {
    let j;
    for (j = 0; j < columns.length; j++) {
      if (this._endsBefore(columns[j], pos, day)) {
        return j;
      }
    }
    return -1;
  }

  protected _endsBefore(component, pos: number, day: Date): boolean {
    return component.getPartDayPosition(day).to <= pos;
  }

  protected _arrangeComponentSetPlacement($children: JQuery, day: Date) {
    let i, $child, stack;

    // loop and place based on data
    for (i = 0; i < $children.length; i++) {
      $child = $children.eq(i);
      stack = $child.data('component').stack[day + ''];

      // make last element smaller
      $child
        .css('width', 100 / stack.w + '%')
        .css('left', stack.x * 100 / stack.w + '%');
    }
  }

  override get$Scrollable(): JQuery {
    return this.$grid;
  }

  protected _onMouseDown(event: JQuery.MouseDownEvent) {
    if (this._moveData) {
      // Do nothing, when dragging is already in progress. This can happen when the user leaves
      // the browser window (e.g. using Alt-Tab) while holding the mouse button pressed and
      // then returns and presses the mouse button again.
      return;
    }

    let component = this._getCalendarComponentForMouseEvent(event);
    // component has to be marked as draggable by the backend
    // dragging of fullDay components is not supported yet
    // dragging of components spanning more than one day is not supported yet
    // @ts-ignore
    if (!component || !component.draggable || (!this._isMonth() && component.fullDay) || component._$parts.length > 1) {
      return;
    }

    // Ignore right mouse button clicks (allow bubble up --> could trigger context menu)
    if (event.which === 3) {
      // Select clicked widget first, otherwise we might display the wrong context menu
      return;
    }

    this.$window
      .off('mousemove touchmove', this._mouseMoveHandler)
      .off('mouseup touchend touchcancel', this._mouseUpHandler)
      .on('mousemove touchmove', this._mouseMoveHandler)
      .on('mouseup touchend touchcancel', this._mouseUpHandler);

    this._moveData = this._newMoveData(event);

    this._onComponentMouseDown(event, component);
    this._moveData.onMove = this._onComponentMouseMove.bind(this);
    this._moveData.onUp = this._onComponentMouseUp.bind(this);
  }

  protected _onMouseMove(event: JQuery.MouseMoveEvent) {
    events.fixTouchEvent(event);

    this._moveData.event = event;
    this._moveData.currentCursorPosition = new Point(
      event.pageX - this._moveData.containerOffset.left + this._moveData.containerScrollPosition.x,
      event.pageY - this._moveData.containerOffset.top + this._moveData.containerScrollPosition.y
    );

    this._scrollViewportWhileDragging(event);

    if (this._moveData.onMove && !this._moveData.cancelled) {
      this._moveData.onMove(event);
    }
  }

  protected _scrollViewportWhileDragging(event: JQuery.MouseMoveEvent) {
    if (!this._moveData || this._moveData.mode === 'pan' || this._moveData.cancelled) {
      return;
    }

    if (!this._moveData.viewportScroller) {
      this._moveData.viewportScroller = scout.create(ViewportScroller, $.extend({
        viewportWidth: this.$grid.width(),
        viewportHeight: this.$grid.height(),
        active: () => !!this._moveData,
        scroll: (dx, dy) => {
          let newScrollTop = this.get$Scrollable().scrollTop() + dy;
          scrollbars.scrollTop(this.get$Scrollable(), newScrollTop);
          this._moveData.containerScrollPosition = new Point(this.get$Scrollable().scrollLeft(), this.get$Scrollable().scrollTop());
        }
      }));
    }

    // Mouse position (viewport-relative coordinates) in pixel
    let mouse = this._moveData.currentCursorPosition.subtract(this._moveData.containerScrollPosition);
    this._moveData.viewportScroller.update(mouse);
  }

  protected _onMouseUp(event: JQuery.MouseUpEvent) {
    this.$window
      .off('mousemove touchmove', this._mouseMoveHandler)
      .off('mouseup touchend touchcancel', this._mouseUpHandler);

    events.fixTouchEvent(event);

    this._moveData.event = event;

    if (this._moveData.onUp && !this._moveData.cancelled) {
      this._moveData.onUp(event);
    }

    this._moveData = null;
  }

  protected _onComponentMouseDown(event: JQuery.MouseDownEvent, component: CalendarComponent) {

    // Prepare for dragging
    this._moveData.component = component;

    // @ts-ignore
    let $firstPart = component._$parts[0];
    this._moveData.logicalX = $firstPart.closest('.calendar-day').data().day;
    this._moveData.logicalY = Math.round($firstPart.position().top) / this.heightPerDivision;
    if (this._isMonth()) {
      this._moveData.logicalY = $firstPart.closest('.calendar-day').data().week;
    }

    // Prevent scrolling on touch devices (like "touch-action: none" but with better browser support).
    // Theoretically, unwanted scrolling can be prevented by adding the CSS rule "touch-action: none"
    // to the element. Unfortunately, not all devices support this (e.g. Apple Safari on iOS).
    // Therefore, we always suppress the scrolling in JS. Because this also suppresses the 'click'
    // event, click actions have to be triggered manually in the 'mouseup' handler.
    event.preventDefault();
  }

  protected _onComponentMouseMove(event: JQuery.MouseMoveEvent) {
    if (!this._moveData.rafId) {
      this._moveData.rafId = requestAnimationFrame(this._whileComponentMove.bind(this));
    }
  }

  protected _onComponentMouseUp(event: JQuery.MouseUpEvent) {
    this._endComponentMove();
  }

  protected _whileComponentMove() {
    this._moveData.rafId = null;

    let pixelDistance = new Point(
      this._moveData.currentCursorPosition.x - this._moveData.startCursorPosition.x,
      this._moveData.currentCursorPosition.y - this._moveData.startCursorPosition.y);

    // Ignore small mouse movements
    if (!this._moveData.moving) {
      if (Math.abs(pixelDistance.x) < 7 && Math.abs(pixelDistance.y) < 7) {
        return;
      }
      this._moveData.moving = true;
    }

    // Snap to grid
    let logicalDistance = this.toLogicalPosition(pixelDistance);

    // Limit logical distance
    let minX = 1;
    let minY = this._isMonth() ? 1 : 0;
    let maxX = this._isWorkWeek() ? this.workDayIndices.length : 7;
    let maxY = this._isMonth() ?
      this.monthViewNumberOfWeeks :
      (24 * this.numberOfHourDivisions) - Math.ceil(this._moveData.component.getLengthInHoursDecimal() * this.numberOfHourDivisions);

    function limitDistance(newPosition: Point) {
      let newX = newPosition.x;
      let newY = newPosition.y;
      if (newX < minX) {
        logicalDistance.x -= (newX - minX);
      } else if (newX > maxX) {
        logicalDistance.x -= (newX - maxX);
      }
      if (newY < minY) {
        logicalDistance.y -= (newY - minY);
      } else if (newY > maxY) {
        logicalDistance.y -= (newY - maxY);
      }
    }

    limitDistance(new Point(this._moveData.logicalX, this._moveData.logicalY).add(logicalDistance));

    this._moveData.distance = logicalDistance;

    // Update logical position
    let newLogicalPosition = new Point(this._moveData.logicalX, this._moveData.logicalY).add(this._moveData.distance);

    this._setComponentLogicalPosition(this._moveData.component, newLogicalPosition);
  }

  protected _endComponentMove() {
    if (this._moveData.rafId) {
      cancelAnimationFrame(this._moveData.rafId);
      this._moveData.rafId = null;
    }

    let component = this._moveData.component;

    if (this._moveData.distance) {
      let moved = false;
      // Move pixel position by distance
      let logicalPosition = new Point(this._moveData.logicalX, this._moveData.logicalY).add(this._moveData.distance);

      // Logical position
      let diffX = logicalPosition.x - this._moveData.logicalX;
      let diffY = logicalPosition.y - this._moveData.logicalY;

      moved = moved || !!diffX || !!diffY;

      if (moved) {
        let appointmentToDate = dates.parseJsonDate(component.toDate);
        let appointmentFromDate = dates.parseJsonDate(component.fromDate);

        let daysShift = diffX;
        if (this._isMonth()) {
          // in month mode the y-axis is a shit in weeks
          daysShift += diffY * 7;
        } else {
          // time difference (y-axis) only if we are not in month mode
          let timeDiff = this._hourMinuteByDivision(diffY);
          appointmentFromDate = dates.shiftTime(appointmentFromDate, timeDiff.hour, timeDiff.minute);
          appointmentToDate = dates.shiftTime(appointmentToDate, timeDiff.hour, timeDiff.minute);
        }
        appointmentFromDate = dates.shift(appointmentFromDate, 0, 0, daysShift);
        appointmentToDate = dates.shift(appointmentToDate, 0, 0, daysShift);

        component.fromDate = this._format(appointmentFromDate, 'yyyy-MM-dd HH:mm:ss.SSS');
        component.toDate = this._format(appointmentToDate, 'yyyy-MM-dd HH:mm:ss.SSS');
        component.coveredDaysRange = new DateRange(dates.trunc(appointmentFromDate), dates.trunc(appointmentToDate));
        this._renderComponents();

        this.trigger('componentMove', {
          component: component
        });
      }
    }
  }

  override trigger<K extends string & keyof EventMapOf<Calendar>>(type: K, eventOrModel?: Event | EventModel<EventMapOf<Calendar>[K]>): EventMapOf<Calendar>[K] {
    return super.trigger(type, eventOrModel);
  }

  protected _setComponentLogicalPosition(component: CalendarComponent, logicalPosition: Point) {
    // @ts-ignore
    let $firstPart = component._$parts[0];
    let currDay = $firstPart.closest('.calendar-day').data('day');
    let currWeek = $firstPart.closest('.calendar-day').data('week');

    if (component.rendered) {
      if (this._isMonth()) {
        if (currDay !== logicalPosition.x || currWeek !== logicalPosition.y) {
          let newContainer =
            $('.calendar-week:not(.hidden) > .calendar-day')
              .filter(function() {
                return $(this).data('day') === logicalPosition.x &&
                  $(this).data('week') === logicalPosition.y;
              });
          let csc = newContainer.find('.calendar-scrollable-components');
          newContainer = csc.length > 0 ? csc : newContainer;
          $firstPart.detach().appendTo(newContainer);
        }
      } else {
        if (currDay !== logicalPosition.x) {
          $firstPart.detach().appendTo($('.calendar-week:not(.hidden) > .calendar-day')
            .filter(function() {
              return $(this).data('day') === logicalPosition.x;
            }));
        }

        let pos = this._dayPositionByDivision(logicalPosition.y) + '%';
        $firstPart.css('top', pos);
      }
    }
  }

  protected _getCalendarComponentForMouseEvent(event: JQuery.MouseDownEvent): CalendarComponent {
    let $elem = $(event.target);
    $elem = $.ensure($elem);
    while ($elem && $elem.length > 0) {
      let component = $elem.data('component');
      if (component) {
        return component;
      }
      $elem = $elem.parent();
    }
    return null;
  }

  protected _newMoveData(event: JQuery.MouseDownEvent): CalendarMoveData {
    let moveData: CalendarMoveData = {};
    moveData.event = event;
    moveData.cancel = () => {
      moveData.cancelled = true;
    };

    moveData.unitX = this.widthPerDivision;
    moveData.unitY = this.heightPerDivision;
    if (this._isMonth()) {
      moveData.unitY = $(event.target).closest('.calendar-day').height();
    }

    moveData.containerOffset = this.$grid.offset();
    moveData.containerScrollPosition = new Point(this.get$Scrollable().scrollLeft(), this.get$Scrollable().scrollTop());

    moveData.startCursorPosition = new Point(
      event.pageX - moveData.containerOffset.left + moveData.containerScrollPosition.x,
      event.pageY - moveData.containerOffset.top + moveData.containerScrollPosition.y
    );
    moveData.currentCursorPosition = moveData.startCursorPosition;

    return moveData;
  }

  toLogicalPosition(point: Point, roundingMode?: RoundingMode): Point;
  toLogicalPosition(x: number, y: number, roundingMode?: RoundingMode): Point;
  toLogicalPosition(vararg: Point | number, y?: number | RoundingMode, roundingMode?: RoundingMode): Point {
    let pixelPosition;
    if (vararg instanceof Point) {
      pixelPosition = vararg;
      roundingMode = y as RoundingMode;
    } else {
      pixelPosition = new Point(vararg, y as number);
    }

    if (this._isDay()) {
      pixelPosition.x = 0;
    }

    return new Point(
      numbers.round(pixelPosition.x / this._moveData.unitX, roundingMode),
      numbers.round(pixelPosition.y / this._moveData.unitY, roundingMode)
    );
  }

  /* -- helper ---------------------------------------------------- */

  protected _hourMinuteByDivision(number: number): { hour: number; minute: number } {
    // from division number to decimal hour.min
    number /= this.numberOfHourDivisions;
    // Separate the int from the decimal part
    let hour = Math.floor(number);
    let decPart = number - hour;

    let min = 1 / 60;
    // Round to nearest minute
    decPart = min * Math.round(decPart / min);

    let minute = Math.floor(decPart * 60);
    return {
      hour: hour,
      minute: minute
    };
  }

  protected _dayPositionByDivision(number): number {
    let hourMin = this._hourMinuteByDivision(number);
    return this._dayPosition(hourMin.hour, hourMin.minute);
  }

  protected _dayPosition(hour: number, minutes: number): number {
    // Height position in percent of total calendar

    let pos;
    if (hour < 0) {
      pos = 0; // All day event
    } else {
      pos = 100 / (24 * 60) * (hour * 60 + minutes);
    }
    return Math.round(pos * 100) / 100;
  }

  protected _format(date: Date, pattern: string): string {
    return dates.format(date, this.session.locale, pattern);
  }

  protected _sortFromTo(c1: CalendarComponent, c2: CalendarComponent): number {
    let from1 = dates.parseJsonDate(c1.fromDate);
    let from2 = dates.parseJsonDate(c2.fromDate);
    let diffFrom = dates.compare(from1, from2);
    if (diffFrom !== 0) {
      return diffFrom;
    }
    let to1 = dates.parseJsonDate(c1.toDate);
    let to2 = dates.parseJsonDate(c2.toDate);
    let diffTo = dates.compare(to1, to2);
    if (diffTo !== 0) {
      return diffTo;
    }
    let s1 = c1.item && c1.item.subject ? c1.item.subject : '';
    let s2 = c2.item && c2.item.subject ? c2.item.subject : '';
    return s1.localeCompare(s2);
  }
}
