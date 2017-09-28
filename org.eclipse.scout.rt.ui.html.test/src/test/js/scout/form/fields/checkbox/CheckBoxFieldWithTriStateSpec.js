/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("CheckBoxFieldWithTriState", function() {

  describe("inheritance", function() {

    var session;
    var checkBoxField;
    var model;

    beforeEach(function() {
      setFixtures(sandbox());
      session = sandboxSession();
      model = createSimpleModel('CheckBoxField', session);
      model.triStateEnabled = true;
      checkBoxField = new scout.CheckBoxField();
      checkBoxField.init(model);
    });

    it("inherits from ValueField", function() {
      expect(scout.ValueField.prototype.isPrototypeOf(checkBoxField)).toBe(true);
    });

    it("_renderValue sets checked and undefined classes", function() {
      var $div = $('<div>');
      checkBoxField.render($div);

      checkBoxField.setValue(false);
      expect(checkBoxField.$checkBox.hasClass('checked')).toBe(false);
      expect(checkBoxField.$checkBox.hasClass('undefined')).toBe(false);
      checkBoxField.toggleChecked();
      expect(checkBoxField.$checkBox.hasClass('checked')).toBe(true);
      expect(checkBoxField.$checkBox.hasClass('undefined')).toBe(false);
      checkBoxField.toggleChecked();
      expect(checkBoxField.$checkBox.hasClass('checked')).toBe(false);
      expect(checkBoxField.$checkBox.hasClass('undefined')).toBe(true);
      checkBoxField.toggleChecked();
      expect(checkBoxField.$checkBox.hasClass('checked')).toBe(false);
      expect(checkBoxField.$checkBox.hasClass('undefined')).toBe(false);
    });

    it("_renderValue sets disabled property", function() {
      var $div = $('<div>');
      checkBoxField.render($div);

      checkBoxField.setEnabled(false);
      expect(checkBoxField.$field.hasClass('disabled')).toBe(true);
      expect(checkBoxField.$checkBox.hasClass('disabled')).toBe(true);
      checkBoxField.setEnabled(true);
      expect(checkBoxField.$field.hasClass('disabled')).toBe(false);
      expect(checkBoxField.$checkBox.hasClass('disabled')).toBe(false);
    });

  });

});
