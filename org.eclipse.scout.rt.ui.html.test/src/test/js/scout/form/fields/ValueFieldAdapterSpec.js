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
describe('ValueFieldAdapter', function() {
  var session, helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
  });

  describe('_createPropertySortFunc', function() {

    it('should order properties', function() {
      var order = ['foo', 'baz', 'bar'];
      var properties = ['x', 'bar', 'foo', 'a', 'y', 'baz'];
      var adapter = new scout.ValueFieldAdapter();
      var sortFunc = adapter._createPropertySortFunc(order);
      properties.sort(sortFunc);
      var expected = ['foo', 'baz', 'bar', 'a', 'x', 'y'];
      expect(properties).toEqual(expected);
    });
  });

});
