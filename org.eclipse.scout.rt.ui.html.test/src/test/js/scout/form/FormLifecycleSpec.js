/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('FormLifecycle', function() {

  var session, helper, form, field;

  function expectMessageBox(shown) {
    expect(session.$entryPoint.find('.messagebox').length).toBe(shown ? 1 : 0);
  }

  function closeMessageBox() {
    session.$entryPoint.find('.messagebox .box-button').click();
  }

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);

    form = helper.createFormWithOneField();
    form.lifecycle = scout.create('FormLifecycle', {widget: form});
    field = form.rootGroupBox.fields[0];
    form.render();
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
  });

  describe('cancel', function() {

    it('don\'t open popup when nothing has been changed', function() {
      form.lifecycle.cancel();
      expectMessageBox(false);
    });

    it('open popup when value of field has been changed', function() {
      field.setValue('Foo');
      form.lifecycle.cancel();
      expectMessageBox(true);
    });

    it('triggers close event after cancel', function() {
      var disposed = false;
      form.lifecycle.on('close', function() {
        disposed = true;
      });
      form.lifecycle.cancel();
      jasmine.clock().tick();
      expect(disposed).toBe(true);
    });

  });

  describe('ok', function() {

    it('should validate fields and display message box when form is saved', function() {
      field.setMandatory(true);
      field.setValue(null);
      form.lifecycle.ok();
      jasmine.clock().tick();
      expectMessageBox(true);
    });

    it('should call save handler when form is saved and all fields are valid', function() {
      var saved = false;
      field.setMandatory(true);
      field.setValue('Foo');
      form.lifecycle.handle('save', function() {
        saved = true;
        return $.resolvedPromise(scout.Status.ok());
      });
      form.lifecycle.ok();
      jasmine.clock().tick(1000);
      expectMessageBox(false);
      expect(saved).toBe(true);
    });

    it('stops lifecycle if severity is ERROR', function() {
      var form2 = helper.createFormWithOneField();
      form2.lifecycle = scout.create('FormLifecycle', {
        widget: form
      });
      form2.lifecycle._validate = function() {
        return $.resolvedPromise(scout.Status.error({
          message: 'This is a fatal error'
        }));
      }.bind(form2.lifecycle);
      var lifecycleComplete = false;
      form2.lifecycle.on('close', function() {
        lifecycleComplete = true;
      });
      form2.render();
      form2.lifecycle.ok();
      jasmine.clock().tick();
      expectMessageBox(true);
      closeMessageBox();
      jasmine.clock().tick(1000); // <- important, otherwise the promise will not be resolved somehow (?)
      expect(lifecycleComplete).toBe(false);
    });

    it('continues lifecycle if severity is WARNING', function() {
      var form2 = helper.createFormWithOneField();
      form2.lifecycle = scout.create('FormLifecycle', {
        widget: form
      });
      form2.lifecycle._validate = function() {
        return $.resolvedPromise(scout.Status.warn({
          message: 'This is only a warning'
        }));
      }.bind(form2.lifecycle);
      var lifecycleComplete = false;
      form2.lifecycle.on('close', function() {
        lifecycleComplete = true;
      });
      form2.render();
      form2.lifecycle.ok();
      jasmine.clock().tick();
      expectMessageBox(true);
      closeMessageBox();
      jasmine.clock().tick(1000); // <- important, otherwise the promise will not be resolved somehow (?)
      expect(lifecycleComplete).toBe(true);
    });

  });

  describe('validation error message', function() {

    it('should list labels of missing and invalid fields', function() {
      field.setLabel('FooField');
      var missingFields = [field];
      var invalidField = helper.createField('StringField', session.desktop);
      invalidField.setLabel('BarField');
      var invalidFields = [invalidField];
      var html = form.lifecycle._createInvalidElementsMessageHtml(missingFields, invalidFields);
      expect(html).toContain('FooField');
      expect(html).toContain('BarField');
    });

  });

});
