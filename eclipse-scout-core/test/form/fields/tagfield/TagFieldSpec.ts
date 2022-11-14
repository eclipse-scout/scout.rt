/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, keys, scout, StaticLookupCall, TagChooserPopup, TagField} from '../../../../src/index';
import {FormSpecHelper} from '../../../../src/testing/index';

describe('TagField', () => {

  let session: SandboxSession, field: TagField, helper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    field = new TagField();
    field.session = session;
    helper = new FormSpecHelper(session);
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
    removePopups(session);
  });

  class SpecTagFieldLookupCall extends StaticLookupCall<string> {

    constructor() {
      super();
      this.setDelay(250);
    }

    protected override _data(): any[] {
      return [
        ['1', 'Foo'],
        ['2', 'Bar', '1'],
        ['3', 'Baz', '1']
      ];
    }
  }


  function typeProposal(field, text, keyCode) {
    let $input = field.$container.find('input');
    $input.val(text);
    $input.focus();
    $input.trigger($.Event('keyup', {
      keyCode: keyCode
    }));
  }

  describe('model', () => {

    it('add tag', () => {
      field.setValue(['foo']);
      field.addTag('bar');
      expect(arrays.equals(['foo', 'bar'], field.value)).toBe(true);
    });

    it('remove tag', () => {
      field.setValue(['foo', 'bar']);
      field.removeTag('bar');
      expect(arrays.equals(['foo'], field.value)).toBe(true);
    });

  });

  describe('rendering', () => {

    it('should render tags (=value)', () => {
      field = scout.create(TagField, {
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
      let $res = field.$container.find('.tag-element');
      expect($res.length).toBe(2);
      expect($res.eq(0).text()).toBe('bar');
      expect($res.eq(1).text()).toBe('baz');
    });

  });

  /**
   * Ticket #230409
   */
  describe('key-strokes', () => {

    it('ENTER', () => {
      field = scout.create(TagField, {
        parent: session.desktop,
        lookupCall: {
          objectType: SpecTagFieldLookupCall
        }
      });

      // type a proposal that yields exactly 1 result, but do NOT
      // select the returned lookup row
      field.render();
      typeProposal(field, 'fo', keys.O);
      jasmine.clock().tick(500);

      expect(field.popup instanceof TagChooserPopup).toBe(true);

      // trigger a keydown event, all the flags are required  to pass
      // the accept-checks in KeyStroke.js
      let $input = field.$container.find('input');
      $input.trigger($.Event('keydown', {
        keyCode: keys.ENTER,
        which: keys.ENTER,
        altKey: false,
        shiftKey: false,
        ctrlKey: false,
        metaKey: false
      }));

      // expect the value to be accepted and the chooser to be closed
      expect(field.popup).toBe(null);
      expect(field.value).toEqual(['fo']);
    });

  });

  describe('tag lookup', () => {

    it('start and prepare a lookup call clone when typing', () => {
      let templatePropertyValue = 11;
      let eventCounter = 0;
      field = scout.create(TagField, {
        parent: session.desktop,
        lookupCall: {
          objectType: SpecTagFieldLookupCall,
          customProperty: templatePropertyValue
        }
      });
      field.on('prepareLookupCall', event => {
        expect(event.lookupCall['customProperty']).toBe(templatePropertyValue);
        expect(event.lookupCall.id).not.toBe(field.lookupCall.id);
        expect(event.type).toBe('prepareLookupCall');
        expect(event.source).toBe(field);

        eventCounter++;
      });

      expect(field.lookupCall instanceof SpecTagFieldLookupCall).toBe(true);

      field.render();
      typeProposal(field, 'ba', keys.A);
      jasmine.clock().tick(500);

      // expect popup is open and has 2 lookup rows (Bar, Baz)
      expect(field.popup instanceof TagChooserPopup).toBe(true);
      expect(field.popup.table.rows.length).toBe(2);
      expect(eventCounter).toBe(1);
    });

  });

});
