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
describe("FormFieldAdapter", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
  });

  describe("onModelPropertyChange", function() {
    var formField, adapter, model;

    beforeEach(function() {
      model = helper.createFieldModel();
      adapter = new scout.FormFieldAdapter();
      adapter.init(model);
      formField = adapter.createWidget(model, session.desktop);
    });

    it("event should update model", function() {
      // Note: normally an event for ID 123 would never be applied
      // to an adapter with ID 2! We only do this here in order to
      // check whether or not the onModelPropertyChange method applies
      // the ID of the event by error (should not happen).
      var event = {
        id:'123',
        type:'property',
        properties: {
          errorStatus: {message: 'foo'}
        }
      };
      // required
      formField._$statusLabel = $('<div></div>');
      adapter.onModelPropertyChange(event);
      expect(formField.errorStatus).toEqual(new scout.Status({message: 'foo'}));
      // never apply id, type, properties on model
      expect(formField.id).toBe(model.id);
      expect(formField.hasOwnProperty('type')).toBe(false);
      expect(formField.hasOwnProperty('properties')).toBe(false);
    });

  });

});
