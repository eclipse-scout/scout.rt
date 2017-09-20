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

  var session, field, lookupRow, helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    field = new scout.SmartField();
    lookupRow = new scout.LookupRow(123, 'Foo');
    helper = new scout.FormSpecHelper(session);
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
    removePopups(session);
    removePopups(session, '.touch-popup');
  });

  function createFieldWithLookupCall(model) {
    model = $.extend({}, {
      parent: session.desktop,
      lookupCall: 'DummyLookupCall'
    }, model);
    return scout.create('SmartField', model);
  }

  function createSmartFieldWithAdapter() {
    var model = helper.createFieldModel('SmartField');
    var smartField = new scout.SmartField();
    smartField.init(model);
    linkWidgetAndAdapter(smartField, 'SmartFieldAdapter');
    return smartField;
  }

  function findTableProposals() {
    var proposals = [];
    session.desktop.$container.find('.table-row').each(function() {
      proposals.push($(this).find('.table-cell').first().text());
    });
    return proposals;
  }

  describe('general behavior', function() {

    it('defaults', function() {
      expect(field.displayStyle).toBe('default');
      expect(field.value).toBe(null);
      expect(field.displayText).toBe(null);
      expect(field.lookupRow).toBe(null);
      expect(field.popup).toBe(null);
    });

    it('setLookupRow', function() {
      field.setLookupRow(lookupRow);
      expect(field.value).toBe(123);
      expect(field.lookupRow).toBe(lookupRow);
      expect(field.displayText).toBe('Foo');
    });

    it('init LookupCall when configured as string', function() {
      field = createFieldWithLookupCall();
      expect(field.lookupCall instanceof scout.DummyLookupCall).toBe(true);
    });

    it('when setValue is called, load and set the correct lookup row', function() {
      field = createFieldWithLookupCall();
      field.setValue(1);
      jasmine.clock().tick(300);
      expect(field.displayText).toBe('Foo');
      expect(field.value).toBe(1);
      expect(field.lookupRow.key).toBe(1);

      // set the value to null again
      field.setValue(null);
      expect(field.lookupRow).toBe(null);
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');

      field.setValue(2);
      jasmine.clock().tick(300);
      expect(field.displayText).toBe('Bar');
      expect(field.value).toBe(2);
      expect(field.lookupRow.key).toBe(2);
    });

    it('load proposals for the current displayText', function() {
      field = createFieldWithLookupCall();
      field.render();
      field.$field.focus(); // must be focused, otherwise popup will not open
      field.$field.val('b');
      field._onFieldKeyUp({});
      jasmine.clock().tick(300);
      expect(field.$container.hasClass('loading')).toBe(false); // loading indicator is not shown before 400 ms
      jasmine.clock().tick(300);
      // expect we have 2 table rows
      expect(field.popup).not.toBe(null);
      expect(findTableProposals()).toEqual(['Bar', 'Baz']);
    });

  });

  describe('clear', function() {

    it('clears the value', function() {
      var field = createFieldWithLookupCall();
      jasmine.clock().tick(500);
      field.render();
      field.$field.focus();
      field.setValue(1);
      jasmine.clock().tick(500);
      field.$field.triggerClick();
      jasmine.clock().tick(500);
      expect(field.value).toBe(1);
      expect(field.displayText).toBe('Foo');
      expect(field.$field.val()).toBe('Foo');
      expect(field.popup.proposalChooser.model.selectedRows.length).toBe(1);

      field.clear();
      jasmine.clock().tick(500);
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');
      expect(field.$field.val()).toBe('');
      expect(field.popup.proposalChooser.model.selectedRows.length).toBe(0);
    });

    it('clears the value, also in touch mode', function() {
      var field = createFieldWithLookupCall({
        touch: true
      });
      field.render();
      field.setValue(1);
      jasmine.clock().tick(500);
      field.$field.triggerClick();
      jasmine.clock().tick(500);
      expect(field.value).toBe(1);
      expect(field.displayText).toBe('Foo');
      expect(field.$field.text()).toBe('Foo');
      expect(field.popup._widget.model.selectedRows.length).toBe(1);

      field.popup._field.$field.focus();
      field.popup._field.clear();
      jasmine.clock().tick(500);
      expect(field.popup._field.value).toBe(null);
      expect(field.popup._field.displayText).toBe('');
      expect(field.popup._field.$field.val()).toBe('');
      expect(field.popup._widget.model.selectedRows.length).toBe(0);

      field.popup.close();
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');
      expect(field.$field.val()).toBe('');
    });

    it('does not close the popup but does a browse all', function() {
      // This is especially important for mobile, but makes sense for regular case too.
      var field = createFieldWithLookupCall();
      field.render();
      field.$field.focus(); // must be focused, otherwise popup will not open
      field.$field.val('b');
      field._onFieldKeyUp({});
      jasmine.clock().tick(500);
      expect(field.popup).not.toBe(null);
      expect(findTableProposals()).toEqual(['Bar', 'Baz']);

      field.clear();
      jasmine.clock().tick(500);
      expect(field.popup).not.toBe(null);
      expect(findTableProposals()).toEqual(['Foo', 'Bar', 'Baz']);
    });

  });

  describe('touch popup', function() {

    it('marks field as clearable even if the field is not focused', function() {
      var field = createFieldWithLookupCall({
        touch: true
      });
      field.render();
      field.$field.focus();
      field.setValue(1);
      jasmine.clock().tick(500);
      field.$field.triggerClick();
      jasmine.clock().tick(500);
      expect(field.popup).not.toBe(null);
      expect(field.popup._field.$field.val()).toBe('Foo');
      expect(field.popup._field.$container).toHaveClass('clearable-always');
    });

    it('stays open if active / inactive radio buttons are clicked', function() {
      var field = createFieldWithLookupCall({
        touch: true,
        activeFilterEnabled: true
      });
      field.render();
      jasmine.clock().tick(500);
      field.$field.triggerClick();
      jasmine.clock().tick(500);
      field.popup._widget.activeFilterGroup.radioButtons[1].select();
      jasmine.clock().tick(500);
      expect(field.popup).not.toBe(null);
    });

    it('stays open even if there are no results (with active filter)', function() {
      // Use case: Click on touch smart field, select inactive radio button, clear the text in the field -> smart field has to stay open
      var field = createFieldWithLookupCall({
        touch: true,
        activeFilterEnabled: true
      });
      field.render();
      jasmine.clock().tick(500);
      field.$field.triggerClick();
      jasmine.clock().tick(500);
      field.popup._widget.activeFilterGroup.radioButtons[1].select();
      // Simulate that lookup call does not return any data (happens if user clicks 'inactive' radio button and there are no inactive rows
      field.popup._field.lookupCall.data = [];
      field.popup._field.$field.focus();
      field.popup._field.$field.triggerKeyDown(scout.keys.BACKSPACE);
      field.popup._field._onFieldKeyUp({});
      jasmine.clock().tick(500);
      expect(field.popup).not.toBe(null);
    });

  });

  describe('acceptInput', function() {

    it('should not be triggered, when search text is (still) empty or equals to the text of the lookup row', function() {
      var field = createFieldWithLookupCall();
      var eventTriggered = false;
      field.render();
      field.on('acceptInput', function() {
        eventTriggered = true;
      });
      // empty case
      field.acceptInput();
      expect(eventTriggered).toBe(false);

      // text equals case
      field.setValue(1); // set lookup row [1, Foo]
      jasmine.clock().tick(500);
      expect(field.lookupRow.text).toBe('Foo');
      expect(field.$field.val()).toBe('Foo');
      field.acceptInput();
      expect(eventTriggered).toBe(false);
    });

  });

  describe('lookup', function() {

    it('should set error status when result has an exception', function() {
      var field = createFieldWithLookupCall();
      field._lookupByTextOrAllDone({
        lookupRows: [],
        exception: 'a total disaster'
      });
      expect(field.errorStatus.severity).toBe(scout.Status.Severity.ERROR);
      expect(field.errorStatus.message).toBe('a total disaster');
    });

  });

  describe('touch / embed', function() {

    it('must clone properties required for embedded field', function() {
      var field = createFieldWithLookupCall({
        touch: true,
        activeFilter: 'TRUE',
        activeFilterEnabled: true,
        activeFilterLabels: ['a', 'b', 'c'],
        browseLoadIncremental: true
      });
      var embedded = field.clone({
        parent: session.desktop
      });
      expect(embedded.activeFilter).toBe('TRUE');
      expect(embedded.activeFilterEnabled).toBe(true);
      expect(embedded.activeFilterLabels).toEqual(['a', 'b', 'c']);
      expect(embedded.browseLoadIncremental).toBe(true);
    });

    it('_copyValuesFromField', function() {
      var touchField = createFieldWithLookupCall();
      var embeddedField = touchField.clone({
        parent: session.desktop
      });
      embeddedField.setLookupRow(new scout.LookupRow(123, 'baz'));
      embeddedField.setErrorStatus(scout.Status.error({message: 'bar'}));
      embeddedField.setDisplayText('Foo');

      touchField._copyValuesFromField(embeddedField);

      expect(touchField.lookupRow.text).toBe('baz');
      expect(touchField.errorStatus.message).toBe('bar');
      expect(touchField.displayText).toBe('Foo');
    });

  });

  describe('maxBrowseRowCount', function() {

    it('default - don\'t limit lookup rows', function() {
      var field = createFieldWithLookupCall();
      expect(field.browseMaxRowCount).toBe(100);
      field.render();
      field.$field.focus();
      var result = {
        lookupRows: [1, 2, 3, 4, 5]
      };
      field._lookupByTextOrAllDone(result);
      expect(result.lookupRows.length).toBe(5); // no limit required
      expect(field.popup.proposalChooser.status).toBe(null);
    });

    it('limit lookup rows', function() {
      var field = createFieldWithLookupCall({
        browseMaxRowCount: 3
      });
      field.render();
      field.$field.focus();
      var result = {
        lookupRows: [1, 2, 3, 4, 5]
      };
      field._lookupByTextOrAllDone(result);
      expect(result.lookupRows.length).toBe(3);
      expect(result.lookupRows[2]).toBe(3); // last element in array should be '3'
      expect(field.popup.proposalChooser.status.severity).toBe(scout.Status.Severity.INFO);
    });

  });

  describe('_onFieldKeyUp', function() {

    beforeEach(function() {
      field = createFieldWithLookupCall();
    });

    it('does not call openPopup() when TAB, CTRL or ALT has been pressed', function() {
      field.render();
      field.openPopup = function(browse) {};

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

      spyOn(field, 'openPopup');
      keyEvents.forEach(function(event) {
        field._onFieldKeyUp(event);
      });
      expect(field.openPopup).not.toHaveBeenCalled();
    });

    it('calls _lookupByTextOrAll() when a character key has been pressed', function() {
      field.render();
      field._pendingOpenPopup = true;
      field._lookupByTextOrAll = function() {};
      var event = {
        which: scout.keys.A
      };
      spyOn(field, '_lookupByTextOrAll').and.callThrough();
      field._onFieldKeyUp(event);
      expect(field._lookupByTextOrAll).toHaveBeenCalled();
    });

  });

  describe('_formatValue', function() {
    var lookupCall;

    beforeEach(function() {
      lookupCall = scout.create('DummyLookupCall', {
        session: session
      });
    });

    it('uses a lookup call to format the value', function() {
      var model = helper.createFieldModel('SmartField', session.desktop, {
        lookupCall: lookupCall
      });
      var smartField = scout.create('SmartField', model);
      expect(smartField.displayText).toBe('');
      smartField.setValue(1);
      jasmine.clock().tick(300);
      expect(smartField.value).toBe(1);
      expect(smartField.displayText).toBe('Foo');
      smartField.setValue(2);
      jasmine.clock().tick(300);
      expect(smartField.value).toBe(2);
      expect(smartField.displayText).toBe('Bar');
    });

    it('returns empty string if value is null or undefined', function() {
      var model = helper.createFieldModel('SmartField', session.desktop, {
        lookupCall: lookupCall
      });
      var smartField = scout.create('SmartField', model);
      expect(smartField.displayText).toBe('');
      smartField.setValue(null);
      jasmine.clock().tick(300);
      expect(smartField.value).toBe(null);
      expect(smartField.displayText).toBe('');
      smartField.setValue(undefined);
      jasmine.clock().tick(300);
      expect(smartField.value).toBe(null);
      expect(smartField.displayText).toBe('');
    });

  });

  describe('multiline', function() {

    var lookupCall;

    beforeEach(function() {
      lookupCall = scout.create('DummyLookupCall', {
        session: session,
        multiline: true
      });
    });

    it('_readSearchText() must concat text of input element and additional lines - required for acceptInput', function() {
      var model = helper.createFieldModel('SmartField', session.desktop, {
        lookupCall: lookupCall,
        value: 1
      });
      var smartField = scout.create('SmartField', model);
      jasmine.clock().tick(300);
      smartField.render();
      expect(smartField._readDisplayText()).toEqual('1:Foo');
      expect(smartField._readSearchText()).toEqual('1:Foo\n2:Foo');

      smartField.$field.val('1:Meep');
      expect(smartField._readDisplayText()).toEqual('1:Meep');
      expect(smartField._readSearchText()).toEqual('1:Meep\n2:Foo');
    });

    it('multi-line lookupcall on single-line field', function() {
      // will be displayed multi-line in proposal, but single-line as display text
      var model = helper.createFieldModel('SmartField', session.desktop, {
        lookupCall: lookupCall,
        value: 1
      });
      var smartField = scout.create('SmartField', model);
      jasmine.clock().tick(300);
      smartField.render();
      expect(smartField.value).toBe(1);
      expect(scout.fields.valOrText(smartField.$field)).toBe('1:Foo');
      expect(smartField.displayText).toEqual('1:Foo\n2:Foo');
    });

    it('multi-line lookupcall on multi-line field', function() {
      // _additionalLines will be rendered to _$multilineField
      var model = helper.createFieldModel('SmartFieldMultiline', session.desktop, {
        lookupCall: lookupCall,
        value: 1
      });
      var smartFieldMultiline = scout.create('SmartFieldMultiline', model);
      jasmine.clock().tick(300);
      smartFieldMultiline.render();
      expect(smartFieldMultiline.value).toBe(1);
      expect(scout.fields.valOrText(smartFieldMultiline.$field)).toBe('1:Foo');
      expect(smartFieldMultiline._$multilineLines.text()).toEqual('2:Foo');
    });
  });

});
