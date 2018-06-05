/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('TagField', function() {

  var session, field, lookupRow, helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    field = new scout.TagField();
    lookupRow = scout.create('LookupRow', {
      key: 123,
      data: 'Foo'
    });
    helper = new scout.FormSpecHelper(session);
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
    removePopups(session);
  });

  describe('model', function() {

    it('add tag', function() {
      field.setValue(['foo']);
      field.addTag('bar');
      expect(scout.arrays.equals(['foo', 'bar'], field.value)).toBe(true);
    });

    it('remove tag', function() {
      field.setValue(['foo', 'bar']);
      field.removeTag('bar');
      expect(scout.arrays.equals(['foo'], field.value)).toBe(true);
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

      expect(field.lookupCall instanceof scout.DummyLookupCall).toBe(true);

      field.render();
      var $input = field.$container.find('input');
      $input.val('ba');
      $input.focus();
      $input.trigger(jQuery.Event('keyup', {
        keyCode: scout.keys.A
      }));

      jasmine.clock().tick(500);

      // expect popup is open and has 2 lookup rows (Bar, Baz)
      expect(field.chooser instanceof scout.TagChooserPopup).toBe(true);
      expect(field.chooser.table.rows.length).toBe(2);
      expect(eventCounter).toBe(1);
    });

  });

});
