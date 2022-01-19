/*
 * Copyright (c) 2014-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Action, arrays, ContextMenuPopupLayout, graphics, HtmlComponent, MenuDestinations, menuNavigationKeyStrokes, Popup, Rectangle, RowLayout} from '../index';
import $ from 'jquery';

export default class ContextMenuPopup extends Popup {

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

  _init(options) {
    options.focusableContainer = true; // In order to allow keyboard navigation, the popup must gain focus. Because menu-items are not focusable, make the container focusable instead.

    // If menu items are cloned, don't link the original menus with the popup, otherwise they would be removed when the context menu is removed
    if (options.cloneMenuItems === false) {
      this._addWidgetProperties('menuItems');
    }

    super._init(options);
  }

  /**
   * @override Popup.js
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    menuNavigationKeyStrokes.registerKeyStrokes(this.keyStrokeContext, this, 'menu-item');
  }

  _createLayout() {
    return new ContextMenuPopupLayout(this);
  }

  /**
   * @return {RowLayout}
   */
  _createBodyLayout() {
    return new RowLayout({
      pixelBasedSizing: false
    });
  }

  _render() {
    super._render();
    this.$container.addClass('context-menu-popup');
    this._renderBody();
    this._installScrollbars();
    this._renderMenuItems();
  }

  _remove() {
    this._toggleSubMenuQueue = [];
    super._remove();
  }

  _renderBody() {
    this.$body = this.$container.appendDiv('context-menu');
    // Complete the layout hierarchy between the popup and the menu items
    let htmlBody = HtmlComponent.install(this.$body, this.session);
    htmlBody.setLayout(this._createBodyLayout());
  }

  _installScrollbars(options) {
    super._installScrollbars({
      axis: 'y',
      scrollShadow: 'none'
    });
  }

  _checkRemoveSubMenuItemsPossible(parentMenu, animated) {
    if (!this.rendered && !this.rendering) {
      return false;
    }
    let openingAnimationRunning = this.isOpeningAnimationRunning();
    let resizeAnimationRunning = this.htmlComp.layout.resizeAnimationRunning;
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

  removeSubMenuItems(parentMenu, animated) {
    if (!this._checkRemoveSubMenuItemsPossible(parentMenu, animated)) {
      return false;
    }
    this.$body = parentMenu.__originalParent.$subMenuBody;
    // move new body to back
    this.$body.insertBefore(parentMenu.$subMenuBody);

    if (parentMenu.__originalParent._doActionTogglesSubMenu) {
      parentMenu.__originalParent._doActionTogglesSubMenu();
    }

    let popupBounds = this.htmlComp.bounds();

    this._adjustTextAlignment();
    HtmlComponent.get(this.$body).invalidateLayoutTree();
    this.validateLayoutTree();
    this.position();

    if (animated) {
      this._animateRemoveSubmenuItems(parentMenu, popupBounds);
    }
  }

  _animateRemoveSubmenuItems(parentMenu, popupBounds) {
    let parentMenuPosition = parentMenu.$placeHolder.position();
    let popupInsets = this.htmlComp.insets();
    let endPopupBounds = this.htmlComp.bounds();
    let oldBodyBounds = HtmlComponent.get(parentMenu.$subMenuBody).bounds();
    let bodyBounds = HtmlComponent.get(this.$body).bounds();
    let startBodyBounds = new Rectangle(0, popupInsets.top, oldBodyBounds.width, oldBodyBounds.height);
    let endBodyBounds = new Rectangle(0, popupInsets.top + parentMenuPosition.top, bodyBounds.width, parentMenu.$container.cssHeight());

    this.bodyAnimating = true;
    this.$container.addClass('animating');
    this.htmlComp.layout.disableAutoPosition();
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

  _completeAnimateRemoveSubMenuItems(parentMenu) {
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

  _processSubMenuQueue() {
    let next = this._toggleSubMenuQueue.shift();
    if (next) {
      next();
    }
  }

  _checkRenderSubMenuItemsPossible(parentMenu, menus, animated, initialSubMenuRendering) {
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
    let resizeAnimationRunning = this.htmlComp.layout.resizeAnimationRunning;
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

  renderSubMenuItems(parentMenu, menus, animated, initialSubMenuRendering) {
    if (!this._checkRenderSubMenuItemsPossible(parentMenu, menus, animated, initialSubMenuRendering)) {
      return false;
    }

    let popupBounds = this.htmlComp.bounds();
    let $oldBody = this.$body;
    parentMenu.__originalParent.$subMenuBody = $oldBody;
    let $menuItems = this.$body.find('.menu-item');
    $menuItems.removeClass('next-to-selected');

    if (!parentMenu.$subMenuBody) {
      this._renderBody();
      parentMenu.$subMenuBody = this.$body;
      this._renderMenuItems(menus, initialSubMenuRendering);
    } else {
      // append $body
      this.$body = parentMenu.$subMenuBody;
    }
    let $insertAfterElement = parentMenu.$container.prev();
    let parentMenuPosition = parentMenu.$container.position();
    parentMenu.$placeHolder = parentMenu.$container.clone();
    // HtmlComponent is necessary for the row layout (it would normally be installed by Menu.js, but $placeholder is just a jquery clone of parentMenu.$container and is not managed by a real widget)
    HtmlComponent.install(parentMenu.$placeHolder, this.session);
    if ($insertAfterElement.length) {
      parentMenu.$placeHolder.insertAfter($insertAfterElement);
    } else {
      $oldBody.prepend(parentMenu.$placeHolder);
    }

    this.$body.insertAfter($oldBody);
    this.$body.prepend(parentMenu.$container);
    parentMenu.$container.toggleClass('expanded');
    this._adjustTextAlignment();

    HtmlComponent.get(this.$body).invalidateLayoutTree();
    this.validateLayoutTree();
    this.position();
    this.updateNextToSelected();

    if (animated) {
      this._animateRenderSubMenuItems(parentMenu, popupBounds, parentMenuPosition);
    } else {
      $oldBody.detach();
      this._updateFirstLastClass();
    }
  }

  _animateRenderSubMenuItems(parentMenu, popupBounds, parentMenuPosition) {
    let $oldBody = parentMenu.__originalParent.$subMenuBody;
    let endPopupBounds = this.htmlComp.bounds();
    let popupInsets = this.htmlComp.insets();
    let oldBodyBounds = graphics.bounds($oldBody);
    let bodyBounds = HtmlComponent.get(this.$body).bounds();
    let startBodyBounds = new Rectangle(0, popupInsets.top + parentMenuPosition.top, oldBodyBounds.width, parentMenu.$container.cssHeight());
    let endBodyBounds = new Rectangle(0, popupInsets.top, bodyBounds.width, bodyBounds.height);

    this.bodyAnimating = true;
    this.$container.addClass('animating');
    this.htmlComp.layout.disableAutoPosition();
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

  _completeAnimateRenderSubMenuItems($oldBody) {
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
    this.htmlComp.layout.resetAutoPosition();
    this._processSubMenuQueue();
  }

  _animateResizePopup($comp, popupBounds, targetBounds) {
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

  _animateTextOffset($body, textOffset, targetOffset) {
    targetOffset = scout.nvl(targetOffset, this.$body.data('text-offset'));
    let $menuItems = this.$visibleMenuItems($body);
    $menuItems.each((index, menuItem) => {
      let $menuItem = $(menuItem);
      let $text = $menuItem.children('.text');
      let padding = this._calcTextPaddingLeft($menuItem, textOffset);
      let targetPadding = this._calcTextPaddingLeft($menuItem, targetOffset);
      $text.cssAnimated({paddingLeft: padding}, {paddingLeft: targetPadding}, {duration: this.animationDuration});
    });
  }

  _renderMenuItems(menus, initialSubMenuRendering) {
    menus = menus ? menus : this._getMenuItems();
    if (this.menuFilter) {
      menus = this.menuFilter(menus, MenuDestinations.CONTEXT_MENU);
    }

    if (!menus || menus.length === 0) {
      return;
    }

    menus.forEach(function(menu) {
      // Invisible menus are rendered as well because their visibility might change dynamically
      if (menu.separator) {
        return;
      }

      // prevent loosing original parent
      let originalParent = menu.parent;
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
    }, this);

    this._handleInitialSubMenus(initialSubMenuRendering);
    this._updateFirstLastClass();
    this._adjustTextAlignment();
  }

  _attachCloneMenuListeners(menu) {
    menu.on('propertyChange', this._onCloneMenuPropertyChange.bind(this));
    menu.childActions.forEach(this._attachCloneMenuListeners.bind(this));
  }

  _onCloneMenuPropertyChange(event) {
    if (event.propertyName === 'selected') {
      let menu = event.source;
      // Only trigger property change, setSelected would try to render the selected state which must not happen for the original menu
      menu.cloneOf.triggerPropertyChange('selected', event.oldValue, event.newValue);
    }
  }

  _handleInitialSubMenus(initialSubMenuRendering) {
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

  _attachMenuListeners(menu) {
    let menuItemActionHandler = this._onMenuItemAction.bind(this);
    let menuItemPropertyChange = this._onMenuItemPropertyChange.bind(this);
    menu.on('action', menuItemActionHandler);
    menu.on('propertyChange', menuItemPropertyChange);
    this.one('remove', () => {
      menu.off('action', menuItemActionHandler);
      menu.off('propertyChange', menuItemPropertyChange);
    });
  }

  updateMenuItems(menuItems) {
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
  _getMenuItems() {
    return this.menuItems;
  }

  /**
   * Currently rendered $menuItems
   */
  $menuItems() {
    return this.$body.children('.menu-item');
  }

  $visibleMenuItems($body) {
    $body = $body || this.$body;
    return $body.children('.menu-item:visible');
  }

  /**
   * Updates the first and last visible menu items with the according css classes.
   * Necessary because invisible menu-items are rendered.
   */
  _updateFirstLastClass(event) {
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

  updateNextToSelected(menuItemClass, $selectedItem) {
    menuItemClass = menuItemClass ? menuItemClass : 'menu-item';
    let $all = this.$body.find('.' + menuItemClass);
    $selectedItem = $selectedItem ? $selectedItem : this.$body.find('.' + menuItemClass + '.selected');

    $all.removeClass('next-to-selected');
    if ($selectedItem.hasClass('selected')) {
      $selectedItem.nextAll(':visible').first().addClass('next-to-selected');
    }
  }

  _onMenuItemAction(event) {
    if (event.source.isToggleAction()) {
      return;
    }
    this.close();
  }

  _onMenuItemPropertyChange(event) {
    if (!this.rendered) {
      return;
    }
    if (event.propertyName === 'visible') {
      this._updateFirstLastClass();
    } else if (event.propertyName === 'selected') {
      // Key stroke navigation marks the currently focused item as selected.
      // When a sub menu item is opened while another element is selected (focused), make sure the other element gets unselected.
      // Otherwise two items would be selected when the sub menu is closed again.
      this._deselectSiblings(event.source);
    }
    // Make sure menu is positioned correctly afterwards (if it is opened upwards hiding/showing a menu item makes it necessary to reposition)
    this.position();
  }

  /**
   * Deselects the visible siblings of the given menu item. It just removes the CSS class and does not modify the selected property.
   */
  _deselectSiblings(menuItem) {
    menuItem.$container.siblings('.menu-item').each((i, elem) => {
      let $menuItem = $(elem);
      $menuItem.select(false);
    }, this);
  }

  _adjustTextAlignment($body) {
    $body = $body || this.$body;
    let $menuItems = this.$visibleMenuItems($body);
    let textOffset = this._calcTextOffset($menuItems);
    $body.data('text-offset', textOffset);
    this._updateTextOffset(textOffset, $menuItems);
  }

  _calcTextOffset($menuItems) {
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

  _updateTextOffset(textOffset, $menuItems) {
    // Update the padding of each text such that the sum of icon width and the padding
    // are the same for all items. This ensures that the texts are all aligned.
    $menuItems = $menuItems || this.$visibleMenuItems();
    $menuItems.each((index, menuItem) => {
      let $menuItem = $(menuItem);
      let $text = $menuItem.children('.text');
      $text.css('padding-left', this._calcTextPaddingLeft($menuItem, textOffset));
      let htmlComp = HtmlComponent.optGet($menuItem);
      if (htmlComp) {
        htmlComp.invalidateLayout();
      }
    });
  }

  _calcTextPaddingLeft($menuItem, textOffset) {
    let $icon = $menuItem.children('.icon');
    let iconWidth = 0;

    if ($icon.length > 0) {
      iconWidth = $icon.outerWidth(true);
    }
    return textOffset - iconWidth;
  }
}
