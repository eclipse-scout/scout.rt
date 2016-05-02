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
      smartField._popup = {};
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
      expect(events[0].displayText).toBe('foo');
      expect(events[0].browseAll).toBe(true);
      expect(events[0].selectCurrentValue).toBe(true);
    });

    it('must search by display-text when field is valid and browseAll parameter is false', function() {
      smartField._openProposal(false);
      expect(events[0].displayText).toBe('foo');
      expect(events[0].selectCurrentValue).toBe(false);
    });

    it('must "browseAll" when field is invalid', function() {
      smartField.errorStatus = {};
      smartField._openProposal(true);
      expect(events[0].displayText).toBe('foo');
      expect(events[0].browseAll).toBe(true);
      expect(events[0].selectCurrentValue).toBe(false);
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
      spyOn(smartField, 'remoteHandler');
      smartField._acceptProposal();
      expect(smartField.remoteHandler).not.toHaveBeenCalled();
    });

    it('send _acceptProposal when displayText has changed', function() {
      smartField.render(session.$entryPoint);
      smartField._oldDisplayText = 'foo';
      smartField.$field.val('bar');
      spyOn(smartField, 'remoteHandler');
      smartField._acceptProposal();
      expect(smartField.remoteHandler).toHaveBeenCalled();
    });

    // test for ticket #168652
    it('send deleteProposal when displayText has been deleted quickly', function() {
      smartField.render(session.$entryPoint);
      smartField._oldDisplayText = 'foo';
      smartField.$field.val('');
      smartField.proposalChooser = {}; // fake proposal-chooser is open
      spyOn(smartField, '_sendDeleteProposal');
      smartField._acceptProposal();
      expect(smartField._sendDeleteProposal).toHaveBeenCalled();
    });

  });

  describe('touch = true', function() {

    it('opens a touch popup when smart field gets touched', function() {
      var proposalChooser = scout.create('Table', {parent: new scout.NullWidget(), session: session, _register: true});

      smartField.touch = true;
      smartField.render(session.$entryPoint);
      smartField.$field.click();
      smartField.onModelPropertyChange(createPropertyChangeEvent(smartField, {
        proposalChooser: proposalChooser.id
      }));
      expect(smartField._popup.rendered).toBe(true);
      expect(smartField._popup._$widgetContainer.has(smartField.proposalChooser.$container));
      expect($('.touch-popup').length).toBe(1);
      expect($('.smart-field-popup').length).toBe(0);

      smartField._popup.close();
      smartField.onModelPropertyChange(createPropertyChangeEvent(smartField, {
        proposalChooser: null
      }));
      expect(smartField._popup.rendered).toBe(false);
      expect($('.touch-popup').length).toBe(0);
      expect($('.smart-field-popup').length).toBe(0);

      // Expect same behavior after a second click
      smartField.$field.click();
      smartField.onModelPropertyChange(createPropertyChangeEvent(smartField, {
        proposalChooser: proposalChooser.id
      }));
      expect(smartField._popup.rendered).toBe(true);
      expect(smartField._popup._$widgetContainer.has(smartField.proposalChooser.$container));
      expect($('.touch-popup').length).toBe(1);
      expect($('.smart-field-popup').length).toBe(0);
      smartField._popup.close();
    });

    it('opens a touch popup if there already is a proposal chooser while rendering', function() {
      smartField.proposalChooser = scout.create('Table', {parent: new scout.NullWidget(), session: session, _register: true});
      smartField.touch = true;
      smartField.render(session.$entryPoint);
      expect(smartField._popup.rendered).toBe(true);
      expect(smartField._popup._$widgetContainer.has(smartField.proposalChooser.$container));
      expect($('.touch-popup').length).toBe(1);
      expect($('.smart-field-popup').length).toBe(0);
      expect($('.smart-field-popup').length).toBe(0);
      smartField._popup.close();
    });

  });


});
