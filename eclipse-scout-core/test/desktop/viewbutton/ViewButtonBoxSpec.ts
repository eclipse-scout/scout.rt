/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {scout, ViewButton, ViewButtonBox} from '../../../src/index';

describe('ViewButtonBox', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('viewMenuTab', () => {
    let viewButtonBox: ViewButtonBox;

    beforeEach(() => {
      viewButtonBox = scout.create(ViewButtonBox, {
        parent: session.desktop,
        viewButtons: [scout.create(ViewButton, {
          parent: session.desktop,
          text: 'Button1',
          displayStyle: 'MENU',
          visible: true
        })]
      });
    });

    it('is only visible if there are at least 2 visible view buttons with displayStyle == "MENU"', () => {
      let viewButtons = [
        scout.create(ViewButton, {
          parent: session.desktop,
          text: 'Button1',
          displayStyle: 'MENU',
          visible: true
        }),
        scout.create(ViewButton, {
          parent: session.desktop,
          text: 'Button2',
          displayStyle: 'MENU',
          visible: true
        })
      ];
      viewButtonBox.setViewButtons(viewButtons);
      viewButtonBox.render();
      expect(viewButtonBox.viewMenuTab.visible).toBe(true);
    });

    it('is not visible if there is only 1 visible view buttons with displayStyle == "MENU"', () => {
      viewButtonBox.render();
      expect(viewButtonBox.viewMenuTab.visible).toBe(false);
    });

    it('is not visible if there are no visible view buttons ith displayStyle == "MENU"', () => {
      viewButtonBox.viewButtons[0].visible = false;
      viewButtonBox.render();
      expect(viewButtonBox.viewMenuTab.visible).toBe(false);
    });

    it('is not visible if there are visible view buttons with displayStyle == "TAB"', () => {
      viewButtonBox.viewButtons[0].displayStyle = 'TAB';
      viewButtonBox.render();
      expect(viewButtonBox.viewMenuTab.visible).toBe(false);
    });

    it('is not visible if there are no view buttons at all', () => {
      viewButtonBox.viewButtons = [];
      viewButtonBox.render();
      expect(viewButtonBox.viewMenuTab.visible).toBe(false);
    });

  });

  describe('viewButtons', () => {
    let viewButtonBox: ViewButtonBox;

    beforeEach(() => {
      viewButtonBox = scout.create(ViewButtonBox, {
        parent: session.desktop
      });
    });

    it('will be rendered as view tab when only one button with displayStyle == "MENU"', () => {
      let viewButtons = [
        scout.create(ViewButton, {
          parent: session.desktop,
          text: 'Button1',
          displayStyle: 'MENU',
          visible: true
        })
      ];
      viewButtonBox.setViewButtons(viewButtons);
      viewButtonBox.render();
      expect(viewButtonBox.viewMenuTab.visible).toBe(false);
      expect(viewButtonBox.tabButtons.length).toBe(1);
      expect(viewButtonBox.menuButtons.length).toBe(0);
    });

    /**
     * if only one button with display style "MENU" is visible this button is rendered as tabButton since a dropdown
     *  with only one menu dos not make sense.
     */
    it('will be rendered as menuButtons when two button with displayStyle == "MENU"', () => {
      let viewButtons = [
        scout.create(ViewButton, {
          parent: session.desktop,
          text: 'Button1',
          displayStyle: 'MENU',
          visible: true
        }),
        scout.create(ViewButton, {
          parent: session.desktop,
          text: 'Button2',
          displayStyle: 'MENU',
          visible: true
        })
      ];
      viewButtonBox.setViewButtons(viewButtons);
      viewButtonBox.render();
      expect(viewButtonBox.viewMenuTab.visible).toBe(true);
      expect(viewButtonBox.tabButtons.length).toBe(0);
      expect(viewButtonBox.menuButtons.length).toBe(2);
    });

    it('will be rendered correctly when displayStyle changes dynamically.', () => {
      let viewButtons = [
        scout.create(ViewButton, {
          parent: session.desktop,
          text: 'Button1',
          displayStyle: 'MENU',
          visible: true
        }),
        scout.create(ViewButton, {
          parent: session.desktop,
          text: 'Button2',
          displayStyle: 'MENU',
          visible: true
        })
      ];
      viewButtonBox.setViewButtons(viewButtons);
      viewButtonBox.render();
      expect(viewButtonBox.viewMenuTab.visible).toBe(true);
      expect(viewButtonBox.tabButtons.length).toBe(0);
      expect(viewButtonBox.menuButtons.length).toBe(2);

      viewButtons[0].setDisplayStyle('TAB');
      expect(viewButtonBox.viewMenuTab.visible).toBe(false);
      expect(viewButtonBox.tabButtons.length).toBe(2);
      expect(viewButtonBox.menuButtons.length).toBe(0);
    });

    it('will be rendered correctly when visibility changes dynamically.', () => {
      let viewButtons = [
        scout.create(ViewButton, {
          parent: session.desktop,
          text: 'Button1',
          displayStyle: 'MENU',
          visible: true
        }),
        scout.create(ViewButton, {
          parent: session.desktop,
          text: 'Button2',
          displayStyle: 'MENU',
          visible: true
        })
      ];
      viewButtonBox.setViewButtons(viewButtons);
      viewButtonBox.render();
      expect(viewButtonBox.viewMenuTab.visible).toBe(true);
      expect(viewButtonBox.tabButtons.length).toBe(0);
      expect(viewButtonBox.menuButtons.length).toBe(2);

      viewButtons[0].setVisible(false);
      expect(viewButtonBox.viewMenuTab.visible).toBe(false);
      expect(viewButtonBox.tabButtons.length).toBe(1);
      expect(viewButtonBox.menuButtons.length).toBe(0);
    });

    it('will remember the current view button when selecting another tab', () => {
      let viewButtons = [
        scout.create(ViewButton, {
          parent: session.desktop,
          text: 'Button1',
          displayStyle: 'MENU',
          selected: true
        }),
        scout.create(ViewButton, {
          parent: session.desktop,
          text: 'Button2',
          displayStyle: 'MENU'
        }),
        scout.create(ViewButton, {
          parent: session.desktop,
          text: 'Button3',
          displayStyle: 'TAB'
        })
      ];
      viewButtonBox.setViewButtons(viewButtons);

      expect(viewButtonBox.viewMenuTab.visible).toBe(true);
      expect(viewButtonBox.tabButtons.length).toBe(1);
      expect(viewButtonBox.menuButtons.length).toBe(2);

      expect(viewButtonBox.viewMenuTab.selected).toBe(true);
      expect(viewButtonBox.viewMenuTab.selectedButton).toBeDefined();
      expect(viewButtonBox.viewMenuTab.selectedButton.cloneOf).toBe(viewButtons[0]);
      expect(viewButtons[0].selected).toBe(true);
      expect(viewButtons[1].selected).toBe(false);
      expect(viewButtons[2].selected).toBe(false);
      expect(viewButtons[0].selectedAsMenu).toBe(true);
      expect(viewButtons[1].selectedAsMenu).toBe(false);
      expect(viewButtons[2].selectedAsMenu).toBe(false);

      viewButtons[0].setSelected(false);
      viewButtons[2].setSelected(true);
      expect(viewButtonBox.viewMenuTab.selected).toBe(false);
      expect(viewButtonBox.viewMenuTab.selectedButton).toBeDefined();
      expect(viewButtonBox.viewMenuTab.selectedButton.cloneOf).toBe(viewButtons[0]);
      expect(viewButtons[0].selected).toBe(false);
      expect(viewButtons[1].selected).toBe(false);
      expect(viewButtons[2].selected).toBe(true);
      expect(viewButtons[0].selectedAsMenu).toBe(true);
      expect(viewButtons[1].selectedAsMenu).toBe(false);
      expect(viewButtons[2].selectedAsMenu).toBe(false);

      viewButtons[2].setSelected(false);
      viewButtons[1].setSelected(true);
      expect(viewButtonBox.viewMenuTab.selected).toBe(true);
      expect(viewButtonBox.viewMenuTab.selectedButton).toBeDefined();
      expect(viewButtonBox.viewMenuTab.selectedButton.cloneOf).toBe(viewButtons[1]);
      expect(viewButtons[0].selected).toBe(false);
      expect(viewButtons[1].selected).toBe(true);
      expect(viewButtons[2].selected).toBe(false);
      expect(viewButtons[0].selectedAsMenu).toBe(false);
      expect(viewButtons[1].selectedAsMenu).toBe(true);
      expect(viewButtons[2].selectedAsMenu).toBe(false);

      viewButtons[1].setSelected(false);
      viewButtons[2].setSelected(true);
      expect(viewButtonBox.viewMenuTab.selected).toBe(false);
      expect(viewButtonBox.viewMenuTab.selectedButton).toBeDefined();
      expect(viewButtonBox.viewMenuTab.selectedButton.cloneOf).toBe(viewButtons[1]);
      expect(viewButtons[0].selected).toBe(false);
      expect(viewButtons[1].selected).toBe(false);
      expect(viewButtons[2].selected).toBe(true);
      expect(viewButtons[0].selectedAsMenu).toBe(false);
      expect(viewButtons[1].selectedAsMenu).toBe(true);
      expect(viewButtons[2].selectedAsMenu).toBe(false);
    });
  });

  describe('aria properties', () => {
    let viewButtonBox: ViewButtonBox;

    beforeEach(() => {
      viewButtonBox = scout.create(ViewButtonBox, {
        parent: session.desktop,
        viewButtons: [scout.create(ViewButton, {
          parent: session.desktop,
          text: 'Button1',
          displayStyle: 'MENU',
          visible: true
        })]
      });
    });

    it('has view button with aria role button', () => {
      viewButtonBox.render();
      expect(viewButtonBox.viewButtons[0].$container).toHaveAttr('role', 'button');
    });

    it('has view button with aria-label set', () => {
      viewButtonBox.render();
      expect(viewButtonBox.viewButtons[0].$container.attr('aria-label')).toBeTruthy();
      expect(viewButtonBox.viewButtons[0].$container.attr('aria-labelledby')).toBeFalsy();
    });
  });
});
