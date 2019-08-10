/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
describe("GroupBox", function() {
  var session;
  var helper;
  var cloneHelper;

  beforeEach(function() {
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

    it("considers the clone properties and deep clones fields and menus", function() {
      var groupBox = scout.create('GroupBox', {
        parent: session.desktop,
        id: 'gb01',
        subLabel: 'abc',
        gridColumnCount: 3,
        logicalGrid: 'HorizontalGrid',
        fields: [{
          objectType: 'StringField'
        }, {
          objectType: 'SmartField',
          label: "a label"
        }, {
          objectType: 'DateField'
        }],
        menus: [{
          objectType: 'Menu'
        }]
      });
      var clone = groupBox.clone({
        parent: groupBox.parent
      });

      cloneHelper.validateClone(groupBox, clone);
      expect(clone.fields.length).toBe(3);
      expect(clone.menus.length).toBe(1);
      expect(clone.cloneOf).toBe(groupBox);
      expect(clone.gridColumnCount).toBe(3);
      expect(clone.fields[0].cloneOf).toBe(groupBox.fields[0]);
      expect(clone.fields[1].cloneOf).toBe(groupBox.fields[1]);
      expect(clone.fields[1].label).toBe('a label');
      expect(clone.menus[0].cloneOf).toBe(groupBox.menus[0]);

      // Assert that logical grid is a new instance
      expect(clone.logicalGrid).not.toBe(groupBox.logicalGrid);
      expect(clone.logicalGrid instanceof scout.HorizontalGrid).toBe(true);
      expect(clone.logicalGrid.gridConfig instanceof scout.GroupBoxGridConfig).toBe(true);
    });

    it("does not render the cloned box", function() {
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
