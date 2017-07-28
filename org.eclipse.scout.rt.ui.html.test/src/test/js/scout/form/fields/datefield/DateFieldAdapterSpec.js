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
describe("DateFieldAdapter", function() {
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
    $('.tooltip').remove();
    $('.date-picker').remove();
    $('.time-picker').remove();
  });

  function createWithAdapter(model) {
    model = model || {};
    model = $.extend({
      parent: session.desktop
    }, model);
    var field = scout.create('DateField', model);
    linkWidgetAndAdapter(field, 'DateFieldAdapter');
    return field;
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

  function find$Day(picker, date) {
    var $box = picker.currentMonth.$container;
    return $box.find('.date-picker-day').filter(function(i, elem) {
      var $day = $(elem);
      return (scout.dates.isSameDay(date, $day.data('date')));
    }.bind(this));
  }

  describe('parseValue', function() {

    it('sets the server errorStatus if the displayText was reverted to the one provoking the error', function() {
      var field = createWithAdapter({
        hasTime: true,
        value: scout.dates.create('2017-05-23 12:30:00.000')
      });
      field.modelAdapter._syncErrorStatus({
        message: 'error status from server'
      });
      field.render();
      field.$dateField.focus();
      expect(field.$dateField.val()).toBe('23.05.2017');
      expect(field.errorStatus.message).toBe('error status from server');

      // Enter another date, but don't press enter
      field.$dateField.val('23.05.201');
      field._onDateFieldInput();
      expect(field.value.toISOString()).toBe(scout.dates.create('2017-05-23 12:30:00.000').toISOString());
      expect(field.errorStatus).toBe(null);

      // Revert to the old date and press enter -> send the event so that server may validate again
      field.$dateField.val('23.05.2017');
      field._onDateFieldInput();
      field.acceptInput();
      expect(field.errorStatus.message).toBe('error status from server');
    });

    it('sets the server errorStatus if the displayText was reverted to the one provoking the error using key down/up', function() {
      var field = createWithAdapter({
        value: scout.dates.create('2017-05-23')
      });
      field.modelAdapter._syncErrorStatus({
        message: 'error status from server'
      });
      field.render();
      field.$dateField.focus();
      expect(field.$dateField.val()).toBe('23.05.2017');
      expect(field.errorStatus.message).toBe('error status from server');

      // Enter another date, but don't press enter
      field.$dateField.triggerKeyDown(scout.keys.DOWN);
      expect(field.displayText).toBe('24.05.2017');

      // Revert to the old date and press enter -> send the event so that server may validate again
      field.$dateField.triggerKeyDown(scout.keys.UP);
      expect(field.displayText).toBe('23.05.2017');
      field.acceptInput();
      expect(field.errorStatus.message).toBe('error status from server');
    });

    it('does not accidentially remove the model error status on acceptInput', function() {
      var field = createWithAdapter({
        value: scout.dates.create('2017-05-23')
      });
      field.modelAdapter._syncErrorStatus({
        message: 'error status from server'
      });
      field.render();
      field.acceptInput();
      expect(field.errorStatus.message).toBe('error status from server');
    });

  });

  describe('picker', function() {

    it('sends displayText and value if date was selected', function() {
      var field = createWithAdapter({
        autoDate: '2016-02-05'
      });
      field.render();
      focusDate(field);
      openDatePicker(field);

      find$Day(field.getDatePicker(), new Date(2016, 1, 1)).triggerClick();
      expect(field.$dateField.val()).toBe('01.02.2016');
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      var events = [
        new scout.RemoteEvent(field.id, 'acceptInput', {
          displayText: '01.02.2016',
          value: '2016-02-01 00:00:00.000',
          errorStatus: null,
          showBusyIndicator: true
        })
      ];
      expect(mostRecentJsonRequest()).toContainEventsExactly(events);
    });

  });
});
