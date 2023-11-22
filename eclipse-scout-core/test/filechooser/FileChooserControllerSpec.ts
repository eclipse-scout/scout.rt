/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DisplayParent, FileChooser, FileChooserController, scout} from '../../src/index';
import {FormSpecHelper} from '../../src/testing';

describe('FileChooserController', () => {
  let session: SandboxSession;
  let displayParent: DisplayParent;
  let fileChooserController: FileChooserController;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    displayParent = new FormSpecHelper(session).createFormWithOneField();
    fileChooserController = new FileChooserController(displayParent, session);
  });

  describe('registerAndRender', () => {

    it('does not register fileChoosers twice on its displayParent', () => {
      const fileChooser = scout.create(FileChooser, {
        parent: displayParent,
        displayParent
      });

      expect(displayParent.fileChoosers.length).toBe(0);

      fileChooserController.registerAndRender(fileChooser);
      expect(displayParent.fileChoosers.length).toBe(1);

      fileChooserController.registerAndRender(fileChooser);
      expect(displayParent.fileChoosers.length).toBe(1);
    });
  });
});
