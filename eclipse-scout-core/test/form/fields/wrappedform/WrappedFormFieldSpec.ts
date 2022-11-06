/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {focusUtils, WrappedFormField, WrappedFormFieldModel} from '../../../../src/index';
import {FormSpecHelper} from '../../../../src/testing/index';

describe('WrappedForm', () => {
  let session: SandboxSession;
  let helper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
  });

  function createField(modelProperties: WrappedFormFieldModel): WrappedFormField {
    return helper.createField(WrappedFormField, session.desktop, modelProperties);
  }

  describe('mandatory indicator', () => {

    // Must not contain an indicator to prevent a double indicator if the first field is mandatory too
    it('does not exist', () => {
      let field = createField({mandatory: true});
      field.render();

      expect(field.$mandatory).toBeUndefined();
    });

  });

  describe('initial focus disabled', () => {
    it('string field in inner form hasn\'t focus', () => {
      let innerForm = helper.createFormWithOneField();
      let field = createField({innerForm: innerForm});
      expect(field.initialFocusEnabled).toBe(false);

      field.render();

      let $stringField = innerForm.rootGroupBox.fields[0].$field;
      expect(focusUtils.isActiveElement($stringField)).toBe(false);
    });
  });

  describe('initial focus enabled', () => {
    it('string field in inner form has focus', () => {
      let innerForm = helper.createFormWithOneField();
      let field = createField({initialFocusEnabled: true, innerForm: innerForm});
      expect(field.initialFocusEnabled).toBe(true);

      field.render();

      let $stringField = innerForm.rootGroupBox.fields[0].$field;
      expect(focusUtils.isActiveElement($stringField)).toBe(true);
    });
  });

  describe('innerForm', () => {
    it('is set to null when being destroyed', () => {
      let innerForm = helper.createFormWithOneField();
      let field = createField({innerForm: innerForm});
      field.render();
      expect(field.innerForm).toBe(innerForm);
      expect(innerForm.rendered).toBe(true);

      innerForm.destroy();
      expect(field.innerForm).toBe(null);
      expect(innerForm.rendered).toBe(false);
      expect(innerForm.destroyed).toBe(true);
    });

    it('will be removed if set to null', () => {
      let innerForm = helper.createFormWithOneField();
      let field = createField({innerForm: innerForm});
      field.render();
      expect(field.innerForm).toBe(innerForm);
      expect(innerForm.rendered).toBe(true);

      field.setInnerForm(null);
      expect(field.innerForm).toBe(null);
      expect(innerForm.rendered).toBe(false);
      expect(innerForm.destroyed).toBe(false);

      // The desktop is the owner of the innerForm in this case -> destroy form explicitly
      innerForm.destroy();
      expect(innerForm.destroyed).toBe(true);
    });
  });

});
