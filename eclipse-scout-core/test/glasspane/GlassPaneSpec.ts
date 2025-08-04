/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {GlassPane, scout, Session} from '../../src/index';

describe('GlassPane', () => {
  let session: Session;
  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('deactivate', () => {
    it('deactivates the glass pane', () => {
      let glassPane = scout.create(GlassPane, {
        parent: session.desktop
      });
      glassPane.render();
      expect(session.focusManager._glassPaneTargets.length).toBe(1);
      expect(session.focusManager._glassPaneTargets[0][0]).toBe(glassPane.$parent[0]);
      expect(glassPane.$parent).toHaveClass('glasspane-parent');
      expect(glassPane.$container).not.toHaveClass('deactivated');

      glassPane.deactivate();
      expect(session.focusManager._glassPaneTargets.length).toBe(0);
      expect(glassPane.$parent).not.toHaveClass('glasspane-parent');
      expect(glassPane.$container).toHaveClass('deactivated');
    });

    it('does nothing if it is already deactivated ', () => {
      let glassPane = scout.create(GlassPane, {
        parent: session.desktop
      });
      glassPane.render();
      expect(session.focusManager._glassPaneTargets.length).toBe(1);
      expect(session.focusManager._glassPaneTargets[0][0]).toBe(session.$entryPoint[0]);

      // The same target may be added by another GlassPane or GlassPaneRenderer
      session.focusManager._glassPaneTargets.push(session.$entryPoint);
      expect(session.focusManager._glassPaneTargets.length).toBe(2);

      glassPane.deactivate();
      expect(session.focusManager._glassPaneTargets.length).toBe(1);

      glassPane.deactivate();
      expect(session.focusManager._glassPaneTargets.length).toBe(1); // Still 1
    });
  });

  describe('activates', () => {
    it('activates the glass pane', () => {
      let glassPane = scout.create(GlassPane, {
        parent: session.desktop
      });
      glassPane.render();
      glassPane.deactivate();
      glassPane.activate();
      expect(session.focusManager._glassPaneTargets.length).toBe(1);
      expect(session.focusManager._glassPaneTargets[0][0]).toBe(glassPane.$parent[0]);
      expect(glassPane.$parent).toHaveClass('glasspane-parent');
      expect(glassPane.$container).not.toHaveClass('deactivated');
    });

    it('does nothing if it is already active', () => {
      let glassPane = scout.create(GlassPane, {
        parent: session.desktop
      });
      glassPane.render();
      expect(session.focusManager._glassPaneTargets.length).toBe(1);
      expect(session.focusManager._glassPaneTargets[0][0]).toBe(glassPane.$parent[0]);
      expect(glassPane.$parent).toHaveClass('glasspane-parent');
      expect(glassPane.$container).not.toHaveClass('deactivated');

      glassPane.activate();
      expect(session.focusManager._glassPaneTargets.length).toBe(1); // Still one
      expect(glassPane.$parent).toHaveClass('glasspane-parent');
      expect(glassPane.$container).not.toHaveClass('deactivated');

      glassPane.deactivate();
      expect(session.focusManager._glassPaneTargets.length).toBe(0);

      glassPane.activate();
      glassPane.activate();
      expect(session.focusManager._glassPaneTargets.length).toBe(1);
    });
  });
});
