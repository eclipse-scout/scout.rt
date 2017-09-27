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
describe('SmartFieldMultiline', function() {

  var session, field;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
  });

  function createFieldWithLookupCall(model) {
    model = $.extend({}, {
      parent: session.desktop,
      lookupCall: 'DummyLookupCall'
    }, model);
    return scout.create('SmartFieldMultiline', model);
  }

  describe('display text', function() {

    beforeEach(function() {
      field = createFieldWithLookupCall({
        displayText: 'Foo\nBar'
      });
      field.render();
    });

    it('show first line as INPUT value, additional lines in separate DIV', function() {
      expect(field.$field.val()).toBe('Foo');
      expect(field._$multilineLines.text()).toBe('Bar');
    });

    it('reset multiline-lines DIV on error', function() {
      field._acceptByTextDone({
        queryBy: scout.QueryBy.TEXT,
        lookupRows: [],
        text: 'Xxx'
      });
      expect(field.$field.val()).toBe('Xxx');
      expect(field._$multilineLines.text()).toBe('');
    });

  });

});
