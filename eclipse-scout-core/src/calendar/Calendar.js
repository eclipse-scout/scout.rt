/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {CalendarComponent, CalendarLayout, CalendarListComponent, DateRange, dates, HtmlComponent, KeyStrokeContext, menus as menus_1, objects, scout, scrollbars, strings, Widget} from '../index';
import * as $ from 'jquery';

export default class Calendar extends Widget {

  constructor() {
    super();

    this.monthViewNumberOfWeeks = 6;
    this.numberOfHourDivisions = 4;
    this.heightPerDivision = 40;
    this.startHour = 6;
    this.workDayIndices = [1, 2, 3, 4, 5]; // Workdays: Mon-Fri (Week starts at Sun in JS)
    this.components = [];
    this.displayMode;
    this.displayCondensed;
    this.loadInProgress;
    this.selectedDate;
    this.showDisplayModeSelection = true;
    this.title;
    this.useOverflowCells = true;
    this.viewRange;
    this.needsScrollToStartHour = true;
    this.calendarToggleListWidth = 270;
    this.calendarToggleYearWidth = 215;

    // main elements
    this.$container;
    this.$header;
    this.$range;
    this.$commands;
    this.$grids;
    this.$grid;
    this.$topGrid;
    this.$list;
    this.$progress;

    // additional modes; should be stored in model
    this._showYearPanel = false;
    this._showListPanel = false;

    /**
     * The narrow view range is different from the regular view range.
     * It contains only dates that exactly match the requested dates,
     * the regular view range contains also dates from the first and
     * next month. The exact range is not sent to the server.
     */
    this._exactRange;

    /**
     * When the list panel is shown, this list contains the scout.CalenderListComponent
     * items visible on the list.
     */
    this._listComponents = [];

    this._addWidgetProperties(['components', 'menus', 'selectedComponent']);
  }

  init(model, session, register) {
    super.init(model, session, register);
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
  };

  /**
   * Used as a multiplier in date calculations back- and forward (in time).
   */
  static Direction = {
    BACKWARD: -1,
    FORWARD: 1
  };

  _isDay() {
    return this.displayMode === Calendar.DisplayMode.DAY;
  }

  _isWeek() {
    return this.displayMode === Calendar.DisplayMode.WEEK;
  }

  _isMonth() {
    return this.displayMode === Calendar.DisplayMode.MONTH;
  }

  _isWorkWeek() {
    return this.displayMode === Calendar.DisplayMode.WORK_WEEK;
  }

  /**
   * @override
   */
  _createKeyStrokeContext() {
    return new KeyStrokeContext();
  }

  _init(model) {
    super._init(model);
    this._yearPanel = scout.create('YearPanel', {
      parent: this
    });
    this._yearPanel.on('dateSelect', this._onYearPanelDateSelect.bind(this));
    this.modesMenu = scout.create('CalendarModesMenu', {
      parent: this,
      visible: false,
      displayMode: this.displayMode
    });
    this._setSelectedDate(model.selectedDate);
    this._setDisplayMode(model.displayMode);
    this._exactRange = this._calcExactRange();
    this._yearPanel.setViewRange(this._exactRange);
    this.viewRange = this._calcViewRange();
  }

  setSelectedDate(date) {
    this.setProperty('selectedDate', date);
  }

  _setSelectedDate(date) {
    date = dates.ensure(date);
    this._setProperty('selectedDate', date);
    this._yearPanel.selectDate(this.selectedDate);
  }

  setDisplayMode(displayMode) {
    if (objects.equals(this.displayMode, displayMode)) {
      return;
    }
    var oldDisplayMode = this.displayMode;
    this._setDisplayMode(displayMode);
    if (this.rendered) {
      this._renderDisplayMode(oldDisplayMode);
    }
  }

  _setDisplayMode(displayMode) {
    this._setProperty('displayMode', displayMode);
    this._yearPanel.setDisplayMode(this.displayMode);
    this.modesMenu.setDisplayMode(displayMode);
    if (this._isWorkWeek()) {
      // change date if selectedDate is on a weekend
      var p = this._dateParts(this.selectedDate, true);
      if (p.day > 4) {
        this.setSelectedDate(new Date(p.year, p.month, p.date - p.day + 4));
      }
    }
  }

  _renderDisplayMode(oldDisplayMode) {
    if (this.rendering) {
      // only do it on property changes
      return;
    }
    this._updateModel(true);

    // only render if components have another layout
    if (oldDisplayMode === Calendar.DisplayMode.MONTH || this.displayMode === Calendar.DisplayMode.MONTH) {
      this._renderComponents();
      this.needsScrollToStartHour = true;
    }
  }

  _setViewRange(viewRange) {
    viewRange = DateRange.ensure(viewRange);
    this._setProperty('viewRange', viewRange);
  }

  _setMenus(menus) {
    this._setProperty('menus', menus);
  }

  _render() {
    this.$container = this.$parent.appendDiv('calendar');

    var layout = new CalendarLayout(this);
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(layout);

    // main elements
    this.$header = this.$container.appendDiv('calendar-header');
    this.$headerRow1 = this.$header.appendDiv('calendar-header-row first');
    this.$headerRow2 = this.$header.appendDiv('calendar-header-row last');
    this._yearPanel.render();

    this.$grids = this.$container.appendDiv('calendar-grids');
    this.$topGrid = this.$grids.appendDiv('calendar-top-grid');
    this.$grid = this.$grids.appendDiv('calendar-grid');

    this.$list = this.$container.appendDiv('calendar-list-container').appendDiv('calendar-list');
    this.$listTitle = this.$list.appendDiv('calendar-list-title');

    // header contains range, title and commands. On small screens title will be moved to headerRow2
    this.$range = this.$headerRow1.appendDiv('calendar-range');
    this.$range.appendDiv('calendar-previous').click(this._onPreviousClick.bind(this));
    this.$range.appendDiv('calendar-today', this.session.text('ui.CalendarToday')).click(this._onTodayClick.bind(this));
    this.$range.appendDiv('calendar-next').click(this._onNextClick.bind(this));

    // title
    this.$title = this.$headerRow1.appendDiv('calendar-title');
    this.$select = this.$title.appendDiv('calendar-select');
    this.$progress = this.$title.appendDiv('busyindicator-label');

    // commands
    this.$commands = this.$headerRow1.appendDiv('calendar-commands');
    this.$commands.appendDiv('calendar-mode first', this.session.text('ui.CalendarDay')).attr('data-mode', Calendar.DisplayMode.DAY).click(this._onDisplayModeClick.bind(this));
    this.$commands.appendDiv('calendar-mode', this.session.text('ui.CalendarWorkWeek')).attr('data-mode', Calendar.DisplayMode.WORK_WEEK).click(this._onDisplayModeClick.bind(this));
    this.$commands.appendDiv('calendar-mode', this.session.text('ui.CalendarWeek')).attr('data-mode', Calendar.DisplayMode.WEEK).click(this._onDisplayModeClick.bind(this));
    this.$commands.appendDiv('calendar-mode last', this.session.text('ui.CalendarMonth')).attr('data-mode', Calendar.DisplayMode.MONTH).click(this._onDisplayModeClick.bind(this));
    this.modesMenu.render(this.$commands);
    this.$commands.appendDiv('calendar-toggle-year').click(this._onYearClick.bind(this));
    this.$commands.appendDiv('calendar-toggle-list').click(this._onListClick.bind(this));

    // Append the top grid (day/week views)
    var $weekHeader = this.$topGrid.appendDiv('calendar-week-header');
    $weekHeader.appendDiv('calendar-week-name');
    for (var dayTop = 0; dayTop < 7; dayTop++) {
      $weekHeader.appendDiv('calendar-day-name')
        .data('day', dayTop);
    }

    this.$topGrid.appendDiv('calendar-week-task').attr('data-axis-name', this.session.text('ui.CalendarDay'));
    var $weekTopGridDays = this.$topGrid.appendDiv('calendar-week-allday-container');
    $weekTopGridDays.appendDiv('calendar-week-name');

    var dayContextMenuCallback = this._onDayContextMenu.bind(this);
    for (var dayBottom = 0; dayBottom < 7; dayBottom++) {
      $weekTopGridDays.appendDiv('calendar-day')
        .data('day', dayBottom)
        .on('contextmenu', dayContextMenuCallback);
    }

    for (var w = 1; w < 7; w++) {
      var $w = this.$grid.appendDiv('calendar-week');
      for (var d = 0; d < 8; d++) {
        var $d = $w.appendDiv();
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
    var mousedownCallback = this._onDayMouseDown.bind(this);
    this.$grids.find('.calendar-day').on('mousedown', mousedownCallback);

    this._updateScreen(false);
  }

  _renderProperties() {
    super._renderProperties();
    this._renderComponents();
    this._renderSelectedComponent();
    this._renderLoadInProgress();
    this._renderDisplayMode();
  }

  _renderComponents() {
    this.components.sort(this._sortFromTo);
    this.components.forEach(function(component) {
      component.remove();
      component.render();
    });

    this._arrangeComponents();
    this._updateListPanel();
  }

  _renderSelectedComponent() {
    if (this.selectedComponent) {
      this.selectedComponent.setSelected(true);
    }
  }

  _renderLoadInProgress() {
    this.$progress.setVisible(this.loadInProgress);
  }

  scrollToInitialTime() {
    if (!this.rendered) {
      // Execute delayed because table may be not layouted yet
      this.session.layoutValidator.schedulePostValidateFunction(this._scrollToInitialTime.bind(this));
    } else {
      this._scrollToInitialTime();
    }
  }

  _scrollToInitialTime() {
    this.needsScrollToStartHour = false;
    if (!this._isMonth()) {
      var $scrollables = this.$grid;
      var scrollTargetTop = this.numberOfHourDivisions * this.heightPerDivision * this.startHour;

      for (var k = 0; k < $scrollables.length; k++) {
        var $scrollable = $scrollables.eq(k);
        scrollbars.scrollTop($scrollable, scrollTargetTop);
      }
    }
  }

  /* -- basics, events -------------------------------------------- */

  _onPreviousClick(event) {
    this._navigateDate(Calendar.Direction.BACKWARD);
  }

  _onNextClick(event) {
    this._navigateDate(Calendar.Direction.FORWARD);
  }

  _dateParts(date, modulo) {
    var parts = {
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

  _navigateDate(direction) {
    this.selectedDate = this._calcSelectedDate(direction);
    this._updateModel(false);
  }

  _calcSelectedDate(direction) {
    var p = this._dateParts(this.selectedDate),
      dayOperand = direction,
      weekOperand = direction * 7,
      monthOperand = direction;

    if (this._isDay()) {
      return new Date(p.year, p.month, p.date + dayOperand);
    } else if (this._isWeek() || this._isWorkWeek()) {
      return new Date(p.year, p.month, p.date + weekOperand);
    } else if (this._isMonth()) {
      return dates.shift(this.selectedDate, 0, monthOperand, 0);
    }
  }

  _updateModel(animate) {
    this._exactRange = this._calcExactRange();
    this._yearPanel.setViewRange(this._exactRange);
    this.viewRange = this._calcViewRange();
    this.trigger('modelChange');
    this._updateScreen(animate);
  }

  /**
   * Calculates exact date range of displayed components based on selected-date.
   */
  _calcExactRange() {
    var from, to,
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
  _calcViewRange() {
    var viewFrom = calcViewFromDate(this._exactRange.from),
      viewTo = calcViewToDate(viewFrom);
    return new DateRange(viewFrom, viewTo);

    function calcViewFromDate(fromDate) {
      var i, tmpDate = new Date(fromDate.valueOf());
      for (i = 0; i < 42; i++) {
        tmpDate.setDate(tmpDate.getDate() - 1);
        if ((tmpDate.getDay() === 1) && tmpDate.getMonth() !== fromDate.getMonth()) {
          return tmpDate;
        }
      }
      throw new Error('failed to calc viewFrom date');
    }

    function calcViewToDate(fromDate) {
      var i, tmpDate = new Date(fromDate.valueOf());
      for (i = 0; i < 42; i++) {
        tmpDate.setDate(tmpDate.getDate() + 1);
      }
      return tmpDate;
    }
  }

  _onTodayClick(event) {
    this.selectedDate = new Date();
    this._updateModel(false);
  }

  _onDisplayModeClick(event) {
    var displayMode = $(event.target).data('mode');
    this.setDisplayMode(displayMode);
  }

  _onYearClick(event) {
    this._showYearPanel = !this._showYearPanel;
    this._updateScreen(true);
  }

  _onListClick(event) {
    this._showListPanel = !this._showListPanel;
    this._updateScreen(true);
  }

  _onDayMouseDown(event) {
    // we cannot use event.stopPropagation() in CalendarComponent.js because this would
    // prevent context-menus from being closed. With this awkward if-statement we only
    // process the event, when it is not bubbling up from somewhere else (= from mousedown
    // event on component).
    if (event.eventPhase === Event.AT_TARGET) {
      var selectedDate = $(event.delegateTarget).data('date');
      this._setSelection(selectedDate, null);
    }
  }

  /**
   * @param selectedDate
   * @param selectedComponent may be null when a day is selected
   */
  _setSelection(selectedDate, selectedComponent) {
    var changed = false;

    // selected date
    if (dates.compare(this.selectedDate, selectedDate) !== 0) {
      changed = true;
      $('.calendar-day', this.$container).each(function(index, element) {
        var $day = $(element),
          date = $day.data('date');
        if (dates.compare(date, this.selectedDate) === 0) {
          $day.select(false); // de-select old date
        } else if (dates.compare(date, selectedDate) === 0) {
          $day.select(true); // select new date
        }
      }.bind(this));
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
    }

    if (this._showYearPanel) {
      this._yearPanel.selectDate(this.selectedDate);
    }
  }

  /* --  set display mode and range ------------------------------------- */

  _updateScreen(animate) {
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
  }

  layoutSize(animate) {
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
    var $selected = $('.selected', this.$grid),
      $topSelected = $('.selected', this.$topGrid),
      containerW = this.$container.width(),
      gridH = this.$grid.height(),
      gridW = containerW - 20; // containerW - @root-group-box-padding-right

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

    var $weeksToHide = $(); // Empty
    var $allWeeks = $('.calendar-week', this.$grid);
    // layout week

    if (this._isDay() || this._isWeek() || this._isWorkWeek()) {
      $('.calendar-week', this.$grid).removeClass('calendar-week-noborder');
      // Parent of selected (Day) is a week
      var selectedWeek = $selected.parent();
      $weeksToHide = $allWeeks.not(selectedWeek); // Hide all (other) weeks delayed, height will animate to zero
      $weeksToHide.data('new-height', 0);
      $weeksToHide.removeClass('invisible');
      var newHeight = 24 * this.numberOfHourDivisions * this.heightPerDivision;
      selectedWeek.data('new-height', newHeight);
      selectedWeek.addClass('calendar-week-noborder');
      selectedWeek.removeClass('hidden invisible'); // Current week must be shown
      $('.calendar-day', selectedWeek).data('new-height', newHeight);
      // Hide the week-number in the lower grid
      $('.calendar-week-name', this.$grid).addClass('invisible'); // Keep the reserved space
      $('.calendar-week-allday-container', this.$topGrid).removeClass('hidden');
      $('.calendar-week-task', this.$topGrid).removeClass('hidden');
    } else {
      // Month
      var newHeightMonth = gridH / this.monthViewNumberOfWeeks;
      $allWeeks.removeClass('calendar-week-noborder invisible');
      $allWeeks.eq(0).addClass('calendar-week-noborder');
      $allWeeks.data('new-height', newHeightMonth);
      $('.calendar-day', this.$grid).data('new-height', newHeightMonth);
      var $allDays = $('.calendar-week-name', this.$grid);
      $allDays.removeClass('hidden invisible');
      $allDays.data('new-height', newHeightMonth);
      $('.calendar-week-allday-container', this.$topGrid).addClass('hidden');
      $('.calendar-week-task', this.$topGrid).addClass('hidden');
    }

    // layout days
    var contentW = gridW - 45; // gridW - @calendar-week-name-width
    if (this._isDay()) {
      $('.calendar-day-name, .calendar-day', this.$topGrid).data('new-width', 0);
      $('.calendar-day', this.$grid).data('new-width', 0);
      $('.calendar-day-name:nth-child(' + ($topSelected.index() + 1) + ')', this.$topGrid)
        .data('new-width', contentW);
      $('.calendar-day:nth-child(' + ($topSelected.index() + 1) + ')', this.$topGrid).data('new-width', contentW);
      $('.calendar-day:nth-child(' + ($selected.index() + 1) + ')', this.$grid).data('new-width', contentW);
    } else if (this._isWorkWeek()) {
      this.$topGrid.find('.calendar-day-name').data('new-width', 0);
      this.$grids.find('.calendar-day').data('new-width', 0);
      $('.calendar-day-name:nth-child(-n+6), ' +
        '.calendar-day:nth-child(-n+6)', this.$topGrid)
        .data('new-width', parseInt(contentW / this.workDayIndices.length, 10));
      $('.calendar-day:nth-child(-n+6)', this.$grid)
        .data('new-width', parseInt(contentW / this.workDayIndices.length, 10));
    } else if (this._isMonth() || this._isWeek()) {
      this.$grids.find('.calendar-day').data('new-width', parseInt(contentW / 7, 10));
      this.$topGrid.find('.calendar-day-name').data('new-width', parseInt(contentW / 7, 10));
    }

    // layout components
    if (this._isMonth()) {
      $('.component-month', this.$grid).each(function() {
        var $comp = $(this),
          $day = $comp.closest('.calendar-day');
        $comp.toggleClass('compact', $day.data('new-width') < CalendarComponent.MONTH_COMPACT_THRESHOLD);
      });
    }

    // set day-name (based on width of shown column)
    var width = this.$container.width(),
      weekdays;

    if (this._isDay()) {
      width /= 1;
    } else if (this._isWorkWeek()) {
      width /= this.workDayIndices.length;
    } else if (this._isWeek()) {
      width /= 7;
    } else if (this._isMonth()) {
      width /= 7;
    }

    if (width > 100) {
      weekdays = this.session.locale.dateFormat.symbols.weekdaysOrdered;
    } else {
      weekdays = this.session.locale.dateFormat.symbols.weekdaysShortOrdered;
    }

    $('.calendar-day-name', this.$topGrid).each(function(index) {
      $(this).attr('data-day-name', weekdays[index]);
    });

    var updateScrollbarCallback = this._updateScrollbars.bind(this);

    // animate old to new sizes
    $('div', this.$container).each(function() {
      var $e = $(this),
        w = $e.data('new-width'),
        h = $e.data('new-height');

      if (w !== undefined && w !== $e.outerWidth()) {
        if (animate) {
          $e.animateAVCSD('width', w, updateScrollbarCallback.bind($e));
        } else {
          $e.css('width', w);
          updateScrollbarCallback($e);
        }
      }
      if (h !== undefined && h !== $e.outerHeight()) {
        if (h > 0) {
          $e.removeClass('hidden');
        }
        if (animate) {
          $e.animateAVCSD('height', h, function() {
            if (h === 0) {
              $e.addClass('hidden');
            }
            updateScrollbarCallback($e);
          });
        } else {
          $e.css('height', h);
          if (h === 0) {
            $e.addClass('hidden');
          }
          updateScrollbarCallback($e);
        }
      }
    });
  }

  _updateScrollbars($parent) {
    var $scrollables = $('.calendar-scrollable-components', $parent);

    for (var k = 0; k < $scrollables.length; k++) {
      var $scrollable = $scrollables.eq(k);
      scrollbars.update($scrollable, true);
    }
    if (this.needsScrollToStartHour) {
      this.scrollToInitialTime();
    }
  }

  layoutYearPanel() {
    if (this._showYearPanel) {
      scrollbars.update(this._yearPanel.$yearList);
      this._yearPanel._scrollYear();
    }
  }

  layoutLabel() {
    var text, $dates, $topGridDates,
      exFrom = this._exactRange.from,
      exTo = this._exactRange.to;

    // set range text
    if (this._isDay()) {
      text = this._format(exFrom, 'd. MMMM yyyy');
    } else if (this._isWorkWeek() || this._isWeek()) {
      var toText = this.session.text('ui.to');
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

    var w, d, cssClass,
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
      var exactDate = new Date(this._exactRange.from.valueOf());

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
    var $e;

    // remove old axis
    $('.calendar-week-axis, .calendar-week-task', this.$grid).remove();

    // set weekname
    var session = this.session;

    $('.calendar-week-name', this.$container).each(function(index) {
      if (index > 0) {
        $e = $(this);
        $e.text(session.text('ui.CW', dates.weekInYear($e.next().data('date'))));
      }
    });

    // day schedule
    if (!this._isMonth()) {
      // Parent of selected day: Week
      //    var $parent = $selected.parent();
      var $parent = $('.calendar-week', this.$grid);

      for (var h = 0; h < 24; h++) { // Render lines for each hour
        var paddedHour = ('00' + h).slice(-2);
        var topPos = h * this.numberOfHourDivisions * this.heightPerDivision;
        $parent.appendDiv('calendar-week-axis hour' + (h === 0 ? ' first' : '')).attr('data-axis-name', paddedHour + ':00').css('top', topPos + 'px');

        for (var m = 1; m < this.numberOfHourDivisions; m++) { // First one rendered above. Start at the next
          topPos += this.heightPerDivision;
          $parent.appendDiv('calendar-week-axis').attr('data-axis-name', '').css('top', topPos + 'px');
        }
      }
    }
  }

  /* -- year events ---------------------------------------- */

  _onYearPanelDateSelect(event) {
    this.selectedDate = event.date;
    this._updateModel(false);
  }

  _updateListPanel() {
    if (this._showListPanel) {

      // remove old list-components
      this._listComponents.forEach(function(listComponent) {
        listComponent.remove();
      });

      this._listComponents = [];
      this._renderListPanel();
    }
  }

  _remove() {
    var $days = $('.calendar-day', this.$grid);

    // Ensure that scrollbars are unregistered
    for (var k = 0; k < $days.length; k++) {
      var $day = $days.eq(k);
      var $scrollableContainer = $day.children('.calendar-scrollable-components');

      if ($scrollableContainer.length > 0) {
        scrollbars.uninstall($scrollableContainer, this.session);
        $scrollableContainer.remove();
      }
    }

    super._remove();
  }

  /**
   * Renders the panel on the left, showing all components of the selected date.
   */
  _renderListPanel() {
    var listComponent, components = [];

    // set title
    this.$listTitle.text(this._format(this.selectedDate, 'd. MMMM yyyy'));

    // find components to display on the list panel
    this.components.forEach(function(component) {
      if (belongsToSelectedDate.call(this, component)) {
        components.push(component);
      }
    }.bind(this));

    function belongsToSelectedDate(component) {
      var selectedDate = dates.trunc(this.selectedDate);
      if (dates.compare(selectedDate, component.coveredDaysRange.from) >= 0 &&
        dates.compare(selectedDate, component.coveredDaysRange.to) <= 0) {
        return true;
      }
      return false;
    }

    components.forEach(function(component) {
      listComponent = new CalendarListComponent(this.selectedDate, component);
      listComponent.render(this.$list);
      this._listComponents.push(listComponent);
    }.bind(this));
  }

  /* -- components, events-------------------------------------------- */

  _selectedComponentChanged(component, partDay) {
    this._setSelection(partDay, component);
  }

  _onDayContextMenu(event) {
    this._showContextMenu(event, 'Calendar.EmptySpace');
  }

  _showContextMenu(event, allowedType) {
    event.preventDefault();
    event.stopPropagation();

    var func = function func(event, allowedType) {
      if (!this.rendered || !this.attached) { // check needed because function is called asynchronously
        return;
      }
      var filteredMenus = menus_1.filter(this.menus, [allowedType], true),
        $part = $(event.currentTarget);
      if (filteredMenus.length === 0) {
        return;
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

  /* -- components, arrangement------------------------------------ */

  _arrangeComponents() {
    var k, j, $day, $allChildren, $children, $scrollableContainer, dayComponents, day;

    var $days = $('.calendar-day', this.$grid);
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
          var $child = $allChildren.eq(j);
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

    scrollbars.uninstall(this.$grid, this.session);
    if (this._isMonth()) {
      this.$grid.removeClass('calendar-scrollable-components');
    } else {
      // If we're in the non-month views, the time can scroll. Add scrollbars
      this.$grid.addClass('calendar-scrollable-components');
      scrollbars.install(this.$grid, {
        parent: this,
        session: this.session,
        axis: 'y'
      });
    }
  }

  _getComponents($children) {
    var i, $child;
    var components = [];
    for (i = 0; i < $children.length; i++) {
      $child = $children.eq(i);
      components.push($child.data('component'));
    }
    return components;
  }

  _sort(components) {
    components.sort(this._sortFromTo);
  }

  /**
   * Arrange components (stack width, stack index) per day
   * */
  _arrange(components, day) {
    var i, j, c, r, k,
      columns = [];

    // ordered by from, to
    this._sort(components);

    // clear existing placement
    for (i = 0; i < components.length; i++) {
      c = components[i];
      if (!c.stack) {
        c.stack = {};
      }
      c.stack[day] = {};
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
        c.stack[day].x = k;
      } else {
        columns.push(c);
        c.stack[day].x = columns.length - 1;
      }

      // update stackW
      for (j = 0; j < columns.length; j++) {
        columns[j].stack[day].w = columns.length;
      }
    }
  }

  _allEndBefore(columns, pos, day) {
    var i;
    for (i = 0; i < columns.length; i++) {
      if (!this._endsBefore(columns[i], pos, day)) {
        return false;
      }
    }
    return true;
  }

  _findReplacableColumn(columns, pos, day) {
    var j;
    for (j = 0; j < columns.length; j++) {
      if (this._endsBefore(columns[j], pos, day)) {
        return j;
      }
    }
    return -1;
  }

  _endsBefore(component, pos, day) {
    return component.getPartDayPosition(day).to <= pos;
  }

  _arrangeComponentSetPlacement($children, day) {
    var i, $child, stack;

    // loop and place based on data
    for (i = 0; i < $children.length; i++) {
      $child = $children.eq(i);
      stack = $child.data('component').stack[day];

      // make last element smaller
      $child
        .css('width', 100 / stack.w + '%')
        .css('left', stack.x * 100 / stack.w + '%');
    }
  }

  /* -- helper ---------------------------------------------------- */

  _dayPosition(hour, minutes) {
    // Height position in percent of total calendar

    var pos;
    if (hour < 0) {
      pos = 0; // All day event
    } else {
      pos = 100 / (24 * 60) * (hour * 60 + minutes);
    }
    return Math.round(pos * 100) / 100;
  }

  _hourToNumber(hour) {
    var splits = hour.split(':');
    return parseFloat(splits[0]) + parseFloat(splits[1]) / 60;
  }

  _format(date, pattern) {
    return dates.format(date, this.session.locale, pattern);
  }

  _sortFromTo(c1, c2) {
    var from1 = dates.parseJsonDate(c1.fromDate);
    var from2 = dates.parseJsonDate(c2.fromDate);
    var dFrom = dates.compare(from1, from2);
    if (dFrom !== 0) {
      return dFrom;
    }
    var to1 = dates.parseJsonDate(c1.toDate);
    var to2 = dates.parseJsonDate(c2.toDate);
    return dates.compare(to1, to2);
  }
}
