/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {DateField, DateFieldModel, dates, keys, RemoteEvent, scout} from '../../../../src/index';
import {triggerClick, triggerKeyDown, triggerMouseDown} from '../../../../src/testing/jquery-testing';
import {Optional} from '../../../../src/types';

describe('DateFieldAdapter', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
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
    override _onDateFieldInput(event?: JQuery.TriggeredEvent) {
      super._onDateFieldInput(event);
    }
  }

  function createWithAdapter(model: Optional<DateFieldModel, 'parent'>): SpecDateField {
    model = model || {};
    model = $.extend({
      parent: session.desktop
    }, model);
    let field = scout.create(SpecDateField, model as DateFieldModel);
    linkWidgetAndAdapter(field, 'DateFieldAdapter');
    return field;
  }

  function focusDate(dateField: DateField) {
    dateField.$dateField.focus();
    jasmine.clock().tick(101);
    expect(dateField.$dateField).toBeFocused();
  }

  function openDatePicker(dateField) {
    triggerMouseDown(dateField.$dateField);
    expect(findDatePicker().length).toBe(1);
  }

  function findDatePicker() {
    return $('.date-picker');
  }

  function find$Day(picker, date) {
    let $box = picker.currentMonth.$container;
    return $box.find('.date-picker-day').filter((i, elem) => {
      let $day = $(elem);
      return (dates.isSameDay(date, $day.data('date')));
    });
  }

  describe('parseValue', () => {

    it('sets the server errorStatus if the displayText was reverted to the one provoking the error', () => {
      let field = createWithAdapter({
        hasTime: true,
        value: dates.create('2017-05-23 12:30:00.000'),
        errorStatus: {
          children: [{message: 'error status from server'}]
        }
      });
      field.render();
      field.$dateField.focus();
      expect(field.$dateField.val()).toBe('23.05.2017');
      expect(field.errorStatus.children.length).toBe(1);
      expect(field.errorStatus.children[0].message).toBe('error status from server');

      // Enter another date, but don't press enter
      field.$dateField.val('23.05.201');
      field._onDateFieldInput();
      expect(field.value.toISOString()).toBe(dates.create('2017-05-23 12:30:00.000').toISOString());
      expect(field.errorStatus.children.length).toBe(1);
      expect(field.errorStatus.children[0].message).toBe('error status from server');

      // Revert to the old date and press enter -> send the event so that server may validate again
      field.$dateField.val('23.05.2017');
      field._onDateFieldInput();
      field.acceptInput();
      expect(field.errorStatus.children.length).toBe(1);
      expect(field.errorStatus.children[0].message).toBe('error status from server');
    });

    it('sets the server errorStatus if the displayText was reverted to the one provoking the error using key down/up', () => {
      let field = createWithAdapter({
        value: dates.create('2017-05-23'),
        errorStatus: {
          children: [{message: 'error status from server'}]
        }
      });
      field.render();
      field.$dateField.focus();
      expect(field.$dateField.val()).toBe('23.05.2017');
      expect(field.errorStatus.children.length).toBe(1);
      expect(field.errorStatus.children[0].message).toBe('error status from server');

      // Enter another date, but don't press enter
      triggerKeyDown(field.$dateField, keys.DOWN);
      expect(field.displayText).toBe('24.05.2017');

      // Revert to the old date and press enter -> send the event so that server may validate again
      triggerKeyDown(field.$dateField, keys.UP);
      expect(field.displayText).toBe('23.05.2017');
      field.acceptInput();
      expect(field.errorStatus.children.length).toBe(1);
      expect(field.errorStatus.children[0].message).toBe('error status from server');
    });

    it('sets the server errorStatus if the displayText was reverted to the one provoking the error using picker', () => {
      let field = createWithAdapter({
        value: dates.create('2017-05-23'),
        errorStatus: {
          children: [{message: 'error status from server'}]
        }
      });
      field.render();
      field.$dateField.focus();
      expect(field.$dateField.val()).toBe('23.05.2017');
      expect(field.errorStatus.children.length).toBe(1);
      expect(field.errorStatus.children[0].message).toBe('error status from server');

      // Open picker and select invalid date again -> error status must not vanish
      openDatePicker(field);
      triggerClick(find$Day(field.getDatePicker(), new Date(2017, 4, 23)));
      expect(field.$dateField.val()).toBe('23.05.2017');
      expect(field.errorStatus.children.length).toBe(1);
      expect(field.errorStatus.children[0].message).toBe('error status from server');
    });

    it('does not accidentally remove the model error status on acceptInput', () => {
      let field = createWithAdapter({
        value: dates.create('2017-05-23'),
        errorStatus: {
          children: [{message: 'error status from server'}]
        }
      });
      field.render();
      field.acceptInput();
      expect(field.errorStatus.children.length).toBe(1);
      expect(field.errorStatus.children[0].message).toBe('error status from server');
    });

  });

  describe('picker', () => {

    it('sends displayText and value if date was selected', () => {
      let field = createWithAdapter({
        autoDate: '2016-02-05'
      });
      field.render();
      focusDate(field);
      openDatePicker(field);

      triggerClick(find$Day(field.getDatePicker(), new Date(2016, 1, 1)));
      expect(field.$dateField.val()).toBe('01.02.2016');
      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      let events = [
        new RemoteEvent(field.id, 'acceptInput', {
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
