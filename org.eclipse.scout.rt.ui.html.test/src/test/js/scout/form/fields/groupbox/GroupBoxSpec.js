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

    it("renders controls initially if expanded", function () {
      var groupBox = helper.createGroupBoxWithOneField(session.desktop);
      spyOn(groupBox, '_renderControls');
      groupBox.render();
      expect(groupBox._renderControls.calls.count()).toEqual(1);
    });

    it("does not render controls initially if collapsed, but on expand", function () {
      var groupBox = helper.createGroupBoxWithOneField(session.desktop);
      spyOn(groupBox, '_renderControls');
      groupBox.setExpanded(false);
      groupBox.render();
      expect(groupBox._renderControls.calls.count()).toEqual(0);
      groupBox.setExpanded(true);
      expect(groupBox._renderControls.calls.count()).toEqual(1);
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
          widthInPixel: 27,
          heightInPixel: 46
        }
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
      expect(form.rootGroupBox.$container.cssHeight()).toBe(46);
      expect(form.rootGroupBox.$container.cssWidth()).toBe(27);
      $tmpStyle.remove();
    });

  });

  describe('focus', function() {
    it('focus first focusable field in groupBox', function() {
      var groupBox = helper.createGroupBoxWithOneField(session.desktop);
      groupBox.render();
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
      var groupBoxWithTwoChildren = helper.createGroupBoxWithFields(session.desktop, 2);
      groupBoxWithTwoChildren.render();

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

  describe('logical grid', function() {
    it('is validated automatically by the logical grid layout', function() {
      var groupBox = scout.create('GroupBox', {
        parent: session.desktop,
        gridColumnCount: 2,
        fields: [{
          objectType: 'StringField'
        },
        {
          objectType: 'StringField'
        },
        {
          objectType: 'StringField'
        }]
      });
      groupBox.render();
      expect(groupBox.fields[0].gridData.x).toBe(-1);
      expect(groupBox.fields[0].gridData.y).toBe(-1);
      expect(groupBox.fields[1].gridData.x).toBe(-1);
      expect(groupBox.fields[1].gridData.y).toBe(-1);
      expect(groupBox.fields[2].gridData.x).toBe(-1);
      expect(groupBox.fields[2].gridData.y).toBe(-1);

      // Logical grid will be validated along with the layout
      groupBox.revalidateLayout();
      expect(groupBox.fields[0].gridData.x).toBe(0);
      expect(groupBox.fields[0].gridData.y).toBe(0);
      expect(groupBox.fields[1].gridData.x).toBe(0);
      expect(groupBox.fields[1].gridData.y).toBe(1);
      expect(groupBox.fields[2].gridData.x).toBe(1);
      expect(groupBox.fields[2].gridData.y).toBe(0);
    });

    it('will get dirty if a field gets invisible', function() {
      var groupBox = scout.create('GroupBox', {
        parent: session.desktop,
        gridColumnCount: 2,
        fields: [{
          objectType: 'StringField'
        },
        {
          objectType: 'StringField'
        },
        {
          objectType: 'StringField'
        }]
      });
      groupBox.render();
      groupBox.revalidateLayout();

      groupBox.fields[2].setVisible(false);
      expect(groupBox.logicalGrid.dirty).toBe(true);

      groupBox.revalidateLayout();
      expect(groupBox.fields[0].gridData.x).toBe(0);
      expect(groupBox.fields[0].gridData.y).toBe(0);
      expect(groupBox.fields[1].gridData.x).toBe(1);
      expect(groupBox.fields[1].gridData.y).toBe(0);
    });

    it('may be specified using the object type', function() {
      var groupBox = scout.create('GroupBox', {
        parent: session.desktop,
        logicalGrid: 'HorizontalGroupBoxBodyGrid'
      });
      expect(groupBox.logicalGrid instanceof scout.HorizontalGroupBoxBodyGrid).toBe(true);

      groupBox = scout.create('GroupBox', {
        parent: session.desktop,
        logicalGrid: 'VerticalSmartGroupBoxBodyGrid'
      });
      expect(groupBox.logicalGrid instanceof scout.VerticalSmartGroupBoxBodyGrid).toBe(true);

      groupBox = scout.create('GroupBox', {
        parent: session.desktop,
        logicalGrid: scout.create('HorizontalGroupBoxBodyGrid')
      });
      expect(groupBox.logicalGrid instanceof scout.HorizontalGroupBoxBodyGrid).toBe(true);
    });
  });

});
