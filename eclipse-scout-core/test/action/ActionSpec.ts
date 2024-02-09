/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Action, keys, scout, Tooltip, tooltips} from '../../src/index';
import {JQueryTesting} from '../../src/testing';

describe('Action', () => {
  let $sandbox: JQuery, session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = $('#sandbox');
  });

  describe('defaults', () => {

    it('should be as expected', () => {
      let action = new Action();
      action.init(createSimpleModel('Action', session));
      expect(action.tabbable).toBe(false);
      expect(action.actionStyle).toBe(Action.ActionStyle.DEFAULT);
    });

  });

  describe('setTabbable', () => {

    it('should modify $container tabindex', () => {
      let action = new Action();
      action.init(createSimpleModel('Action', session));
      // because Action is 'abstract' and has no _render method yet
      // but _renderProperties() is called anyway
      action.$container = $sandbox;
      action.render($sandbox);
      expect(action.$container.attr('tabindex')).toBe(undefined);

      action.setTabbable(true);
      expect(action.$container.attr('tabindex')).toBe('0');
    });

  });

  describe('key stroke', () => {

    it('triggers action', () => {
      let action = scout.create(Action, {
        parent: session.desktop,
        keyStroke: 'ctrl-x'
      });
      session.desktop.keyStrokeContext.registerKeyStroke(action);
      action.render();
      let executed = 0;
      action.on('action', event => {
        executed++;
      });

      expect(executed).toBe(0);
      JQueryTesting.triggerKeyInputCapture(session.desktop.$container, keys.X, 'ctrl');
      expect(executed).toBe(1);
    });

    it('is not triggered if another action with the same key stroke handled it first', () => {
      let action = scout.create(Action, {
        parent: session.desktop,
        keyStroke: 'ctrl-x'
      });
      session.desktop.keyStrokeContext.registerKeyStroke(action);
      let actionExecuted = 0;
      action.on('action', event => {
        actionExecuted++;
      });
      action.render();

      let action2 = scout.create(Action, {
        parent: session.desktop,
        keyStroke: 'ctrl-x'
      });
      session.desktop.keyStrokeContext.registerKeyStroke(action2);
      let action2Executed = 0;
      action2.on('action', event => {
        action2Executed++;
      });
      action2.render();

      expect(actionExecuted).toBe(0);
      expect(action2Executed).toBe(0);
      JQueryTesting.triggerKeyInputCapture(session.desktop.$container, keys.X, 'ctrl');
      expect(actionExecuted).toBe(1);
      expect(action2Executed).toBe(0);
    });

  });

  describe('action event', () => {

    it('is triggered when doAction is called', () => {
      let action = scout.create(Action, {
        parent: session.desktop
      });
      let executed = 0;
      action.on('action', event => {
        executed++;
      });

      expect(executed).toBe(0);
      action.doAction();
      expect(executed).toBe(1);
    });

    it('is fired when doAction is called even if it is a toggle action', () => {
      let action = scout.create(Action, {
        parent: session.desktop,
        toggleAction: true
      });
      let executed = 0;
      let selected = null;
      action.on('action', event => {
        // State is already changed so that listener can react on new state
        selected = action.selected;
        executed++;
      });
      expect(executed).toBe(0);
      expect(selected).toBe(null);

      action.doAction();
      expect(executed).toBe(1);
      expect(selected).toBe(true);

      action.doAction();
      expect(executed).toBe(2);
      expect(selected).toBe(false);
    });

  });

  describe('tooltipText', () => {
    beforeEach(() => {
      jasmine.clock().install();
    });

    afterEach(() => {
      jasmine.clock().uninstall();
    });

    it('is shown on mouseover after a short delay ', () => {
      let action = scout.create(Action, {
        parent: session.desktop,
        tooltipText: 'tooltip'
      });
      action.render();
      JQueryTesting.triggerMouseEnter(action.$container);
      expect(action.findChild(Tooltip)).toBeFalsy();

      jasmine.clock().tick(tooltips.DEFAULT_TOOLTIP_DELAY - 100);
      expect(action.findChild(Tooltip)).toBeFalsy();

      jasmine.clock().tick(100);
      expect(action.findChild(Tooltip).rendered).toBe(true);
    });

    it('is shown even if action is disabled', () => {
      let action = scout.create(Action, {
        parent: session.desktop,
        tooltipText: 'tooltip',
        enabled: false
      });
      action.render();
      JQueryTesting.triggerMouseEnter(action.$container);
      expect(action.findChild(Tooltip)).toBeFalsy();

      jasmine.clock().tick(tooltips.DEFAULT_TOOLTIP_DELAY);
      expect(action.findChild(Tooltip).rendered).toBe(true);
    });
  });

  describe('aria properties', () => {

    it('has aria role button', () => {
      let action = scout.create(Action, {
        parent: session.desktop,
        toggleAction: false
      });
      action.render();
      expect(action.$container).toHaveAttr('role', 'button');
    });

    it('has aria label set if text is not visible', () => {
      let action = scout.create(Action, {
        parent: session.desktop,
        toggleAction: false,
        text: 'hello',
        actionStyle: Action.ActionStyle.BUTTON
      });
      action.setTextVisible(false);
      action.render();
      expect(action.$container.attr('aria-label')).toBeTruthy();
      expect(action.$container).toHaveAttr('aria-label', 'hello');
      expect(action.$container.attr('aria-labelledBy')).toBeFalsy();
    });

    it('has aria description set if there is a tooltip', () => {
      let action = scout.create(Action, {
        parent: session.desktop,
        toggleAction: false,
        tooltipText: 'hello',
        actionStyle: Action.ActionStyle.BUTTON
      });
      action.render();

      expect(action.$container.attr('aria-description')).toBeTruthy();
      expect(action.$container.attr('aria-description')).toBe('hello');
      expect(action.$container.attr('aria-describedby')).toBeFalsy();
    });

    it('has aria pressed set correctly if toggle action', () => {
      let action = scout.create(Action, {
        parent: session.desktop,
        toggleAction: true
      });
      action.render();
      expect(action.$container).toHaveAttr('aria-pressed', 'false');
      action.setSelected(true);
      expect(action.$container).toHaveAttr('aria-pressed', 'true');
      action.setToggleAction(false);
      expect(action.$container.attr('aria-pressed')).toBeFalsy();
    });
  });
});
