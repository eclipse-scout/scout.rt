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
describe("GroupBox", function() {
  var session;
  var helper;
  var cloneHelper;

  beforeEach(function() {
    jasmine.addMatchers(scout.CloneSpecHelper.CUSTOM_MATCHER);
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
    cloneHelper = new scout.CloneSpecHelper();
  });

  function createField(model, parent) {
    var field = new scout.GroupBox();
    model.session = session;
    model.parent = parent || session.desktop;
    field.init(model);
    return field;
  }

  function expectEnabled(field, expectedEnabled, expectedEnabledComputed, hasClass) {
    expect(field.enabled).toBe(expectedEnabled);
    expect(field.enabledComputed).toBe(expectedEnabledComputed);
    if (hasClass) {
      if (field.$field) {
        expect(field.$field).toHaveClass(hasClass);
      } else {
        expect(field.$container).toHaveClass(hasClass);
      }
    }
  }

  describe("clone", function() {
    it("rendered", function() {
      var clone,
        groupBox = scout.create('GroupBox', {
          parent: session.desktop,
          id: 'gb01',
          subLabel: 'abc',
          gridColumnCount: 2,
          fields: [{
            objectType: 'StringField'
          }, {
            objectType: 'SmartField'
          }, {
            objectType: 'DateField'
          }],
          menus: [{
            objectType: 'Menu'
          }]
        });
      groupBox.render($('#sandbox'));
      clone = groupBox.clone({
        parent: groupBox.parent
      });

      expect(groupBox.rendered).toBe(true);
      expect(clone.rendered).toBe(false);

      cloneHelper.validateClone(groupBox, clone);
      expect(clone.controls.length).toBe(3);
      expect(clone.menus.length).toBe(1);
    });
  });

});
