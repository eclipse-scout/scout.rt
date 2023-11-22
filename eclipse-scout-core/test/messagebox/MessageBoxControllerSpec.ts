/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DisplayParent, MessageBox, MessageBoxController, scout} from '../../src/index';
import {FormSpecHelper} from '../../src/testing';

describe('MessageBoxController', () => {
  let session: SandboxSession;
  let displayParent: DisplayParent;
  let messageBoxController: MessageBoxController;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    displayParent = new FormSpecHelper(session).createFormWithOneField();
    messageBoxController = new MessageBoxController(displayParent, session);
  });

  describe('registerAndRender', () => {

    it('does not register messageBoxes twice on its displayParent', () => {
      const messageBox = scout.create(MessageBox, {
        parent: displayParent,
        displayParent
      });

      expect(displayParent.messageBoxes.length).toBe(0);

      messageBoxController.registerAndRender(messageBox);
      expect(displayParent.messageBoxes.length).toBe(1);

      messageBoxController.registerAndRender(messageBox);
      expect(displayParent.messageBoxes.length).toBe(1);
    });
  });
});
