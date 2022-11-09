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
import {FormField, FormFieldAdapter, RemoteEvent, Status} from '../../../src/index';
import {FormSpecHelper} from '../../../src/testing/index';

describe('FormFieldAdapter', () => {
  let session: SandboxSession;
  let helper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
  });

  describe('onModelPropertyChange', () => {
    let formField: FormField, adapter: FormFieldAdapter, model;

    beforeEach(() => {
      model = helper.createFieldModel();
      adapter = new FormFieldAdapter();
      adapter.init(model);
      formField = adapter.createWidget(model, session.desktop);
    });

    it('event should update model', () => {
      // Note: normally an event for ID 123 would never be applied
      // to an adapter with ID 2! We only do this here in order to
      // check whether the onModelPropertyChange method applies
      // the ID of the event by error (should not happen).
      let event = {
        id: '123',
        type: 'property',
        properties: {
          errorStatus: {message: 'foo'}
        }
      } as unknown as RemoteEvent;
      adapter.onModelPropertyChange(event);
      expect(formField.errorStatus.equals(Status.ensure({message: 'foo'}))).toBe(true);
      // never apply id, type, properties on model
      expect(formField.id).toBe(model.id);
      expect(formField.hasOwnProperty('type')).toBe(false);
      expect(formField.hasOwnProperty('properties')).toBe(false);
    });

  });

});
