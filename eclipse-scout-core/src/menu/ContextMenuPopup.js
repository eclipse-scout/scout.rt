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
import {Action, arrays, ContextMenuPopupLayout, HtmlComponent, MenuDestinations, menuNavigationKeyStrokes, Popup, PopupWithHead, RowLayout, scrollbars} from '../index';
import $ from 'jquery';

export default class ContextMenuPopup extends PopupWithHead {

  constructor() {
    super();

    // Make sure head won't be rendered, there is a css selector which is applied only if there is a head
    this._headVisible = false;
    this.menuItems = [];
    this.cloneMenuItems = true;
    this._toggleSubMenuQueue = [];
    this.repositionEnabled = true;
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

  _createBodyLayout() {
    return new RowLayout({
      pixelBasedSizing: false
    });
  }

  _render() {
    super._render();
    this._installScrollbars();
    this._renderMenuItems();
  }

  /**
   * @param [options]
   * @override
   */
  _installScrollbars(options) {
    super._installScrollbars({
      axis: 'y'
    });
  }

  /**
   * @override
   */
  get$Scrollable() {
    return this.$body;
  }

  removeSubMenuItems(parentMenu, animated) {
    if (!this.rendered && !this.rendering) {
      return;
    }
    if (this.bodyAnimating) {
      // Let current animation finish and execute afterwards to prevent an unpredictable behavior and inconsistent state
      this._toggleSubMenuQueue.push(this.removeSubMenuItems.bind(this, parentMenu, animated));
      return;
    }

    this.$body = parentMenu.__originalParent.$subMenuBody;
    // move new body to back
    this.$body.insertBefore(parentMenu.$subMenuBody);

    if (parentMenu.__originalParent._doActionTogglesSubMenu) {
      parentMenu.__originalParent._doActionTogglesSubMenu();
    }

    let actualBounds = this.htmlComp.offsetBounds().subtractFromDimension(this.htmlComp.insets());

    this.revalidateLayout();
    this.position();

    if (animated) {
      this.bodyAnimating = true;
      let duration = 300;
      let position = parentMenu.$placeHolder.position();
      parentMenu.$subMenuBody.css({
        width: 'auto',
        height: 'auto'
      });
      let targetBounds = this.htmlComp.offsetBounds().subtractFromDimension(this.htmlComp.insets());
      parentMenu.$subMenuBody.css('box-shadow', 'none');
      this.htmlComp.setBounds(actualBounds);
      if (this.verticalAlignment !== Popup.Alignment.TOP) {
        // set container to element
        parentMenu.$subMenuBody.cssTop();
      }

      this._animateTopAndLeft(this.htmlComp.$comp, actualBounds, targetBounds, duration);

      // move new body to top of popup
      parentMenu.$subMenuBody.cssHeightAnimated(actualBounds.height, parentMenu.$container.cssHeight(), {
        duration: duration,
        queue: false
      });

      let endTopposition = position.top - this.$body.cssHeight(),
        startTopposition = 0 - actualBounds.height;

      parentMenu.$subMenuBody.cssTopAnimated(startTopposition, endTopposition, {
        duration: duration,
        queue: false,
        complete: function() {
          if (parentMenu.$container) { // check if $container is not removed before by closing operation.
            scrollbars.uninstall(parentMenu.$subMenuBody, this.session);
            parentMenu.$placeHolder.replaceWith(parentMenu.$container);
            parentMenu.$container.toggleClass('expanded', false);
            this._updateFirstLastClass();
            this.updateNextToSelected('menu-item', parentMenu.$container);

            parentMenu.$subMenuBody.detach();
            this._installScrollbars();
            this.$body.css('box-shadow', '');
            this.bodyAnimating = false;
            // Do one final layout to fix any potentially wrong sizes (e.g. due to async image loading)
            this._invalidateLayoutTreeAndRepositionPopup();
            let next = this._toggleSubMenuQueue.shift();
            if (next) {
              next();
            }
          }
        }.bind(this)
      });

      this.$body.cssWidthAnimated(actualBounds.width, targetBounds.width, {
        duration: duration,
        start: this.revalidateLayout.bind(this, true),
        progress: this.revalidateLayout.bind(this, false),
        queue: false
      });

      if (targetBounds.height !== actualBounds.height) {
        this.$body.cssHeightAnimated(actualBounds.height, targetBounds.height, {
          duration: duration,
          queue: false
        });
      }
    }
  }

  renderSubMenuItems(parentMenu, menus, animated, initialSubMenuRendering) {
    if (!this.session.desktop.rendered && !initialSubMenuRendering) {
      this.initialSubMenusToRender = {
        parentMenu: parentMenu,
        menus: menus
      };
      return;
    }
    if (!this.rendered && !this.rendering) {
      return;
    }
    if (this.bodyAnimating) {
      // Let current animation finish and execute afterwards to prevent an unpredictable behavior and inconsistent state
      this._toggleSubMenuQueue.push(this.renderSubMenuItems.bind(this, parentMenu, menus, animated, initialSubMenuRendering));
      return;
    }

    let actualBounds = this.htmlComp.offsetBounds().subtractFromDimension(this.htmlComp.insets());

    parentMenu.__originalParent.$subMenuBody = this.$body;

    let $all = this.$body.find('.' + 'menu-item');
    $all.removeClass('next-to-selected');

    if (!parentMenu.$subMenuBody) {
      this._$createBody();
      parentMenu.$subMenuBody = this.$body;
      this._renderMenuItems(menus, initialSubMenuRendering);
    } else {
      // append $body
      this.$body = parentMenu.$subMenuBody;
    }
    let $insertAfterElement = parentMenu.$container.prev();
    let position = parentMenu.$container.position();
    parentMenu.$placeHolder = parentMenu.$container.clone();
    // HtmlComponent is necessary for the row layout (it would normally be installed by Menu.js, but $placeholder is just a jquery clone of parentMenu.$container and is not managed by a real widget)
    HtmlComponent.install(parentMenu.$placeHolder, this.session);
    if ($insertAfterElement.length) {
      parentMenu.$placeHolder.insertAfter($insertAfterElement);
    } else {
      parentMenu.__originalParent.$subMenuBody.prepend(parentMenu.$placeHolder);
    }

    this.$body.insertAfter(parentMenu.__originalParent.$subMenuBody);
    this.$body.prepend(parentMenu.$container);
    parentMenu.$container.toggleClass('expanded');

    this.revalidateLayout();
    this.position();

    this.updateNextToSelected();

    if (animated) {
      this.bodyAnimating = true;
      let duration = 300;
      parentMenu.__originalParent.$subMenuBody.css({
        width: 'auto',
        height: 'auto'
      });
      let targetBounds = this.htmlComp.offsetBounds().subtractFromDimension(this.htmlComp.insets());

      this._animateTopAndLeft(this.htmlComp.$comp, actualBounds, targetBounds, duration);

      this.$body.css('box-shadow', 'none');
      // set container to element
      this.$body.cssWidthAnimated(actualBounds.width, targetBounds.width, {
        duration: duration,
        start: this.revalidateLayout.bind(this, true),
        progress: this.revalidateLayout.bind(this, false),
        queue: false
      });

      this.$body.cssHeightAnimated(parentMenu.$container.cssHeight(), targetBounds.height, {
        duration: duration,
        queue: false
      });

      let endTopposition = 0 - targetBounds.height,
        startTopposition = position.top - parentMenu.__originalParent.$subMenuBody.cssHeight(),
        topMargin = 0;

      // move new body to top of popup.
      this.$body.cssTopAnimated(startTopposition, endTopposition, {
        duration: duration,
        queue: false,
        complete: function() {
          this.bodyAnimating = false;
          if (parentMenu.__originalParent.$subMenuBody) {
            scrollbars.uninstall(parentMenu.__originalParent.$subMenuBody, this.session);
            parentMenu.__originalParent.$subMenuBody.detach();
            this.$body.cssTop(topMargin);
            this._installScrollbars();
            this._updateFirstLastClass();
            this.$body.css('box-shadow', '');
          }
          // Do one final layout to fix any potentially wrong sizes (e.g. due to async image loading)
          this._invalidateLayoutTreeAndRepositionPopup();
          let next = this._toggleSubMenuQueue.shift();
          if (next) {
            next();
          }
        }.bind(this)
      });

      if (actualBounds.height !== targetBounds.height) {
        parentMenu.__originalParent.$subMenuBody.cssHeightAnimated(actualBounds.height, targetBounds.height, {
          duration: duration,
          queue: false
        });
        this.$container.cssHeight(actualBounds.height, targetBounds.height, {
          duration: duration,
          queue: false
        });
      }
      if (this.verticalAlignment === Popup.Alignment.TOP) {
        this.$container.cssTopAnimated(actualBounds.y, targetBounds.y, {
          duration: duration,
          queue: false
        }).css('overflow', 'visible');
        // ajust top of head and deco
        this.$head.cssTopAnimated(actualBounds.height, targetBounds.height, {
          duration: duration,
          queue: false
        });
        this.$deco.cssTopAnimated(actualBounds.height - 1, targetBounds.height - 1, {
          duration: duration,
          queue: false
        });
      }
    } else {
      if (!initialSubMenuRendering) {
        scrollbars.uninstall(parentMenu.__originalParent.$subMenuBody, this.session);
      }
      parentMenu.__originalParent.$subMenuBody.detach();
      this._installScrollbars();
      this._updateFirstLastClass();
    }
  }

  _animateTopAndLeft($comp, actualBounds, targetBounds, duration) {
    let options = {
      duration: duration,
      queue: false
    };
    $comp
      .cssTopAnimated(actualBounds.y, targetBounds.y, options)
      .cssLeftAnimated(actualBounds.x, targetBounds.x, options);
  }

  revalidateLayout(repositionEnabled) {
    this.repositionEnabled = scout.nvl(repositionEnabled, true);
    super.revalidateLayout();
    this.repositionEnabled = true;
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
      if (this.cloneMenuItems && !menu.cloneOf) {
        // clone will recursively also clone all child actions.
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
      this._attachMenuListeners(menu);

      // Invalidate popup layout after images icons have been loaded, because the
      // correct size might not be known yet. If the layout would not be revalidated, the popup
      // size will be wrong (text is cut off after image has been loaded).
      // The menu item actually does it by itself, but the popup needs to be repositioned too.
      if (menu.icon) {
        menu.icon.on('load error', this._invalidateLayoutTreeAndRepositionPopup.bind(this));
      }
    }, this);

    this._handleInitialSubMenus(initialSubMenuRendering);
    this._updateFirstLastClass();
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

  /**
   * @override PopupWithHead.js
   */
  _modifyBody() {
    this.$body.addClass('context-menu');
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

  $visibleMenuItems() {
    return this.$body.children('.menu-item:visible');
  }

  /**
   * Updates the first and last visible menu items with the according css classes.
   * Necessary because invisible menu-items are rendered.
   */
  _updateFirstLastClass(event) {
    let $firstMenuItem, $lastMenuItem;

    this.$body.children('.menu-item').each(function() {
      let $menuItem = $(this);
      $menuItem.removeClass('context-menu-item-first context-menu-item-last');

      if ($menuItem.isVisible()) {
        if (!$firstMenuItem) {
          $firstMenuItem = $menuItem;
        }
        $lastMenuItem = $menuItem;
      }
    });
    if ($firstMenuItem) {
      $firstMenuItem.addClass('context-menu-item-first');
    }
    if ($lastMenuItem) {
      $lastMenuItem.addClass('context-menu-item-last');
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

  _invalidateLayoutTreeAndRepositionPopup() {
    this.invalidateLayoutTree();
    this.session.layoutValidator.schedulePostValidateFunction(() => {
      if (!this.rendered) { // check needed because this is an async callback
        return;
      }
      this.position();
    });
  }
}
