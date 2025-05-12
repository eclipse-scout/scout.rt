/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {clipboard, scout, StringField} from '../../src/index';

describe('clipboard', () => {
  let session: SandboxSession, $sandbox: JQuery;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = session.$entryPoint;
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  describe('showNotification', () => {

    it('shows a desktop notification', () => {
      let parent = scout.create(StringField, {
        parent: session.desktop
      });
      parent.render();
      expect(session.desktop.notifications.length).toBe(0);

      clipboard.showNotification(parent);
      expect(session.desktop.notifications.length).toBe(1);

      // Destroy parent widget -> should not destroy notification
      parent.destroy();
      expect(session.desktop.notifications.length).toBe(1);

      // Wait for notification to be removed
      jasmine.clock().tick(5000);
      expect(session.desktop.notifications.length).toBe(0);
    });
  });
});
