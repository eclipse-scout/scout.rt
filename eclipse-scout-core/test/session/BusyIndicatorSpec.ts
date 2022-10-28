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
import {BusyIndicator, scout} from '../../src/index';

describe('BusyIndicator', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('render', () => {
    it('uses entry point as parent if no $parent is provided', () => {
      let busyIndicator = scout.create(BusyIndicator, {
        parent: session.desktop,
        showTimeout: 0
      });
      busyIndicator.render();
      expect(busyIndicator.$parent[0]).toBe(session.$entryPoint[0]);
      busyIndicator.destroy();
    });

    it('uses $parent as parent if provided', () => {
      let $parent = session.$entryPoint.appendDiv();
      let busyIndicator = scout.create(BusyIndicator, {
        parent: session.desktop,
        showTimeout: 0
      });
      busyIndicator.render($parent);
      expect(busyIndicator.$parent[0]).toBe($parent[0]);
      busyIndicator.destroy();
    });
  });
});
