/*
 * Copyright (c) 2010-2021 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FormSpecHelper, OutlineSpecHelper} from '../../src/testing/index';
import {Dimension, fields, Form, NullWidget, Rectangle, scout, Status, webstorage} from '../../src/index';

describe('Form', () => {
  let session, helper, outlineHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    jasmine.Ajax.install();
    session = sandboxSession({
      desktop: {
        headerVisible: true,
        navigationVisible: true,
        benchVisible: true
      }
    });
    helper = new FormSpecHelper(session);
    outlineHelper = new OutlineSpecHelper(session);
    uninstallUnloadHandlers(session);
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
  });

  describe('init', () => {
    it('marks the root group box as main box', () => {
      let form = scout.create('Form', {
        parent: session.desktop,
        rootGroupBox: {
          objectType: 'GroupBox',
          fields: [{
            objectType: 'GroupBox'
          }]
        }
      });
      expect(form.rootGroupBox.mainBox).toBe(true);
      expect(form.rootGroupBox.fields[0].mainBox).toBe(false);
    });
  });

  describe('open', () => {

    it('opens the form', done => {
      let form = helper.createFormWithOneField();
      form.open()
        .then(() => {
          expect(form.rendered).toBe(true);
          expect(session.desktop.dialogs.indexOf(form) > -1).toBe(true);
        })
        .catch(fail)
        .always(done);
    });

    it('adds it to the desktop if no display parent is provided', done => {
      let form = helper.createFormWithOneField();
      form.open()
        .then(() => {
          expect(session.desktop.dialogs.indexOf(form) > -1).toBe(true);
        })
        .catch(fail)
        .always(done);
    });

    it('adds it to the provided display parent', done => {
      let parentForm = helper.createFormWithOneField();
      let form = helper.createFormWithOneField();
      form.displayParent = parentForm;
      parentForm.open()
        .then(form.open.bind(form))
        .then(() => {
          expect(form.rendered).toBe(true);
          expect(session.desktop.dialogs.indexOf(form) > -1).toBe(false);
          expect(parentForm.dialogs.indexOf(form) > -1).toBe(true);
        })
        .catch(fail)
        .always(done);
    });
  });

  describe('close', () => {

    it('closes the form', done => {
      let form = helper.createFormWithOneField();
      form.open()
        .then(() => {
          form.close();
          expect(session.desktop.dialogs.indexOf(form) > -1).toBe(false);
          expect(form.rendered).toBe(false);
          expect(form.destroyed).toBe(true);
        })
        .catch(fail)
        .always(done);
    });

    it('closes the form even if opening is still pending', done => {
      let form = helper.createFormWithOneField();
      form.open()
        .then(() => {
          expect(session.desktop.dialogs.indexOf(form) > -1).toBe(false);
          expect(form.rendered).toBe(false);
          expect(form.destroyed).toBe(true);
        })
        .catch(fail)
        .always(done);

      // Close immediately without waiting for the open promise to resolve
      form.close();
    });

    it('removes it from the display parent', done => {
      let parentForm = helper.createFormWithOneField();
      let form = helper.createFormWithOneField();
      form.displayParent = parentForm;
      parentForm.open()
        .then(form.open.bind(form))
        .then(() => {
          expect(parentForm.dialogs.indexOf(form) > -1).toBe(true);

          form.close();
          expect(parentForm.dialogs.indexOf(form) > -1).toBe(false);
          expect(form.rendered).toBe(false);
          expect(form.destroyed).toBe(true);
        })
        .catch(fail)
        .always(done);
    });

  });

  describe('whenClose', () => {

    it('returns a promise which is resolved when the form is closed', done => {
      let form = helper.createFormWithOneField();
      form.open()
        .then(() => {
          form.close();
        })
        .catch(fail);

      form.whenClose()
        .then(() => {
          expect(form.rendered).toBe(false);
          expect(form.destroyed).toBe(true);
        })
        .catch(fail)
        .always(done);
    });

  });

  describe('save', () => {

    it('calls _save', done => {
      let form = helper.createFormWithOneField();
      let saveCalled = false;
      form._save = data => {
        saveCalled = true;
        return $.resolvedPromise();
      };

      form.touch();
      form.save()
        .then(() => {
          expect(saveCalled).toBe(true);
        })
        .catch(fail)
        .always(done);
    });

    it('is marked saved after save', done => {
      let form = helper.createFormWithOneField();
      let field = form.rootGroupBox.fields[0];

      field.setValue('whatever');
      form.save()
        .then(() => {
          // it should be marked saved as the save was successful
          expect(field.touched).toBe(false);
        })
        .catch(fail)
        .always(done);
    });

    it('is not marked saved on error', done => {
      jasmine.clock().install();
      let form = helper.createFormWithOneField();
      let field = form.rootGroupBox.fields[0];

      form._save = data => $.resolvedPromise(Status.error());

      field.setValue('whatever');
      form.save()
        .then(() => {
          // it should not be marked saved because the save returned an error
          expect(field.touched).toBe(true);
        })
        .catch(fail)
        .always(done);
      jasmine.clock().tick(1000);
      helper.closeMessageBoxes();
      jasmine.clock().tick(1000);
      jasmine.clock().uninstall();
    });

    it('does not call save if save is not required', done => {
      let form = helper.createFormWithOneField();
      let saveCalled = false;
      form._save = data => {
        saveCalled = true;
        return $.resolvedPromise();
      };

      // form.touch() has not been called -> _save must not be called
      form.save()
        .then(() => {
          expect(saveCalled).toBe(false);
        })
        .catch(fail)
        .always(done);
    });

  });

  describe('whenSave', () => {

    it('returns a promise which is resolved when the form is saved', done => {
      let form = helper.createFormWithOneField();
      let saveCalled = false;
      form._save = data => {
        saveCalled = true;
        return $.resolvedPromise();
      };

      form.whenSave()
        .then(() => {
          expect(saveCalled).toBe(true);
        })
        .catch(fail)
        .always(done);

      form.touch();
      form.save();
    });

  });

  describe('abort', () => {

    it('closes the form if there is a close button', done => {
      let form = scout.create('Form', {
        parent: session.desktop,
        rootGroupBox: {
          objectType: 'GroupBox',
          menus: [{
            objectType: 'CloseMenu'
          }]
        }
      });
      spyOn(form, 'close').and.callThrough();
      spyOn(form, 'cancel').and.callThrough();
      form.open()
        .then(() => {
          form.abort();
          expect(form.close.calls.count()).toEqual(1);
          expect(form.cancel.calls.count()).toEqual(0);
          expect(form.destroyed).toBe(true);
        })
        .catch(fail)
        .always(done);
    });

    it('closes the form even if opening is still pending', done => {
      let form = scout.create('Form', {
        parent: session.desktop,
        rootGroupBox: {
          objectType: 'GroupBox',
          menus: [{
            objectType: 'CloseMenu'
          }]
        }
      });
      spyOn(form, 'close').and.callThrough();
      spyOn(form, 'cancel').and.callThrough();
      form.open()
        .then(() => {
          expect(form.close.calls.count()).toEqual(1);
          expect(form.cancel.calls.count()).toEqual(0);
          expect(form.destroyed).toBe(true);
        })
        .catch(fail)
        .always(done);

      // Abort immediately without waiting for the open promise to resolve
      form.abort();
    });

    it('closes the form by using cancel if there is no close button', done => {
      let form = scout.create('Form', {
        parent: session.desktop,
        rootGroupBox: {
          objectType: 'GroupBox'
        }
      });
      spyOn(form, 'close').and.callThrough();
      spyOn(form, 'cancel').and.callThrough();
      form.open()
        .then(() => {
          form.abort();
          expect(form.close.calls.count()).toEqual(0);
          expect(form.cancel.calls.count()).toEqual(1);
          expect(form.destroyed).toBe(true);
        })
        .catch(fail)
        .always(done);
    });
  });

  describe('destroy', () => {

    it('destroys its children', () => {
      let form = helper.createFormWithOneField();

      expect(form.rootGroupBox).toBeTruthy();
      expect(form.rootGroupBox.fields[0]).toBeTruthy();

      form.destroy();
      expect(form.rootGroupBox.destroyed).toBeTruthy();
      expect(form.rootGroupBox.fields[0].destroyed).toBeTruthy();
    });

    it('does not fail on form close if a field has focus and validation wants to show a warning', done => {
      let form = helper.createFormWithOneField();
      form.setDisplayHint(Form.DisplayHint.DIALOG);
      let field = helper.createField('DateField');
      field.setValidator(value => {
        throw Status.warning({
          message: 'Invalid value'
        });
      });
      field.getValidationResult = () => {
        // Form has to close (warning state is not supported yet in Scout JS)
        return {
          valid: true,
          validByErrorStatus: true,
          validByMandatory: true
        };
      };
      field.setOwner(form.rootGroupBox);
      form.rootGroupBox.insertField(field);
      form.open()
        .then(() => {
          field.focus();
          field.setValue(new Date());
          expect(field.errorStatus.message).toBe('Invalid value');

          // Field will be validated when it loses the focus which will happen when it is removed
          // -> The warning cannot be displayed because the field status is already removed
          // Test itself won't fail but there will be an error on console if it goes wrong. Don't know why.
          return form.ok();
        })
        .catch(fail)
        .then(done);
    });
  });

  describe('cacheBounds', () => {

    let form;

    beforeEach(() => {
      form = helper.createFormWithOneField();
      form.cacheBounds = true;
      form.cacheBoundsKey = 'FOO';
      form.render();

      webstorage.removeItemFromLocalStorage('scout:formBounds:FOO');
    });

    it('read and store bounds', () => {
      // should return null when local storage not contains the requested key
      expect(form.readCacheBounds()).toBe(null);

      // should return the stored Rectangle
      let storeBounds = new Rectangle(0, 1, 2, 3);
      form.storeCacheBounds(storeBounds);
      let readBounds = form.readCacheBounds();
      expect(readBounds).toEqual(storeBounds);
    });

    it('update bounds - if cacheBounds is true', () => {
      form.updateCacheBounds();
      expect(form.readCacheBounds() instanceof Rectangle).toBe(true);
    });

    it('update bounds - if cacheBounds is false', () => {
      form.cacheBounds = false;
      form.updateCacheBounds();
      expect(form.readCacheBounds()).toBe(null);
    });

  });

  describe('modal', () => {

    it('creates a glass pane if true', done => {
      let form = helper.createFormWithOneField({
        modal: true
      });
      form.open()
        .then(() => {
          expect($('.glasspane').length).toBe(3);
          form.close();
          expect($('.glasspane').length).toBe(0);
        })
        .catch(fail)
        .always(done);
    });

    it('does not create a glass pane if false', done => {
      let form = helper.createFormWithOneField({
        modal: false
      });
      form.open()
        .then(() => {
          expect($('.glasspane').length).toBe(0);
          form.close();
          expect($('.glasspane').length).toBe(0);
        })
        .catch(fail)
        .always(done);
    });

  });

  describe('displayParent', () => {
    let desktop;

    beforeEach(() => {
      desktop = session.desktop;
    });

    it('is required if form is managed by a form controller, defaults to desktop', done => {
      let form = helper.createFormWithOneField();
      expect(form.displayParent).toBe(null);
      form.open()
        .then(() => {
          expect(form.displayParent).toBe(desktop);
          form.close();
        })
        .catch(fail)
        .always(done);
    });

    it('is not required if form is just rendered', () => {
      let form = helper.createFormWithOneField();
      expect(form.displayParent).toBe(null);
      form.render();
      expect(form.displayParent).toBe(null);
      form.destroy();
    });

    it('same as parent if display parent is set', done => {
      // Parent would be something different, removing the parent would remove the form which is not expected, because only removing the display parent has to remove the form
      let initialParent = new NullWidget();
      let form = helper.createFormWithOneField({
        parent: initialParent,
        session: session
      });
      expect(form.displayParent).toBe(null);
      expect(form.parent).toBe(initialParent);
      form.open()
        .then(() => {
          expect(form.displayParent).toBe(desktop);
          expect(form.parent).toBe(desktop);
          form.close();
        })
        .catch(fail)
        .always(done);
    });

    it('not same as parent if display parent is outline', done => {
      // Parent must not be outline if display parent is outline, otherwise making the outline invisible would remove the form, which is not expected. See also DesktopSpec
      let outline = outlineHelper.createOutlineWithOneDetailForm();
      desktop.setOutline(outline);
      let initialParent = new NullWidget();
      let form = helper.createFormWithOneField({
        parent: initialParent,
        session: session,
        displayParent: outline
      });
      expect(form.displayParent).toBe(outline);
      expect(form.parent).toBe(desktop);
      form.open()
        .then(() => {
          expect(form.displayParent).toBe(outline);
          expect(form.parent).toBe(desktop);
          form.close();
        })
        .catch(fail)
        .always(done);
    });

    it('blocks desktop if modal and displayParent is desktop', done => {
      let form = helper.createFormWithOneField({
        modal: true,
        displayParent: desktop
      });
      form.open()
        .then(() => {
          expect($('.glasspane').length).toBe(3);
          expect(desktop.navigation.$container.children('.glasspane').length).toBe(1);
          expect(desktop.bench.$container.children('.glasspane').length).toBe(1);
          expect(desktop.header.$container.children('.glasspane').length).toBe(1);
          form.close();
          expect($('.glasspane').length).toBe(0);
        })
        .catch(fail)
        .always(done);
    });

    it('blocks detail form and outline if modal and displayParent is outline', done => {
      let outline = outlineHelper.createOutlineWithOneDetailForm();
      desktop.setOutline(outline);
      outline.selectNodes(outline.nodes[0]);
      let form = helper.createFormWithOneField({
        displayHint: Form.DisplayHint.DIALOG,
        modal: true,
        displayParent: outline
      });
      form.open()
        .then(() => {
          expect($('.glasspane').length).toBe(2);
          expect(desktop.navigation.$body.children('.glasspane').length).toBe(1);
          expect(outline.nodes[0].detailForm.$container.children('.glasspane').length).toBe(1);
          expect(desktop.header.$container.children('.glasspane').length).toBe(0);
          form.close();
          expect($('.glasspane').length).toBe(0);
        })
        .catch(fail)
        .always(done);
    });

    it('blocks form if modal and displayParent is form', done => {
      let outline = outlineHelper.createOutlineWithOneDetailForm();
      let detailForm = outline.nodes[0].detailForm;
      desktop.setOutline(outline);
      outline.selectNodes(outline.nodes[0]);
      let form = helper.createFormWithOneField({
        modal: true,
        displayParent: detailForm
      });
      form.open()
        .then(() => {
          expect($('.glasspane').length).toBe(1);
          expect(desktop.navigation.$body.children('.glasspane').length).toBe(0);
          expect(detailForm.$container.children('.glasspane').length).toBe(1);
          expect(desktop.header.$container.children('.glasspane').length).toBe(0);
          form.close();
          expect($('.glasspane').length).toBe(0);
        })
        .catch(fail)
        .always(done);
    });

  });

  describe('rootGroupBox.gridData', () => {
    it('is created using gridDataHints when the logical grid is validated', () => {
      let form = scout.create('Form', {
        parent: session.desktop,
        rootGroupBox: {
          objectType: 'GroupBox',
          gridDataHints: {
            heightInPixel: 100
          }
        }
      });
      form.render();
      expect(form.rootGroupBox.gridData.heightInPixel).toBe(0);

      // Logical grid will be validated along with the layout
      form.revalidateLayout();
      expect(form.rootGroupBox.gridData.heightInPixel).toBe(100);
    });
  });

  describe('initialFocus', () => {
    it('references the widget which should gain focus after the form is displayed', () => {
      let form = scout.create('Form', {
        parent: session.desktop,
        initialFocus: 'tabItem1',
        rootGroupBox: {
          objectType: 'GroupBox',
          fields: [{
            objectType: 'TabBox',
            id: 'tabBox',
            tabItems: [{
              objectType: 'TabItem',
              id: 'tabItem1'
            }, {
              objectType: 'TabItem',
              id: 'tabItem2'
            }]
          }]
        }
      });
      form.render();
      form.validateLayoutTree();
      expect(form.widget('tabItem1').isFocused()).toBe(true);

      // InitialFocus property must not modify parent of tab items
      expect(form.widget('tabItem1').parent).toBe(form.widget('tabBox'));
      expect(form.widget('tabItem2').parent).toBe(form.widget('tabBox'));
    });

    it('works correctly even for wrapped forms', done => {
      let form = scout.create({
        parent: session.desktop,
        objectType: 'Form',
        displayHint: Form.DisplayHint.VIEW,
        rootGroupBox: {
          objectType: 'GroupBox',
          fields: [{
            objectType: 'WrappedFormField',
            initialFocusEnabled: true,
            innerForm: {
              objectType: 'Form',
              initialFocus: 'Field2',
              rootGroupBox: {
                objectType: 'GroupBox',
                fields: [
                  {
                    id: 'Field1',
                    objectType: 'StringField',
                    value: 'Field1'
                  },
                  {
                    id: 'Field2',
                    objectType: 'StringField',
                    value: 'Field2'
                  }
                ]
              }
            }
          }]
        }
      });
      form.open()
        .then(() => {
          expect(form.widget('Field2').$field).toBeFocused();
          form.close();
        })
        .catch(fail)
        .always(done);
    });
  });

  describe('restore focus', () => {

    let outlineHelper, desktop;

    beforeEach(() => {
      desktop = session.desktop;
      outlineHelper = new OutlineSpecHelper(session);
    });

    /**
     * Scenario: Switch between two outline nodes and expect the focus in its detail forms are preserved.
     */
    it('on detail forms', () => {
      // setup an outline with 2 nodes each node has a detail form with 3 fields
      let model = outlineHelper.createModelFixture(2, 0, true);
      let outline = outlineHelper.createOutline(model);
      outline.nodes.forEach(node => {
        node.detailForm = helper.createFormWithFields(desktop, false, 3);
        node.detailForm.nodeText = node.text;
        node.detailForm.initialFocus = node.detailForm.rootGroupBox.fields[1];
        node.detailFormVisible = true;
      });

      desktop.setOutline(outline);
      outline.selectNodes(outline.nodes[0]);

      // expect the initial focus of the detail form is rendered.
      expect(outline.nodes[0].detailForm.rootGroupBox.fields[1].isFocused()).toBe(true);
      // focus second field
      outline.nodes[0].detailForm.rootGroupBox.fields[2].$field.focus();
      outline.selectNodes(outline.nodes[1]);
      // expect the initial focus of the detail form is rendered.
      expect(outline.nodes[1].detailForm.rootGroupBox.fields[1].isFocused()).toBe(true);
      outline.nodes[1].detailForm.rootGroupBox.fields[0].$field.focus();

      // switch back and expect the focus gets restored
      outline.selectNodes(outline.nodes[0]);
      expect(outline.nodes[0].detailForm.rootGroupBox.fields[2].isFocused()).toBe(true);
    });
  });

  describe('disabled form', () => {
    it('can be closed although it is disabled', done => {
      let form = scout.create('Form', {
        parent: session.desktop,
        rootGroupBox: {
          id: 'mainbox',
          objectType: 'GroupBox',
          gridDataHints: {
            widthInPixel: 1000
          },
          menus: [{
            id: 'okmenu',
            objectType: 'OkMenu'
          }, {
            id: 'cancelmenu',
            objectType: 'CancelMenu'
          }, {
            id: 'closemenu',
            objectType: 'CloseMenu'
          }, {
            id: 'resetmenu',
            objectType: 'ResetMenu'
          }, {
            id: 'savemenu',
            objectType: 'SaveMenu'
          }],
          fields: [{
            id: 'stringfield1',
            objectType: 'StringField'
          }, {
            id: 'stringfield2',
            objectType: 'StringField',
            inheritAccessibility: false
          }]
        }
      });
      form.setEnabled(false);
      form.open()
        .then(() => {
          expect(session.desktop.activeForm).toBe(form);

          // enabled
          expect(form.enabled).toBe(false);
          expect(form.widget('mainbox').enabled).toBe(true);
          expect(form.widget('stringfield1').enabled).toBe(true);
          expect(form.widget('stringfield2').enabled).toBe(true);
          expect(form.widget('okmenu').enabled).toBe(true);
          expect(form.widget('cancelmenu').enabled).toBe(true);
          expect(form.widget('closemenu').enabled).toBe(true);
          expect(form.widget('resetmenu').enabled).toBe(true);
          expect(form.widget('savemenu').enabled).toBe(true);

          // enabledComputed
          expect(form.enabledComputed).toBe(false);
          expect(form.widget('mainbox').enabledComputed).toBe(false);
          expect(form.widget('stringfield1').enabledComputed).toBe(false);
          expect(form.widget('stringfield2').enabledComputed).toBe(true);
          expect(form.widget('okmenu').enabledComputed).toBe(false);
          expect(form.widget('cancelmenu').enabledComputed).toBe(true);
          expect(form.widget('closemenu').enabledComputed).toBe(true);
          expect(form.widget('resetmenu').enabledComputed).toBe(false);
          expect(form.widget('savemenu').enabledComputed).toBe(false);

          return form.close().then(() => {
            expect(session.desktop.activeForm).toBe(null);
            done();
          });
        })
        .catch(fail);
    });
  });

  describe('maximized', () => {
    let mockPrefSize = () => new Dimension(50, 50);

    it('makes form as big as window after open', done => {
      let form = helper.createFormWithOneField();
      form.animateOpening = false;
      form.setMaximized(true);
      form.one('render', () => {
        form.htmlComp.layout.preferredLayoutSize = mockPrefSize;
      });
      form.open()
        .then(() => {
          expect(form.$container.width()).toBeGreaterThan(50);
          expect(form.$container.height()).toBeGreaterThan(50);
          form.close();
        })
        .catch(fail)
        .always(done);
    });

    it('can be toggled dynamically', done => {
      let form = helper.createFormWithOneField();
      form.animateOpening = false;
      form.one('render', () => {
        form.htmlComp.layout.preferredLayoutSize = mockPrefSize;
      });
      form.open()
        .then(() => {
          expect(form.$container.width()).toBe(50);
          expect(form.$container.height()).toBe(50);
          form.setMaximized(true);
          expect(form.$container.width()).toBeGreaterThan(50);
          expect(form.$container.height()).toBeGreaterThan(50);
          form.close();
        })
        .catch(fail)
        .always(done);
    });

    it('sets size to prefSize when set to false', done => {
      let form = helper.createFormWithOneField();
      form.animateOpening = false;
      form.setMaximized(true);
      form.one('render', () => {
        form.htmlComp.layout.preferredLayoutSize = mockPrefSize;
      });
      form.open()
        .then(() => {
          form.setMaximized(false);
          expect(form.$container.width()).toBe(50);
          expect(form.$container.height()).toBe(50);
          form.close();
        })
        .catch(fail)
        .always(done);
    });

    it('sets size to previous size when set to false when modified before', done => {
      let form = helper.createFormWithOneField();
      form.animateOpening = false;
      form.one('render', () => {
        form.htmlComp.layout.preferredLayoutSize = mockPrefSize;
      });
      form.open()
        .then(() => {
          form.$container.width(60);
          form.$container.height(70);
          form.setMaximized(true);
          expect(form.$container.width()).toBeGreaterThan(50);
          expect(form.$container.height()).toBeGreaterThan(50);
          form.setMaximized(false);
          expect(form.$container.width()).toBe(60);
          expect(form.$container.height()).toBe(70);
          form.close();
        })
        .catch(fail)
        .always(done);
    });

    it('removes resize handles', done => {
      let form = helper.createFormWithOneField();
      form.animateOpening = false;
      form.one('render', () => {
        form.htmlComp.layout.preferredLayoutSize = mockPrefSize;
      });
      form.open()
        .then(() => {
          expect(form.$container.data('resizable')).toBeTruthy();
          form.setMaximized(true);
          expect(form.$container.data('resizable')).toBeFalsy();
          form.setMaximized(false);
          expect(form.$container.data('resizable')).toBeTruthy();
          form.close();
        })
        .catch(fail)
        .always(done);
    });
  });

  describe('reveal invalid element', () => {
    it('revealInvalidElement selects all parent tabs of field', done => {
      let form = helper.createFormWithFieldsAndTabBoxes();

      let field2 = form.widget('Field2'),
        tabBox = form.widget('TabBox'),
        tabA = form.widget('TabA'),
        fieldA1 = form.widget('FieldA1'),
        tabBoxA = form.widget('TabBoxA'),
        tabAA = form.widget('TabAA'),
        fieldAA2 = form.widget('FieldAA2'),
        tabAB = form.widget('TabAB'),
        fieldAB1 = form.widget('FieldAB1'),
        tabAC = form.widget('TabAC'),
        fieldAC1 = form.widget('FieldAC1'),
        tabB = form.widget('TabB'),
        fieldB3 = form.widget('FieldB3'),
        createValidationResult = field => ({
          valid: false,
          validByErrorStatus: true,
          validByMandatory: true,
          field: field,
          reveal: () => {
            fields.selectAllParentTabsOf(field);
            field.focus();
          }
        });

      form.open()
        .then(() => {
          expect(tabBox.selectedTab).toBe(tabA);
          expect(tabBoxA.selectedTab).toBe(tabAA);

          form.revealInvalidField(createValidationResult(field2));

          expect(tabBox.selectedTab).toBe(tabA);
          expect(tabBoxA.selectedTab).toBe(tabAA);

          form.revealInvalidField(createValidationResult(fieldAC1));

          expect(tabBox.selectedTab).toBe(tabA);
          expect(tabBoxA.selectedTab).toBe(tabAC);

          form.revealInvalidField(createValidationResult(fieldB3));

          expect(tabBox.selectedTab).toBe(tabB);
          expect(tabBoxA.selectedTab).toBe(tabAC);

          form.revealInvalidField(createValidationResult(fieldA1));

          expect(tabBox.selectedTab).toBe(tabA);
          expect(tabBoxA.selectedTab).toBe(tabAC);

          form.revealInvalidField(createValidationResult(fieldAA2));

          expect(tabBox.selectedTab).toBe(tabA);
          expect(tabBoxA.selectedTab).toBe(tabAA);

          form.revealInvalidField(createValidationResult(fieldAB1));

          expect(tabBox.selectedTab).toBe(tabA);
          expect(tabBoxA.selectedTab).toBe(tabAB);

          form.close();
        })
        .catch(fail)
        .always(done);
    });
  });
});
