/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("Planner", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createPlannerModel(numResources) {
    var model = createSimpleModel('Planner', session);
    model.resources = [];
    for (var i = 0; i < numResources; i++) {
      model.resources[i] = createResource('resource' + i);
    }
    return model;
  }

  function createResource(text) {
    return {
      id: scout.objectFactory.createUniqueId(),
      resourceCell: {
        text: text
      },
      activities: [{
        beginTime: '2015-04-01 01:23:45.678Z',
        endTime: '2015-04-31 01:23:45.678Z',
        id: scout.objectFactory.createUniqueId()
      }, {
        beginTime: '2016-02-29 01:23:45.678Z',
        endTime: '2400-02-29 01:23:45.678Z',
        id: scout.objectFactory.createUniqueId()
      }]
    };
  }

  function createPlanner(model) {
    var planner = new scout.Planner();
    planner.init(model);
    return planner;
  }

  function find$Resources(planner) {
    return planner.$grid.find('.planner-resource');
  }

  function find$ActivitiesForResource(resource) {
    return resource.$cells.children('.planner-activity');
  }

  describe("deleteResources", function() {
    var model, planner, resource0, resource1, resource2;

    beforeEach(function() {
      model = createPlannerModel(3);
      planner = createPlanner(model);
      resource0 = model.resources[0];
      resource1 = model.resources[1];
      resource2 = model.resources[2];
    });

    it("deletes resources from model", function() {
      expect(planner.resources.length).toBe(3);
      expect(planner.resources[0]).toBe(resource0);
      expect(Object.keys(planner.resourceMap).length).toBe(3);
      expect(Object.keys(planner.activityMap).length).toBe(2 * 3);
      expect(planner.resourceMap[resource0.id]).toBe(resource0);
      expect(planner.activityMap[resource0.activities[0].id]).toBe(resource0.activities[0]);

      planner.deleteResources([resource0]);
      expect(planner.resources.length).toBe(2);
      expect(planner.resources[0]).toBe(resource1);
      expect(Object.keys(planner.resourceMap).length).toBe(2);
      expect(Object.keys(planner.activityMap).length).toBe(2 * 2);
      expect(planner.resourceMap[resource0.id]).toBeUndefined();
      expect(planner.activityMap[resource0.activities[0].id]).toBeUndefined();

      planner.deleteResources([resource1, resource2]);
      expect(Object.keys(planner.resourceMap).length).toBe(0);
      expect(Object.keys(planner.activityMap).length).toBe(0);
      expect(planner.resourceMap.length).toBe(0);
      expect(planner.activityMap.length).toBe(0);
    });

    it("deletes resources from html document", function() {
      planner.render(session.$entryPoint);
      expect(find$Resources(planner).length).toBe(3);

      planner.deleteResources([resource0]);
      expect(find$Resources(planner).length).toBe(2);
      expect(find$Resources(planner).eq(0).data('resource')).toBe(resource1);

      planner.deleteResources([resource1, resource2]);
      expect(find$Resources(planner).length).toBe(0);
    });

    it("also adjusts selectedResources and selectionRange if deleted resource was selected", function() {
      planner.selectedResources = [resource0];
      expect(planner.selectedResources.length).toBe(1);
      planner.deleteResources([resource0]);
      expect(planner.selectedResources.length).toBe(0);
      expect(planner.selectionRange.from).toBeUndefined();
      expect(planner.selectionRange.to).toBeUndefined();
    });

  });

  describe("updateResources", function() {
    var model, planner, resource0, resource1, resource2, $resource1;

    beforeEach(function() {
      model = createPlannerModel(3);
      planner = createPlanner(model);
      resource0 = model.resources[0];
      resource1 = model.resources[1];
      resource2 = model.resources[2];
    });

    it("updates resources in model", function() {
      expect(planner.resources[1]).toBe(resource1);
      expect(planner.resources[1].resourceCell.text).toBe('resource1');
      expect(planner.resourceMap[resource1.id]).toBe(planner.resources[1]);

      var updatedResource = createResource('new resource1');
      updatedResource.id = resource1.id;
      planner.updateResources([updatedResource]);
      expect(planner.resources[1]).not.toBe(resource1);
      expect(planner.resources[1].resourceCell.text).toBe('new resource1');
      expect(planner.resourceMap[resource1.id]).toBe(planner.resources[1]);
    });

    it("updates resources in html document", function() {
      planner.render(session.$entryPoint);
      $resource1 = find$Resources(planner).eq(1);
      expect($resource1.children('.resource-title').text()).toBe('resource1');
      expect($resource1[0]).toBe(resource1.$resource[0]);

      var updatedResource = createResource('new resource1');
      updatedResource.id = resource1.id;
      planner.updateResources([updatedResource]);
      $resource1 = find$Resources(planner).eq(1);
      expect($resource1.children('.resource-title').text()).toBe('new resource1');
      expect($resource1[0]).toBe(updatedResource.$resource[0]);
      expect($resource1.data('resource')).toBe(updatedResource);
    });

    it("updates activities", function() {
      planner.render(session.$entryPoint);
      $resource1 = find$Resources(planner).eq(1);
      var $activity0 = find$ActivitiesForResource(resource1);
      expect($activity0.text()).toBe('');
      expect($activity0[0]).toBe(resource1.activities[0].$activity[0]);

      var updatedResource = createResource('new resource1');
      updatedResource.id = resource1.id;
      updatedResource.activities[0].text = 'updated activity';
      planner.updateResources([updatedResource]);
      $resource1 = find$Resources(planner).eq(1);
      $activity0 = find$ActivitiesForResource(updatedResource);
      var updatedActivity = updatedResource.activities[0];
      expect($activity0.text()).toBe('updated activity');
      expect($activity0[0]).toBe(updatedActivity.$activity[0]);
      expect($activity0.data('activity')).toBe(updatedActivity);
      expect(planner.activityMap[updatedActivity.id]).toBe(updatedActivity);
    });
  });

  describe("renderScale", function() {
    var model, planner;

    beforeEach(function() {
      model = createPlannerModel(0);
      planner = createPlanner(model);
      planner.render(session.$entryPoint);
      planner.displayModeOptions = {};
    });

    describe("displayMode: DAY", function() {

      beforeEach(function() {
        planner.displayMode = scout.Planner.DisplayMode.DAY;
        planner.viewRange = new scout.DateRange(scout.dates.create('2016-06-20'), scout.dates.create('2016-06-21'));
      });

      afterEach(function() {
        planner._renderDisplayModeOptions();
        validateRendering();
      });

      function validateRendering() {
        var options, interval, labelPeriod, firstHourOfDay, lastHourOfDay, hours, hourParts, smallCount;
        options = planner.displayModeOptions[planner.displayMode];
        interval = options.interval;
        labelPeriod = options.labelPeriod;
        firstHourOfDay = options.firstHourOfDay;
        lastHourOfDay = options.lastHourOfDay;

        hourParts = 60 / interval;
        hours = lastHourOfDay - firstHourOfDay + 1;
        smallCount = hours * hourParts;

        // element count
        expect(planner.$timelineLarge.children().length).toBe(Math.min(hours, smallCount));
        expect(planner.$timelineSmall.children().length).toBe(smallCount);

        // labels
        for (var i = 0; i < smallCount; i++) {
          var visible = i % labelPeriod === 0;
          expect(planner.$timelineSmall.children()[i].classList.contains('label-invisible')).toBe(!visible);
          var labalValue = planner._dateFormat(new Date().setMinutes((i % hourParts) * interval), ':mm');
          expect(planner.$timelineSmall.children()[i].textContent).toBe(labalValue);
        }
      }

      it("draws scale for whole day", function() {
        planner.displayModeOptions[planner.displayMode] = {
          interval: 30,
          labelPeriod: 1,
          firstHourOfDay: 0,
          lastHourOfDay: 23
        };
      });

      it("draws scale for one hour", function() {
        planner.displayModeOptions[planner.displayMode] = {
          interval: 30,
          labelPeriod: 1,
          firstHourOfDay: 0,
          lastHourOfDay: 0
        };
      });

      it("draws scale for two hour interval", function() {
        planner.displayModeOptions[planner.displayMode] = {
          interval: 120,
          labelPeriod: 1,
          firstHourOfDay: 0,
          lastHourOfDay: 23
        };
      });

      it("draws scale with only showing every second label", function() {
        planner.displayModeOptions[planner.displayMode] = {
          interval: 30,
          labelPeriod: 2,
          firstHourOfDay: 0,
          lastHourOfDay: 0
        };
      });

    });

    describe("displayMode: WEEK / WORK_WEEK", function() {

      beforeEach(function() {
        planner.displayMode = scout.Planner.DisplayMode.WEEK;
        planner.viewRange = new scout.DateRange(scout.dates.create('2016-06-20'), scout.dates.create('2016-06-27'));
      });

      afterEach(function() {
        planner._renderDisplayModeOptions();
        validateRendering();
      });

      function validateRendering() {
        var options, interval, labelPeriod, firstHourOfDay, lastHourOfDay, hours, days, dayParts, smallCount;
        options = planner.displayModeOptions[planner.displayMode];
        interval = options.interval;
        labelPeriod = options.labelPeriod;
        firstHourOfDay = options.firstHourOfDay;
        lastHourOfDay = options.lastHourOfDay;

        days = (planner.viewRange.to - planner.viewRange.from) / (24 * 60 * 60 * 1000);
        hours = lastHourOfDay - firstHourOfDay + 1;

        // cap interval to first-/lastHourOfDay view range
        interval = Math.min(hours * 60, interval);

        dayParts = (hours * 60) / interval;
        smallCount = days * dayParts;

        // element count
        expect(planner.$timelineLarge.children().length).toBe(days);
        expect(planner.$timelineSmall.children().length).toBe(smallCount);

        // labels
        for (var i = 0; i < smallCount; i++) {
          var visible = i % labelPeriod === 0;
          expect(planner.$timelineSmall.children()[i].classList.contains('label-invisible')).toBe(!visible);
          var labalValue = planner._dateFormat(scout.dates.shiftTime(new Date(planner.viewRange.from.valueOf()), 0, interval * (i % dayParts), 0, 0), 'HH:mm');
          expect(planner.$timelineSmall.children()[i].textContent).toBe(labalValue);
        }
      }

      it("draws scale for WEEK for whole day with 6h interval", function() {
        planner.displayModeOptions[planner.displayMode] = {
          interval: 360,
          labelPeriod: 1,
          firstHourOfDay: 0,
          lastHourOfDay: 23
        };
      });

      it("draws scale for WEEK with only showing every second label", function() {
        planner.displayModeOptions[planner.displayMode] = {
          interval: 360,
          labelPeriod: 2,
          firstHourOfDay: 0,
          lastHourOfDay: 23
        };
      });

      it("draws scale for WEEK with changing month", function() {
        planner.viewRange = new scout.DateRange(scout.dates.create('2016-06-27'), scout.dates.create('2016-07-04'));
        planner.displayModeOptions[planner.displayMode] = {
          interval: 360,
          labelPeriod: 2,
          firstHourOfDay: 0,
          lastHourOfDay: 23
        };
      });

      it("draws scale for WORK_WEEK for whole day with 6h interval", function() {
        planner.displayMode = scout.Planner.DisplayMode.WORK_WEEK;
        planner.viewRange = new scout.DateRange(scout.dates.create('2016-06-20'), scout.dates.create('2016-06-25'));
        planner.displayModeOptions[planner.displayMode] = {
          interval: 360,
          labelPeriod: 1,
          firstHourOfDay: 0,
          lastHourOfDay: 23
        };
      });

      it("draws scale for WORK_WEEK with only showing every second label", function() {
        planner.displayMode = scout.Planner.DisplayMode.WORK_WEEK;
        planner.viewRange = new scout.DateRange(scout.dates.create('2016-06-20'), scout.dates.create('2016-06-25'));
        planner.displayModeOptions[planner.displayMode] = {
          interval: 360,
          labelPeriod: 1,
          firstHourOfDay: 0,
          lastHourOfDay: 23
        };
      });
    });

    describe("displayMode: MONTH", function() {

      beforeEach(function() {
        planner.displayMode = scout.Planner.DisplayMode.MONTH;
        planner.viewRange = new scout.DateRange(scout.dates.create('2016-06-20'), scout.dates.create('2016-08-20'));
      });

      afterEach(function() {
        planner._renderDisplayModeOptions();
        validateRendering();
      });

      function validateRendering() {
        var options, interval, labelPeriod, months, days;
        options = planner.displayModeOptions[planner.displayMode];
        labelPeriod = options.labelPeriod;

        months = planner.viewRange.to.getMonth() - planner.viewRange.from.getMonth() + 1;
        if (months < 0) {
          months += 12;
        }
        days = (planner.viewRange.to - planner.viewRange.from) / (24 * 60 * 60 * 1000);

        // element count
        expect(planner.$timelineLarge.children().length).toBe(months);
        expect(planner.$timelineSmall.children().length).toBe(days);

        // labels
        for (var i = 0; i < days; i++) {
          var visible = scout.dates.shift(planner.viewRange.from, 0, 0, i).getDate() % labelPeriod === 0;
          expect(planner.$timelineSmall.children()[i].classList.contains('label-invisible')).toBe(!visible);
          var labalValue = planner._dateFormat(scout.dates.shift(new Date(planner.viewRange.from.valueOf()), 0, 0, i), 'dd');
          expect(planner.$timelineSmall.children()[i].textContent).toBe(labalValue);
        }
      }

      it("draws scale", function() {
        planner.displayModeOptions[planner.displayMode] = {
          labelPeriod: 1
        };
      });

      it("draws scale with only showing every second label", function() {
        planner.displayModeOptions[planner.displayMode] = {
          labelPeriod: 2
        };
      });

    });

    describe("displayMode: CALENDAR_WEEK", function() {

      beforeEach(function() {
        planner.displayMode = scout.Planner.DisplayMode.CALENDAR_WEEK;
        planner.viewRange = new scout.DateRange(scout.dates.create('2016-06-20'), scout.dates.create('2017-03-20'));
      });

      afterEach(function() {
        planner._renderDisplayModeOptions();
        validateRendering();
      });

      function validateRendering() {
        var options, interval, labelPeriod, months, weeks;
        options = planner.displayModeOptions[planner.displayMode];
        labelPeriod = options.labelPeriod;

        months = planner.viewRange.to.getMonth() - planner.viewRange.from.getMonth() + 1;
        if (months < 0) {
          months += 12;
        }
        weeks = scout.dates.weekInYear(scout.dates.trunc(planner.viewRange.to)) - scout.dates.weekInYear(scout.dates.trunc(planner.viewRange.from));
        if (weeks < 0) {
          weeks += scout.dates.weekInYear(scout.dates.create(planner.viewRange.from.getFullYear() + '-12-28'));
        }

        // element count
        expect(planner.$timelineLarge.children().length).toBe(months);
        expect(planner.$timelineSmall.children().length).toBe(weeks);

        // labels
        for (var i = 0; i < weeks; i++) {
          var weekInYear = scout.dates.weekInYear(scout.dates.shift(new Date(planner.viewRange.from.valueOf()), 0, 0, 7 * i));
          var visible = weekInYear % labelPeriod === 0;
          expect(planner.$timelineSmall.children()[i].classList.contains('label-invisible')).toBe(!visible);
          var labalValue = weekInYear + '';
          expect(planner.$timelineSmall.children()[i].textContent).toBe(labalValue);
        }
      }

      it("draws scale for CALENDAR_WEEK displayMode", function() {
        planner.displayModeOptions[planner.displayMode] = {
          labelPeriod: 1
        };
      });

      it("draws scale with only showing every second label", function() {
        planner.displayModeOptions[planner.displayMode] = {
          labelPeriod: 2
        };
      });

      it("draws scale with only showing every third label", function() {
        planner.displayModeOptions[planner.displayMode] = {
          labelPeriod: 3
        };
      });

    });

    describe("displayMode: YEAR", function() {

      beforeEach(function() {
        planner.displayMode = scout.Planner.DisplayMode.YEAR;
        planner.viewRange = new scout.DateRange(scout.dates.create('2016-06-20'), scout.dates.create('2018-06-20'));
      });

      afterEach(function() {
        planner._renderDisplayModeOptions();
        validateRendering();
      });

      function validateRendering() {
        var options, interval, labelPeriod, years, months;
        options = planner.displayModeOptions[planner.displayMode];
        labelPeriod = options.labelPeriod;

        years = planner.viewRange.to.getYear() - planner.viewRange.from.getYear() + 1;
        months = (planner.viewRange.to.getYear() - planner.viewRange.from.getYear()) * 12 + (planner.viewRange.to.getMonth() - planner.viewRange.from.getMonth());

        // element count
        expect(planner.$timelineLarge.children().length).toBe(years);
        expect(planner.$timelineSmall.children().length).toBe(months);

        // labels
        for (var i = 0; i < months; i++) {
          var visible = (scout.dates.shift(planner.viewRange.from, 0, i, 0).getMonth()) % labelPeriod === 0;
          expect(planner.$timelineSmall.children()[i].classList.contains('label-invisible')).toBe(!visible);
          var labalValue = planner._dateFormat(scout.dates.shift(new Date(planner.viewRange.from.valueOf()), 0, i, 0), 'MMMM');
          expect(planner.$timelineSmall.children()[i].textContent).toBe(labalValue);
        }
      }

      it("draws scale for YEAR displayMode", function() {
        planner.displayModeOptions[planner.displayMode] = {
          labelPeriod: 1
        };
      });

      it("draws scale with only showing every second label", function() {
        planner.displayModeOptions[planner.displayMode] = {
          labelPeriod: 2
        };
      });

      it("draws scale with only showing every third label", function() {
        planner.displayModeOptions[planner.displayMode] = {
          labelPeriod: 3
        };
      });
    });

  });

  describe("transformLeft/transformWidth", function() {
    var model, planner;

    beforeEach(function() {
      model = createPlannerModel(0);
      planner = createPlanner(model);
      planner.render(session.$entryPoint);
      planner.displayModeOptions = {};
    });

    it("calculates left and width in WEEK mode for whole days", function() {
      planner.viewRange = new scout.DateRange(scout.dates.create('2016-06-20'), scout.dates.create('2016-06-27'));
      planner.displayMode = scout.Planner.DisplayMode.WEEK;
      planner.displayModeOptions[planner.displayMode] = {
        interval: 360,
        firstHourOfDay: 0,
        lastHourOfDay: 23
      };
      planner._renderDisplayModeOptions();

      var options = planner.displayModeOptions[planner.displayMode];
      var cellWidthPercent = 100 / (((options.lastHourOfDay - options.firstHourOfDay + 1) * 7 * 60) / options.interval);

      expect(planner.transformLeft(scout.dates.create('2016-06-20 06:00:00'))).toBeCloseTo(cellWidthPercent, 5);
      expect(planner.transformWidth(scout.dates.create('2016-06-20 06:00:00'), scout.dates.create('2016-06-20 12:00:00'))).toBeCloseTo(cellWidthPercent, 5);

      expect(planner.transformLeft(scout.dates.create('2016-06-21 06:00:00'))).toBeCloseTo(5 * cellWidthPercent, 5);
      expect(planner.transformWidth(scout.dates.create('2016-06-20 06:00:00'), scout.dates.create('2016-06-21 12:00:00'))).toBeCloseTo(5 * cellWidthPercent, 5);

    });

    it("calculates left and width in WEEK mode for limitted day range", function() {
      planner.viewRange = new scout.DateRange(scout.dates.create('2016-06-20'), scout.dates.create('2016-06-27'));
      planner.displayMode = scout.Planner.DisplayMode.WEEK;
      planner.displayModeOptions[planner.displayMode] = {
        interval: 60,
        firstHourOfDay: 8,
        lastHourOfDay: 17
      };
      planner._renderDisplayModeOptions();

      var options = planner.displayModeOptions[planner.displayMode];
      var cellWidthPercent = 100 / (((options.lastHourOfDay - options.firstHourOfDay + 1) * 7 * 60) / options.interval);

      expect(planner.transformLeft(scout.dates.create('2016-06-20 09:00:00'))).toBeCloseTo(cellWidthPercent, 5);
      expect(planner.transformWidth(scout.dates.create('2016-06-20 09:00:00'), scout.dates.create('2016-06-20 12:00:00'))).toBeCloseTo(3 * cellWidthPercent, 5);

      expect(planner.transformLeft(scout.dates.create('2016-06-21 08:00:00'))).toBeCloseTo(10 * cellWidthPercent, 5);
      expect(planner.transformWidth(scout.dates.create('2016-06-20 16:00:00'), scout.dates.create('2016-06-21 09:00:00'))).toBeCloseTo(3 * cellWidthPercent, 5);
    });
  });
});
