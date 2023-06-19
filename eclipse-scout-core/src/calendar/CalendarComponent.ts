/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {aria, Calendar, CalendarComponentEventMap, CalendarComponentModel, DateRange, dates, icons, InitModelOf, JsonDateRange, Label, Popup, Range, scout, strings, Widget, WidgetPopup} from '../index';
import $ from 'jquery';

export class CalendarComponent extends Widget implements CalendarComponentModel {
  declare model: CalendarComponentModel;
  declare eventMap: CalendarComponentEventMap;
  declare self: CalendarComponent;
  declare parent: Calendar;

  fromDate: string;
  toDate: string;
  selected: boolean;
  fullDay: boolean;
  fullDayIndex: number;
  draggable: boolean;
  item: CalendarItem;
  stack: Record<string, { x?: number; w?: number }>;
  coveredDaysRange: DateRange;

  /** @internal */
  _$parts: JQuery[];

  constructor() {
    super();
    this.selected = false;
    this.fullDay = false;
    this.fullDayIndex = -1;
    this.item = null;
    this._$parts = [];
  }

  /**
   * If day of a month is smaller than 100px, the components get the class compact
   */
  static MONTH_COMPACT_THRESHOLD = 100;

  static DAY_OF_MONTH_HEIGHT = 30;
  static COMPONENT_HEIGHT = 24;
  static COMPONENT_VGAP = 2;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);

    this._syncCoveredDaysRange(model.coveredDaysRange as JsonDateRange);
  }

  protected _syncCoveredDaysRange(coveredDaysRange: JsonDateRange) {
    if (coveredDaysRange) {
      this.coveredDaysRange = new DateRange(
        dates.parseJsonDate(coveredDaysRange.from),
        dates.parseJsonDate(coveredDaysRange.to));
    }
  }

  protected override _remove() {
    // remove $parts because they're not children of this.$container
    this._$parts.forEach($part => $part.remove());
    this._$parts = [];
    super._remove();
  }

  protected _startLoopDay(): Date {
    // start date is either beginning of the component or beginning of viewRange
    if (dates.compare(this.coveredDaysRange.from, this.parent.viewRange.from) > 0) {
      return this.coveredDaysRange.from;
    }
    return this.parent.viewRange.from;
  }

  protected override _render() {
    let partDay: Date, $day: JQuery, $part: JQuery;
    if (!this.coveredDaysRange) {
      // coveredDaysRange is not set on current CalendarComponent. Cannot show calendar component without from and to values.
      return;
    }

    // Calculate visible
    let calendarDescriptor = this.parent.findCalendarForComponent(this);
    if (calendarDescriptor) {
      // components with no calendarId are alwas shown in the default column
      this.setVisible(calendarDescriptor.visible);
    }

    if (!this.isVisible()) {
      return;
    }

    let loopDay = this._startLoopDay();

    let appointmentToDate: Date | string = dates.parseJsonDate(this.toDate);
    let appointmentFromDate = dates.parseJsonDate(this.fromDate);
    let coveredDaysRangeTo = this.coveredDaysRange.to;
    let calendarCssClass = calendarDescriptor ? calendarDescriptor.cssClass : null;

    if (!this.fullDay) {
      let truncToDate = dates.trunc(appointmentToDate);
      if (!dates.isSameDay(appointmentFromDate, appointmentToDate) && dates.compare(appointmentToDate, truncToDate) === 0) {
        appointmentToDate = dates.shiftTime(appointmentToDate, 0, 0, 0, -1);
        coveredDaysRangeTo = dates.shift(coveredDaysRangeTo, 0, 0, -1);
      }
    }
    appointmentToDate = dates.toJsonDate(appointmentToDate);

    let lastComponentDay = dates.shift(coveredDaysRangeTo, 0, 0, 1);

    if (dates.compare(loopDay, lastComponentDay) > 0) {
      // start day for the while loop is greater than the exit condition
      return;
    }

    while (!dates.isSameDay(loopDay, lastComponentDay)) {
      partDay = loopDay;
      loopDay = dates.shift(loopDay, 0, 0, 1); // increase day for loop

      // check if day is in visible view range
      if (dates.compare(partDay, this.parent.viewRange.to) > 0) {
        // break condition, partDay is now out of range.
        break;
      }

      if (this.fullDay && !this.parent.isMonth()) {
        $day = this._findDayInGrid(partDay, this.parent.$topGrid);
      } else {
        $day = this._findDayInGrid(partDay, this.parent.$grid);
      }
      if (!$day) {
        // next day, partDay not found in grid
        continue;
      }

      // Find corresponding calendar
      let $calendar = this._findCalendarColumnInDay($day, this.item.calendarId);
      if (!$calendar) {
        continue;
      }
      $part = $calendar.appendDiv('calendar-component');

      $part
        .addClass(this.item.cssClass)
        .addClass(calendarCssClass)
        .data('component', this)
        .data('partDay', partDay)
        .on('mouseup', this._onMouseUp.bind(this))
        .on('contextmenu', this._onContextMenu.bind(this));
      $part.appendDiv('calendar-component-leftcolorborder');
      let $partContent = $part.appendDiv('content');
      if (this.item.subjectIconId) {
        $partContent.appendIcon(this.item.subjectIconId);
      }
      $partContent.appendSpan('subject', this.item.subject);

      this._$parts.push($part);

      if (this.parent.isMonth()) {
        let width = $day.data('new-width') || $day.width(); // prefer width from layoutSize
        $part.addClass('component-month')
          .toggleClass('compact', width < CalendarComponent.MONTH_COMPACT_THRESHOLD);
      } else {
        if (this.fullDay) {
          // Full day tasks are rendered in the topGrid
          // Offset of initial task: CalendarComponent.DAY_OF_MONTH_HEIGHT
          // Offset of following tasks: (CalendarComponent.COMPONENT_HEIGHT + CalendarComponent.COMPONENT_VGAP) * preceding number of tasks
          this._arrangeTask(CalendarComponent.DAY_OF_MONTH_HEIGHT + (CalendarComponent.COMPONENT_HEIGHT + CalendarComponent.COMPONENT_VGAP) * Math.max(this.fullDayIndex, 0));
          $part.addClass('component-task');
        } else {
          let
            fromDate = dates.parseJsonDate(this.fromDate),
            toDate = dates.parseJsonDate(appointmentToDate),
            partFrom = this._getHours(this.fromDate),
            partTo = this._getHours(appointmentToDate);

          // position and height depending on start and end date
          $part.addClass('component-day');
          if (dates.isSameDay(dates.trunc(this.coveredDaysRange.from), dates.trunc(coveredDaysRangeTo))) {
            this._partPosition($part, partFrom, partTo);
          } else if (dates.isSameDay(partDay, fromDate)) {
            this._partPosition($part, partFrom, 25) // 25: indicate that it takes longer than that day
              .addClass('component-open-bottom');
          } else if (dates.isSameDay(partDay, toDate)) {
            // Start at zero: No need to indicate that it starts earlier since indicator needs no extra space
            this._partPosition($part, 0, partTo)
              .addClass('component-open-top');
          } else {

            this._partPosition($part, 0, 25) // 25: indicate that it takes longer than that day
              .addClass('component-open-top')
              .addClass('component-open-bottom');
          }
        }
      }
    }
  }

  protected _getHours(date: string): number {
    let d = dates.parseJsonDate(date);
    return d.getHours() + d.getMinutes() / 60;
  }

  getLengthInHoursDecimal(): number {
    let toTimestamp = dates.parseJsonDate(this.toDate);
    let fromTimestamp = dates.parseJsonDate(this.fromDate);
    return (toTimestamp.getTime() - fromTimestamp.getTime()) / (1000 * 60 * 60);
  }

  protected _findDayInGrid(date: Date, $grid: JQuery): JQuery {
    return $grid.find('.calendar-day')
      .filter(function(i, elem) {
        return dates.isSameDay($(this).data('date'), date);
      }).eq(0);
  }

  protected _findCalendarColumnInDay($day: JQuery, calendarId: string | number): JQuery {
    if (!this.parent.isDay() || !calendarId) {
      calendarId = 'default';
    }
    return $day.find('.calendar-column')
      .filter((index: number, element: HTMLSelectElement) => $(element).data('calendarId').toString() === calendarId.toString());
  }

  protected _isTask(): boolean {
    return !this.parent.isMonth() && this.fullDay;
  }

  protected _arrangeTask(taskOffset: number) {
    this._$parts.forEach($part => $part.css('top', taskOffset + 'px'));
  }

  protected _isDayPart(): boolean {
    return !this.parent.isMonth() && !this.fullDay;
  }

  protected _getHourRange(day: Date): Range {
    let hourRange = new Range(this._getHours(this.fromDate), this._getHours(this.toDate));
    let dateRange = new DateRange(dates.parseJsonDate(this.fromDate), dates.parseJsonDate(this.toDate));

    if (dates.isSameDay(day, dateRange.from) && dates.isSameDay(day, dateRange.to)) {
      return new Range(hourRange.from, hourRange.to);
    }
    if (dates.isSameDay(day, dateRange.from)) {
      return new Range(hourRange.from, 24);
    }
    if (dates.isSameDay(day, dateRange.to)) {
      return new Range(0, hourRange.to);
    }
    return new Range(0, 24);
  }

  getPartDayPosition(day: Date): Range {
    return this._getDisplayDayPosition(this._getHourRange(day));
  }

  protected _getDisplayDayPosition(range: Range): Range {
    // Doesn't support minutes yet...
    let preferredRange = new Range(this.parent._dayPosition(range.from, 0), this.parent._dayPosition(range.to, 0));
    // Fixed number of divisions...
    let minRangeSize = Math.round(100 * 100 / 24 / this.parent.numberOfHourDivisions) / 100; // Round to two digits
    if (preferredRange.size() < minRangeSize) {
      return new Range(preferredRange.from, preferredRange.from + minRangeSize);
    }
    return preferredRange;
  }

  protected _partPosition($part: JQuery, y1: number, y2: number): JQuery {
    // Compensate open bottom (height: square of 16px, rotated 45°, approx. 23px = sqrt(16^2 + 16^2)
    let compensateBottom = y2 === 25 ? 23 : 0;
    y2 = Math.min(24, y2);

    let range = new Range(y1, y2);
    let r = this._getDisplayDayPosition(range);

    // Convert to %, rounded to two decimal places
    compensateBottom = Math.round(100 * (100 / 1920 * compensateBottom)) / 100;

    return $part
      .css('top', r.from + '%')
      .css('height', r.to - r.from - compensateBottom + '%');
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderSelected();
  }

  protected _renderSelected() {
    this._$parts.forEach($part => $part.toggleClass('comp-selected', this.selected));
  }

  setSelected(selected: boolean) {
    this.setProperty('selected', selected);
  }

  updateSelectedComponent($part: JQuery, updateScrollPosition: boolean) {
    this.parent._selectedComponentChanged(this, this.item.calendarId, $part.data('partDay') as Date, updateScrollPosition);
  }

  protected _onMouseUp(event: JQuery.MouseUpEvent) {
    // don't show popup if dragging is in process
    if (this.parent._moveData && this.parent._moveData.moving) {
      return;
    }

    // don't show popup when range selection is in progress
    if (this.parent._rangeSelectionStarted) {
      return;
    }

    let $part = $(event.delegateTarget);
    this.updateSelectedComponent($part, false);

    if (event.button === 0) {
      let popup = scout.create((WidgetPopup<Label>), {
        parent: this.parent,
        $anchor: $part,
        closeOnAnchorMouseDown: true,
        closeOnMouseDownOutside: true,
        closeOnOtherPopupOpen: true,
        horizontalAlignment: Popup.Alignment.LEFT,
        verticalAlignment: Popup.Alignment.CENTER,
        trimWidth: false,
        trimHeight: true,
        horizontalSwitch: true,
        verticalSwitch: false,
        withArrow: true,
        cssClass: 'popup',
        scrollType: 'remove',
        location: {
          y: event.originalEvent.clientY
        },
        content: {
          objectType: Label,
          htmlEnabled: true,
          scrollable: true,
          cssClass: 'calendar-component-tooltip-content tooltip-content',
          value: this.description(true)
        }
      });
      popup.open();
      popup.$container.find('.app-link')
        .on('click', this._onAppLinkAction.bind(this));
    }
  }

  /** @internal */
  _onContextMenu(event: JQuery.ContextMenuEvent) {
    this.parent._showContextMenu(event, Calendar.MenuType.CalendarComponent);
  }

  protected _format(date: Date, pattern: string): string {
    return dates.format(date, this.session.locale, pattern);
  }

  description(linkAllowed: boolean): string {
    let range = null,
      $container = $('<div>'),
      fromDate = dates.parseJsonDate(this.fromDate),
      toDate = dates.parseJsonDate(this.toDate),
      descriptionAvailable = strings.hasText(this.item.description) || this.item.descriptionElements;

    let $header = $container.appendDiv('calendar-component-header');
    if (descriptionAvailable) {
      $header.addClass('with-description');
    }

    // subject
    if (strings.hasText(this.item.subject)) {
      if (strings.hasText(this.item.subjectLabel)) {
        $header.appendDiv('calendar-component-title-label', this.item.subjectLabel);
      }
      let $subject = $header.appendDiv('calendar-component-title', this.item.subject);
      if (linkAllowed && strings.hasText(this.item.subjectAppLink)) {
        $subject.addClass('app-link').attr('data-ref', this.item.subjectAppLink);
        aria.role($subject, 'link');
      }
    }

    // time-range
    if (this.fullDay) {
      // NOP
    } else if (dates.isSameDay(fromDate, toDate)) {
      range = this.session.text('ui.FromXToY', this._format(fromDate, 'HH:mm'), this._format(toDate, 'HH:mm'));
    } else {
      range = this.session.text('ui.FromXToY', this._format(fromDate, 'EEEE HH:mm '), this._format(toDate, 'EEEE HH:mm'));
    }

    if (strings.hasText(range)) {
      let $timeContainer = $header.appendDiv('calendar-component-intro');
      $timeContainer.appendIcon(icons.CLOCK);
      $timeContainer.appendSpan('', range);
    }

    if (descriptionAvailable) {
      let $description = $container.appendDiv('calendar-component-description-container');

      // description
      if (strings.hasText(this.item.description)) {
        $description.appendSpan('calendar-component-description').html(strings.nl2br(this.item.description));
      }

      if (this.item.descriptionElements) {
        let descriptionIconExists = false;
        for (let i = 0; i < this.item.descriptionElements.length; i++) {
          if (this.item.descriptionElements[i].iconId) {
            descriptionIconExists = true;
            break;
          }
        }
        for (let i = 0; i < this.item.descriptionElements.length; i++) {
          let descriptionElement = this.item.descriptionElements[i];
          let $descriptionElementContainer = $description.appendDiv('calendar-component-description-element');
          if (i === 0) {
            $descriptionElementContainer.addClass('first');
          }
          if (i === this.item.descriptionElements.length - 1) {
            $descriptionElementContainer.addClass('last');
          }
          if (strings.hasText(descriptionElement.iconId) || descriptionIconExists) {
            $descriptionElementContainer.appendIcon(descriptionElement.iconId);
          }
          let $text = $descriptionElementContainer.appendDiv('text').html(strings.nl2br(descriptionElement.text));
          if (linkAllowed && strings.hasText(descriptionElement.appLink)) {
            $text.addClass(' app-link').attr('data-ref', descriptionElement.appLink);
            aria.role($text, 'link');
          }
        }
      }
    }
    return $container[0].innerHTML;
  }

  triggerAppLinkAction(ref: string) {
    this.trigger('appLinkAction', {
      ref: ref
    });
  }

  _onAppLinkAction(event: JQuery.ClickEvent) {
    let $target = $(event.delegateTarget);
    let ref = $target.data('ref') as string;
    this.triggerAppLinkAction(ref);
  }
}

/**
 * See JsonCalendarItem.java
 */
export type CalendarItem = {
  exists: boolean;
  lastModified: number;
  itemId: any;
  owner: string;
  cssClass: string;
  calendarId: number;
  subject: string;
  description: string;
  recurrencePattern: {
    lastModified: number;
    regenerate: boolean;
    startTimeMinutes: number;
    endTimeMinutes: number;
    durationMinutes: number;
    firstDate: Date;
    lastDate: Date;
    occurrences: number;
    noEndDate: boolean;
    /**
     * @see RecurrencePattern.java TYPE* constants
     */
    type: number;
    interval: number;
    /**
     * @see RecurrencePattern.java INST_* constants
     */
    instance: number;
    dayOfWeekBits: number;
    dayOfMonth: number;
    monthOfYear: number;
  };
  subjectLabel: string;
  subjectAppLink: string;
  subjectIconId: string;
  descriptionElements: CalendarItemDescriptionElement[];
};

export type CalendarItemDescriptionElement = {
  text: string;
  iconId: string;
  appLink: string;
};
