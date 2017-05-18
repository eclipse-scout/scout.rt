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
describe('SmartField2', function() {

  var session, field, lookupRow;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    field = new scout.SmartField2();
    lookupRow = new scout.LookupRow(123, 'Foo');
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
  });

  function createFieldWithLookupCall() {
    return scout.create('SmartField2', {
      parent: session.desktop,
      lookupCall: 'DummyLookupCall'
    });
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

    it('when setValue is called, load the correct lookup row', function() {
      field = createFieldWithLookupCall();
      field.setValue(1);
      jasmine.clock().tick(300);
      expect(field.displayText).toBe('Foo');
      expect(field.value).toBe(1);

      // set the value to null again
      field.setValue(null);
      expect(field.lookupRow).toBe(null);
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('');

      field.setValue(2);
      jasmine.clock().tick(300);
      expect(field.displayText).toBe('Bar');
      expect(field.value).toBe(2);
    });

    it('load proposals for the current displayText', function() {
      field = createFieldWithLookupCall();
      field.render(session.desktop.$container);
      field.$field.val('b');
      field._onFieldKeyup({});
      expect(field.$field.hasClass('lookup-in-progress')).toBe(true);
      jasmine.clock().tick(300);
      // expect we have 2 table rows
      expect(field.popup).not.toBe(null);
      expect(findTableProposals()).toEqual(['Bar', 'Baz']);
    });

  });

  describe('default (smart field) with table proposal', function() {


  });

  describe('default (smart field) with tree proposal', function() {

  });

  describe('proposal field', function() {

    beforeEach(function() {
      field = scout.create('SmartField2', {
        parent: session.desktop,
        variant: 'proposal',
        lookupCall: 'DummyLookupCall'
      });
    });

    it('defaults', function() {
      expect(field.displayStyle).toBe('proposal');
    });

    /**
     * Proposal field acts as regular string field when setValue is called
     * No matter if the typed text exists as record in the lookup call, we
     * simply set value/display text to it.
     */
    it('setValue', function() {
      field.setValue('Foo');
      expect(field.value).toBe('Foo');
      expect(field.displayText).toBe('Foo');

      field.setValue('Xyz');
      expect(field.value).toBe('Xyz');
      expect(field.displayText).toBe('Xyz');
      expect(field.errorStatus).toBe(null);
    });

  });

});
