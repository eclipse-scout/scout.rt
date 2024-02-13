/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormSpecHelper, OutlineSpecHelper, SpecForm} from '../../src/testing/index';
import {
  App, CancelMenu, CloseMenu, Dimension, fields, Form, FormFieldMenu, FormModel, InitModelOf, Menu, MessageBox, NotificationBadgeStatus, NullWidget, NumberField, ObjectFactory, OkMenu, Popup, PopupBlockerHandler, Rectangle, ResetMenu,
  SaveMenu, scout, SearchMenu, SequenceBox, Session, SplitBox, Status, StringField, strings, TabBox, TabItem, webstorage, WrappedFormField
} from '../../src/index';
import {DateField, GroupBox} from '../../src';

describe('Form', () => {
  let session: Session;
  let helper: FormSpecHelper;
  let outlineHelper: OutlineSpecHelper;

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
      let form = scout.create(Form, {
        parent: session.desktop,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [{
            objectType: GroupBox
          }]
        }
      });
      expect(form.rootGroupBox.mainBox).toBe(true);
      let formField = form.rootGroupBox.fields[0] as GroupBox;
      expect(formField.mainBox).toBe(false);
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

    it('does not load the form multiple times', async () => {
      let loadCounter = 0;
      let form = scout.create(Form, {parent: session.desktop});
      form.open();
      form.on('load', () => loadCounter++);
      expect(form.isShown()).toBe(false);
      expect(loadCounter).toBe(0);

      form.open();
      expect(form.isShown()).toBe(false);
      expect(loadCounter).toBe(0);

      await form.whenLoad();
      expect(loadCounter).toBe(1);
      expect(form.isShown()).toBe(false);

      await form.when('render');
      expect(form.isShown()).toBe(true);

      await form.open();
      expect(loadCounter).toBe(1);

      form.hide();
      expect(form.isShown()).toBe(false);

      await form.open();
      expect(loadCounter).toBe(1);
      expect(form.isShown()).toBe(true);

      // Manually triggering load still works
      await form.load();
      expect(loadCounter).toBe(2);
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

  describe('load', () => {
    it('sets formLoading to true while loading and formLoaded to true after loading', async () => {
      let form = scout.create(Form, {parent: session.desktop});
      expect(form.formLoading).toBe(false);
      expect(form.formLoaded).toBe(false);

      form.load();
      expect(form.formLoading).toBe(true);
      expect(form.formLoaded).toBe(false);

      await form.whenLoad();
      expect(form.formLoading).toBe(false);
      expect(form.formLoaded).toBe(true);
    });

    it('does not load multiple times if load is called while it is still loading', async () => {
      let loadCounter = 0;
      let form = scout.create(Form, {parent: session.desktop});
      form.on('load', () => loadCounter++);
      expect(loadCounter).toBe(0);

      form.load();
      expect(loadCounter).toBe(0);

      form.load();
      expect(loadCounter).toBe(0);

      await form.whenLoad();
      expect(loadCounter).toBe(1);
    });

    it('the second call to load returns a promise that is resolved when loaded', async () => {
      let loadCounter = 0;
      let form = scout.create(Form, {parent: session.desktop});
      form.on('load', () => loadCounter++);
      expect(loadCounter).toBe(0);

      form.load();
      expect(loadCounter).toBe(0);

      await form.load();
      expect(loadCounter).toBe(1);
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

      expect(form.formSaved).toBe(false);
      form.touch();
      expect(form.formSaved).toBe(false);
      form.save()
        .then(() => {
          expect(saveCalled).toBe(true);
          expect(form.formSaved).toBe(true);
        })
        .catch(fail)
        .always(done);
    });

    it('is marked saved after save', done => {
      let form = helper.createFormWithOneField();
      let field = form.rootGroupBox.fields[0] as StringField;

      field.setValue('whatever');
      form.save()
        .then(() => {
          // it should be marked saved as the save was successful
          expect(field.saveNeeded).toBe(false);
        })
        .catch(fail)
        .always(done);
    });

    it('is not marked saved on error', done => {
      jasmine.clock().install();
      let form = helper.createFormWithOneField();
      let field = form.rootGroupBox.fields[0] as StringField;

      form._save = data => $.rejectedPromise(Status.error());

      field.setValue('whatever');
      form.save()
        .then(fail)
        .catch(() => {
          // it should not be marked saved because the save returned an error
          expect(field.saveNeeded).toBe(true);
        })
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
      let form = scout.create(Form, {
        parent: session.desktop,
        rootGroupBox: {
          objectType: GroupBox,
          menus: [{
            objectType: CloseMenu
          }]
        }
      });
      let closeSpy = spyOn(form, 'close').and.callThrough();
      let cancelSpy = spyOn(form, 'cancel').and.callThrough();
      form.open()
        .then(() => {
          form.abort();
          expect(closeSpy.calls.count()).toEqual(1);
          expect(cancelSpy.calls.count()).toEqual(0);
          expect(form.destroyed).toBe(true);
        })
        .catch(fail)
        .always(done);
    });

    it('closes the form even if opening is still pending', done => {
      let form = scout.create(Form, {
        parent: session.desktop,
        rootGroupBox: {
          objectType: GroupBox,
          menus: [{
            objectType: CloseMenu
          }]
        }
      });
      let closeSpy = spyOn(form, 'close').and.callThrough();
      let cancelSpy = spyOn(form, 'cancel').and.callThrough();
      form.open()
        .then(() => {
          expect(closeSpy.calls.count()).toEqual(1);
          expect(cancelSpy.calls.count()).toEqual(0);
          expect(form.destroyed).toBe(true);
        })
        .catch(fail)
        .always(done);

      // Abort immediately without waiting for the open promise to resolve
      form.abort();
    });

    it('closes the form by using cancel if there is no close button', done => {
      let form = scout.create(Form, {
        parent: session.desktop,
        rootGroupBox: {
          objectType: GroupBox
        }
      });
      let closeSpy = spyOn(form, 'close').and.callThrough();
      let cancelSpy = spyOn(form, 'cancel').and.callThrough();
      form.open()
        .then(() => {
          form.abort();
          expect(closeSpy.calls.count()).toEqual(0);
          expect(cancelSpy.calls.count()).toEqual(1);
          expect(form.destroyed).toBe(true);
        })
        .catch(fail)
        .always(done);
    });

    it('activates the form if it is still open after close', done => {
      // Use case: press x icon on an inactive tab for a form that shows a message box
      // -> form cannot be closed because the user has to confirm the msg box
      let form = helper.createFormWithOneField();
      form.setDisplayHint(Form.DisplayHint.VIEW);
      form.open()
        .then(() => {
          form.touch();
          let desktop = form.session.desktop;
          expect(desktop.activeForm).toBe(form);

          desktop.activateForm(null);
          expect(desktop.activeForm).toBe(null);

          form.abort();
          expect(form.destroyed).toBe(false);
          expect(desktop.activeForm).toBe(form);
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
      let field = helper.createField(DateField);
      field.setValidator(value => {
        throw Status.warning({
          message: 'Invalid value'
        });
      });
      field.getValidationResult = () => {
        // Form has to close (warning state is not supported yet in Scout JS)
        return {
          valid: true,
          validByMandatory: true,
          field,
          label: field.label,
          reveal: () => {
            // nop
          }
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
      openFormAndExpectGlassPane(form, true, done);
    });

    it('creates a glass pane if true also for views', done => {
      let form = helper.createFormWithOneField({
        modal: true,
        displayHint: Form.DisplayHint.VIEW
      });
      openFormAndExpectGlassPane(form, true, done);
    });

    it('does not create a glass pane if false', done => {
      let form = helper.createFormWithOneField({
        modal: false
      });
      openFormAndExpectGlassPane(form, false, done);
    });

    it('is true for dialogs if not explicitly set', done => {
      let form = helper.createFormWithOneField({
        displayHint: Form.DisplayHint.DIALOG
      });
      expect(form.modal).toBe(true);
      openFormAndExpectGlassPane(form, true, done);
    });

    it('is false for views if not explicitly set', done => {
      let form = helper.createFormWithOneField({
        displayHint: Form.DisplayHint.VIEW
      });
      expect(form.modal).toBe(false);
      openFormAndExpectGlassPane(form, false, done);
    });

    function openFormAndExpectGlassPane(form: Form, expectGlasspane: boolean, done: DoneFn) {
      form.open()
        .then(() => {
          expect($('.glasspane').length).toBe(expectGlasspane ? form.displayHint === 'dialog' ? 3 : 2 : 0);
          form.close();
          expect($('.glasspane').length).toBe(0);
        })
        .catch(fail)
        .always(done);
    }
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

    it('does not block child popup if dialog is modal', done => {
      let dialog = helper.createFormWithOneField({
        modal: true
      });
      let popup = scout.create(Popup, {
        parent: dialog,
        withGlassPane: true
      });
      dialog.open()
        .then(() => popup.open())
        .then(() => {
          expect($('.glasspane').length).toBe(7);
          expect(desktop.navigation.$container.children('.glasspane').length).toBe(2);
          expect(desktop.header.$container.children('.glasspane').length).toBe(2);
          expect(desktop.bench.$container.children('.glasspane').length).toBe(2);
          expect(dialog.$container.children('.glasspane').length).toBe(1);
          expect(popup.$container.children('.glasspane').length).toBe(0); // Must not be covered

          popup.close();
          expect($('.glasspane').length).toBe(3);

          dialog.close();
          expect($('.glasspane').length).toBe(0);
        })
        .catch(fail)
        .always(done);
    });

    it('does not block child popup if dialog is modal and displayParent a view', done => {
      // Use case: on mobile, a view with a dialog containing a touch popup is open
      let view = helper.createFormWithOneField({
        modal: false,
        displayHint: Form.DisplayHint.VIEW
      });
      let dialog = helper.createFormWithOneField({
        modal: true,
        displayParent: view
      });
      let popup = scout.create(Popup, {
        parent: dialog,
        withGlassPane: true
      });
      view.open()
        .then(() => dialog.open())
        .then(() => popup.open())
        .then(() => {
          expect($('.glasspane').length).toBe(5);
          expect(desktop.navigation.$container.children('.glasspane').length).toBe(1); // Provided by popup
          expect(desktop.header.$container.children('.glasspane').length).toBe(1); // Provided by popup
          expect(desktop.bench.$container.children('.glasspane').length).toBe(1); // Provided by popup
          expect(view.$container.children('.glasspane').length).toBe(1); // Provided by dialog
          expect(dialog.$container.children('.glasspane').length).toBe(1); // Provided by popup
          expect(popup.$container.children('.glasspane').length).toBe(0); // Must not be covered

          popup.close();
          expect($('.glasspane').length).toBe(1);

          dialog.close();
          expect($('.glasspane').length).toBe(0);

          view.close();
        })
        .catch(fail)
        .always(done);
    });
  });

  describe('rootGroupBox.gridData', () => {
    it('is created using gridDataHints when the logical grid is validated', () => {
      let form = scout.create(Form, {
        parent: session.desktop,
        rootGroupBox: {
          objectType: GroupBox,
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
      let form = scout.create(Form, {
        parent: session.desktop,
        initialFocus: 'tabItem1',
        rootGroupBox: {
          objectType: GroupBox,
          fields: [{
            objectType: TabBox,
            id: 'tabBox',
            tabItems: [{
              objectType: TabItem,
              id: 'tabItem1'
            }, {
              objectType: TabItem,
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
        objectType: Form,
        displayHint: Form.DisplayHint.VIEW,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [{
            objectType: WrappedFormField,
            initialFocusEnabled: true,
            innerForm: {
              objectType: Form,
              initialFocus: 'Field2',
              rootGroupBox: {
                objectType: GroupBox,
                fields: [
                  {
                    id: 'Field1',
                    objectType: StringField,
                    value: 'Field1'
                  },
                  {
                    id: 'Field2',
                    objectType: StringField,
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
          expect(form.widget('Field2', StringField).$field).toBeFocused();
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
      // set up an outline with 2 nodes each node has a detail form with 3 fields
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
      let form = scout.create(Form, {
        parent: session.desktop,
        rootGroupBox: {
          id: 'mainbox',
          objectType: GroupBox,
          gridDataHints: {
            widthInPixel: 1000
          },
          menus: [{
            id: 'okmenu',
            objectType: OkMenu
          }, {
            id: 'cancelmenu',
            objectType: CancelMenu
          }, {
            id: 'closemenu',
            objectType: CloseMenu
          }, {
            id: 'resetmenu',
            objectType: ResetMenu
          }, {
            id: 'savemenu',
            objectType: SaveMenu
          }, {
            id: 'searchmenu',
            objectType: SearchMenu
          }],
          fields: [{
            id: 'stringfield1',
            objectType: StringField
          }, {
            id: 'stringfield2',
            objectType: StringField,
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
          expect(form.widget('searchmenu').enabled).toBe(true);

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
          expect(form.widget('searchmenu').enabledComputed).toBe(false);

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
        tabBox = form.widget('TabBox', TabBox),
        tabA = form.widget('TabA', TabItem),
        fieldA1 = form.widget('FieldA1'),
        tabBoxA = form.widget('TabBoxA', TabBox),
        tabAA = form.widget('TabAA', TabItem),
        fieldAA2 = form.widget('FieldAA2'),
        tabAB = form.widget('TabAB', TabItem),
        fieldAB1 = form.widget('FieldAB1'),
        tabAC = form.widget('TabAC', TabItem),
        fieldAC1 = form.widget('FieldAC1'),
        tabB = form.widget('TabB', TabItem),
        fieldB3 = form.widget('FieldB3'),
        createValidationResult = field => ({
          valid: false,
          validByMandatory: true,
          field,
          label: field.label,
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

  describe('notificationBadgeText', () => {

    it('is updated correctly', () => {
      const form = helper.createFormWithOneField();
      expect(form.getNotificationBadgeText()).toBeUndefined();

      form.setNotificationBadgeText('foo');
      expect(form.getNotificationBadgeText()).toBe('foo');
      form.setNotificationBadgeText('bar');
      expect(form.getNotificationBadgeText()).toBe('bar');
      form.setNotificationBadgeText(null);
      expect(form.getNotificationBadgeText()).toBeUndefined();

      form.setNotificationBadgeText('foo');
      expect(form.getNotificationBadgeText()).toBe('foo');
      form.addStatus(new NotificationBadgeStatus({message: 'bar'}));
      expect(form.getNotificationBadgeText()).toBe('foo');
      form.setNotificationBadgeText(null);
      expect(form.getNotificationBadgeText()).toBeUndefined();

      form.addStatus(new NotificationBadgeStatus({message: 'bar'}));
      expect(form.getNotificationBadgeText()).toBeUndefined();
      form.setNotificationBadgeText('foo');
      expect(form.getNotificationBadgeText()).toBe('foo');
      form.setNotificationBadgeText(null);
      expect(form.getNotificationBadgeText()).toBeUndefined();
    });
  });

  describe('saveNeeded', () => {
    let form: Form;
    let firstField: StringField;
    let secondField: StringField;

    beforeEach(() => {
      form = scout.create(Form, {
        parent: session.desktop,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [{
            objectType: StringField,
            id: 'FirstField'
          }, {
            objectType: StringField,
            id: 'SecondField'
          }]
        }
      });
      firstField = form.widget('FirstField', StringField);
      secondField = form.widget('SecondField', StringField);
    });

    it('turns true as soon as a field requires to be saved', done => {
      expect(form.saveNeeded).toBe(false);

      firstField.setValue('hi');
      expect(firstField.saveNeeded).toBe(true);
      expect(form.rootGroupBox.saveNeeded).toBe(true);
      expect(form.saveNeeded).toBe(true);

      secondField.setValue('there');
      expect(secondField.saveNeeded).toBe(true);
      expect(form.rootGroupBox.saveNeeded).toBe(true);
      expect(form.saveNeeded).toBe(true);

      form.save()
        .then(() => {
          expect(firstField.saveNeeded).toBe(false);
          expect(secondField.saveNeeded).toBe(false);
          expect(form.rootGroupBox.saveNeeded).toBe(false);
          expect(form.saveNeeded).toBe(false);
        })
        .catch(fail)
        .always(done);
    });

    it('turns false when no fields require to be saved anymore', () => {
      expect(form.saveNeeded).toBe(false);

      firstField.setValue('hi');
      expect(firstField.saveNeeded).toBe(true);
      expect(form.rootGroupBox.saveNeeded).toBe(true);
      expect(form.saveNeeded).toBe(true);

      firstField.setValue(null);
      expect(form.saveNeeded).toBe(false);

      secondField.setValue('there');
      expect(form.saveNeeded).toBe(true);

      firstField.setValue('there');
      expect(form.saveNeeded).toBe(true);

      secondField.setValue(null);
      expect(form.saveNeeded).toBe(true);

      firstField.setValue(null);
      expect(firstField.saveNeeded).toBe(false);
      expect(form.rootGroupBox.saveNeeded).toBe(false);
      expect(form.saveNeeded).toBe(false);
    });

    it('is false right after loading even if values are set while init or loading', done => {
      class MyForm extends Form {
        protected override _jsonModel(): FormModel {
          return {
            rootGroupBox: {
              objectType: GroupBox,
              fields: [{
                objectType: TabBox,
                tabItems: [{
                  objectType: TabItem,
                  fields: [{
                    objectType: StringField,
                    id: 'TabField'
                  }, {
                    objectType: StringField,
                    id: 'TabField2'
                  }]
                }]
              }]
            }
          };
        }

        protected override _init(model: InitModelOf<this>) {
          super._init(model);
          this.widget('TabField', StringField).setValue('hello');
        }

        protected override _load(): JQuery.Promise<object> {
          this.widget('TabField', StringField).setValue('there');
          return super._load();
        }

        override importData() {
          this.widget('TabField2', StringField).setValue('hola');
        }
      }

      form = scout.create(MyForm, {parent: session.desktop});
      form.load()
        .then(() => {
          expect(form.saveNeeded).toBe(false);
        })
        .catch(fail)
        .always(done);
    });

    it('works if fields are changed dynamically', () => {
      expect(form.saveNeeded).toBe(false);

      firstField.setValue('hi');
      expect(form.saveNeeded).toBe(true);

      form.rootGroupBox.deleteField(firstField);
      expect(form.rootGroupBox.saveNeeded).toBe(false);
      expect(form.saveNeeded).toBe(false);

      let newField = scout.create(StringField, {parent: form.rootGroupBox});
      form.rootGroupBox.insertField(newField);
      expect(form.rootGroupBox.saveNeeded).toBe(false);
      expect(form.saveNeeded).toBe(false);

      newField.setValue('hi');
      expect(newField.saveNeeded).toBe(true);
      expect(form.rootGroupBox.saveNeeded).toBe(true);
      expect(form.saveNeeded).toBe(true);

      form.markAsSaved();
      expect(newField.saveNeeded).toBe(false);
      expect(form.rootGroupBox.saveNeeded).toBe(false);
      expect(form.saveNeeded).toBe(false);

      // Insert a new field that has saveNeeded set to true
      let newField2 = scout.create(StringField, {
        parent: form,
        value: '123'
      });
      newField2.touch();
      form.rootGroupBox.insertField(newField2);
      expect(newField2.saveNeeded).toBe(true);
      expect(form.rootGroupBox.saveNeeded).toBe(true);
      expect(form.saveNeeded).toBe(true);
    });

    it('works if fields are removed temporarily', () => {
      form = scout.create(Form, {
        parent: session.desktop,
        rootGroupBox: {
          objectType: GroupBox
        }
      });
      expect(form.saveNeeded).toBe(false);

      // Owner is form so field won't be destroyed when removed from the box
      let newField = scout.create(StringField, {parent: form});
      form.rootGroupBox.insertField(newField);
      expect(form.saveNeeded).toBe(false);

      newField.setValue('hi');
      expect(form.saveNeeded).toBe(true);

      form.rootGroupBox.deleteField(newField);
      expect(form.saveNeeded).toBe(false);
      expect(newField.destroyed).toBe(false);

      form.rootGroupBox.insertField(newField);
      expect(form.saveNeeded).toBe(true);
    });

    it('manages listeners correctly', () => {
      form = scout.create(Form, {
        parent: session.desktop,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [{
            objectType: GroupBox,
            id: 'Box1'
          }, {
            objectType: GroupBox,
            id: 'Box2',
            menus: [{
              objectType: FormFieldMenu,
              id: 'Menu'
            }]
          }]
        }
      });
      let newField = scout.create(StringField, {parent: form});
      let box1 = form.widget('Box1', GroupBox);
      let box2 = form.widget('Box2', GroupBox);
      let menu = form.widget('Menu', FormFieldMenu);
      let fieldListenerCount = newField.events.count('hierarchyChange');
      let box1ListenerCount = box1.events.count('hierarchyChange');
      let box2ListenerCount = box2.events.count('hierarchyChange');
      let menuListenerCount = menu.events.count('hierarchyChange');
      let mainBoxListenerCount = form.rootGroupBox.events.count('hierarchyChange');
      let formListenerCount = form.events.count('hierarchyChange');

      box1.insertField(newField);
      expect(newField.events.count('hierarchyChange')).toBe(fieldListenerCount);
      expect(box1.events.count('hierarchyChange')).toBe(box1ListenerCount);
      expect(form.rootGroupBox.events.count('hierarchyChange')).toBe(mainBoxListenerCount);
      expect(form.events.count('hierarchyChange')).toBe(formListenerCount - 1); // Field was attached to form -> box1 will be the new parent

      box1.deleteField(newField);
      expect(newField.events.count('hierarchyChange')).toBe(fieldListenerCount);
      expect(box1.events.count('hierarchyChange')).toBe(box1ListenerCount);
      expect(form.rootGroupBox.events.count('hierarchyChange')).toBe(mainBoxListenerCount);
      expect(form.events.count('hierarchyChange')).toBe(formListenerCount);

      menu.setField(newField);
      expect(newField.events.count('hierarchyChange')).toBe(fieldListenerCount);
      expect(menu.events.count('hierarchyChange')).toBe(menuListenerCount + 1);
      expect(box2.events.count('hierarchyChange')).toBe(box2ListenerCount);
      expect(form.rootGroupBox.events.count('hierarchyChange')).toBe(mainBoxListenerCount);
      expect(form.events.count('hierarchyChange')).toBe(formListenerCount - 1);

      menu.setField(null);
      expect(newField.events.count('hierarchyChange')).toBe(fieldListenerCount);
      expect(menu.events.count('hierarchyChange')).toBe(menuListenerCount);
      expect(box2.events.count('hierarchyChange')).toBe(box2ListenerCount);
      expect(form.rootGroupBox.events.count('hierarchyChange')).toBe(mainBoxListenerCount);
      expect(form.events.count('hierarchyChange')).toBe(formListenerCount);

      expect(form.saveNeeded).toBe(false);

      // Field is not linked -> nothing should happen
      newField.touch();
      expect(form.saveNeeded).toBe(false);

      box2.insertField(newField);
      expect(form.saveNeeded).toBe(true);

      // Move field into menu and move menu into box1
      expect(box1.saveNeeded).toBe(false);
      box2.deleteField(newField);
      menu.setField(newField);
      menu.setOwner(form);
      box1.insertMenu(menu);
      expect(newField.events.count('hierarchyChange')).toBe(fieldListenerCount);
      expect(menu.events.count('hierarchyChange')).toBe(menuListenerCount + 1);
      expect(box1.events.count('hierarchyChange')).toBe(box1ListenerCount);
      expect(box2.events.count('hierarchyChange')).toBe(box2ListenerCount);
      expect(form.rootGroupBox.events.count('hierarchyChange')).toBe(mainBoxListenerCount);
      expect(form.events.count('hierarchyChange')).toBe(formListenerCount - 1);
      expect(box1.saveNeeded).toBe(true);
    });

    it('works for tab boxes', () => {
      let form = scout.create(Form, {
        parent: session.desktop,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [{
            objectType: TabBox,
            tabItems: [{
              objectType: TabItem,
              fields: [{
                objectType: StringField,
                id: 'TabField'
              }]
            }]
          }]
        }
      });
      let tabField = form.widget('TabField', StringField);
      expect(form.saveNeeded).toBe(false);

      tabField.setValue('hi');
      expect(tabField.getParentGroupBox().saveNeeded).toBe(true);
      expect(form.saveNeeded).toBe(true);

      let tabBox = tabField.findParent(TabBox);
      tabBox.deleteTabItem(tabField.findParent(TabItem));
      expect(form.rootGroupBox.saveNeeded).toBe(false);
      expect(form.saveNeeded).toBe(false);

      let newTabItem = scout.create(TabItem, {
        parent: form,
        fields: [{
          objectType: StringField
        }]
      });
      tabBox.insertTabItem(newTabItem);
      expect(form.saveNeeded).toBe(false);

      (newTabItem.fields[0] as StringField).setValue('hi');
      expect(tabBox.saveNeeded).toBe(true);
      expect(form.saveNeeded).toBe(true);

      form.markAsSaved();
      expect(tabBox.saveNeeded).toBe(false);
      expect(form.saveNeeded).toBe(false);
    });

    it('works for sequence boxes', () => {
      let form = scout.create(Form, {
        parent: session.desktop,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [{
            objectType: SequenceBox,
            fields: [{
              objectType: StringField,
              id: 'SeqField'
            }, {
              objectType: StringField,
              id: 'SeqField2'
            }]
          }]
        }
      });
      expect(form.saveNeeded).toBe(false);

      let seqField = form.widget('SeqField', StringField);
      let seqBox = seqField.findParent(SequenceBox);
      seqField.setValue('hi');
      expect(seqBox.saveNeeded).toBe(true);
      expect(form.saveNeeded).toBe(true);

      let seqField2 = form.widget('SeqField2', StringField);
      seqField2.setValue('there');
      expect(form.saveNeeded).toBe(true);

      seqField.setValue(null);
      expect(seqBox.saveNeeded).toBe(true);
      expect(form.saveNeeded).toBe(true);

      seqField2.setValue(null);
      expect(seqBox.saveNeeded).toBe(false);
      expect(form.saveNeeded).toBe(false);
    });

    it('works for split boxes', () => {
      let form = scout.create(Form, {
        parent: session.desktop,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [{
            objectType: SplitBox,
            id: 'SplitBox',
            firstField: {
              objectType: StringField
            },
            secondField: {
              objectType: StringField
            }
          }]
        }
      });
      expect(form.saveNeeded).toBe(false);

      let splitBox = form.widget('SplitBox', SplitBox);
      (splitBox.firstField as StringField).setValue('hi');
      expect(splitBox.saveNeeded).toBe(true);
      expect(form.saveNeeded).toBe(true);

      (splitBox.secondField as StringField).setValue('there');
      expect(form.saveNeeded).toBe(true);

      (splitBox.firstField as StringField).setValue(null);
      expect(splitBox.saveNeeded).toBe(true);
      expect(form.saveNeeded).toBe(true);

      (splitBox.secondField as StringField).setValue(null);
      expect(splitBox.saveNeeded).toBe(false);
      expect(form.saveNeeded).toBe(false);
    });

    it('considers fields in menu bars as well', done => {
      expect(form.saveNeeded).toBe(false);

      let menu = scout.create(FormFieldMenu, {
        parent: form,
        field: {
          objectType: StringField
        }
      });
      form.rootGroupBox.insertMenu(menu);
      let menubarField = (menu.field as StringField);
      menubarField.setValue('there');
      expect(menubarField.saveNeeded).toBe(true);
      expect(form.rootGroupBox.saveNeeded).toBe(true);
      expect(form.saveNeeded).toBe(true);

      let menu2 = scout.create(FormFieldMenu, {
        parent: secondField,
        field: {
          objectType: StringField
        }
      });
      secondField.insertMenu(menu2);
      let menuField = (menu2.field as StringField);
      menuField.setValue('hello');
      expect(menuField.saveNeeded).toBe(true);
      expect(form.rootGroupBox.saveNeeded).toBe(true);
      expect(form.saveNeeded).toBe(true);

      menubarField.setValue(null);
      expect(form.saveNeeded).toBe(true);

      menuField.setValue(null);
      expect(form.saveNeeded).toBe(false);

      menubarField.setValue('hi');
      menuField.setValue('there');
      expect(form.saveNeeded).toBe(true);

      form.save()
        .then(() => {
          expect(menubarField.saveNeeded).toBe(false);
          expect(menuField.saveNeeded).toBe(false);
          expect(form.rootGroupBox.saveNeeded).toBe(false);
          expect(form.saveNeeded).toBe(false);
        })
        .catch(fail)
        .always(done);
    });

    it('considers natural form bounds', async () => {
      let form = scout.create(Form, {
        parent: session.desktop,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [{
            objectType: StringField,
            id: 'StringField'
          }]
        }
      });
      let form2 = scout.create(Form, {
        parent: form.widget('StringField'),
        rootGroupBox: {
          objectType: GroupBox,
          fields: [{
            objectType: StringField,
            id: 'StringField2'
          }]
        }
      });
      await form.open();
      await form2.open();
      form2.widget('StringField2', StringField).setValue('asdf');
      expect(form2.saveNeeded).toBe(true);
      expect(form.saveNeeded).toBe(false);

      form.markAsSaved();
      expect(form2.saveNeeded).toBe(true);
      expect(form.saveNeeded).toBe(false);

      form2.markAsSaved();
      expect(form2.saveNeeded).toBe(false);
      expect(form.saveNeeded).toBe(false);
    });

    it('will be false after resetting', () => {
      let form = scout.create(Form, {
        parent: session.desktop,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [{
            objectType: TabBox,
            tabItems: [{
              objectType: TabItem,
              fields: [{
                id: 'TabField',
                objectType: StringField,
                value: 'tab init'
              }]
            }]
          }, {
            id: 'StringField',
            objectType: StringField,
            value: 'str init'
          }],
          menus: [{
            objectType: FormFieldMenu,
            field: {
              id: 'MenuField',
              objectType: StringField,
              value: 'menu init'
            }
          }]
        }
      });
      let tabField = form.widget('TabField', StringField);
      let stringField = form.widget('StringField', StringField);
      let menuField = form.widget('MenuField', StringField);
      expect(tabField.value).toBe('tab init');
      expect(stringField.value).toBe('str init');
      expect(menuField.value).toBe('menu init');
      expect(form.saveNeeded).toBe(false);

      tabField.setValue('hi');
      stringField.setValue('you');
      menuField.setValue('there');
      expect(tabField.value).toBe('hi');
      expect(stringField.value).toBe('you');
      expect(menuField.value).toBe('there');
      expect(form.saveNeeded).toBe(true);

      form.reset();
      expect(tabField.value).toBe('tab init');
      expect(stringField.value).toBe('str init');
      expect(menuField.value).toBe('menu init');
      expect(form.saveNeeded).toBe(false);

      tabField.setValue('hi');
      stringField.setValue('you');
      menuField.setValue('there');
      expect(tabField.value).toBe('hi');
      expect(stringField.value).toBe('you');
      expect(menuField.value).toBe('there');
      expect(form.saveNeeded).toBe(true);

      form.markAsSaved();
      expect(tabField.value).toBe('hi');
      expect(stringField.value).toBe('you');
      expect(menuField.value).toBe('there');
      expect(form.saveNeeded).toBe(false);

      form.reset();
      expect(tabField.value).toBe('hi');
      expect(stringField.value).toBe('you');
      expect(menuField.value).toBe('there');
      expect(form.saveNeeded).toBe(false);

      tabField.setValue('fields');
      stringField.setValue('changed');
      menuField.setValue('again');
      expect(tabField.value).toBe('fields');
      expect(stringField.value).toBe('changed');
      expect(menuField.value).toBe('again');
      expect(form.saveNeeded).toBe(true);

      form.reset();
      expect(tabField.value).toBe('hi');
      expect(stringField.value).toBe('you');
      expect(menuField.value).toBe('there');
      expect(form.saveNeeded).toBe(false);
    });
  });

  describe('validate', () => {

    let form: SpecForm;
    let mandatoryStringField: StringField;
    let numberField: NumberField;

    beforeEach(() => {
      form = helper.createFormWithOneField();

      mandatoryStringField = form.rootGroupBox.fields[0] as StringField;
      mandatoryStringField.setMandatory(true);

      numberField = helper.createField(NumberField);
      numberField.setOwner(form.rootGroupBox);
      form.rootGroupBox.insertField(numberField);
    });

    it('returns true if all fields are valid', async () => {
      mandatoryStringField.setValue('whatever');
      numberField.setValue(42);

      const status = await form.validate();
      expect(status.isValid()).toBeTrue();
    });

    it('default invalid box can be disabled', async () => {
      spyOn(form, '_showFormInvalidMessageBox').and.callThrough();
      form.on('invalid', event => event.preventDefault());
      const status = await form.validate();
      expect(status.severity).toBe(Status.Severity.ERROR); // still invalid: the mandatory field is missing
      expect(form._showFormInvalidMessageBox).not.toHaveBeenCalled(); // default handling was skipped
    });

    it('validation status can be modified to ok in listener', async () => {
      spyOn(form, '_createStatusMessageBox').and.callThrough();
      form.on('invalid', event => {
        event.status = Status.ok({message: 'custom'});
      });
      const status = await form.validate();
      expect(status.severity).toBe(Status.Severity.OK); // was modified by listener
      expect(status.message).toBe('custom'); // was modified by listener
      expect(form._createStatusMessageBox).not.toHaveBeenCalled(); // because ok severity creates no message box
    });

    it('validation status can be modified to warning in listener', async () => {
      spyOn(form, '_showFormInvalidMessageBox').and.callFake(status => $.resolvedPromise(status));
      form.on('invalid', event => {
        event.status = Status.warning({message: 'custom'});
      });
      const status = await form.validate();
      expect(status.severity).toBe(Status.Severity.WARNING); // was modified by listener
      expect(status.message).toBe('custom'); // was modified by listener
      expect(form._showFormInvalidMessageBox).toHaveBeenCalledTimes(1);
    });

    it('returns false if mandatory field is empty', done => {
      jasmine.clock().install();

      numberField.setValue(42);

      form.validate()
        .then(status => {
          expect(status.isValid()).toBeFalse();
        })
        .catch(fail)
        .then(done);

      jasmine.clock().tick(1000);
      helper.closeMessageBoxes();
      jasmine.clock().tick(1000);
      jasmine.clock().uninstall();
    });

    it('returns false if field is invalid', done => {
      jasmine.clock().install();

      mandatoryStringField.setValue('whatever');
      numberField.setValue('whatever');

      form.validate()
        .then(status => {
          expect(status.isValid()).toBeFalse();
        })
        .catch(fail)
        .then(done);

      jasmine.clock().tick(1000);
      helper.closeMessageBoxes();
      jasmine.clock().tick(1000);
      jasmine.clock().uninstall();
    });

    it('waits for all validators to complete and returns true if all are valid', done => {
      jasmine.clock().install();

      mandatoryStringField.setValue('whatever');
      numberField.setValue(42);

      const deferred1 = $.Deferred();
      const deferred2 = $.Deferred();
      const deferred3 = $.Deferred();

      form.setValidators([f => deferred1.promise(), f => deferred2.promise(), f => deferred3.promise()]);

      const validate = form.validate();
      validate
        .then(status => {
          expect(status.isValid()).toBeTrue();
        })
        .catch(fail)
        .then(done);

      jasmine.clock().tick(1000);
      expect(validate.state()).toBe('pending');

      deferred1.resolve(Status.ok());
      deferred2.resolve(Status.ok());
      jasmine.clock().tick(1000);
      expect(validate.state()).toBe('pending');

      deferred3.resolve(Status.ok());
      jasmine.clock().tick(1000);
      expect(validate.state()).toBe('resolved');

      jasmine.clock().uninstall();
    });

    it('waits for all validators to complete and returns false if at least one is invalid', done => {
      jasmine.clock().install();

      mandatoryStringField.setValue('whatever');
      numberField.setValue(42);

      const deferred1 = $.Deferred();
      const deferred2 = $.Deferred();
      const deferred3 = $.Deferred();

      form.setValidators([f => deferred1.promise(), f => deferred2.promise(), f => deferred3.promise()]);

      const validate = form.validate();
      validate
        .then(status => {
          expect(status.isValid()).toBeFalse();
        })
        .catch(fail)
        .then(done);

      jasmine.clock().tick(1000);
      expect(validate.state()).toBe('pending');

      deferred1.resolve(Status.ok());
      deferred2.resolve(Status.error());
      jasmine.clock().tick(1000);
      expect(validate.state()).toBe('pending');

      deferred3.resolve(Status.ok());
      jasmine.clock().tick(1000);
      expect(validate.state()).toBe('pending');

      helper.closeMessageBoxes();
      jasmine.clock().tick(1000);
      expect(validate.state()).toBe('resolved');

      jasmine.clock().uninstall();
    });
  });

  describe('error', () => {

    let originalErrorHandlerSession: Session;
    let form: FixtureErrorForm;
    let catchCalled: boolean;

    class FixtureErrorForm extends Form {
      throwInLoad = false;
      throwInPostLoad = false;
      throwInSave = false;

      protected override _load(): JQuery.Promise<object> {
        expect(this.session.desktop.busy).toBe(true);
        if (this.throwInLoad) {
          return $.rejectedPromise('load');
        }
        return $.resolvedPromise({});
      }

      protected override _postLoad(): JQuery.Promise<void> {
        expect(this.session.desktop.busy).toBe(true);
        if (this.throwInPostLoad) {
          return $.rejectedPromise('postLoad');
        }
        return $.resolvedPromise();
      }

      protected override _save(data: object): JQuery.Promise<void> {
        expect(this.session.desktop.busy).toBe(true);
        if (this.throwInSave) {
          return $.rejectedPromise('save');
        }
        return $.resolvedPromise();
      }
    }

    beforeEach(() => {
      jasmine.clock().install();
      catchCalled = false;
      form = scout.create(FixtureErrorForm, {
        parent: session.desktop,
        rootGroupBox: {
          objectType: GroupBox
        }
      });
      // add the session to the error handler in order to show message boxes for errors
      originalErrorHandlerSession = App.get().errorHandler.session;
      App.get().errorHandler.session = session;
      spyOn(App.get().errorHandler, 'handleErrorInfo').and.callThrough();
    });

    afterEach(() => {
      // reset session of the error handler
      App.get().errorHandler.session = originalErrorHandlerSession;
      jasmine.clock().uninstall();
    });

    it('is automatically handled in load', done => {
      expect(session.desktop.busy).toBe(false);

      form.throwInLoad = true;
      form.load()
        .then(fail)
        .catch(e => {
          expect(e).toEqual('load');
          catchCalled = true;
        })
        .always(() => {
          expect(catchCalled).toBe(true);
          expect(App.get().errorHandler.handleErrorInfo).toHaveBeenCalledTimes(1);
          done();
        });

      jasmine.clock().tick(1000);

      const messageBoxes = helper.findMessageBoxes();
      expect(messageBoxes.size).toBe(1);
      expect(session.desktop.busy).toBe(false);

      const messageBox: MessageBox = messageBoxes.values().next().value;
      expect(messageBox.$container.children('.glasspane').length).toBe(0); // not blocked

      helper.closeMessageBoxes();
      jasmine.clock().tick(1000);
    });

    it('is automatically handled in postLoad', done => {
      form.throwInPostLoad = true;
      form.on('error', e => {
        expect(e.phase).toBe('postLoad');
        expect(e.error).toBe('postLoad');
        done();
      });
      form.load()
        .catch(fail); // error in postLoad does not reject the load promise
      jasmine.clock().tick(1000);
    });

    it('is automatically handled in save', done => {
      expect(session.desktop.busy).toBe(false);

      form.throwInSave = true;
      form.load()
        .then(() => {
          form.touch();
          form.ok()
            .then(fail)
            .catch(e => {
              catchCalled = true;
              expect(e).toEqual('save');
            })
            .always(() => {
              expect(catchCalled).toBe(true);
              expect(form.formSaved).toBe(false); // save failed: do not mark as stored
              expect(App.get().errorHandler.handleErrorInfo).toHaveBeenCalledTimes(1);
              done();
            });
        })
        .catch(fail);

      jasmine.clock().tick(1000);

      const messageBoxes = helper.findMessageBoxes();
      expect(messageBoxes.size).toBe(1);
      expect(session.desktop.busy).toBe(false);

      const messageBox: MessageBox = messageBoxes.values().next().value;
      expect(messageBox.$container.children('.glasspane').length).toBe(0); // not blocked

      helper.closeMessageBoxes();
      jasmine.clock().tick(1000);
    });

    it('load error handling can be exchanged', done => {
      form.throwInLoad = true;
      let numHandled = 0;
      form.on('error', e => {
        numHandled++;
        e.preventDefault(); // disable default error handling
      });
      form.load()
        .then(fail)
        .catch(e => {
          catchCalled = true;
          expect(form.session.desktop.busy).toBe(false);
          expect(e).toBe('load');
          expect(numHandled).toBe(1);
        })
        .always(() => {
          expect(catchCalled).toBe(true);
          expect(App.get().errorHandler.handleErrorInfo).not.toHaveBeenCalled();
          done();
        });
      jasmine.clock().tick(1000);
    });

    it('save error handling can be exchanged', done => {
      form.throwInSave = true;
      let numHandled = 0;
      form.on('error', e => {
        numHandled++;
        expect(e.phase).toBe('save');
        e.preventDefault(); // disable default error handling
      });
      form.load()
        .then(() => {
          form.touch();
          form.ok()
            .then(fail)
            .catch(e => {
              catchCalled = true;
              expect(form.session.desktop.busy).toBe(false);
              expect(e).toEqual('save');
              expect(numHandled).toBe(1);
            })
            .always(() => {
              expect(catchCalled).toBe(true);
              expect(App.get().errorHandler.handleErrorInfo).not.toHaveBeenCalled();
              done();
            });
        })
        .catch(fail);
      jasmine.clock().tick(1000);
    });
  });

  describe('aria properties', () => {

    it('has aria role dialog if it is a dialog', () => {
      let form = scout.create(Form, {
        parent: session.desktop,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [{
            objectType: GroupBox
          }]
        }
      });
      form.setDisplayHint(Form.DisplayHint.DIALOG);
      form.render();
      expect(form.$container).toHaveAttr('role', 'dialog');
    });

    it('has aria role form if it is a view', () => {
      let form = scout.create(Form, {
        parent: session.desktop,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [{
            objectType: GroupBox
          }]
        }
      });
      form.setDisplayHint(Form.DisplayHint.VIEW);
      form.render();
      expect(form.$container).toHaveAttr('role', 'form');
    });

    it('has aria-labelledby set if form is a dialog', () => {
      let form = scout.create(Form, {
        parent: session.desktop,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [{
            objectType: GroupBox
          }]
        }
      });
      form.setTitle('testTitle');
      form.setSubTitle('testSubTitle');
      form.setDisplayHint(Form.DisplayHint.DIALOG);
      form.render();
      expect(form.$container.attr('aria-labelledby')).toBeTruthy();
      expect(form.$container).toHaveAttr('aria-labelledby', strings.join(' ', form.$title.attr('id'), form.$subTitle.attr('id')));
      expect(form.$container.attr('aria-label')).toBeFalsy();
    });

    it('has aria-label set if form is a view', () => {
      let form = scout.create(Form, {
        parent: session.desktop,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [{
            objectType: GroupBox
          }]
        }
      });
      form.setTitle('testTitle');
      form.setSubTitle('testSubTitle');
      form.setDisplayHint(Form.DisplayHint.VIEW);
      form.render();
      expect(form.$container.attr('aria-label')).toBeTruthy();
      expect(form.$container).toHaveAttr('aria-label', 'testTitle testSubTitle');
      expect(form.$container.attr('aria-labelledby')).toBeFalsy();
    });

    it('has aria-label set if form is a pop up', () => {
      let form = scout.create(Form, {
        parent: session.desktop,
        rootGroupBox: {
          objectType: GroupBox,
          fields: [{
            objectType: GroupBox
          }]
        }
      });
      form.setTitle('testTitle');
      form.setSubTitle('testSubTitle');
      form.setDisplayHint(Form.DisplayHint.POPUP_WINDOW);
      form.render();
      expect(form.$container.attr('aria-label')).toBeTruthy();
      expect(form.$container).toHaveAttr('aria-label', 'testTitle testSubTitle');
      expect(form.$container.attr('aria-labelledby')).toBeFalsy();
    });
  });

  describe('form with displayHint = POPUP_WINDOW', () => {
    class PreventOpenPopupBlockHandler extends PopupBlockerHandler {
      override openWindow(uri: string, windowName?: string, windowSpecs?: string, onWindowOpened?: (popup: Window) => void) {
        // Do nothing to not open the popup and also prevent logging a warning and opening of the desktop notification
      }
    }

    beforeEach(() => {
      ObjectFactory.get().register(PopupBlockerHandler, () => new PreventOpenPopupBlockHandler());
    });

    afterEach(() => {
      ObjectFactory.get().unregister(PopupBlockerHandler);
    });

    it('can be closed without error even if open failed due to popup blocker', async () => {
      let form = scout.create(Form, {parent: session.desktop, displayHint: Form.DisplayHint.POPUP_WINDOW});
      await form.open();
      form.close();
      expect(form.destroyed).toBe(true);
    });

    it('can be hidden without error even if show failed due to popup blocker', () => {
      let form = scout.create(Form, {parent: session.desktop, displayHint: Form.DisplayHint.POPUP_WINDOW});
      form.show();
      form.hide();
      expect(form.rendered).toBe(false);
    });
  });
});
