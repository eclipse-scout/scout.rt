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
describe('DateField', function() {
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
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    $('.tooltip').remove();
    removePopups(session, '.date-picker-popup');
    removePopups(session, '.time-picker-popup');
    removePopups(session, '.touch-popup');
  });

  function createModel(model) {
    model = $.extend({
      objectType: 'DateField'
    }, scout.nvl(model, {}));
    model = $.extend(model, createSimpleModel(model.objectType, session));
    registerAdapterData(model, session);
    return model;
  }

  function createField(modelProperties) {
    var model = createModel(modelProperties);
    return session.getOrCreateWidget(model.id, session.desktop);
  }

  function createFieldAndFocusAndOpenPicker(modelProperties) {
    var dateField = createField(modelProperties);
    dateField.render();

    focusAndOpenDatePicker(dateField);

    return dateField;
  }

  function focusAndOpenDatePicker(dateField) {
    focusDate(dateField);
    openDatePicker(dateField);
  }

  function focusDate(dateField) {
    dateField.$dateField.focus();
    jasmine.clock().tick(101);
    expect(dateField.$dateField).toBeFocused();
  }

  function openDatePicker(dateField) {
    dateField.$dateField.triggerMouseDown();
    expect(findDatePicker().length).toBe(1);
  }

  function findDatePicker() {
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

  function find$Day(picker, date) {
    var $box = picker.currentMonth.$container;
    return $box.find('.date-picker-day').filter(function(i, elem) {
      var $day = $(elem);
      return (scout.dates.isSameDay(date, $day.data('date')));
    }.bind(this));
  }

  describe('displayText', function() {

    it('is shown correctly after rendering', function() {
      var dateField = scout.create('DateField', {
        parent: session.desktop,
        displayText: '14.04.2016\n12:28',
        hasDate: true,
        hasTime: true
      });
      dateField.render();
      dateField.dateDisplayText = '14.04.2016';
      dateField.timeDisplayText = '12:28';
      expect(dateField.$dateField.val()).toBe('14.04.2016');
      expect(dateField.$timeField.val()).toBe('12:28');
      expect(dateField.displayText).toBe('14.04.2016\n12:28');
    });

    it('is removed properly when setting to \'\'', function() {
      var dateField = scout.create('DateField', {
        parent: session.desktop,
        displayText: '14.04.2016\n12:28',
        hasDate: true,
        hasTime: true
      });
      dateField.render();

      dateField.setDisplayText('');
      expect(dateField.dateDisplayText).toBe('');
      expect(dateField.timeDisplayText).toBe('');
      expect(dateField.$dateField.val()).toBe('');
      expect(dateField.$timeField.val()).toBe('');
    });

  });

  describe('parseAndSetValue', function() {

    it('parses and sets the value', function() {
      var field = scout.create('DateField', {
        parent: session.desktop,
        hasDate: true,
        hasTime: true
      });
      field.parseAndSetValue('14.04.2016\n12:28');
      expect(field.dateDisplayText).toBe('14.04.2016');
      expect(field.timeDisplayText).toBe('12:28');
      expect(field.displayText).toBe('14.04.2016\n12:28');
      expect(field.value.toISOString()).toBe(scout.dates.create('2016-04-14 12:28:00.000').toISOString());
    });

  });

  describe('init', function() {

    it('sets display text using formatValue if value is set initially', function() {
      var field = scout.create('DateField', {
        parent: session.desktop,
        value: '2014-10-01 05:00:00.000',
        hasTime: true
      });
      field.render();
      expectDate(field.value, 2014, 10, 1);
      expect(field.$dateField.val()).toBe('01.10.2014');
      expect(field.$timeField.val()).toBe('05:00');
      expect(field.displayText).toBe('01.10.2014\n05:00');
      expect(field.empty).toBe(false);
    });

  });

  describe('setValue', function() {

    it('sets the value, formats it and sets the display text', function() {
      var field = scout.create('DateField', {
        parent: session.desktop,
        hasTime: true
      });
      field.setValue(scout.dates.create('2017-05-23 12:30:00.000'));
      expect(field.value.toISOString()).toBe(scout.dates.create('2017-05-23 12:30:00.000').toISOString());
      expect(field.displayText).toBe('23.05.2017\n12:30');
      field.setValue(null);
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');
    });

    it('does not set the value but the error status and display text if the validation fails', function() {
      var field = scout.create('DateField', {
        parent: session.desktop,
        hasTime: true
      });
      field.setValidator(function(value) {
        throw new Error('Validation failed');
      });
      field.setValue(scout.dates.create('2017-05-23 12:30:00.000'));
      expect(field.value).toBe(null);
      expect(field.errorStatus instanceof scout.Status).toBe(true);
      expect(field.displayText).toBe('23.05.2017\n12:30');
    });

    it('deletes the error status if value is valid', function() {
      var field = scout.create('DateField', {
        parent: session.desktop,
        hasTime: true
      });
      field.setValidator(function(value) {
        throw new Error('Validation failed');
      });
      field.setValue(scout.dates.create('2017-05-23 12:30:00.000'));
      expect(field.value).toBe(null);
      expect(field.errorStatus instanceof scout.Status).toBe(true);

      field.setValidator(function(value) {
        return value;
      });
      field.setValue(scout.dates.create('2017-05-23 12:30:00.000'));
      expect(field.value.toISOString()).toBe(scout.dates.create('2017-05-23 12:30:00.000').toISOString());
      expect(field.errorStatus).toBe(null);
    });

  });

  describe('acceptInput', function() {

    it('validate again if a new date was typed and reverted', function() {
      var field = scout.create('DateField', {
        parent: session.desktop,
        hasTime: true
      });
      field.render();
      field.$dateField.focus();
      field.setValidator(function(value) {
        if (scout.dates.equals(value, scout.dates.create('2017-05-23 12:30:00.000'))) {
          throw new Error('Validation failed');
        }
        return value;
      });
      // Enter invalid date
      field.setValue(scout.dates.create('2017-05-23 12:30:00.000'));
      expect(field.value).toBe(null);
      expect(field.errorStatus instanceof scout.Status).toBe(true);

      // Enter another date, but don't press enter
      field.$dateField.val('23.05.201');
      field._onDateFieldInput();
      expect(field.value).toBe(null);
      expect(field.errorStatus).toBe(null);

      // Revert to the old date and press enter -> validate value needs to be executed again
      field.$dateField.val('23.05.2017');
      field._onDateFieldInput();
      field.acceptInput();
      expect(field.value).toBe(null);
      expect(field.errorStatus instanceof scout.Status).toBe(true);
    });

  });

  describe('acceptDate', function() {
    it('removes time if date was deleted', function() {
      var dateField = scout.create('DateField', {
        parent: session.desktop,
        value: '2014-10-01 05:00:00.000',
        hasTime: true
      });
      dateField.render();
      focusDate(dateField);
      openDatePicker(dateField);
      expectDate(dateField.value, 2014, 10, 1);
      expect(dateField.$dateField.val()).toBe('01.10.2014');
      expect(dateField.$timeField.val()).toBe('05:00');

      dateField.$dateField.val('');
      expect(dateField.$dateField.val()).toBe('');
      expect(dateField.$timeField.val()).toBe('05:00');

      dateField.acceptDate();
      expect(dateField.$dateField.val()).toBe('');
      expect(dateField.$timeField.val()).toBe('');
      expect(dateField.value).toBe(null);
    });

    it('does not remove time if date was deleted but time has an error', function() {
      var dateField = scout.create('DateField', {
        parent: session.desktop,
        displayText: '01.10.2014\nasdf',
        hasTime: true
      });
      dateField.render();
      focusDate(dateField);
      openDatePicker(dateField);
      dateField.acceptInput();
      expect(dateField.$dateField.val()).toBe('01.10.2014');
      expect(dateField.$timeField.val()).toBe('asdf');
      expect(dateField.errorStatus instanceof scout.Status).toBe(true);

      dateField.$dateField.val('');
      expect(dateField.$dateField.val()).toBe('');
      expect(dateField.$timeField.val()).toBe('asdf');

      dateField.acceptDate();
      expect(dateField.$dateField.val()).toBe('');
      expect(dateField.$timeField.val()).toBe('asdf');
      expect(dateField.value).toBe(null);
      expect(dateField.errorStatus instanceof scout.Status).toBe(true);
    });

  });

  describe('acceptTime', function() {
    it('removes date if time was deleted', function() {
      var dateField = scout.create('DateField', {
        parent: session.desktop,
        value: '2014-10-01 05:00:00.000',
        hasTime: true
      });
      dateField.render();
      focusDate(dateField);
      openDatePicker(dateField);
      expectDate(dateField.value, 2014, 10, 1);
      expect(dateField.$dateField.val()).toBe('01.10.2014');
      expect(dateField.$timeField.val()).toBe('05:00');

      dateField.$timeField.val('');
      expect(dateField.$dateField.val()).toBe('01.10.2014');
      expect(dateField.$timeField.val()).toBe('');

      dateField.acceptTime();
      expect(dateField.$dateField.val()).toBe('');
      expect(dateField.$timeField.val()).toBe('');
      expect(dateField.value).toBe(null);
    });

    it('does not remove date if time was deleted but date has an error', function() {
      var dateField = scout.create('DateField', {
        parent: session.desktop,
        displayText: 'asdf\n05:00',
        hasTime: true
      });
      dateField.render();
      focusDate(dateField);
      openDatePicker(dateField);
      dateField.acceptInput();
      expect(dateField.$dateField.val()).toBe('asdf');
      expect(dateField.$timeField.val()).toBe('05:00');
      expect(dateField.errorStatus instanceof scout.Status).toBe(true);

      dateField.$timeField.val('');
      expect(dateField.$dateField.val()).toBe('asdf');
      expect(dateField.$timeField.val()).toBe('');

      dateField.acceptTime();
      expect(dateField.$dateField.val()).toBe('asdf');
      expect(dateField.$timeField.val()).toBe('');
      expect(dateField.value).toBe(null);
      expect(dateField.errorStatus instanceof scout.Status).toBe(true);
    });

  });

  describe('click', function() {

    it('opens the datepicker', function() {
      var dateField = scout.create('DateField', {
        parent: session.desktop
      });
      dateField.render();
      expect(findDatePicker().length).toBe(0);

      dateField.$dateField.triggerMouseDown();
      expect(findDatePicker().length).toBe(1);
    });

    it('opens the picker and preselects the current date but not the previous date if it was cleared before', function() {
      var dateField = scout.create('DateField', {
        parent: session.desktop
      });
      dateField.render();
      dateField.setValue(scout.dates.create('2017-05-23 00:00:00.000'));
      dateField.$dateField.triggerMouseDown();

      expect(scout.dates.isSameDay(dateField.getDatePicker().selectedDate, scout.dates.create('2017-05-23 00:00:00.000'))).toBe(true);
      dateField.popup.close();

      dateField.clear();
      dateField.$dateField.triggerMouseDown();
      expect(dateField.getDatePicker().selectedDate).toBe(null);
      expect(scout.dates.isSameDay(dateField.getDatePicker().preselectedDate, new Date())).toBe(true);
      expect(dateField.value).toBe(null);
    });

  });

  describe('blur', function() {

    it('closes the datepicker', function() {
      var model = createModel();
      var dateField = createFieldAndFocusAndOpenPicker(model);
      expect(findDatePicker().length).toBe(1);

      dateField._onDateFieldBlur();

      expect(findDatePicker().length).toBe(0);
    });

    it('accepts the prediction', function() {
      var model = createModel();
      var dateField = createFieldAndFocusAndOpenPicker(model);

      // Set reference date, so result is reliable for testing
      dateField.autoDate = new Date(2015, 10, 1);
      dateField.$dateField.val('02');
      dateField._onDateFieldBlur();
      expect(dateField.$dateField.val()).toBe('02.11.2015');
    });

    it('accepts the prediction with autoDate', function() {
      var model = createModel();
      model.autoDate = '1999-10-14';
      var dateField = createFieldAndFocusAndOpenPicker(model);

      dateField.$dateField.val('02');
      dateField._onDateFieldBlur();
      expect(dateField.$dateField.val()).toBe('02.10.1999');
    });

    it('updates the model with the selected value', function() {
      var dateField = scout.create('DateField', {
        parent: session.desktop,
        value: '2014-10-01'
      });
      dateField.render();
      focusAndOpenDatePicker(dateField);
      var dateBefore = dateField.value;
      expectDate(dateBefore, 2014, 10, 1);

      dateField.$dateField.val('11.02.2015');
      dateBefore = dateField.value;
      expectDate(dateBefore, 2014, 10, 1);

      dateField._onDateFieldBlur();
      var date = dateField.value;
      expectDate(date, 2015, 2, 11);
    });

    it('sends value and displayText', function() {
      var dateField = createFieldAndFocusAndOpenPicker({
        value: '2014-10-01'
      });

      dateField.$dateField.val('11.02.2015');
      dateField._onDateFieldBlur();
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      // Order is important, displayText needs to be before value
      // Otherwise server would generate a display text as soon as value changes and send it back even if it is the same as the ui is sending
      var events = [
        new scout.RemoteEvent(dateField.id, 'acceptInput', {
          displayText: '11.02.2015',
          value: '2015-02-11 00:00:00.000',
          errorStatus: null,
          showBusyIndicator: true
        })
      ];
      expect(mostRecentJsonRequest()).toContainEventsExactly(events);
    });

    it('does not send value and displayText again if not changed', function() {
      var dateField = createFieldAndFocusAndOpenPicker({
        value: '2014-10-01'
      });

      dateField.$dateField.val('11.02.2015');
      dateField._onDateFieldBlur();
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      dateField._onDateFieldBlur();
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1); // still 1
    });

    it('does not send value and displayText if no date was entered', function() {
      var dateField = createFieldAndFocusAndOpenPicker();

      dateField._onDateFieldBlur();
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(0);
    });

  });

  describe('validation', function() {

    it('invalidates field if value is invalid (not a date)', function() {
      var model = createModel();
      var dateField = createFieldAndFocusAndOpenPicker(model);

      dateField.$dateField.val('33');
      dateField._onDateFieldBlur();

      expect(dateField.$dateField).toHaveClass('has-error');
    });

    it('prevents model update if value is invalid', function() {
      var model = createModel();
      var dateField = createFieldAndFocusAndOpenPicker(model);

      dateField.$dateField.val('33');
      expect(dateField.displayText).toBeFalsy();

      expect(mostRecentJsonRequest()).toBeUndefined();
    });

  });

  describe('picker', function() {

    it('sets selected date as field value when a date was selected', function() {
      var dateField = scout.create('DateField', {
        parent: session.desktop,
        autoDate: '2016-02-05'
      });
      dateField.render();
      focusAndOpenDatePicker(dateField);

      find$Day(dateField.getDatePicker(), new Date(2016, 1, 1)).triggerClick();
      expect(dateField.$dateField.val()).toBe('01.02.2016');
      expect(dateField.displayText).toBe('01.02.2016');
      expect(dateField.value.toISOString()).toBe(scout.dates.create('2016-02-01 00:00:00.000').toISOString());
    });

    it('unselects the date if the field\'s text was removed', function() {
      var dateField = scout.create('DateField', {
        parent: session.desktop,
        autoDate: '2016-02-05'
      });
      dateField.render();
      focusAndOpenDatePicker(dateField);
      find$Day(dateField.getDatePicker(), new Date(2016, 1, 1)).triggerClick();
      openDatePicker(dateField);
      expect(dateField.getDatePicker().selectedDate.toISOString()).toBe(scout.dates.create('2016-02-01 00:00:00.000').toISOString());

      dateField.$dateField.val('');
      dateField.$dateField.trigger('input');
      expect(dateField.getDatePicker().selectedDate).toBe(null);
      expect(dateField.getDatePicker().preselectedDate.toISOString()).toBe(scout.dates.create('2016-02-05 00:00:00.000').toISOString());
    });

    it('sets selected date as field value when a date was selected even if another date was typed', function() {
      var dateField = scout.create('DateField', {
        parent: session.desktop,
        value: '2016-02-01'
      });
      dateField.render();
      focusAndOpenDatePicker(dateField);
      expect(dateField.$dateField.val()).toBe('01.02.2016');
      expect(dateField.displayText).toBe('01.02.2016');
      expect(dateField.value.toISOString()).toBe(scout.dates.create('2016-02-01 00:00:00.000').toISOString());
      expect(dateField.getDatePicker().selectedDate.toISOString()).toBe(scout.dates.create('2016-02-01 00:00:00.000').toISOString());

      // Enter another date
      dateField.$dateField.val('02.02.2016');
      dateField.$dateField.trigger('input');
      expect(dateField.getDatePicker().selectedDate.toISOString()).toBe(scout.dates.create('2016-02-02 00:00:00.000').toISOString());

      // Click the date which was selected when the picker opened
      find$Day(dateField.getDatePicker(), new Date(2016, 1, 1)).triggerClick();
      expect(dateField.$dateField.val()).toBe('01.02.2016');
      expect(dateField.displayText).toBe('01.02.2016');
      expect(dateField.value.toISOString()).toBe(scout.dates.create('2016-02-01 00:00:00.000').toISOString());
    });

  });

  describe('key handling', function() {

    describe('ESC', function() {

      it('closes the datepicker', function() {
        var model = createModel();
        var dateField = createFieldAndFocusAndOpenPicker(model);
        expect(findDatePicker().length).toBe(1);

        dateField.$dateField.triggerKeyDown(scout.keys.ESC);
        expect(findDatePicker().length).toBe(0);
      });

    });

    describe('ENTER', function() {

      it('updates the model with the selected value and closes picker', function() {
        var model = createModel();
        var dateField = scout.create('DateField', {
          parent: session.desktop,
          value: '2014-10-01'
        });
        dateField.render();
        focusAndOpenDatePicker(dateField);
        var dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);

        dateField.$dateField.val('11.02.2015');
        dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);

        dateField.$dateField.triggerKeyDown(scout.keys.ENTER);
        var date = dateField.value;
        expectDate(date, 2015, 2, 11);
        expect(findDatePicker().length).toBe(0);
      });

    });

    describe('DOWN', function() {

      var model;

      beforeEach(function() {
        model = createModel();
        model.value = '2014-10-01';
        model.displayText = '01.10.2014\n';
      });

      it('opens the picker and selects the current date', function() {
        var dateField = scout.create('DateField', {
          parent: session.desktop
        });
        dateField.render();
        dateField.$dateField.triggerKeyDown(scout.keys.DOWN);

        expect(scout.dates.isSameDay(dateField.getDatePicker().selectedDate, new Date())).toBe(true);
        expect(dateField.$dateField.val()).toBe(dateField.isolatedDateFormat.format(new Date()));
        expect(dateField.displayText).toBe(dateField.isolatedDateFormat.format(new Date()));
        expect(dateField.value).toBe(null); // value is still unchanged

        dateField.acceptInput();
        expect(scout.dates.isSameDay(dateField.value, new Date())).toBe(true);
      });

      it('selects the current date if picker is open and no date is selected', function() {
        var dateField = scout.create('DateField', {
          parent: session.desktop
        });
        dateField.render();
        focusDate(dateField);
        dateField.$dateField.triggerKeyDown(scout.keys.DOWN);
        expect(scout.dates.isSameDay(dateField.getDatePicker().selectedDate, new Date())).toBe(true);
        expect(dateField.$dateField.val()).toBe(dateField.isolatedDateFormat.format(new Date()));
        expect(dateField.displayText).toBe(dateField.isolatedDateFormat.format(new Date()));
        expect(dateField.value).toBe(null); // value is still unchanged

        // Clear date
        dateField.$dateField.val('');
        dateField.$dateField.trigger('input');
        expect(dateField.getDatePicker().selectedDate).toBe(null);
        expect(scout.dates.isSameDay(dateField.getDatePicker().preselectedDate, new Date())).toBe(true);

        // Assert that current date is selected
        dateField.$dateField.triggerKeyDown(scout.keys.DOWN);
        expect(scout.dates.isSameDay(dateField.getDatePicker().selectedDate, new Date())).toBe(true);
        expect(dateField.$dateField.val()).toBe(dateField.isolatedDateFormat.format(new Date()));
        expect(dateField.displayText).toBe(dateField.isolatedDateFormat.format(new Date()));
        expect(dateField.value).toBe(null); // value is still unchanged
      });

      it('removes the error status if the date was invalid before opening the picker', function() {
        var dateField = scout.create('DateField', {
          parent: session.desktop,
          displayText: 'asdf'
        });
        dateField.render();
        dateField.acceptInput();
        expect(dateField.errorStatus instanceof scout.Status).toBe(true);
        dateField.$dateField.triggerKeyDown(scout.keys.DOWN);

        expect(scout.dates.isSameDay(dateField.getDatePicker().selectedDate, new Date())).toBe(true);
        expect(dateField.$dateField.val()).toBe(dateField.isolatedDateFormat.format(new Date()));
        expect(dateField.displayText).toBe(dateField.isolatedDateFormat.format(new Date()));
        expect(dateField.value).toBe(null); // value is still unchanged
        expect(dateField.errorStatus).toBe(null);

        dateField.acceptInput();
        expect(scout.dates.isSameDay(dateField.value, new Date())).toBe(true);
      });

      it('increases day by one', function() {
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);

        dateField.$dateField.triggerKeyDown(scout.keys.DOWN);

        dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);
        expect(dateField.$dateField.val()).toBe('02.10.2014');
        expect(dateField.displayText).toBe('02.10.2014');
        expectDate(dateField.value, 2014, 10, 1); // value is still unchanged

        dateField.acceptInput();
        expectDate(dateField.value, 2014, 10, 2);
      });

      it('increases month by one if shift is used as modifier', function() {
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);

        dateField.$dateField.triggerKeyDown(scout.keys.DOWN, 'shift');
        dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);
        expect(dateField.$dateField.val()).toBe('01.11.2014');
      });

      it('increases year by one if ctrl is used as modifier', function() {
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);

        dateField.$dateField.triggerKeyDown(scout.keys.DOWN, 'ctrl');

        dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);
        expect(dateField.$dateField.val()).toBe('01.10.2015');
      });

    });

    describe('UP', function() {

      var model;

      beforeEach(function() {
        model = createModel();
        model.value = '2014-10-01';
        model.displayText = '01.10.2014\n';
      });

      it('decreases day by one', function() {
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);

        dateField.$dateField.triggerKeyDown(scout.keys.UP);

        dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);
        expect(dateField.$dateField.val()).toBe('30.09.2014');
      });

      it('decreases month by one if shift is used as modifier', function() {
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);

        dateField.$dateField.triggerKeyDown(scout.keys.UP, 'shift');

        dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);
        expect(dateField.$dateField.val()).toBe('01.09.2014');
      });

      it('decreases year by one if ctrl is used as modifier', function() {
        var dateField = createFieldAndFocusAndOpenPicker(model);
        var dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);

        dateField.$dateField.triggerKeyDown(scout.keys.UP, 'ctrl');

        dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);
        expect(dateField.$dateField.val()).toBe('01.10.2013');
      });

    });

  });

  describe('date validation and prediction', function() {

    it('can validate inputs', function() {
      var dateField = scout.create('DateField', {
        parent: session.desktop
      });

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

    it('can predict dates', function() {
      var dateField = scout.create('DateField', {
        parent: session.desktop
      });
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

    it('can predict yyyy.MM', function() {
      var model = createModel();
      model.dateFormatPattern = 'yyyy.MM';
      var dateField = createFieldAndFocusAndOpenPicker(model);
      var now = new Date();

      dateField.$dateField.val('2');
      dateField._onDateFieldBlur();
      expect(dateField.$dateField.val()).toBe('2002.' + ('0' + (now.getMonth() + 1)).slice(-2));
    });

  });

  it('can predict partial years', function() {
    var model = createModel();
    model.value = '2017-11-11';
    var dateField = createFieldAndFocusAndOpenPicker(model);

    dateField.$dateField.val('11.11.68');
    dateField._onDateFieldBlur();
    expect(dateField.$dateField.val()).toBe('11.11.1968');

    dateField.$dateField.val('11.11.68');
    dateField._onDateFieldBlur();
    expect(dateField.$dateField.val()).toBe('11.11.1968');
  });

  describe('allowed dates', function() {

    it('_referenceDate returns only allowed date - only one date', function() {
      var dateField = scout.create('DateField', {
        parent: session.desktop,
        allowedDates: ["2016-04-15"]
      });

      var date = dateField._referenceDate();
      expectDate(date, 2016, 4, 15);
    });

    it('_referenceDate returns only allowed date - choose nearest date in the future', function() {
      var dateField = scout.create('DateField', {
        parent: session.desktop,
        allowedDates: ["2016-03-14", "2016-04-16", "2016-04-17"],
        autoDate: '2016-04-15'
      });
      var date = dateField._referenceDate();
      expectDate(date, 2016, 4, 16);
    });

    it('_referenceDate returns only allowed date - when no date in future is available, choose nearest date in past', function() {
      var dateField = scout.create('DateField', {
        parent: session.desktop,
        allowedDates: ["2016-02-14", "2016-03-16", "2016-04-03"],
        autoDate: '2016-04-15'
      });
      var date = dateField._referenceDate();
      expectDate(date, 2016, 4, 3);
    });

    it('_setAllowedDates must convert date strings into Dates', function() {
      var dateField = scout.create('DateField', {
        parent: session.desktop
      });
      dateField._setAllowedDates(["2016-02-14"]);
      expectDate(dateField.allowedDates[0], 2016, 2, 14);
    });

  });

  describe('touch = true', function() {

    describe('touch popup', function() {

      it('updates display text and is not used for time fields', function() {
        var dateField = scout.create('DateField', {
          parent: session.desktop,
          autoDate: '1999-10-03',
          touch: true,
          hasTime: true
        });
        dateField.render();

        dateField.$dateField.triggerMouseDown();
        expect(findDatePicker().length).toBe(1);

        selectFirstDayInPicker(dateField.popup._widget.currentMonth.$container);

        dateField.$timeField.val('0442');
        dateField._onTimeFieldBlur();

        // selected date in picker (first day) must be 09/27/1999
        expect(dateField.$dateField.text()).toBe('27.09.1999');
        expect(dateField.$timeField.text()).toBe('04:42');
        expect(dateField.displayText).toBe('27.09.1999\n04:42');

        dateField.$dateField.triggerMouseDown();
        expect(findDatePicker().length).toBe(1);
      });

      it('is opened if datefield is touched', function() {
        var dateField = scout.create('DateField', {
          parent: session.desktop,
          touch: true
        });
        dateField.render();

        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);
        expect($('.touch-popup').length).toBe(1);
        expect($('.date-picker-popup').length).toBe(0);
        dateField.popup.close();
      });

      it('is not opened if timefield is touched', function() {
        var dateField = scout.create('DateField', {
          parent: session.desktop,
          touch: true,
          hasTime: true
        });
        dateField.render();

        dateField.$timeField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);
        expect($('.touch-popup').length).toBe(1);
        expect($('.date-picker-popup').length).toBe(0);
      });

      it('is closed when date in picker is selected', function() {
        var dateField = scout.create('DateField', {
          parent: session.desktop,
          touch: true,
        });
        dateField.render();

        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);

        var selectedDate = selectFirstDayInPicker(dateField.popup._widget.currentMonth.$container);
        expect(dateField.popup).toBe(null);
      });

      it('unregisters clone after close', function() {
        var dateField = scout.create('DateField', {
          parent: session.desktop,
          touch: true
        });
        dateField.render();

        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);

        // Popup creates a clone -> validate that it will be destroyed when popup closes
        var expectedClone = dateField.popup._field;
        expect(dateField).toBe(expectedClone.cloneOf);
        dateField.popup.close();
        expect(dateField.popup).toBe(null);
        expect(expectedClone.destroyed).toBe(true);
      });

      it('updates displayText and value of datefield if date in picker is selected', function() {
        var dateField = scout.create('DateField', {
          parent: session.desktop,
          touch: true
        });
        dateField.render();

        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);

        var selectedDate = selectFirstDayInPicker(dateField.popup._widget.currentMonth.$container);
        expect(dateField.popup).toBe(null);
        expect(dateField.value).toEqual(selectedDate);
        expect(dateField.displayText).toBe(dateField.isolatedDateFormat.format(selectedDate));
        expect(dateField.$dateField.text()).toBe(dateField.displayText);
      });

      it('updates displayText and value of datefield if date in picker is entered', function() {
        var dateField = scout.create('DateField', {
          parent: session.desktop,
          touch: true
        });
        dateField.render();

        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);

        dateField.popup._field.$dateField.val('11.02.2015');
        dateField.popup._field.$dateField.triggerKeyDown(scout.keys.ENTER);
        expect(dateField.popup).toBe(null);
        expectDate(dateField.value, 2015, 02, 11);
        expect(dateField.displayText).toBe('11.02.2015');
        expect(dateField.$dateField.text()).toBe(dateField.displayText);
      });

      it('updates displayText and value of datefield if date and time in picker are entered', function() {
        var dateField = scout.create('DateField', {
          parent: session.desktop,
          touch: true,
          hasTime: true
        });
        dateField.render();

        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);

        dateField.popup._field.$dateField.val('29.02.2016');
        dateField.popup._field.$dateField.triggerKeyDown(scout.keys.ENTER);

        expect(dateField.popup).toBe(null);
        dateField.$timeField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);

        dateField.popup._field.$timeField.val('10:42');
        dateField.popup._field.$timeField.triggerKeyDown(scout.keys.ENTER);

        expect(dateField.popup).toBe(null);

        expectDate(dateField.value, 2016, 02, 29, 10, 42);
        expect(dateField.displayText).toBe('29.02.2016\n10:42');
        expect(dateField.$dateField.text()).toBe('29.02.2016');
        expect(dateField.$timeField.text()).toBe('10:42');
      });

      it('shows datefield with same date as clicked datefield', function() {
        var dateField = scout.create('DateField', {
          parent: session.desktop,
          touch: true,
          value: '2012-07-01',
          displayText: '01.07.2012'
        });
        dateField.render();

        expect(dateField.$dateField.text()).toBe(dateField.displayText);
        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);
        expect(dateField.popup._field.value).toEqual(dateField.value);
        expect(dateField.popup._field.displayText).toBe(dateField.displayText);
        expect(dateField.popup._field.$dateField.val()).toBe(dateField.displayText);
      });

      it('shows datefield with same date as clicked datefield, if field empty initially', function() {
        var dateField = scout.create('DateField', {
          parent: session.desktop,
          touch: true
        });
        dateField.render();

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
        expect(dateField.popup._field.value).toEqual(dateField.value);
        expect(dateField.popup._field.displayText).toBe(dateField.displayText);
        expect(dateField.popup._field.$dateField.val()).toBe(dateField.displayText);
      });

      it('clears displayText and value of datefield if date in picker was removed', function() {
        var dateField = scout.create('DateField', {
          parent: session.desktop,
          touch: true,
          value: '2012-07-01',
          displayText: '01.07.2012'
        });
        dateField.render();

        dateField.$dateField.triggerClick();
        expect(dateField.popup.rendered).toBe(true);
        expect(dateField.popup._field.$dateField.val()).toBe(dateField.displayText);

        dateField.popup._field.$dateField.val('');
        dateField.popup._field.$dateField.triggerKeyDown(scout.keys.ENTER);
        expect(dateField.popup).toBe(null);
        expect(dateField.value).toBe(null);
        expect(dateField.displayText).toBe('');
        expect(dateField.$dateField.text()).toBe('');
      });

      it('shows datefield with same date as clicked datefield, even if value was deleted before', function() {
        var dateField = scout.create('DateField', {
          parent: session.desktop,
          touch: true,
          value: '2012-07-01',
          displayText: '01.07.2012'
        });
        dateField.render();

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
        expect(dateField.value).toEqual(selectedDate);
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
