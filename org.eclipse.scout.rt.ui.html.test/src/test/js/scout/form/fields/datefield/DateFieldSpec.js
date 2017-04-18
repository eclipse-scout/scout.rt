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
describe("DateField", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
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
    var model = helper.createFieldModel('DateField');
    model.hasDate = true;
    model.timeFormatPattern = 'HH:mm';
    model.dateFormatPattern = 'dd.MM.yyyy';
    return model;
  }

  function findPicker() {
    return $('.date-picker');
  }

  // Used to expect a date.
  // Deals with the akward 0=january behavior of the Date#getMonth() method,
  // which means month=1 is january
  function expectDate(date, year, month, day, hour, minute) {
    if (month === 0) {
      throw new Error('invalid month 0. Months start at 1=january');
    }
    expect(date.getFullYear()).toBe(year);
    expect(date.getMonth()).toBe(month - 1);
    expect(date.getDate()).toBe(day);
    if (hour !== undefined && minute !== undefined) {
      expect(date.getHours()).toBe(hour);
      expect(date.getMinutes()).toBe(minute);
    }
  }

  function selectFirstDayInPicker($picker) {
    var $day = $picker.find('.date-picker-day').first();
    var date = $day.data('date');
    $day.triggerClick();
    return date;
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

  describe("displayText", function() {

    it("is shown correctly after rendering", function() {
      var model = createModel();
      model.displayText = '14.04.2016\n12:28';
      model.hasDate = true;
      model.hasTime = true;
      var dateField = createField(model);
      dateField.render(session.$entryPoint);
      dateField.dateDisplayText = '14.04.2016';
      dateField.timeDisplayText = '12:28';
      expect(dateField.$dateField.val()).toBe('14.04.2016');
      expect(dateField.$timeField.val()).toBe('12:28');
      expect(dateField.displayText).toBe('14.04.2016\n12:28');
    });

    it("is removed properly when setting to ''", function() {
      var model = createModel();
      model.displayText = '14.04.2016\n12:28';
      model.hasDate = true;
      model.hasTime = true;
      var dateField = createField(model);
      dateField.render(session.$entryPoint);

      dateField._syncDisplayText('');
      dateField._renderDisplayText();
      expect(dateField.dateDisplayText).toBe('');
      expect(dateField.timeDisplayText).toBe('');
      expect(dateField.$dateField.val()).toBe('');
      expect(dateField.$timeField.val()).toBe('');
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
      expectDate(dateBefore, 2014, 10, 1);

      dateField.$dateField.val('11.02.2015');
      dateBefore = dateField.timestampAsDate;
      expectDate(dateBefore, 2014, 10, 1);

      dateField._onDateFieldBlur();
      var date = dateField.timestampAsDate;
      expectDate(date, 2015, 2, 11);
    });

    it("sends timestamp and displayText", function() {
      var model = createModel();
      model.timestamp = '2014-10-01';
      var dateField = createFieldAndFocusAndOpenPicker(model);

      dateField.$dateField.val('11.02.2015');
      dateField._onDateFieldBlur();
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      // Order is important, displayText needs to be before timestamp
      // Otherwise server would generate a display text as soon as timestamp changes and send it back even if it is the same as the ui is sending
      var events = [
        new scout.Event(dateField.id, 'displayTextChanged', {
          displayText: '11.02.2015',
          showBusyIndicator: true
        }),
        new scout.Event(dateField.id, 'timestampChanged', {
          timestamp: '2015-02-11'
        })
      ];
      expect(mostRecentJsonRequest()).toContainEventsExactly(events);
    });

    it("does not send timestamp and displayText again if not changed", function() {
      var model = createModel();
      model.timestamp = '2014-10-01';
      var dateField = createFieldAndFocusAndOpenPicker(model);

      dateField.$dateField.val('11.02.2015');
      dateField._onDateFieldBlur();
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      dateField._onDateFieldBlur();
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1); // still 1
    });

    it("does not send timestamp and displayText if no date was entered", function() {
      var model = createModel();
      var dateField = createFieldAndFocusAndOpenPicker(model);

      dateField._onDateFieldBlur();
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(0);
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

  describe("Picker", function() {

    it("sends displayText and timestamp if date was selected", function() {
      var model = createModel();
      var dateField = createFieldAndFocusAndOpenPicker(model);

      dateField._onDatePickerDateSelect({
        type: 'dateSelect',
        date: new Date(2016, 1, 1)
      });
      expect(dateField.$dateField.val()).toBe('01.02.2016');
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      var events = [
        new scout.Event(dateField.id, 'displayTextChanged', {
          displayText: '01.02.2016',
          showBusyIndicator: true
        }),
        new scout.Event(dateField.id, 'timestampChanged', {
          timestamp: '2016-02-01'
        })
      ];
      expect(mostRecentJsonRequest()).toContainEventsExactly(events);
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
        expectDate(dateBefore, 2014, 10, 1);

        dateField.$dateField.val('11.02.2015');
        dateBefore = dateField.timestampAsDate;
        expectDate(dateBefore, 2014, 10, 1);

        dateField.$dateField.triggerKeyDown(scout.keys.ENTER);
        var date = dateField.timestampAsDate;
        expectDate(date, 2015, 2, 11);
        expect(findPicker().length).toBe(0);
      });

    });

    describe("DOWN", function() {

      var model;

      beforeEach(function() {
        model = createModel();
        model.timestamp = '2014-10-01';
        model.displayText = '01.10.2014\n';
      });

      it("increases day by one", function() {
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = dateField.timestampAsDate;
        expectDate(dateBefore, 2014, 10, 1);

        dateField.$dateField.triggerKeyDown(scout.keys.DOWN);

        dateBefore = dateField.timestampAsDate;
        expectDate(dateBefore, 2014, 10, 1);
        expect(dateField.$dateField.val()).toBe('02.10.2014');
      });

      it("increases month by one if shift is used as modifier", function() {
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = dateField.timestampAsDate;
        expectDate(dateBefore, 2014, 10, 1);

        dateField.$dateField.triggerKeyDown(scout.keys.DOWN, 'shift');
        dateBefore = dateField.timestampAsDate;
        expectDate(dateBefore, 2014, 10, 1);
        expect(dateField.$dateField.val()).toBe('01.11.2014');
      });

      it("increases year by one if ctrl is used as modifier", function() {
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = dateField.timestampAsDate;
        expectDate(dateBefore, 2014, 10, 1);

        dateField.$dateField.triggerKeyDown(scout.keys.DOWN, 'ctrl');

        dateBefore = dateField.timestampAsDate;
        expectDate(dateBefore, 2014, 10, 1);
        expect(dateField.$dateField.val()).toBe('01.10.2015');
      });

    });

    describe("UP", function() {

      var model;

      beforeEach(function() {
        model = createModel();
        model.timestamp = '2014-10-01';
        model.displayText = '01.10.2014\n';
      });

      it("decreases day by one", function() {
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = dateField.timestampAsDate;
        expectDate(dateBefore, 2014, 10, 1);

        dateField.$dateField.triggerKeyDown(scout.keys.UP);

        dateBefore = dateField.timestampAsDate;
        expectDate(dateBefore, 2014, 10, 1);
        expect(dateField.$dateField.val()).toBe('30.09.2014');
      });

      it("decreases month by one if shift is used as modifier", function() {
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = dateField.timestampAsDate;
        expectDate(dateBefore, 2014, 10, 1);

        dateField.$dateField.triggerKeyDown(scout.keys.UP, 'shift');

        dateBefore = dateField.timestampAsDate;
        expectDate(dateBefore, 2014, 10, 1);
        expect(dateField.$dateField.val()).toBe('01.09.2014');
      });

      it("decreases year by one if ctrl is used as modifier", function() {
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = dateField.timestampAsDate;
        expectDate(dateBefore, 2014, 10, 1);

        dateField.$dateField.triggerKeyDown(scout.keys.UP, 'ctrl');

        dateBefore = dateField.timestampAsDate;
        expectDate(dateBefore, 2014, 10, 1);
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

    it("can predict yyyy.MM", function() {
      var model = createModel();
      model.dateFormatPattern = 'yyyy.MM';
      var dateField = createFieldAndFocusAndOpenPicker(model);
      var now = new Date();

      dateField.$dateField.val('2');
      dateField._onDateFieldBlur();
      expect(dateField.$dateField.val()).toBe('2002.' + ('0' + (now.getMonth() + 1)).slice(-2));
    });

  });

  it("can predict partial years", function() {
    var model = createModel();
    model.timestamp = '2017-11-11';
    var dateField = createFieldAndFocusAndOpenPicker(model);

    dateField.$dateField.val('11.11.68');
    dateField._onDateFieldBlur();
    expect(dateField.$dateField.val()).toBe('11.11.1968');

    dateField.$dateField.val('11.11.68');
    dateField._onDateFieldBlur();
    expect(dateField.$dateField.val()).toBe('11.11.1968');
  });

  describe("Allowed dates", function() {

    it("_referenceDate returns only allowed date - only one date", function() {
      var model = createModel();
      model.allowedDates = ["2016-04-15"];
      var dateField = createField(model);
      var date = dateField._referenceDate();
      expectDate(date, 2016, 4, 15);
    });

    it("_referenceDate returns only allowed date - choose nearest date in the future", function() {
      var model = createModel();
      model.allowedDates = ["2016-03-14", "2016-04-16", "2016-04-17"];
      model.autoTimestamp = '2016-04-15';
      var dateField = createField(model);
      var date = dateField._referenceDate();
      expectDate(date, 2016, 4, 16);
    });

    it("_referenceDate returns only allowed date - when no date in future is available, choose nearest date in past", function() {
      var model = createModel();
      model.allowedDates = ["2016-02-14", "2016-03-16", "2016-04-03"];
      model.autoTimestamp = '2016-04-15';
      var dateField = createField(model);
      var date = dateField._referenceDate();
      expectDate(date, 2016, 4, 3);
    });

    it("_syncAllowedDates must convert date strings into Dates", function() {
      var dateField = createField(createModel());
      dateField._syncAllowedDates(["2016-02-14"]);
      expectDate(dateField.allowedDates[0], 2016, 2, 14);
    });

  });

  describe('Touch = true', function() {

    describe('touch popup', function() {

      it("updates display text and is not used for time fields", function() {
        var model = createModel();
        model.autoTimestamp = '1999-10-03';
        model.touch = true;
        model.hasDate = true;
        model.hasTime = true;

        var dateField = createField(model);
        dateField.render(session.$entryPoint);

        dateField.$dateField.triggerMouseDown();
        expect(findPicker().length).toBe(1);

        selectFirstDayInPicker(dateField.popup.$container.find('.date-picker'));

        dateField.$timeField.val('0442');
        dateField._onTimeFieldBlur();

        // selected date in picker (first day) must be 09/27/1999
        expect(dateField.$dateField.text()).toBe('27.09.1999');
        expect(dateField.$timeField.val()).toBe('04:42');
        expect(dateField.displayText).toBe('27.09.1999\n04:42');

        dateField.$dateField.triggerMouseDown();
        expect(findPicker().length).toBe(1);
      });

      it('is opened if datefield is touched', function() {
        var model = createModel();
        model.touch = true;
        var dateField = createField(model);
        dateField.render(session.$entryPoint);

        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);
        expect($('.touch-popup').length).toBe(1);
        expect($('.date-picker-popup').length).toBe(0);
        dateField.popup.close();
      });

      it('is not opened if timefield is touched', function() {
        var model = createModel();
        model.touch = true;
        model.hasTime = true;
        var dateField = createField(model);
        dateField.render(session.$entryPoint);

        dateField.$timeField.triggerClick();
        expect(dateField.popup).toBe(undefined);
        expect($('.touch-popup').length).toBe(0);
        expect($('.date-picker-popup').length).toBe(0);
      });

      it('is closed when date in picker is selected', function() {
        var model = createModel();
        model.touch = true;
        var dateField = createField(model);
        dateField.render(session.$entryPoint);

        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);

        var selectedDate = selectFirstDayInPicker(dateField.popup.$container.find('.date-picker'));
        expect(dateField.popup).toBe(null);
      });

      it('unregisters clone after close', function() {
        var model = createModel();
        model.touch = true;
        var dateField = createField(model);
        dateField.render(session.$entryPoint);

        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);

        // Popup creates a clone -> validate that it will be unregistered when popup closes
        expect(session._clonedModelAdapterRegistry[dateField.id][0]).toBe(dateField.popup._field);
        dateField.popup.close();
        expect(dateField.popup).toBe(null);
        expect(session._clonedModelAdapterRegistry[dateField.id].length).toBe(0);
      });

      it('updates displayText and timestamp of datefield if date in picker is selected', function() {
        var model = createModel();
        model.touch = true;
        var dateField = createField(model);
        dateField.render(session.$entryPoint);

        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);

        var selectedDate = selectFirstDayInPicker(dateField.popup.$container.find('.date-picker'));
        expect(dateField.popup).toBe(null);
        expect(dateField.timestampAsDate).toEqual(selectedDate);
        expect(dateField.displayText).toBe(dateField.isolatedDateFormat.format(selectedDate));
        expect(dateField.$dateField.text()).toBe(dateField.displayText);
      });

      it('updates displayText and timestamp of datefield if date in picker is entered', function() {
        var model = createModel();
        model.touch = true;
        var dateField = createField(model);
        dateField.render(session.$entryPoint);

        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);

        dateField.popup._field.$dateField.val('11.02.2015');
        dateField.popup._field.$dateField.triggerKeyDown(scout.keys.ENTER);
        expect(dateField.popup).toBe(null);
        expectDate(dateField.timestampAsDate, 2015, 02, 11);
        expect(dateField.displayText).toBe('11.02.2015');
        expect(dateField.$dateField.text()).toBe(dateField.displayText);
      });

      it('updates displayText and timestamp of datefield if date and time in picker are entered', function() {
        var model = createModel();
        model.touch = true;
        model.hasTime = true;
        var dateField = createField(model);
        dateField.render(session.$entryPoint);

        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);

        dateField.popup._field.$dateField.val('29.02.2016');
        dateField.popup._field.$dateField.triggerKeyDown(scout.keys.ENTER);

        expect(dateField.popup).toBe(null);
        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);
        
        dateField.popup._field.$timeField.val('10:42');
        dateField.popup._field.$timeField.triggerKeyDown(scout.keys.ENTER);
                
        expect(dateField.popup).toBe(null);
        
        expectDate(dateField.timestampAsDate, 2016, 02, 29, 10, 42);
        expect(dateField.displayText).toBe('29.02.2016\n10:42');
        expect(dateField.$dateField.text()).toBe('29.02.2016');
        expect(dateField.$timeField.val()).toBe('10:42');
      });

      it('shows datefield with same date as clicked datefield', function() {
        var model = createModel();
        model.touch = true;
        model.timestamp = '2012-07-01';
        model.displayText = '01.07.2012';
        var dateField = createField(model);
        dateField.render(session.$entryPoint);

        expect(dateField.$dateField.text()).toBe(dateField.displayText);
        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);
        expect(dateField.popup._field.timestampAsDate).toEqual(dateField.timestampAsDate);
        expect(dateField.popup._field.displayText).toBe(dateField.displayText);
        expect(dateField.popup._field.$dateField.val()).toBe(dateField.displayText);
      });

      it('shows datefield with same date as clicked datefield, if field empty initially', function() {
        var model = createModel();
        model.touch = true;
        var dateField = createField(model);
        dateField.render(session.$entryPoint);

        // Open
        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);

        // Enter date and close
        dateField.popup._field.$dateField.val('11.02.2015');
        dateField.popup._field.$dateField.triggerKeyDown(scout.keys.ENTER);
        expect(dateField.popup).toBe(null);
        expect(dateField.displayText).toBe('11.02.2015');

        // Reopen and verify
        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);
        expect(dateField.popup._field.timestampAsDate).toEqual(dateField.timestampAsDate);
        expect(dateField.popup._field.displayText).toBe(dateField.displayText);
        expect(dateField.popup._field.$dateField.val()).toBe(dateField.displayText);
      });

      it('clears displayText and timestamp of datefield if date in picker was removed', function() {
        var model = createModel();
        model.touch = true;
        model.timestamp = '2012-07-01';
        model.displayText = '01.07.2012';
        var dateField = createField(model);
        dateField.render(session.$entryPoint);

        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);
        expect(dateField.popup._field.$dateField.val()).toBe(dateField.displayText);

        dateField.popup._field.$dateField.val('');
        dateField.popup._field.$dateField.triggerKeyDown(scout.keys.ENTER);
        expect(dateField.popup).toBe(null);
        expect(dateField.timestampAsDate).toBe(null);
        expect(dateField.displayText).toBe('');
        expect(dateField.$dateField.text()).toBe('');
      });

      it('shows datefield with same date as clicked datefield, even if value was deleted before', function() {
        var model = createModel();
        model.touch = true;
        model.timestamp = '2012-07-01';
        model.displayText = '01.07.2012';
        var dateField = createField(model);
        dateField.render(session.$entryPoint);

        // Open and check display text
        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);
        expect(dateField.popup._field.$dateField.val()).toBe(dateField.displayText);

        // Clear text
        dateField.popup._field.$dateField.val('');
        dateField.popup._field.$dateField.triggerKeyDown(scout.keys.ENTER);
        expect(dateField.popup).toBe(null);
        expect(dateField.$dateField.text()).toBe('');

        // Open again
        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);
        expect(dateField.popup._field.$dateField.val()).toBe(dateField.displayText);

        // Select a date
        var selectedDate = selectFirstDayInPicker(dateField.popup.$container.find('.date-picker'));
        expect(dateField.popup).toBe(null);
        expect(dateField.timestampAsDate).toEqual(selectedDate);
        expect(dateField.displayText).toBe(dateField.isolatedDateFormat.format(selectedDate));
        expect(dateField.$dateField.text()).toBe(dateField.displayText);

        // Open again and verify
        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);
        expect(dateField.popup._field.$dateField.val()).toBe(dateField.displayText);
      });

    });
  });

});
