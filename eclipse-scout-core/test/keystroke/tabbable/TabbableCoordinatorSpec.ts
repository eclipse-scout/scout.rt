/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  Action, aria, AriaOrientation, AriaRole, FocusNextTabTargetKeyStroke, FocusPreviousTabTargetKeyStroke, InitModelOf, keys, KeyStroke, KeyStrokeContext, scout, TabbableCoordinator, TabbableCoordinatorModel, Widget, WidgetModel
} from '../../../src';
import {JQueryTesting} from '../../../src/testing';

describe('TabbableCoordinator', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  function createActions(): Action[] {
    let actions: Action[] = [];
    for (let i = 0; i < 3; i++) {
      actions.push(scout.create(Action, {parent: session.desktop, text: `Action ${i}`}));
    }
    return actions;
  }

  class ActionBar extends Widget {
    declare model: ActionBarModel;
    actions: Action[] = [];
    tabbableCoordinator: TabbableCoordinator;

    protected override _init(model: InitModelOf<this>) {
      super._init(model);
      this.tabbableCoordinator = scout.create(TabbableCoordinator, {parent: this, ...model.tabbableCoordinatorModel});
      this.tabbableCoordinator.setItems(this.actions);
    }

    protected override _render() {
      this.$container = this.$parent.appendDiv();
      this.actions.forEach(action => action.render(this.$container));
    }
  }

  class ActionBarWithKeyStrokeContext extends ActionBar {
    protected override _createKeyStrokeContext(): KeyStrokeContext {
      return new KeyStrokeContext();
    }
  }

  class ActionBarWithChildContext extends ActionBar {

    protected override _render() {
      this.$container = this.$parent.appendDiv();
      let $child = this.$container.appendDiv('child');
      let keyStrokeContext = new KeyStrokeContext({$bindTarget: $child, $scopeTarget: $child});
      this.session.keyStrokeManager.installKeyStrokeContext(keyStrokeContext);
      this.tabbableCoordinator.registerKeyStrokes(this, keyStrokeContext);
      this.actions.forEach(action => action.render($child));
    }
  }

  interface ActionBarModel extends WidgetModel {
    actions?: Action[];
    tabbableCoordinatorModel?: TabbableCoordinatorModel;
  }

  describe('currentItem', () => {
    it('is initially set to the first item that is a tab target', () => {
      let actions = createActions();
      actions[0].setEnabled(false);
      let tabbableCoordinator = scout.create(TabbableCoordinator, {parent: session.desktop});
      tabbableCoordinator.setItems(actions);
      expect(tabbableCoordinator.currentItem).toBe(actions[1]);
    });

    it('is initially set to the item provided by initial item provider', () => {
      let actions = createActions();
      actions[0].setEnabled(false);
      let tabbableCoordinator = scout.create(TabbableCoordinator, {
        parent: session.desktop,
        initialItemProvider: () => actions[2]
      });
      tabbableCoordinator.setItems(actions);
      expect(tabbableCoordinator.currentItem).toBe(actions[2]);
    });

    it('is initially set to the item provided by initial item provider but only if it is a tab target', () => {
      let actions = createActions();
      actions[0].setEnabled(false);
      actions[2].setEnabled(false);
      let tabbableCoordinator = scout.create(TabbableCoordinator, {
        parent: session.desktop,
        initialItemProvider: () => actions[2]
      });
      tabbableCoordinator.setItems(actions);
      expect(tabbableCoordinator.currentItem).toBe(actions[1]);
    });

    it('is the only tabbable element', () => {
      let actions = createActions();
      actions[1].setTabbable(true); // Should be set to false when passed to the coordinator
      actions.forEach(action => action.render());

      let tabbableCoordinator = scout.create(TabbableCoordinator, {parent: session.desktop});
      tabbableCoordinator.setItems(actions);
      expect(tabbableCoordinator.currentItem).toBe(actions[0]);
      expect(actions[0].$container).toHaveAttr('tabindex', '0');
      expect(actions[1].$container).not.toHaveAttr('tabindex');
      expect(actions[2].$container).not.toHaveAttr('tabindex');

      tabbableCoordinator.setCurrentItem(actions[1]);
      expect(tabbableCoordinator.currentItem).toBe(actions[1]);
      expect(actions[0].$container).not.toHaveAttr('tabindex');
      expect(actions[1].$container).toHaveAttr('tabindex', '0');
      expect(actions[2].$container).not.toHaveAttr('tabindex');

      tabbableCoordinator.setCurrentItem(null);
      expect(actions[0].$container).not.toHaveAttr('tabindex');
      expect(actions[1].$container).not.toHaveAttr('tabindex');
      expect(actions[2].$container).not.toHaveAttr('tabindex');

      actions.forEach(action => action.remove());
      tabbableCoordinator.setCurrentItem(actions[1]);
      expect(actions[0].tabbable).toBe(false);
      expect(actions[1].tabbable).toBe(true);
      expect(actions[0].tabbable).toBe(false);

      actions.forEach(action => action.render());
      expect(actions[0].$container).not.toHaveAttr('tabindex');
      expect(actions[1].$container).toHaveAttr('tabindex', '0');
      expect(actions[2].$container).not.toHaveAttr('tabindex');
    });

    it('is reset if a tab target relevant property change happens on the currentItem', () => {
      let actions = createActions();
      let tabbableCoordinator = scout.create(TabbableCoordinator, {parent: session.desktop});
      tabbableCoordinator.setItems(actions);
      expect(tabbableCoordinator.currentItem).toBe(actions[0]);

      actions[0].setEnabled(false);
      expect(tabbableCoordinator.currentItem).toBe(actions[1]);

      actions[1].setVisible(false);
      expect(tabbableCoordinator.currentItem).toBe(actions[2]);

      actions[2].setVisible(false);
      expect(tabbableCoordinator.currentItem).toBe(undefined); // There is no tab target anymore
    });

    it('is reset if the initial item turns into a tab target', () => {
      let actions = createActions();
      let tabbableCoordinator = scout.create(TabbableCoordinator, {parent: session.desktop});
      tabbableCoordinator.setItems(actions);
      actions[0].setEnabled(false);
      actions[2].setEnabled(false);
      expect(tabbableCoordinator.currentItem).toBe(actions[1]);

      actions[2].setEnabled(true);
      expect(tabbableCoordinator.currentItem).toBe(actions[1]); // Nothing happens, initial item not affected

      actions[0].setEnabled(true);
      expect(tabbableCoordinator.currentItem).toBe(actions[0]);
    });

    it('is set to the initial tab target on property change if no current item has been set yet', () => {
      let actions = createActions();
      actions.forEach(action => action.setEnabled(false));
      let tabbableCoordinator = scout.create(TabbableCoordinator, {parent: session.desktop});
      tabbableCoordinator.setItems(actions);
      expect(tabbableCoordinator.currentItem).toBe(undefined);

      actions[2].setEnabled(true);
      expect(tabbableCoordinator.currentItem).toBe(actions[2]);
    });

    it('is reset if items change', () => {
      let actions = createActions();
      let tabbableCoordinator = scout.create(TabbableCoordinator, {parent: session.desktop});
      tabbableCoordinator.setItems(actions);
      expect(tabbableCoordinator.currentItem).toBe(actions[0]);

      tabbableCoordinator.setItems([actions[1], actions[2]]);
      expect(tabbableCoordinator.currentItem).toBe(actions[1]);

      tabbableCoordinator.setItems([actions[1], actions[2]]);
      expect(tabbableCoordinator.currentItem).toBe(actions[1]); // Still 1 because items did not change

      tabbableCoordinator.setItems([]);
      expect(tabbableCoordinator.currentItem).toBe(undefined);
    });

    it('is not reset if items change but current item is focused', () => {
      // Use case: focus is in a menu box which adds ellipsis menu dynamically while the user resizes the screen
      // This should not change current item because it is focused
      let actions = createActions();
      actions.forEach(action => action.render());
      let tabbableCoordinator = scout.create(TabbableCoordinator, {parent: session.desktop});
      tabbableCoordinator.setItems([actions[0], actions[1]]);
      expect(tabbableCoordinator.currentItem).toBe(actions[0]);

      tabbableCoordinator.setCurrentItem(actions[1]);
      actions[1].focus();
      tabbableCoordinator.setItems(actions);
      expect(tabbableCoordinator.currentItem).toBe(actions[1]); // Don't change to action0 because action1 is focused
    });
  });

  describe('setCurrentItem', () => {
    it('focuses the new current item if it was focused before', () => {
      // Use case: if the currently focused item is removed or not a tab target anymore, the focus should stay in the tabbable group and not reset to another widget or event the body
      let actions = createActions();
      actions.forEach(action => action.render());
      let tabbableCoordinator = scout.create(TabbableCoordinator, {parent: session.desktop});
      tabbableCoordinator.setItems(actions);
      expect(tabbableCoordinator.currentItem).toBe(actions[0]);
      expect(actions[0].isFocused()).toBe(false);

      actions[0].focus();
      expect(actions[0].isFocused()).toBe(true);

      tabbableCoordinator.setCurrentItem(actions[1]);
      expect(actions[1].isFocused()).toBe(true);

      // Use case: a toggle menu that is not a tab target anymore if it is selected.
      // In this spec, enabled is used instead of selected.
      // property change calls resetCurrentItem
      actions[1].setEnabled(false);
      expect(actions[0].isFocused()).toBe(true);

      // The same should happen if the item is removed completely
      // Use case: focus is on ellipsis menu in menu box and ellipsis menu removed because user resizes the screen
      // setItems calls resetCurrentItem
      tabbableCoordinator.setCurrentItem(actions[2]);
      expect(actions[2].isFocused()).toBe(true);
      actions[2].remove();
      tabbableCoordinator.setItems([actions[0], actions[1]]);
      // Actually, this should be true, but it is complex to implement because focus will be set to another element on remove before tabbable coordinator can react
      expect(actions[0].isFocused()).toBe(false);

      tabbableCoordinator.setCurrentItem(null);
      expect(actions[0].isFocused()).toBe(false);
    });

    it('focuses the new current item even if the parent was re-rendered', () => {
      let actions = createActions();
      let actionBar = scout.create(ActionBar, {parent: session.desktop, actions});
      actions.forEach(action => action.setParent(actionBar));
      actionBar.render();
      actions[0].focus();
      expect(actions[0].isFocused()).toBe(true);

      actionBar.tabbableCoordinator.setCurrentItem(actions[1]);
      expect(actions[1].isFocused()).toBe(true);

      actionBar.remove();
      actionBar.render();
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actions[1]);

      actions[1].focus();
      expect(actions[1].isFocused()).toBe(true);

      actionBar.tabbableCoordinator.setCurrentItem(actions[2]);
      expect(actions[2].isFocused()).toBe(true);

      actionBar.tabbableCoordinator.setCurrentItem(actions[0]);
      expect(actions[0].isFocused()).toBe(true);
    });
  });

  describe('left/right keystrokes', () => {

    it('change currentItem and focus', () => {
      let actionBar = scout.create(ActionBar, {parent: session.desktop, actions: createActions()});
      actionBar.render();
      actionBar.actions[0].focus();
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[0]);
      expect(actionBar.actions[0].$container).toBeFocused();

      JQueryTesting.triggerKeyDown(actionBar.$container, keys.RIGHT);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[1]);
      expect(actionBar.actions[1].$container).toBeFocused();

      JQueryTesting.triggerKeyDown(actionBar.$container, keys.RIGHT);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[2]);
      expect(actionBar.actions[2].$container).toBeFocused();

      JQueryTesting.triggerKeyDown(actionBar.$container, keys.RIGHT);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[2]);
      expect(actionBar.actions[2].$container).toBeFocused();

      JQueryTesting.triggerKeyDown(actionBar.$container, keys.LEFT);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[1]);
      expect(actionBar.actions[1].$container).toBeFocused();

      JQueryTesting.triggerKeyDown(actionBar.$container, keys.LEFT);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[0]);
      expect(actionBar.actions[0].$container).toBeFocused();

      JQueryTesting.triggerKeyDown(actionBar.$container, keys.LEFT);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[0]);
      expect(actionBar.actions[0].$container).toBeFocused();
    });

    it('consider tab targets', () => {
      let actionBar = scout.create(ActionBar, {parent: session.desktop, actions: createActions()});
      actionBar.render();
      actionBar.actions[0].focus();
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[0]);
      expect(actionBar.actions[0].$container).toBeFocused();

      actionBar.actions[1].setEnabled(false);
      JQueryTesting.triggerKeyDown(actionBar.$container, keys.RIGHT);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[2]);
      expect(actionBar.actions[2].$container).toBeFocused();

      JQueryTesting.triggerKeyDown(actionBar.$container, keys.LEFT);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[0]);
      expect(actionBar.actions[0].$container).toBeFocused();
    });

    it('don\'t have an effect if orientation is set to vertical', () => {
      let actionBar = scout.create(ActionBar, {
        parent: session.desktop, actions: createActions(), tabbableCoordinatorModel: {
          orientation: 'vertical'
        }
      });
      actionBar.render();
      actionBar.actions[0].focus();
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[0]);
      expect(actionBar.actions[0].$container).toBeFocused();

      JQueryTesting.triggerKeyDown(actionBar.$container, keys.RIGHT);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[0]);
      expect(actionBar.actions[0].$container).toBeFocused(); // Nothing changed

      JQueryTesting.triggerKeyDown(actionBar.$container, keys.DOWN);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[1]);
      expect(actionBar.actions[1].$container).toBeFocused();

      JQueryTesting.triggerKeyDown(actionBar.$container, keys.LEFT);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[1]);
      expect(actionBar.actions[1].$container).toBeFocused(); // Nothing changed

      JQueryTesting.triggerKeyDown(actionBar.$container, keys.UP);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[0]);
      expect(actionBar.actions[0].$container).toBeFocused();

      // Orientation can be changed on the fly
      actionBar.tabbableCoordinator.setOrientation('horizontal');
      JQueryTesting.triggerKeyDown(actionBar.$container, keys.RIGHT);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[1]);
      expect(actionBar.actions[1].$container).toBeFocused(); // left/right active again

      JQueryTesting.triggerKeyDown(actionBar.$container, keys.DOWN);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[1]);
      expect(actionBar.actions[1].$container).toBeFocused(); // No effect

      actionBar.tabbableCoordinator.setOrientation('vertical');
      JQueryTesting.triggerKeyDown(actionBar.$container, keys.RIGHT);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[1]);
      expect(actionBar.actions[1].$container).toBeFocused(); // No effect

      JQueryTesting.triggerKeyDown(actionBar.$container, keys.DOWN);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[2]);
      expect(actionBar.actions[2].$container).toBeFocused();

      actionBar.tabbableCoordinator.setOrientation('both');
      JQueryTesting.triggerKeyDown(actionBar.$container, keys.LEFT);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[1]);
      expect(actionBar.actions[1].$container).toBeFocused();

      JQueryTesting.triggerKeyDown(actionBar.$container, keys.UP);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[0]);
      expect(actionBar.actions[0].$container).toBeFocused();
    });

    it('work the same way as up/down if orientation is set to both', () => {
      let actionBar = scout.create(ActionBar, {
        parent: session.desktop, actions: createActions(), tabbableCoordinatorModel: {
          orientation: 'both'
        }
      });
      actionBar.render();
      actionBar.actions[0].focus();
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[0]);
      expect(actionBar.actions[0].$container).toBeFocused();

      JQueryTesting.triggerKeyDown(actionBar.$container, keys.RIGHT);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[1]);
      expect(actionBar.actions[1].$container).toBeFocused();

      JQueryTesting.triggerKeyDown(actionBar.$container, keys.DOWN);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[2]);
      expect(actionBar.actions[2].$container).toBeFocused();

      JQueryTesting.triggerKeyDown(actionBar.$container, keys.LEFT);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[1]);
      expect(actionBar.actions[1].$container).toBeFocused();

      JQueryTesting.triggerKeyDown(actionBar.$container, keys.UP);
      expect(actionBar.tabbableCoordinator.currentItem).toBe(actionBar.actions[0]);
      expect(actionBar.actions[0].$container).toBeFocused();
    });
  });

  it('removes listeners if widget is destroyed', () => {
    let actions = createActions();
    let initialEventCount = actions[0].events.count();

    let actionBar = scout.create(ActionBar, {parent: session.desktop, actions: actions});
    expect(actions[0].events.count()).toBeGreaterThan(initialEventCount);

    actionBar.destroy();
    expect(actions[0].events.count()).toBe(initialEventCount);
  });

  describe('registerKeyStrokes', () => {
    it('is called during init and registers the navigation keystrokes if autoRegisterKeyStrokes is true', () => {
      let actionBar = scout.create(ActionBar, {parent: session.desktop});
      expect(actionBar.keyStrokeContext.keyStrokes.length).toBe(2);
      expect(actionBar.keyStrokeContext.keyStrokes[0]).toBeInstanceOf(FocusPreviousTabTargetKeyStroke);
      expect(actionBar.keyStrokeContext.keyStrokes[1]).toBeInstanceOf(FocusNextTabTargetKeyStroke);

      let actionBar2 = scout.create(ActionBar, {
        parent: session.desktop, tabbableCoordinatorModel: {
          autoRegisterKeyStrokes: false
        }
      });
      expect(actionBar2.keyStrokeContext).toBe(null);

      let actionBar3 = scout.create(ActionBarWithKeyStrokeContext, {
        parent: session.desktop, tabbableCoordinatorModel: {
          autoRegisterKeyStrokes: false
        }
      });
      expect(actionBar3.keyStrokeContext.keyStrokes.length).toBe(0);
    });

    it('creates a keystroke context on the parent if there is none yet', () => {
      let actionBar = new ActionBar();
      expect(actionBar.keyStrokeContext).toBe(null);
      actionBar.init({parent: session.desktop});
      expect(actionBar.keyStrokeContext).toBeDefined();

      let actionBar2 = new ActionBarWithKeyStrokeContext();
      let context = actionBar2.keyStrokeContext;
      expect(actionBar2.keyStrokeContext).not.toBe(null);
      actionBar2.init({parent: session.desktop});
      expect(actionBar2.keyStrokeContext).toBe(context); // must not be changed
    });

    it('registers navigation keystrokes on demand', () => {
      let actionBar = scout.create(ActionBarWithKeyStrokeContext, {
        parent: session.desktop, tabbableCoordinatorModel: {
          autoRegisterKeyStrokes: false
        }
      });
      expect(actionBar.keyStrokeContext.keyStrokes.length).toBe(0);

      actionBar.tabbableCoordinator.registerKeyStrokes();
      expect(actionBar.keyStrokeContext.keyStrokes.length).toBe(2);
      expect(actionBar.keyStrokeContext.keyStrokes[0]).toBeInstanceOf(FocusPreviousTabTargetKeyStroke);
      expect(actionBar.keyStrokeContext.keyStrokes[1]).toBeInstanceOf(FocusNextTabTargetKeyStroke);

      actionBar.tabbableCoordinator.registerKeyStrokes();
      expect(actionBar.keyStrokeContext.keyStrokes.length).toBe(2);
      expect(actionBar.keyStrokeContext.keyStrokes[0]).toBeInstanceOf(FocusPreviousTabTargetKeyStroke);
      expect(actionBar.keyStrokeContext.keyStrokes[1]).toBeInstanceOf(FocusNextTabTargetKeyStroke);
    });
  });

  describe('unregisterKeyStrokes', () => {
    it('removes the navigation keystrokes', () => {
      let actionBar = scout.create(ActionBar, {parent: session.desktop});
      expect(actionBar.keyStrokeContext.keyStrokes.length).toBe(2);
      expect(actionBar.keyStrokeContext.keyStrokes[0]).toBeInstanceOf(FocusPreviousTabTargetKeyStroke);
      expect(actionBar.keyStrokeContext.keyStrokes[1]).toBeInstanceOf(FocusNextTabTargetKeyStroke);

      let dummyKeyStroke = new KeyStroke();
      actionBar.registerKeyStrokes([dummyKeyStroke]);
      expect(actionBar.keyStrokeContext.keyStrokes.length).toBe(3);

      actionBar.tabbableCoordinator.unregisterKeyStrokes();
      expect(actionBar.keyStrokeContext.keyStrokes.length).toBe(1);
      expect(actionBar.keyStrokeContext.keyStrokes[0]).toBe(dummyKeyStroke);
    });
  });

  describe('aria', () => {
    class ActionBarWithRole extends ActionBar {
      protected override _render() {
        super._render();
        aria.role(this.$container, 'menubar');
      }
    }

    it('adds the role toolbar to the parent if there is no role yet', () => {
      let actionBar = scout.create(ActionBar, {parent: session.desktop});
      actionBar.render();
      expect(actionBar.$container).toHaveAttr('role', 'toolbar');

      // Check again after re-rendering
      actionBar.remove();
      actionBar.render();
      expect(actionBar.$container).toHaveAttr('role', 'toolbar');
    });

    it('does not add the role toolbar if the parent already has a role set', () => {
      let actionBar = scout.create(ActionBarWithRole, {parent: session.desktop});
      actionBar.render();
      expect(actionBar.$container).toHaveAttr('role', 'menubar');
    });

    it('adds the orientation attribute', () => {
      let actionBar = scout.create(ActionBar, {parent: session.desktop});
      actionBar.render();
      expect(actionBar.$container).not.toHaveAttr('aria-orientation'); // Default for toolbar role

      let actionBar2 = scout.create(ActionBar, {
        parent: session.desktop, tabbableCoordinatorModel: {
          orientation: 'vertical'
        }
      });
      actionBar2.render();
      expect(actionBar2.$container).toHaveAttr('aria-orientation', 'vertical');

      let actionBar3 = scout.create(ActionBar, {
        parent: session.desktop, tabbableCoordinatorModel: {
          orientation: 'both'
        }
      });
      actionBar3.render();
      expect(actionBar3.$container).not.toHaveAttr('aria-orientation'); // ambiguous
    });

    it('does not add the orientation attribute if it is the role\'s default', () => {
      let actionBar = scout.create(ActionBar, {parent: session.desktop});
      actionBar.render();
      expect(actionBar.$container).not.toHaveAttr('aria-orientation');

      // Orientation is set to horizontal
      // Roles with horizontal as default, don't need the attribute to be set
      // Roles with vertical as default need to have aria-orientation="horizontal" to be set
      for (let [role, orientation] of Object.entries(aria.orientationDefault())) {
        changeRoleAndAssert(actionBar, role as AriaRole, orientation === 'horizontal' ? null : 'horizontal');
      }

      let actionBar2 = scout.create(ActionBar, {
        parent: session.desktop, tabbableCoordinatorModel: {
          orientation: 'vertical'
        }
      });
      actionBar2.render();
      for (let [role, orientation] of Object.entries(aria.orientationDefault())) {
        changeRoleAndAssert(actionBar2, role as AriaRole, orientation === 'vertical' ? null : 'vertical');
      }

      let actionBar3 = scout.create(ActionBar, {
        parent: session.desktop, tabbableCoordinatorModel: {
          orientation: 'both'
        }
      });
      actionBar3.render();
      for (let [role] of Object.entries(aria.orientationDefault())) {
        changeRoleAndAssert(actionBar3, role as AriaRole, null);
      }

      function changeRoleAndAssert(actionBar: ActionBar, role: AriaRole, orientation: AriaOrientation) {
        actionBar.$container.attr('role', role);
        actionBar.tabbableCoordinator.updateAriaAttributes();
        if (!orientation) {
          expect(actionBar.$container).not.toHaveAttr('aria-orientation');
        } else {
          expect(actionBar.$container).toHaveAttr('aria-orientation', orientation);
        }
      }
    });

    it('does not add the orientation attribute if the role does not support it', () => {
      class RowActionBar extends ActionBar {
        protected override _render() {
          super._render();
          aria.role(this.$container, 'row');
        }
      }

      // Role row does not support orientation attribute -> don't add it
      let rowActionBar = scout.create(RowActionBar, {parent: session.desktop});
      rowActionBar.render();
      expect(rowActionBar.$container).not.toHaveAttr('aria-orientation');

      let rowActionBar2 = scout.create(RowActionBar, {
        parent: session.desktop, tabbableCoordinatorModel: {
          orientation: 'vertical'
        }
      });
      rowActionBar2.render();
      expect(rowActionBar2.$container).not.toHaveAttr('aria-orientation');
    });

    it('can add the aria attributes to the bind target of the keystroke context', () => {
      let actionBar = scout.create(ActionBarWithChildContext, {
        parent: session.desktop, tabbableCoordinatorModel: {
          autoRegisterKeyStrokes: false,
          orientation: 'vertical'
        }
      });
      actionBar.render();
      expect(actionBar.keyStrokeContext).toBe(null);
      expect(actionBar.$container).not.toHaveAttr('role');
      expect(actionBar.$container).not.toHaveAttr('aria-orientation');

      expect(actionBar.$container.children('.child')).toHaveAttr('role', 'toolbar');
      expect(actionBar.$container.children('.child')).toHaveAttr('aria-orientation', 'vertical');
    });
  });
});
