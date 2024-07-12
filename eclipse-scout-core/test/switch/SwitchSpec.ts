/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {scout, Switch} from '../../src';

describe('Switch', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('events', () => {

    it('does not change activated flag when event is prevented', () => {
      let switch_ = scout.create(Switch, {
        parent: session.desktop
      });
      let switchEventCount = 0;
      switch_.on('switch', event => {
        switchEventCount++;
      });

      expect(switchEventCount).toBe(0);
      expect(switch_.activated).toBe(false);
      switch_.toggleSwitch();
      expect(switchEventCount).toBe(1);
      expect(switch_.activated).toBe(true);
      switch_.toggleSwitch();
      expect(switchEventCount).toBe(2);
      expect(switch_.activated).toBe(false);
      switch_.toggleSwitch(undefined, false);
      expect(switchEventCount).toBe(3);
      expect(switch_.activated).toBe(false);

      switch_.on('switch', event => event.preventDefault());
      switch_.toggleSwitch();
      expect(switchEventCount).toBe(4);
      expect(switch_.activated).toBe(false);

      switch_.setActivated(true);
      switch_.toggleSwitch();
      expect(switchEventCount).toBe(5);
      expect(switch_.activated).toBe(true);
    });
  });

  it('renders activated = null as not activated', () => {
    let switch_ = scout.create(Switch, {
      parent: session.desktop
    });
    switch_.render();
    switch_.setActivated(false);
    switch_.setActivated(null);
    expect(switch_.$button).not.toHaveClass('activated');
  });
});
