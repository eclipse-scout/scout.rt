/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
import Action, {ActionStyle} from '../action/Action';
import HtmlComponent from '../layout/HtmlComponent';
import * as strings from '../util/strings';
import * as scout from '../scout';

export const SUBMENU_ICON = '';// icons.ANGLE_DOWN_BOLD;

/**
 * Special styles of the menu, calculated by the MenuBar. The default value is MenuStyle.NONE.
 */
export const MenuStyle = {
  NONE: 0,
  DEFAULT: 1
};

export default class Menu extends Action {
  constructor() {
    super();

    this.childActions = [];
    this.defaultMenu = null; // null = determined by the menu bar
    this.excludedByFilter = false;
    this.menuTypes = [];
    this.menuStyle = MenuStyle.NONE;
    /**
     * This property is true when the menu instance was moved into a overflow-menu
     * when there's not enough space on the screen (see MenuBarLayout.js). When set
     * to true, button style menus must be displayed as regular menus.
     */
    this.overflown = false;
    /**
     * This property is set if this is a subMenu. The property is set when this submenu is rendered.
     */
    this.parentMenu = null;
    this.popup = null;
    this.preventDoubleClick = false;
    this.stackable = true;
    this.separator = false;
    this.shrinkable = false;

    this.menuFilter = null;

    this._addCloneProperties(['defaultMenu', 'menuTypes', 'overflow', 'stackable', 'separator', 'shrinkable']);
    this._addWidgetProperties('childActions');
  }

  /**
   * @override
   */
  _initKeyStrokeContext() {
    super._initKeyStrokeContext.call(this);

    // this.keyStrokeContext.registerKeyStroke(new MenuExecKeyStroke(this));
  }

  _render() {
    if (this.separator) {
      this._renderSeparator();
    } else {
      this._renderItem();
    }
    this.$container.unfocusable();
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
  }

  _renderProperties() {
    super._renderProperties.call(this);
    this._renderOverflown();
    this._renderMenuStyle();
    this._renderMenuButton();
  }

  _remove() {
    super._remove.call(this);
    this.$submenuIcon = null;
    this.$subMenuBody = null;
  }

  _renderSeparator() {
    this.$container = this.$parent.appendDiv('menu-separator');
  }

  _renderItem() {
    this.$container = this.$parent.appendDiv('menu-item');
    if (this.uiCssClass) {
      this.$container.addClass(this.uiCssClass);
    }

    var mouseEventHandler = this._onMouseEvent.bind(this);
    this.$container
      .on('mousedown', mouseEventHandler)
      .on('contextmenu', mouseEventHandler)
      .on('click', mouseEventHandler);

    this._renderSubMenuIcon();
  }

  _renderMenuButton() {
    this.$container.toggleClass('menu-button', this.isButton() && !this.overflown);
  }

  _renderSelected() {
    if (!this._doActionTogglesSubMenu()) {
      super._renderSelected.call(this);
      // Cannot be done in ContextMenuPopup,
      // because the property change event is fired before renderSelected is called,
      // and updateNextToSelected depends on the UI state
      // if (this.parent instanceof ContextMenuPopup) {
      //   this.parent.updateNextToSelected();
      // }
    }
    if (this.selected) {
      if (this._doActionTogglesSubMenu()) {
        this._renderSubMenuItems(this, this.childActions);
      } else if (this._doActionTogglesPopup()) {
        this._openPopup();
      }
    } else {
      if (this._doActionTogglesSubMenu() && this.rendered) {
        this._removeSubMenuItems(this);
      } else {
        this._closePopup();
        this._closeSubMenues();
      }
    }
  }

  _closeSubMenues() {
    this.childActions.forEach(function(menu) {
      if (menu._doActionTogglesPopup()) {
        menu._closeSubMenues();
        menu.setSelected(false);
      }
    });
  }

  _removeSubMenuItems(parentMenu) {
    if (this.parent instanceof ContextMenuPopup) {
      this.parent.removeSubMenuItems(parentMenu, true);
    } else if (this.parent instanceof Menu) {
      this.parent._removeSubMenuItems(parentMenu);
    }
  }

  _renderSubMenuItems(parentMenu, menus) {
    // if (this.parent instanceof ContextMenuPopup) {
    //   this.parent.renderSubMenuItems(parentMenu, menus, true);
    //   var closeHandler = function(event) {
    //     parentMenu.setSelected(false);
    //   }.bind(this);
    //   var propertyChangeHandler = function(event) {
    //     if (event.propertyName === 'selected' && event.newValue === false) {
    //       this.parent.off('close', closeHandler);
    //       parentMenu.off('propertyChange', propertyChangeHandler);
    //     }
    //   }.bind(this);
    //   this.parent.on('close', closeHandler);
    //   parentMenu.on('propertyChange', propertyChangeHandler);
    // } else if (this.parent instanceof Menu) {
    //   this.parent._renderSubMenuItems(parentMenu, menus);
    // }
  }

  _doActionTogglesSubMenu() {
    return false; // this.childActions.length > 0 && (this.parent instanceof ContextMenuPopup || this.parent instanceof Menu);
  }

  _getSubMenuLevel() {
    // if (this.parent instanceof ContextMenuPopup) {
    //   return 0;
    // }
    return super._getSubMenuLevel.call(this) + 1;
  }

  _onMouseEvent(event) {
    if (!this._allowMouseEvent(event)) {
      return;
    }

    // When the action is clicked the user wants to execute the action and not see the tooltip -> cancel the task
    // If it is already displayed it will stay
    // tooltips.cancel(this.$container);

    // If menu has childActions, a popup should be rendered on click. To create
    // the impression of a faster UI, open the popup already on 'mousedown', not
    // on 'click'. All other actions are handled on 'click'.
    if (event.type === 'mousedown' && this._doActionTogglesPopup()) {
      this.doAction();
    } else if ((event.type === 'click' || event.type === 'contextmenu') && !this._doActionTogglesPopup()) {
      this.doAction();
    }
  }

  /**
   * May be overridden if the criteria to open a popup differs
   */
  _doActionTogglesPopup() {
    return this.childActions.length > 0;
  }

  /**
   * Only render child actions if the sub-menu popup is open.
   */
  _renderChildActions() {
    // if (objects.optProperty(this.popup, 'rendered')) {
    //   var $popup = this.popup.$container;
    //   this.childActions.forEach(function(menu) {
    //     menu.render($popup);
    //   });
    // }
    //
    // this._renderSubMenuIcon();
  }

  _renderSubMenuIcon() {
    var shouldBeVisible = this.childActions.length > 0 && this.text;

    if (shouldBeVisible) {
      if (!this.$submenuIcon) {
        // var icon = icons.parseIconId(Menu.SUBMENU_ICON);
        // this.$submenuIcon = this.$container
        //   .appendSpan('submenu-icon')
        //   .text(icon.iconCharacter);
        // this.invalidateLayoutTree();
      }
    } else {
      if (this.$submenuIcon) {
        this.$submenuIcon.remove();
        this.$submenuIcon = null;
        this.invalidateLayoutTree();
      }
    }
  }

  _renderText(text) {
    super._renderText(text);
    // Ensure submenu-icon is the last element in the DOM
    if (this.$submenuIcon) {
      this.$submenuIcon.appendTo(this.$container);
    }
    this.$container.toggleClass('has-text', strings.hasText(this.text) && this.textVisible);
    this._updateIconAndTextStyle();
    this.invalidateLayoutTree();
  }

  _renderIconId() {
    super._renderIconId.call(this);
    this.$container.toggleClass('has-icon', !!this.iconId);
    this._updateIconAndTextStyle();
    this.invalidateLayoutTree();
  }

  isTabTarget() {
    return this.enabledComputed && this.visible && !this.overflown && (this.isButton() || !this.separator);
  }

  _updateIconAndTextStyle() {
    var hasText = strings.hasText(this.text) && this.textVisible;
    var hasTextAndIcon = !!(hasText && this.iconId);
    this.$container.toggleClass('menu-textandicon', hasTextAndIcon);
    this.$container.toggleClass('menu-icononly', !hasText);
  }

  _closePopup() {
    if (this.popup) {
      this.popup.close();
    }
  }

  _openPopup() {
    if (this.popup) {
      // already open
      return;
    }
    this.popup = this._createPopup();
    this.popup.open();
    this.popup.on('remove', function(event) {
      this.popup = null;
    }.bind(this));
    // Reason for separating remove and close event:
    // Remove may be called if parent (menubar) gets removed or rebuilt.
    // In that case, we do not want to change the selected state because after rebuilding the popup should still be open
    // In every other case the state of the menu needs to be reseted if the popup closes
    this.popup.on('close', function(event) {
      this.setSelected(false);
    }.bind(this));

    if (this.uiCssClass) {
      this.popup.$container.addClass(this.uiCssClass);
    }
  }

  _createPopup(event) {
    var options = {
      parent: this,
      menu: this,
      menuFilter: this.menuFilter,
      ignoreEvent: event,
      horizontalAlignment: this.popupHorizontalAlignment,
      verticalAlignment: this.popupVerticalAlignment
    };

    return scout.create('MenuBarPopup', options);
  }

  _createActionKeyStroke() {
    return null; // new MenuKeyStroke(this);
  }

  isToggleAction() {
    return this.childActions.length > 0 || this.toggleAction;
  }

  isButton() {
    return ActionStyle.BUTTON === this.actionStyle;
  }

  setChildActions(childActions) {
    this.setProperty('childActions', childActions);
  }

  setSelected(selected) {
    if (selected === this.selected) {
      return;
    }
    super.setSelected(selected);
    if (!this._doActionTogglesSubMenu() && !this._doActionTogglesPopup()) {
      return;
    }
    // If menu toggles a popup and is in an ellipsis menu which is not selected it needs a special treatment
    if (this.overflowMenu && !this.overflowMenu.selected) {
      this._handleSelectedInEllipsis();
    }
  }

  _handleSelectedInEllipsis() {
    // If the selection toggles a popup, open the ellipsis menu as well, otherwise the popup would not be shown
    if (this.selected) {
      this.overflowMenu.setSelected(true);
    }
  }

  setStackable(stackable) {
    this.setProperty('stackable', stackable);
  }

  setShrinkable(shrinkable) {
    this.setProperty('shrinkable', shrinkable);
  }

  /**
   * For internal usage only.
   * Used by the MenuBarLayout when a menu is moved to the ellipsis drop down.
   */
  _setOverflown(overflown) {
    if (this.overflown === overflown) {
      return;
    }
    this._setProperty('overflown', overflown);
    if (this.rendered) {
      this._renderOverflown();
    }
  }

  _renderOverflown() {
    this.$container.toggleClass('overflown', this.overflown);
    this._renderMenuButton();
  }

  setMenuStyle(menuStyle) {
    this.setProperty('menuStyle', menuStyle);
  }

  _renderMenuStyle() {
    this.$container.toggleClass('default-menu', this.menuStyle === MenuStyle.DEFAULT);
  }

  setDefaultMenu(defaultMenu) {
    this.setProperty('defaultMenu', defaultMenu);
  }

  setMenuFilter(menuFilter) {
    this.setProperty('menuFilter', menuFilter);
  }

  clone(model, options) {
    var clone = super.clone(model, options);
    this._deepCloneProperties(clone, 'childActions', options);
    return clone;
  }
}
