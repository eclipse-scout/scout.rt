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
describe('SmartField', function() {
  var session;
  var smartField;
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
    $('.smart-field-popup').remove();
    $('.touch-popup').remove();
  });

  beforeEach(function() {
    var model = helper.createFieldModel('SmartField');
    smartField = new scout.SmartField();
    smartField.init(model);
    linkWidgetAndAdapter(smartField, 'SmartFieldAdapter');
  });

  describe('_onKeyUp', function() {

    it('doesn not call _openProposal() when TAB has been pressed', function() {
      smartField.render(session.$entryPoint);
      smartField._openProposal = function(displayText, selectCurrentValue) {};
      var event = {
        which: scout.keys.TAB
      };
      spyOn(smartField, '_openProposal');
      smartField._onKeyUp(event);
      expect(smartField._openProposal).not.toHaveBeenCalled();
    });

    it('calls _openProposal() when a character key has been pressed', function() {
      smartField.render(session.$entryPoint);
      smartField._browseOnce = true;
      smartField._openProposal = function(displayText, selectCurrentValue) {};
      var event = {
        which: scout.keys.A
      };
      spyOn(smartField, '_openProposal').and.callThrough();
      smartField._onKeyUp(event);
      expect(smartField._openProposal).toHaveBeenCalled();
    });

  });

  describe('_syncProposalChooser', function() {

    it('must reset _requestProposal property', function() {
      smartField.render(session.$entryPoint);
      expect(smartField._requestedProposal).toBe(false);
      smartField._openProposal(true);
      expect(smartField._requestedProposal).toBe(true);
      smartField._syncProposalChooser({});
      expect(smartField._requestedProposal).toBe(false);
    });

  }),

  describe('_openProposal', function() {

    var events = [null];

    beforeEach(function() {
      smartField.render(session.$entryPoint);
      smartField.$field.val('foo');
      smartField.remoteHandler = function(event, delay) {
        events[0] = event;
      };
    });

    it('must "browse all" when field is valid and browseAll parameter is true', function() {
      smartField._openProposal(true);
      sendQueuedAjaxCalls();
      var expectedEvent = new scout.Event(smartField.id, 'openProposal', {
        displayText: 'foo',
        browseAll: true,
        selectCurrentValue: true});
      expect(mostRecentJsonRequest()).toContainEvents([expectedEvent]);
    });

    it('must search by display-text when field is valid and browseAll parameter is false', function() {
      smartField._openProposal(false);
      sendQueuedAjaxCalls();
      var expectedEvent = new scout.Event(smartField.id, 'openProposal', {
        displayText: 'foo',
        browseAll: false,
        selectCurrentValue: false});
      expect(mostRecentJsonRequest()).toContainEvents([expectedEvent]);
    });

    it('must "browseAll" when field is invalid', function() {
      smartField.errorStatus = {};
      smartField._openProposal(true);
      sendQueuedAjaxCalls();
      var expectedEvent = new scout.Event(smartField.id, 'openProposal', {
        displayText: 'foo',
        browseAll: true,
        selectCurrentValue: false});
      expect(mostRecentJsonRequest()).toContainEvents([expectedEvent]);
    });
  });

  describe('_acceptProposal', function() {

    it('must set displayText', function() {
      smartField.render(session.$entryPoint);
      smartField.$field.val('foo');
      smartField._acceptProposal();
      expect(smartField.displayText).toBe('foo');
    });

    it('must call clearTimeout() for pending typedProposal events', function() {
      smartField.render(session.$entryPoint);
      smartField._sendTimeoutId = null;
      smartField.$field.val('bar');
      smartField._proposalTyped();
      expect(smartField._pendingProposalTyped).toBeTruthy();
      smartField._acceptProposal();
      expect(smartField._pendingProposalTyped).toBe(null);
    });

    it('dont send _acceptProposal when displayText has not changed', function() {
      smartField.render(session.$entryPoint);
      smartField._oldDisplayText = 'foo';
      smartField.$field.val('foo');
      smartField._acceptProposal();

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(0);
    });

    it('send _acceptProposal when displayText has changed', function() {
      smartField.render(session.$entryPoint);
      smartField._oldDisplayText = 'foo';
      smartField.$field.val('bar');
      smartField._acceptProposal();

      sendQueuedAjaxCalls();
      var expectedEvent = new scout.Event(smartField.id, 'acceptProposal', {
        chooser: false,
        displayText: 'bar',
        forceClose: false});
      expect(mostRecentJsonRequest()).toContainEvents([expectedEvent]);
    });

    // test for ticket #168652
    it('send deleteProposal when displayText has been deleted quickly', function() {
      smartField.render(session.$entryPoint);
      smartField._oldDisplayText = 'foo';
      smartField.$field.val('');
      smartField.proposalChooser = {}; // fake proposal-chooser is open
      smartField._acceptProposal();

      sendQueuedAjaxCalls();
      expect(mostRecentJsonRequest()).toContainEventTypesExactly(['deleteProposal']);
    });

  });

  describe('touch = true', function() {

    it('opens a touch popup when smart field gets touched', function() {
      var proposalChooser = scout.create('Table', {parent: new scout.NullWidget(), session: session});
      linkWidgetAndAdapter(proposalChooser, 'ProposalChooserAdapter');

      smartField.touch = true;
      smartField.render(session.$entryPoint);
      smartField.$field.click();
      smartField.remoteAdapter.onModelPropertyChange(createPropertyChangeEvent(smartField, {
        proposalChooser: proposalChooser.id
      }));
      expect(smartField.popup.rendered).toBe(true);
      expect(smartField.popup._$widgetContainer.has(smartField.proposalChooser.$container));
      expect($('.touch-popup').length).toBe(1);
      expect($('.smart-field-popup').length).toBe(0);

      smartField.popup.close();
      smartField.remoteAdapter.onModelPropertyChange(createPropertyChangeEvent(smartField, {
        proposalChooser: null
      }));
      expect(smartField.popup).toBe(null);
      expect($('.touch-popup').length).toBe(0);
      expect($('.smart-field-popup').length).toBe(0);

      // Expect same behavior after a second click
      smartField.$field.click();
      smartField.remoteAdapter.onModelPropertyChange(createPropertyChangeEvent(smartField, {
        proposalChooser: proposalChooser.id
      }));
      expect(smartField.popup.rendered).toBe(true);
      expect(smartField.popup._$widgetContainer.has(smartField.proposalChooser.$container));
      expect($('.touch-popup').length).toBe(1);
      expect($('.smart-field-popup').length).toBe(0);
      smartField.popup.close();
    });

    it('opens a touch popup if there already is a proposal chooser while rendering', function() {
      smartField.proposalChooser = scout.create('Table', {parent: new scout.NullWidget(), session: session});
      smartField.touch = true;
      smartField.render(session.$entryPoint);
      expect(smartField.popup.rendered).toBe(true);
      expect(smartField.popup._$widgetContainer.has(smartField.proposalChooser.$container));
      expect($('.touch-popup').length).toBe(1);
      expect($('.smart-field-popup').length).toBe(0);
      expect($('.smart-field-popup').length).toBe(0);
      smartField.popup.close();
    });

    it('shows smartfield with same text as clicked smartfield', function() {
      smartField.proposalChooser = scout.create('Table', {parent: new scout.NullWidget(), session: session});
      smartField.touch = true;
      smartField.displayText = 'row 1';
      smartField.render(session.$entryPoint);

      smartField.$field.triggerClick();
      expect(smartField.popup.rendered).toBe(true);
      expect(smartField.popup._field.displayText).toBe(smartField.displayText);
      expect(smartField.popup._field.$field.val()).toBe(smartField.displayText);
      smartField.popup.close();
    });

  });


});
