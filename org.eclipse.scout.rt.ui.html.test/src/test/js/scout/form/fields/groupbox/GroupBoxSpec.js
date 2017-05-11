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

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
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

  describe("_render", function() {
    var groupBox, model = {
      id: '2',
      label: "fooBar",
      gridData: {
        x: 0,
        y: 0
      }
    };

    beforeEach(function() {
      groupBox = createField(model);
    });

    it("adds group-box div when label is set", function() {
      groupBox.render($('#sandbox'));
      expect($('#sandbox')).toContainElement('div.group-box');
      expect($('#sandbox')).toContainElement('div.group-box-title');
    });
  });

  describe('test predefined height and width in pixel', function() {
    var form, formAdapter, formController, rootGroupBox, model;

    beforeEach(function() {
      model = $.extend(createSimpleModel('GroupBox', session), {
        id: '3',
        label: "fooBar",
        gridData: {
          x: 0,
          y: 0,
          widthInPixel: 97,
          heightInPixel: 123
        },
        mainBox: true
      });
      rootGroupBox = scout.create('GroupBox', model);
      form = scout.create('Form', {
        parent: session.desktop,
        rootGroupBox: rootGroupBox
      });
      session.desktop.$container = $('#sandbox');
    });

    it('adds group-box div when label is set', function() {
      var $tmpStyle = $('<style type="text/css">.dialog { position: absolute; }</style>')
        .appendTo($('head'));
      session.desktop.formController._renderDialog(form);
      expect(form.rootGroupBox.$container.cssHeight()).toBe(123);
      expect(form.rootGroupBox.$container.cssWidth()).toBe(97);
      $tmpStyle.remove();
    });

  });

  describe('focus', function() {
    it('focus first focusable field in groupBox', function() {
      var groupBox = helper.createGroupBoxWithOneField(session.desktop);
      groupBox.render(session.$entryPoint);
      expect(scout.focusUtils.isActiveElement(groupBox.fields[0].$field[0])).toBe(false);
      groupBox.focus();
      expect(scout.focusUtils.isActiveElement(groupBox.fields[0].$field[0])).toBe(true);
    });
  });

  describe('default values', function() {

    it('gridDataHints', function() {
      var groupBox = helper.createGroupBoxWithOneField(session.desktop);
      var gdh = groupBox.gridDataHints;
      expect(gdh.useUiHeight).toBe(true);
      expect(gdh.w).toBe(scout.FormField.FULL_WIDTH);
    });

  });

  describe('enabled', function() {
    it('propagation', function() {
      var groupBoxWithTwoChildren = helper.createGroupBoxWithFields(session.desktop, false, 2);
      groupBoxWithTwoChildren.render(session.$entryPoint);

      expectEnabled(groupBoxWithTwoChildren, true, true);
      expectEnabled(groupBoxWithTwoChildren.getFields()[0], true, true);
      expectEnabled(groupBoxWithTwoChildren.getFields()[1], true, true);

      groupBoxWithTwoChildren.setEnabled(false);
      expectEnabled(groupBoxWithTwoChildren, false, false, 'disabled');
      expectEnabled(groupBoxWithTwoChildren.getFields()[0], true, false, 'disabled');
      expectEnabled(groupBoxWithTwoChildren.getFields()[1], true, false, 'disabled');

      groupBoxWithTwoChildren.setEnabled(false, true, true);
      expectEnabled(groupBoxWithTwoChildren, false, false, 'disabled');
      expectEnabled(groupBoxWithTwoChildren.getFields()[0], false, false, 'disabled');
      expectEnabled(groupBoxWithTwoChildren.getFields()[1], false, false, 'disabled');

      groupBoxWithTwoChildren.getFields()[0].setEnabled(true, true, true);
      expectEnabled(groupBoxWithTwoChildren, true, true);
      expectEnabled(groupBoxWithTwoChildren.getFields()[0], true, true);
      expectEnabled(groupBoxWithTwoChildren.getFields()[1], false, false);
    });
  });

});
