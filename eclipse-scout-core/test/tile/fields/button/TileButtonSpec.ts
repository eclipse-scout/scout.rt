/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {icons, scout, TileButton} from '../../../../src';

describe('TileButton', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  class SpecTileButton extends TileButton {
    protected override _isInDashboardTile(): boolean {
      // Simulate surrounding dashboard tile (otherwise, the TileButton behaves more like a normal Button)
      return true;
    }
  }

  describe('labelVisible', () => {

    it('does not render label as $label', () => {
      let button = scout.create(SpecTileButton, {
        parent: session.desktop,
        label: 'Test'
      });
      button.render();

      expect(button.labelVisible).toBe(true);
      expect(button.label).toBe('Test');
      expect(button.$label).toBe(null);
      expect(button.$buttonLabel).toBeVisible();
      expect(button.$buttonLabel.html()).toBe('Test');
    });

    it('shows the $buttonLabel when labelVisible=true', () => {
      let button = scout.create(SpecTileButton, {
        parent: session.desktop,
        labelVisible: true,
        label: null
      });
      button.render();
      expect(button.$buttonLabel).toBeVisible();
      expect(button.$buttonLabel.html()).toBe('&nbsp;');

      button = scout.create(SpecTileButton, {
        parent: session.desktop,
        labelVisible: true,
        label: 'Options'
      });
      button.render();
      expect(button.$buttonLabel).toBeVisible();
      expect(button.$buttonLabel.html()).toBe('Options');
    });

    it('never shows the label if labelVisible=false', () => {
      // Note: This behaves differently from a normal Button widget.

      let button = scout.create(SpecTileButton, {
        parent: session.desktop,
        labelVisible: false,
        label: null
      });
      button.render();
      expect(button.$buttonLabel).toBeHidden(); // <--
      expect(button.$buttonLabel.html()).toBe('&nbsp;');

      button = scout.create(SpecTileButton, {
        parent: session.desktop,
        labelVisible: false,
        label: 'Options'
      });
      button.render();
      expect(button.$buttonLabel).toBeHidden(); // <--
      expect(button.$buttonLabel.html()).toBe('Options');
    });

    it('shows or hides the label dynamically when the properties change', () => {
      // Note: This behaves differently from a normal Button widget.

      let button = scout.create(SpecTileButton, {
        parent: session.desktop
      });
      button.render();
      expect(button.$buttonLabel).toBeVisible();
      expect(button.$buttonLabel.html()).toBe('&nbsp;');
      expect(button.get$Icon().length).toBe(0);

      button.setLabel('Options');
      expect(button.$buttonLabel).toBeVisible();
      expect(button.$buttonLabel.html()).toBe('Options');
      expect(button.get$Icon().length).toBe(0);

      button.setIconId(icons.GEAR);
      expect(button.$buttonLabel).toBeVisible();
      expect(button.$buttonLabel.html()).toBe('Options');
      expect(button.get$Icon()).toBeVisible();

      button.setLabel(null);
      expect(button.$buttonLabel).toBeVisible();
      expect(button.$buttonLabel.html()).toBe('&nbsp;');
      expect(button.get$Icon()).toBeVisible();

      button.setIconId(null);
      expect(button.$buttonLabel).toBeVisible();
      expect(button.$buttonLabel.html()).toBe('&nbsp;');
      expect(button.get$Icon().length).toBe(0);

      button.setLabelVisible(false);
      expect(button.$buttonLabel).toBeHidden();
      expect(button.$buttonLabel.html()).toBe('&nbsp;');
      expect(button.get$Icon().length).toBe(0);

      button.setLabel('Options');
      expect(button.$buttonLabel).toBeHidden();
      expect(button.$buttonLabel.html()).toBe('Options');
      expect(button.get$Icon().length).toBe(0);

      button.setIconId(icons.GEAR);
      expect(button.$buttonLabel).toBeHidden();
      expect(button.$buttonLabel.html()).toBe('Options');
      expect(button.get$Icon()).toBeVisible();

      button.setLabelVisible(true);
      expect(button.$buttonLabel).toBeVisible();
      expect(button.$buttonLabel.html()).toBe('Options');
      expect(button.get$Icon()).toBeVisible();
    });
  });
});
