/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DisplayParent, Form, FormController, scout} from '../../src/index';
import {FormSpecHelper} from '../../src/testing';

describe('FormController', () => {
  let session: SandboxSession;
  let formHelper: FormSpecHelper;
  let displayParent: DisplayParent;
  let formController: FormController;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    formHelper = new FormSpecHelper(session);
    displayParent = formHelper.createFormWithOneField();
    formController = scout.create(FormController, {displayParent, session});
  });

  describe('registerAndRender', () => {

    it('does not register views twice on its displayParent', () => {
      const view = formHelper.createFormWithOneField({
        parent: displayParent,
        displayParent,
        displayHint: Form.DisplayHint.VIEW
      });

      expect(displayParent.views.length).toBe(0);

      formController.registerAndRender(view);
      expect(displayParent.views.length).toBe(1);

      formController.registerAndRender(view);
      expect(displayParent.views.length).toBe(1);
    });

    it('does not register dialogs twice on its displayParent', () => {
      const dialog = formHelper.createFormWithOneField({
        parent: displayParent,
        displayParent,
        displayHint: Form.DisplayHint.DIALOG
      });

      expect(displayParent.dialogs.length).toBe(0);

      formController.registerAndRender(dialog);
      expect(displayParent.dialogs.length).toBe(1);

      formController.registerAndRender(dialog);
      expect(displayParent.dialogs.length).toBe(1);
    });
  });
});
