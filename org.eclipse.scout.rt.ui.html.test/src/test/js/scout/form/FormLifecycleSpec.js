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
describe('FormLifecycle', function() {

  var session, helper, form, field;

  function expectMessageBox(shown) {
    expect(session.$entryPoint.find('.messagebox').length).toBe(shown ? 1 : 0);
  }

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);

    form = helper.createFormWithOneField();
    form.lifecycle = scout.create('FormLifecycle', form);
    field = form.rootGroupBox.fields[0];
    form.render(session.$entryPoint);
  });

  describe('doCancel', function() {

    it('don\'t open popup when nothing has been changed', function() {
      form.lifecycle.doCancel();
      expectMessageBox(false);
    });

    it('open popup when value of field has been changed', function() {
      field.setValue('Foo');
      form.lifecycle.doCancel();
      expectMessageBox(true);
    });

    it('triggers disposeForm event after cancel', function() {
      var disposed = false;
      form.lifecycle.on('disposeForm', function() {
        disposed = true;
      });
      form.lifecycle.doCancel();
      expect(disposed).toBe(true);
    });

  });

  describe('doOk', function() {

    it('should validate fields and display message box when form is saved', function() {
      field.setMandatory(true);
      field.setValue(null);
      form.lifecycle.doOk();
      expectMessageBox(true);
    });

    it('should call save handler when form is saved and all fields are valid', function() {
      var saved = false;
      field.setMandatory(true);
      field.setValue('Foo');
      form.lifecycle.handle('save', function() {
        saved = true;
        return scout.Status.ok();
      });
      form.lifecycle.doOk();
      expectMessageBox(false);
      expect(saved).toBe(true);
    });

  });

  describe('validation error message', function() {

    it('should list labels of missing and invalid fields', function() {
      field.setLabel('FooField');
      var missingFields = [field];
      var invalidField = helper.createField('StringField', session.desktop);
      invalidField.setLabel('BarField');
      var invalidFields = [invalidField];
      var html = form.lifecycle._createInvalidFieldsMessageHtml(missingFields, invalidFields);
      expect(html).toContain('FooField');
      expect(html).toContain('BarField');
    });

  });

});
