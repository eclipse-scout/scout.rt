/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DateRange, dates, ObjectUuidProvider, Planner, PlannerActivity, PlannerModel, PlannerResourceModel, Widget} from '../../src/index';
import {ObjectType} from '../../src/ObjectFactory';
import {PlannerResource} from '../../src/planner/Planner';
import {InitModelOf} from '../../src/scout';

describe('Planner', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  class SpecPlanner extends Planner {
    override _renderDisplayModeOptions() {
      super._renderDisplayModeOptions();
    }

    override _dateFormat(date: Date, pattern: string): string {
      return super._dateFormat(date, pattern);
    }
  }

  function createPlannerModel(numResources): PlannerModel & { id: string; objectType: ObjectType<Planner>; parent: Widget; session: SandboxSession } {
    let model = createSimpleModel('Planner', session) as PlannerModel & { id: string; objectType: ObjectType<Planner>; parent: Widget; session: SandboxSession };
    model.resources = [];
    for (let i = 0; i < numResources; i++) {
      model.resources[i] = createResource('resource' + i);
    }
    return model;
  }

  function createResource(text?: string): PlannerResourceModel {
    return {
      id: ObjectUuidProvider.createUiId(),
      resourceCell: {
        text: text
      },
      activities: [{
        beginTime: '2015-04-01 01:23:45.678Z',
        endTime: '2015-04-31 01:23:45.678Z',
        id: ObjectUuidProvider.createUiId()
      }, {
        beginTime: '2016-02-29 01:23:45.678Z',
        endTime: '2400-02-29 01:23:45.678Z',
        id: ObjectUuidProvider.createUiId()
      }]
    };
  }

  function createPlanner(model: InitModelOf<Planner>): SpecPlanner {
    let planner = new SpecPlanner();
    planner.init(model);
    return planner;
  }

  function find$Resources(planner: Planner): JQuery {
    return planner.$grid.find('.planner-resource');
  }

  function find$ActivitiesForResource(resource: PlannerResourceModel): JQuery {
    return resource.$cells.children('.planner-activity');
  }

  describe('deleteResources', () => {
    let model: PlannerModel & { id: string; objectType: ObjectType<Planner>; parent: Widget; session: SandboxSession };
    let planner: SpecPlanner;
    let resource0: PlannerResource;
    let resource1: PlannerResource;
    let resource2: PlannerResource;

    beforeEach(() => {
      model = createPlannerModel(3);
      planner = createPlanner(model);
      resource0 = model.resources[0] as PlannerResource;
      resource1 = model.resources[1] as PlannerResource;
      resource2 = model.resources[2] as PlannerResource;
    });

    it('deletes resources from model', () => {
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
    });

    it('deletes resources from html document', () => {
      planner.render();
      expect(find$Resources(planner).length).toBe(3);

      planner.deleteResources([resource0]);
      expect(find$Resources(planner).length).toBe(2);
      expect(find$Resources(planner).eq(0).data('resource')).toBe(resource1);

      planner.deleteResources([resource1, resource2]);
      expect(find$Resources(planner).length).toBe(0);
    });

    it('also adjusts selectedResources and selectionRange if deleted resource was selected', () => {
      planner.selectedResources = [resource0];
      expect(planner.selectedResources.length).toBe(1);
      planner.deleteResources([resource0]);
      expect(planner.selectedResources.length).toBe(0);
      expect(planner.selectionRange.from).toBeUndefined();
      expect(planner.selectionRange.to).toBeUndefined();
    });

  });

  describe('updateResources', () => {
    let model: PlannerModel & { id: string; objectType: ObjectType<Planner>; parent: Widget; session: SandboxSession };
    let planner: SpecPlanner;
    let resource0: PlannerResource;
    let resource1: PlannerResource;
    let resource2: PlannerResource;
    let $resource1: JQuery;

    beforeEach(() => {
      model = createPlannerModel(3);
      planner = createPlanner(model);
      resource0 = model.resources[0] as PlannerResource;
      resource1 = model.resources[1] as PlannerResource;
      resource2 = model.resources[2] as PlannerResource;
    });

    it('updates resources in model', () => {
      expect(planner.resources[1]).toBe(resource1);
      expect(planner.resources[1].resourceCell.text).toBe('resource1');
      expect(planner.resourceMap[resource1.id]).toBe(planner.resources[1]);

      let updatedResource = createResource('new resource1');
      updatedResource.id = resource1.id;
      planner.updateResources([updatedResource]);
      expect(planner.resources[1]).not.toBe(resource1);
      expect(planner.resources[1].resourceCell.text).toBe('new resource1');
      expect(planner.resourceMap[resource1.id]).toBe(planner.resources[1]);
    });

    it('updates resources in html document', () => {
      planner.render();
      $resource1 = find$Resources(planner).eq(1);
      expect($resource1.children('.resource-title').text()).toBe('resource1');
      expect($resource1[0]).toBe(resource1.$resource[0]);

      let updatedResource = createResource('new resource1');
      updatedResource.id = resource1.id;
      planner.updateResources([updatedResource]);
      $resource1 = find$Resources(planner).eq(1);
      expect($resource1.children('.resource-title').text()).toBe('new resource1');
      expect($resource1[0]).toBe(updatedResource.$resource[0]);
      expect($resource1.data('resource')).toBe(updatedResource);
    });

    it('updates activities', () => {
      planner.render();
      $resource1 = find$Resources(planner).eq(1);
      let $activity0 = find$ActivitiesForResource(resource1);
      expect($activity0.text()).toBe('');
      expect($activity0[0]).toBe(resource1.activities[0].$activity[0]);

      let updatedResource = createResource('new resource1');
      updatedResource.id = resource1.id;
      updatedResource.activities[0].text = 'updated activity';
      planner.updateResources([updatedResource]);
      $resource1 = find$Resources(planner).eq(1);
      $activity0 = find$ActivitiesForResource(updatedResource);
      let updatedActivity = updatedResource.activities[0] as PlannerActivity;
      expect($activity0.text()).toBe('updated activity');
      expect($activity0[0]).toBe(updatedActivity.$activity[0]);
      expect($activity0.data('activity')).toBe(updatedActivity);
      expect(planner.activityMap[updatedActivity.id]).toBe(updatedActivity);
    });
  });

  describe('renderScale', () => {
    let model: PlannerModel & { id: string; objectType: ObjectType<Planner>; parent: Widget; session: SandboxSession };
    let planner: SpecPlanner;

    beforeEach(() => {
      model = createPlannerModel(0);
      planner = createPlanner(model);
      planner.render();
      // @ts-expect-error
      planner.displayModeOptions = {};
    });

    describe('displayMode: DAY', () => {

      beforeEach(() => {
        planner.displayMode = Planner.DisplayMode.DAY;
        planner.viewRange = new DateRange(dates.create('2016-06-20'), dates.create('2016-06-21'));
      });

      afterEach(() => {
        planner._renderDisplayModeOptions();
        validateRendering();
      });

      function validateRendering() {
        let options, interval, labelPeriod, firstHourOfDay, lastHourOfDay, hours, hourParts, smallCount;
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
        for (let i = 0; i < smallCount; i++) {
          let visible = i % labelPeriod === 0;
          expect(planner.$timelineSmall.children()[i].classList.contains('label-invisible')).toBe(!visible);
          let date = new Date();
          date.setMinutes((i % hourParts) * interval);
          let labelValue = planner._dateFormat(date, ':mm');
          expect(planner.$timelineSmall.children()[i].textContent).toBe(labelValue);
        }
      }

      it('draws scale for whole day', () => {
        planner.displayModeOptions[planner.displayMode] = {
          interval: 30,
          labelPeriod: 1,
          firstHourOfDay: 0,
          lastHourOfDay: 23
        };
      });

      it('draws scale for one hour', () => {
        planner.displayModeOptions[planner.displayMode] = {
          interval: 30,
          labelPeriod: 1,
          firstHourOfDay: 0,
          lastHourOfDay: 0
        };
      });

      it('draws scale for two hour interval', () => {
        planner.displayModeOptions[planner.displayMode] = {
          interval: 120,
          labelPeriod: 1,
          firstHourOfDay: 0,
          lastHourOfDay: 23
        };
      });

      it('draws scale with only showing every second label', () => {
        planner.displayModeOptions[planner.displayMode] = {
          interval: 30,
          labelPeriod: 2,
          firstHourOfDay: 0,
          lastHourOfDay: 0
        };
      });

    });

    describe('displayMode: WEEK / WORK_WEEK', () => {

      beforeEach(() => {
        planner.displayMode = Planner.DisplayMode.WEEK;
        planner.viewRange = new DateRange(dates.create('2016-06-20'), dates.create('2016-06-27'));
      });

      afterEach(() => {
        planner._renderDisplayModeOptions();
        validateRendering();
      });

      function validateRendering() {
        let options, interval, labelPeriod, firstHourOfDay, lastHourOfDay, hours, days, dayParts, smallCount;
        options = planner.displayModeOptions[planner.displayMode];
        interval = options.interval;
        labelPeriod = options.labelPeriod;
        firstHourOfDay = options.firstHourOfDay;
        lastHourOfDay = options.lastHourOfDay;

        days = (planner.viewRange.to.getTime() - planner.viewRange.from.getTime()) / (24 * 60 * 60 * 1000);
        hours = lastHourOfDay - firstHourOfDay + 1;

        // cap interval to first-/lastHourOfDay view range
        interval = Math.min(hours * 60, interval);

        dayParts = (hours * 60) / interval;
        smallCount = days * dayParts;

        // element count
        expect(planner.$timelineLarge.children().length).toBe(days);
        expect(planner.$timelineSmall.children().length).toBe(smallCount);

        // labels
        for (let i = 0; i < smallCount; i++) {
          let visible = i % labelPeriod === 0;
          expect(planner.$timelineSmall.children()[i].classList.contains('label-invisible')).toBe(!visible);
          let labelValue = planner._dateFormat(dates.shiftTime(new Date(planner.viewRange.from.valueOf()), 0, interval * (i % dayParts), 0, 0), 'HH:mm');
          expect(planner.$timelineSmall.children()[i].textContent).toBe(labelValue);
        }
      }

      it('draws scale for WEEK for whole day with 6h interval', () => {
        planner.displayModeOptions[planner.displayMode] = {
          interval: 360,
          labelPeriod: 1,
          firstHourOfDay: 0,
          lastHourOfDay: 23
        };
      });

      it('draws scale for WEEK with only showing every second label', () => {
        planner.displayModeOptions[planner.displayMode] = {
          interval: 360,
          labelPeriod: 2,
          firstHourOfDay: 0,
          lastHourOfDay: 23
        };
      });

      it('draws scale for WEEK with changing month', () => {
        planner.viewRange = new DateRange(dates.create('2016-06-27'), dates.create('2016-07-04'));
        planner.displayModeOptions[planner.displayMode] = {
          interval: 360,
          labelPeriod: 2,
          firstHourOfDay: 0,
          lastHourOfDay: 23
        };
      });

      it('draws scale for WORK_WEEK for whole day with 6h interval', () => {
        planner.displayMode = Planner.DisplayMode.WORK_WEEK;
        planner.viewRange = new DateRange(dates.create('2016-06-20'), dates.create('2016-06-25'));
        planner.displayModeOptions[planner.displayMode] = {
          interval: 360,
          labelPeriod: 1,
          firstHourOfDay: 0,
          lastHourOfDay: 23
        };
      });

      it('draws scale for WORK_WEEK with only showing every second label', () => {
        planner.displayMode = Planner.DisplayMode.WORK_WEEK;
        planner.viewRange = new DateRange(dates.create('2016-06-20'), dates.create('2016-06-25'));
        planner.displayModeOptions[planner.displayMode] = {
          interval: 360,
          labelPeriod: 1,
          firstHourOfDay: 0,
          lastHourOfDay: 23
        };
      });
    });

    describe('displayMode: MONTH', () => {

      beforeEach(() => {
        planner.displayMode = Planner.DisplayMode.MONTH;
        planner.viewRange = new DateRange(dates.create('2016-06-20'), dates.create('2016-08-20'));
      });

      afterEach(() => {
        planner._renderDisplayModeOptions();
        validateRendering();
      });

      function validateRendering() {
        let options, labelPeriod, months, days;
        options = planner.displayModeOptions[planner.displayMode];
        labelPeriod = options.labelPeriod;

        months = planner.viewRange.to.getMonth() - planner.viewRange.from.getMonth() + 1;
        if (months < 0) {
          months += 12;
        }
        days = (planner.viewRange.to.getTime() - planner.viewRange.from.getTime()) / (24 * 60 * 60 * 1000);

        // element count
        expect(planner.$timelineLarge.children().length).toBe(months);
        expect(planner.$timelineSmall.children().length).toBe(days);

        // labels
        for (let i = 0; i < days; i++) {
          let visible = dates.shift(planner.viewRange.from, 0, 0, i).getDate() % labelPeriod === 0;
          expect(planner.$timelineSmall.children()[i].classList.contains('label-invisible')).toBe(!visible);
          let labelValue = planner._dateFormat(dates.shift(new Date(planner.viewRange.from.valueOf()), 0, 0, i), 'dd');
          expect(planner.$timelineSmall.children()[i].textContent).toBe(labelValue);
        }
      }

      it('draws scale', () => {
        planner.displayModeOptions[planner.displayMode] = {
          labelPeriod: 1
        };
      });

      it('draws scale with only showing every second label', () => {
        planner.displayModeOptions[planner.displayMode] = {
          labelPeriod: 2
        };
      });

    });

    describe('displayMode: CALENDAR_WEEK', () => {

      beforeEach(() => {
        planner.displayMode = Planner.DisplayMode.CALENDAR_WEEK;
        planner.viewRange = new DateRange(dates.create('2016-06-20'), dates.create('2017-03-20'));
      });

      afterEach(() => {
        planner._renderDisplayModeOptions();
        validateRendering();
      });

      function validateRendering() {
        let options, labelPeriod, months, weeks;
        options = planner.displayModeOptions[planner.displayMode];
        labelPeriod = options.labelPeriod;

        months = planner.viewRange.to.getMonth() - planner.viewRange.from.getMonth() + 1;
        if (months < 0) {
          months += 12;
        }
        weeks = dates.weekInYear(dates.trunc(planner.viewRange.to)) - dates.weekInYear(dates.trunc(planner.viewRange.from));
        if (weeks < 0) {
          weeks += dates.weekInYear(dates.create(planner.viewRange.from.getFullYear() + '-12-28'));
        }

        // element count
        expect(planner.$timelineLarge.children().length).toBe(months);
        expect(planner.$timelineSmall.children().length).toBe(weeks);

        // labels
        for (let i = 0; i < weeks; i++) {
          let weekInYear = dates.weekInYear(dates.shift(new Date(planner.viewRange.from.valueOf()), 0, 0, 7 * i));
          let visible = weekInYear % labelPeriod === 0;
          expect(planner.$timelineSmall.children()[i].classList.contains('label-invisible')).toBe(!visible);
          let labelValue = weekInYear + '';
          expect(planner.$timelineSmall.children()[i].textContent).toBe(labelValue);
        }
      }

      it('draws scale for CALENDAR_WEEK displayMode', () => {
        planner.displayModeOptions[planner.displayMode] = {
          labelPeriod: 1
        };
      });

      it('draws scale with only showing every second label', () => {
        planner.displayModeOptions[planner.displayMode] = {
          labelPeriod: 2
        };
      });

      it('draws scale with only showing every third label', () => {
        planner.displayModeOptions[planner.displayMode] = {
          labelPeriod: 3
        };
      });

    });

    describe('displayMode: YEAR', () => {

      beforeEach(() => {
        planner.displayMode = Planner.DisplayMode.YEAR;
        planner.viewRange = new DateRange(dates.create('2016-06-20'), dates.create('2018-06-20'));
      });

      afterEach(() => {
        planner._renderDisplayModeOptions();
        validateRendering();
      });

      function validateRendering() {
        let options, labelPeriod, years, months;
        options = planner.displayModeOptions[planner.displayMode];
        labelPeriod = options.labelPeriod;

        years = planner.viewRange.to.getFullYear() - planner.viewRange.from.getFullYear() + 1;
        months = (planner.viewRange.to.getFullYear() - planner.viewRange.from.getFullYear()) * 12 + (planner.viewRange.to.getMonth() - planner.viewRange.from.getMonth());

        // element count
        expect(planner.$timelineLarge.children().length).toBe(years);
        expect(planner.$timelineSmall.children().length).toBe(months);

        // labels
        for (let i = 0; i < months; i++) {
          let visible = (dates.shift(planner.viewRange.from, 0, i, 0).getMonth()) % labelPeriod === 0;
          expect(planner.$timelineSmall.children()[i].classList.contains('label-invisible')).toBe(!visible);
          let labelValue = planner._dateFormat(dates.shift(new Date(planner.viewRange.from.valueOf()), 0, i, 0), 'MMMM');
          expect(planner.$timelineSmall.children()[i].textContent).toBe(labelValue);
        }
      }

      it('draws scale for YEAR displayMode', () => {
        planner.displayModeOptions[planner.displayMode] = {
          labelPeriod: 1
        };
      });

      it('draws scale with only showing every second label', () => {
        planner.displayModeOptions[planner.displayMode] = {
          labelPeriod: 2
        };
      });

      it('draws scale with only showing every third label', () => {
        planner.displayModeOptions[planner.displayMode] = {
          labelPeriod: 3
        };
      });
    });

  });

  describe('transformLeft/transformWidth', () => {
    let model, planner;

    beforeEach(() => {
      model = createPlannerModel(0);
      planner = createPlanner(model);
      planner.render();
      planner.displayModeOptions = {};
    });

    it('calculates left and width in WEEK mode for whole days', () => {
      planner.viewRange = new DateRange(dates.create('2016-06-20'), dates.create('2016-06-27'));
      planner.displayMode = Planner.DisplayMode.WEEK;
      planner.displayModeOptions[planner.displayMode] = {
        interval: 360,
        firstHourOfDay: 0,
        lastHourOfDay: 23
      };
      planner._renderDisplayModeOptions();

      let options = planner.displayModeOptions[planner.displayMode];
      let cellWidthPercent = 100 / (((options.lastHourOfDay - options.firstHourOfDay + 1) * 7 * 60) / options.interval);

      expect(planner.transformLeft(dates.create('2016-06-20 06:00:00'))).toBeCloseTo(cellWidthPercent, 5);
      expect(planner.transformWidth(dates.create('2016-06-20 06:00:00'), dates.create('2016-06-20 12:00:00'))).toBeCloseTo(cellWidthPercent, 5);

      expect(planner.transformLeft(dates.create('2016-06-21 06:00:00'))).toBeCloseTo(5 * cellWidthPercent, 5);
      expect(planner.transformWidth(dates.create('2016-06-20 06:00:00'), dates.create('2016-06-21 12:00:00'))).toBeCloseTo(5 * cellWidthPercent, 5);
    });

    it('calculates left and width in WEEK mode for limited day range', () => {
      planner.viewRange = new DateRange(dates.create('2016-06-20'), dates.create('2016-06-27'));
      planner.displayMode = Planner.DisplayMode.WEEK;
      planner.displayModeOptions[planner.displayMode] = {
        interval: 60,
        firstHourOfDay: 8,
        lastHourOfDay: 17
      };
      planner._renderDisplayModeOptions();

      let options = planner.displayModeOptions[planner.displayMode];
      let cellWidthPercent = 100 / (((options.lastHourOfDay - options.firstHourOfDay + 1) * 7 * 60) / options.interval);

      // during a day
      expect(planner.transformLeft(dates.create('2016-06-20 09:00:00'))).toBeCloseTo(cellWidthPercent, 5);
      expect(planner.transformWidth(dates.create('2016-06-20 09:00:00'), dates.create('2016-06-20 12:00:00'))).toBeCloseTo(3 * cellWidthPercent, 5);
      expect(planner.transformLeft(dates.create('2016-06-20 12:00:00'))).toBeCloseTo(4 * cellWidthPercent, 5);

      // till the end of the day
      expect(planner.transformLeft(dates.create('2016-06-20 16:00:00'))).toBeCloseTo(8 * cellWidthPercent, 5);
      expect(planner.transformWidth(dates.create('2016-06-20 16:00:00'), dates.create('2016-06-20 18:00:00'))).toBeCloseTo(2 * cellWidthPercent, 5);
      expect(planner.transformLeft(dates.create('2016-06-20 18:00:00'))).toBeCloseTo(10 * cellWidthPercent, 5);

      // to the next day
      expect(planner.transformLeft(dates.create('2016-06-20 16:00:00'))).toBeCloseTo(8 * cellWidthPercent, 5);
      expect(planner.transformWidth(dates.create('2016-06-20 16:00:00'), dates.create('2016-06-21 09:00:00'))).toBeCloseTo(3 * cellWidthPercent, 5);
      expect(planner.transformLeft(dates.create('2016-06-21 09:00:00'))).toBeCloseTo(11 * cellWidthPercent, 5);

      // into the day
      expect(planner.transformLeft(dates.create('2016-06-21 06:00:00'))).toBeCloseTo(10 * cellWidthPercent, 5);
      expect(planner.transformWidth(dates.create('2016-06-21 06:00:00'), dates.create('2016-06-21 09:00:00'))).toBeCloseTo(cellWidthPercent, 5);
      expect(planner.transformLeft(dates.create('2016-06-21 09:00:00'))).toBeCloseTo(11 * cellWidthPercent, 5);

      // into the day from previous day
      expect(planner.transformLeft(dates.create('2016-06-20 20:00:00'))).toBeCloseTo(10 * cellWidthPercent, 5);
      expect(planner.transformWidth(dates.create('2016-06-20 20:00:00'), dates.create('2016-06-21 09:00:00'))).toBeCloseTo(cellWidthPercent, 5);
      expect(planner.transformLeft(dates.create('2016-06-21 09:00:00'))).toBeCloseTo(11 * cellWidthPercent, 5);

      // during night
      expect(planner.transformLeft(dates.create('2016-06-21 06:00:00'))).toBeCloseTo(10 * cellWidthPercent, 5);
      expect(planner.transformWidth(dates.create('2016-06-21 06:00:00'), dates.create('2016-06-21 08:00:00'))).toBeCloseTo(0, 5);
      expect(planner.transformLeft(dates.create('2016-06-21 08:00:00'))).toBeCloseTo(10 * cellWidthPercent, 5);

      // over two days
      expect(planner.transformLeft(dates.create('2016-06-20 16:00:00'))).toBeCloseTo(8 * cellWidthPercent, 5);
      expect(planner.transformWidth(dates.create('2016-06-20 16:00:00'), dates.create('2016-06-22 09:00:00'))).toBeCloseTo(13 * cellWidthPercent, 5);
      expect(planner.transformLeft(dates.create('2016-06-22 09:00:00'))).toBeCloseTo(21 * cellWidthPercent, 5);
    });

    it('calculates left and width in WEEK mode for limited day range (only firstHourOfDay set)', () => {
      planner.viewRange = new DateRange(dates.create('2016-06-20'), dates.create('2016-06-27'));
      planner.displayMode = Planner.DisplayMode.WEEK;
      planner.displayModeOptions[planner.displayMode] = {
        interval: 60,
        firstHourOfDay: 8,
        lastHourOfDay: 23
      };
      planner._renderDisplayModeOptions();

      let options = planner.displayModeOptions[planner.displayMode];
      let cellWidthPercent = 100 / (((options.lastHourOfDay - options.firstHourOfDay + 1) * 7 * 60) / options.interval);

      // during a day
      expect(planner.transformLeft(dates.create('2016-06-20 09:00:00'))).toBeCloseTo(cellWidthPercent, 5);
      expect(planner.transformWidth(dates.create('2016-06-20 09:00:00'), dates.create('2016-06-20 12:00:00'))).toBeCloseTo(3 * cellWidthPercent, 5);

      // till the end of the day
      expect(planner.transformLeft(dates.create('2016-06-20 23:00:00'))).toBeCloseTo(15 * cellWidthPercent, 5);
      expect(planner.transformWidth(dates.create('2016-06-20 16:00:00'), dates.create('2016-06-20 24:00:00'))).toBeCloseTo(8 * cellWidthPercent, 5);

      // to the next day
      expect(planner.transformLeft(dates.create('2016-06-21 08:00:00'))).toBeCloseTo(16 * cellWidthPercent, 5);
      expect(planner.transformWidth(dates.create('2016-06-20 16:00:00'), dates.create('2016-06-21 09:00:00'))).toBeCloseTo(9 * cellWidthPercent, 5);

      // over two days
      expect(planner.transformLeft(dates.create('2016-06-22 08:00:00'))).toBeCloseTo(32 * cellWidthPercent, 5);
      expect(planner.transformWidth(dates.create('2016-06-20 16:00:00'), dates.create('2016-06-22 09:00:00'))).toBeCloseTo(25 * cellWidthPercent, 5);
    });
  });

  describe('select', () => {
    let model, planner;

    beforeEach(() => {
      model = createPlannerModel(1);
      planner = createPlanner(model);
      planner.render();
    });

    it('selects at least the number of intervals configured by display mode options', () => {
      planner.viewRange = new DateRange(dates.create('2016-06-20'), dates.create('2016-06-27'));
      planner.displayMode = Planner.DisplayMode.WEEK;
      planner.displayModeOptions[planner.displayMode] = {
        interval: 60,
        firstHourOfDay: 0,
        lastHourOfDay: 23,
        minSelectionIntervalCount: 3
      };
      planner._renderDisplayModeOptions();

      // start of view range
      planner.startRow = planner.resources[0];
      planner.lastRow = planner.resources[0];
      planner.startRange = new DateRange(dates.create('2016-06-20 00:00:00'), dates.create('2016-06-20 01:00:00'));
      planner.lastRange = new DateRange(dates.create('2016-06-20 00:00:00'), dates.create('2016-06-20 01:00:00'));
      planner._select();
      expect(planner.selectionRange.from.toISOString()).toBe(dates.create('2016-06-20 00:00:00').toISOString());
      expect(planner.selectionRange.to.toISOString()).toBe(dates.create('2016-06-20 03:00:00').toISOString());

      // end of view range
      planner.startRange = new DateRange(dates.create('2016-06-26 23:00:00'), dates.create('2016-06-20 24:00:00'));
      planner.lastRange = new DateRange(dates.create('2016-06-26 23:00:00'), dates.create('2016-06-20 24:00:00'));
      planner._select();
      expect(planner.selectionRange.from.toISOString()).toBe(dates.create('2016-06-26 21:00:00').toISOString());
      expect(planner.selectionRange.to.toISOString()).toBe(dates.create('2016-06-26 24:00:00').toISOString());

      // selection to right
      planner.startRange = new DateRange(dates.create('2016-06-20 16:00:00'), dates.create('2016-06-20 17:00:00'));
      planner.lastRange = new DateRange(dates.create('2016-06-20 17:00:00'), dates.create('2016-06-20 18:00:00'));
      planner._select();
      expect(planner.selectionRange.from.toISOString()).toBe(dates.create('2016-06-20 16:00:00').toISOString());
      expect(planner.selectionRange.to.toISOString()).toBe(dates.create('2016-06-20 19:00:00').toISOString());

      // selection to left
      planner.startRange = new DateRange(dates.create('2016-06-20 16:00:00'), dates.create('2016-06-20 17:00:00'));
      planner.lastRange = new DateRange(dates.create('2016-06-20 15:00:00'), dates.create('2016-06-20 16:00:00'));
      planner._select();
      expect(planner.selectionRange.from.toISOString()).toBe(dates.create('2016-06-20 14:00:00').toISOString());
      expect(planner.selectionRange.to.toISOString()).toBe(dates.create('2016-06-20 17:00:00').toISOString());
    });

    it('respects end of day if minSelectionIntervalCount is set', () => {
      planner.viewRange = new DateRange(dates.create('2016-06-20'), dates.create('2016-06-27'));
      planner.displayMode = Planner.DisplayMode.WEEK;
      planner.displayModeOptions[planner.displayMode] = {
        interval: 30,
        firstHourOfDay: 8,
        lastHourOfDay: 17,
        minSelectionIntervalCount: 2
      };
      planner._renderDisplayModeOptions();
      planner.startRow = planner.resources[0];
      planner.lastRow = planner.resources[0];

      // end of day
      planner.startRange = new DateRange(dates.create('2016-06-26 17:00:00'), dates.create('2016-06-20 17:30:00'));
      planner.lastRange = new DateRange(dates.create('2016-06-26 17:00:00'), dates.create('2016-06-20 17:30:00'));
      planner._select();
      expect(planner.selectionRange.from.toISOString()).toBe(dates.create('2016-06-26 17:00:00').toISOString());
      expect(planner.selectionRange.to.toISOString()).toBe(dates.create('2016-06-26 18:00:00').toISOString());

      // end of day, click in last interval -> actual end would be 18:30 but since this is not visible to the user use 18:00 as end
      planner.startRange = new DateRange(dates.create('2016-06-26 17:30:00'), dates.create('2016-06-20 18:00:00'));
      planner.lastRange = new DateRange(dates.create('2016-06-26 17:30:00'), dates.create('2016-06-20 18:00:00'));
      planner._select();
      expect(planner.selectionRange.from.toISOString()).toBe(dates.create('2016-06-26 17:00:00').toISOString());
      expect(planner.selectionRange.to.toISOString()).toBe(dates.create('2016-06-26 18:00:00').toISOString());

      // manual selection over end of day should still be possible
      planner.startRange = new DateRange(dates.create('2016-06-20 17:30:00'), dates.create('2016-06-20 18:00:00'));
      planner.lastRange = new DateRange(dates.create('2016-06-21 09:00:00'), dates.create('2016-06-21 09:30:00'));
      planner._select();
      expect(planner.selectionRange.from.toISOString()).toBe(dates.create('2016-06-20 17:30:00').toISOString());
      expect(planner.selectionRange.to.toISOString()).toBe(dates.create('2016-06-21 09:30:00').toISOString());

      // selection to left
      planner.startRange = new DateRange(dates.create('2016-06-20 17:30:00'), dates.create('2016-06-20 18:00:00'));
      planner.lastRange = new DateRange(dates.create('2016-06-20 15:00:00'), dates.create('2016-06-20 15:30:00'));
      planner._select();
      expect(planner.selectionRange.from.toISOString()).toBe(dates.create('2016-06-20 15:00:00').toISOString());
      expect(planner.selectionRange.to.toISOString()).toBe(dates.create('2016-06-20 18:00:00').toISOString());
    });
  });

  describe('clickToday', () => {
    let model, planner;

    beforeEach(() => {
      model = createPlannerModel(0);
      planner = createPlanner(model);
      planner.displayModeOptions = {};
      planner.displayMode = Planner.DisplayMode.WEEK;
    });

    describe('Regular view range', () => {

      beforeEach(() => {
        planner.viewRange = new DateRange(dates.create('2020-10-01'), dates.create('2020-10-10')); // 9 days (starts Thursday)
        planner._today = () => dates.create('2021-01-19'); // Tuesday
        planner._onTodayClick();
      });

      it('correct viewRange', () => {
        expect(planner.viewRange.from.toISOString()).toBe(dates.create('2021-01-18').toISOString()); // Monday (today will use first day of week)
        expect(planner.viewRange.to.toISOString()).toBe(dates.create('2021-01-27').toISOString());
      });
    });

    describe('Current view range with summer and winter time', () => {

      beforeEach(() => {
        planner.viewRange = new DateRange(dates.create('2020-10-22'), dates.create('2020-11-03')); // 12 days
        planner._today = () => dates.create('2021-01-19'); // Tuesday
        planner._onTodayClick();
      });

      it('correct viewRange', () => {
        expect(planner.viewRange.from.toISOString()).toBe(dates.create('2021-01-18').toISOString()); // Monday (today will use first day of week)
        expect(planner.viewRange.to.toISOString()).toBe(dates.create('2021-01-30').toISOString());
      });
    });

    describe('New view range with summer and winter time', () => {

      beforeEach(() => {
        planner.viewRange = new DateRange(dates.create('2020-10-01'), dates.create('2020-10-10')); // 9 days (starts Thursday)
        planner._today = () => dates.create('2021-03-26'); // Friday
        planner._onTodayClick();
      });

      it('correct viewRange', () => {
        expect(planner.viewRange.from.toISOString()).toBe(dates.create('2021-03-22').toISOString()); // Monday (today will use first day of week)
        expect(planner.viewRange.to.toISOString()).toBe(dates.create('2021-03-31').toISOString());
      });
    });

    describe('Current/new view range with summer and winter time', () => {

      beforeEach(() => {
        planner.viewRange = new DateRange(dates.create('2020-10-22'), dates.create('2020-11-03')); // 12 days
        planner._today = () => dates.create('2021-03-26'); // Friday
        planner._onTodayClick();
      });

      it('correct viewRange', () => {
        expect(planner.viewRange.from.toISOString()).toBe(dates.create('2021-03-22').toISOString()); // Monday (today will use first day of week)
        expect(planner.viewRange.to.toISOString()).toBe(dates.create('2021-04-03').toISOString());
      });
    });
  });
});
