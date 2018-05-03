/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
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

  function createFieldWithLookupCall(model, lookupCallModel) {
    lookupCallModel = $.extend({
      objectType: 'DummyLookupCall'
    }, lookupCallModel);

    model = $.extend({}, {
      parent: session.desktop,
      lookupCall: lookupCallModel
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
      expect(field.value).toBe(1);
      field.acceptInput();
      expect(eventTriggered).toBe(false);
    });

    // ticket #214831
    it('should not be triggered, when search text is (still) empty or equals to the text of the lookup row (lookupRow.text is null)', function() {
      var field = createFieldWithLookupCall({}, {
        showText: false
      });
      var eventTriggered = false;
      field.render();
      field.on('acceptInput', function() {
        eventTriggered = true;
      });
      // empty case
      field.acceptInput();
      expect(eventTriggered).toBe(false);

      // text equals case
      field.setValue(1); // set lookup row [1, null]
      jasmine.clock().tick(500);
      expect(field.lookupRow.text).toBe(null);
      expect(field.$field.val()).toBe('');
      expect(field.value).toBe(1);
      field.acceptInput();
      expect(eventTriggered).toBe(false);
    });

    // ticket #221944
    describe('should (not) reset selected lookup row', function() {
      var field, selectedLookupRow, searchTextChanged;

      // mocks for popup, lookup-row
      beforeEach(function() {
        field = createFieldWithLookupCall();
        selectedLookupRow = {};
        field.popup = {
          lookupResult: {
            seqNo: 7
          },
          getSelectedLookupRow: function() {
            return selectedLookupRow;
          }
        };
        field._userWasTyping = false;
        field.lookupSeqNo = 7;
        searchTextChanged = false;
      });

      it('use lookup row', function() {
        var lookupRow = field._getSelectedLookupRow(false);
        expect(lookupRow).toBe(selectedLookupRow);
      });

      it('reset when popup is closed', function() {
        field.popup = null;
        expect(field._getSelectedLookupRow(false)).toBe(null);
      });

      it('reset when user was typing or search-text has changed', function() {
        field._userWasTyping = true;
        expect(field._getSelectedLookupRow(true)).toBe(null);
      });

      it('reset when lookup result is out-dated', function() {
        field.lookupSeqNo = 8;
        expect(field._getSelectedLookupRow(false)).toBe(null);
      });

    });

  });

  describe('lookupCall', function() {

    it('should be cloned and prepared for each lookup', function() {
      var templatePropertyValue = 11;
      var preparedPropertyValue = 22;
      var eventCounter = 0;
      var field = createFieldWithLookupCall({}, {
        customProperty: templatePropertyValue,
        _dataToLookupRow: function(data) { // overwrite mapping function to use the custom property
          return new scout.LookupRow(data[0], data[1] + this.customProperty);
        }
      });
      field.on('prepareLookupCall', function(event) {
        expect(event.lookupCall.customProperty).toBe(templatePropertyValue);
        expect(event.lookupCall.id).not.toBe(field.lookupCall.id);
        expect(event.type).toBe('prepareLookupCall');
        expect(event.source).toBe(field);

        event.lookupCall.customProperty = preparedPropertyValue; // change property for this call. Must not have any effect on the next call
        eventCounter++;
      });

      field.setValue(1); // triggers lookup call by key
      jasmine.clock().tick(500);
      expect(field.value).toBe(1);
      expect(field.displayText).toBe('Foo' + preparedPropertyValue);

      field._acceptByText('Bar'); // triggers lookup call by text
      jasmine.clock().tick(500);
      expect(field.value).toBe(2);
      expect(field.displayText).toBe('Bar' + preparedPropertyValue);

      expect(eventCounter).toBe(2);
    });

  });

  describe('lookup', function() {

    it('should increase lookupSeqNo when a lookup is executed', function() {
      var field = createFieldWithLookupCall();
      field.render();
      field.$field.focus();
      expect(field.lookupSeqNo).toBe(0);
      field._lookupByTextOrAll(false, 'Bar');
      jasmine.clock().tick(500);
      expect(field.lookupSeqNo).toBe(1);
      expect(field.popup.lookupResult.seqNo).toBe(1); // seqNo must be set on the lookupResult of the popup
    });

    it('should set error status when result has an exception', function() {
      var field = createFieldWithLookupCall();
      field._lookupByTextOrAllDone({
        queryBy: scout.QueryBy.ALL,
        lookupRows: [],
        exception: 'a total disaster'
      });
      expect(field.errorStatus.severity).toBe(scout.Status.Severity.ERROR);
      expect(field.errorStatus.message).toBe('a total disaster');
    });

    it('_executeLookup should always remove lookup-status (but not the error-status)', function() {
      var field = createFieldWithLookupCall();
      var lookupStatus = scout.Status.warn({
        message: 'bar'
      });
      var errorStatus = scout.Status.error({
        message: 'foo'
      });
      field.setLookupStatus(lookupStatus);
      field.setErrorStatus(errorStatus);
      field._executeLookup(field.lookupCall.cloneForKey(1));
      jasmine.clock().tick(500);
      expect(field.errorStatus).toBe(errorStatus);
      expect(field.lookupStatus).toBe(null);
    });

    /**
     * The hierarchical result contains 2 lookup-rows, but only leafs are counted when the numLookupRows
     * property is set which is used to determine whether or not the result is unqiue.
     */
    it('hierarchical lookup with unique result', function() {
      var field = createFieldWithLookupCall({
        browseHierarchy: true
      }, {
        hierarchical: true
      });
      var result = null;
      field.render();
      field.$field.val('Bar');
      field._lookupByTextOrAll()
        .then(function(result0) {
          result = result0;
        });
      jasmine.clock().tick(500); // 2 ticks required for promises in StaticLookupCall.js
      jasmine.clock().tick(500);
      expect(result.empty).toBe(false);
      expect(result.numLookupRows).toBe(1);
      expect(result.lookupRows.length).toBe(2); // 2 because parent row has been added to result
      expect(result.uniqueMatch.text).toBe('Bar');
      expect(result.byText).toBe(true);
    });

    it('lookupByKey should set first lookup-row from result as this.lookupRow', function() {
      var field = createFieldWithLookupCall();
      var displayText = null;
      field._formatValue(3) // triggers lookup by key
        .then(function(displayText0) {
          displayText = displayText0;
        });
      jasmine.clock().tick(500);
      expect(displayText).toBe('Baz');
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
      embeddedField.setErrorStatus(scout.Status.error({
        message: 'bar'
      }));
      embeddedField.setDisplayText('Foo');

      touchField._copyValuesFromField(embeddedField);

      expect(touchField.lookupRow.text).toBe('baz');
      expect(touchField.errorStatus.message).toBe('bar');
      expect(touchField.displayText).toBe('Foo');
    });

  });

  describe('searchRequired', function() {

    it('opens popup if search available and searchRequired=true', function() {
      var field = createFieldWithLookupCall();
      field.render();
      field.$field.focus(); // must be focused, otherwise popup will not open
      field.setSearchRequired(true);
      field.setDisplayText('Fo'); // DummyLookupCall contains row named 'Foo'.
      var result = field.openPopup();
      jasmine.clock().tick(500);

      expect(field.isPopupOpen()).toBe(true);
      expect(findTableProposals()).toEqual(['Foo']);
      expect(field.lookupStatus).toBe(null);
    });

    it('opens popup if no search available and searchRequired=false', function() {
      var field = createFieldWithLookupCall();
      field.render();
      field.$field.focus(); // must be focused, otherwise popup will not open
      var result = field.openPopup();
      jasmine.clock().tick(500);

      expect(field.isPopupOpen()).toBe(true);
      expect(findTableProposals()).toEqual(['Foo', 'Bar', 'Baz']);
    });

    it('has no popup if no search available and searchRequired=true', function() {
      var field = createFieldWithLookupCall();
      field.render();
      field.$field.focus(); // must be focused, otherwise popup will not open
      field.setSearchRequired(true);
      var result = field.openPopup();
      jasmine.clock().tick(500);

      expect(field.isPopupOpen()).toBe(false);
      expect(field.lookupStatus.code).toBe(scout.SmartField.ErrorCode.SEARCH_REQUIRED);
    });

    it('has empty popup if no search available and searchRequired=true and touch', function() {
      var field = createFieldWithLookupCall({
        touch: true
      });
      field.render();
      field.setSearchRequired(true);
      var result = field.openPopup();
      jasmine.clock().tick(500);

      expect(field.isPopupOpen()).toBe(true);
      expect(findTableProposals().length).toBe(0);
      expect(field.lookupStatus.code).toBe(scout.SmartField.ErrorCode.SEARCH_REQUIRED);
    });

  });

  describe('maxBrowseRowCount', function() {

    it('default - don\'t limit lookup rows', function() {
      var field = createFieldWithLookupCall();
      expect(field.browseMaxRowCount).toBe(100);
      field.render();
      field.$field.focus();
      var result = {
        queryBy: scout.QueryBy.ALL,
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
        queryBy: scout.QueryBy.ALL,
        lookupRows: [1, 2, 3, 4, 5]
      };
      field._lookupByTextOrAllDone(result);
      expect(result.lookupRows.length).toBe(3);
      expect(result.lookupRows[2]).toBe(3); // last element in array should be '3'
      expect(field.popup.proposalChooser.status.severity).toBe(scout.Status.Severity.INFO);
    });

  });

  describe('_onFieldKeyDown', function() {

    beforeEach(function() {
      field = createFieldWithLookupCall();
    });

    it('must update flag _userWasTyping', function() {
      // intial-state
      expect(field._userWasTyping).toBe(false);

      // send a regular key-press (no navigation)
      field._onFieldKeyDown({
        which: scout.keys.A
      });
      expect(field._userWasTyping).toBe(true);

      // when the display text is set, reset the userWasTyping flag
      // this is especially important in the remote case where the
      // server may send a new display text as a result of execFormatValue
      // in that case we don't want the SmartField to start a new search
      // by text, because the user has not typed anything into the field.
      field.setDisplayText('foo');
      expect(field.displayText).toEqual('foo');
      expect(field._userWasTyping).toBe(false);
    });

  });

  describe('_onFieldKeyUp', function() {

    beforeEach(function() {
      field = createFieldWithLookupCall();
    });

    it('does not call openPopup() when TAB, CTRL or ALT has been pressed', function() {
      field.render();
      field.openPopup = function(browse) {};

      var keyEvents = [{
        which: scout.keys.TAB
      }, {
        ctrlKey: true,
        which: scout.keys.A
      }, {
        altKey: true,
        which: scout.keys.A
      }];

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

  describe('label', function() {

    it('is linked with the field', function() {
      var smartField = scout.create('SmartField', {
        parent: session.desktop
      });
      smartField.render();
      expect(smartField.$field.attr('aria-labelledby')).toBeTruthy();
      expect(smartField.$field.attr('aria-labelledby')).toBe(smartField.$label.attr('id'));
    });

    it('focuses the field when clicked', function() {
      var smartField = scout.create('SmartField', {
        parent: session.desktop,
        label: 'label',
        lookupCall: 'DummyLookupCall'
      });
      smartField.render();
      smartField.$label.triggerClick();
      jasmine.clock().tick(500);
      expect(smartField.popup).toBeTruthy();

      smartField.popup.close();
    });

    it('is linked with the field (also in multiline mode)', function() {
      var smartField = scout.create('SmartFieldMultiline', {
        parent: session.desktop,
        label: 'label'
      });
      smartField.render();
      expect(smartField.$field.attr('aria-labelledby')).toBeTruthy();
      expect(smartField.$field.attr('aria-labelledby')).toBe(smartField.$label.attr('id'));
    });

    it('focuses the field when clicked (also in multiline mode)', function() {
      var smartField = scout.create('SmartFieldMultiline', {
        parent: session.desktop,
        label: 'label',
        lookupCall: 'DummyLookupCall'
      });
      smartField.render();
      smartField.$label.triggerClick();
      jasmine.clock().tick(500);
      expect(smartField.popup).toBeTruthy();

      smartField.popup.close();
    });

  });

});
