/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormSpecHelper} from '../../src/testing/index';

describe('FormAdapter', () => {
  let session: SandboxSession, helper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    jasmine.Ajax.install();
    jasmine.clock().install();
    session = sandboxSession();
    helper = new FormSpecHelper(session);
    uninstallUnloadHandlers(session);
  });

  afterEach(() => {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe('form destroy', () => {

    it('destroys the adapters of the children', () => {
      let form = helper.createFormWithOneField();
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

  describe('onModelAction', () => {

    describe('disposeAdapter', () => {

      function createDisposeAdapterEvent(model) {
        return {
          target: session.uiSessionId,
          type: 'disposeAdapter',
          adapter: model.id
        };
      }

      it('destroys the form', () => {
        let form = helper.createFormWithOneField();
        linkWidgetAndAdapter(form, 'FormAdapter');
        spyOn(form, 'destroy');

        let message = {
          events: [createDisposeAdapterEvent(form)]
        };
        session._processSuccessResponse(message);

        expect(form.destroy).toHaveBeenCalled();
      });

    });

  });

});
