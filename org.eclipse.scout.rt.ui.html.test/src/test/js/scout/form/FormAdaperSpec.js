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
/* global linkWidgetAndAdapter */
describe('FormAdapter', function() {
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

  describe('form destroy', function() {

    it('destroys the adapters of the children', function() {
      var form = helper.createFormWithOneField();
      linkWidgetAndAdapter(form, 'FormAdapter');
      linkWidgetAndAdapter(form.rootGroupBox, 'GroupBoxAdapter');
      linkWidgetAndAdapter(form.rootGroupBox.fields[0], 'StringFieldAdapter');

      expect(session.getModelAdapter(form.id).widget).toBe(form);
      expect(session.getModelAdapter(form.rootGroupBox.id).widget).toBe(form.rootGroupBox);
      expect(session.getModelAdapter(form.rootGroupBox.fields[0].id).widget).toBe(form.rootGroupBox.fields[0]);

      form.destroy();
      expect(session.getModelAdapter(form.id)).toBeFalsy();
      expect(session.getModelAdapter(form.rootGroupBox.id)).toBeFalsy();
      expect(session.getModelAdapter(form.rootGroupBox.fields[0].id)).toBeFalsy();
    });

  });

  describe('onModelAction', function() {

    describe('disposeAdapter', function() {

      function createDisposeAdapterEvent(model) {
        return {
          target: session.uiSessionId,
          type: 'disposeAdapter',
          adapter: model.id
        };
      }

      it('destroys the form', function() {
        var form = helper.createFormWithOneField();
        linkWidgetAndAdapter(form, 'FormAdapter');
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
