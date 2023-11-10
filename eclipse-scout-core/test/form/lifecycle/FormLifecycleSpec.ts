/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormSpecHelper, SpecForm, SpecLifecycle} from '../../../src/testing/index';
import {FormField, FormLifecycle, GroupBox, MessageBox, scout, Status, StringField, TabBox, TabItem, TableField, TreeVisitResult, ValidationResult} from '../../../src/index';
import $ from 'jquery';

describe('FormLifecycle', () => {

  let session: SandboxSession, helper: FormSpecHelper, form: SpecForm, field: StringField;

  function expectMessageBox(shown: boolean) {
    expect(session.$entryPoint.find('.messagebox').length).toBe(shown ? 1 : 0);
  }

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);

    form = helper.createFormWithOneField();
    form.lifecycle = scout.create(SpecLifecycle, {widget: form});
    field = form.rootGroupBox.fields[0] as StringField;
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
      jasmine.clock().tick(0);
      expect(disposed).toBe(true);
    });
  });

  describe('ok', () => {

    it('should validate fields and display message box when form is saved', () => {
      field.setMandatory(true);
      field.setValue(null);
      form.lifecycle.ok();
      jasmine.clock().tick(10);
      expectMessageBox(true);
    });

    it('should call save handler when form is saved and all fields are valid', () => {
      let saved = false;
      field.setMandatory(true);
      field.setValue('Foo');
      form.lifecycle.handle('save', () => {
        saved = true;
        return $.resolvedPromise();
      });
      form.lifecycle.ok();
      jasmine.clock().tick(1000);
      expectMessageBox(false);
      expect(saved).toBe(true);
    });

    it('stops lifecycle if severity is ERROR', () => {
      let form2 = helper.createFormWithOneField() as SpecForm;
      form2.lifecycle = scout.create(SpecLifecycle, {
        widget: form
      });
      form2.lifecycle._validate = () => $.resolvedPromise(Status.error({
        message: 'This is a fatal error'
      }));
      runTestWithLifecycleOk(form2, false);
    });

    it('continues lifecycle if severity is WARNING', () => {
      let form2 = helper.createFormWithOneField() as SpecForm;
      form2.lifecycle = scout.create(SpecLifecycle, {
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
      jasmine.clock().tick(10);
      expectMessageBox(true);
      helper.closeMessageBoxes();
      jasmine.clock().tick(1000); // <- important, otherwise the promise will not be resolved somehow (?)
      expect(lifecycleComplete).toBe(expected);
    }

    it('should call _lifecycleValidate function on form', () => {
      // validate should always be called, even when there is not a single touched field in the form
      let form2 = helper.createFormWithOneField();
      form2.lifecycle = scout.create(SpecLifecycle, {
        widget: form2
      });
      let validateCalled = false;
      form2.addValidator(() => {
        validateCalled = true;
        return Status.ok();
      });
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
      formWithFieldsAndTabBoxes.lifecycle = scout.create(FormLifecycle, {
        widget: formWithFieldsAndTabBoxes
      });

      let field3 = formWithFieldsAndTabBoxes.widget('Field3') as StringField,
        field4 = formWithFieldsAndTabBoxes.widget('Field4') as StringField,
        tabBox = formWithFieldsAndTabBoxes.widget('TabBox') as TabBox,
        tabA = formWithFieldsAndTabBoxes.widget('TabA') as TabItem,
        fieldA2 = formWithFieldsAndTabBoxes.widget('FieldA2') as StringField,
        tabBoxA = formWithFieldsAndTabBoxes.widget('TabBoxA') as TabBox,
        tabAA = formWithFieldsAndTabBoxes.widget('TabAA') as TabItem,
        fieldAA2 = formWithFieldsAndTabBoxes.widget('FieldAA2') as StringField,
        tabAB = formWithFieldsAndTabBoxes.widget('TabAB') as TabItem,
        fieldAB2 = formWithFieldsAndTabBoxes.widget('FieldAB2') as StringField,
        fieldAC2 = formWithFieldsAndTabBoxes.widget('FieldAC2') as StringField,
        tabB = formWithFieldsAndTabBoxes.widget('TabB') as TabItem,
        fieldB3 = formWithFieldsAndTabBoxes.widget('FieldB3') as StringField,
        fieldB4 = formWithFieldsAndTabBoxes.widget('FieldB4') as StringField,
        tableFieldB5 = formWithFieldsAndTabBoxes.widget('TableFieldB5') as TableField,
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
      jasmine.clock().tick(0);
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
     * Errors that are thrown directly in the _load function should be wrapped into a Promise
     * so that the catch() of the Promise is called in all error cases. Otherwise, custom error handling is not possible.
     */
    it('should handle errors that occur in _load function', done => {
      jasmine.clock().install();
      let form = helper.createFormWithOneField();
      let error = null;
      form._load = () => {
        throw 'Something went wrong';
      };
      form.render();
      try {
        form.load()
          .catch(e => {
            error = e;
          });
      } catch (error0) {
        // should not happen
        fail();
      }
      jasmine.clock().tick(10);
      expect(form.destroyed).toBe(true);
      expect(error).toBe('Something went wrong');
      jasmine.clock().uninstall();
      done();
    });

  });

  describe('validation error message', () => {

    it('should list labels of missing and invalid fields', () => {
      field.setLabel('FooField');
      const invalidField = helper.createField('StringField', session.desktop);
      invalidField.setLabel('BarField');

      const missingFields = [createValidationResult(field, false)],
        invalidFields = [createValidationResult(invalidField, true, Status.error())],
        html = form.lifecycle._createInvalidElementsMessageHtml(missingFields, invalidFields);

      expect(html).toContain('FooField');
      expect(html).toContain('BarField');
    });

    it('should list labels of missing and invalid fields with html labels as plain text', () => {
      field.setLabelHtmlEnabled(true);
      field.setLabel('<i>Foo</i><b>Field</b>');
      const invalidField = helper.createField('StringField', session.desktop);
      invalidField.setLabelHtmlEnabled(true);
      invalidField.setLabel('<b>Bar</b><i>Field</i>');

      const missingFields = [createValidationResult(field, false)],
        invalidFields = [createValidationResult(invalidField, true, Status.error())],
        html = form.lifecycle._createInvalidElementsMessageHtml(missingFields, invalidFields);

      expect(html).toContain('FooField');
      expect(html).toContain('BarField');
    });

    it('should list labels of invalid fields with a warning', () => {
      field.setLabel('FooField');

      const invalidFields = [createValidationResult(field, true, Status.warning())],
        html = form.lifecycle._createInvalidElementsMessageHtml([], invalidFields);

      expect(html).toContain('FooField');
    });

    it('should NOT list labels of invalid fields with a info/ok', () => {
      field.setLabel('FooField');
      const barField = helper.createField('StringField', session.desktop);
      barField.setLabel('BarField');

      const invalidFields = [
          createValidationResult(field, true, Status.ok()),
          createValidationResult(barField, true, Status.info())
        ],
        html = form.lifecycle._createInvalidElementsMessageHtml([], invalidFields);

      expect(html).not.toContain('FooField');
      expect(html).not.toContain('BarField');
    });

    it('should list labels and messages of the errorStatus of invalid fields', () => {
      field.setLabel('FooField');
      const barField = helper.createField('StringField', session.desktop),
        fieldWithoutLabel = helper.createField('StringField', session.desktop),
        mandatoryFieldWithError = helper.createField('StringField', session.desktop),
        mandatoryFieldWithWarning = helper.createField('StringField', session.desktop);
      barField.setLabel('BarField');
      mandatoryFieldWithError.setLabel('Mandatory with ERROR');
      mandatoryFieldWithWarning.setLabel('Mandatory with WARNING');

      const missingFields = [
          createValidationResult(mandatoryFieldWithWarning, false, Status.error('MandatoryFieldWithWarning has a WARNING!!!'))
        ],
        invalidFields = [
          createValidationResult(field, true, Status.warning('FooField has a WARNING!!!')),
          createValidationResult(barField, true, Status.error()),
          createValidationResult(fieldWithoutLabel, true, Status.error('FieldWithoutLabel has an ERROR!!!')),
          createValidationResult(mandatoryFieldWithError, false, Status.error('MandatoryFieldWithError has an ERROR!!!'))
        ],
        html = form.lifecycle._createInvalidElementsMessageHtml(missingFields, invalidFields);

      expect(html).toContain('<li>Mandatory with WARNING</li>');
      expect(html).toContain('<li>FooField: \'FooField has a WARNING!!!\'</li>');
      expect(html).toContain('<li>BarField</li>');
      expect(html).toContain('<li>\'FieldWithoutLabel has an ERROR!!!\'</li>');
      expect(html).toContain('<li>Mandatory with ERROR: \'MandatoryFieldWithError has an ERROR!!!\'</li>');
    });
  });

  describe('validation result', () => {

    it('should visit all fields recursively by default', () => {
      let form = scout.create(SpecForm, {
        parent: session.desktop,
        rootGroupBox: {
          id: 'MainBox',
          objectType: GroupBox,
          mainBox: true,
          fields: [{
            id: 'GroupBox',
            objectType: GroupBox,
            fields: [{
              id: 'Field1',
              objectType: StringField
            }]
          }]
        }
      });
      let field1 = form.widget('Field1', StringField); // not mandatory
      expect(field1.getValidationResult().validByMandatory).toBe(true);
      // Add a nested, mandatory value field
      let field2 = scout.create(StringField, {
        parent: field1,
        mandatory: true
      });
      expect(field2.getValidationResult().validByMandatory).toBe(false);

      let invalidElements = form.lifecycle.invalidElements();
      expect(invalidElements.missingElements.length).toBe(1);
      expect(invalidElements.missingElements[0].field).toBe(field2);

      // Set field1 to mandatory, now both fields should be reported
      field1.setMandatory(true);
      invalidElements = form.lifecycle.invalidElements();
      expect(invalidElements.missingElements.length).toBe(2);
      expect(invalidElements.missingElements[0].field).toBe(field1);
      expect(invalidElements.missingElements[1].field).toBe(field2);
    });

    it('should consider visit result when visiting fields', () => {
      let form = scout.create(SpecForm, {
        parent: session.desktop,
        rootGroupBox: {
          id: 'MainBox',
          objectType: GroupBox,
          mainBox: true,
          fields: [{
            id: 'GroupBox',
            objectType: GroupBox,
            fields: [{
              id: 'Field1',
              objectType: StringField
            }]
          }]
        }
      });
      let field1 = form.widget('Field1', StringField); // not mandatory
      // Simulate a field class that ignores child fields by returning visit result SKIP_SUBTREE
      let origGetValidationResult = field1.getValidationResult;
      field1.getValidationResult = () => {
        let result = origGetValidationResult.call(field1);
        result.visitResult = TreeVisitResult.SKIP_SUBTREE;
        return result;
      };
      expect(field1.getValidationResult().validByMandatory).toBe(true);
      let field2 = scout.create(StringField, {
        parent: field1,
        mandatory: true
      });
      expect(field2.getValidationResult().validByMandatory).toBe(false);

      // No errors should be reported
      let invalidElements = form.lifecycle.invalidElements();
      expect(invalidElements.missingElements.length).toBe(0);

      // Set field1 to mandatory, now it should be reported
      field1.setMandatory(true);
      invalidElements = form.lifecycle.invalidElements();
      expect(invalidElements.missingElements.length).toBe(1);
      expect(invalidElements.missingElements[0].field).toBe(field1);
    });

    it('should consider lifecycle boundary when visiting fields', () => {
      let form = scout.create(SpecForm, {
        parent: session.desktop,
        rootGroupBox: {
          id: 'MainBox',
          objectType: GroupBox,
          mainBox: true,
          fields: [{
            id: 'Field1',
            objectType: StringField
          }]
        }
      });
      let field1 = form.widget('Field1', StringField); // not mandatory
      expect(field1.getValidationResult().validByMandatory).toBe(true);

      let field2 = scout.create(StringField, {
        parent: field1,
        mandatory: true
      });
      expect(field2.getValidationResult().validByMandatory).toBe(false);

      // field2 should be reported
      let invalidElements = form.lifecycle.invalidElements();
      expect(invalidElements.missingElements.length).toBe(1);
      expect(invalidElements.missingElements[0].field).toBe(field2);

      // Mark field1 as lifecycle boundary -> error on field2 should no longer be reported
      field1.lifecycleBoundary = true;
      // Assert that lifecycleBoundary flag takes precedence
      let origGetValidationResult = field1.getValidationResult;
      field1.getValidationResult = () => {
        let result = origGetValidationResult.call(field1);
        result.visitResult = TreeVisitResult.CONTINUE; // <-- this should be ignored because of lifecycleBoundary=true
        return result;
      };
      invalidElements = form.lifecycle.invalidElements();
      expect(invalidElements.missingElements.length).toBe(0);

      // Set field1 to mandatory, it should be reported (lifecycleBoundary property only affects child fields)
      field1.setMandatory(true);
      invalidElements = form.lifecycle.invalidElements();
      expect(invalidElements.missingElements.length).toBe(1);
      expect(invalidElements.missingElements[0].field).toBe(field1);

      // -----

      // resetValue() also considers the lifecycleBoundary flag
      field1.setValue('a');
      field2.setValue('b');
      form.reset();
      expect(field1.value).toBe(null);
      expect(field2.value).toBe('b');
    });

    it('has severity ERROR if mandatory field is missing', async () => {
      jasmine.clock().uninstall();

      field.setMandatory(true);
      field.setValue(null);

      let status = await form.lifecycle._validate();
      expect(status).not.toBeNull();
      expect(status.severity).toBe(Status.Severity.ERROR);

      field.setErrorStatus(Status.ok());
      status = await form.lifecycle._validate();
      expect(status).not.toBeNull();
      expect(status.severity).toBe(Status.Severity.ERROR);

      field.setErrorStatus(Status.warning());
      status = await form.lifecycle._validate();
      expect(status).not.toBeNull();
      expect(status.severity).toBe(Status.Severity.ERROR);

      field.setErrorStatus(Status.error());
      status = await form.lifecycle._validate();
      expect(status).not.toBeNull();
      expect(status.severity).toBe(Status.Severity.ERROR);
    });

    it('has max severity of all invalid fields', async () => {
      jasmine.clock().uninstall();

      const field2 = helper.createField(StringField, form.rootGroupBox);
      form.rootGroupBox.insertField(field2);

      let status = await form.lifecycle._validate();
      expect(status).not.toBeNull();
      expect(status.severity).toBe(Status.Severity.OK);

      field.setErrorStatus(Status.ok());
      status = await form.lifecycle._validate();
      expect(status).not.toBeNull();
      expect(status.severity).toBe(Status.Severity.OK);

      field2.setErrorStatus(Status.warning());
      status = await form.lifecycle._validate();
      expect(status).not.toBeNull();
      expect(status.severity).toBe(Status.Severity.WARNING);

      field.setErrorStatus(Status.info());
      status = await form.lifecycle._validate();
      expect(status).not.toBeNull();
      expect(status.severity).toBe(Status.Severity.WARNING);

      field2.clearErrorStatus();
      status = await form.lifecycle._validate();
      expect(status).not.toBeNull();
      // errorStatus with severity INFO will not be transferred to the validationResult as it is considered valid
      // -> validation gives OK
      expect(status.severity).toBe(Status.Severity.OK);

      field2.setErrorStatus(Status.error());
      status = await form.lifecycle._validate();
      expect(status).not.toBeNull();
      expect(status.severity).toBe(Status.Severity.ERROR);
    });
  });

  describe('statusMessageBox', () => {

    it('converts WARNING to OK on yes', done => {
      form._showFormInvalidMessageBox(Status.warning())
        .then(status => {
          expect(status).not.toBeNull();
          expect(status.severity).toBe(Status.Severity.OK);
        })
        .catch(fail)
        .always(done);
      jasmine.clock().tick(0);
      expectMessageBox(true);
      helper.closeMessageBoxes(MessageBox.Buttons.YES);
      jasmine.clock().tick(1000);
    });

    it('keeps WARNING on no', done => {
      form._showFormInvalidMessageBox(Status.warning())
        .then(status => {
          expect(status).not.toBeNull();
          expect(status.severity).toBe(Status.Severity.WARNING);
        })
        .catch(fail)
        .always(done);
      jasmine.clock().tick(0);
      expectMessageBox(true);
      helper.closeMessageBoxes(MessageBox.Buttons.NO);
      jasmine.clock().tick(1000);
    });

    it('keeps ERROR on yes', done => {
      form._showFormInvalidMessageBox(Status.error())
        .then(status => {
          expect(status).not.toBeNull();
          expect(status.severity).toBe(Status.Severity.ERROR);
        })
        .catch(fail)
        .always(done);
      jasmine.clock().tick(0);
      expectMessageBox(true);
      helper.closeMessageBoxes(MessageBox.Buttons.YES);
      jasmine.clock().tick(1000);
    });

    it('return status if it is valid', async () => {
      jasmine.clock().uninstall();
      const info = Status.info();
      const status = await form._showFormInvalidMessageBox(info);
      expect(status).toBe(info);
    });
  });

  function createValidationResult(field: FormField, validByMandatory: boolean, errorStatus?: Status): ValidationResult {
    return {
      valid: validByMandatory && (!errorStatus || errorStatus.isValid()),
      validByMandatory,
      errorStatus,
      field,
      label: field && field.label,
      reveal: () => {
        // nop
      }
    };
  }
});
