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
/* global FormSpecHelper */
describe("DateField", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    $('.tooltip').remove();
    $('.date-picker').remove();
  });

  function createField(model) {
    var field = new scout.DateField();
    field.init(model);
    return field;
  }

  function createFieldAndFocusAndOpenPicker(model) {
    var dateField = createField(model);
    dateField.render(session.$entryPoint);

    dateField.$dateField.focus();
    jasmine.clock().tick(101);
    expect(dateField.$dateField).toBeFocused();
    dateField.$dateField.triggerMouseDown();
    expect(findPicker().length).toBe(1);

    return dateField;
  }

  function createModel() {
    var model = helper.createFieldModel('scout.DateField');
    model.hasDate = true;
    model.timeFormatPattern = 'HH:mm';
    model.dateFormatPattern = 'dd.MM.yyyy';
    return model;
  }

  function findPicker() {
    return $('.date-picker');
  }

  describe("Clicking the field", function() {

    it("opens the datepicker", function() {
      var model = createModel();
      var dateField = createField(model);
      dateField.render(session.$entryPoint);
      expect(findPicker().length).toBe(0);

      dateField.$dateField.triggerMouseDown();

      expect(findPicker().length).toBe(1);
    });

  });

  describe("Leaving the field", function() {

    it("closes the datepicker", function() {
      var model = createModel();
      var dateField = createFieldAndFocusAndOpenPicker(model);
      expect(findPicker().length).toBe(1);

      dateField._onDateFieldBlur();

      expect(findPicker().length).toBe(0);
    });

    it("accepts the prediction", function() {
      var model = createModel();
      var dateField = createFieldAndFocusAndOpenPicker(model);

      // Set reference date, so result is reliable for testing
      dateField.autoTimestampAsDate = new Date(2015, 10, 1);
      dateField.$dateField.val('02');

      dateField._onDateFieldBlur();

      expect(dateField.$dateField.val()).toBe('02.11.2015');
    });

    it("accepts the prediction with autoTimestamp", function() {
      var model = createModel();
      model.autoTimestamp = '1999-10-14';
      var dateField = createFieldAndFocusAndOpenPicker(model);

      dateField.$dateField.val('02');

      dateField._onDateFieldBlur();

      expect(dateField.$dateField.val()).toBe('02.10.1999');
    });

    it("updates the model with the selected value", function() {
      var model = createModel();
      model.timestamp = '2014-10-01';
      var dateField = createFieldAndFocusAndOpenPicker(model);
      var dateBefore = dateField.timestampAsDate;
      expect(dateBefore.getFullYear()).toBe(2014);
      expect(dateBefore.getMonth()).toBe(9);
      expect(dateBefore.getDate()).toBe(1);

      dateField.$dateField.val('11.02.2015');
      dateBefore = dateField.timestampAsDate;
      expect(dateBefore.getFullYear()).toBe(2014);
      expect(dateBefore.getMonth()).toBe(9);
      expect(dateBefore.getDate()).toBe(1);

      dateField._onDateFieldBlur();
      var date= dateField.timestampAsDate;
      expect(date.getFullYear()).toBe(2015);
      expect(date.getMonth()).toBe(1);
      expect(date.getDate()).toBe(11);
    });

  });

  describe("Validation", function() {

    it("invalidates field if value is invalid (not a date)", function() {
      var model = createModel();
      var dateField = createFieldAndFocusAndOpenPicker(model);

      dateField.$dateField.val('33');
      dateField._onDateFieldBlur();

      expect(dateField.$dateField).toHaveClass('has-error');
    });

    it("prevents model update if value is invalid", function() {
      var model = createModel();
      var dateField = createFieldAndFocusAndOpenPicker(model);

      dateField.$dateField.val('33');
      expect(dateField.displayText).toBeFalsy();

      expect(mostRecentJsonRequest()).toBeUndefined();
    });

  });

  describe("Key handling", function() {

    describe("ESC", function() {

      it("closes the datepicker", function() {
        var model = createModel();
        var dateField = createFieldAndFocusAndOpenPicker(model);
        expect(findPicker().length).toBe(1);

        dateField.$dateField.triggerKeyDown(scout.keys.ESC);

        expect(findPicker().length).toBe(0);
      });

    });

    describe("ENTER", function() {

      it("updates the model with the selected value and closes picker", function() {
        var model = createModel();
        model.timestamp = '2014-10-01';
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = dateField.timestampAsDate;
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);


        dateField.$dateField.val('11.02.2015');
        dateBefore=dateField.timestampAsDate;
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);


        dateField.$dateField.triggerKeyDown(scout.keys.ENTER);
        var date= dateField.timestampAsDate;
        expect(date.getFullYear()).toBe(2015);
        expect(date.getMonth()).toBe(1);
        expect(date.getDate()).toBe(11);
        expect(findPicker().length).toBe(0);
      });

    });

    describe("DOWN", function() {

      it("increases day by one", function() {
        var model = createModel();
        model.timestamp = '2014-10-01';
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = dateField.timestampAsDate;
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);

        dateField.$dateField.triggerKeyDown(scout.keys.DOWN);

        dateBefore = dateField.timestampAsDate;
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);
        expect(dateField.$dateField.val()).toBe('02.10.2014');
      });

      it("increases month by one if shift is used as modifier", function() {
        var model = createModel();
        model.timestamp = '2014-10-01';
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = dateField.timestampAsDate;
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);

        dateField.$dateField.triggerKeyDown(scout.keys.DOWN, 'shift');
        dateBefore = dateField.timestampAsDate;
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);

        expect(dateField.$dateField.val()).toBe('01.11.2014');
      });

      it("increases year by one if ctrl is used as modifier", function() {
        var model = createModel();
        model.timestamp = '2014-10-01';
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = dateField.timestampAsDate;
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);

        dateField.$dateField.triggerKeyDown(scout.keys.DOWN, 'ctrl');

        dateBefore = dateField.timestampAsDate;
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);

        expect(dateField.$dateField.val()).toBe('01.10.2015');
      });

    });

    describe("UP", function() {

      it("decreases day by one", function() {
        var model = createModel();
        model.timestamp = '2014-10-01';
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = dateField.timestampAsDate;
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);

        dateField.$dateField.triggerKeyDown(scout.keys.UP);

        dateBefore = dateField.timestampAsDate;
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);
        expect(dateField.$dateField.val()).toBe('30.09.2014');
      });

      it("decreases month by one if shift is used as modifier", function() {
        var model = createModel();
        model.timestamp = '2014-10-01';
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = dateField.timestampAsDate;
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);

        dateField.$dateField.triggerKeyDown(scout.keys.UP, 'shift');

        dateBefore = dateField.timestampAsDate;
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);
        expect(dateField.$dateField.val()).toBe('01.09.2014');
      });

      it("decreases year by one if ctrl is used as modifier", function() {
        var model = createModel();
        model.timestamp = '2014-10-01';
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = dateField.timestampAsDate;
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);

        dateField.$dateField.triggerKeyDown(scout.keys.UP, 'ctrl');

        dateBefore = dateField.timestampAsDate;
        expect(dateBefore.getFullYear()).toBe(2014);
        expect(dateBefore.getMonth()).toBe(9);
        expect(dateBefore.getDate()).toBe(1);
        expect(dateField.$dateField.val()).toBe('01.10.2013');
      });

    });

  });

  describe("Date validation and prediction", function() {

    it("can validate inputs", function() {
      var model = createModel();
      var dateField = createField(model);

      dateField.isolatedDateFormat = new scout.DateFormat(session.locale, 'dd.MM.yyyy');
      expect(!!dateField._predictDate('')).toBe(true);
      expect(!!dateField._predictDate(undefined)).toBe(true);
      expect(!!dateField._predictDate('0')).toBe(true);
      expect(!!dateField._predictDate('1')).toBe(true);
      expect(!!dateField._predictDate('-7')).toBe(true);
      expect(!!dateField._predictDate('01')).toBe(true);
      expect(!!dateField._predictDate('17')).toBe(true);
      expect(!!dateField._predictDate('31')).toBe(true);
      expect(!!dateField._predictDate('32')).toBe(false);
      expect(!!dateField._predictDate('112')).toBe(true); // february 11
      expect(!!dateField._predictDate('1.')).toBe(true);
      expect(!!dateField._predictDate('1.3')).toBe(true);
      expect(!!dateField._predictDate('1.3.2')).toBe(true);
      expect(!!dateField._predictDate('1.3.2015')).toBe(true);
      expect(!!dateField._predictDate('1.3.21015')).toBe(false);
      expect(!!dateField._predictDate('01.13.2015')).toBe(false);
      expect(!!dateField._predictDate('01.03.2015')).toBe(true);
      expect(!!dateField._predictDate('01032015')).toBe(true);
      expect(!!dateField._predictDate('20150301')).toBe(false); // wrong order, does not match locale
      expect(!!dateField._predictDate('010315')).toBe(true);
      expect(!!dateField._predictDate('dummy')).toBe(false);
      expect(!!dateField._predictDate('1...2')).toBe(false);
      expect(!!dateField._predictDate('11x')).toBe(false);
      expect(!!dateField._predictDate('31.02.2015')).toBe(false);
    });

    it("can predict dates", function() {
      var model = createModel();
      var dateField = createField(model);
      var now = new Date();

      function expectPrediction(input, expectedPrediction) {
        var prediction = dateField._predictDate(input);
        expect(prediction).not.toBeNull();
        expect(prediction.text).toBe(expectedPrediction);
      }

      dateField.isolatedDateFormat = new scout.DateFormat(session.locale, 'dd.MM.yyyy');
      expectPrediction('0', '01.' + ('0' + (now.getMonth() + 1)).slice(-2) + '.' + now.getFullYear());
      expectPrediction('1', '1.' + ('0' + (now.getMonth() + 1)).slice(-2) + '.' + now.getFullYear());
      expectPrediction('2', '2.' + ('0' + (now.getMonth() + 1)).slice(-2) + '.' + now.getFullYear());
    });

  });

});
