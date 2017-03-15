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

  function createSmartFieldWithAdapter() {
    var model = helper.createFieldModel('SmartField');
    var smartField = new scout.SmartField();
    smartField.init(model);
    linkWidgetAndAdapter(smartField, 'SmartFieldAdapter');
    return smartField;
  }

  describe('_onKeyUp', function() {
    var smartField;

    beforeEach(function() {
      smartField = createSmartFieldWithAdapter();
    });

    it('does not call _openProposal() when TAB, CTRL or ALT has been pressed', function() {
      smartField.render(session.$entryPoint);
      smartField._openProposal = function(displayText, selectCurrentValue) {};

      var keyEvents = [
        {
          which: scout.keys.TAB
        },
        {
          ctrlKey: true,
          which: scout.keys.A
        },
        {
          altKey: true,
          which: scout.keys.A
        }
      ];

      spyOn(smartField, '_openProposal');
      keyEvents.forEach(function(event) {
        smartField._onKeyUp(event);
      });
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
    var smartField;

    beforeEach(function() {
      smartField = createSmartFieldWithAdapter();
    });

    it('must reset _requestProposal property', function() {
      smartField.render(session.$entryPoint);
      expect(smartField._requestedProposal).toBe(false);
      smartField._openProposal(true);
      expect(smartField._requestedProposal).toBe(true);
      smartField.modelAdapter._syncProposalChooser(null);
      expect(smartField._requestedProposal).toBe(false);
    });

  }),

  describe('_openProposal', function() {
    var events = [null];
    var smartField;

    beforeEach(function() {
      smartField = createSmartFieldWithAdapter();
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
        selectCurrentValue: true
      });
      expect(mostRecentJsonRequest()).toContainEvents([expectedEvent]);
    });

    it('must search by display-text when field is valid and browseAll parameter is false', function() {
      smartField._openProposal(false);
      sendQueuedAjaxCalls();
      var expectedEvent = new scout.Event(smartField.id, 'openProposal', {
        displayText: 'foo',
        browseAll: false,
        selectCurrentValue: false
      });
      expect(mostRecentJsonRequest()).toContainEvents([expectedEvent]);
    });

    it('must "browseAll" when field is invalid', function() {
      smartField.errorStatus = {};
      smartField._openProposal(true);
      sendQueuedAjaxCalls();
      var expectedEvent = new scout.Event(smartField.id, 'openProposal', {
        displayText: 'foo',
        browseAll: true,
        selectCurrentValue: false
      });
      expect(mostRecentJsonRequest()).toContainEvents([expectedEvent]);
    });
  });

  describe('_acceptProposal', function() {
    var smartField;

    beforeEach(function() {
      smartField = createSmartFieldWithAdapter();
    });

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
        showBusyIndicator: false,
        forceClose: false
      });
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
    var smartField;

    beforeEach(function() {
      smartField = createSmartFieldWithAdapter();
    });

    it('opens a touch popup when smart field gets touched', function() {
      var proposalChooser = scout.create('Table', {
        parent: new scout.NullWidget(),
        session: session
      });
      linkWidgetAndAdapter(proposalChooser, 'ProposalChooserAdapter');

      smartField.touch = true;
      smartField.render(session.$entryPoint);
      smartField.$field.click();
      smartField.modelAdapter.onModelPropertyChange(createPropertyChangeEvent(smartField, {
        proposalChooser: proposalChooser.id
      }));
      expect(smartField.popup.rendered).toBe(true);
      expect(smartField.popup._$widgetContainer.has(smartField.proposalChooser.$container));
      expect($('.touch-popup').length).toBe(1);
      expect($('.smart-field-popup').length).toBe(0);

      smartField.popup.close();
      smartField.modelAdapter.onModelPropertyChange(createPropertyChangeEvent(smartField, {
        proposalChooser: null
      }));
      expect(smartField.popup).toBe(null);
      expect($('.touch-popup').length).toBe(0);
      expect($('.smart-field-popup').length).toBe(0);

      // Expect same behavior after a second click
      smartField.$field.click();
      smartField.modelAdapter.onModelPropertyChange(createPropertyChangeEvent(smartField, {
        proposalChooser: proposalChooser.id
      }));
      expect(smartField.popup.rendered).toBe(true);
      expect(smartField.popup._$widgetContainer.has(smartField.proposalChooser.$container));
      expect($('.touch-popup').length).toBe(1);
      expect($('.smart-field-popup').length).toBe(0);
      smartField.popup.close();
    });

    it('opens a touch popup if there already is a proposal chooser while rendering', function() {
      smartField.proposalChooser = scout.create('Table', {
        parent: new scout.NullWidget(),
        session: session
      });
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
      smartField.proposalChooser = scout.create('Table', {
        parent: new scout.NullWidget(),
        session: session
      });
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

  describe('_formatValue', function() {
    var lookupCall;

    beforeEach(function() {
      lookupCall = scout.create('LookupCall', {
        session: session
      });
      lookupCall._textById = function(value) {
        if (value === 1) {
          return 'hello';
        } else {
          return 'bye';
        }
      };
    });

    it('uses a lookupcall to format the value', function() {
      var model = helper.createFieldModel('SmartField', session.desktop, {
        lookupCall: lookupCall
      });
      var smartField = scout.create('SmartField', model);
      expect(smartField.displayText).toBe('');
      smartField.setValue(1);
      expect(smartField.value).toBe(1);
      expect(smartField.displayText).toBe('hello');
      smartField.setValue(2);
      expect(smartField.value).toBe(2);
      expect(smartField.displayText).toBe('bye');
    });

    it('returns empty string if value is null or undefined', function() {
      var model = helper.createFieldModel('SmartField', session.desktop, {
        lookupCall: lookupCall
      });
      var smartField = scout.create('SmartField', model);
      expect(smartField.displayText).toBe('');
      smartField.setValue(null);
      expect(smartField.value).toBe(null);
      expect(smartField.displayText).toBe('');
      smartField.setValue(undefined);
      expect(smartField.value).toBe(undefined);
      expect(smartField.displayText).toBe('');
    });

    it('returns the value as string if there is no lookup call', function() {
      // This is important for the remote case when there is no lookup call at all
      var model = helper.createFieldModel('SmartField');
      var smartField = scout.create('SmartField', model);
      smartField.setDisplayText('hello');
      expect(smartField.displayText).toBe('hello');
      smartField.parseAndSetValue('hello 2');
      expect(smartField.value).toBe('hello 2');
      expect(smartField.displayText).toBe('hello 2');
    });

  });

  describe('multiline', function() {
    var lookupCall;
    beforeEach(function() {
      lookupCall = scout.create('LookupCall', {
        session: session
      });
      lookupCall._textById = function(value) {
        if (value === 1) {
          return 'A Line1\nA Line2';
        } else {
          return 'B Line1\nB Line2';
        }
      };
    });
    it('multi-line lookupcall on single-line field', function() {
      // will be displayed multi-line in proposal, but single-line as display text
      var model = helper.createFieldModel('SmartField', session.desktop, {
        lookupCall: lookupCall
      });
      var smartField = scout.create('SmartField', model);
      smartField.render(session.$entryPoint);
      expect(smartField.displayText).toBe('');
      smartField.setValue(1);
      expect(smartField.value).toBe(1);
      expect(scout.fields.valOrText(smartField, smartField.$field)).toBe('A Line1');
      expect(smartField._additionalLines).toEqual(['A Line2']);
    });
    it('multi-line lookupcall on multi-line field', function() {
      // _additionalLines will be rendered to _$multilineField
      var model = helper.createFieldModel('SmartFieldMultiline', session.desktop, {
        lookupCall: lookupCall
      });
      var smartFieldMultiline = scout.create('SmartFieldMultiline', model);
      smartFieldMultiline.render(session.$entryPoint);
      expect(smartFieldMultiline.displayText).toBe('');
      smartFieldMultiline.setValue(1);
      expect(smartFieldMultiline.value).toBe(1);
      expect(scout.fields.valOrText(smartFieldMultiline, smartFieldMultiline.$field)).toBe('A Line1');
      expect(smartFieldMultiline._additionalLines).toEqual(['A Line2']);
      expect(smartFieldMultiline._$multilineField.html()).toEqual('A Line2');
    });
  });

});
