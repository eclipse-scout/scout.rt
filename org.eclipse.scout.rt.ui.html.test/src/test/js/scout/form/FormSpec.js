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
describe("Form", function() {
  var session, helper;

  beforeEach(function() {
    setFixtures(sandbox());
    jasmine.Ajax.install();
    jasmine.clock().install();
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
    uninstallUnloadHandlers(session);
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe("destroy", function() {

    it("destroys the adapter and its children", function() {
      var form = helper.createFormWithOneField();

      expect(form.rootGroupBox).toBeTruthy();
      expect(session.getModelAdapter(form.rootGroupBox.id)).toBe(form.rootGroupBox);
      expect(form.rootGroupBox.fields[0]).toBeTruthy();
      expect(session.getModelAdapter(form.rootGroupBox.fields[0].id)).toBe(form.rootGroupBox.fields[0]);

      form.destroy();

      expect(session.getModelAdapter(form.rootGroupBox.id)).toBeFalsy();
      expect(session.getModelAdapter(form.rootGroupBox.fields[0].id)).toBeFalsy();
    });

  });

  describe("onModelAction", function() {

    describe("formClose", function() {

      function createDisposeAdapterEvent(model) {
        return {
          target: session.uiSessionId,
          type: 'disposeAdapter',
          adapter: model.id
        };
      }

      it("destroys the form", function() {
        var form = helper.createFormWithOneField();
        spyOn(form, 'destroy');

        var message = {
          events: [createDisposeAdapterEvent(form)]
        };
        session._processSuccessResponse(message);

        expect(form.destroy).toHaveBeenCalled();
      });

    });

  });

});
