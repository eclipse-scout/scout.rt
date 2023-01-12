/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Mode} from '../../src/index';

describe('Mode', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('defaults', () => {

    it('should be as expected', () => {
      let mode = new Mode();
      mode.init(createSimpleModel('Mode', session));
      expect(mode.selected).toBe(false);
    });
  });
});
