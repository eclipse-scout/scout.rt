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
describe('TabItem', function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
  });

  function createTabBox(tabItems) {
    var model = helper.createFieldModel('TabBox');

    // Form is necessary to make keystrokes work
    var form = helper.createFormWithOneField();
    form.render(session.$entryPoint);

    model.tabItems = [];
    for (var i=0; i < tabItems.length; i++) {
      model.tabItems.push(tabItems[i].id);
    }
    model.selectedTab = 0;
    model.owner = form.id;
    model.parent = form;
    return createAdapter(model, session, tabItems);
  }

  describe('_renderStatusVisible', function() {
    var field;

    beforeEach(function() {
      var groupBox = helper.createFieldModel('TabItem');
      groupBox.label = 'Foo';
      field = createTabBox([groupBox]);
    });

    it('invalidates tabarea if status visibility changes', function() {
      field.render(session.$entryPoint);
      field.validateLayout();
      expect(scout.HtmlComponent.get(field._$tabArea).valid).toBe(true);
      expect(field.tabItems[0]._computeStatusVisible()).toBe(false);

      // TabArea needs to be invalidated, it may necessary to show ellipsis now because status got visible
      field.tabItems[0].setTooltipText('test');
      expect(field.tabItems[0]._computeStatusVisible()).toBe(true);
      expect(scout.HtmlComponent.get(field._$tabArea).valid).toBe(false);
    });

  });

  describe('_renderCssClass', function() {
    var groupBox;

    beforeEach(function() {
      groupBox = helper.createFieldModel('TabItem');
      groupBox.cssClass = 'foo1';
      var field = createTabBox([groupBox]);
      field.render(session.$entryPoint);
      groupBox = field.tabItems[0];
    });

    it('adds CSS class to both, TabItem and GroupBox', function() {
      // Test initial CSS class
      expect(groupBox.$tabContainer.hasClass('foo1')).toBe(true);
      expect(groupBox.$container.hasClass('foo1')).toBe(true);

      // Test adding a CSS class
      groupBox.onModelPropertyChange({
        type:'property',
        properties: {
          cssClass: 'foo2'
        }
      });
      expect(groupBox.$tabContainer.hasClass('foo1')).toBe(false);
      expect(groupBox.$container.hasClass('foo1')).toBe(false);
      expect(groupBox.$tabContainer.hasClass('foo2')).toBe(true);
      expect(groupBox.$container.hasClass('foo2')).toBe(true);

      // Test adding another CSS class
      groupBox.onModelPropertyChange({
        type:'property',
        properties: {
          cssClass: 'foo3'
        }
      });
      expect(groupBox.$tabContainer.hasClass('foo2')).toBe(false);
      expect(groupBox.$container.hasClass('foo2')).toBe(false);
      expect(groupBox.$tabContainer.hasClass('foo3')).toBe(true);
      expect(groupBox.$container.hasClass('foo3')).toBe(true);
    });
  });
});
