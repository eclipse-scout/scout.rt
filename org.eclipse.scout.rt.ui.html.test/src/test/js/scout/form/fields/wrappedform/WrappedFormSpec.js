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
describe('WrappedForm', function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
  });

  function createField(modelProperties) {
    return helper.createField('WrappedFormField', session.desktop, modelProperties);
  }

  describe('mandatory indicator', function() {

    // Must not contain an indicator to prevent a double indicator if the first field is mandatory too
    it('does not exist', function() {
      var field = createField({mandatory: true});
      field.render();

      expect(field.$mandatory).toBeUndefined();
    });

  });

  describe('test initial focus disabled', function() {
    it('string field in inner form hasn\'t focus', function() {
      var innerForm = helper.createFormWithOneField();
      var field = createField({innerForm: innerForm});
      expect(field.initialFocusEnabled).toBe(false);

      field.render();

      var $stringField = innerForm.rootGroupBox.fields[0].$field;
      expect(scout.focusUtils.isActiveElement($stringField)).toBe(false);
    });
  });

  describe('test initial focus enabled', function() {
    it('string field in inner form has focus', function() {
      var innerForm = helper.createFormWithOneField();
      var field = createField({initialFocusEnabled: true, innerForm: innerForm});
      expect(field.initialFocusEnabled).toBe(true);

      field.render();

      var $stringField = innerForm.rootGroupBox.fields[0].$field;
      expect(scout.focusUtils.isActiveElement($stringField)).toBe(true);
    });
  });

});
