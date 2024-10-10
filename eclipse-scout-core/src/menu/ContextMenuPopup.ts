/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {
  AbstractLayout, Action, aria, arrays, ContextMenuPopupLayout, ContextMenuPopupModel, Event, graphics, HtmlComponent, InitModelOf, Menu, MenuDestinations, MenuFilter, menuNavigationKeyStrokes, Popup, PopupLayout, PropertyChangeEvent,
  Rectangle, RowLayout, scout, ScrollbarInstallOptions
} from '../index';
import $ from 'jquery';

export class ContextMenuPopup extends Popup implements ContextMenuPopupModel {
  declare model: ContextMenuPopupModel;

  menuItems: Menu[];
  cloneMenuItems: boolean;
  bodyAnimating: boolean;
  animationDuration: number;
  $body: JQuery;
  initialSubMenusToRender: { parentMenu: Menu; menus: Menu[] };
  menuFilter: MenuFilter;

  /** @internal */
  _toggleSubMenuQueue: (() => void)[];

  constructor() {
    super();

    this.animateOpening = true;
    this.animateRemoval = true;
    this.menuItems = [];
    this.cloneMenuItems = true;
    this.bodyAnimating = false;
    this._toggleSubMenuQueue = [];
    this.animationDuration = 300;
  }

  protected override _init(options: InitModelOf<this>) {
    options.focusableContainer = true; // In order to allow keyboard navigation, the popup must gain focus. Because menu-items are not focusable, make the container focusable instead.

    // If menu items are cloned, don't link the original menus with the popup, otherwise they would be removed when the context menu is removed
    if (options.cloneMenuItems === false) {
      this._addWidgetProperties('menuItems');
    }

    super._init(options);
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    menuNavigationKeyStrokes.registerKeyStrokes(this.keyStrokeContext, this, 'menu-item');
  }

  protected override _createLayout(): PopupLayout {
    return new ContextMenuPopupLayout(this);
  }

  protected _createBodyLayout(): AbstractLayout {
    return new RowLayout({
      pixelBasedSizing: false
    });
  }

  protected override _render() {
    super._render();
    this.$container.addClass('context-menu-popup');
    aria.role(this.$container, 'menu');
    this._renderBody();
    this._installScrollbars();
    this._renderMenuItems();
  }

  protected override _remove() {
    this._toggleSubMenuQueue = [];
    super._remove();
  }

  protected _renderBody() {
    this.$body = this.$container.appendDiv('context-menu');
    // Complete the layout hierarchy between the popup and the menu items
    let htmlBody = HtmlComponent.install(this.$body, this.session);
    htmlBody.setLayout(this._createBodyLayout());
  }

  protected override _installScrollbars(options?: ScrollbarInstallOptions) {
    super._installScrollbars({
      axis: 'y',
      scrollShadow: 'none'
    });
  }

  protected _checkRemoveSubMenuItemsPossible(parentMenu: Menu, animated: boolean): boolean {
    if (!this.rendered && !this.rendering) {
      return false;
    }
    let openingAnimationRunning = this.isOpeningAnimationRunning();
    let layout = this.htmlComp.layout as PopupLayout;
    let resizeAnimationRunning = layout.resizeAnimationRunning;
    if (this.bodyAnimating || openingAnimationRunning || resizeAnimationRunning) {
      // Let current animation finish and execute afterwards to prevent an unpredictable behavior and inconsistent state
      this._toggleSubMenuQueue.push(this.removeSubMenuItems.bind(this, parentMenu, animated));
      if (openingAnimationRunning) {
        this.$container.oneAnimationEnd(() => this._processSubMenuQueue());
      }
      return false;
    }
    return true;
  }

  removeSubMenuItems(parentMenu: Menu, animated: boolean) {
    let internalParentMenu: Menu & { __originalParent?: Menu } = parentMenu;
    if (!this._checkRemoveSubMenuItemsPossible(internalParentMenu, animated)) {
      return;
    }
    this.$body = internalParentMenu.__originalParent.$subMenuBody;
    // move new body to back
    this.$body.insertBefore(internalParentMenu.$subMenuBody);

    if (internalParentMenu.__originalParent._doActionTogglesSubMenu) {
      internalParentMenu.__originalParent._doActionTogglesSubMenu();
    }

    let popupBounds = this.htmlComp.bounds();

    this._adjustTextAlignment();
    HtmlComponent.get(this.$body).invalidateLayoutTree();
    this.validateLayoutTree();
    this.position();

    if (animated) {
      this._animateRemoveSubmenuItems(internalParentMenu, popupBounds);
    }
  }

  protected _animateRemoveSubmenuItems(parentMenu: Menu, popupBounds: Rectangle) {
    let parentMenuPosition = parentMenu.$placeHolder.position();
    let popupInsets = this.htmlComp.insets();
    let endPopupBounds = this.htmlComp.bounds();
    let oldBodyBounds = HtmlComponent.get(parentMenu.$subMenuBody).bounds();
    let bodyBounds = HtmlComponent.get(this.$body).bounds();
    let startBodyBounds = new Rectangle(0, popupInsets.top, oldBodyBounds.width, oldBodyBounds.height);
    let endBodyBounds = new Rectangle(0, popupInsets.top + parentMenuPosition.top, bodyBounds.width, parentMenu.$container.cssHeight());

    this.bodyAnimating = true;
    this.$container.addClass('animating');
    let layout = this.htmlComp.layout as PopupLayout;
    layout.disableAutoPosition();
    this._animateResizePopup(this.htmlComp.$comp, popupBounds, endPopupBounds);
    this._animateTextOffset(parentMenu.$subMenuBody, parentMenu.$subMenuBody.data('text-offset'));

    // Collapse old body
    parentMenu.$subMenuBody
      .cssWidthAnimated(startBodyBounds.width, endBodyBounds.width, {
        duration: this.animationDuration,
        progress: this.revalidateLayout.bind(this),
        complete: () => this._completeAnimateRemoveSubMenuItems(parentMenu),
        queue: false
      })
      .cssHeightAnimated(startBodyBounds.height, endBodyBounds.height, {
        duration: this.animationDuration,
        queue: false
      })
      .cssTopAnimated(startBodyBounds.y, endBodyBounds.y, {
        duration: this.animationDuration,
        queue: false
      });

    // Resize new body so that it doesn't increase the popup height and shows unnecessary scrollbars if new body will be bigger
    // It also ensures correct text ellipsis during animation, position is already correct
    this.$body
      .cssWidthAnimated(oldBodyBounds.width, bodyBounds.width, {
        duration: this.animationDuration,
        queue: false
      })
      .cssHeightAnimated(oldBodyBounds.height, bodyBounds.height, {
        duration: this.animationDuration,
        queue: false
      });
  }

  protected _completeAnimateRemoveSubMenuItems(parentMenu: Menu) {
    this.bodyAnimating = false;
    if (!this.rendered || !parentMenu.$container) {
      return;
    }
    this.$container.removeClass('animating');
    parentMenu.$placeHolder.replaceWith(parentMenu.$container);
    parentMenu.$container.toggleClass('expanded', false);
    this._updateFirstLastClass();
    this.updateNextToSelected('menu-item', parentMenu.$container);

    parentMenu.$subMenuBody.detach();
    this._processSubMenuQueue();
  }

  /** @internal */
  _processSubMenuQueue() {
    let next = this._toggleSubMenuQueue.shift();
    if (next) {
      next();
    }
  }

  protected _checkRenderSubMenuItemsPossible(parentMenu: Menu, menus: Menu[], animated: any, initialSubMenuRendering: any): boolean {
    if (!this.session.desktop.rendered && !initialSubMenuRendering) {
      this.initialSubMenusToRender = {
        parentMenu: parentMenu,
        menus: menus
      };
      return false;
    }
    if (!this.rendered && !this.rendering) {
      return false;
    }
    let openingAnimationRunning = this.isOpeningAnimationRunning();
    let layout = this.htmlComp.layout as PopupLayout;
    let resizeAnimationRunning = layout.resizeAnimationRunning;
    if (this.bodyAnimating || openingAnimationRunning || resizeAnimationRunning) {
      // Let current animation finish and execute afterwards to prevent an unpredictable behavior and inconsistent state
      this._toggleSubMenuQueue.push(this.renderSubMenuItems.bind(this, parentMenu, menus, animated, initialSubMenuRendering));
      if (openingAnimationRunning) {
        this.$container.oneAnimationEnd(() => this._processSubMenuQueue());
      }
      return false;
    }
    return true;
  }

  renderSubMenuItems(parentMenu: Menu, menus: Menu[], animated: boolean, initialSubMenuRendering?: boolean): boolean | undefined {
    let internalParentMenu: Menu & { __originalParent?: Menu } = parentMenu;
    if (!this._checkRenderSubMenuItemsPossible(internalParentMenu, menus, animated, initialSubMenuRendering)) {
      return false;
    }

    let popupBounds = this.htmlComp.bounds();
    let $oldBody = this.$body;
    internalParentMenu.__originalParent.$subMenuBody = $oldBody;
    let $menuItems = this.$body.find('.menu-item');
    $menuItems.removeClass('next-to-selected');

    if (!internalParentMenu.$subMenuBody) {
      this._renderBody();
      internalParentMenu.$subMenuBody = this.$body;
      this._renderMenuItems(menus, initialSubMenuRendering);
    } else {
      // append $body
      this.$body = internalParentMenu.$subMenuBody;
    }
    let $insertAfterElement = internalParentMenu.$container.prev();
    let parentMenuPosition = internalParentMenu.$container.position();
    internalParentMenu.$placeHolder = internalParentMenu.$container.clone();
    // HtmlComponent is necessary for the row layout (it would normally be installed by Menu.js, but $placeholder is just a jquery clone of internalParentMenu.$container and is not managed by a real widget)
    HtmlComponent.install(internalParentMenu.$placeHolder, this.session);
    if ($insertAfterElement.length) {
      internalParentMenu.$placeHolder.insertAfter($insertAfterElement);
    } else {
      $oldBody.prepend(internalParentMenu.$placeHolder);
    }

    this.$body.insertAfter($oldBody);
    this.$body.prepend(internalParentMenu.$container);
    internalParentMenu.$container.toggleClass('expanded');
    this._adjustTextAlignment();

    HtmlComponent.get(this.$body).invalidateLayoutTree();
    this.validateLayoutTree();
    this.position();
    this.updateNextToSelected();

    if (animated) {
      this._animateRenderSubMenuItems(internalParentMenu, popupBounds, parentMenuPosition);
    } else {
      $oldBody.detach();
      this._updateFirstLastClass();
    }
  }

  protected _animateRenderSubMenuItems(parentMenu: Menu, popupBounds: Rectangle, parentMenuPosition: JQuery.Coordinates) {
    let internalParentMenu: Menu & { __originalParent?: Menu } = parentMenu;
    let $oldBody: JQuery = internalParentMenu.__originalParent.$subMenuBody;
    let endPopupBounds = this.htmlComp.bounds();
    let popupInsets = this.htmlComp.insets();
    let oldBodyBounds = graphics.bounds($oldBody);
    let bodyBounds = HtmlComponent.get(this.$body).bounds();
    let startBodyBounds = new Rectangle(0, popupInsets.top + parentMenuPosition.top, oldBodyBounds.width, internalParentMenu.$container.cssHeight());
    let endBodyBounds = new Rectangle(0, popupInsets.top, bodyBounds.width, bodyBounds.height);

    this.bodyAnimating = true;
    this.$container.addClass('animating');
    let layout = this.htmlComp.layout as PopupLayout;
    layout.disableAutoPosition();
    this._animateResizePopup(this.htmlComp.$comp, popupBounds, endPopupBounds);
    this._animateTextOffset(this.$body, $oldBody.data('text-offset'));

    // Expand new body
    this.$body
      .cssWidthAnimated(startBodyBounds.width, endBodyBounds.width, {
        duration: this.animationDuration,
        progress: this.revalidateLayout.bind(this),
        complete: () => this._completeAnimateRenderSubMenuItems($oldBody),
        queue: false
      })
      .cssHeightAnimated(startBodyBounds.height, endBodyBounds.height, {
        duration: this.animationDuration,
        queue: false
      })
      .cssTopAnimated(startBodyBounds.y, endBodyBounds.y, {
        duration: this.animationDuration,
        queue: false
      });

    // Resize old body so that it doesn't increase the popup height and shows unnecessary scrollbars if new body will be smaller
    // It also ensures correct text ellipsis during animation, position is already correct
    $oldBody
      .cssWidthAnimated(oldBodyBounds.width, endBodyBounds.width, {
        duration: this.animationDuration,
        queue: false
      })
      .cssHeightAnimated(oldBodyBounds.height, endBodyBounds.height, {
        duration: this.animationDuration,
        queue: false
      });
  }

  protected _completeAnimateRenderSubMenuItems($oldBody: JQuery) {
    this.bodyAnimating = false;
    if (!this.rendered) {
      return;
    }
    this.$container.removeClass('animating');
    if ($oldBody) {
      $oldBody.detach();
      this.$body.cssTop('');
      this._updateFirstLastClass();
    }
    let layout = this.htmlComp.layout as PopupLayout;
    layout.resetAutoPosition();
    this._processSubMenuQueue();
  }

  protected _animateResizePopup($comp: JQuery, popupBounds: Rectangle, targetBounds: Rectangle) {
    let options = {
      duration: this.animationDuration,
      queue: false
    };
    $comp
      .cssTopAnimated(popupBounds.y, targetBounds.y, options)
      .cssLeftAnimated(popupBounds.x, targetBounds.x, options)
      .cssWidthAnimated(popupBounds.width, targetBounds.width, options)
      .cssHeightAnimated(popupBounds.height, targetBounds.height, options);
  }

  /** @internal */
  _animateTextOffset($body: JQuery, textOffset: number, targetOffset?: number) {
    targetOffset = scout.nvl(targetOffset, this.$body.data('text-offset'));
    let $menuItems = this.$visibleMenuItems($body);
    $menuItems.each((index: number, menuItem: HTMLElement) => {
      let $menuItem = $(menuItem);
      let $text = $menuItem.children('.text');
      let padding = this._calcTextPaddingLeft($menuItem, textOffset);
      let targetPadding = this._calcTextPaddingLeft($menuItem, targetOffset);
      $text.cssAnimated({paddingLeft: padding}, {paddingLeft: targetPadding}, {duration: this.animationDuration});
    });
  }

  protected _renderMenuItems(menus?: Menu[], initialSubMenuRendering?: boolean) {
    menus = menus ? menus : this._getMenuItems();
    if (this.menuFilter) {
      menus = this.menuFilter(menus, MenuDestinations.CONTEXT_MENU);
    }

    if (!menus || menus.length === 0) {
      return;
    }

    menus.forEach((menu: Menu & { __originalParent?: Menu }) => {
      // Invisible menus are rendered as well because their visibility might change dynamically
      if (menu.separator) {
        return;
      }

      // prevent loosing original parent
      let originalParent = menu.parent as Menu;
      // Clone menu items but only clone once unless it is for a different context menu (e.g. a context menu of a combo menu inside a context menu)
      // Clone will recursively also clone all child actions.
      if (this.cloneMenuItems && !menu.cloneOf || !this.has(menu)) {
        menu = menu.clone({
          parent: this,
          textPosition: Action.TextPosition.DEFAULT
        }, {
          delegateEventsToOriginal: ['acceptInput', 'action', 'click'],
          delegateAllPropertiesToClone: true,
          delegateAllPropertiesToOriginal: true,
          excludePropertiesToOriginal: ['selected', 'logicalGrid', 'tabbable']
        });
        menu.setTabbable(false);
        // attach listener
        this._attachCloneMenuListeners(menu);
      }

      // just set once because on second execution of this menu.parent is set to a popup
      if (!menu.__originalParent) {
        menu.__originalParent = originalParent;
      }
      menu.render(this.$body);
      menu.$container.removeClass('menu-button');
      this._attachMenuListeners(menu);
    });

    this._handleInitialSubMenus(initialSubMenuRendering);
    this._updateFirstLastClass();
    this._adjustTextAlignment();
  }

  protected _attachCloneMenuListeners(menu: Menu) {
    menu.on('propertyChange', this._onCloneMenuPropertyChange.bind(this));
    menu.childActions.forEach(this._attachCloneMenuListeners.bind(this));
  }

  protected _onCloneMenuPropertyChange(event: PropertyChangeEvent) {
    if (event.propertyName === 'selected') {
      let menu = event.source as Menu;
      // Only trigger property change, setSelected would try to render the selected state which must not happen for the original menu
      menu.cloneOf.triggerPropertyChange('selected', event.oldValue, event.newValue);
    }
  }

  protected _handleInitialSubMenus(initialSubMenuRendering?: boolean) {
    if (initialSubMenuRendering) {
      return;
    }
    let menusObj;
    while (this.initialSubMenusToRender) {
      menusObj = this.initialSubMenusToRender;
      this.initialSubMenusToRender = undefined;
      this.renderSubMenuItems(menusObj.parentMenu, menusObj.menus, false, true);
    }
  }

  protected _attachMenuListeners(menu: Menu) {
    let menuItemActionHandler = this._onMenuItemAction.bind(this);
    let menuItemPropertyChange = this._onMenuItemPropertyChange.bind(this);
    menu.on('action', menuItemActionHandler);
    menu.on('propertyChange', menuItemPropertyChange);
    this.one('remove', () => {
      menu.off('action', menuItemActionHandler);
      menu.off('propertyChange', menuItemPropertyChange);
    });
  }

  updateMenuItems(menuItems: Menu[]) {
    menuItems = arrays.ensure(menuItems);
    // Only update if list of menus changed. Don't compare this.menuItems, because that list
    // may contain additional UI separators, and may not be in the same order
    if (!arrays.equals(this.menuItems, menuItems)) {
      this.close();
    }
  }

  /**
   * Override this method to return menu items or actions used to render menu items.
   */
  protected _getMenuItems(): Menu[] {
    return this.menuItems;
  }

  /**
   * Currently rendered $menuItems
   */
  $menuItems(): JQuery {
    return this.$body.children('.menu-item');
  }

  $visibleMenuItems($body?: JQuery): JQuery {
    $body = $body || this.$body;
    return $body.children('.menu-item:visible');
  }

  /**
   * Updates the first and last visible menu items with the according css classes.
   * Necessary because invisible menu-items are rendered.
   */
  protected _updateFirstLastClass() {
    let $firstMenuItem, $lastMenuItem;

    this.$body.children('.menu-item').each(function() {
      let $menuItem = $(this);
      $menuItem.removeClass('first last');

      if ($menuItem.isVisible()) {
        if (!$firstMenuItem) {
          $firstMenuItem = $menuItem;
        }
        $lastMenuItem = $menuItem;
      }
    });
    if ($firstMenuItem) {
      $firstMenuItem.addClass('first');
    }
    if ($lastMenuItem) {
      $lastMenuItem.addClass('last');
    }
  }

  updateNextToSelected(menuItemClass?: string, $selectedItem?: JQuery) {
    menuItemClass = menuItemClass ? menuItemClass : 'menu-item';
    let $all = this.$body.find('.' + menuItemClass);
    $selectedItem = $selectedItem ? $selectedItem : this.$body.find('.' + menuItemClass + '.selected');
    aria.linkElementWithActiveDescendant(this.$container, $selectedItem);
    $all.removeClass('next-to-selected');
    if ($selectedItem.hasClass('selected')) {
      $selectedItem.nextAll(':visible').first().addClass('next-to-selected');
    }
  }

  protected _onMenuItemAction(event: Event<Action>) {
    if (event.source.isToggleAction()) {
      return;
    }
    if (event.defaultPrevented) {
      return;
    }
    this.close();
  }

  protected _onMenuItemPropertyChange(event: PropertyChangeEvent) {
    if (!this.rendered) {
      return;
    }
    if (event.propertyName === 'visible') {
      this._updateFirstLastClass();
    } else if (event.propertyName === 'selected') {
      // Keystroke navigation marks the currently focused item as selected.
      // When a sub menu item is opened while another element is selected (focused), make sure the other element gets unselected.
      // Otherwise, two items would be selected when the sub menu is closed again.
      this._deselectSiblings(event.source as Menu);
    } else if (event.propertyName === 'iconId') {
      if (this.rendered) {
        // Update text alignment if an icon changes while popup is open
        // Rendering of the icon happens after the property change event -> update text later
        queueMicrotask(() => {
          if (this.rendered) {
            this._adjustTextAlignment();
          }
        });
      }
    }
    // Make sure menu is positioned correctly afterwards (if it is opened upwards hiding/showing a menu item makes it necessary to reposition)
    this.position();
  }

  /**
   * Deselects the visible siblings of the given menu item. It just removes the CSS class and does not modify the selected property.
   */
  protected _deselectSiblings(menuItem: Menu) {
    menuItem.$container.siblings('.menu-item').each((i, elem) => {
      let $menuItem = $(elem);
      $menuItem.select(false);
    });
  }

  /** @internal */
  _adjustTextAlignment($body?: JQuery) {
    $body = $body || this.$body;
    let $menuItems = this.$visibleMenuItems($body);
    let textOffset = this._calcTextOffset($menuItems);
    $body.data('text-offset', textOffset);
    this._updateTextOffset(textOffset, $menuItems);
  }

  protected _calcTextOffset($menuItems: JQuery): number {
    let textOffset = 0;
    $menuItems = $menuItems || this.$visibleMenuItems();
    $menuItems.each((index, menuItem) => {
      let $menuItem = $(menuItem);
      let $icon = $menuItem.children('.icon');
      let iconWidth = 0;

      if ($icon.length > 0) {
        iconWidth = $icon.outerWidth(true);
      }
      textOffset = Math.max(textOffset, iconWidth);
    });
    return textOffset;
  }

  protected _updateTextOffset(textOffset: number, $menuItems?: JQuery) {
    // Update the padding of each text such that the sum of icon width and the padding
    // are the same for all items. This ensures that the texts are all aligned.
    $menuItems = $menuItems || this.$visibleMenuItems();
    $menuItems.each((index: number, menuItem: HTMLElement) => {
      let $menuItem = $(menuItem);
      let $text = $menuItem.children('.text');
      $text.css('padding-left', this._calcTextPaddingLeft($menuItem, textOffset));
      let htmlComp = HtmlComponent.optGet($menuItem);
      if (htmlComp) {
        htmlComp.invalidateLayout();
      }
    });
  }

  protected _calcTextPaddingLeft($menuItem: JQuery, textOffset: number): number {
    let $icon = $menuItem.children('.icon');
    let iconWidth = 0;

    if ($icon.length > 0) {
      iconWidth = $icon.outerWidth(true);
    }
    return textOffset - iconWidth;
  }
}
