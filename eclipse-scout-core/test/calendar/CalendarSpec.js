/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Calendar, CalendarComponent, dates, scout} from '../../src/index';

describe('Calendar', () => {
  let session;

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

  describe('init', () => {

    it('creates an empty calendar', () => {
      let cal = scout.create(Calendar, {parent: session.desktop});
      expect(cal.viewRange).toBeDefined();
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
    let cal, c1, c2, c3, c4, c5, c6, c7, c8;
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
      cal = scout.create(Calendar, {parent: session.desktop});
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
        cal._sort(components, day);
        expect(components[0] === c1).toBe(true);
        expect(components[1] === c2).toBe(true);
        expect(components[2] === c4).toBe(true);
      });
    });

    describe('arrangeComponents', () => {

      it('does nothing for no components', () => {
        let components = [];
        cal._arrange(components, day);
        expect(components).toEqual([]);
      });

      it('arranges a single component', () => {
        let components = [c1];
        cal._arrange(components, day);
        expect(components[0]).toEqual(c1);
        expect(c1.stack[day].x).toEqual(0);
        expect(c1.stack[day].w).toEqual(1);
      });

      it('arranges intersecting components', () => {
        let components = [c5, c1];
        cal._arrange(components, day);
        expect(components[0]).toEqual(c1);
        expect(components[1]).toEqual(c5);
        expect(c1.stack[day].x).toEqual(0);
        expect(c1.stack[day].w).toEqual(2);
        expect(c5.stack[day].x).toEqual(1);
        expect(c5.stack[day].w).toEqual(2);
      });

      it('arranges equal components', () => {
        let components = [c6, c7];
        cal._arrange(components, day);
        expect(components[0]).toEqual(c6);
        expect(components[1]).toEqual(c7);
        expect(c6.stack[day].x).toEqual(0);
        expect(c6.stack[day].w).toEqual(2);
        expect(c7.stack[day].x).toEqual(1);
        expect(c7.stack[day].w).toEqual(2);
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
        expect(c1.stack[day].w).toEqual(3);
        expect(c2.stack[day].w).toEqual(3);
        expect(c3.stack[day].w).toEqual(3);
        expect(c4.stack[day].w).toEqual(3);
        expect(c5.stack[day].w).toEqual(3);
        expect(c6.stack[day].w).toEqual(3);

        expect(c6.stack[day].x).toEqual(0);
        expect(c1.stack[day].x).toEqual(1);
        expect(c5.stack[day].x).toEqual(2);
        expect(c2.stack[day].x).toEqual(0);
        expect(c3.stack[day].x).toEqual(0);
        expect(c4.stack[day].x).toEqual(1);
      });

      it('reduces rows when arranging components', () => {
        let components = [c1, c3, c6];
        cal._arrange(components, day);
        expect(components[0]).toEqual(c6);
        expect(components[1]).toEqual(c1);
        expect(components[2]).toEqual(c3);
        expect(c6.stack[day].w).toEqual(2);
        expect(c1.stack[day].w).toEqual(2);
        expect(c3.stack[day].w).toEqual(1);

        expect(c6.stack[day].x).toEqual(0);
        expect(c1.stack[day].x).toEqual(1);
        expect(c3.stack[day].x).toEqual(0);
      });

      it('arranges intersecting components spanning more than one day', () => {
        let day1 = day;
        let components = [c8, c3];

        cal._arrange(components, day1);
        expect(components[0]).toEqual(c8);
        expect(components[1]).toEqual(c3);
        expect(c8.stack[day1].w).toEqual(2);
        expect(c3.stack[day1].w).toEqual(2);

        expect(c8.stack[day1].x).toEqual(0);
        expect(c3.stack[day1].x).toEqual(1);
      });

    });
  });

  describe('navigation', () => {

    it('navigate forward and back (with first day of month selected)', () => {
      // empty parent div
      let $div = $('<div></div>');

      let cal = scout.create(Calendar, {
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

      let cal = scout.create(Calendar, {
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

});
