/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
describe("SliderField", function() {
  var session, helper, field;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
    field = createField(createModel());
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    jasmine.clock().uninstall();
    jasmine.Ajax.uninstall();
  });

  function createField(model) {
    var field = new scout.SliderField();
    field.init(model);
    return field;
  }

  function createModel() {
    return helper.createFieldModel();
  }

  describe("slider", function() {

    it("accepts the value", function() {
      field.render();
      field.setValue(25);

      expect(field.value).toBe(null);
      expect(field.slider.value).toBe(25);
      expect(field.displayText).toBe('');
      field.acceptInput();
      expect(field.displayText).toBe('25');

      field.slider.setValue(30);
      field.acceptInput();
      expect(field.value).toBe(null);
      expect(field.slider.value).toBe(30);
      expect(field.displayText).toBe('30');
    });

  });

});
