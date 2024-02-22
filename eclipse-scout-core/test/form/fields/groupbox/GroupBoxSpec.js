/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {DialogLayout, Dimension, FormField, GroupBox, HorizontalGrid, scout, VerticalSmartGrid} from '../../../../src/index';
import {FormSpecHelper} from '../../../../src/testing/index';

describe('GroupBox', () => {
  let session;
  let helper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
  });

  function createField(model, parent) {
    let field = new GroupBox();
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

  describe('_render', () => {

    it('adds group-box div when label is set', () => {
      let model = {
        id: '2',
        label: 'fooBar',
        gridDataHints: {
          x: 0,
          y: 0
        }
      };
      let groupBox = createField(model);
      groupBox.render($('#sandbox'));
      expect($('#sandbox')).toContainElement('div.group-box');
      expect($('#sandbox')).toContainElement('div.group-box-header');
    });

    it('renders controls initially if expanded', () => {
      let groupBox = helper.createGroupBoxWithOneField(session.desktop);
      spyOn(groupBox, '_renderControls');
      groupBox.render();
      expect(groupBox._renderControls.calls.count()).toEqual(1);
    });

    it('does not render controls initially if collapsed, but on expand', () => {
      let groupBox = helper.createGroupBoxWithOneField(session.desktop);
      spyOn(groupBox, '_renderControls');
      groupBox.setExpanded(false);
      groupBox.render();
      expect(groupBox._renderControls.calls.count()).toEqual(0);
      groupBox.setExpanded(true);
      expect(groupBox._renderControls.calls.count()).toEqual(1);
    });

    it('automatically hides the label if it is empty', () => {
      // Test 1: render first
      let groupBox = createField({});
      groupBox.render();

      expect(groupBox.labelVisible).toBe(true);
      expect(groupBox._computeTitleVisible()).toBe(false);
      expect(groupBox.$header.isVisible()).toBe(false);
      groupBox.setLabel('test');
      expect(groupBox.labelVisible).toBe(true);
      expect(groupBox._computeTitleVisible()).toBe(true);
      expect(groupBox.$header.isVisible()).toBe(true);
      expect(groupBox.$title.text().trim()).toBe('test');
      groupBox.setLabelVisible(false);
      expect(groupBox.labelVisible).toBe(false);
      expect(groupBox._computeTitleVisible()).toBe(false);
      expect(groupBox.$header.isVisible()).toBe(false);
      expect(groupBox.$title.text().trim()).toBe('test');

      // Test 2: render later
      let groupBox2 = createField({});
      expect(groupBox2.labelVisible).toBe(true);
      expect(groupBox2._computeTitleVisible()).toBe(false);
      groupBox2.setLabel('test2');
      expect(groupBox2.labelVisible).toBe(true);
      expect(groupBox2._computeTitleVisible()).toBe(true);
      groupBox2.render();
      expect(groupBox2.$header.isVisible()).toBe(true);
      expect(groupBox2.$title.text().trim()).toBe('test2');

      // Cleanup
      groupBox.destroy();
      groupBox2.destroy();
    });
  });

  describe('focus', () => {
    it('focuses the first field', () => {
      let box = scout.create('GroupBox', {
        parent: session.desktop,
        fields: [{
          objectType: 'StringField'
        }, {
          objectType: 'StringField'
        }]
      });
      box.render();
      expect(box.fields[0].$field).not.toBeFocused();

      box.focus();
      expect(box.fields[0].$field).toBeFocused();
    });

    it('focuses the second field if the first is disabled', () => {
      let box = scout.create('GroupBox', {
        parent: session.desktop,
        fields: [{
          objectType: 'StringField',
          enabled: false
        }, {
          objectType: 'StringField',
          enabled: true
        }]
      });
      box.render();
      expect(box.fields[1].$field).not.toBeFocused();

      box.focus();
      expect(box.fields[1].$field).toBeFocused();
    });

    it('focuses the second field if the first not focusable', () => {
      let box = scout.create('GroupBox', {
        parent: session.desktop,
        fields: [{
          objectType: 'LabelField'
        }, {
          objectType: 'StringField'
        }]
      });
      box.render();
      expect(box.fields[1].$field).not.toBeFocused();

      box.focus();
      expect(box.fields[1].$field).toBeFocused();
    });

    it('considers child group boxes', () => {
      let box = scout.create('GroupBox', {
        parent: session.desktop,
        fields: [{
          objectType: 'GroupBox',
          fields: [{
            objectType: 'LabelField'
          }, {
            objectType: 'StringField'
          }]
        }]
      });
      box.render();
      expect(box.fields[0].fields[1].$field).not.toBeFocused();

      box.focus();
      expect(box.fields[0].fields[1].$field).toBeFocused();
    });
  });

  describe('default values', () => {

    it('gridDataHints', () => {
      let groupBox = helper.createGroupBoxWithOneField(session.desktop);
      let gdh = groupBox.gridDataHints;
      expect(gdh.useUiHeight).toBe(true);
      expect(gdh.w).toBe(FormField.FULL_WIDTH);
    });

  });

  describe('enabled', () => {
    it('is not propagated to children by default', () => {
      let groupBoxWithTwoChildren = helper.createGroupBoxWithFields(session.desktop, 2);
      groupBoxWithTwoChildren.render();

      expectEnabled(groupBoxWithTwoChildren, true, true);
      expectEnabled(groupBoxWithTwoChildren.getFields()[0], true, true);
      expectEnabled(groupBoxWithTwoChildren.getFields()[1], true, true);

      groupBoxWithTwoChildren.setEnabled(false);
      expectEnabled(groupBoxWithTwoChildren, false, false, 'disabled');
      expectEnabled(groupBoxWithTwoChildren.getFields()[0], true, false, 'disabled');
      expectEnabled(groupBoxWithTwoChildren.getFields()[1], true, false, 'disabled');
    });

    it('but maybe propagated to children if required', () => {
      let groupBoxWithTwoChildren = helper.createGroupBoxWithFields(session.desktop, 2);
      groupBoxWithTwoChildren.render();

      expectEnabled(groupBoxWithTwoChildren, true, true);
      expectEnabled(groupBoxWithTwoChildren.getFields()[0], true, true);
      expectEnabled(groupBoxWithTwoChildren.getFields()[1], true, true);

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

  describe('logical grid', () => {
    it('is validated automatically by the logical grid layout', () => {
      let groupBox = scout.create('GroupBox', {
        parent: session.desktop,
        gridColumnCount: 2,
        fields: [
          {
            objectType: 'StringField'
          },
          {
            objectType: 'StringField'
          },
          {
            objectType: 'StringField'
          }
        ]
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

    it('will get dirty if a field gets invisible', () => {
      let groupBox = scout.create('GroupBox', {
        parent: session.desktop,
        gridColumnCount: 2,
        fields: [
          {
            objectType: 'StringField'
          },
          {
            objectType: 'StringField'
          },
          {
            objectType: 'StringField'
          }
        ]
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

    it('may be specified using the object type', () => {
      let groupBox = scout.create('GroupBox', {
        parent: session.desktop,
        logicalGrid: 'HorizontalGrid'
      });
      expect(groupBox.logicalGrid instanceof HorizontalGrid).toBe(true);

      groupBox = scout.create('GroupBox', {
        parent: session.desktop,
        logicalGrid: 'VerticalSmartGrid'
      });
      expect(groupBox.logicalGrid instanceof VerticalSmartGrid).toBe(true);

      groupBox = scout.create('GroupBox', {
        parent: session.desktop,
        logicalGrid: scout.create('HorizontalGrid')
      });
      expect(groupBox.logicalGrid instanceof HorizontalGrid).toBe(true);
    });

    it('uses widthInPixel and heightInPixel as dialog width and height if set on main box', done => {
      let $tmpStyle = $('<style>.dialog { position: absolute; }</style>')
        .appendTo($('head'));

      // stub function because when running in phantom js the window has an unpredictable size, it seems to get smaller when adding new specs...
      spyOn(DialogLayout, 'fitContainerInWindow').and.callFake((windowSize, containerPosition, containerSize, containerMargins) => {
        return containerSize;
      });

      let form = scout.create('Form', {
        parent: session.desktop,
        rootGroupBox: {
          objectType: 'GroupBox',
          gridDataHints: {
            widthInPixel: 27,
            heightInPixel: 30
          }
        }
      });
      form.open()
        .then(() => {
          expect(form.rootGroupBox.$container.cssHeight()).toBe(30);
          expect(form.rootGroupBox.$container.cssWidth()).toBe(27);
          form.close();
          $tmpStyle.remove();
        })
        .catch(fail)
        .always(done);
    });
  });

  describe('notification', () => {

    it('validates the notification layout on size change', () => {
      let groupBox = helper.createGroupBoxWithFields(session.desktop, 2);
      groupBox.render();

      groupBox.htmlComp.setSize(new Dimension(500, 300));
      expect(groupBox.htmlComp.valid).toBe(true);
      expect(groupBox.htmlComp.size()).toEqual(new Dimension(500, 300));
      expect(groupBox.htmlBody.valid).toBe(true);
      expect(groupBox.htmlBody.size()).toEqual(new Dimension(500, 300));

      groupBox.setNotification(scout.create('Notification', {
        parent: groupBox,
        message: 'Lorem ipsum dolor sit amet, ipsum lorem dolor sit amet, sit lorem dolor ipsum amet, ipsum amet dolor sit lorem.'
      }));
      groupBox.revalidateLayout(); // trigger layout manually, because the group box is not inside a form

      let bodySize1 = groupBox.htmlBody.size();
      let notificationSize1 = groupBox.notification.htmlComp.size();
      expect(groupBox.htmlComp.valid).toBe(true);
      expect(groupBox.htmlComp.size()).toEqual(new Dimension(500, 300));
      expect(groupBox.htmlBody.valid).toBe(true);
      expect(bodySize1.width).toBe(500);
      expect(bodySize1.height).toBeLessThan(300);
      expect(groupBox.notification.htmlComp.valid).toBe(true);
      expect(notificationSize1.width).toBe(500);
      expect(notificationSize1.height).toBeLessThan(300);

      groupBox.htmlComp.setSize(new Dimension(200, 300));

      let bodySize2 = groupBox.htmlBody.size();
      let notificationSize2 = groupBox.notification.htmlComp.size();
      expect(groupBox.htmlComp.valid).toBe(true);
      expect(groupBox.htmlComp.size()).toEqual(new Dimension(200, 300));
      expect(groupBox.htmlBody.valid).toBe(true);
      expect(bodySize2.width).toBe(200);
      expect(bodySize2.height).toBeLessThan(300);
      expect(bodySize2.height).toBeLessThan(bodySize1.height);
      expect(groupBox.notification.htmlComp.valid).toBe(true);
      expect(notificationSize2.width).toBe(200);
      expect(notificationSize2.height).toBeLessThan(300);
      expect(notificationSize2.height).toBeGreaterThan(notificationSize1.height);
    });

    it('adjusts notification height when computing preferred size', () => {
      let groupBox = helper.createGroupBoxWithFields(session.desktop, 2);
      groupBox.setNotification(scout.create('Notification', {
        parent: groupBox,
        message: 'Lorem ipsum dolor sit amet, ipsum lorem dolor sit amet, sit lorem dolor ipsum amet, ipsum amet dolor sit lorem.'
      }));
      groupBox.gridData.widthInPixel = 700;
      groupBox.render();
      groupBox.pack();

      let size1 = groupBox.htmlComp.size();
      let bodySize1 = groupBox.htmlBody.size();

      groupBox.gridData.widthInPixel = 200;
      groupBox.invalidateLayout();
      groupBox.pack();

      let size2 = groupBox.htmlComp.size();
      let bodySize2 = groupBox.htmlBody.size();

      expect(size1.width).toBe(700);
      expect(size2.width).toBe(200);
      expect(bodySize1.width).toBe(700);
      expect(bodySize2.width).toBe(200);

      expect(size2.height).toBeGreaterThan(size1.height);
      expect(bodySize2.height).toBe(bodySize1.height);
    });
  });

  describe('scrollable', () => {
    it('null by default', () => {
      let groupBox = scout.create('GroupBox', {
        parent: session.desktop
      });
      expect(groupBox.scrollable).toBe(null);
    });

    it('is set to true if it is a mainbox', () => {
      let groupBox = scout.create('GroupBox', {
        parent: session.desktop,
        mainBox: true
      });
      expect(groupBox.scrollable).toBe(true);

      groupBox = scout.create('GroupBox', {
        parent: session.desktop
      });
      expect(groupBox.scrollable).toBe(null);

      groupBox.setMainBox(true);
      expect(groupBox.scrollable).toBe(true);
    });

    it('is not set to true if it is a mainbox but explicitly set to false', () => {
      let groupBox = scout.create('GroupBox', {
        parent: session.desktop,
        mainBox: true,
        scrollable: false
      });
      expect(groupBox.scrollable).toBe(false);

      groupBox = scout.create('GroupBox', {
        parent: session.desktop,
        mainBox: true
      });
      expect(groupBox.scrollable).toBe(true);

      groupBox.setScrollable(false);
      expect(groupBox.scrollable).toBe(false);

      groupBox = scout.create('GroupBox', {
        parent: session.desktop
      });
      expect(groupBox.scrollable).toBe(null);

      groupBox.setScrollable(false);
      expect(groupBox.scrollable).toBe(false);

      groupBox.setMainBox(true);
      expect(groupBox.scrollable).toBe(false);
    });
  });

  describe('insertField', () => {
    it('inserts the field at the given index', () => {
      let groupBox = helper.createGroupBoxWithFields(session.desktop, 2);
      expect(groupBox.fields.length).toBe(2);
      expect(groupBox.controls.length).toBe(2);

      let newField = scout.create('StringField', {parent: groupBox});
      groupBox.insertField(newField, 1);
      expect(groupBox.fields.length).toBe(3);
      expect(groupBox.controls.length).toBe(3);
      expect(groupBox.fields[1]).toBe(newField);
      expect(groupBox.controls[1]).toBe(newField);

      // At the beginning
      let newField2 = scout.create('StringField', {parent: groupBox});
      groupBox.insertField(newField2, 0);
      expect(groupBox.fields.length).toBe(4);
      expect(groupBox.controls.length).toBe(4);
      expect(groupBox.fields[0]).toBe(newField2);
      expect(groupBox.controls[0]).toBe(newField2);

      // At the end
      let newField3 = scout.create('StringField', {parent: groupBox});
      groupBox.insertField(newField3, 4);
      expect(groupBox.fields.length).toBe(5);
      expect(groupBox.controls.length).toBe(5);
      expect(groupBox.fields[4]).toBe(newField3);
      expect(groupBox.controls[4]).toBe(newField3);
    });

    it('inserts the field at the end if no index is provided', () => {
      let groupBox = helper.createGroupBoxWithFields(session.desktop, 2);
      expect(groupBox.fields.length).toBe(2);
      expect(groupBox.controls.length).toBe(2);

      let newField = scout.create('StringField', {parent: groupBox});
      groupBox.insertField(newField);
      expect(groupBox.fields.length).toBe(3);
      expect(groupBox.controls.length).toBe(3);
      expect(groupBox.fields[2]).toBe(newField);
      expect(groupBox.controls[2]).toBe(newField);
    });
  });

  describe('insertBefore', () => {
    it('inserts the field before the given other field', () => {
      let groupBox = helper.createGroupBoxWithFields(session.desktop, 2);
      expect(groupBox.fields.length).toBe(2);
      expect(groupBox.controls.length).toBe(2);

      let newField = scout.create('StringField', {parent: groupBox});
      let sibling = groupBox.fields[1];
      groupBox.insertFieldBefore(newField, sibling);
      expect(groupBox.fields.length).toBe(3);
      expect(groupBox.controls.length).toBe(3);
      expect(groupBox.fields[1]).toBe(newField);
      expect(groupBox.controls[1]).toBe(newField);
      expect(groupBox.fields[2]).toBe(sibling);
      expect(groupBox.controls[2]).toBe(sibling);

      // At the beginning
      let newField2 = scout.create('StringField', {parent: groupBox});
      sibling = groupBox.fields[0];
      groupBox.insertFieldBefore(newField2, sibling);
      expect(groupBox.fields.length).toBe(4);
      expect(groupBox.controls.length).toBe(4);
      expect(groupBox.fields[0]).toBe(newField2);
      expect(groupBox.controls[0]).toBe(newField2);
      expect(groupBox.fields[1]).toBe(sibling);
      expect(groupBox.controls[1]).toBe(sibling);
    });
  });

  describe('insertAfter', () => {
    it('inserts the field after the given other field', () => {
      let groupBox = helper.createGroupBoxWithFields(session.desktop, 2);
      expect(groupBox.fields.length).toBe(2);
      expect(groupBox.controls.length).toBe(2);

      let newField = scout.create('StringField', {parent: groupBox});
      let sibling = groupBox.fields[1];
      groupBox.insertFieldAfter(newField, sibling);
      expect(groupBox.fields.length).toBe(3);
      expect(groupBox.controls.length).toBe(3);
      expect(groupBox.fields[2]).toBe(newField);
      expect(groupBox.controls[2]).toBe(newField);
      expect(groupBox.fields[1]).toBe(sibling);
      expect(groupBox.controls[1]).toBe(sibling);

      // At the end
      let newField2 = scout.create('StringField', {parent: groupBox});
      sibling = groupBox.fields[2];
      groupBox.insertFieldAfter(newField2, sibling);
      expect(groupBox.fields.length).toBe(4);
      expect(groupBox.controls.length).toBe(4);
      expect(groupBox.fields[3]).toBe(newField2);
      expect(groupBox.controls[3]).toBe(newField2);
      expect(groupBox.fields[2]).toBe(sibling);
      expect(groupBox.controls[2]).toBe(sibling);
    });
  });
});
