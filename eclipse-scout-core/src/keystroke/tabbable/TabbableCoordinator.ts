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
  Action, aria, AriaRole, arrays, Event, EventHandler, FocusNextTabTargetKeyStroke, FocusOptions, FocusPreviousTabTargetKeyStroke, focusUtils, InitModelOf, KeyStrokeContext, PropertyChangeEvent, PropertyEventEmitter, PropertyEventMap,
  scout, scrollbars, SomeRequired, Widget
} from '../..';

/**
 * The tabbable coordinator can be used to add keyboard navigation for widgets holding items where only one item must be tabbable at a time.
 *
 * The tabbable item has tabindex = '0', all others have '-1'. The user can change the tabbable item by using the arrow keys.
 * If he tabs away from the widget and returns, the focus returns to the item that had focus.
 * This technique is known as `roving tabindex`.
 */
export class TabbableCoordinator extends PropertyEventEmitter implements TabbableCoordinatorModel {
  declare initModel: SomeRequired<this['model'], 'parent'>;
  declare model: TabbableCoordinatorModel;
  declare eventMap: TabbableCoordinatorEventMap;
  declare self: TabbableCoordinator;

  parent: Widget;
  items: TabbableItem[] = [];
  currentItem: TabbableItem;
  autoRegisterKeyStrokes = true;
  orientation: TabbableCoordinatorOrientation = 'horizontal';
  initialItemProvider: () => TabbableItem;
  protected _actionItemPropertyChangeHandler: EventHandler<PropertyChangeEvent>;
  protected _parentRenderHandler: EventHandler;

  constructor() {
    super();
    this._actionItemPropertyChangeHandler = this._onActionItemPropertyChange.bind(this);
    this._parentRenderHandler = this._onParentRender.bind(this);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._setItems(this.items);

    if (this.parent.rendered) {
      this._onParentRender();
    } else {
      this.parent.on('render', this._parentRenderHandler);
    }
    this.parent.one('destroy', () => this.destroy());

    if (this.autoRegisterKeyStrokes) {
      this.registerKeyStrokes();
    }
  }

  protected _onParentRender() {
    this._attachParentFocusInHandler();

    if (this.autoRegisterKeyStrokes) {
      // Only set a role if autoRegisterKeyStrokes is set, otherwise parent may get the toolbar role even though it has no keystrokes
      this.updateAriaAttributes();
    }
  }

  destroy() {
    this.parent.off('render', this._parentRenderHandler);

    // The items may not belong to the widget that owns the tabbable coordinator and may therefore still be used after the widget itself is destroyed
    // -> ensure listeners are removed
    this.setItems([]);

    if (this.autoRegisterKeyStrokes) {
      this.unregisterKeyStrokes();
    }
  }

  setItems(items: TabbableItem[]) {
    this.setProperty('items', items);
  }

  protected _setItems(items: TabbableItem[]) {
    if (arrays.equals(this.items, items)) {
      return;
    }
    for (const item of this.items) {
      if (item instanceof Action) {
        item.off('propertyChange', this._actionItemPropertyChangeHandler);
      }
    }
    this._setProperty('items', items);
    if (!this.currentItem || !this.items.includes(this.currentItem) || !this.currentItem.isFocused()) {
      this.resetCurrentItem();
    }
    for (const item of this.items) {
      if (item !== this.currentItem) {
        item.setTabbable(false);
      }
      if (item instanceof Action) {
        item.on('propertyChange', this._actionItemPropertyChangeHandler);
      }
    }
  }

  /**
   * Sets the current item to the given item which makes it tabbable so it can be focused.
   * The former current item won't be tabbable anymore.
   */
  setCurrentItem(currentItem: TabbableItem): boolean {
    if (currentItem === this.currentItem) {
      return false;
    }

    currentItem?.setTabbable(true);

    let oldItem = this.currentItem;
    let wasFocused = oldItem?.isFocused();

    this.setProperty('currentItem', currentItem);

    if (wasFocused) {
      // If former currentItem was focused, set the focus to the new one to keep it inside the container
      currentItem?.focus();
    }

    // Remove tabindex from old item _after_ the new current item gets it, otherwise the focus would be temporarily moved to the body if the old item was focused.
    oldItem?.setTabbable(false);

    return true;
  }

  get initialItem(): TabbableItem {
    let initialItem = this.initialItemProvider?.();
    if (initialItem?.isTabTarget()) {
      return initialItem;
    }
    return this.items.find(item => item.isTabTarget());
  }

  /**
   * Sets the current item to the {@link initialItem}.
   */
  resetCurrentItem() {
    this.setCurrentItem(this.initialItem);
  }

  protected _isResetItemNecessary(newItem: TabbableItem) {
    if (!this.currentItem) {
      return true;
    }
    if (newItem === this.currentItem && !this.currentItem.isTabTarget()) {
      // If a property changes on the currentItem and turns it into a non-tab target, a new currentItem needs to be set.
      return true;
    }
    if (newItem === this.initialItem && !this.currentItem?.isFocused()) {
      // If a property changes on the initialItem and turns it into a tab target, use it as new currentItem.
      // Don't do it if the currentItem is focused because it would be confusing if the focus changes suddenly.
      return true;
    }
    return false;
  }

  /**
   * @returns the {@link TabbableItem} that contains the given `$item`.
   */
  findItemFor($item: JQuery): TabbableItem {
    if (!$item) {
      return null;
    }
    return this.items.find(item => item.$container?.[0] === $item[0]);
  }

  protected _onCurrentItemFocus() {
    this.trigger('itemFocus', {item: this.currentItem});
  }

  protected _attachParentFocusInHandler() {
    this.parent.$container.on('focusin', event => this._onParentFocusIn(event));
  }

  setOrientation(orientation: TabbableCoordinatorOrientation) {
    this.setProperty('orientation', orientation);

    if (this.autoRegisterKeyStrokes) {
      this.registerKeyStrokes();
    }
  }

  /**
   * Sets the role and aria-orientation on the parent's container.
   *
   * @param $container the container to put the roles on. Default is the container of the {@link parent}.
   *                   If the keystrokes are registered on a different container, that container needs to be used.
   * @param role the role to be set, default is `toolbar`.
   */
  updateAriaAttributes($container?: JQuery, role: AriaRole = 'toolbar') {
    $container = scout.nvl($container, this.parent.$container);
    if (!$container) {
      return;
    }
    // The container needs a role that supports arrow keys to navigate (like toolbar, menubar, tablist, etc.) so that a screen reader can announce that the focus is in such a group.
    // Also, if such a role is missing, some screen readers like NVDA switch to a browse mode when pressing an arrow key which prevents our keystrokes from being triggered.
    if (!$container.attr('role')) {
      aria.role($container, role);
    }
    aria.orientation($container, this.orientation === 'both' ? undefined : this.orientation);
  }

  protected _onParentFocusIn(event: JQuery.FocusInEvent) {
    let target = event.target;
    if (target === this.currentItem?.$container?.[0]) {
      this._onCurrentItemFocus();
    }
  }

  protected _onActionItemPropertyChange(event: PropertyChangeEvent<any, Action>) {
    // Listen to properties which could potentially influence the result of item.isTabTarget()
    if (scout.isOneOf(event.propertyName, 'overflown', 'enabledComputed', 'visible', 'selected', 'hidden')) { // 'hidden' belongs to the EllipsisMenu
      if (this._isResetItemNecessary(event.source)) {
        this.resetCurrentItem();
      }
    }
  }

  /**
   * Registers the keystrokes which allow navigating over the {@link items}.
   *
   * If the keystrokes are already registered, they will be unregistered first.
   *
   * Also creates a new {@link KeyStrokeContext} on the `target` if no `keyStrokeContext` and the target does not already have one.
   *
   * @param target the widget to take the`keyStrokeContext` from if no context is passed. Defaults to {@link parent}.
   * @param keyStrokeContext the context to register the keystrokes on. Defaults to `target.keyStrokeContext`.
   */
  registerKeyStrokes(target?: Widget, keyStrokeContext?: KeyStrokeContext) {
    target = scout.nvl(target, this.parent);
    keyStrokeContext = scout.nvl(keyStrokeContext, target.keyStrokeContext);

    // Create a keystroke context on the parent if there is none yet.
    if (!keyStrokeContext && !target.initialized) {
      // A widget initializes the keystroke context after Widget._init()
      // Because the coordinator is typically created in the constructor or _init() of the widget,
      // a keystroke context can be created automatically and will be initialized after _init()
      keyStrokeContext = new KeyStrokeContext();
      target.keyStrokeContext = keyStrokeContext;
    }

    this.unregisterKeyStrokes(target, keyStrokeContext);
    keyStrokeContext.registerKeyStrokes([
      new FocusPreviousTabTargetKeyStroke(target, this),
      new FocusNextTabTargetKeyStroke(target, this)
    ]);
    this.updateAriaAttributes(keyStrokeContext.$getBindTarget());
  }

  /**
   * Unregisters the keystrokes which allow navigating over the {@link items}.
   *
   * @param target the widget to take the`keyStrokeContext` from if no context is passed. Defaults to {@link parent}.
   * @param keyStrokeContext the context to unregister the keystrokes on. Defaults to `target.keyStrokeContext`.
   */
  unregisterKeyStrokes(target?: Widget, keyStrokeContext?: KeyStrokeContext) {
    target = scout.nvl(target, this.parent);
    keyStrokeContext = scout.nvl(keyStrokeContext, target.keyStrokeContext);
    let keyStrokes = keyStrokeContext.keyStrokes.filter(keystroke =>
      keystroke instanceof FocusPreviousTabTargetKeyStroke ||
      keystroke instanceof FocusNextTabTargetKeyStroke);
    keyStrokeContext.unregisterKeyStrokes(keyStrokes);
  }
}

export class TabbableItem {
  $container: JQuery;

  constructor($item: JQuery) {
    this.$container = $item;
  }

  setTabbable(tabbable: boolean) {
    this.$container.setTabbable(tabbable);
  }

  isTabTarget(): boolean {
    return this.$container.isVisible() && this.$container.isEnabled();
  }

  focus(options?: FocusOptions): void {
    this.$container[0].focus(options);
  }

  reveal() {
    scrollbars.reveal(this.$container);
  }

  isFocused(): boolean {
    return focusUtils.isActiveElement(this.$container);
  }
}

export interface TabbableCoordinatorModel {
  parent?: Widget;
  /**
   * Specifies the orientation of the items.
   *
   * This defines which arrow keys are allowed to change the current item.
   * The orientation will also be set as aria attribute (`aria-orientation`).
   *
   * Default is `horizontal`.
   */
  orientation?: TabbableCoordinatorOrientation;
  /**
   * Defines the tabbable items.
   */
  items?: TabbableItem[];
  /**
   * Defines a function that should be used when computing the initial {@link TabbableCoordinator.currentItem}.
   *
   * If no provider is set, the first item that is a tab target ({@link TabbableItem.isTabTarget}) is used as initial item.
   *
   * By default, there is no provider set.
   */
  initialItemProvider?: () => TabbableItem;
  /**
   * Defines whether the keystrokes which allow navigating over the {@link items} should be registered automatically on the {@link parent}.
   *
   * If set to false, they can still be registered manually using {@link TabbableCoordinator.registerKeyStrokes}.
   *
   * If the {@link parent} does not have a {@link KeyStrokeContext},
   * a new one will be created as long as the {@link TabbableCoordinator} is created before or while the parent is being initialized.
   *
   * Default is true.
   *
   * @see FocusPreviousTabTargetKeyStroke
   * @see FocusNextTabTargetKeyStroke
   */
  autoRegisterKeyStrokes?: boolean;
}

export interface ItemFocusEvent extends Event<TabbableCoordinator> {
  item: TabbableItem;
}

export interface TabbableCoordinatorEventMap extends PropertyEventMap {
  'itemFocus': ItemFocusEvent;
  'propertyChange:currentItem': PropertyChangeEvent<TabbableItem>;
  'propertyChange:items': PropertyChangeEvent<TabbableItem[]>;
}

export type TabbableCoordinatorOrientation = 'horizontal' | 'vertical' | 'both';
