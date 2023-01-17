/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {QueryBy, scout, SmartFieldModel, SmartFieldMultiline} from '../../../../src/index';
import {SmartFieldLookupResult} from '../../../../src/form/fields/smartfield/SmartField';
import {InitModelOf} from '../../../../src/scout';

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

});
