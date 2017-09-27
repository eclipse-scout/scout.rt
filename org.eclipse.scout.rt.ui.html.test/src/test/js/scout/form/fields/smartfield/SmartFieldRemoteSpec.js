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
/* global linkWidgetAndAdapter */
describe('SmartFieldRemote', function() {

  // This spec contains test that use the SmartFieldAdapter (= remote case)

  var session, helper;

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
    removePopups(session);
    removePopups(session, '.touch-popup');
  });

  function createSmartFieldWithAdapter() {
    var model = helper.createFieldModel('SmartField');
    var smartField = new scout.SmartField();
    smartField.init(model);
    linkWidgetAndAdapter(smartField, 'SmartFieldAdapter');
    return smartField;
  }

  describe('openPopup', function() {
    var events = [null], smartField;

    beforeEach(function() {
      smartField = createSmartFieldWithAdapter();
      smartField.render();
      smartField.$field.val('foo');
      smartField.remoteHandler = function(event, delay) {
        events[0] = event;
      };
    });

    it('must "browse all" when field is valid and browse parameter is true', function() {
      smartField.openPopup(true);
      sendQueuedAjaxCalls();
      var expectedEvent = new scout.RemoteEvent(smartField.id, 'lookupByAll', {
        showBusyIndicator: false
      });
      expect(mostRecentJsonRequest()).toContainEvents([expectedEvent]);
    });

    it('must "lookup by text" when called without arguments and display-text is not empty', function() {
      smartField.openPopup();
      jasmine.clock().tick(500); // because we use a debounce in SmartField
      sendQueuedAjaxCalls();
      var expectedEvent = new scout.RemoteEvent(smartField.id, 'lookupByText', {
        showBusyIndicator: false,
        text: 'foo',
      });
      expect(mostRecentJsonRequest()).toContainEvents([expectedEvent]);
    });

    it('must "lookup by text" when field is invalid, even though the browse parameter is true', function() {
      smartField.errorStatus = scout.Status.error({
        message: 'bar'
      });
      smartField.openPopup(true);
      jasmine.clock().tick(500); // because we use a debounce in SmartField
      sendQueuedAjaxCalls();
      var expectedEvent = new scout.RemoteEvent(smartField.id, 'lookupByText', {
        showBusyIndicator: false,
        text: 'foo',
      });
      expect(mostRecentJsonRequest()).toContainEvents([expectedEvent]);
    });
  });

  describe('acceptInput', function() {
    var smartField;

    beforeEach(function() {
      smartField = createSmartFieldWithAdapter();
    });

    it('must set displayText', function() {
      smartField.render();
      smartField.$field.val('foo');
      smartField.acceptInput();
      expect(smartField.displayText).toBe('foo');
    });

    it('must call clearTimeout() for pending lookups', function() {
      smartField.render();
      smartField._pendingLookup = null;
      smartField.$field.val('bar');
      smartField._lookupByTextOrAll();
      expect(smartField._pendingLookup).toBeTruthy();
      smartField.acceptInput();
      expect(smartField._pendingLookup).toBe(null);
    });

    it('don\'t send acceptInput event when display-text has not changed', function() {
      smartField.displayText = 'foo';
      smartField.render();
      smartField.$field.val('foo');
      smartField.acceptInput();

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(0);
    });

    it('send acceptInput event when lookup row is set and display-text has not changed', function() {
      var lookupRow = new scout.LookupRow(123, 'foo');
      smartField.displayText = 'foo';
      smartField.render();
      smartField.$field.val('foo');
      smartField.popup = scout.create('SmartFieldPopup', {
        parent: smartField,
        lookupResult: {
          lookupRows: [lookupRow]
        }
      });
      smartField.acceptInput();

      sendQueuedAjaxCalls();
      var expectedEvent = new scout.RemoteEvent(smartField.id, 'acceptInput', {
        value: 123,
        displayText: 'foo',
        errorStatus: null,
        lookupRow: {
          active: true,
          enabled: true,
          key: 123,
          text: 'foo'
        },
        showBusyIndicator: true
      });
      expect(mostRecentJsonRequest()).toContainEvents([expectedEvent]);
    });

    it('do a "lookup by text" when display-text has changed and no lookup row is set', function() {
      smartField.displayText = 'foo';
      smartField.render();
      smartField.$field.val('bar');
      smartField.acceptInput();

      sendQueuedAjaxCalls();
      var expectedEvent = new scout.RemoteEvent(smartField.id, 'lookupByText', {
        text: 'bar',
        showBusyIndicator: false
      });
      expect(mostRecentJsonRequest()).toContainEvents([expectedEvent]);
    });

  });

  describe('touch mode', function() {
    var smartField;

    beforeEach(function() {
      smartField = createSmartFieldWithAdapter();
    });

    function resolveLookupCall(smartField) {
      smartField.lookupCall.resolveLookup({
        queryBy: scout.QueryBy.ALL,
        lookupRows: [new scout.LookupRow(123, 'foo')]
      });
      jasmine.clock().tick(500);
    }

    it('opens a touch popup when smart field gets touched', function() {
      smartField.touch = true;
      smartField.render();
      smartField.$field.triggerClick();
      resolveLookupCall(smartField);
      expect(smartField.popup.rendered).toBe(true);
      expect($('.touch-popup').length).toBe(1);
      expect($('.smart-field-popup').length).toBe(0);

      smartField.popup.close();
      expect(smartField.popup).toBe(null);
      expect($('.touch-popup').length).toBe(0);
      expect($('.smart-field-popup').length).toBe(0);

      // Expect same behavior after a second click
      smartField.$field.triggerClick();
      resolveLookupCall(smartField);
      expect(smartField.popup.rendered).toBe(true);
      expect($('.touch-popup').length).toBe(1);
      expect($('.smart-field-popup').length).toBe(0);
      smartField.popup.close();
    });

    it('shows smartfield with same text as clicked smartfield', function() {
      smartField.touch = true;
      smartField.displayText = 'row 1';
      smartField.render();

      smartField.$field.triggerClick();
      resolveLookupCall(smartField);
      expect(smartField.popup.rendered).toBe(true);
      expect(smartField.popup._field.displayText).toBe(smartField.displayText);
      expect(smartField.popup._field.$field.val()).toBe(smartField.displayText);
      smartField.popup.close();
    });

  });

});
