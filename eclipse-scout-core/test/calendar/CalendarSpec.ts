/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Calendar, CalendarComponent, CalendarDescriptor, CalendarItem, DateRange, dates, scout, TreeBoxTreeNode, UuidPool} from '../../src/index';
import {JQueryTesting} from '../../src/testing/index';

describe('Calendar', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    jasmine.Ajax.install();
    jasmine.clock().install();
    session = sandboxSession();
    uninstallUnloadHandlers(session);
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  class SpecCalendar extends Calendar {
    declare _exactRange: DateRange;

    override _updateFullDayIndices(fullDayComponents?: CalendarComponent[]) {
      super._updateFullDayIndices(fullDayComponents);
    }

    override _setSelection(selectedDate: Date, selectedCalendar: CalendarDescriptor | string, selectedComponent: CalendarComponent, updateScrollPosition: boolean, timeChanged: boolean) {
      super._setSelection(selectedDate, selectedCalendar, selectedComponent, updateScrollPosition, timeChanged);
    }

    override _updateCalendarVisibility(updatedCalendars: [calendarId: string, visible: boolean][]) {
      super._updateCalendarVisibility(updatedCalendars);
    }

    override _calculateStackKey(date: Date, calendarId?: string): string {
      return super._calculateStackKey(date, calendarId);
    }

    override _onNextClick() {
      super._onNextClick();
    }

    override _onPreviousClick() {
      super._onPreviousClick();
    }

    override _arrange(components: CalendarComponent[], day: Date) {
      super._arrange(components, day);
    }
  }

  describe('init', () => {

    it('creates an empty calendar', () => {
      let cal = scout.create(Calendar, {parent: session.desktop});
      expect(cal.viewRange).toBeDefined();
    });

    describe('expands panels corretly', () => {

      const getContainterWidth = (container: JQuery): number => {
        return container.width();
      };

      const getContainterHeight = (container: JQuery): number => {
        return container.height();
      };

      it('should expand the calendar sidebar when showCalendarSidebar is true', () => {
        // Arrange
        let model = {parent: session.desktop, showCalendarSidebar: true};
        let cal = scout.create(Calendar, model);

        // Act
        cal.render();
        let calendarSidebarWidth = getContainterWidth(cal.calendarSidebar.$container);
        let yearPanelWidth = getContainterWidth(cal.calendarSidebar.$container);

        // Assert
        expect(calendarSidebarWidth).toBeGreaterThan(0);
        expect(yearPanelWidth).toBeGreaterThan(0);
      });

      it('should not expand the calendar sidebar when showCalendarSidebar is false', () => {
        // Arrange
        let model = {parent: session.desktop, showCalendarSidebar: false};
        let cal = scout.create(Calendar, model);

        // Act
        cal.render();
        let calendarSidebarWidth = getContainterWidth(cal.calendarSidebar.$container);

        // Assert
        expect(calendarSidebarWidth).toBe(0);
      });

      it('should expand the calendar sidebar and the calendars panel', () => {
        // Arrange
        let model = {
          parent: session.desktop,
          showCalendarSidebar: true,
          showCalendarsPanel: true,
          calendars: [{calendarId: 'a'}, {calendarId: 'b'}]
        };
        let cal = scout.create(Calendar, model);

        // Act
        cal.render();
        cal.calendarSidebar.revalidateLayout();
        let calendarSidebarWidth = getContainterWidth(cal.calendarSidebar.$container);
        let calendarsPanelHeight = getContainterHeight(cal.calendarSidebar.calendarsPanel.$container);

        // Assert
        expect(calendarSidebarWidth).toBeGreaterThan(0);
        expect(calendarsPanelHeight).toBeGreaterThan(40);
      });

      it('should not expand the calendars panel', () => {
        // Arrange
        let model = {
          parent: session.desktop,
          showCalendarSidebar: true,
          showCalendarsPanel: false,
          calendars: [{calendarId: 'a'}, {calendarId: 'b'}]
        };
        let cal = scout.create(Calendar, model);

        // Act
        cal.render();
        let calendarSidebarWidth = getContainterWidth(cal.calendarSidebar.$container);
        let calendarsPanelHeight = getContainterHeight(cal.calendarSidebar.calendarsPanel.$container);

        // Assert
        expect(calendarSidebarWidth).toBeGreaterThan(0);
        expect(calendarsPanelHeight).toBeLessThan(40);
      });

      it('should expand the list panel when showListPanel is true', () => {
        // Arrange
        let model = {parent: session.desktop, showListPanel: true};
        let cal = scout.create(Calendar, model);

        // Act
        cal.render();
        let listPanelWidth = getContainterWidth(cal.$listContainer);

        // Assert
        expect(listPanelWidth).toBeGreaterThan(0);
      });

      it('should expand nothing when no variable is set', () => {
        // Arrange
        let model = {parent: session.desktop};
        let cal = scout.create(Calendar, model);

        // Act
        cal.render();
        let yearPanelWidth = getContainterWidth(cal.calendarSidebar.yearPanel.$container);
        let calendarsPanelWidth = getContainterWidth(cal.calendarSidebar.calendarsPanel.$container);
        let listPanelWidth = getContainterWidth(cal.$listContainer);

        // Assert
        expect(yearPanelWidth).toBe(0);
        expect(calendarsPanelWidth).toBe(0);
        expect(listPanelWidth).toBe(0);
      });
    });
  });

  describe('dayPosition', () => {

    it('calculates the day position', () => {
      let cal = scout.create(Calendar, {parent: session.desktop});
      // fix total size: 80
      // All day event. Not relevant since in top grid
      expect(cal._dayPosition(-1, 0)).toBe(0);
      expect(cal._dayPosition(0, 0)).toBe(0);
      expect(cal._dayPosition(4, 0)).toBe(16.67); // one sixth
      expect(cal._dayPosition(8, 0)).toBe(33.33); // one third
      expect(cal._dayPosition(10, 0)).toBe(41.67);
      expect(cal._dayPosition(12, 0)).toBe(50);
      expect(cal._dayPosition(12.5, 0)).toBe(52.08);
      expect(cal._dayPosition(13, 0)).toBe(54.17);
      expect(cal._dayPosition(17, 0)).toBe(70.83);
      expect(cal._dayPosition(24, 0)).toBe(100);
    });

  });

  describe('component', () => {
    let cal: SpecCalendar,
      c1: CalendarComponent,
      c2: CalendarComponent,
      c3: CalendarComponent,
      c4: CalendarComponent,
      c5: CalendarComponent,
      c6: CalendarComponent,
      c7: CalendarComponent,
      c8: CalendarComponent;
    let day = dates.parseJsonDate('2016-07-20 00:00:00.000');
    let option1 = {
      fromDate: '2016-07-20 12:00:00.000',
      toDate: '2016-07-20 12:30:00.000'
    };
    let option2 = {
      fromDate: '2016-07-20 12:30:00.000',
      toDate: '2016-07-20 13:00:00.000'
    };
    let option3 = {
      fromDate: '2016-07-20 13:00:00.000',
      toDate: '2016-07-20 20:00:00.000'
    };
    let option4 = {
      fromDate: '2016-07-20 13:30:00.000',
      toDate: '2016-07-20 15:00:00.000'
    };
    let option5 = {
      fromDate: '2016-07-20 12:15:00.000',
      toDate: '2016-07-20 16:00:00.000'
    };

    let optionSmall1 = {
      fromDate: '2016-07-20 11:59:00.000',
      toDate: '2016-07-20 12:00:00.000'
    };

    let option8 = {
      fromDate: '2016-07-20 12:00:00.000',
      toDate: '2016-07-21 08:00:00.000'
    };

    beforeEach(() => {
      cal = scout.create(SpecCalendar, {parent: session.desktop});
      c1 = scout.create(CalendarComponent, $.extend({parent: cal}, option1));
      c2 = scout.create(CalendarComponent, $.extend({parent: cal}, option2));
      c3 = scout.create(CalendarComponent, $.extend({parent: cal}, option3));
      c4 = scout.create(CalendarComponent, $.extend({parent: cal}, option4));
      c5 = scout.create(CalendarComponent, $.extend({parent: cal}, option5));
      c6 = scout.create(CalendarComponent, $.extend({parent: cal}, optionSmall1));
      c7 = scout.create(CalendarComponent, $.extend({parent: cal}, optionSmall1));
      c8 = scout.create(CalendarComponent, $.extend({parent: cal}, option8));
    });

    describe('part day position', () => {

      it('calculates the part day position', () => {
        let posRange = c4.getPartDayPosition(day);
        expect(posRange.from).toBe(56.25);
        expect(posRange.to).toBe(62.5);
      });

      it('calculates the part day position for a range smaller than the minimum', () => {
        let posRange = c7.getPartDayPosition(day);
        let minRange = 2.08; // Rounded to two digits: 30min (default division in calendar)
        expect(posRange.from).toBe(49.93);
        expect(posRange.to).toBe(49.93 + minRange);
      });

    });

    describe('sort', () => {
      it('sorts first from then to', () => {
        let components = [c4, c2, c1];
        cal._sort(components);
        expect(components[0] === c1).toBe(true);
        expect(components[1] === c2).toBe(true);
        expect(components[2] === c4).toBe(true);
      });
    });

    describe('arrangeComponents', () => {
      let stackKey;

      beforeAll(() => {
        stackKey = cal._calculateStackKey(day);
      });

      it('does nothing for no components', () => {
        let components = [];
        cal._arrange(components, day);
        expect(components).toEqual([]);
      });

      it('arranges a single component', () => {
        let components = [c1];
        cal._arrange(components, day);
        expect(components[0]).toEqual(c1);
        expect(c1.stack[stackKey].x).toEqual(0);
        expect(c1.stack[stackKey].w).toEqual(1);
      });

      it('arranges intersecting components', () => {
        let components = [c5, c1];
        cal._arrange(components, day);
        expect(components[0]).toEqual(c1);
        expect(components[1]).toEqual(c5);
        expect(c1.stack[stackKey].x).toEqual(0);
        expect(c1.stack[stackKey].w).toEqual(2);
        expect(c5.stack[stackKey].x).toEqual(1);
        expect(c5.stack[stackKey].w).toEqual(2);
      });

      it('arranges equal components', () => {
        let components = [c6, c7];
        cal._arrange(components, day);
        expect(components[0]).toEqual(c6);
        expect(components[1]).toEqual(c7);
        expect(c6.stack[stackKey].x).toEqual(0);
        expect(c6.stack[stackKey].w).toEqual(2);
        expect(c7.stack[stackKey].x).toEqual(1);
        expect(c7.stack[stackKey].w).toEqual(2);
      });

      it('arranges intersecting and non-intersecting components', () => {
        let components = [c1, c2, c3, c4, c5, c6];
        cal._arrange(components, day);
        expect(components[0]).toEqual(c6);
        expect(components[1]).toEqual(c1);
        expect(components[2]).toEqual(c5);
        expect(components[3]).toEqual(c2);
        expect(components[4]).toEqual(c3);
        expect(components[5]).toEqual(c4);
        expect(c1.stack[stackKey].w).toEqual(3);
        expect(c2.stack[stackKey].w).toEqual(3);
        expect(c3.stack[stackKey].w).toEqual(3);
        expect(c4.stack[stackKey].w).toEqual(3);
        expect(c5.stack[stackKey].w).toEqual(3);
        expect(c6.stack[stackKey].w).toEqual(3);

        expect(c6.stack[stackKey].x).toEqual(0);
        expect(c1.stack[stackKey].x).toEqual(1);
        expect(c5.stack[stackKey].x).toEqual(2);
        expect(c2.stack[stackKey].x).toEqual(0);
        expect(c3.stack[stackKey].x).toEqual(0);
        expect(c4.stack[stackKey].x).toEqual(1);
      });

      it('reduces rows when arranging components', () => {
        let components = [c1, c3, c6];
        cal._arrange(components, day);
        expect(components[0]).toEqual(c6);
        expect(components[1]).toEqual(c1);
        expect(components[2]).toEqual(c3);
        expect(c6.stack[stackKey].w).toEqual(2);
        expect(c1.stack[stackKey].w).toEqual(2);
        expect(c3.stack[stackKey].w).toEqual(1);

        expect(c6.stack[stackKey].x).toEqual(0);
        expect(c1.stack[stackKey].x).toEqual(1);
        expect(c3.stack[stackKey].x).toEqual(0);
      });

      it('arranges intersecting components spanning more than one day', () => {
        let day1 = day;
        let components = [c8, c3];

        cal._arrange(components, day1);
        expect(components[0]).toEqual(c8);
        expect(components[1]).toEqual(c3);
        expect(c8.stack[stackKey].w).toEqual(2);
        expect(c3.stack[stackKey].w).toEqual(2);

        expect(c8.stack[stackKey].x).toEqual(0);
        expect(c3.stack[stackKey].x).toEqual(1);
      });

    });

    describe('_updateFullDayIndices', () => {
      const mondayStr = '2023-05-22 00:00:00.000';
      const tuesdayStr = '2023-05-23 00:00:00.000';
      const wednesdayStr = '2023-05-24 00:00:00.000';
      const thursdayStr = '2023-05-25 00:00:00.000';
      const fridayStr = '2023-05-26 00:00:00.000';
      const mondayDate = dates.parseJsonDate(mondayStr);
      const wednesdayDate = dates.parseJsonDate(wednesdayStr);
      const thursdayDate = dates.parseJsonDate(thursdayStr);
      const fridayDate = dates.parseJsonDate(fridayStr);

      let mondayWednesday, tuesdayThursday, wednesdayFriday,
        monday, tuesday1, tuesday2, wednesday, thursday1, thursday2, friday,
        fullDayComponents;

      beforeEach(() => {
        mondayWednesday = createFullDay(mondayStr, wednesdayStr);
        tuesdayThursday = createFullDay(tuesdayStr, thursdayStr);
        wednesdayFriday = createFullDay(wednesdayStr, fridayStr);
        monday = createFullDay(mondayStr, mondayStr);
        tuesday1 = createFullDay(tuesdayStr, tuesdayStr);
        tuesday2 = createFullDay(tuesdayStr, tuesdayStr);
        wednesday = createFullDay(wednesdayStr, wednesdayStr);
        thursday1 = createFullDay(thursdayStr, thursdayStr);
        thursday2 = createFullDay(thursdayStr, thursdayStr);
        friday = createFullDay(fridayStr, fridayStr);

        fullDayComponents = [mondayWednesday, tuesdayThursday, wednesdayFriday,
          monday, tuesday1, tuesday2, wednesday, thursday1, thursday2, friday];

        cal.render();
      });

      function createFullDay(fromDate: string, toDate: string): CalendarComponent {
        return scout.create(CalendarComponent, {
          parent: cal,
          fromDate, toDate,
          coveredDaysRange: {
            from: fromDate,
            to: toDate
          },
          fullDay: true
        });
      }

      it('is not updated if component is out of range', () => {
        expect(mondayWednesday.fullDayIndex).toBe(-1);
        expect(tuesdayThursday.fullDayIndex).toBe(-1);
        expect(wednesdayFriday.fullDayIndex).toBe(-1);
        expect(monday.fullDayIndex).toBe(-1);
        expect(tuesday1.fullDayIndex).toBe(-1);
        expect(tuesday2.fullDayIndex).toBe(-1);
        expect(wednesday.fullDayIndex).toBe(-1);
        expect(thursday1.fullDayIndex).toBe(-1);
        expect(thursday2.fullDayIndex).toBe(-1);
        expect(friday.fullDayIndex).toBe(-1);

        cal._exactRange = new DateRange(mondayDate, mondayDate);
        cal._updateFullDayIndices(fullDayComponents);

        expect(mondayWednesday.fullDayIndex).not.toBe(-1);
        expect(tuesdayThursday.fullDayIndex).toBe(-1);
        expect(wednesdayFriday.fullDayIndex).toBe(-1);
        expect(monday.fullDayIndex).not.toBe(-1);
        expect(tuesday1.fullDayIndex).toBe(-1);
        expect(tuesday2.fullDayIndex).toBe(-1);
        expect(wednesday.fullDayIndex).toBe(-1);
        expect(thursday1.fullDayIndex).toBe(-1);
        expect(thursday2.fullDayIndex).toBe(-1);
        expect(friday.fullDayIndex).toBe(-1);

        cal._exactRange = new DateRange(wednesdayDate, thursdayDate);
        cal._updateFullDayIndices(fullDayComponents);

        expect(mondayWednesday.fullDayIndex).not.toBe(-1);
        expect(tuesdayThursday.fullDayIndex).not.toBe(-1);
        expect(wednesdayFriday.fullDayIndex).not.toBe(-1);
        expect(monday.fullDayIndex).toBe(-1);
        expect(tuesday1.fullDayIndex).toBe(-1);
        expect(tuesday2.fullDayIndex).toBe(-1);
        expect(wednesday.fullDayIndex).not.toBe(-1);
        expect(thursday1.fullDayIndex).not.toBe(-1);
        expect(thursday2.fullDayIndex).not.toBe(-1);
        expect(friday.fullDayIndex).toBe(-1);

        cal._exactRange = new DateRange(thursdayDate, thursdayDate);
        cal._updateFullDayIndices(fullDayComponents);

        expect(mondayWednesday.fullDayIndex).toBe(-1);
        expect(tuesdayThursday.fullDayIndex).not.toBe(-1);
        expect(wednesdayFriday.fullDayIndex).not.toBe(-1);
        expect(monday.fullDayIndex).toBe(-1);
        expect(tuesday1.fullDayIndex).toBe(-1);
        expect(tuesday2.fullDayIndex).toBe(-1);
        expect(wednesday.fullDayIndex).toBe(-1);
        expect(thursday1.fullDayIndex).not.toBe(-1);
        expect(thursday2.fullDayIndex).not.toBe(-1);
        expect(friday.fullDayIndex).toBe(-1);
      });

      it('is correctly updated', () => {
        expect(mondayWednesday.fullDayIndex).toBe(-1);
        expect(tuesdayThursday.fullDayIndex).toBe(-1);
        expect(wednesdayFriday.fullDayIndex).toBe(-1);
        expect(monday.fullDayIndex).toBe(-1);
        expect(tuesday1.fullDayIndex).toBe(-1);
        expect(tuesday2.fullDayIndex).toBe(-1);
        expect(wednesday.fullDayIndex).toBe(-1);
        expect(thursday1.fullDayIndex).toBe(-1);
        expect(thursday2.fullDayIndex).toBe(-1);
        expect(friday.fullDayIndex).toBe(-1);

        cal._exactRange = new DateRange(mondayDate, mondayDate);
        cal._updateFullDayIndices(fullDayComponents);

        // monday
        // mondayWednesday

        expect(mondayWednesday.fullDayIndex).toBe(1);
        expect(tuesdayThursday.fullDayIndex).toBe(-1);
        expect(wednesdayFriday.fullDayIndex).toBe(-1);
        expect(monday.fullDayIndex).toBe(0);
        expect(tuesday1.fullDayIndex).toBe(-1);
        expect(tuesday2.fullDayIndex).toBe(-1);
        expect(wednesday.fullDayIndex).toBe(-1);
        expect(thursday1.fullDayIndex).toBe(-1);
        expect(thursday2.fullDayIndex).toBe(-1);
        expect(friday.fullDayIndex).toBe(-1);

        cal._exactRange = new DateRange(mondayDate, fridayDate);
        cal._updateFullDayIndices(fullDayComponents);

        // monday
        // mondayWednesday

        // tuesday1
        // mondayWednesday
        // tuesday2
        // tuesdayThursday

        // wednesday
        // mondayWednesday
        // wednesdayFriday
        // tuesdayThursday

        // thursday1
        // thursday2
        // wednesdayFriday
        // tuesdayThursday

        // friday
        //
        // wednesdayFriday

        expect(mondayWednesday.fullDayIndex).toBe(1);
        expect(tuesdayThursday.fullDayIndex).toBe(3);
        expect(wednesdayFriday.fullDayIndex).toBe(2);
        expect(monday.fullDayIndex).toBe(0);
        expect(tuesday1.fullDayIndex).toBe(0);
        expect(tuesday2.fullDayIndex).toBe(2);
        expect(wednesday.fullDayIndex).toBe(0);
        expect(thursday1.fullDayIndex).toBe(0);
        expect(thursday2.fullDayIndex).toBe(1);
        expect(friday.fullDayIndex).toBe(0);

        cal._exactRange = new DateRange(fridayDate, fridayDate);
        cal._updateFullDayIndices(fullDayComponents);

        // wednesdayFriday
        // friday

        expect(mondayWednesday.fullDayIndex).toBe(-1);
        expect(tuesdayThursday.fullDayIndex).toBe(-1);
        expect(wednesdayFriday.fullDayIndex).toBe(0);
        expect(monday.fullDayIndex).toBe(-1);
        expect(tuesday1.fullDayIndex).toBe(-1);
        expect(tuesday2.fullDayIndex).toBe(-1);
        expect(wednesday.fullDayIndex).toBe(-1);
        expect(thursday1.fullDayIndex).toBe(-1);
        expect(thursday2.fullDayIndex).toBe(-1);
        expect(friday.fullDayIndex).toBe(1);
      });
    });
  });

  describe('navigation', () => {

    it('navigate forward and back (with first day of month selected)', () => {
      // empty parent div
      let $div = $('<div></div>');

      let cal = scout.create(SpecCalendar, {
        parent: session.desktop,
        selectedDate: '2016-01-01 12:00:00.000',
        displayMode: Calendar.DisplayMode.MONTH
      });
      cal.render($div);

      let viewRange = cal.viewRange;
      let selectedDate = cal.selectedDate;

      // go two months forward, four month back and two  month forward
      // (navigate over JAN/FEB (31. vs. 27. days) month-boundary and 2015/2016 year-boundary)
      for (let f1 = 0; f1 < 2; f1++) {
        cal._onNextClick();
      }
      for (let b1 = 0; b1 < 4; b1++) {
        cal._onPreviousClick();
      }
      for (let f2 = 0; f2 < 2; f2++) {
        cal._onNextClick();
      }

      // expect viewRange is the same as before navigation
      expect(cal.viewRange).toEqual(viewRange);
      // expect selectedDate is the same as before navigation
      expect(cal.selectedDate).toEqual(selectedDate);
    });

    it('navigate forward and back (with last day of month selected)', () => {
      // empty parent div
      let $div = $('<div></div>');

      let cal = scout.create(SpecCalendar, {
        parent: session.desktop,
        selectedDate: '2016-01-31 12:00:00.000',
        displayMode: Calendar.DisplayMode.MONTH
      });
      cal.render($div);

      let viewRange = cal.viewRange;

      // go two months forward, four month back and two  month forward
      // (navigate over JAN/FEB (31. vs. 27. days) month-boundary and 2015/2016 year-boundary)
      for (let f1 = 0; f1 < 2; f1++) {
        cal._onNextClick();
      }
      for (let b1 = 0; b1 < 4; b1++) {
        cal._onPreviousClick();
      }
      for (let f2 = 0; f2 < 2; f2++) {
        cal._onNextClick();
      }

      // expect viewRange is the same as before navigation
      expect(cal.viewRange).toEqual(viewRange);

      // expect selectedDate is the same as 2016-01-29,
      // because the day was shifted to 29 while navigating over Feb. 2016
      expect(cal.selectedDate).toEqual(dates.parseJsonDate('2016-01-29 12:00:00.000'));
    });
  });

  describe('multiple calendars', () => {
    let stringDay = '2023-10-27 00:00:00.000';
    let day = dates.parseJsonDate(stringDay);
    let dateRangeNoon = {from: '2023-10-27 12:00:00.000', to: '2023-10-27 12:30:00.000'};

    const createCalendarComponent = (calendar: Calendar, fromDate: string, toDate: string, calendarId?: string, fullDay?: boolean): CalendarComponent => {
      let model = {
        parent: calendar,
        item: {
          calendarId: calendarId
        } as unknown as CalendarItem,
        fromDate: fromDate,
        toDate: toDate,
        coveredDaysRange: {
          from: fromDate,
          to: toDate
        },
        fullDay: fullDay
      };
      let comp = scout.create(CalendarComponent, model);
      calendar.addComponents([comp]);
      return comp;
    };

    const createCalendarDescriptor = (name = 'Test calendar', visible = true, selectable = true): CalendarDescriptor => {
      let calendarId = UuidPool.take(session);
      return {
        calendarId: calendarId,
        name: name,
        visible: visible,
        selectable: selectable
      };
    };

    const getCurrentCalendarIdFor = (comp: CalendarComponent): string | number => {
      return comp._$parts[0].parents('.calendar-column').data('calendarId');
    };

    const initCalendar = (...calendars: CalendarDescriptor[]): SpecCalendar => {
      let calendar = scout.create(SpecCalendar, {
        parent: session.desktop,
        selectedDate: day,
        calendars: calendars
      });
      calendar.render();
      return calendar;
    };

    it('should render components without calendarId in default column in day view', () => {
      // Arrange
      let businessCalendar = createCalendarDescriptor();
      let calendar = initCalendar(businessCalendar);
      let comp = createCalendarComponent(calendar, dateRangeNoon.from, dateRangeNoon.to);
      calendar.setDisplayMode(Calendar.DisplayMode.DAY);

      // Act
      let calendarIdData = getCurrentCalendarIdFor(comp);

      // Assert
      expect(calendarIdData).toEqual(calendar.defaultCalendar.calendarId);
    });

    it('should render components with calendarId in corresponding column in day view', () => {
      // Arrange
      let businessCalendar = createCalendarDescriptor('Business calendar');
      let otherCalendar = createCalendarDescriptor('Other calendar', true, false);
      let calendar = initCalendar(businessCalendar, otherCalendar);
      let comp = createCalendarComponent(calendar, dateRangeNoon.from, dateRangeNoon.to, businessCalendar.calendarId);
      calendar.setDisplayMode(Calendar.DisplayMode.DAY);

      // Act
      let calendarIdData = getCurrentCalendarIdFor(comp);

      // Assert
      expect(calendarIdData).toEqual(businessCalendar.calendarId);
    });

    it('should move component from default to corresponding calendar column when displayMode is changed from week to day', () => {
      // Arrange
      let businessCalendar = createCalendarDescriptor('Business calendar');
      let calendar = initCalendar(businessCalendar);
      calendar.setDisplayMode(Calendar.DisplayMode.WORK_WEEK);
      let comp = createCalendarComponent(calendar, dateRangeNoon.from, dateRangeNoon.to, businessCalendar.calendarId);

      // Act
      let calendarIdForWeek = getCurrentCalendarIdFor(comp);
      calendar.setDisplayMode(Calendar.DisplayMode.DAY);
      let calendarIdForDay = getCurrentCalendarIdFor(comp);
      calendar.setDisplayMode(Calendar.DisplayMode.MONTH);
      let calendarIdForMonth = getCurrentCalendarIdFor(comp);

      // Assert
      expect(calendarIdForWeek).toBe(calendar.defaultCalendar.calendarId);
      expect(calendarIdForDay).toBe(businessCalendar.calendarId);
      expect(calendarIdForMonth).toBe(calendar.defaultCalendar.calendarId);
    });

    it('should hide components, when calendar is made invisible on week view', () => {
      // Arrange
      let businessCalendar = createCalendarDescriptor('Business calendar');
      let otherCalendar = createCalendarDescriptor('Other calendar', true, false);
      let calendar = initCalendar(businessCalendar, otherCalendar);
      calendar.setDisplayMode(Calendar.DisplayMode.WEEK);
      let businessComp = createCalendarComponent(calendar, dateRangeNoon.from, dateRangeNoon.to, businessCalendar.calendarId);
      let externalComp = createCalendarComponent(calendar, dateRangeNoon.from, dateRangeNoon.to, otherCalendar.calendarId);

      // Act
      calendar._updateCalendarVisibility([[otherCalendar.calendarId, false]]);

      // Assert
      expect(businessComp.visible).toBe(true);
      expect(externalComp.visible).toBe(false);
    });

    // No range selection on disabled column
    it('should not apply a selection on calendars which are not selectable', () => {
      // Arrange
      let nonSelectableCalendar = createCalendarDescriptor('Non selectable calendar', true, false);
      let calendar = initCalendar(nonSelectableCalendar);
      calendar.setDisplayMode(Calendar.DisplayMode.DAY);

      // Act
      calendar._setSelection(calendar.selectedDate, nonSelectableCalendar, null, false, false);

      // Assert
      expect(calendar.selectedCalendar).not.toBe(nonSelectableCalendar);
    });

    it('should correctly update full day indices on day', () => {
      // Arrange
      let businessCalendar = createCalendarDescriptor('Business calendar');
      let externalCalendar = createCalendarDescriptor('External calendar', true, false);
      let calendar = initCalendar(businessCalendar, externalCalendar);

      let businessComp = createCalendarComponent(calendar, stringDay, stringDay, businessCalendar.calendarId, true);
      let secondBusniessComp = createCalendarComponent(calendar, stringDay, stringDay, businessCalendar.calendarId, true);
      let externalComp = createCalendarComponent(calendar, stringDay, stringDay, externalCalendar.calendarId, true);

      // Act
      calendar._updateFullDayIndices(calendar.components);

      // Assert
      expect(businessComp.fullDayIndex).toBe(0);
      expect(secondBusniessComp.fullDayIndex).toBe(1);
      expect(externalComp.fullDayIndex).toBe(0);
    });

    describe('multiple calendar selection visible', () => {

      const isCalendarsSelectionVisible = (calendar: Calendar): boolean => {
        return calendar.calendarSidebar.calendarsPanel.$container.height() > 40;
      };

      it('should hide calendars menu when no calendar is set', () => {
        // Arrange
        let calendar = initCalendar();

        // Act
        let menuVisible = isCalendarsSelectionVisible(calendar);

        // Assert
        expect(menuVisible).toBe(false);
      });

      it('should hide calendars menu when only one calendar is set', () => {
        // Arrange
        let calDesc = createCalendarDescriptor('Calendar');
        let calendar = initCalendar(calDesc);

        // Act
        let menuVisible = isCalendarsSelectionVisible(calendar);

        // Assert
        expect(menuVisible).toBe(false);
      });

      it('should make calendars menu visible when more than one calendar is set', () => {
        // Arrange
        let businessCal = createCalendarDescriptor('Business calendar');
        let otherCal = createCalendarDescriptor('Other calendar');
        let calendar = initCalendar(businessCal, otherCal);
        jasmine.clock().tick(500); // await the lookup

        // Act
        let menuVisible = isCalendarsSelectionVisible(calendar);

        // Assert
        expect(menuVisible).toBe(true);
      });

      it('should make calendars menu visible when an additional calendar is added', () => {
        // Arrange
        let businessCal = createCalendarDescriptor('Business calendar');
        let otherCal = createCalendarDescriptor('Other calendar');
        let calendar = initCalendar(businessCal);
        jasmine.clock().tick(500); // await the lookup

        // Act
        let menuVisibleFirst = isCalendarsSelectionVisible(calendar);
        calendar.setCalendars([...calendar.calendars, otherCal]);
        jasmine.clock().tick(500); // await the lookup
        let menuVisibleAfter = isCalendarsSelectionVisible(calendar);

        // Assert
        expect(menuVisibleFirst).toBe(false);
        expect(menuVisibleAfter).toBe(true);
      });
    });

    it('should not be a problem to have an empty named calendar descriptor', () => {
      // Arrange
      let unnamedCalendar = createCalendarDescriptor(null);
      let calendar = initCalendar(unnamedCalendar);
      let component = createCalendarComponent(calendar, stringDay, stringDay, unnamedCalendar.calendarId);
      calendar.setDisplayMode(Calendar.DisplayMode.DAY);

      // Act
      let currentComponenteCalendarId = getCurrentCalendarIdFor(component);

      // Assert
      expect(currentComponenteCalendarId).toBe(unnamedCalendar.calendarId);
    });

    describe('range selection on multiple calendar', () => {
      it('should apply selection on a selectable calendar', () => {
        // Arrange
        let selectableCalendar = createCalendarDescriptor('Selectable calendar', true, true);
        let calendar = initCalendar(selectableCalendar);
        calendar.setDisplayMode(Calendar.DisplayMode.DAY);

        // Act
        calendar._setSelection(new Date(stringDay), selectableCalendar, null, false, false);

        // Assert
        expect(calendar.selectedCalendar).toBe(selectableCalendar);
      });

      it('should not apply selection on a non-selectable calendar', () => {
        // Arrange
        let unselectableCalendar = createCalendarDescriptor('Selectable calendar', true, false);
        let calendar = initCalendar(unselectableCalendar);
        calendar.setDisplayMode(Calendar.DisplayMode.DAY);

        // Act
        calendar._setSelection(new Date(stringDay), unselectableCalendar, null, false, false);

        // Assert
        expect(calendar.selectedCalendar).toBe(null);
      });

      it('should preserve selected calendar when a non-selectable calendar is selected', () => {
        // Arrange
        let selectableCalendar = createCalendarDescriptor('Selectable calendar', true, true);
        let unselectableCalendar = createCalendarDescriptor('Selectable calendar', true, false);
        let calendar = initCalendar(selectableCalendar, unselectableCalendar);
        calendar.setDisplayMode(Calendar.DisplayMode.DAY);

        // Act
        calendar._setSelection(new Date(stringDay), selectableCalendar, null, false, false);
        calendar._setSelection(new Date(stringDay), unselectableCalendar, null, false, false);

        // Assert
        expect(calendar.selectedCalendar).toBe(selectableCalendar);
      });

      it('should be able to handle calendarId when selection is set', () => {
        // Arrange
        let selectableCalendar = createCalendarDescriptor('Selectable calendar', true, true);
        let calendar = initCalendar(selectableCalendar);
        calendar.setDisplayMode(Calendar.DisplayMode.DAY);

        // Act
        calendar._setSelection(new Date(stringDay), selectableCalendar.calendarId, null, false, false);

        // Assert
        expect(calendar.selectedCalendar).toBe(selectableCalendar);
      });
    });

    describe('one calendar should always be selected', () => {

      const clickTreeNodeForCalendarId = (calendar: Calendar, calendarId: string) => {
        let tree = calendar.calendarSidebar.calendarsPanel.treeBox.tree;
        tree.visitNodes(node => {
          if ((<TreeBoxTreeNode<string>>node).lookupRow.key === calendarId) {
            JQueryTesting.triggerClick(node.$node);
            return true;
          }
        });
      };

      it('should not be possible to uncheck the last checked calendar', () => {
        // Arrange
        let calendar1 = createCalendarDescriptor('Calendar 1');
        let calendar2 = createCalendarDescriptor('Calendar 2');
        let calendar = initCalendar(calendar1, calendar2);
        jasmine.clock().tick(500); // await the lookup

        // Act
        clickTreeNodeForCalendarId(calendar, calendar1.calendarId);
        clickTreeNodeForCalendarId(calendar, calendar2.calendarId);

        // Assert
        expect(calendar1.visible).toBe(false);
        expect(calendar2.visible).toBe(true);
      });

      it('should not be possible to uncheck the calendar when calendars are in group', () => {
        // Arrange
        let parentCalendar = createCalendarDescriptor('Parent calendar');
        let calendar1 = createCalendarDescriptor('Calendar 1');
        calendar1.parentId = parentCalendar.calendarId;
        let calendar2 = createCalendarDescriptor('Calendar 2');
        calendar2.parentId = parentCalendar.calendarId;
        let calendar = initCalendar(parentCalendar, calendar1, calendar2);
        jasmine.clock().tick(500); // await the lookup

        // Act
        clickTreeNodeForCalendarId(calendar, calendar1.calendarId);
        clickTreeNodeForCalendarId(calendar, calendar2.calendarId);

        // Assert
        expect(calendar1.visible).toBe(false);
        expect(calendar2.visible).toBe(true);
      });

      it('should not be possible to uncheck the calendar group when the group consists of the last selected calendars', () => {
        // Arrange
        let parentCalendar = createCalendarDescriptor('Parent calendar');
        let calendar1 = createCalendarDescriptor('Calendar 1');
        calendar1.parentId = parentCalendar.calendarId;
        let calendar2 = createCalendarDescriptor('Calendar 2');
        calendar2.parentId = parentCalendar.calendarId;
        let calendar = initCalendar(parentCalendar, calendar1, calendar2);
        jasmine.clock().tick(500); // await the lookup

        // Act
        clickTreeNodeForCalendarId(calendar, parentCalendar.calendarId);

        // Assert
        expect(calendar1.visible).toBe(true);
        expect(calendar2.visible).toBe(true);
      });

      it('should not hide a calendar when its double clicked', () => {
        // Arrange
        let parentCalendar = createCalendarDescriptor('Parent calendar');
        let calendar1 = createCalendarDescriptor('Calendar 1');
        calendar1.parentId = parentCalendar.calendarId;
        let calendar2 = createCalendarDescriptor('Calendar 2');
        calendar2.parentId = parentCalendar.calendarId;
        let calendar = initCalendar(parentCalendar, calendar1, calendar2);
        jasmine.clock().tick(500); // await the lookup

        // Act
        clickTreeNodeForCalendarId(calendar, calendar2.calendarId);
        clickTreeNodeForCalendarId(calendar, calendar1.calendarId);
        clickTreeNodeForCalendarId(calendar, calendar1.calendarId);

        // Assert
        expect(calendar1.visible).toBe(true);
        expect(calendar2.visible).toBe(false);
      });

      it('should not be possible to unselect the only calendar', () => {
        // Arrange
        let calendar1 = createCalendarDescriptor('Calendar 1');
        let calendar = initCalendar(calendar1);
        jasmine.clock().tick(500); // await the lookup

        // Act
        clickTreeNodeForCalendarId(calendar, calendar1.calendarId);

        // Assert
        expect(calendar1.visible).toBe(true);
      });
    });
  });
});
