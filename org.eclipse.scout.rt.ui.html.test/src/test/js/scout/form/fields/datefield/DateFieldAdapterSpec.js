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
  });

  describe('parseValue', function() {

    it('sets the server errorStatus if the displayText was reverted to the one provoking the error', function() {
      var field = scout.create('DateField', {
        parent: session.desktop,
        hasTime: true,
        value: scout.dates.create('2017-05-23 12:30:00.000')
      });
      linkWidgetAndAdapter(field, 'DateFieldAdapter');
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
      var field = scout.create('DateField', {
        parent: session.desktop,
        value: scout.dates.create('2017-05-23')
      });
      linkWidgetAndAdapter(field, 'DateFieldAdapter');
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
      var field = scout.create('DateField', {
        parent: session.desktop,
        value: scout.dates.create('2017-05-23')
      });
      linkWidgetAndAdapter(field, 'DateFieldAdapter');
      field.modelAdapter._syncErrorStatus({
        message: 'error status from server'
      });
      field.render();
      field.acceptInput();
      expect(field.errorStatus.message).toBe('error status from server');
    });

  });
});
