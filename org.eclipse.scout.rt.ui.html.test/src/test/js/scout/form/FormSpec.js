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
describe('Form', function() {
  var session, helper, outlineHelper;

  function closeMessageBox() {
    if (session && session.$entryPoint) {
      session.$entryPoint.find('.messagebox .box-button').click();
    }
  }

  beforeEach(function() {
    setFixtures(sandbox());
    jasmine.Ajax.install();
    session = sandboxSession({
      desktop: {
        headerVisible: true,
        navigationVisible: true,
        benchVisible: true
      }
    });
    helper = new scout.FormSpecHelper(session);
    outlineHelper = new scout.OutlineSpecHelper(session);
    uninstallUnloadHandlers(session);
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
  });

  describe('init', function() {
    it('marks the root group box as main box', function() {
      var form = scout.create('Form', {
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

  describe('open', function() {

    it('opens the form', function(done) {
      var form = helper.createFormWithOneField();
      form.open()
        .then(function() {
          expect(form.rendered).toBe(true);
          expect(session.desktop.dialogs.indexOf(form) > -1).toBe(true);
        })
        .catch(fail)
        .always(done);
    });

    it('adds it to the desktop if no display parent is provided', function(done) {
      var form = helper.createFormWithOneField();
      form.open()
        .then(function() {
          expect(session.desktop.dialogs.indexOf(form) > -1).toBe(true);
        })
        .catch(fail)
        .always(done);
    });

    it('adds it to the provided display parent', function(done) {
      var parentForm = helper.createFormWithOneField();
      var form = helper.createFormWithOneField();
      form.displayParent = parentForm;
      parentForm.open()
        .then(form.open.bind(form))
        .then(function() {
          expect(form.rendered).toBe(true);
          expect(session.desktop.dialogs.indexOf(form) > -1).toBe(false);
          expect(parentForm.dialogs.indexOf(form) > -1).toBe(true);
        })
        .catch(fail)
        .always(done);
    });
  });

  describe('close', function() {

    it('closes the form', function(done) {
      var form = helper.createFormWithOneField();
      form.open()
        .then(function() {
          form.close();
          expect(session.desktop.dialogs.indexOf(form) > -1).toBe(false);
          expect(form.rendered).toBe(false);
          expect(form.destroyed).toBe(true);
        })
        .catch(fail)
        .always(done);
    });

    it('closes the form even if opening is still pending', function(done) {
      var form = helper.createFormWithOneField();
      form.open()
        .then(function() {
          expect(session.desktop.dialogs.indexOf(form) > -1).toBe(false);
          expect(form.rendered).toBe(false);
          expect(form.destroyed).toBe(true);
        })
        .catch(fail)
        .always(done);

      // Close immediately without waiting for the open promise to resolve
      form.close();
    });

    it('removes it from the display parent', function(done) {
      var parentForm = helper.createFormWithOneField();
      var form = helper.createFormWithOneField();
      form.displayParent = parentForm;
      parentForm.open()
        .then(form.open.bind(form))
        .then(function() {
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

  describe('whenClose', function() {

    it('returns a promise which is resolved when the form is closed', function(done) {
      var form = helper.createFormWithOneField();
      form.open()
        .then(function() {
          form.close();
        })
        .catch(fail);

      form.whenClose()
        .then(function() {
          expect(form.rendered).toBe(false);
          expect(form.destroyed).toBe(true);
        })
        .catch(fail)
        .always(done);
    });

  });

  describe('save', function() {

    it('calls _save', function(done) {
      var form = helper.createFormWithOneField();
      var saveCalled = false;
      form._save = function(data) {
        saveCalled = true;
        return $.resolvedPromise();
      };

      form.touch();
      form.save()
        .then(function() {
          expect(saveCalled).toBe(true);
        })
        .catch(fail)
        .always(done);
    });

    it('is marked saved after save', function(done) {
      var form = helper.createFormWithOneField();
      var field = form.rootGroupBox.fields[0];

      field.setValue('whatever');
      form.save()
        .then(function() {
          // it should be marked saved as the save was successful
          expect(field.touched).toBe(false);
        })
        .catch(fail)
        .always(done);
    });

    it('is not marked saved on error', function(done) {
      jasmine.clock().install();
      var form = helper.createFormWithOneField();
      var field = form.rootGroupBox.fields[0];

      form._save = function(data) {
        return $.resolvedPromise(scout.Status.error());
      };

      field.setValue('whatever');
      form.save()
        .then(function() {
          // it should not be marked saved because the save returned an error
          expect(field.touched).toBe(true);
        })
        .catch(fail)
        .always(done);
      jasmine.clock().tick(1000);
      closeMessageBox();
      jasmine.clock().tick(1000);
      jasmine.clock().uninstall();
    });

    it('does not call save if save is not required', function(done) {
      var form = helper.createFormWithOneField();
      var saveCalled = false;
      form._save = function(data) {
        saveCalled = true;
        return $.resolvedPromise();
      };

      // form.touch() has not been called -> _save must not be called
      form.save()
        .then(function() {
          expect(saveCalled).toBe(false);
        })
        .catch(fail)
        .always(done);
    });

  });

  describe('whenSave', function() {

    it('returns a promise which is resolved when the form is saved', function(done) {
      var form = helper.createFormWithOneField();
      var saveCalled = false;
      form._save = function(data) {
        saveCalled = true;
        return $.resolvedPromise();
      };

      form.whenSave()
        .then(function() {
          expect(saveCalled).toBe(true);
        })
        .catch(fail)
        .always(done);

      form.touch();
      form.save();
    });

  });

  describe('abort', function() {

    it('closes the form if there is a close button', function(done) {
      var form = scout.create('Form', {
        parent: session.desktop,
        rootGroupBox: {
          objectType: 'GroupBox',
          menus: [{
            objectType: 'CloseMenu',
          }]
        }
      });
      spyOn(form, 'close').and.callThrough();
      spyOn(form, 'cancel').and.callThrough();
      form.open()
        .then(function() {
          form.abort();
          expect(form.close.calls.count()).toEqual(1);
          expect(form.cancel.calls.count()).toEqual(0);
          expect(form.destroyed).toBe(true);
        })
        .catch(fail)
        .always(done);
    });

    it('closes the form even if opening is still pending', function(done) {
      var form = scout.create('Form', {
        parent: session.desktop,
        rootGroupBox: {
          objectType: 'GroupBox',
          menus: [{
            objectType: 'CloseMenu',
          }]
        }
      });
      spyOn(form, 'close').and.callThrough();
      spyOn(form, 'cancel').and.callThrough();
      form.open()
        .then(function() {
          expect(form.close.calls.count()).toEqual(1);
          expect(form.cancel.calls.count()).toEqual(0);
          expect(form.destroyed).toBe(true);
        })
        .catch(fail)
        .always(done);

      // Abort immediately without waiting for the open promise to resolve
      form.abort();
    });

    it('closes the form by using cancel if there is no close button', function(done) {
      var form = scout.create('Form', {
        parent: session.desktop,
        rootGroupBox: {
          objectType: 'GroupBox'
        }
      });
      spyOn(form, 'close').and.callThrough();
      spyOn(form, 'cancel').and.callThrough();
      form.open()
        .then(function() {
          form.abort();
          expect(form.close.calls.count()).toEqual(0);
          expect(form.cancel.calls.count()).toEqual(1);
          expect(form.destroyed).toBe(true);
        })
        .catch(fail)
        .always(done);
    });
  });

  describe('destroy', function() {

    it('destroys its children', function() {
      var form = helper.createFormWithOneField();

      expect(form.rootGroupBox).toBeTruthy();
      expect(form.rootGroupBox.fields[0]).toBeTruthy();

      form.destroy();
      expect(form.rootGroupBox.destroyed).toBeTruthy();
      expect(form.rootGroupBox.fields[0].destroyed).toBeTruthy();
    });

    it('does not fail on form close if a field has focus and validation wants to show a warning', function(done) {
      var form = helper.createFormWithOneField();
      form.setDisplayHint(scout.Form.DisplayHint.DIALOG);
      var field = helper.createField('DateField');
      field.setValidator(function(value) {
        throw scout.Status.warning({
          message: 'Invalid value'
        });
      });
      field.getValidationResult = function() {
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
        .then(function() {
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

  describe('cacheBounds', function() {

    var form;

    beforeEach(function() {
      form = helper.createFormWithOneField();
      form.cacheBounds = true;
      form.cacheBoundsKey = 'FOO';
      form.render();

      scout.webstorage.removeItem(localStorage, 'scout:formBounds:FOO');
    });

    it('read and store bounds', function() {
      // should return null when local storage not contains the requested key
      expect(form.readCacheBounds()).toBe(null);

      // should return the stored Rectangle
      var storeBounds = new scout.Rectangle(0, 1, 2, 3);
      form.storeCacheBounds(storeBounds);
      var readBounds = form.readCacheBounds();
      expect(readBounds).toEqual(storeBounds);
    });

    it('update bounds - if cacheBounds is true', function() {
      form.updateCacheBounds();
      expect(form.readCacheBounds() instanceof scout.Rectangle).toBe(true);
    });

    it('update bounds - if cacheBounds is false', function() {
      form.cacheBounds = false;
      form.updateCacheBounds();
      expect(form.readCacheBounds()).toBe(null);
    });

  });

  describe('modal', function() {

    it('creates a glass pane if true', function(done) {
      var form = helper.createFormWithOneField({
        modal: true
      });
      form.open()
        .then(function() {
          expect($('.glasspane').length).toBe(3);
          form.close();
          expect($('.glasspane').length).toBe(0);
        })
        .catch(fail)
        .always(done);
    });

    it('does not create a glass pane if false', function(done) {
      var form = helper.createFormWithOneField({
        modal: false
      });
      form.open()
        .then(function() {
          expect($('.glasspane').length).toBe(0);
          form.close();
          expect($('.glasspane').length).toBe(0);
        })
        .catch(fail)
        .always(done);
    });

  });

  describe('displayParent', function() {
    var desktop;

    beforeEach(function() {
      desktop = session.desktop;
    });

    it('is required if form is managed by a form controller, defaults to desktop', function(done) {
      var form = helper.createFormWithOneField();
      expect(form.displayParent).toBe(null);
      form.open()
        .then(function() {
          expect(form.displayParent).toBe(desktop);
          form.close();
        })
        .catch(fail)
        .always(done);
    });

    it('is not required if form is just rendered', function() {
      var form = helper.createFormWithOneField();
      expect(form.displayParent).toBe(null);
      form.render();
      expect(form.displayParent).toBe(null);
      form.destroy();
    });

    it('same as parent if display parent is set', function(done) {
      // Parent would be something different, removing the parent would remove the form which is not expected, because only removing the display parent has to remove the form
      var initialParent = new scout.NullWidget();
      var form = helper.createFormWithOneField({
        parent: initialParent,
        session: session
      });
      expect(form.displayParent).toBe(null);
      expect(form.parent).toBe(initialParent);
      form.open()
        .then(function() {
          expect(form.displayParent).toBe(desktop);
          expect(form.parent).toBe(desktop);
          form.close();
        })
        .catch(fail)
        .always(done);
    });

    it('not same as parent if display parent is outline', function(done) {
      // Parent must not be outline if display parent is outline, otherwise making the outline invisible would remove the form, which is not expected. See also DesktopSpec
      var outline = outlineHelper.createOutlineWithOneDetailForm();
      desktop.setOutline(outline);
      var initialParent = new scout.NullWidget();
      var form = helper.createFormWithOneField({
        parent: initialParent,
        session: session,
        displayParent: outline
      });
      expect(form.displayParent).toBe(outline);
      expect(form.parent).toBe(desktop);
      form.open()
        .then(function() {
          expect(form.displayParent).toBe(outline);
          expect(form.parent).toBe(desktop);
          form.close();
        })
        .catch(fail)
        .always(done);
    });

    it('blocks desktop if modal and displayParent is desktop', function(done) {
      var form = helper.createFormWithOneField({
        modal: true,
        displayParent: desktop
      });
      form.open()
        .then(function() {
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

    it('blocks detail form and outline if modal and displayParent is outline', function(done) {
      var outline = outlineHelper.createOutlineWithOneDetailForm();
      desktop.setOutline(outline);
      outline.selectNodes(outline.nodes[0]);
      var form = helper.createFormWithOneField({
        displayHint: scout.Form.DisplayHint.DIALOG,
        modal: true,
        displayParent: outline
      });
      form.open()
        .then(function() {
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

    it('blocks form if modal and displayParent is form', function(done) {
      var outline = outlineHelper.createOutlineWithOneDetailForm();
      var detailForm = outline.nodes[0].detailForm;
      desktop.setOutline(outline);
      outline.selectNodes(outline.nodes[0]);
      var form = helper.createFormWithOneField({
        modal: true,
        displayParent: detailForm
      });
      form.open()
        .then(function() {
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

  describe('rootGroupBox.gridData', function() {
    it('is created using gridDataHints when the logical grid is validated', function() {
      var form = scout.create('Form', {
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

  describe('initialFocus', function() {
    it('references the widget which should gain focus after the form is displayed', function() {
      var form = scout.create('Form', {
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
      // TODO aho fix this test
      //      expect(form.widget('tabItem1').isFocused()).toBe(true);

      // InitialFocus property must not modify parent of tab items
      expect(form.widget('tabItem1').parent).toBe(form.widget('tabBox'));
      expect(form.widget('tabItem2').parent).toBe(form.widget('tabBox'));
    });

    it('works correctly even for wrapped forms', function(done) {
      var form = scout.create({
        parent: session.desktop,
        objectType: 'Form',
        displayHint: scout.Form.DisplayHint.VIEW,
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
        .then(function() {
          expect(form.widget('Field2').$field).toBeFocused();
          form.close();
        })
        .catch(fail)
        .always(done);
    });
  });

  describe('restore focus', function() {

    var outlineHelper, desktop;

    beforeEach(function() {
      desktop = session.desktop;
      outlineHelper = new scout.OutlineSpecHelper(session);
    });

    /**
     * Scenario: Switch between two outline nodes and expect the focus in its detail forms are preserved.
     */
    it('on detail forms', function() {
      // setup an outline with 2 nodes each node has a detail form with 3 fields
      var model = outlineHelper.createModelFixture(2, 0, true);
      var outline = outlineHelper.createOutline(model);
      outline.nodes.forEach(function(node) {
        node.detailForm = helper.createFormWithFields(desktop, false, 3);
        node.detailForm.nodeText = node.text;
        node.detailForm.initialFocus = node.detailForm.rootGroupBox.fields[1];
        node.detailFormVisible = true;
      }, this);

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

});
