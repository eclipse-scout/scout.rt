/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Mode, scout} from '../../src/index';

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

  describe('aria properties', () => {

    it('has aria role radio', () => {
      let mode = scout.create(Mode, {
        parent: session.desktop
      });
      mode.render();
      expect(mode.$container).toHaveAttr('role', 'radio');
    });

    it('has aria-checked set if mode selected', () => {
      let mode = scout.create(Mode, {
        parent: session.desktop
      });
      mode.render();
      expect(mode.$container).toHaveAttr('aria-checked', 'false');
      // also check that aria pressed is not set (not supported for radio role)
      expect(mode.$container.attr('aria-pressed')).toBeFalsy();
      mode.setSelected(true);
      expect(mode.$container).toHaveAttr('aria-checked', 'true');
      expect(mode.$container.attr('aria-pressed')).toBeFalsy();
    });
  });
});
