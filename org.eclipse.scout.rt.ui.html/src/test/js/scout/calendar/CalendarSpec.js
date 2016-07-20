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
/* global CalendarSpecHelper */
describe("Calendar", function() {
  var session, helper;

  beforeEach(function() {
    setFixtures(sandbox());
    jasmine.Ajax.install();
    jasmine.clock().install();
    session = sandboxSession();
    session.init();
    helper = new CalendarSpecHelper(session);
    uninstallUnloadHandlers(session);
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe("init", function() {

    it("creates an empty calendar", function() {
      var cal = helper.createCalendar(helper.createSimpleModel());
      expect(cal.viewRange).toBeDefined;
    });

  });

  describe("dayPosition", function() {

    it("calculates the day position", function() {
    var cal = helper.createCalendar(helper.createSimpleModel());
    //total size: 80
    expect(cal._dayPosition(0)).toBe(5);
    expect(cal._dayPosition(4)).toBe(10);
    expect(cal._dayPosition(8)).toBe(15);
    expect(cal._dayPosition(10)).toBe(27.5);
    expect(cal._dayPosition(12)).toBe(40);
    expect(cal._dayPosition(12.5)).toBe(42.5);
    expect(cal._dayPosition(13)).toBe(45);
    expect(cal._dayPosition(17)).toBe(70);
    expect(cal._dayPosition(24)).toBe(80);
    expect(cal._dayPosition(-1)).toBe(85);
    });
  });



  describe("component", function() {
    var cal;
    var option1 = {
      fromDate: "2016-07-20 12:00:00.000",
      toDate: "2016-07-20 12:30:00.000"
    };
    var option2 = {
      fromDate: "2016-07-20 12:30:00.000",
      toDate: "2016-07-20 13:00:00.000"
    };
    var option3 = {
      fromDate: "2016-07-20 13:00:00.000",
      toDate: "2016-07-20 20:00:00.000"
    };
    var option4 = {
      fromDate: "2016-07-20 13:30:00.000",
      toDate: "2016-07-20 15:00:00.000"
    };
    var option5 = {
      fromDate: "2016-07-20 12:15:00.000",
      toDate: "2016-07-20 16:00:00.000"
    };

    var optionSmall1 = {
      fromDate: "2016-07-20 11:59:00.000",
      toDate: "2016-07-20 12:00:00.000"
    };


    beforeEach(function() {
      cal = helper.createCalendar(helper.createSimpleModel());
    });

    it("intersects with itself", function() {
      var c1 = helper.createCompoment(option1, cal);
      expect(cal._intersect(c1, c1)).toBe(true);
    });

    it("does not intersect with an adjacent component ", function() {
      var c1 = helper.createCompoment(option1, cal);
      var c2 = helper.createCompoment(option2, cal);
      expect(cal._intersect(c1, c2)).toBe(false);
      expect(cal._intersect(c2, c1)).toBe(false);
    });

    it("does not intersect with a later component", function() {
      var c1 = helper.createCompoment(option1, cal);
      var c3 = helper.createCompoment(option3, cal);
      expect(cal._intersect(c1, c3)).toBe(false);
      expect(cal._intersect(c3, c1)).toBe(false);
    });

    it("intersects with an included component", function() {
      var c3 = helper.createCompoment(option3, cal);
      var c4 = helper.createCompoment(option4, cal);
      expect(cal._intersect(c3, c4)).toBe(true);
      expect(cal._intersect(c4, c3)).toBe(true);
    });

    it("intersects with an intersecting component", function() {
      var c1 = helper.createCompoment(option1, cal);
      var c5 = helper.createCompoment(option5, cal);
      expect(cal._intersect(c1, c5)).toBe(true);
      expect(cal._intersect(c5, c1)).toBe(true);
    });

    it("intersects with a small component that is larger when displayed", function() {
      var c1 = helper.createCompoment(option1, cal);
      var c2 = helper.createCompoment(optionSmall1, cal);
      expect(cal._intersect(c1, c2)).toBe(true);
      expect(cal._intersect(c2, c1)).toBe(true);
    });

  });

});
