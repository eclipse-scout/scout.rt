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
import {FormSpecHelper} from '../../../src/testing/index';
import {scout, Status} from '../../../src';

describe('FormLifecycle', () => {

  let session, helper, form, field;

  function expectMessageBox(shown) {
    expect(session.$entryPoint.find('.messagebox').length).toBe(shown ? 1 : 0);
  }

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);

    form = helper.createFormWithOneField();
    form.lifecycle = scout.create('FormLifecycle', {widget: form});
    field = form.rootGroupBox.fields[0];
    form.render();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  describe('cancel', () => {

    it('don\'t open popup when nothing has been changed', () => {
      form.lifecycle.cancel();
      expectMessageBox(false);
    });

    it('open popup when value of field has been changed', () => {
      field.setValue('Foo');
      form.lifecycle.cancel();
      expectMessageBox(true);
    });

    it('triggers close event after cancel', () => {
      let disposed = false;
      form.lifecycle.on('close', () => {
        disposed = true;
      });
      form.lifecycle.cancel();
      jasmine.clock().tick();
      expect(disposed).toBe(true);
    });

  });

  describe('ok', () => {

    it('should validate fields and display message box when form is saved', () => {
      field.setMandatory(true);
      field.setValue(null);
      form.lifecycle.ok();
      jasmine.clock().tick();
      expectMessageBox(true);
    });

    it('should call save handler when form is saved and all fields are valid', () => {
      let saved = false;
      field.setMandatory(true);
      field.setValue('Foo');
      form.lifecycle.handle('save', () => {
        saved = true;
        return $.resolvedPromise(Status.ok());
      });
      form.lifecycle.ok();
      jasmine.clock().tick(1000);
      expectMessageBox(false);
      expect(saved).toBe(true);
    });

    it('stops lifecycle if severity is ERROR', () => {
      let form2 = helper.createFormWithOneField();
      form2.lifecycle = scout.create('FormLifecycle', {
        widget: form
      });
      form2.lifecycle._validate = () => $.resolvedPromise(Status.error({
        message: 'This is a fatal error'
      }));
      runTestWithLifecycleOk(form2, false);
    });

    it('continues lifecycle if severity is WARNING', () => {
      let form2 = helper.createFormWithOneField();
      form2.lifecycle = scout.create('FormLifecycle', {
        widget: form
      });
      form2.lifecycle._validate = () => $.resolvedPromise(Status.warning({
        message: 'This is only a warning'
      }));
      runTestWithLifecycleOk(form2, true);
    });

    function runTestWithLifecycleOk(form2, expected, render = true) {
      let lifecycleComplete = false;
      form2.lifecycle.on('close', () => {
        lifecycleComplete = true;
      });
      if (render) {
        form2.render();
      }
      form2.lifecycle.ok();
      jasmine.clock().tick();
      expectMessageBox(true);
      helper.closeMessageBoxes();
      jasmine.clock().tick(1000); // <- important, otherwise the promise will not be resolved somehow (?)
      expect(lifecycleComplete).toBe(expected);
    }

    it('should call _validate function on form', () => {
      // validate should always be called, even when there is not a single touched field in the form
      let form2 = helper.createFormWithOneField();
      form2.lifecycle = scout.create('FormLifecycle', {
        widget: form2
      });
      let validateCalled = false;
      Object.getPrototypeOf(form2)._validate = () => {
        validateCalled = true;
        return Status.ok();
      };
      form2.ok();
      expect(validateCalled).toBe(true);

      // validate should not be called when there is an invalid field (field is mandatory but empty in this case)
      validateCalled = false;
      let formField = form2.rootGroupBox.fields[0];
      formField.touch();
      formField.setMandatory(true);
      form2.ok();
      expect(validateCalled).toBe(false);
    });

    it('should focus first invalid element', () => {
      let formWithFieldsAndTabBoxes = helper.createFormWithFieldsAndTabBoxes();
      formWithFieldsAndTabBoxes.lifecycle = scout.create('FormLifecycle', {
        widget: formWithFieldsAndTabBoxes
      });

      let field3 = formWithFieldsAndTabBoxes.widget('Field3'),
        field4 = formWithFieldsAndTabBoxes.widget('Field4'),
        tabBox = formWithFieldsAndTabBoxes.widget('TabBox'),
        tabA = formWithFieldsAndTabBoxes.widget('TabA'),
        fieldA2 = formWithFieldsAndTabBoxes.widget('FieldA2'),
        tabBoxA = formWithFieldsAndTabBoxes.widget('TabBoxA'),
        tabAA = formWithFieldsAndTabBoxes.widget('TabAA'),
        fieldAA2 = formWithFieldsAndTabBoxes.widget('FieldAA2'),
        tabAB = formWithFieldsAndTabBoxes.widget('TabAB'),
        fieldAB2 = formWithFieldsAndTabBoxes.widget('FieldAB2'),
        fieldAC2 = formWithFieldsAndTabBoxes.widget('FieldAC2'),
        tabB = formWithFieldsAndTabBoxes.widget('TabB'),
        fieldB3 = formWithFieldsAndTabBoxes.widget('FieldB3'),
        fieldB4 = formWithFieldsAndTabBoxes.widget('FieldB4'),
        tableFieldB5 = formWithFieldsAndTabBoxes.widget('TableFieldB5'),
        tableFieldB5Table = tableFieldB5.table,
        columnB52 = tableFieldB5Table.columns.filter(col => col.id === 'ColumnB52')[0],
        tableFieldB5TableRows = tableFieldB5Table.rows;

      field3.setValue('something');
      fieldAA2.setValue('something');
      fieldAC2.setValue('something');
      fieldB3.setValue('something');
      tableFieldB5Table.cell(columnB52, tableFieldB5TableRows[0]).setValue('something');

      expect(tabBox.selectedTab).toBe(tabA);
      expect(tabBoxA.selectedTab).toBe(tabAA);

      runTestWithLifecycleOk(formWithFieldsAndTabBoxes, false);

      expect(field4.focused).toBe(true);
      expect(tabBox.selectedTab).toBe(tabA);
      expect(tabBoxA.selectedTab).toBe(tabAA);

      field4.setValue('something');

      runTestWithLifecycleOk(formWithFieldsAndTabBoxes, false, false);

      expect(fieldA2.focused).toBe(true);
      expect(tabBox.selectedTab).toBe(tabA);
      expect(tabBoxA.selectedTab).toBe(tabAA);

      fieldA2.setValue('something');

      runTestWithLifecycleOk(formWithFieldsAndTabBoxes, false, false);

      expect(fieldAB2.focused).toBe(true);
      expect(tabBox.selectedTab).toBe(tabA);
      expect(tabBoxA.selectedTab).toBe(tabAB);

      fieldAB2.setValue('something');

      runTestWithLifecycleOk(formWithFieldsAndTabBoxes, false, false);

      expect(fieldB4.focused).toBe(true);
      expect(tabBox.selectedTab).toBe(tabB);
      expect(tabBoxA.selectedTab).toBe(tabAB);

      fieldB4.setValue('something');

      runTestWithLifecycleOk(formWithFieldsAndTabBoxes, false, false);

      let cell = tableFieldB5Table.cell(columnB52, tableFieldB5TableRows[1]);
      expect(cell.value).toBeNull();
      expect(cell.field).not.toBeNull();
      expect(cell.field.value).toBeNull();
      expect(tabBox.selectedTab).toBe(tabB);
      expect(tabBoxA.selectedTab).toBe(tabAB);

      cell.field.setValue('something');
      tableFieldB5Table.completeCellEdit();

      formWithFieldsAndTabBoxes.lifecycle.ok();
      jasmine.clock().tick();
      expectMessageBox(false);
    });
  });

  describe('load', () => {

    beforeEach(() => {
      jasmine.clock().uninstall(); // we don't need a mock-clock for this test
    });

    it('should handle errors that occur in promise', done => {
      let form = helper.createFormWithOneField();
      form._load = () => {
        return $.resolvedPromise()
          .then(() => {
            throw 'Something went wrong';
          });
      };
      form.render();
      form.load().catch(error => {
        expect(form.destroyed).toBe(true);
      }).always(done);
    });

    /**
     * Errors that are thrown directly in the _load function should not be wrapped into a Promise
     * and must be catched with try/catch or are finally handled by the global error handler.
     */
    it('should handle errors that occur in _load function', done => {
      let form = helper.createFormWithOneField();
      let error = null;
      form._load = () => {
        throw 'Something went wrong';
      };
      form.render();
      try {
        form.load();
      } catch (error0) {
        error = error0;
      }
      expect(form.destroyed).toBe(true);
      expect(error).toBe('Something went wrong');
      done();
    });

  });

  describe('validation error message', () => {

    it('should list labels of missing and invalid fields', () => {
      field.setLabel('FooField');
      let missingFields = [{
        valid: false,
        validByErrorStatus: false,
        validByMandatory: true,
        field: field,
        label: field.label,
        reveal: () => {
        }
      }];
      let invalidField = helper.createField('StringField', session.desktop);
      invalidField.setLabel('BarField');
      let invalidFields = [{
        valid: false,
        validByErrorStatus: true,
        validByMandatory: false,
        field: invalidField,
        label: invalidField.label,
        reveal: () => {
        }
      }];
      let html = form.lifecycle._createInvalidElementsMessageHtml(missingFields, invalidFields);
      expect(html).toContain('FooField');
      expect(html).toContain('BarField');
    });

  });
});
