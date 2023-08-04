/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  DateField, DateFieldModel, DateFieldPredictionResult, DateFormat, DatePicker, DatePickerTouchPopup, dates, FullModelOf, keys, Popup, RemoteEvent, scout, Status, TimePicker, TimePickerTouchPopup, ValidationFailedStatus
} from '../../../../src/index';
import {FormSpecHelper, JQueryTesting} from '../../../../src/testing/index';

describe('DateField', () => {
  let session: SandboxSession;
  let helper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
    $('.tooltip').remove();
    removePopups(session, '.date-picker-popup');
    removePopups(session, '.time-picker-popup');
    removePopups(session, '.touch-popup');
  });

  class SpecDateField extends DateField {
    declare popup: Popup & { getDatePicker?(): DatePicker; getTimePicker?(): TimePicker };

    override _onDateFieldInput(event?: JQuery.TriggeredEvent) {
      super._onDateFieldInput(event);
    }

    override _onDateFieldBlur(event?: JQuery.BlurEvent) {
      super._onDateFieldBlur(event);
    }

    override _predictDate(inputText: string): DateFieldPredictionResult {
      return super._predictDate(inputText);
    }

    override _referenceDate(): Date {
      return super._referenceDate();
    }

    override _setAllowedDates(allowedDates: (string | Date)[]) {
      super._setAllowedDates(allowedDates);
    }
  }

  function createModel(model?: DateFieldModel): FullModelOf<DateField> {
    model = $.extend({
      objectType: 'DateField'
    }, scout.nvl(model, {}));
    model = $.extend(model, createSimpleModel(model.objectType as string, session));
    registerAdapterData(model, session);
    return model as FullModelOf<DateField>;
  }

  function createField(modelProperties?: DateFieldModel): SpecDateField {
    let model = createModel(modelProperties);
    return session.getOrCreateWidget(model.id, session.desktop) as SpecDateField;
  }

  function createFieldAndFocusAndOpenPicker(modelProperties?: DateFieldModel): SpecDateField {
    let dateField = createField(modelProperties);
    dateField.render();

    focusAndOpenDatePicker(dateField);

    return dateField;
  }

  function focusAndOpenDatePicker(dateField: DateField) {
    focusDate(dateField);
    openDatePicker(dateField);
  }

  function focusDate(dateField: DateField) {
    dateField.$dateField.focus();
    jasmine.clock().tick(101);
    expect(dateField.$dateField).toBeFocused();
  }

  function openDatePicker(dateField: DateField) {
    JQueryTesting.triggerMouseDown(dateField.$dateField);
    expect(findDatePicker().length).toBe(1);
  }

  function findDatePicker(): JQuery {
    return $('.date-picker');
  }

  // Used to expect a date.
  // Deals with the akward 0=january behavior of the Date#getMonth() method,
  // which means month=1 is january
  function expectDate(date: Date, year: number, month: number, day: number, hour?: number, minute?: number) {
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

  function expectTime(date: Date, hour: number, minute: number, second: number) {
    expect(date.getHours()).toBe(hour);
    expect(date.getMinutes()).toBe(minute);
    expect(date.getSeconds()).toBe(second);
  }

  function selectFirstDayInPicker($picker: JQuery) {
    let $day = $picker.find('.date-picker-day').first();
    let date = $day.data('date');
    JQueryTesting.triggerClick($day);
    return date;
  }

  function selectFirstTimeInPicker($picker: JQuery) {
    let $time = $picker.find('.cell.minutes').first();
    let date = $time.data('time');
    JQueryTesting.triggerClick($time);
    return date;
  }

  function find$Day(picker: DatePicker, date: Date): JQuery {
    let $box = picker.currentMonth.$container;
    return $box.find('.date-picker-day').filter((i, elem) => {
      let $day = $(elem);
      return (dates.isSameDay(date, $day.data('date')));
    });
  }

  describe('displayText', () => {

    it('is shown correctly after rendering', () => {
      let dateField = scout.create(DateField, {
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

    it('is removed properly when setting to \'\'', () => {
      let dateField = scout.create(DateField, {
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

  describe('parseAndSetValue', () => {

    it('parses and sets the value', () => {
      let field = scout.create(DateField, {
        parent: session.desktop,
        hasDate: true,
        hasTime: true
      });
      field.parseAndSetValue('14.04.2016\n12:28');
      expect(field.dateDisplayText).toBe('14.04.2016');
      expect(field.timeDisplayText).toBe('12:28');
      expect(field.displayText).toBe('14.04.2016\n12:28');
      expect(field.value.toISOString()).toBe(dates.create('2016-04-14 12:28:00.000').toISOString());
    });

  });

  describe('init', () => {

    it('sets display text using formatValue if value is set initially', () => {
      let field = scout.create(DateField, {
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

  describe('setValue', () => {

    it('sets the value, formats it and sets the display text', () => {
      let field = scout.create(DateField, {
        parent: session.desktop,
        hasTime: true
      });
      field.setValue(dates.create('2017-05-23 12:30:00.000'));
      expect(field.value.toISOString()).toBe(dates.create('2017-05-23 12:30:00.000').toISOString());
      expect(field.displayText).toBe('23.05.2017\n12:30');
      field.setValue(null);
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');
    });

    it('does not set the value but the error status and display text if the validation fails', () => {
      let field = scout.create(DateField, {
        parent: session.desktop,
        hasTime: true
      });
      field.setValidator(value => {
        throw new Error('Validation failed');
      });
      field.setValue(dates.create('2017-05-23 12:30:00.000'));
      expect(field.value).toBe(null);
      expect(field.errorStatus instanceof Status).toBe(true);
      expect(field.displayText).toBe('23.05.2017\n12:30');
    });

    it('deletes the error status if value is valid', () => {
      let field = scout.create(DateField, {
        parent: session.desktop,
        hasTime: true
      });
      field.setValidator(value => {
        throw new Error('Validation failed');
      });
      field.setValue(dates.create('2017-05-23 12:30:00.000'));
      expect(field.value).toBe(null);
      expect(field.errorStatus instanceof Status).toBe(true);

      field.setValue('2019-03-14');
      expect(field.value).toBe(null);
      expect(field.errorStatus instanceof Status).toBe(true);
      expect(field.displayText).toBe('14.03.2019\n00:00');

      field.setValidator(value => {
        return value;
      });
      field.setValue(dates.create('2017-05-23 12:30:00.000'));
      expect(field.value.toISOString()).toBe(dates.create('2017-05-23 12:30:00.000').toISOString());
      expect(field.errorStatus).toBe(null);
    });

  });

  describe('acceptInput', () => {

    it('validate again if a new date was typed and reverted', () => {
      let field = scout.create(SpecDateField, {
        parent: session.desktop,
        hasTime: true
      });
      field.render();
      field.$dateField.focus();
      field.setValidator(value => {
        if (dates.equals(value, dates.create('2017-05-23 12:30:00.000'))) {
          throw new Error('Validation failed');
        }
        return value;
      });
      // Enter invalid date
      field.setValue(dates.create('2017-05-23 12:30:00.000'));
      expect(field.value).toBe(null);
      expect(field.errorStatus.children.length).toBe(1);
      expect(field.errorStatus.children[0] instanceof ValidationFailedStatus).toBe(true);

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
      expect(field.errorStatus.children.length).toBe(1);
      expect(field.errorStatus.children[0] instanceof ValidationFailedStatus).toBe(true);
    });

  });

  describe('acceptDate', () => {
    it('removes time as well if date was deleted', () => {
      let dateField = scout.create(DateField, {
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
      expect(dateField.errorStatus).toBe(null);

      dateField.$dateField.val('');
      expect(dateField.$dateField.val()).toBe('');
      expect(dateField.$timeField.val()).toBe('05:00');

      dateField.acceptDate();
      expect(dateField.$dateField.val()).toBe('');
      expect(dateField.$timeField.val()).toBe('');
      expect(dateField.value).toBe(null);
      expect(dateField.errorStatus instanceof Status).toBe(false);
    });

  });

  describe('acceptTime', () => {
    it('removes date as well if time was deleted', () => {
      let dateField = scout.create(DateField, {
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
      expect(dateField.errorStatus).toBe(null);

      dateField.$timeField.val('');
      expect(dateField.$dateField.val()).toBe('01.10.2014');
      expect(dateField.$timeField.val()).toBe('');

      dateField.acceptTime();
      expect(dateField.$dateField.val()).toBe('');
      expect(dateField.$timeField.val()).toBe('');
      expect(dateField.value).toBe(null);
      expect(dateField.errorStatus instanceof Status).toBe(false);
    });

  });

  describe('click', () => {

    it('opens the datepicker', () => {
      let dateField = scout.create(DateField, {
        parent: session.desktop
      });
      dateField.render();
      expect(findDatePicker().length).toBe(0);

      JQueryTesting.triggerMouseDown(dateField.$dateField);
      expect(findDatePicker().length).toBe(1);
    });

    it('opens the picker and preselects the current date but not the previous date if it was cleared before', () => {
      let dateField = scout.create(DateField, {
        parent: session.desktop
      });
      dateField.render();
      dateField.setValue(dates.create('2017-05-23 00:00:00.000'));
      JQueryTesting.triggerMouseDown(dateField.$dateField);

      expect(dates.isSameDay(dateField.getDatePicker().selectedDate, dates.create('2017-05-23 00:00:00.000'))).toBe(true);
      dateField.popup.close();

      dateField.clear();
      JQueryTesting.triggerMouseDown(dateField.$dateField);
      expect(dateField.getDatePicker().selectedDate).toBe(null);
      expect(dates.isSameDay(dateField.getDatePicker().preselectedDate, new Date())).toBe(true);
      expect(dateField.value).toBe(null);
    });

  });

  describe('blur', () => {

    it('closes the datepicker', () => {
      let model = createModel();
      let dateField = createFieldAndFocusAndOpenPicker(model);
      expect(findDatePicker().length).toBe(1);

      dateField._onDateFieldBlur();

      expect(findDatePicker().length).toBe(0);
    });

    it('accepts the prediction', () => {
      let model = createModel();
      let dateField = createFieldAndFocusAndOpenPicker(model);

      // Set reference date, so result is reliable for testing
      dateField.autoDate = new Date(2015, 10, 1);
      dateField.$dateField.val('02');
      dateField._onDateFieldBlur();
      expect(dateField.$dateField.val()).toBe('02.11.2015');
    });

    it('accepts the prediction with autoDate', () => {
      let model = createModel();
      model.autoDate = '1999-10-14';
      let dateField = createFieldAndFocusAndOpenPicker(model);

      dateField.$dateField.val('02');
      dateField._onDateFieldBlur();
      expect(dateField.$dateField.val()).toBe('02.10.1999');
    });

    it('updates the model with the selected value', () => {
      let dateField = scout.create(SpecDateField, {
        parent: session.desktop,
        value: '2014-10-01'
      });
      dateField.render();
      focusAndOpenDatePicker(dateField);
      let dateBefore = dateField.value;
      expectDate(dateBefore, 2014, 10, 1);

      dateField.$dateField.val('11.02.2015');
      dateBefore = dateField.value;
      expectDate(dateBefore, 2014, 10, 1);

      dateField._onDateFieldBlur();
      let date = dateField.value;
      expectDate(date, 2015, 2, 11);
    });

    it('sends value and displayText', () => {
      let dateField = createFieldAndFocusAndOpenPicker({
        value: '2014-10-01'
      });

      dateField.$dateField.val('11.02.2015');
      dateField._onDateFieldBlur();
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      // Order is important, displayText needs to be before value
      // Otherwise server would generate a display text as soon as value changes and send it back even if it is the same as the ui is sending
      let events = [
        new RemoteEvent(dateField.id, 'acceptInput', {
          displayText: '11.02.2015',
          value: '2015-02-11 00:00:00.000',
          errorStatus: null,
          showBusyIndicator: true
        })
      ];
      expect(mostRecentJsonRequest()).toContainEventsExactly(events);
    });

    it('does not send value and displayText again if not changed', () => {
      let dateField = createFieldAndFocusAndOpenPicker({
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

    it('does not send value and displayText if no date was entered', () => {
      let dateField = createFieldAndFocusAndOpenPicker();

      dateField._onDateFieldBlur();
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(0);
    });

  });

  describe('validation', () => {

    it('invalidates field if value is invalid (not a date)', () => {
      let model = createModel();
      let dateField = createFieldAndFocusAndOpenPicker(model);

      dateField.$dateField.val('33');
      dateField._onDateFieldBlur();

      expect(dateField.$dateField).toHaveClass('has-error');
    });

    it('prevents model update if value is invalid', () => {
      let model = createModel();
      let dateField = createFieldAndFocusAndOpenPicker(model);

      dateField.$dateField.val('33');
      expect(dateField.displayText).toBeFalsy();

      expect(mostRecentJsonRequest()).toBeUndefined();
    });

  });

  describe('picker', () => {

    it('sets selected date as field value when a date was selected', () => {
      let dateField = scout.create(DateField, {
        parent: session.desktop,
        autoDate: '2016-02-05'
      });
      dateField.render();
      focusAndOpenDatePicker(dateField);

      JQueryTesting.triggerClick(find$Day(dateField.getDatePicker(), new Date(2016, 1, 1)));
      expect(dateField.$dateField.val()).toBe('01.02.2016');
      expect(dateField.displayText).toBe('01.02.2016');
      expect(dateField.value.toISOString()).toBe(dates.create('2016-02-01 00:00:00.000').toISOString());
    });

    it('unselects the date if the field\'s text was removed', () => {
      let dateField = scout.create(DateField, {
        parent: session.desktop,
        autoDate: '2016-02-05'
      });
      dateField.render();
      focusAndOpenDatePicker(dateField);
      JQueryTesting.triggerClick(find$Day(dateField.getDatePicker(), new Date(2016, 1, 1)));
      openDatePicker(dateField);
      expect(dateField.getDatePicker().selectedDate.toISOString()).toBe(dates.create('2016-02-01 00:00:00.000').toISOString());

      dateField.$dateField.val('');
      dateField.$dateField.trigger('input');
      expect(dateField.getDatePicker().selectedDate).toBe(null);
      expect(dateField.getDatePicker().preselectedDate.toISOString()).toBe(dates.create('2016-02-05 00:00:00.000').toISOString());
    });

    it('sets selected date as field value when a date was selected even if another date was typed', () => {
      let dateField = scout.create(DateField, {
        parent: session.desktop,
        value: '2016-02-01'
      });
      dateField.render();
      focusAndOpenDatePicker(dateField);
      expect(dateField.$dateField.val()).toBe('01.02.2016');
      expect(dateField.displayText).toBe('01.02.2016');
      expect(dateField.value.toISOString()).toBe(dates.create('2016-02-01 00:00:00.000').toISOString());
      expect(dateField.getDatePicker().selectedDate.toISOString()).toBe(dates.create('2016-02-01 00:00:00.000').toISOString());

      // Enter another date
      dateField.$dateField.val('02.02.2016');
      dateField.$dateField.trigger('input');
      expect(dateField.getDatePicker().selectedDate.toISOString()).toBe(dates.create('2016-02-02 00:00:00.000').toISOString());

      // Click the date which was selected when the picker opened
      JQueryTesting.triggerClick(find$Day(dateField.getDatePicker(), new Date(2016, 1, 1)));
      expect(dateField.$dateField.val()).toBe('01.02.2016');
      expect(dateField.displayText).toBe('01.02.2016');
      expect(dateField.value.toISOString()).toBe(dates.create('2016-02-01 00:00:00.000').toISOString());
    });

  });

  describe('key handling', () => {

    describe('ESC', () => {

      it('closes the datepicker', () => {
        let model = createModel();
        let dateField = createFieldAndFocusAndOpenPicker(model);
        expect(findDatePicker().length).toBe(1);

        JQueryTesting.triggerKeyDown(dateField.$dateField, keys.ESC);
        expect(findDatePicker().length).toBe(0);
      });

    });

    describe('ENTER', () => {

      it('updates the model with the selected value and closes picker', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop,
          value: '2014-10-01'
        });
        dateField.render();
        focusAndOpenDatePicker(dateField);
        let dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);

        dateField.$dateField.val('11.02.2015');
        dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);

        JQueryTesting.triggerKeyDown(dateField.$dateField, keys.ENTER);
        let date = dateField.value;
        expectDate(date, 2015, 2, 11);
        expect(findDatePicker().length).toBe(0);
      });

    });

    describe('DOWN', () => {

      let model;

      beforeEach(() => {
        model = createModel();
        model.value = '2014-10-01';
        model.displayText = '01.10.2014\n';
      });

      it('opens the picker and selects the current date and time', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop,
          hasTime: true
        });
        dateField.render();
        JQueryTesting.triggerKeyDown(dateField.$dateField, keys.DOWN);

        let expectedTime = dates.ceil(new Date(), dateField.timePickerResolution);
        expect(dates.isSameDay(dateField.getDatePicker().selectedDate, new Date())).toBe(true);
        expect(dateField.$dateField.val()).toBe(dateField.isolatedDateFormat.format(new Date()));
        expect(dateField.$timeField.val()).toBe(dateField.isolatedTimeFormat.format(expectedTime));
        expect(dateField.displayText).toBe(dateField.formatValue(expectedTime) as string);
        expect(dateField.value).toBe(null); // value is still unchanged

        dateField.acceptInput();
        expect(dates.isSameDay(dateField.value, new Date())).toBe(true);
        expectTime(dateField.value, expectedTime.getHours(), expectedTime.getMinutes(), expectedTime.getSeconds());
      });

      it('selects the current date if picker is open and no date is selected', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop
        });
        dateField.render();
        focusDate(dateField);
        JQueryTesting.triggerKeyDown(dateField.$dateField, keys.DOWN);
        expect(dates.isSameDay(dateField.getDatePicker().selectedDate, new Date())).toBe(true);
        expect(dateField.$dateField.val()).toBe(dateField.isolatedDateFormat.format(new Date()));
        expect(dateField.displayText).toBe(dateField.isolatedDateFormat.format(new Date()));
        expect(dateField.value).toBe(null); // value is still unchanged

        // Clear date
        dateField.$dateField.val('');
        dateField.$dateField.trigger('input');
        expect(dateField.getDatePicker().selectedDate).toBe(null);
        expect(dates.isSameDay(dateField.getDatePicker().preselectedDate, new Date())).toBe(true);

        // Assert that current date is selected
        JQueryTesting.triggerKeyDown(dateField.$dateField, keys.DOWN);
        expect(dates.isSameDay(dateField.getDatePicker().selectedDate, new Date())).toBe(true);
        expect(dateField.$dateField.val()).toBe(dateField.isolatedDateFormat.format(new Date()));
        expect(dateField.displayText).toBe(dateField.isolatedDateFormat.format(new Date()));
        expect(dateField.value).toBe(null); // value is still unchanged
      });

      it('removes the error status if the date was invalid before opening the picker', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop,
          displayText: 'asdf'
        });
        dateField.render();
        dateField.acceptInput();
        expect(dateField.errorStatus instanceof Status).toBe(true);
        JQueryTesting.triggerKeyDown(dateField.$dateField, keys.DOWN);

        expect(dates.isSameDay(dateField.getDatePicker().selectedDate, new Date())).toBe(true);
        expect(dateField.$dateField.val()).toBe(dateField.isolatedDateFormat.format(new Date()));
        expect(dateField.displayText).toBe(dateField.isolatedDateFormat.format(new Date()));
        expect(dateField.value).toBe(null); // value is still unchanged
        expect(dateField.errorStatus).toBe(null);

        dateField.acceptInput();
        expect(dates.isSameDay(dateField.value, new Date())).toBe(true);
      });

      it('increases day by one', () => {
        let dateField = createFieldAndFocusAndOpenPicker(model);
        let dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);

        JQueryTesting.triggerKeyDown(dateField.$dateField, keys.DOWN);

        dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);
        expect(dateField.$dateField.val()).toBe('02.10.2014');
        expect(dateField.displayText).toBe('02.10.2014');
        expectDate(dateField.value, 2014, 10, 1); // value is still unchanged

        dateField.acceptInput();
        expectDate(dateField.value, 2014, 10, 2);
      });

      it('increases month by one if shift is used as modifier', () => {
        let dateField = createFieldAndFocusAndOpenPicker(model);
        let dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);

        JQueryTesting.triggerKeyDown(dateField.$dateField, keys.DOWN, 'shift');
        dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);
        expect(dateField.$dateField.val()).toBe('01.11.2014');
      });

      it('increases year by one if ctrl is used as modifier', () => {
        let dateField = createFieldAndFocusAndOpenPicker(model);
        let dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);

        JQueryTesting.triggerKeyDown(dateField.$dateField, keys.DOWN, 'ctrl');

        dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);
        expect(dateField.$dateField.val()).toBe('01.10.2015');
      });

      it('increases minutes to the next 30 if pressed in time field', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop,
          hasTime: true,
          value: dates.create('2017-04-14 12:18:00.000')
        });
        dateField.render();
        JQueryTesting.triggerKeyDown(dateField.$timeField, keys.DOWN);

        expectTime(dateField.value, 12, 18, 0);
        expect(dateField.$timeField.val()).toBe('12:30');
        expect(dateField.displayText).toBe('14.04.2017\n12:30');
        expectTime(dateField.value, 12, 18, 0); // value is still unchanged

        dateField.acceptInput();
        expectTime(dateField.value, 12, 30, 0);
      });

    });

    describe('UP', () => {

      let model;

      beforeEach(() => {
        model = createModel();
        model.value = '2014-10-01';
        model.displayText = '01.10.2014\n';
      });

      it('decreases day by one', () => {
        let dateField = createFieldAndFocusAndOpenPicker(model);
        let dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);

        JQueryTesting.triggerKeyDown(dateField.$dateField, keys.UP);

        dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);
        expect(dateField.$dateField.val()).toBe('30.09.2014');
      });

      it('decreases month by one if shift is used as modifier', () => {
        let dateField = createFieldAndFocusAndOpenPicker(model);
        let dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);

        JQueryTesting.triggerKeyDown(dateField.$dateField, keys.UP, 'shift');

        dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);
        expect(dateField.$dateField.val()).toBe('01.09.2014');
      });

      it('decreases year by one if ctrl is used as modifier', () => {
        let dateField = createFieldAndFocusAndOpenPicker(model);
        let dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);

        JQueryTesting.triggerKeyDown(dateField.$dateField, keys.UP, 'ctrl');

        dateBefore = dateField.value;
        expectDate(dateBefore, 2014, 10, 1);
        expect(dateField.$dateField.val()).toBe('01.10.2013');
      });

    });

  });

  describe('date validation and prediction', () => {

    it('can validate inputs', () => {
      let dateField = scout.create(SpecDateField, {
        parent: session.desktop
      });

      dateField.isolatedDateFormat = new DateFormat(session.locale, 'dd.MM.yyyy');
      expect(!!dateField._predictDate('')).toBe(true);
      expect(!!dateField._predictDate(undefined)).toBe(true);
      expect(!!dateField._predictDate('0')).toBe(true);
      expect(!!dateField._predictDate('1')).toBe(true);
      expect(!!dateField._predictDate('+4')).toBe(true);
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
      expect(!!dateField._predictDate('+999999999999999')).toBe(false); // NaN
      expect(!!dateField._predictDate('-999999999999999')).toBe(false); // NaN
    });

    it('can predict dates', () => {
      let dateField = scout.create(SpecDateField, {
        parent: session.desktop
      });
      let now = new Date();

      function expectPrediction(input, expectedPrediction) {
        let prediction = dateField._predictDate(input);
        expect(prediction).not.toBeNull();
        expect(prediction.text).toBe(expectedPrediction);
      }

      dateField.isolatedDateFormat = new DateFormat(session.locale, 'dd.MM.yyyy');
      expectPrediction('0', '01.' + ('0' + (now.getMonth() + 1)).slice(-2) + '.' + now.getFullYear());
      expectPrediction('1', '1.' + ('0' + (now.getMonth() + 1)).slice(-2) + '.' + now.getFullYear());
      expectPrediction('2', '2.' + ('0' + (now.getMonth() + 1)).slice(-2) + '.' + now.getFullYear());
    });

    it('can predict yyyy.MM', () => {
      let model = createModel();
      model.dateFormatPattern = 'yyyy.MM';
      let dateField = createFieldAndFocusAndOpenPicker(model);
      let now = new Date();

      dateField.$dateField.val('2');
      dateField._onDateFieldBlur();
      expect(dateField.$dateField.val()).toBe('2002.' + ('0' + (now.getMonth() + 1)).slice(-2));
    });

  });

  it('can predict partial years', () => {
    let model = createModel();
    model.value = '2017-11-11';
    let dateField = createFieldAndFocusAndOpenPicker(model);

    dateField.$dateField.val('11.11.98');
    dateField._onDateFieldBlur();
    expect(dateField.$dateField.val()).toBe('11.11.1998');

    dateField.$dateField.val('11.11.98');
    dateField._onDateFieldBlur();
    expect(dateField.$dateField.val()).toBe('11.11.1998');
  });

  describe('allowed dates', () => {

    it('_referenceDate returns only allowed date - only one date', () => {
      let dateField = scout.create(SpecDateField, {
        parent: session.desktop,
        allowedDates: ['2016-04-15']
      });

      let date = dateField._referenceDate();
      expectDate(date, 2016, 4, 15);
    });

    it('_referenceDate returns only allowed date - choose nearest date in the future', () => {
      let dateField = scout.create(SpecDateField, {
        parent: session.desktop,
        allowedDates: ['2016-07-14', '2016-04-16', '2016-04-17'],
        autoDate: '2016-04-15'
      });
      let date = dateField._referenceDate();
      expectDate(date, 2016, 4, 16);
    });

    it('_referenceDate returns only allowed date - when no date in future is available, choose nearest date in past', () => {
      let dateField = scout.create(SpecDateField, {
        parent: session.desktop,
        allowedDates: ['2016-02-14', '2016-03-16', '2016-04-03'],
        autoDate: '2016-04-15'
      });
      let date = dateField._referenceDate();
      expectDate(date, 2016, 4, 3);
    });

    it('_setAllowedDates must convert date strings into Dates', () => {
      let dateField = scout.create(SpecDateField, {
        parent: session.desktop
      });
      dateField._setAllowedDates(['2016-02-14']);
      expectDate(dateField.allowedDates[0], 2016, 2, 14);
    });

    it('_setAllowedDates truncates dates', () => {
      let dateField = scout.create(SpecDateField, {
        parent: session.desktop
      });
      dateField._setAllowedDates(['2016-02-14 15:13:00.000', '2016-05-18 11:08:00.000']);
      expectDate(dateField.allowedDates[0], 2016, 2, 14, 0, 0);
      expectDate(dateField.allowedDates[1], 2016, 5, 18, 0, 0);
    });

  });

  describe('touch = true', () => {

    describe('date picker touch popup', () => {

      it('is opened if datefield is touched', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop,
          touchMode: true
        });
        dateField.render();

        JQueryTesting.triggerClick(dateField.$dateField);
        expect(dateField.popup.rendered).toBe(true);
        expect($('.touch-popup').length).toBe(1);
        expect(scout.widget($('.touch-popup')) instanceof DatePickerTouchPopup).toBe(true);
        expect($('.date-picker-popup').length).toBe(0);
        dateField.popup.close();
      });

      it('is closed when date in picker is selected', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop,
          touchMode: true
        });
        dateField.render();

        JQueryTesting.triggerClick(dateField.$dateField);
        expect(dateField.popup.rendered).toBe(true);

        selectFirstDayInPicker(dateField.popup['_widget'].currentMonth.$container);
        expect(dateField.popup).toBe(null);
      });

      it('unregisters clone after close', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop,
          touchMode: true
        });
        dateField.render();

        JQueryTesting.triggerClick(dateField.$dateField);
        expect(dateField.popup.rendered).toBe(true);

        // Popup creates a clone -> validate that it will be destroyed when popup closes
        let expectedClone = dateField.popup['_field'];
        expect(dateField).toBe(expectedClone.cloneOf);
        dateField.popup.close();
        expect(dateField.popup).toBe(null);
        expect(expectedClone.destroyed).toBe(true);
      });

      it('updates displayText and value of datefield if date in picker is selected', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop,
          touchMode: true
        });
        dateField.render();

        JQueryTesting.triggerClick(dateField.$dateField);
        expect(dateField.popup.rendered).toBe(true);

        let selectedDate = selectFirstDayInPicker(dateField.popup['_widget'].currentMonth.$container);
        expect(dateField.popup).toBe(null);
        expect(dateField.value).toEqual(selectedDate);
        expect(dateField.displayText).toBe(dateField.isolatedDateFormat.format(selectedDate));
        expect(dateField.$dateField.text()).toBe(dateField.displayText);
      });

      it('updates displayText and value of datefield if date in picker is entered', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop,
          touchMode: true
        });
        dateField.render();

        JQueryTesting.triggerClick(dateField.$dateField);
        expect(dateField.popup.rendered).toBe(true);

        dateField.popup['_field'].$dateField.val('11.02.2015');
        JQueryTesting.triggerKeyDown(dateField.popup['_field'].$dateField, keys.ENTER);
        expect(dateField.popup).toBe(null);
        expectDate(dateField.value, 2015, 2, 11);
        expect(dateField.displayText).toBe('11.02.2015');
        expect(dateField.$dateField.text()).toBe(dateField.displayText);
      });

      it('updates displayText and value of datefield if date and time in picker are entered', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop,
          touchMode: true,
          hasTime: true
        });
        dateField.render();

        JQueryTesting.triggerClick(dateField.$dateField);
        expect(dateField.popup.rendered).toBe(true);

        dateField.popup['_field'].$dateField.val('29.02.2016');
        JQueryTesting.triggerKeyDown(dateField.popup['_field'].$dateField, keys.ENTER);

        expect(dateField.popup).toBe(null);
        JQueryTesting.triggerClick(dateField.$timeField);
        expect(dateField.popup.rendered).toBe(true);

        dateField.popup['_field'].$timeField.val('10:42');
        JQueryTesting.triggerKeyDown(dateField.popup['_field'].$timeField, keys.ENTER);

        expect(dateField.popup).toBe(null);

        expectDate(dateField.value, 2016, 2, 29, 10, 42);
        expect(dateField.displayText).toBe('29.02.2016\n10:42');
        expect(dateField.$dateField.text()).toBe('29.02.2016');
        expect(dateField.$timeField.text()).toBe('10:42');
      });

      it('shows datefield with same date as clicked datefield', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop,
          touchMode: true,
          value: '2012-07-01',
          displayText: '01.07.2012'
        });
        dateField.render();

        expect(dateField.$dateField.text()).toBe(dateField.displayText);
        JQueryTesting.triggerClick(dateField.$dateField);
        expect(dateField.popup.rendered).toBe(true);
        let field = dateField.popup['_field'];
        expect(field.value).toEqual(dateField.value);
        expect(field.displayText).toBe(dateField.displayText);
        expect(field.$dateField.val()).toBe(dateField.displayText);
      });

      it('shows datefield with same date as clicked datefield, if field empty initially', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop,
          touchMode: true
        });
        dateField.render();

        // Open
        JQueryTesting.triggerClick(dateField.$dateField);
        expect(dateField.popup.rendered).toBe(true);

        // Enter date and close
        dateField.popup['_field'].$dateField.val('11.02.2015');
        JQueryTesting.triggerKeyDown(dateField.popup['_field'].$dateField, keys.ENTER);
        expect(dateField.popup).toBe(null);
        expect(dateField.displayText).toBe('11.02.2015');

        // Reopen and verify
        JQueryTesting.triggerClick(dateField.$dateField);
        expect(dateField.popup.rendered).toBe(true);
        expect(dateField.popup['_field'].value).toEqual(dateField.value);
        expect(dateField.popup['_field'].displayText).toBe(dateField.displayText);
        expect(dateField.popup['_field'].$dateField.val()).toBe(dateField.displayText);
      });

      it('clears displayText and value of datefield if date in picker was removed', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop,
          touchMode: true,
          value: '2012-07-01',
          displayText: '01.07.2012'
        });
        dateField.render();

        JQueryTesting.triggerClick(dateField.$dateField);
        expect(dateField.popup.rendered).toBe(true);
        expect(dateField.popup['_field'].$dateField.val()).toBe(dateField.displayText);

        dateField.popup['_field'].$dateField.val('');
        JQueryTesting.triggerKeyDown(dateField.popup['_field'].$dateField, keys.ENTER);
        expect(dateField.popup).toBe(null);
        expect(dateField.value).toBe(null);
        expect(dateField.displayText).toBe('');
        expect(dateField.$dateField.text()).toBe('');
      });

      it('shows datefield with same date as clicked datefield, even if value was deleted before', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop,
          touchMode: true,
          value: '2012-07-01',
          displayText: '01.07.2012'
        });
        dateField.render();

        // Open and check display text
        JQueryTesting.triggerClick(dateField.$dateField);
        expect(dateField.popup.rendered).toBe(true);
        expect(dateField.popup['_field'].$dateField.val()).toBe(dateField.displayText);

        // Clear text
        dateField.popup['_field'].$dateField.val('');
        JQueryTesting.triggerKeyDown(dateField.popup['_field'].$dateField, keys.ENTER);
        expect(dateField.popup).toBe(null);
        expect(dateField.$dateField.text()).toBe('');

        // Open again
        JQueryTesting.triggerClick(dateField.$dateField);
        expect(dateField.popup.rendered).toBe(true);
        expect(dateField.popup['_field'].$dateField.val()).toBe(dateField.displayText);

        // Select a date
        let selectedDate = selectFirstDayInPicker(dateField.popup.$container.find('.date-picker'));
        expect(dateField.popup).toBe(null);
        expect(dateField.value).toEqual(selectedDate);
        expect(dateField.displayText).toBe(dateField.isolatedDateFormat.format(selectedDate));
        expect(dateField.$dateField.text()).toBe(dateField.displayText);

        // Open again and verify
        JQueryTesting.triggerClick(dateField.$dateField);
        expect(dateField.popup.rendered).toBe(true);
        expect(dateField.popup['_field'].$dateField.val()).toBe(dateField.displayText);
      });

      it('does not remove time if date was cleared but another date selected ', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop,
          touchMode: true,
          hasTime: true,
          value: '2017-05-01 05:50:00.000'
        });
        dateField.render();
        expect(dateField.$dateField.text()).toBe('01.05.2017');
        expect(dateField.$timeField.text()).toBe('05:50');

        // Open and check display text
        JQueryTesting.triggerClick(dateField.$dateField);
        expect(dateField.popup.rendered).toBe(true);
        expect(dateField.popup['_field'].$dateField.val()).toBe('01.05.2017');

        // Clear text
        dateField.popup['_field'].$dateField.focus();
        dateField.popup['_field'].clear();
        // Selecting a date using the mouse when the focus is still in the input field will normally trigger blur, this seems not to happen here -> do it manually
        dateField.popup['_field'].$dateField.blur();

        // Select another date
        let selectedDate = selectFirstDayInPicker(dateField.popup.$container.find('.date-picker'));
        expect(dateField.popup).toBe(null);
        expect(dateField.value).toEqual(selectedDate);
        expect(dateField.$dateField.text()).toBe(dateField.isolatedDateFormat.format(selectedDate));

        // Time must not have been modified
        expect(dateField.$timeField.text()).toBe('05:50');
      });
    });

    describe('time picker touch popup', () => {
      it('is opened if datefield is touched', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop,
          touchMode: true,
          hasDate: false,
          hasTime: true
        });
        dateField.render();

        JQueryTesting.triggerClick(dateField.$timeField);
        expect(dateField.popup.rendered).toBe(true);
        expect($('.touch-popup').length).toBe(1);
        expect(scout.widget($('.touch-popup')) instanceof TimePickerTouchPopup).toBe(true);
        expect($('.time-picker-popup').length).toBe(0);
        dateField.popup.close();
      });

      it('is closed when time in picker is selected', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop,
          touchMode: true,
          hasDate: false,
          hasTime: true
        });
        dateField.render();

        JQueryTesting.triggerClick(dateField.$timeField);
        expect(dateField.popup.rendered).toBe(true);

        selectFirstTimeInPicker(dateField.popup['_widget'].$container);
        expect(dateField.popup).toBe(null);
      });

      it('updates displayText and value of datefield if date in picker is selected', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop,
          touchMode: true,
          hasDate: false,
          hasTime: true
        });
        dateField.render();

        JQueryTesting.triggerClick(dateField.$timeField);
        expect(dateField.popup.rendered).toBe(true);

        let selectedDate = selectFirstTimeInPicker(dateField.popup['_widget'].$container);
        expect(dateField.popup).toBe(null);
        expectTime(dateField.value, selectedDate.getHours(), selectedDate.getMinutes(), selectedDate.getSeconds());
        expect(dateField.displayText).toBe(dateField.isolatedTimeFormat.format(selectedDate));
        expect(dateField.$timeField.text()).toBe(dateField.displayText);
      });

      it('updates displayText and value of datefield if date in picker is entered', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop,
          touchMode: true,
          hasDate: false,
          hasTime: true
        });
        dateField.render();

        JQueryTesting.triggerClick(dateField.$timeField);
        expect(dateField.popup.rendered).toBe(true);

        dateField.popup['_field'].$timeField.val('05:13');
        JQueryTesting.triggerKeyDown(dateField.popup['_field'].$timeField, keys.ENTER);
        expect(dateField.popup).toBe(null);
        expectTime(dateField.value, 5, 13, 0);
        expect(dateField.displayText).toBe('05:13');
        expect(dateField.$timeField.text()).toBe(dateField.displayText);
      });

      it('does not remove date if time was cleared but another time selected ', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop,
          touchMode: true,
          hasTime: true,
          value: '2017-05-01 05:50:00.000'
        });
        dateField.render();
        expect(dateField.$dateField.text()).toBe('01.05.2017');
        expect(dateField.$timeField.text()).toBe('05:50');

        // Open and check display text
        JQueryTesting.triggerClick(dateField.$timeField);
        expect(dateField.popup.rendered).toBe(true);
        expect(dateField.popup['_field'].$timeField.val()).toBe('05:50');

        // Clear text
        dateField.popup['_field'].$timeField.focus();
        dateField.popup['_field'].clear();
        // Selecting a date using the mouse when the focus is still in the input field will normally trigger blur, this seems not to happen here -> do it manually
        dateField.popup['_field'].$timeField.blur();

        // Select another time
        let selectedDate = selectFirstTimeInPicker(dateField.popup['_widget'].$container);
        expect(dateField.popup).toBe(null);
        expectTime(dateField.value, selectedDate.getHours(), selectedDate.getMinutes(), selectedDate.getSeconds());
        expect(dateField.$timeField.text()).toBe(dateField.isolatedTimeFormat.format(selectedDate));

        // Date must not have been modified
        expect(dateField.$dateField.text()).toBe('01.05.2017');
      });
    });

    describe('clear', () => {
      it('removes the display text and sets the value to null', () => {
        let dateField = scout.create(DateField, {
          parent: session.desktop,
          touchMode: true,
          hasTime: true,
          value: '2017-05-01 05:50:00.000'
        });
        dateField.render();
        expect(dateField.$dateField.text()).toBe('01.05.2017');
        expect(dateField.$timeField.text()).toBe('05:50');

        dateField.clear();
        expect(dateField.$dateField.text()).toBe('');
        expect(dateField.$timeField.text()).toBe('');
        expect(dateField.value).toBe(null);
        expect(dateField.displayText).toBe('');
      });
    });
  });

  describe('hasDate', () => {

    it('renders date field if set to true', () => {
      let dateField = scout.create(DateField, {
        parent: session.desktop,
        hasDate: true
      });
      dateField.render();
      expect(dateField.$dateField.length).toBe(1);
    });

    it('renders before time field even if set later', () => {
      let dateField = scout.create(DateField, {
        parent: session.desktop,
        hasDate: false,
        hasTime: true
      });
      dateField.render();
      expect(dateField.$dateField).toBe(null);

      dateField.setHasDate(true);
      expect(dateField.$dateField.length).toBe(1);
      expect(dateField.$dateField.next('.time').length).toBe(1);
    });

    it('does not loose date if hasDate is toggled', () => {
      let field = scout.create(DateField, {
        parent: session.desktop,
        value: '2017-05-01 05:50:00.000',
        hasDate: true,
        hasTime: true
      });
      field.render();
      expect(field.$dateField.val()).toBe('01.05.2017');
      expect(field.$timeField.val()).toBe('05:50');
      expect(field.value.toISOString()).toBe(dates.create('2017-05-01 05:50:00.000').toISOString());
      expect(field.displayText).toBe('01.05.2017\n05:50');

      field.setHasDate(false);
      expect(field.$timeField.val()).toBe('05:50');
      expect(field.value.toISOString()).toBe(dates.create('2017-05-01 05:50:00.000').toISOString());
      expect(field.displayText).toBe('05:50');

      // enter another time, date should be preserved
      field.$timeField.val('02:30');
      field.$timeField.trigger('input');
      field.acceptInput();
      expect(field.value.toISOString()).toBe(dates.create('2017-05-01 02:30:00.000').toISOString());
      expect(field.displayText).toBe('02:30');

      field.setHasDate(true);
      expect(field.$dateField.val()).toBe('01.05.2017');
      expect(field.$timeField.val()).toBe('02:30');
      expect(field.value.toISOString()).toBe(dates.create('2017-05-01 02:30:00.000').toISOString());
      expect(field.displayText).toBe('01.05.2017\n02:30');
    });

    it('sets enabled property correctly if hasDate is toggled', () => {
      let field = scout.create(DateField, {
        parent: session.desktop,
        value: '2017-05-01 05:50:00.000',
        hasDate: true,
        hasTime: true
      });
      field.render();
      field.setEnabled(false);
      expect(field.$dateField.isEnabled()).toBe(false);
      expect(field.$timeField.isEnabled()).toBe(false);

      field.setHasDate(false);
      expect(field.$timeField.isEnabled()).toBe(false);

      field.setHasDate(true);
      expect(field.$dateField.isEnabled()).toBe(false);
      expect(field.$timeField.isEnabled()).toBe(false);

      field.setEnabled(true);
      expect(field.$dateField.isEnabled()).toBe(true);
      expect(field.$timeField.isEnabled()).toBe(true);

      field.setHasDate(false);
      expect(field.$timeField.isEnabled()).toBe(true);

      field.setHasDate(true);
      expect(field.$dateField.isEnabled()).toBe(true);
      expect(field.$timeField.isEnabled()).toBe(true);
    });

  });

  describe('hasTime', () => {

    it('renders time field if set to true', () => {
      let dateField = scout.create(DateField, {
        parent: session.desktop,
        hasTime: true
      });
      dateField.render();
      expect(dateField.$timeField.length).toBe(1);
    });

    it('renders after date field even if set later', () => {
      let dateField = scout.create(DateField, {
        parent: session.desktop,
        hasDate: true
      });
      dateField.render();
      expect(dateField.$timeField).toBe(null);

      dateField.setHasTime(true);
      expect(dateField.$timeField.length).toBe(1);
      expect(dateField.$timeField.prev('.date').length).toBe(1);
    });

    it('does not loose time if hasTime is toggled', () => {
      let field = scout.create(DateField, {
        parent: session.desktop,
        value: '2017-05-01 05:50:00.000',
        hasDate: true,
        hasTime: true
      });
      field.render();
      expect(field.$dateField.val()).toBe('01.05.2017');
      expect(field.$timeField.val()).toBe('05:50');
      expect(field.value.toISOString()).toBe(dates.create('2017-05-01 05:50:00.000').toISOString());
      expect(field.displayText).toBe('01.05.2017\n05:50');

      field.setHasTime(false);
      expect(field.$dateField.val()).toBe('01.05.2017');
      expect(field.value.toISOString()).toBe(dates.create('2017-05-01 05:50:00.000').toISOString());
      expect(field.displayText).toBe('01.05.2017');

      // enter another date, time should be preserved
      field.$dateField.val('02.02.2016');
      field.$dateField.trigger('input');
      field.acceptInput();
      expect(field.value.toISOString()).toBe(dates.create('2016-02-02 05:50:00.000').toISOString());
      expect(field.displayText).toBe('02.02.2016');

      field.setHasTime(true);
      expect(field.$dateField.val()).toBe('02.02.2016');
      expect(field.$timeField.val()).toBe('05:50');
      expect(field.value.toISOString()).toBe(dates.create('2016-02-02 05:50:00.000').toISOString());
      expect(field.displayText).toBe('02.02.2016\n05:50');
    });

    it('sets enabled property correctly if hasTime is toggled', () => {
      let field = scout.create(DateField, {
        parent: session.desktop,
        value: '2017-05-01 05:50:00.000',
        hasDate: true,
        hasTime: true
      });
      field.render();
      field.setEnabled(false);
      expect(field.$dateField.isEnabled()).toBe(false);
      expect(field.$timeField.isEnabled()).toBe(false);

      field.setHasTime(false);
      expect(field.$dateField.isEnabled()).toBe(false);

      field.setHasTime(true);
      expect(field.$dateField.isEnabled()).toBe(false);
      expect(field.$timeField.isEnabled()).toBe(false);

      field.setEnabled(true);
      expect(field.$dateField.isEnabled()).toBe(true);
      expect(field.$timeField.isEnabled()).toBe(true);

      field.setHasTime(false);
      expect(field.$dateField.isEnabled()).toBe(true);

      field.setHasTime(true);
      expect(field.$dateField.isEnabled()).toBe(true);
      expect(field.$timeField.isEnabled()).toBe(true);
    });

  });

  describe('label', () => {

    it('focuses the date field when clicked', () => {
      let field = scout.create(DateField, {
        parent: session.desktop,
        label: 'label'
      });
      field.render();
      JQueryTesting.triggerClick(field.$label);
      expect(field.$dateField).toBeFocused();
      expect(field.popup.rendered).toBe(true);

      field.popup.close();
    });

    it('focuses the time field when clicked if hasDate is false and hasTime is true', () => {
      let field = scout.create(DateField, {
        parent: session.desktop,
        label: 'label',
        hasDate: false,
        hasTime: true
      });
      field.render();
      JQueryTesting.triggerClick(field.$label);
      expect(field.$timeField).toBeFocused();
      expect(field.popup.rendered).toBe(true);

      field.popup.close();
    });

  });

  describe('aria properties', () => {

    it('has aria-required set on date and time if mandatory', () => {
      let field = scout.create(DateField, {
        parent: session.desktop,
        label: 'label',
        mandatory: true,
        hasTime: true
      });
      field.render();

      expect(field.$field.attr('aria-required')).toBeFalsy();
      expect(field.$dateField.attr('aria-required')).toBeTruthy();
      expect(field.$timeField.attr('aria-required')).toBeTruthy();

      field.setMandatory(false);

      expect(field.$field.attr('aria-required')).toBeFalsy();
      expect(field.$dateField.attr('aria-required')).toBeFalsy();
      expect(field.$timeField.attr('aria-required')).toBeFalsy();
    });

    it('has aria-labelledby set on date and time', () => {
      let field = scout.create(DateField, {
        parent: session.desktop,
        label: 'label',
        hasTime: true
      });
      field.render();

      expect(field.$dateField.attr('aria-labelledby')).toBeTruthy();
      expect(field.$dateField.attr('aria-labelledby')).toBe(field.$label.attr('id'));
      expect(field.$dateField.attr('aria-label')).toBeFalsy();

      expect(field.$timeField.attr('aria-labelledby')).toBeTruthy();
      expect(field.$timeField.attr('aria-labelledby')).toBe(field.$label.attr('id'));
      expect(field.$timeField.attr('aria-label')).toBeFalsy();
    });

    it('has date and time field icons set to hidden', () => {
      let field = scout.create(DateField, {
        parent: session.desktop,
        label: 'label',
        hasTime: true
      });
      field.render();
      expect(field.$dateFieldIcon).toHaveAttr('aria-hidden', 'true');
      expect(field.$timeFieldIcon).toHaveAttr('aria-hidden', 'true');
    });

    it('has aria-describedby descriptions for its functionality', () => {
      let field = scout.create(DateField, {
        parent: session.desktop,
        label: 'label',
        hasTime: true
      });
      field.render();
      let $dateFieldDescription = field.$fieldContainer.find('#desc' + field.id + '-date-func-desc');
      expect(field.$dateField.attr('aria-describedby')).toBeTruthy();
      expect(field.$dateField.attr('aria-describedby')).toBe($dateFieldDescription.eq(0).attr('id'));
      expect(field.$dateField.attr('aria-description')).toBeFalsy();

      let $timeFieldDescription = field.$fieldContainer.find('#desc' + field.id + '-time-func-desc');
      expect(field.$timeField.attr('aria-describedby')).toBeTruthy();
      expect(field.$timeField.attr('aria-describedby')).toBe($timeFieldDescription.eq(0).attr('id'));
      expect(field.$timeField.attr('aria-description')).toBeFalsy();
    });

    it('has date and time clear icons correctly rendered with role button and a label', () => {
      let field = scout.create(DateField, {
        parent: session.desktop,
        label: 'label',
        hasTime: true
      });
      field.render();
      expect(field.$dateClearIcon).toHaveAttr('role', 'button');
      expect(field.$dateClearIcon).toHaveAttr('aria-label');
      expect(field.$dateClearIcon.attr('aria-label')).not.toBe('');
      expect(field.$timeClearIcon).toHaveAttr('role', 'button');
      expect(field.$timeClearIcon).toHaveAttr('aria-label');
      expect(field.$timeClearIcon.attr('aria-label')).not.toBe('');
    });
  });
});
