/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Mode} from '../../src/index';

describe('Mode', () => {
  let session;

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
