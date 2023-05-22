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
  arrays, CalendarComponent, CalendarDescriptor, CalendarEventMap, CalendarLayout, CalendarListComponent, CalendarModel, CalendarModesMenu, CalendarSidebar, ContextMenuPopup, DateRange, dates, Device, EnumObject, EventHandler, events, GroupBox,
  HtmlComponent, InitModelOf, JsonDateRange, KeyStrokeContext, Menu, menus, numbers, objects, Point, PropertyChangeEvent, ResourcesPanel, ResourcesPanelTreeNode, RoundingMode, scout, scrollbars, strings, TreeNodeClickEvent,
  ViewportScroller, Widget, YearPanel, YearPanelDateSelectEvent
} from '../index';
import $ from 'jquery';

export type CalendarDisplayMode = EnumObject<typeof Calendar.DisplayMode>;
export type CalendarMenuType = EnumObject<typeof Calendar.MenuType>;
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

export class Calendar extends Widget implements CalendarModel {
  declare model: CalendarModel;
  declare eventMap: CalendarEventMap;
  declare self: Calendar;

  monthViewNumberOfWeeks: number;
  numberOfHourDivisions: number;
  widthPerDivision: number;
  heightPerDivision: number;
  startHour: number;
  heightPerHour: number;
  heightPerDay: number;
  spaceBeforeScrollTop: number;
  workDayIndices: number[];
  displayCondensed: boolean;
  displayMode: CalendarDisplayMode;
  components: CalendarComponent[];
  selectedComponent: CalendarComponent;
  loadInProgress: boolean;
  selectedDate: Date;
  selectorStart: Date;
  selectorEnd: Date;
  showDisplayModeSelection: boolean;
  rangeSelectionAllowed: boolean;
  calendars: CalendarDescriptor[];
  title: string;
  useOverflowCells: boolean;
  viewRange: DateRange;
  calendarToggleListWidth: number;
  calendarToggleYearWidth: number;
  menuInjectionTarget: GroupBox;
  modesMenu: CalendarModesMenu;
  menus: Menu[];
  calendarSidebar: CalendarSidebar;
  yearPanel: YearPanel;
  resourcesPanel: ResourcesPanel;
  selectedRange: DateRange;
  needsScrollToStartHour: boolean;
  defaultMenuTypes: string[];
  splitDay: boolean;

  $header: JQuery;
  $range: JQuery;
  $commands: JQuery;
  $grids: JQuery;
  $grid: JQuery;
  $topGrid: JQuery;
  $list: JQuery;
  $listContainer: JQuery;
  $listTitle: JQuery;
  $progress: JQuery;
  $headerRow1: JQuery;
  $headerRow2: JQuery;
  $title: JQuery;
  $select: JQuery;
  $window: JQuery<Window>;

  /** additional modes; should be stored in model */
  protected _showYearPanel: boolean;
  protected _showListPanel: boolean;
  protected _showResourcesPanel: boolean;

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

  /**
   * Temporary data structure to store data while mouse actions are handled
   * @internal
   */
  _moveData: CalendarMoveData;
  /** @internal */
  _rangeSelectionStarted: boolean;

  protected _mouseMoveHandler: (event: JQuery.MouseMoveEvent) => void;
  protected _mouseUpHandler: (event: JQuery.MouseUpEvent) => void;
  protected _mouseMoveRangeSelectionHandler: (event: JQuery.MouseMoveEvent) => void;
  protected _mouseUpRangeSelectionHandler: (event: JQuery.MouseUpEvent) => void;

  constructor() {
    super();

    this.monthViewNumberOfWeeks = 6;
    this.numberOfHourDivisions = 2;
    this.heightPerDivision = 30;
    this.startHour = 6;
    this.heightPerHour = this.numberOfHourDivisions * this.heightPerDivision;
    this.heightPerDay = 24 * this.heightPerHour;
    this.spaceBeforeScrollTop = 15;
    this.workDayIndices = [1, 2, 3, 4, 5]; // Workdays: Mon-Fri (Week starts at Sun in JS)
    this.components = [];
    this.displayCondensed = false;
    this.displayMode = Calendar.DisplayMode.MONTH;
    this.loadInProgress = false;
    this.selectedDate = new Date();
    this.showDisplayModeSelection = true;
    this.rangeSelectionAllowed = false;
    this.calendars = [];
    this.title = null;
    this.useOverflowCells = true;
    this.viewRange = null;
    this.calendarToggleListWidth = 270;
    this.calendarToggleYearWidth = 215;
    this.defaultMenuTypes = [Calendar.MenuType.EmptySpace];
    this.splitDay = true;

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
    this._showResourcesPanel = false;

    this._exactRange = null;

    this._listComponents = [];
    this.menuInjectionTarget = null;
    this._menuInjectionTargetMenusChangedHandler = null;

    this._moveData = null;

    this._mouseMoveHandler = this._onMouseMove.bind(this);
    this._mouseUpHandler = this._onMouseUp.bind(this);
    this._mouseMoveRangeSelectionHandler = this._onMouseMoveRangeSelection.bind(this);
    this._mouseUpRangeSelectionHandler = this._onMouseUpRangeSelection.bind(this);
    this.selectedRange = null;

    this._addWidgetProperties(['components', 'menus', 'selectedComponent']);
    this._addPreserveOnPropertyChangeProperties(['selectedComponent']);
  }

  override init(model: InitModelOf<this>) {
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

  static MenuType = {
    EmptySpace: 'Calendar.EmptySpace',
    CalendarComponent: 'Calendar.CalendarComponent'
  } as const;

  isDay(): boolean {
    return this.displayMode === Calendar.DisplayMode.DAY;
  }

  isWeek(): boolean {
    return this.displayMode === Calendar.DisplayMode.WEEK;
  }

  isMonth(): boolean {
    return this.displayMode === Calendar.DisplayMode.MONTH;
  }

  isWorkWeek(): boolean {
    return this.displayMode === Calendar.DisplayMode.WORK_WEEK;
  }

  protected override _createKeyStrokeContext(): KeyStrokeContext {
    return new KeyStrokeContext();
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this.calendarSidebar = scout.create(CalendarSidebar, {
      parent: this
    });
    this.yearPanel = this.calendarSidebar.yearPanel;
    this.resourcesPanel = this.calendarSidebar.resourcesPanel;

    this.resourcesPanel.tree.on('nodeClick', this._onCalendarTreeNodeSelected.bind(this));

    this.yearPanel.on('dateSelect', this._onYearPanelDateSelect.bind(this));
    this.modesMenu = scout.create(CalendarModesMenu, {
      parent: this,
      visible: false,
      displayMode: this.displayMode
    });
    this._setSelectedDate(this.selectedDate);
    this._setDisplayMode(this.displayMode);
    this._setMenuInjectionTarget(this.menuInjectionTarget);
    this._setCalendars(this.calendars);
    this._exactRange = this._calcExactRange();
    this.yearPanel.setViewRange(this._exactRange);
    this.viewRange = this._calcViewRange();
  }

  setCalendars(calendars: CalendarDescriptor[]) {
    this.setProperty('calendars', calendars);
  }

  protected _setCalendars(calendars: CalendarDescriptor[]) {
    this._setProperty('calendars', calendars);
    this.resourcesPanel.tree.removeAllNodes();
    this.calendars.forEach(descriptor => {
      this.resourcesPanel.tree.insertNode(scout.create(ResourcesPanelTreeNode, {
        parent: this.resourcesPanel.tree,
        calendarId: descriptor.calendarId,
        text: descriptor.name,
        checked: descriptor.visible,
        cssClass: descriptor.cssClass
      }));
    });
  }

  setSelectedDate(date: Date | string) {
    this.setProperty('selectedDate', date);
  }

  protected _setSelectedDate(date: Date | string) {
    date = dates.ensure(date);
    scout.assertParameter('selectedDate', date, Date);
    this._setProperty('selectedDate', date);
    this.yearPanel.selectDate(this.selectedDate);
  }

  protected _renderSelectedDate() {
    this._updateModel(false);
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
    this.yearPanel.setDisplayMode(this.displayMode);
    this.modesMenu.setDisplayMode(displayMode);
    if (this.isWorkWeek()) {
      // change date if selectedDate is on a weekend
      let p = this._dateParts(this.selectedDate, true);
      if (p.day > 4) {
        this.setSelectedDate(new Date(p.year, p.month, p.date - p.day + 4));
      }
    }
    this.selectedRange = null;
    this.trigger('selectedRangeChange');
  }

  protected _renderDisplayMode(oldDisplayMode?: CalendarDisplayMode) {
    if (this.rendering) {
      // only do it on property changes
      return;
    }
    this._updateModel(true);

    // only render if components have another layout
    // if (oldDisplayMode === Calendar.DisplayMode.MONTH || this.displayMode === Calendar.DisplayMode.MONTH) {
    this._renderComponents();
    this.needsScrollToStartHour = true;
    // }
  }

  protected _setViewRange(viewRange: DateRange | JsonDateRange) {
    viewRange = DateRange.ensure(viewRange);
    this._setProperty('viewRange', viewRange);
  }

  protected _setRangeSelectionAllowed(rangeSelectionAllowed: boolean) {
    this.rangeSelectionAllowed = rangeSelectionAllowed;
    if (!this.rangeSelectionAllowed) {
      this._setSelectedRange(null);
    }
  }

  protected _setSelectedRange(range: DateRange | JsonDateRange) {
    let selectedRange = DateRange.ensure(range);
    if (selectedRange && selectedRange.from && selectedRange.to) {
      this.selectorStart = new Date(selectedRange.from);
      this.selectorStart.setHours(0, this._getHours(this.selectorStart) * 60);

      this.selectorEnd = new Date(selectedRange.to);
      this.selectorEnd.setHours(0, this._getHours(this.selectorEnd) * 60 - 30);
      this._setRangeSelection();
    } else {
      this.selectorStart = null;
      this.selectorEnd = null;
      this._removeRangeSelection();
    }
    this._updateSelectedRange();
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
    this.calendarSidebar.render();

    this.$grids = this.$container.appendDiv('calendar-grids');
    this.$topGrid = this.$grids.appendDiv('calendar-top-grid');
    this.$topGrid.toggleClass('mobile', isMobile);
    this.$grid = this.$grids.appendDiv('calendar-grid');
    this.$grid.toggleClass('mobile', isMobile);

    this.$listContainer = this.$container.appendDiv('calendar-list-container');
    this.$list = this.$listContainer.appendDiv('calendar-list calendar-scrollable-components');
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
    this.$commands.appendDiv('calendar-toggle-resources')
      .on('click', this._onResourcesClick.bind(this));

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
    this._renderCalendars();
    this._renderComponents();
    this._renderSelectedComponent();
    this._renderLoadInProgress();
    this._renderDisplayMode();
  }

  protected _renderCalendars() {
    let $dayName = this.$topGrid.find('.calendar-week-header > .calendar-day-name');
    let $fullDay = this.$topGrid.find('.calendar-week-allday-container > .calendar-day');
    let $day = this.$grid.find('.calendar-week > .calendar-day');

    // Remove old calendar columns
    $dayName.find('.calendar-column').remove();
    $fullDay.find('.calendar-column').remove();
    $day.find('.calendar-column').remove();

    // Add default calendar columns
    $dayName.appendDiv('calendar-column')
      .data('calendarId', 'default');
    $fullDay.appendDiv('calendar-column')
      .data('calendarId', 'default');
    $day.appendDiv('calendar-column')
      .data('calendarId', 'default');

    // Add new calendar columns
    this.calendars.forEach(calendar => {
      $dayName.appendDiv('calendar-column')
        .data('calendarId', calendar.calendarId)
        .attr('data-calendar-name', calendar.name);
      $fullDay.appendDiv('calendar-column')
        .data('calendarId', calendar.calendarId);
      $day.appendDiv('calendar-column')
        .data('calendarId', calendar.calendarId);
    });
  }

  protected _renderComponents() {
    this.components.sort(this._sortFromTo);
    this._updateFullDayIndices();
    this.components.forEach(component => component.remove());
    this.components.forEach(component => component.render());
    scrollbars.update(this.$grid);
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
    if (this.isMonth()) {
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
    if (this.selectedComponent && this.selectedComponent._$parts[0] && this.selectedComponent._$parts[0].parent() && this.selectedComponent._$parts[0].isVisible()) {
      scrollbars.scrollTo(this.selectedComponent._$parts[0].parent(), this.selectedComponent._$parts[0], {
        animate: animate
      });
    }
  }

  protected _scrollToInitialTime(animate: boolean) {
    this.needsScrollToStartHour = false;
    if (!this.isMonth()) {
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

  /** @internal */
  _onPreviousClick() {
    this._navigateDate(Calendar.Direction.BACKWARD);
  }

  /** @internal */
  _onNextClick() {
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
    this._updateModel(false);
  }

  protected _calcSelectedDate(direction: CalendarDirection): Date {
    // noinspection UnnecessaryLocalVariableJS
    let p = this._dateParts(this.selectedDate),
      dayOperand = direction,
      weekOperand = direction * 7,
      monthOperand = direction;

    if (this.isDay()) {
      return new Date(p.year, p.month, p.date + dayOperand);
    }
    if (this.isWeek() || this.isWorkWeek()) {
      return new Date(p.year, p.month, p.date + weekOperand);
    }
    if (this.isMonth()) {
      return dates.shift(this.selectedDate, 0, monthOperand, 0);
    }
  }

  protected _updateModel(animate: boolean) {
    this._exactRange = this._calcExactRange();
    this.yearPanel.setViewRange(this._exactRange);
    this.viewRange = this._calcViewRange();
    this.trigger('modelChange');
    this._updateScreen(true, animate);
  }

  /**
   * Calculates exact date range of displayed components based on selected-date.
   */
  protected _calcExactRange(): DateRange {
    let from, to,
      p = this._dateParts(this.selectedDate, true);

    if (this.isDay()) {
      from = new Date(p.year, p.month, p.date);
      to = new Date(p.year, p.month, p.date);
    } else if (this.isWeek()) {
      from = new Date(p.year, p.month, p.date - p.day);
      to = new Date(p.year, p.month, p.date - p.day + 6);
    } else if (this.isMonth()) {
      from = new Date(p.year, p.month, 1);
      to = new Date(p.year, p.month + 1, 0);
    } else if (this.isWorkWeek()) {
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

  protected _onTodayClick(event: JQuery.ClickEvent) {
    this.selectedDate = new Date();
    this._updateModel(false);
  }

  protected _onDisplayModeClick(event: JQuery.ClickEvent) {
    let displayMode = $(event.target).data('mode');
    this.setDisplayMode(displayMode);
  }

  protected _onYearClick(event: JQuery.ClickEvent) {
    this._showYearPanel = !this._showYearPanel;
    this._updateScreen(true, true);
  }

  protected _onListClick(event: JQuery.ClickEvent) {
    this._showListPanel = !this._showListPanel;
    this._updateScreen(false, true);
  }

  protected _onResourcesClick(event: JQuery.ClickEvent) {
    this._showResourcesPanel = !this._showResourcesPanel;
    this._updateScreen(false, true);
  }

  protected _onDayMouseDown(withTime: boolean, event: JQuery.MouseDownEvent) {
    let selectedDate = new Date($(event.delegateTarget).data('date')),
      timeChanged = false;
    if (withTime && (this.isDay() || this.isWeek() || this.isWorkWeek())) {
      let seconds = this._getSelectedSeconds(event);
      if (seconds < 60 * 60 * 24) {
        selectedDate.setSeconds(seconds);
        timeChanged = true;
      }
      this._startRangeSelection(event);
    }
    this._setSelection(selectedDate, null, false, timeChanged);
  }

  protected _getSelectedDate(event: JQuery.MouseEventBase): Date {
    let date = null;
    if ($(event.target).hasClass('calendar-day')) {
      date = $(event.target).data('date');
    } else if ($(event.target).hasClass('calendar-component')
      || $(event.target).parents('.calendar-component').length > 0
      || $(event.target).hasClass('calendar-range-selector')) {
      date = $(event.target).closest('.calendar-day').data('date');
    }
    if (date) {
      return new Date(date);
    }
    return null;
  }

  protected _getSelectedSeconds(event: JQuery.MouseEventBase): number {
    // @ts-expect-error
    let y = event.originalEvent.layerY;
    if ($(event.target).hasClass('calendar-component') || $(event.target).parents('.calendar-component').length > 0) {
      y += $(event.target).closest('.calendar-component').position().top;
    } else if ($(event.target).hasClass('calendar-range-selector')) {
      y += $(event.target).position().top;
    }
    return Math.floor(y / this.heightPerDivision) / this.numberOfHourDivisions * 60 * 60;
  }

  protected _getSelectedDateTime(event: JQuery.MouseEventBase): Date {
    let selectedDate = this._getSelectedDate(event);
    if (selectedDate && (this.isDay() || this.isWeek() || this.isWorkWeek())) {
      let seconds = this._getSelectedSeconds(event);
      if (seconds < 60 * 60 * 24) {
        selectedDate.setSeconds(seconds);
      }
    }
    return selectedDate;
  }

  /**
   * @param selectedComponent may be null when a day is selected
   */
  protected _setSelection(selectedDate: Date, selectedComponent: CalendarComponent, updateScrollPosition: boolean, timeChanged: boolean) {
    let changed = false;
    let dateChanged = dates.compareDays(this.selectedDate, selectedDate) !== 0;

    // selected date
    if (dateChanged || timeChanged) {
      changed = true;
      if (dateChanged) {
        $('.calendar-day', this.$container).each((index, element) => {
          let $day = $(element),
            date = $day.data('date');
          if (!date || dates.compareDays(date, this.selectedDate) === 0) {
            $day.select(false); // de-select old date
          } else if (dates.compareDays(date, selectedDate) === 0) {
            $day.select(true); // select new date
          }
        });
      }
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
      if (dateChanged) {
        this._updateListPanel();
      }
      if (updateScrollPosition) {
        this._updateScrollPosition(false, true);
      }
    }

    if (this._showYearPanel) {
      this.yearPanel.selectDate(this.selectedDate);
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
      this.yearPanel.selectDate(this.selectedDate);
    }

    this._updateListPanel();
    this._updateScrollbars(this.$grid, animate);
    if (updateTopGrid && !this.isMonth()) {
      this._updateTopGrid();
    }

    this._setSelectedRange(this.selectedRange);
  }

  layoutSize(animate?: boolean) {
    // reset animation sizes
    $('div', this.$container).removeData(['new-width', 'new-height']);

    this.$grids.toggleClass('calendar-grids-month', this.isMonth());

    // init vars (Selected: Day)
    let $selected = $('.selected', this.$grid),
      $topSelected = $('.selected', this.$topGrid),
      containerW = this.$container.width(),
      gridH = this.$grid.height(),
      gridPaddingX = this.$grid.innerWidth() - this.$grid.width(),
      gridW = containerW - gridPaddingX;

    // show or hide calendar sidebar
    $('.calendar-toggle-year', this.$commands).select(this._showYearPanel);
    $('.calendar-toggle-resources', this.$commands).select(this._showResourcesPanel);
    this.yearPanel.setVisible(this._showYearPanel);
    this.resourcesPanel.setVisible(this._showResourcesPanel);
    if (this._showYearPanel || this._showResourcesPanel) {
      this.calendarSidebar.$container.data('new-width', this.calendarToggleYearWidth);
      gridW -= this.calendarToggleYearWidth;
      containerW -= this.calendarToggleYearWidth;
      this.calendarSidebar.invalidateLayoutTree(false);
    } else {
      this.calendarSidebar.$container.data('new-width', 0);
      this.calendarSidebar.invalidateLayoutTree(false);
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

    if (this.isDay() || this.isWeek() || this.isWorkWeek()) {
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
    if (this.isDay()) {
      $('.calendar-day-name, .calendar-day', this.$topGrid).data('new-width', 0);
      $('.calendar-day', this.$grid).data('new-width', 0);
      $('.calendar-day-name:nth-child(' + ($topSelected.index() + 1) + ')', this.$topGrid)
        .data('new-width', contentW);
      $('.calendar-day:nth-child(' + ($topSelected.index() + 1) + ')', this.$topGrid).data('new-width', contentW);
      $('.calendar-day:nth-child(' + ($selected.index() + 1) + ')', this.$grid).data('new-width', contentW);
      this.widthPerDivision = contentW;
    } else if (this.isWorkWeek()) {
      this.$topGrid.find('.calendar-day-name').data('new-width', 0);
      this.$grids.find('.calendar-day').data('new-width', 0);
      let newWidthWorkWeek = Math.round(contentW / this.workDayIndices.length);
      this.$topGrid.find('.calendar-day-name').slice(0, 5).data('new-width', newWidthWorkWeek);
      this.$topGrid.find('.calendar-day').slice(0, 5).data('new-width', newWidthWorkWeek);
      $('.calendar-day:nth-child(-n+6)', this.$grid).data('new-width', newWidthWorkWeek);
      this.widthPerDivision = newWidthWorkWeek;
    } else if (this.isMonth() || this.isWeek()) {
      let newWidthMonthOrWeek = Math.round(contentW / 7);
      this.$grids.find('.calendar-day').data('new-width', newWidthMonthOrWeek);
      this.$topGrid.find('.calendar-day-name').data('new-width', newWidthMonthOrWeek);
      this.widthPerDivision = newWidthMonthOrWeek;
    }

    // layout calendar columns
    let columnWidth = 0;
    if (this.isDay() && this.splitDay) {
      columnWidth = Math.round(contentW / (this.calendars.filter(c => c.visible).length + 1));
    } else if (this.isWorkWeek()) {
      columnWidth = Math.round(contentW / this.workDayIndices.length);
    } else {
      columnWidth = Math.round(contentW / 7);
    }

    if (this.isDay()) {
      // Set size to 0 for all
      $('.calendar-column', this.$grids).data('new-width', 0);

      // Resize visible columns of selected day
      $('.calendar-day-name:nth-child(' + ($topSelected.index() + 1) + ')', this.$topGrid)
        .add($('.calendar-day:nth-child(' + ($topSelected.index() + 1) + ')', this.$grids))
        .find('.calendar-column')
        .filter((i, e) => {
          let id = $(e).data('calendarId');
          return id === 'default' || this.calendars.find(cal => cal.calendarId === id).visible;
        }).data('new-width', columnWidth);
    } else {
      // Set size 0 for all calendar columns
      $('.calendar-column', this.$grids).data('new-width', 0);

      // Full size for default column
      $('.calendar-column', this.$grids)
        .filter((i, e) => $(e).data('calendarId') === 'default')
        .data('new-width', columnWidth);
    }

    // layout components
    if (this.isMonth()) {
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

    if (this.isDay()) {
      weekdayWidth /= 1;
    } else if (this.isWorkWeek()) {
      weekdayWidth /= this.workDayIndices.length;
    } else if (this.isWeek()) {
      weekdayWidth /= 7;
    } else if (this.isMonth()) {
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
    const fullDayComponents = this.components.filter(component => component.fullDay);
    this._updateFullDayIndices(fullDayComponents);
    // first remove all components and add them from scratch
    fullDayComponents.forEach(component => component.remove());
    fullDayComponents.forEach(component => component.render());
    this._updateScrollbars(this.$topGrid, false);
    scrollbars.update(this.$grid);
  }

  protected _updateFullDayIndices(fullDayComponents?: CalendarComponent[]) {
    if (!fullDayComponents) {
      fullDayComponents = this.components.filter(component => component.fullDay);
    }
    fullDayComponents.sort(this._sortFromTo);

    const {from, to} = this._exactRange;
    const usedIndicesMap = new Map();
    let maxComponentsPerDay = 0;

    for (const component of fullDayComponents) {
      component.fullDayIndex = -1;
      if (component.coveredDaysRange.to < from || component.coveredDaysRange.from > to) {
        // component is not in range
        continue;
      }

      let date = component.coveredDaysRange.from;
      if (date < from) {
        date = from;
      }

      let usedIndices = arrays.ensure(usedIndicesMap.get(date.valueOf()));

      // get the first unused index
      // create [0, 1, 2, ..., maxIndex, maxIndex + 1] remove the used indices
      // => the minimum of the remaining array is the first unused index
      const maxIndex = usedIndices.length ? arrays.max(usedIndices) : 0;
      const indexCandidates = arrays.init(maxIndex + 2, null).map((elem, idx) => idx);
      arrays.removeAll(indexCandidates, usedIndices);
      const index = arrays.min(indexCandidates);
      component.fullDayIndex = index;

      // mark the index as used for all dates of the components range
      // none of these indices can be used already due to the order of the components
      while (date <= component.coveredDaysRange.to && date <= to) {
        usedIndices.push(index);
        usedIndicesMap.set(date.valueOf(), usedIndices);

        date = dates.shift(date, 0, 0, 1);
        usedIndices = arrays.ensure(usedIndicesMap.get(date.valueOf()));
      }

      maxComponentsPerDay = Math.max(index + 1, maxComponentsPerDay);
    }

    this.$grids.css('--full-day-components', maxComponentsPerDay);
  }

  layoutYearPanel() {
    if (this._showYearPanel) {
      scrollbars.update(this.yearPanel.$yearList);
      this.yearPanel._scrollYear();
    }
  }

  layoutLabel() {
    let text, $dates, $topGridDates,
      exFrom = this._exactRange.from,
      exTo = this._exactRange.to;

    // set range text
    if (this.isDay()) {
      text = this._format(exFrom, 'd. MMMM yyyy');
    } else if (this.isWorkWeek() || this.isWeek()) {
      let toText = this.session.text('ui.to');
      if (exFrom.getMonth() === exTo.getMonth()) {
        text = strings.join(' ', this._format(exFrom, 'd.'), toText, this._format(exTo, 'd. MMMM yyyy'));
      } else if (exFrom.getFullYear() === exTo.getFullYear()) {
        text = strings.join(' ', this._format(exFrom, 'd. MMMM'), toText, this._format(exTo, 'd. MMMM yyyy'));
      } else {
        text = strings.join(' ', this._format(exFrom, 'd. MMMM yyyy'), toText, this._format(exTo, 'd. MMMM yyyy'));
      }

    } else if (this.isMonth()) {
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
        if (!this.isMonth()) {
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
    if (!this.isMonth()) {
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
    if (!this.isMonth()) {
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
    this._updateModel(false);
  }

  protected _updateListPanel() {
    if (this._showListPanel) {
      scrollbars.uninstall(this.$list, this.session);

      // remove old list-components
      this._listComponents.forEach(listComponent => listComponent.remove());
      this._listComponents = [];
      this._renderListPanel();
      scrollbars.install(this.$list, {
        parent: this,
        session: this.session,
        axis: 'y'
      });
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

  /** @internal */
  _selectedComponentChanged(component: CalendarComponent, partDay: Date, updateScrollPosition: boolean) {
    this._setSelection(partDay, component, updateScrollPosition, false);
  }

  protected _onDayContextMenu(event: JQuery.ContextMenuEvent) {
    this._showContextMenu(event, Calendar.MenuType.EmptySpace);
  }

  /** @internal */
  _showContextMenu(event: JQuery.ContextMenuEvent, allowedType: string) {
    event.preventDefault();
    event.stopPropagation();

    let func = function func(event: JQuery.ContextMenuEvent, allowedType: string) {
      if (!this.rendered || !this.attached) { // check needed because function is called asynchronously
        return;
      }
      let filteredMenus = menus.filter(this.menus, [allowedType], {
          onlyVisible: true,
          defaultMenuTypes: this.defaultMenuTypes
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

  protected _onCalendarTreeNodeSelected(event: TreeNodeClickEvent) {
    let node = event.node as ResourcesPanelTreeNode;
    if (!node.calendarId) {
      return;
    }
    let calendar = this.calendars.find(calendar => calendar.calendarId === node.calendarId);
    if (!calendar) {
      return;
    }
    calendar.visible = node.checked;
    this.trigger('calendarVisibilityChange', {
      calendarId: node.calendarId,
      visible: node.checked
    });
    this.components
      .filter(comp => comp.item.calendarId === node.calendarId)
      .forEach(comp => comp.setVisible(node.checked));
    this.layoutSize(true);
  }

  /* -- components, arrangement------------------------------------ */

  protected _arrangeComponents() {
    let k: number, c: number, j: number,
      $day: JQuery, $columns: JQuery, $allChildren: JQuery, $children: JQuery,
      $scrollableContainer: JQuery, dayComponents: CalendarComponent[], day: Date;

    let $days = $('.calendar-day', this.$grid);
    // Main (Bottom) grid: Iterate over days
    for (k = 0; k < $days.length; k++) {
      $day = $days.eq(k);
      day = $day.data('date');
      $columns = $day.children('.calendar-column');
      $allChildren = $columns.find('.calendar-component');

      // Remove old element containers
      $scrollableContainer = $columns.children('.calendar-scrollable-components');
      if ($scrollableContainer.length > 0) {
        scrollbars.uninstall($scrollableContainer, this.session);
        $scrollableContainer.remove();
      }

      if (this.isMonth() && $allChildren.length > 0) {
        $scrollableContainer = $day.appendDiv('calendar-scrollable-components');

        for (j = 0; j < $allChildren.length; j++) {
          let $child = $allChildren.eq(j);
          // non-tasks (communications) are distributed manually
          // within the parent container in all views except the monthly view.
          if (!this.isMonth() && !$child.hasClass('component-task')) {
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

      for (c = 0; c < $columns.length; c++) {
        $children = $columns.eq(c).children('.calendar-component:not(.component-task)');

        if (this.isMonth() && $children.length > 2) {
          $day.addClass('many-items');
        } else if (!this.isMonth() && $children.length > 1) {
          // logical placement
          dayComponents = this._getComponents($children);
          this._arrange(dayComponents, day);

          // screen placement
          this._arrangeComponentSetPlacement($children, day);
        }
      }
    }

    if (this.isMonth()) {
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

  /** @internal */
  _sort(components: CalendarComponent[]) {
    components.sort(this._sortFromTo);
  }

  /**
   * Arrange components (stack width, stack index) per day
   * @internal
   */
  _arrange(components: CalendarComponent[], day: Date) {
    let columns: CalendarComponent[] = [],
      key = day + '';

    // ordered by from, to
    this._sort(components);

    // clear existing placement
    for (let i = 0; i < components.length; i++) {
      let c = components[i];
      if (!c.stack) {
        c.stack = {};
      }
      c.stack[this._calculateStackKey(day, c.item.calendarId)] = {};
    }

    for (let i = 0; i < components.length; i++) {
      let c = components[i];
      let r = c.getPartDayPosition(day); // Range [from,to]
      key = this._calculateStackKey(day, c.item.calendarId);

      // reduce number of columns, if all components end before this one
      if (columns.length > 0 && this._allEndBefore(columns, r.from, day)) {
        columns = [];
      }

      // replace an component that ends before and can be replaced
      let k = this._findReplacableColumn(columns, r.from, day);

      // insert
      if (k >= 0) {
        columns[k] = c;
        c.stack[key].x = k;
      } else {
        columns.push(c);
        c.stack[key].x = columns.length - 1;
      }

      // update stackW
      for (let j = 0; j < columns.length; j++) {
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

  protected _endsBefore(component: CalendarComponent, pos: number, day: Date): boolean {
    return component.getPartDayPosition(day).to <= pos;
  }

  protected _arrangeComponentSetPlacement($children: JQuery, day: Date) {
    let i, $child, component: CalendarComponent, stack;

    // loop and place based on data
    for (i = 0; i < $children.length; i++) {
      $child = $children.eq(i);
      component = $child.data('component');
      stack = component.stack[this._calculateStackKey(day, component.item.calendarId)];

      // make last element smaller
      $child
        .css('width', 100 / stack.w + '%')
        .css('left', stack.x * 100 / stack.w + '%');
    }
  }

  protected _calculateStackKey(date: Date, calendarId: number): string {
    if (!this.isDay() || calendarId === 0) {
      return 'default';
    }
    return date + '' + calendarId ? calendarId.toString() : 'default';
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
    if (!component || !component.draggable || (!this.isMonth() && component.fullDay) || component._$parts.length > 1) {
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

    let $firstPart = component._$parts[0];
    this._moveData.logicalX = $firstPart.closest('.calendar-day').data().day;
    this._moveData.logicalY = Math.round($firstPart.position().top) / this.heightPerDivision;
    if (this.isMonth()) {
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
    let minY = this.isMonth() ? 1 : 0;
    let maxX = this.isWorkWeek() ? this.workDayIndices.length : 7;
    let maxY = this.isMonth() ?
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
        if (this.isMonth()) {
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

  protected _setComponentLogicalPosition(component: CalendarComponent, logicalPosition: Point) {
    let $firstPart = component._$parts[0];
    let currDay = $firstPart.closest('.calendar-day').data('day');
    let currWeek = $firstPart.closest('.calendar-day').data('week');

    if (component.rendered) {
      if (this.isMonth()) {
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
    if (this.isMonth()) {
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

    if (this.isDay()) {
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

  protected _dayPositionByDivision(number: number): number {
    let hourMin = this._hourMinuteByDivision(number);
    return this._dayPosition(hourMin.hour, hourMin.minute);
  }

  /** @internal */
  _dayPosition(hour: number, minutes: number): number {
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

  protected _startRangeSelection(event: JQuery.MouseDownEvent) {
    if (!this.rangeSelectionAllowed || this._rangeSelectionStarted || Device.get().type === Device.Type.MOBILE) {
      return;
    }

    // Ignore right mouse button clicks within current selection
    let selectedDateTime = this._getSelectedDateTime(event);
    if (event.which === 3 &&
      this.selectedRange && selectedDateTime &&
      dates.compare(selectedDateTime, this.selectedRange.from) >= 0 &&
      dates.compare(selectedDateTime, this.selectedRange.to) < 0) {
      return;
    }

    // Ignore clicks to calendar components, this should open the tooltip
    if (!$(event.target).hasClass('calendar-range-selector') && !$(event.target).hasClass('calendar-day')) {
      return;
    }

    // init selector
    this.selectorStart = this._getSelectedDateTime(event);
    if (!this.selectorStart) {
      return;
    }
    this.selectorEnd = new Date(this.selectorStart);
    if (!this.selectorStart || !this.selectorEnd) {
      return;
    }

    this.$window
      .off('mousemove', this._mouseMoveRangeSelectionHandler)
      .off('mouseup', this._mouseUpRangeSelectionHandler)
      .on('mousemove', this._mouseMoveRangeSelectionHandler)
      .on('mouseup', this._mouseUpRangeSelectionHandler);

    // draw
    this._setRangeSelection();
    this._rangeSelectionStarted = true;
  }

  protected _findDayInCalendar(selectedDate: Date): JQuery {
    let $foundDay: JQuery = null;
    $('.calendar-day', this.$container).each((index, element) => {
      let $day = $(element),
        date = $day.data('date');
      if (dates.compareDays(date, selectedDate) === 0) {
        $foundDay = $day;
      }
    });
    return $foundDay;
  }

  protected _setRangeSelection() {
    let selectorFirst: Date, selectorLast: Date;

    if (!this.selectorStart || !this.selectorEnd) {
      return;
    }

    this._removeRangeSelection();

    if (dates.compare(this.selectorStart, this.selectorEnd) > 0) {
      selectorFirst = this.selectorEnd;
      selectorLast = this.selectorStart;
    } else {
      selectorFirst = this.selectorStart;
      selectorLast = this.selectorEnd;
    }

    let numberOfDays = dates.compareDays(selectorLast, selectorFirst) + 1;
    if (numberOfDays === 1) {
      this._appendCalendarRangeSelection(selectorFirst, selectorFirst, selectorLast);
    } else if (numberOfDays > 1) {
      this._appendCalendarRangeSelection(selectorFirst, selectorFirst, null);
      for (let i = 1; i < numberOfDays - 1; i++) {
        let day = new Date(selectorFirst);
        day.setDate(day.getDate() + i);
        this._appendCalendarRangeSelection(day, null, null);
      }
      this._appendCalendarRangeSelection(selectorLast, null, selectorLast);
    }
  }

  protected _removeRangeSelection() {
    $('.calendar-range-selector').remove();
  }

  protected _appendCalendarRangeSelection(date: Date, fromTime: Date, toTime: Date) {
    let $parent = this._findDayInCalendar(date);
    if (!$parent) {
      return;
    }

    // top and height
    let startPosition = fromTime ? this._dayPosition(this._getHours(fromTime), 0) : 0;
    let endPosition = toTime ? this._dayPosition(this._getHours(toTime) + 0.5, 0) : 100;

    $parent.appendDiv('calendar-range-selector')
      .css('top', startPosition + '%')
      .css('height', endPosition - startPosition + '%');
  }

  protected _getHours(date: Date): number {
    // round to 0.5h
    return Math.round((date.getHours() + date.getMinutes() / 60) * 2) / 2;
  }

  protected _onMouseMoveRangeSelection(event: JQuery.MouseMoveEvent) {
    let selectorEndTime = this._getSelectedDateTime(event);
    if (selectorEndTime) {
      this.selectorEnd = selectorEndTime;
    }

    this._setRangeSelection();
  }

  protected _onMouseUpRangeSelection(event: JQuery.MouseUpEvent) {
    if (this._mouseMoveRangeSelectionHandler) {
      this.$window.off('mousemove', this._mouseMoveRangeSelectionHandler);
    }
    if (this._mouseUpRangeSelectionHandler) {
      this.$window.off('mouseup', this._mouseUpRangeSelectionHandler);
    }
    if (!this._rangeSelectionStarted) {
      return;
    }
    this._rangeSelectionStarted = false;
    if (this.rendered) {
      this._setRangeSelection();
      this._updateSelectedRange();
    }
  }

  protected _updateSelectedRange() {
    let start: Date, end: Date;

    if (this.selectorStart && this.selectorEnd) {
      if (dates.compare(this.selectorStart, this.selectorEnd) > 0) {
        start = new Date(this.selectorEnd);
        end = new Date(this.selectorStart);
      } else {
        start = new Date(this.selectorStart);
        end = new Date(this.selectorEnd);
      }

      start.setHours(0, this._getHours(start) * 60);
      end.setHours(0, this._getHours(end) * 60 + 30);

      this.selectedRange = new DateRange(start, end);
    } else {
      this.selectedRange = null;
    }
    this.trigger('selectedRangeChange');
  }
}
