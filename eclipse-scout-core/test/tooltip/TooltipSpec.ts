/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {keys, Menu, ModelOf, scout, Tooltip} from '../../src/index';
import {JQueryTesting} from '../../src/testing';

describe('Tooltip', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('withFocusContext', () => {
    function createTooltip(model?: ModelOf<Tooltip>): Tooltip {
      return scout.create(Tooltip, {
        parent: session.desktop,
        $anchor: session.$entryPoint,
        text: 'tooltip',
        withFocusContext: true,
        ...model
      });
    }

    it('sets a tabindex on the tooltip', () => {
      let tooltip = createTooltip();
      tooltip.render();
      expect(tooltip.$container).toHaveAttr('tabindex', '-1');

      tooltip.setWithFocusContext(false);
      expect(tooltip.$container).not.toHaveAttr('tabindex');

      tooltip.setWithFocusContext(true);
      expect(tooltip.$container).toHaveAttr('tabindex', '-1');
    });

    it('installs a focus context', () => {
      let $input = session.$entryPoint.appendElement('<input>');
      $input[0].focus();

      let tooltip = createTooltip();
      tooltip.render();
      expect(tooltip.$container[0]).toBeFocused();

      tooltip.remove();
      expect($input[0]).toBeFocused();

      tooltip.render();
      expect(tooltip.$container[0]).toBeFocused();

      tooltip.setWithFocusContext(false);
      expect($input[0]).toBeFocused();

      tooltip.setWithFocusContext(true);
      expect(tooltip.$container[0]).toBeFocused();
    });

    it('makes the tooltip closable using ESC', () => {
      let tooltip = createTooltip();
      tooltip.render();

      JQueryTesting.triggerKeyDown(tooltip.$container, keys.ESC);
      expect(tooltip.destroyed).toBe(true);

      // Also works if autoRemove is false
      tooltip = createTooltip({autoRemove: false});
      tooltip.render();

      JQueryTesting.triggerKeyDown(tooltip.$container, keys.ESC);
      expect(tooltip.destroyed).toBe(true);
    });

    it('does not close the tooltip on keypress when focus is inside even if autoRemove is true', () => {
      let tooltip = createTooltip();
      tooltip.render();
      expect(tooltip.autoRemove).toBe(true);

      JQueryTesting.triggerKeyDown(tooltip.$container, keys.A);
      expect(tooltip.rendered).toBe(true);

      JQueryTesting.triggerKeyDown(session.$entryPoint, keys.A);
      expect(tooltip.destroyed).toBe(true);
    });

    it('allows the use of menu keystrokes', () => {
      let tooltip = createTooltip();
      tooltip.render();
      tooltip.setMenus([{
        objectType: Menu
      }, {
        objectType: Menu,
        childActions: [{
          objectType: Menu
        }, {
          objectType: Menu
        }]
      }]);
      expect(tooltip.$container[0]).toBeFocused();
      expect(tooltip.menus[0].$container).not.toHaveClass('focused');
      expect(tooltip.menus[1].$container).not.toHaveClass('focused');

      JQueryTesting.triggerKeyDown(tooltip.$container, keys.DOWN);
      expect(tooltip.menus[0].$container).toHaveClass('focused');
      expect(tooltip.menus[1].$container).not.toHaveClass('focused');

      JQueryTesting.triggerKeyDown(tooltip.$container, keys.DOWN);
      expect(tooltip.menus[0].$container).not.toHaveClass('focused');
      expect(tooltip.menus[1].$container).toHaveClass('focused');
      expect(tooltip.menus[1].$container).not.toHaveClass('selected');

      session.desktop.one('popupOpen', event => {
        event.popup.animateOpening = false;
      });
      JQueryTesting.triggerKeyDown(tooltip.$container, keys.SPACE);
      expect(tooltip.menus[1].popup.$container[0]).toBeFocused();
      expect(tooltip.menus[0].$container).not.toHaveClass('focused');
      expect(tooltip.menus[1].$container).toHaveClass('focused');
      expect(tooltip.menus[1].$container).toHaveClass('selected');

      JQueryTesting.triggerKeyDown(tooltip.menus[1].popup.$container, keys.DOWN);
      expect(tooltip.menus[1].popup.$container.find('.menu-item').eq(0)).toHaveClass('focused');
      expect(tooltip.menus[1].popup.$container.find('.menu-item').eq(1)).not.toHaveClass('focused');

      JQueryTesting.triggerKeyDown(tooltip.menus[1].popup.$container, keys.DOWN);
      expect(tooltip.menus[1].popup.$container.find('.menu-item').eq(0)).not.toHaveClass('focused');
      expect(tooltip.menus[1].popup.$container.find('.menu-item').eq(1)).toHaveClass('focused');

      tooltip.menus[1].popup.animateRemoval = false;
      JQueryTesting.triggerKeyDown(tooltip.menus[1].popup.$container, keys.ESC);
      expect(tooltip.menus[0].$container).not.toHaveClass('focused');
      expect(tooltip.menus[1].$container).toHaveClass('focused');
      expect(tooltip.menus[1].$container).not.toHaveClass('selected');
      expect(tooltip.menus[1].popup).toBe(null);

      JQueryTesting.triggerKeyDown(tooltip.$container, keys.UP);
      expect(tooltip.menus[0].$container).toHaveClass('focused');
      expect(tooltip.menus[1].$container).not.toHaveClass('focused');

      JQueryTesting.triggerKeyDown(tooltip.$container, keys.ESC);
      expect(tooltip.destroyed).toBe(true);
    });
  });
});
