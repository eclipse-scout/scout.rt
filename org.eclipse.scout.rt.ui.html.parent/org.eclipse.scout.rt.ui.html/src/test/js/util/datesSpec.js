describe("scout.dates", function() {

  describe("shift", function() {

    // Note: Test dates need explicit time zone setting, otherwise the result of toISOString (which always
    // returns UTC dates) would depend on the browser's time zone. For convenience, we use UTC as well ("+00:00").

    it("shifts year or month or day", function() {
      var date = scout.dates.create('2014-11-21');
      expect(scout.dates.shift(date, 1).toISOString()).toBe(scout.dates.create('2015-11-21 00:00:00.000').toISOString());

      date = scout.dates.create('2014-11-21');
      expect(scout.dates.shift(date, 0, 1).toISOString()).toBe(scout.dates.create('2014-12-21 00:00:00.000').toISOString());

      date = scout.dates.create('2014-11-21');
      expect(scout.dates.shift(date, 0, 0, 1).toISOString()).toBe(scout.dates.create('2014-11-22 00:00:00.000').toISOString());
    });

    it("shifts year and month if both provided", function() {
      var date = scout.dates.create('2014-01-01');
      expect(scout.dates.shift(date, 1, 1).toISOString()).toBe(scout.dates.create('2015-02-01 00:00:00.000').toISOString());
    });

    it("shifts year and month and day if all provided", function() {
      var date = scout.dates.create('2014-01-01');
      expect(scout.dates.shift(date, 1, 1, 1).toISOString()).toBe(scout.dates.create('2015-02-02 00:00:00.000').toISOString());
    });

    describe("shift year", function() {

      it("adds or removes years", function() {
        var date = scout.dates.create('2014-11-21');
        expect(scout.dates.shift(date, 1).toISOString()).toBe(scout.dates.create('2015-11-21 00:00:00.000').toISOString());

        date = scout.dates.create('2014-11-01');
        expect(scout.dates.shift(date, -1).toISOString()).toBe(scout.dates.create('2013-11-01 00:00:00.000').toISOString());
      });

      it("handles edge case leap year", function() {
        var date = scout.dates.create('2016-02-29T00:00:00.000');
        expect(scout.dates.shift(date, 1).toISOString()).toBe(scout.dates.create('2017-02-28 00:00:00.000').toISOString());
      });

    });

    describe("shift month", function() {

      it("adds or removes months", function() {
        var date = scout.dates.create('2014-11-21');
        expect(scout.dates.shift(date, 0, 1).toISOString()).toBe(scout.dates.create('2014-12-21 00:00:00.000').toISOString());

        date = scout.dates.create('2014-12-21');
        expect(scout.dates.shift(date, 0, -1).toISOString()).toBe(scout.dates.create('2014-11-21 00:00:00.000').toISOString());

        // Check if it also works when we cross the "daylight saving time" border (saving->normal)
        date = scout.dates.create('2014-11-21');
        expect(scout.dates.shift(date, 0, -1).toISOString()).toBe(scout.dates.create('2014-10-21 00:00:00.000').toISOString());
        date = scout.dates.create('2014-10-21');
        expect(scout.dates.shift(date, 0, 1).toISOString()).toBe(scout.dates.create('2014-11-21 00:00:00.000').toISOString());

        // Check if it also works when we cross the "daylight saving time" border (normal->saving)
        date = scout.dates.create('2014-04-10');
        expect(scout.dates.shift(date, 0, -1).toISOString()).toBe(scout.dates.create('2014-03-10 00:00:00.000').toISOString());
        date = scout.dates.create('2014-03-10');
        expect(scout.dates.shift(date, 0, 1).toISOString()).toBe(scout.dates.create('2014-04-10 00:00:00.000').toISOString());

        date = scout.dates.create('2014-11-21');
        expect(scout.dates.shift(date, 0, 12).toISOString()).toBe(scout.dates.create('2015-11-21 00:00:00.000').toISOString());

        date = scout.dates.create('2014-11-21');
        expect(scout.dates.shift(date, 0, -12).toISOString()).toBe(scout.dates.create('2013-11-21 00:00:00.000').toISOString());
      });

      it("handles edge case start month", function() {
        var date = scout.dates.create('2014-11-01');
        expect(scout.dates.shift(date, 0, 1).toISOString()).toBe(scout.dates.create('2014-12-01 00:00:00.000').toISOString());

        date = scout.dates.create('2014-11-01');
        expect(scout.dates.shift(date, 0, -1).toISOString()).toBe(scout.dates.create('2014-10-01 00:00:00.000').toISOString());

        date = scout.dates.create('2014-11-01');
        expect(scout.dates.shift(date, 0, 12).toISOString()).toBe(scout.dates.create('2015-11-01 00:00:00.000').toISOString());

        date = scout.dates.create('2014-11-01');
        expect(scout.dates.shift(date, 0, -12).toISOString()).toBe(scout.dates.create('2013-11-01 00:00:00.000').toISOString());

        date = scout.dates.create('2014-11-01');
        expect(scout.dates.shift(date, 0, 13).toISOString()).toBe(scout.dates.create('2015-12-01 00:00:00.000').toISOString());

        date = scout.dates.create('2014-11-01');
        expect(scout.dates.shift(date, 0, -13).toISOString()).toBe(scout.dates.create('2013-10-01 00:00:00.000').toISOString());
      });

      it("handles edge case end month", function() {
        var date = scout.dates.create('2014-10-31');
        expect(scout.dates.shift(date, 0, 1).toISOString()).toBe(scout.dates.create('2014-11-30 00:00:00.000').toISOString());

        date = scout.dates.create('2014-10-31');
        expect(scout.dates.shift(date, 0, -1).toISOString()).toBe(scout.dates.create('2014-09-30 00:00:00.000').toISOString());

        date = scout.dates.create('2014-10-31');
        expect(scout.dates.shift(date, 0, 12).toISOString()).toBe(scout.dates.create('2015-10-31 00:00:00.000').toISOString());

        date = scout.dates.create('2014-10-31');
        expect(scout.dates.shift(date, 0, 13).toISOString()).toBe(scout.dates.create('2015-11-30 00:00:00.000').toISOString());

        date = scout.dates.create('2014-10-31');
        expect(scout.dates.shift(date, 0, -13).toISOString()).toBe(scout.dates.create('2013-09-30 00:00:00.000').toISOString());

        date = scout.dates.create('2014-10-31');
        expect(scout.dates.shift(date, 0, -25).toISOString()).toBe(scout.dates.create('2012-09-30 00:00:00.000').toISOString());
      });

      it("handles edge case leap year", function() {
        var date = scout.dates.create('2016-02-29');
        expect(scout.dates.shift(date, 0, 12).toISOString()).toBe(scout.dates.create('2017-02-28 00:00:00.000').toISOString());
      });
    });

  });

  describe("isSameDay", function() {
    it("returns true if day, month and year matches", function() {
      var date = scout.dates.create('2014-11-21');
      var date2 = scout.dates.create('2014-11-21 11:13');
      expect(scout.dates.isSameDay(date, date2)).toBe(true);

      date = scout.dates.create('2014-11-21');
      date2 = scout.dates.create('2014-11-21');
      expect(scout.dates.isSameDay(date, date2)).toBe(true);

      date = scout.dates.create('2014-11-21');
      date2 = scout.dates.create('2014-11-20');
      expect(scout.dates.isSameDay(date, date2)).toBe(false);

      date = scout.dates.create('2014-11-21');
      date2 = scout.dates.create('2014-10-21');
      expect(scout.dates.isSameDay(date, date2)).toBe(false);

      date = scout.dates.create('2014-11-21');
      date2 = scout.dates.create('2013-11-21');
      expect(scout.dates.isSameDay(date, date2)).toBe(false);

      date = new Date('2014-11-21');
      date2 = new Date('2014-11-20T22:00:00.000-02:00');
      expect(scout.dates.isSameDay(date, date2)).toBe(true);
    });

  });


  describe("compareMonths", function() {
    it("returns the differences in number of months", function() {
      var date = scout.dates.create('2014-11-21');
      var date2 = scout.dates.create('2014-11-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(0);

      date = scout.dates.create('2014-11-21');
      date2 = scout.dates.create('2014-12-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(-1);

      date = scout.dates.create('2014-11-21');
      date2 = scout.dates.create('2014-10-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(1);
    });

    it("ignores time", function() {
      var date = scout.dates.create('2014-11-21T23:00');
      var date2 = scout.dates.create('2014-11-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(0);

      date = scout.dates.create('2014-11-21');
      date2 = scout.dates.create('2014-12-21T15:15');
      expect(scout.dates.compareMonths(date, date2)).toBe(-1);

      date = scout.dates.create('2014-11-21T20:20');
      date2 = scout.dates.create('2014-10-21T15:10');
      expect(scout.dates.compareMonths(date, date2)).toBe(1);
    });

    it("works with different years", function() {
      var date = scout.dates.create('2014-11-21');
      var date2 = scout.dates.create('2013-11-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(12);

      date = scout.dates.create('2014-11-21');
      date2 = scout.dates.create('2015-11-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(-12);

      date = scout.dates.create('2014-11-21');
      date2 = scout.dates.create('2013-10-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(13);

      date = scout.dates.create('2014-11-21');
      date2 = scout.dates.create('2015-12-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(-13);
    });

  });

  describe("timestamp", function() {

    it("returns a string of the expected length withonly digits", function() {
      var ts = scout.dates.timestamp();
      expect(typeof ts).toBe('string');
      expect(ts.length).toBe(17);
      expect(/^\d+$/.test(ts)).toBe(true);

      var date = scout.dates.create('2014-11-21 00:33:00.000Z');
      expect(scout.dates.timestamp(date)).toBe('20141121003300000');
    });

  });

  describe("orderWeekdays", function() {

    it("orders weekdays", function() {
      var weekdays = ['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa'];
      var check0 = scout.dates.orderWeekdays(weekdays, 0);
      var check1 = scout.dates.orderWeekdays(weekdays, 1);
      var check2 = scout.dates.orderWeekdays(weekdays, 2);
      var check3 = scout.dates.orderWeekdays(weekdays, 3);
      expect(check0.join('-')).toBe('So-Mo-Di-Mi-Do-Fr-Sa');
      expect(check1.join('-')).toBe('Mo-Di-Mi-Do-Fr-Sa-So');
      expect(check2.join('-')).toBe('Di-Mi-Do-Fr-Sa-So-Mo');
      expect(check3.join('-')).toBe('Mi-Do-Fr-Sa-So-Mo-Di');
    });

  });

  describe("toJsonDate / parseJsonDate", function() {

    it("can handle missing or invalid inputs", function() {
      expect(scout.dates.toJsonDate()).toBe(undefined);
      expect(scout.dates.parseJsonDate()).toBe(undefined);
      expect(function() {
        scout.dates.parseJsonDate('invalid date string');
      }).toThrow();
    });

    it("can convert JSON and JS dates", function() {
      var date, jsonDate;

      // Test 1 - UTC
      date = scout.dates.parseJsonDate('2014-11-21 00:00:00.000Z');
      expect(date).not.toBe(undefined);
      expect(date).not.toBe(null);
      expect(date.toISOString()).toBe('2014-11-21T00:00:00.000Z');
      jsonDate = scout.dates.toJsonDate(date, true);
      expect(jsonDate).toBe('2014-11-21 00:00:00.000Z');
      // Date only
      date = scout.dates.parseJsonDate('2014-11-21Z');
      expect(date.toISOString()).toBe('2014-11-21T00:00:00.000Z');
      // Time only
      date = scout.dates.parseJsonDate('15:23:00.123Z');
      expect(date.toISOString()).toBe('1970-01-01T15:23:00.123Z');
      expect(function() {
        scout.dates.parseJsonDate('15:23:00');
      }).toThrow(); // missing millis

      // Test 2 - local time zone
      date = scout.dates.parseJsonDate('2014-11-21 00:00:00.000');
      expect(date).not.toBe(undefined);
      expect(date).not.toBe(null);
      // We cannot check for the exact value of date, because we don't know the executing browser's time zone.
      // But we can convert it back to JSON, which should result in the original string (because TZ are the same).
      jsonDate = scout.dates.toJsonDate(date);
      expect(jsonDate).toBe('2014-11-21 00:00:00.000');
      jsonDate = scout.dates.toJsonDate(date, false); // should be the same as above
      expect(jsonDate).toBe('2014-11-21 00:00:00.000');
    });

  });

  describe("create", function() {

    it("can create dates", function() {
      expect(scout.dates.create()).toBe(undefined);
      expect(scout.dates.create('')).toBe(undefined);
      expect(function() {
        scout.dates.create('invalid date string');
      }).toThrow();

      expect(scout.dates.create('2014').toISOString()).toBe(scout.dates.create('2014-01-01 00:00:00.000').toISOString());
      expect(scout.dates.create('2014-10').toISOString()).toBe(scout.dates.create('2014-10-01 00:00:00.000').toISOString());
      expect(scout.dates.create('2014-10-31').toISOString()).toBe(scout.dates.create('2014-10-31 00:00:00.000').toISOString());
      expect(scout.dates.create('2014-10-31 23').toISOString()).toBe(scout.dates.create('2014-10-31 23:00:00.000').toISOString());
      expect(scout.dates.create('2014-10-31 23:59').toISOString()).toBe(scout.dates.create('2014-10-31 23:59:00.000').toISOString());
      expect(scout.dates.create('2014-10-31 23:59:58').toISOString()).toBe(scout.dates.create('2014-10-31 23:59:58.000').toISOString());
      expect(scout.dates.create('2014-10-31 23:59:58.882').toISOString()).toBe(scout.dates.create('2014-10-31 23:59:58.882').toISOString());
      expect(scout.dates.create('2014-10-31 23:59:58.882Z').toISOString()).toBe(scout.dates.create('2014-10-31 23:59:58.882Z').toISOString());
    });

  });

});
