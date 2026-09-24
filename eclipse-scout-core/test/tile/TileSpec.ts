/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {scout, Tile} from '../../src/index';

describe('Tile', () => {

  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  describe('autoReloadRate', () => {

    it('is not set initially', () => {
      let tile = scout.create(Tile, {parent: session.desktop});
      expect(tile.autoReloadRate).toBe(null);
    });

    it('does not trigger an event if not set', () => {
      let tile = scout.create(Tile, {parent: session.desktop});
      let eventTriggered = false;
      tile.on('load', event => {
        eventTriggered = true;
      });
      tile.render();
      jasmine.clock().tick(5000);
      expect(eventTriggered).toBe(false);
    });

    it('does not trigger an event if set and loading ist set', () => {
      let tile = scout.create(Tile, {
        parent: session.desktop,
        loading: true,
        autoReloadRate: 3
      });
      let eventTriggered = false;
      tile.on('load', event => {
        eventTriggered = true;
      });
      tile.render();
      jasmine.clock().tick(5000);
      expect(eventTriggered).toBe(false);
    });

    it('does trigger an event if set', () => {
      let tile = scout.create(Tile, {
        parent: session.desktop,
        loading: false,
        autoReloadRate: 3
      });
      let eventTriggered = false;
      tile.on('load', event => {
        eventTriggered = true;
      });
      tile.render();
      jasmine.clock().tick(5000);
      expect(eventTriggered).toBe(true);
    });

    it('does not trigger an event if set and tile is detached', () => {
      let tile = scout.create(Tile, {
        parent: session.desktop,
        loading: false,
        autoReloadRate: 3
      });
      tile.render();

      // detach tile
      let eventTriggered = false;
      tile.on('load', event => {
        eventTriggered = true;
      });
      tile.detach();
      jasmine.clock().tick(5000);
      expect(eventTriggered).toBe(false);
    });

    it('does not trigger an event if set and tile is removed', () => {
      let tile = scout.create(Tile, {
        parent: session.desktop,
        loading: false,
        autoReloadRate: 3
      });
      tile.render();

      // remove tile
      let eventTriggered = false;
      tile.on('load', event => {
        eventTriggered = true;
      });
      tile.remove();
      jasmine.clock().tick(5000);
      expect(eventTriggered).toBe(false);
    });

    it('does trigger an event if set and tile is re-attached', () => {
      let tile = scout.create(Tile, {
        parent: session.desktop,
        loading: true,
        autoReloadRate: 3
      });
      tile.render();
      tile.setLoading(false);
      let eventTriggered = false;
      tile.on('load', event => {
        eventTriggered = true;
      });
      jasmine.clock().tick(5000);
      expect(eventTriggered).toBe(true);

      // detach
      eventTriggered = false;
      tile.detach();
      jasmine.clock().tick(5000);
      expect(eventTriggered).toBe(false);

      // re-attach
      eventTriggered = false;
      tile.attach();
      jasmine.clock().tick(5000);
      expect(eventTriggered).toBe(true);
    });

    it('does trigger an event if first unset and then set again', () => {
      let tile = scout.create(Tile, {
        parent: session.desktop,
        loading: true
      });
      tile.render();
      let eventTriggered = false;
      tile.on('load', event => {
        eventTriggered = true;
      });
      jasmine.clock().tick(5000);
      expect(eventTriggered).toBe(false);

      // switch on
      eventTriggered = false;
      tile.setLoading(false);
      tile.setAutoReloadRate(3);
      jasmine.clock().tick(5000);
      expect(eventTriggered).toBe(true);

      // switch off
      eventTriggered = false;
      tile.setAutoReloadRate(0);
      jasmine.clock().tick(5000);
      expect(eventTriggered).toBe(false);
    });
  });
});
