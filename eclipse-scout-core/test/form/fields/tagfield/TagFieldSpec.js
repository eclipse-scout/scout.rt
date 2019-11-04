/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, keys, scout, TagChooserPopup, TagField} from '../../../../src/index';
import {DummyLookupCall, FormSpecHelper} from '@eclipse-scout/testing';

describe('TagField', function() {

  var session, field, lookupRow, helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    field = new TagField();
    field.session = session;
    lookupRow = scout.create('LookupRow', {
      key: 123,
      data: 'Foo'
    });
    helper = new FormSpecHelper(session);
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
    removePopups(session);
  });

  function typeProposal(field, text, keyCode) {
    var $input = field.$container.find('input');
    $input.val(text);
    $input.focus();
    // eslint-disable-next-line new-cap
    $input.trigger(jQuery.Event('keyup', {
      keyCode: keyCode
    }));
  }

  describe('model', function() {

    it('add tag', function() {
      field.setValue(['foo']);
      field.addTag('bar');
      expect(arrays.equals(['foo', 'bar'], field.value)).toBe(true);
    });

    it('remove tag', function() {
      field.setValue(['foo', 'bar']);
      field.removeTag('bar');
      expect(arrays.equals(['foo'], field.value)).toBe(true);
    });

  });

  describe('rendering', function() {

    it('should render tags (=value)', function() {
      field = scout.create('TagField', {
        parent: session.desktop
      });

      field.setValue(['foo', 'bar']);
      field.render();
      expect(field.$container.find('.tag-element').length).toBe(2);

      // remove a tag
      field.removeTag('foo');
      expect(field.$container.find('.tag-element').length).toBe(1);

      // add a tag
      field.addTag('baz');
      var $res = field.$container.find('.tag-element');
      expect($res.length).toBe(2);
      expect($res.eq(0).text()).toBe('bar');
      expect($res.eq(1).text()).toBe('baz');
    });

  });

  /**
   * Ticket #230409
   */
  describe('key-strokes', function() {

    it('ENTER', function() {
      field = scout.create('TagField', {
        parent: session.desktop,
        lookupCall: {
          objectType: 'DummyLookupCall'
        }
      });

      // type a proposal that yields exactly 1 result, but do NOT
      // select the returned lookup row
      field.render();
      typeProposal(field, 'fo', keys.O);
      jasmine.clock().tick(500);

      expect(field.chooser instanceof TagChooserPopup).toBe(true);

      // trigger a keydown event, all the flags are required  to pass
      // the accept-checks in KeyStroke.js
      var $input = field.$container.find('input');
      // eslint-disable-next-line new-cap
      $input.trigger(jQuery.Event('keydown', {
        keyCode: keys.ENTER,
        which: keys.ENTER,
        altKey: false,
        shiftKey: false,
        ctrlKey: false,
        metaKey: false
      }));

      // expect the value to be accepted and the chooser to be closed
      expect(field.chooser).toBe(null);
      expect(field.value).toEqual(['fo']);
    });

  });

  describe('tag lookup', function() {

    it('start and prepare a lookup call clone when typing', function() {
      var templatePropertyValue = 11;
      var preparedPropertyValue = 22;
      var eventCounter = 0;
      field = scout.create('TagField', {
        parent: session.desktop,
        lookupCall: {
          objectType: 'DummyLookupCall',
          customProperty: templatePropertyValue
        }
      });
      field.on('prepareLookupCall', function(event) {
        expect(event.lookupCall.customProperty).toBe(templatePropertyValue);
        expect(event.lookupCall.id).not.toBe(field.lookupCall.id);
        expect(event.type).toBe('prepareLookupCall');
        expect(event.source).toBe(field);

        eventCounter++;
      });

      expect(field.lookupCall instanceof DummyLookupCall).toBe(true);

      field.render();
      typeProposal(field, 'ba', keys.A);
      jasmine.clock().tick(500);

      // expect popup is open and has 2 lookup rows (Bar, Baz)
      expect(field.chooser instanceof TagChooserPopup).toBe(true);
      expect(field.chooser.table.rows.length).toBe(2);
      expect(eventCounter).toBe(1);
    });

  });

});
