describe("scout.dates", function() {

  describe("shift", function() {

    it("shifts year or month or day", function() {
      var date = new Date('2014-11-21');
      expect(scout.dates.shift(date, 1).toISOString()).toBe('2015-11-21T00:00:00.000Z');

      date = new Date('2014-11-21');
      expect(scout.dates.shift(date, 0, 1).toISOString()).toBe('2014-12-21T00:00:00.000Z');

      date = new Date('2014-11-21');
      expect(scout.dates.shift(date, 0, 0, 1).toISOString()).toBe('2014-11-22T00:00:00.000Z');
    });

    it("shifts year and month if both provided", function() {
      var date = new Date('2014-01-01');
      expect(scout.dates.shift(date, 1, 1).toISOString()).toBe('2015-02-01T00:00:00.000Z');
    });

    it("shifts year and month and day if all provided", function() {
      var date = new Date('2014-01-01');
      expect(scout.dates.shift(date, 1, 1, 1).toISOString()).toBe('2015-02-02T00:00:00.000Z');
    });

    describe("shift year", function() {

      it("adds or removes years", function() {
        var date = new Date('2014-11-21');
        expect(scout.dates.shift(date, 1).toISOString()).toBe('2015-11-21T00:00:00.000Z');

        date = new Date('2014-11-01');
        expect(scout.dates.shift(date, -1).toISOString()).toBe('2013-11-01T00:00:00.000Z');
      });

      it("handles edge case leap year", function() {
        var date = new Date('2016-02-29');
        expect(scout.dates.shift(date, 1).toISOString()).toBe('2017-02-28T00:00:00.000Z');
      });

    });

    describe("shift month", function() {

      it("adds or removes months", function() {
        var date = new Date('2014-11-21');
        expect(scout.dates.shift(date, 0, 1).toISOString()).toBe('2014-12-21T00:00:00.000Z');

        date = new Date('2014-11-21');
        expect(scout.dates.shift(date, 0, -1).toISOString()).toBe('2014-10-21T00:00:00.000Z');

        date = new Date('2014-11-21');
        expect(scout.dates.shift(date, 0, 12).toISOString()).toBe('2015-11-21T00:00:00.000Z');

        date = new Date('2014-11-21');
        expect(scout.dates.shift(date, 0, -12).toISOString()).toBe('2013-11-21T00:00:00.000Z');
      });

      it("handles edge case start month", function() {
        var date = new Date('2014-11-01');
        expect(scout.dates.shift(date, 0, 1).toISOString()).toBe('2014-12-01T00:00:00.000Z');

        date = new Date('2014-11-01');
        expect(scout.dates.shift(date, 0, -1).toISOString()).toBe('2014-10-01T00:00:00.000Z');

        date = new Date('2014-11-01');
        expect(scout.dates.shift(date, 0, 12).toISOString()).toBe('2015-11-01T00:00:00.000Z');

        date = new Date('2014-11-01');
        expect(scout.dates.shift(date, 0, -12).toISOString()).toBe('2013-11-01T00:00:00.000Z');

        date = new Date('2014-11-01');
        expect(scout.dates.shift(date, 0, 13).toISOString()).toBe('2015-12-01T00:00:00.000Z');

        date = new Date('2014-11-01');
        expect(scout.dates.shift(date, 0, -13).toISOString()).toBe('2013-10-01T00:00:00.000Z');
      });

      it("handles edge case end month", function() {
        var date = new Date('2014-10-31');
        expect(scout.dates.shift(date, 0, 1).toISOString()).toBe('2014-11-30T00:00:00.000Z');

        date = new Date('2014-10-31');
        expect(scout.dates.shift(date, 0, -1).toISOString()).toBe('2014-09-30T00:00:00.000Z');

        date = new Date('2014-10-31');
        expect(scout.dates.shift(date, 0, 12).toISOString()).toBe('2015-10-31T00:00:00.000Z');

        date = new Date('2014-10-31');
        expect(scout.dates.shift(date, 0, 13).toISOString()).toBe('2015-11-30T00:00:00.000Z');

        date = new Date('2014-10-31');
        expect(scout.dates.shift(date, 0, -13).toISOString()).toBe('2013-09-30T00:00:00.000Z');

        date = new Date('2014-10-31');
        expect(scout.dates.shift(date, 0, -25).toISOString()).toBe('2012-09-30T00:00:00.000Z');
      });

      it("handles edge case leap year", function() {
        var date = new Date('2016-02-29');
        expect(scout.dates.shift(date, 0, 12).toISOString()).toBe('2017-02-28T00:00:00.000Z');
      });
    });

  });

  describe("isSameDay", function() {
    it("returns true if day, month and year matches", function() {
      var date = new Date('2014-11-21');
      var date2 = new Date('2014-11-21T11:13');
      expect(scout.dates.isSameDay(date, date2)).toBe(true);

      date = new Date('2014-11-21');
      date2 = new Date('2014-11-21');
      expect(scout.dates.isSameDay(date, date2)).toBe(true);

      date = new Date('2014-11-21');
      date2 = new Date('2014-11-20');
      expect(scout.dates.isSameDay(date, date2)).toBe(false);

      date = new Date('2014-11-21');
      date2 = new Date('2014-10-21');
      expect(scout.dates.isSameDay(date, date2)).toBe(false);

      date = new Date('2014-11-21');
      date2 = new Date('2013-11-21');
      expect(scout.dates.isSameDay(date, date2)).toBe(false);
    });

  });


  describe("compareMonths", function() {
    it("returns the differences in number of months", function() {
      var date = new Date('2014-11-21');
      var date2 = new Date('2014-11-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(0);

      date = new Date('2014-11-21');
      date2 = new Date('2014-12-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(-1);

      date = new Date('2014-11-21');
      date2 = new Date('2014-10-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(1);
    });

    it("ignores time", function() {
      var date = new Date('2014-11-21T23:00');
      var date2 = new Date('2014-11-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(0);

      date = new Date('2014-11-21');
      date2 = new Date('2014-12-21T15:15');
      expect(scout.dates.compareMonths(date, date2)).toBe(-1);

      date = new Date('2014-11-21T20:20');
      date2 = new Date('2014-10-21T15:10');
      expect(scout.dates.compareMonths(date, date2)).toBe(1);
    });

    it("works with different years", function() {
      var date = new Date('2014-11-21');
      var date2 = new Date('2013-11-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(12);

      date = new Date('2014-11-21');
      date2 = new Date('2015-11-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(-12);

      date = new Date('2014-11-21');
      date2 = new Date('2013-10-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(13);

      date = new Date('2014-11-21');
      date2 = new Date('2015-12-21');
      expect(scout.dates.compareMonths(date, date2)).toBe(-13);
    });

  });

  describe("timestamp", function() {

    it("returns a string of the expected length withonly digits", function() {
      var ts = scout.dates.timestamp();
      expect(typeof ts).toBe('string');
      expect(ts.length).toBe(17);
      expect(/^\d+$/.test(ts)).toBe(true);

      var date = new Date('21 Nov 2014 00:33:00 +0000');
      expect(scout.dates.timestamp(date)).toBe('20141121003300000');
    });

  });

});
