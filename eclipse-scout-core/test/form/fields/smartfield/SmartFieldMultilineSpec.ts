/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, QueryBy, scout, SmartFieldModel, SmartFieldMultiline} from '../../../../src/index';
import {SmartFieldLookupResult} from '../../../../src/form/fields/smartfield/SmartField';
import {InitModelOf} from '../../../../src/scout';
import {JQueryTesting} from '../../../../src/testing';

describe('SmartFieldMultiline', () => {

  let session: SandboxSession, field: SpecSmartFieldMultiline;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  class SpecSmartFieldMultiline extends SmartFieldMultiline<number> {
    declare _$multilineLines: JQuery;

    override _acceptByTextDone(result: SmartFieldLookupResult<number>) {
      super._acceptByTextDone(result);
    }
  }

  function createFieldWithLookupCall(model: SmartFieldModel<number>): SpecSmartFieldMultiline {
    model = $.extend({}, {
      parent: session.desktop,
      lookupCall: 'DummyLookupCall'
    }, model);
    return scout.create(SpecSmartFieldMultiline, model as InitModelOf<SpecSmartFieldMultiline>);
  }

  describe('display text', () => {

    beforeEach(() => {
      field = createFieldWithLookupCall({
        displayText: 'Foo\nBar'
      });
      field.render();
    });

    it('show first line as INPUT value, additional lines in separate DIV', () => {
      expect(field.$field.val()).toBe('Foo');
      expect(field._$multilineLines.text()).toBe('Bar');
    });

    it('reset multiline-lines DIV on error', () => {
      field._acceptByTextDone({
        queryBy: QueryBy.TEXT,
        lookupRows: [],
        text: 'Xxx'
      });
      expect(field.$field.val()).toBe('Xxx');
      expect(field._$multilineLines.text()).toBe('');
    });

  });

  describe('aria properties', () => {

    beforeEach(() => {
      field = createFieldWithLookupCall({
        displayText: 'Foo\nBar'
      });
    });

    it('has aria role combobox', () => {
      field.render();
      expect(field.$field).toHaveAttr('role', 'combobox');
    });

    it('has aria-describedby description for its functionality', () => {
      field.render();
      let $fieldDescription = field.$container.find('#desc' + field.id + '-func-desc');
      expect(field.$field.attr('aria-describedby')).toBeTruthy();
      expect(field.$field.attr('aria-describedby')).toBe($fieldDescription.eq(0).attr('id'));
      expect(field.$field.attr('aria-description')).toBeFalsy();
    });

    it('has a non empty status container that lists count of available options', () => {
      field.render();
      field.$field.focus(); // must be focused, otherwise popup will not open
      // @ts-expect-error
      field._onFieldKeyUp({});
      jasmine.clock().tick(500);
      expect(field.$screenReaderStatus).toHaveAttr('role', 'status');
      expect(field.$screenReaderStatus).toHaveClass('sr-only');
      expect(field.$screenReaderStatus.children('.sr-lookup-row-count').length).toBe(1);
      expect(field.$screenReaderStatus.children('.sr-lookup-row-count').eq(0)).not.toBeEmpty();
    });

    it('has a aria-expanded set correctly if pop up is open/closed', () => {
      field.render();
      expect(field.$field).toHaveAttr('aria-expanded', 'false');
      field.$field.focus(); // must be focused, otherwise popup will not open
      // @ts-expect-error
      field._onFieldKeyUp({});
      jasmine.clock().tick(500);
      expect(field.$field).toHaveAttr('aria-expanded', 'true');
      field.closePopup();
    });

    it('has a aria-controls set correctly if pop up is open/closed', () => {

      field.render();
      expect(field.$field.attr('aria-controls')).toBeFalsy();
      field.$field.focus(); // must be focused, otherwise popup will not open
      // @ts-expect-error
      field._onFieldKeyUp({});
      jasmine.clock().tick(500);
      expect(field.$field.attr('aria-controls')).toBe(field.popup.$container.attr('id'));
      field.closePopup();
    });

    it('has a aria-activedescendant set correctly if pop up is open/closed', () => {
      field.render();
      expect(field.$field.attr('aria-activedescendant')).toBeFalsy();
      field.$field.focus(); // must be focused, otherwise popup will not open
      // @ts-expect-error
      field._onFieldKeyUp({});
      jasmine.clock().tick(500);
      JQueryTesting.triggerKeyDown(field.$field, keys.DOWN);
      expect(field.$field.attr('aria-activedescendant')).toBeTruthy();
      field.closePopup();
    });
  });
});
