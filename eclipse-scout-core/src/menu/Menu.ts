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
  Action, ActionKeyStroke, aria, arrays, CloneOptions, ContextMenuPopup, EnumObject, HtmlComponent, icons, InitModelOf, MenuBarPopup, MenuDestinations, MenuEventMap, MenuExecKeyStroke, MenuKeyStroke, MenuModel, MenuOrder,
  ObjectOrChildModel, Popup, PopupAlignment, PropertyChangeEvent, scout, strings, tooltips, TreeVisitor, TreeVisitResult
} from '../index';

export type SubMenuVisibility = EnumObject<typeof Menu.SubMenuVisibility>;
export type MenuStyle = EnumObject<typeof Menu.MenuStyle>;
export type MenuFilter = (menus: Menu[], destination: MenuDestinations) => Menu[];

export class Menu extends Action implements MenuModel {
  declare model: MenuModel;
  declare eventMap: MenuEventMap;
  declare self: Menu;

  childActions: Menu[];
  defaultMenu: boolean;
  excludedByFilter: boolean;
  menuTypes: string[];
  menuStyle: MenuStyle;
  uiCssClass: string;
  overflowMenu: Menu;
  /**
   * This property is set if this is a subMenu
   */
  parentMenu: Menu;
  ellipsis: boolean;
  rightAligned: boolean;
  popup: Popup;
  popupHorizontalAlignment: PopupAlignment;
  popupVerticalAlignment: PopupAlignment;
  stackable: boolean;
  separator: boolean;
  shrinkable: boolean;
  subMenuVisibility: SubMenuVisibility;
  menuFilter: MenuFilter;
  createdBy: MenuOrder;
  $submenuIcon: JQuery;
  $subMenuBody: JQuery;
  $placeHolder: JQuery;

  constructor() {
    super();

    this.childActions = [];
    this.defaultMenu = null;
    this.excludedByFilter = false;
    this.menuTypes = [];
    this.menuStyle = Menu.MenuStyle.NONE;
    this.parentMenu = null;
    this.popup = null;
    this.popupHorizontalAlignment = undefined;
    this.popupVerticalAlignment = undefined;
    this.stackable = true;
    this.separator = false;
    this.shrinkable = false;
    this.rightAligned = false;
    this.subMenuVisibility = Menu.SubMenuVisibility.DEFAULT;
    this.menuFilter = null;
    this.$submenuIcon = null;
    this.$subMenuBody = null;
    this._addCloneProperties(['defaultMenu', 'menuTypes', 'overflow', 'stackable', 'separator', 'shrinkable', 'parentMenu', 'menuFilter', 'subMenuVisibility']);
    this._addWidgetProperties('childActions');
  }

  static SUBMENU_ICON = icons.ANGLE_DOWN_BOLD;

  /**
   * Special styles of the menu, calculated by the MenuBar. The default value is MenuStyle.NONE.
   */
  static MenuStyle = {
    NONE: 0,
    DEFAULT: 1
  } as const;

  static SubMenuVisibility = {
    /**
     * Default: sub-menu icon is only visible when menu has text.
     */
    DEFAULT: 'default',
    /**
     * Text or icon: sub-menu icon is only visible when menu has text or an icon.
     */
    TEXT_OR_ICON: 'textOrIcon',
    /**
     * Always: sub-menu icon is always visible when menu has child-actions.
     */
    ALWAYS: 'always',
    /**
     * Never: sub-menu icon never visible.
     */
    NEVER: 'never'
  } as const;

  protected override _init(options: InitModelOf<this>) {
    super._init(options);
    this._setChildActions(this.childActions);
  }

  protected override _initKeyStrokeContext() {
    super._initKeyStrokeContext();

    this.keyStrokeContext.registerKeyStroke(new MenuExecKeyStroke(this));
  }

  protected override _render() {
    if (this.separator) {
      this._renderSeparator();
    } else {
      this._renderItem();
    }
    this.$container.unfocusable();
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderMenuStyle();
    this._renderActionStyle();
    this._updateIconAndTextStyle();
  }

  protected override _remove() {
    super._remove();
    this.$submenuIcon = null;
    this.$subMenuBody = null;
  }

  protected _renderSeparator() {
    this.$container = this.$parent.appendDiv('menu-separator');
  }

  protected _renderItem() {
    this.$container = this.$parent.appendDiv('menu-item');
    if (this.uiCssClass) {
      this.$container.addClass(this.uiCssClass);
    }

    let mouseEventHandler = this._onMouseEvent.bind(this);
    this.$container
      .on('mousedown', mouseEventHandler)
      .on('contextmenu', mouseEventHandler)
      .on('click', mouseEventHandler);

    this._renderSubMenuIcon();
  }

  protected override _renderActionStyle() {
    this.$container.toggleClass('menu-button', this.isButton() && !this.overflown);
    this.updateAriaRole();
  }

  override _renderToggleAction() {
    this.updateAriaRole();
  }

  /**
   * Aria role for menus is based on multiple properties. Properties that influence the menu role should call
   * this update when rendering the property
   */
  updateAriaRole() {
    if (this.separator) {
      aria.role(this.$container, 'separator');
      return;
    }
    let hasPopup = this._doActionTogglesSubMenu() || this._doActionTogglesPopup();
    aria.role(this.$container, this.isToggleAction() && !hasPopup ? 'menuitemcheckbox' : 'menuitem');
  }

  protected override _renderSelected() {
    if (!this._doActionTogglesSubMenu()) {
      super._renderSelected();
      // Cannot be done in ContextMenuPopup,
      // because the property change event is fired before renderSelected is called,
      // and updateNextToSelected depends on the UI state
      if (this.parent instanceof ContextMenuPopup) {
        this.parent.updateNextToSelected();
      }
    }
    let hasPopup = this._doActionTogglesSubMenu() || this._doActionTogglesPopup();
    if (this.selected) {
      if (this._doActionTogglesSubMenu()) {
        this._renderSubMenuItems(this, this.childActions);
      } else if (this._doActionTogglesPopup()) {
        this._openPopup();
      }
      aria.expanded(this.$container, hasPopup ? true : null);
    } else {
      if (this._doActionTogglesSubMenu() && this.rendered) {
        this._removeSubMenuItems(this);
      } else {
        this._closePopup();
        this._closeSubMenus();
      }
      aria.expanded(this.$container, hasPopup ? false : null);
    }
    this.$container.toggleClass('has-popup', hasPopup);
    aria.hasPopup(this.$container, hasPopup ? 'menu' : null);
    aria.pressed(this.$container, null); // remove pressed set by action
    aria.checked(this.$container, this.isToggleAction() && !hasPopup ? this.selected : null);
  }

  protected _closeSubMenus() {
    this.childActions.forEach(menu => {
      if (menu._doActionTogglesPopup()) {
        menu._closeSubMenus();
        menu.setSelected(false);
      }
    });
  }

  protected _removeSubMenuItems(parentMenu: Menu) {
    if (this.parent instanceof ContextMenuPopup) {
      this.parent.removeSubMenuItems(parentMenu, true);
    } else if (this.parent instanceof Menu) {
      this.parent._removeSubMenuItems(parentMenu);
    }
  }

  protected _renderSubMenuItems(parentMenu: Menu, menus: Menu[]) {
    let parent = this.parent;
    if (parent instanceof ContextMenuPopup) {
      parent.renderSubMenuItems(parentMenu, menus, true);
      let closeHandler = event => parentMenu.setSelected(false);
      let selectedChangeChangeHandler = (event: PropertyChangeEvent<boolean>) => {
        if (event.newValue === false) {
          parent.off('destroy', closeHandler);
          parentMenu.off('propertyChange:selected', selectedChangeChangeHandler);
        }
      };
      parent.one('destroy', closeHandler);
      parentMenu.on('propertyChange:selected', selectedChangeChangeHandler);
    } else if (parent instanceof Menu) {
      parent._renderSubMenuItems(parentMenu, menus);
    }
  }

  /**
   * Override this method to control the toggles sub-menu behavior when this menu instance is used as parent.
   * Some menu subclasses like the ComboMenu need to show the popup menu instead.
   * @see _doActionTogglesSubMenu
   */
  protected _togglesSubMenu(): boolean {
    return true;
  }

  /** @internal */
  _doActionTogglesSubMenu(): boolean {
    if (!this.childActions.length) {
      return false;
    }
    if (this.parent instanceof ContextMenuPopup) {
      return true;
    }
    if (this.parent instanceof Menu) {
      return this.parent._togglesSubMenu();
    }
    return false;
  }

  protected _onMouseEvent(event: JQuery.MouseEventBase) {
    if (event.type === 'mousedown') {
      this._doubleClickSupport.mousedown(event as JQuery.MouseDownEvent);
    }
    if (!this._allowMouseEvent(event)) {
      return;
    }

    // When the action is clicked the user wants to execute the action and not see the tooltip -> cancel the task
    // If it is already displayed it will stay
    tooltips.cancel(this.$container);

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
  protected _doActionTogglesPopup(): boolean {
    return this.childActions.length > 0;
  }

  protected _renderChildActions() {
    // Child action in a sub menu cannot be replaced dynamically, popup has to be closed first.
    if (!this.rendering) {
      this._renderSubMenuIcon();
    }
  }

  setSubMenuVisibility(subMenuVisibility: SubMenuVisibility) {
    this.setProperty('subMenuVisibility', subMenuVisibility);
  }

  protected _renderSubMenuVisibility() {
    this._renderSubMenuIcon();
  }

  protected _renderSubMenuIcon() {
    let visible = false;

    // calculate visibility of sub-menu icon
    if (this.childActions.length > 0) {
      switch (this.subMenuVisibility) {
        case Menu.SubMenuVisibility.DEFAULT:
          visible = this._hasText();
          break;
        case Menu.SubMenuVisibility.TEXT_OR_ICON:
          visible = this._hasText() || !!this.iconId;
          break;
        case Menu.SubMenuVisibility.ALWAYS:
          visible = true;
          break;
        case Menu.SubMenuVisibility.NEVER:
          visible = false;
          break;
      }
    }

    if (visible) {
      if (!this.$submenuIcon) {
        let icon = icons.parseIconId(Menu.SUBMENU_ICON);
        this.$submenuIcon = this.$container
          .appendSpan('submenu-icon')
          .text(icon.iconCharacter);
        aria.hidden(this.$submenuIcon, true);
        this.invalidateLayoutTree();
      }
    } else {
      if (this.$submenuIcon) {
        this.$submenuIcon.remove();
        this.$submenuIcon = null;
        this.invalidateLayoutTree();
      }
    }
    if (!this.rendering) {
      this._renderTextPosition();
      this._updateIconAndTextStyle();
    }
  }

  protected override _renderText() {
    super._renderText();
    this.$container.toggleClass('has-text', strings.hasText(this.text) && this.textVisible);
    if (!this.rendering) {
      this._renderSubMenuIcon();
    }
    this.invalidateLayoutTree();
  }

  protected override _renderTextPosition() {
    super._renderTextPosition();
    let $parent = this.$container;
    if (this.textPosition === Action.TextPosition.BOTTOM && this.$text && this.iconId) {
      // Move submenu icon into text
      $parent = this.$text;
    }
    if (this.$submenuIcon) {
      // Always append to make sure submenu-icon is the last element in the DOM
      this.$submenuIcon.appendTo($parent);
    }
  }

  protected override _renderIconId() {
    super._renderIconId();
    this.$container.toggleClass('has-icon', !!this.iconId);
    if (!this.rendering) {
      this._renderSubMenuIcon();
    }
    this.invalidateLayoutTree();
  }

  isTabTarget(): boolean {
    return this.enabledComputed && this.visible && !this.overflown && (this.isButton() || !this.separator)
      && (!this.parentMenu || this.parentMenu.visible && !this.parentMenu.overflown); // Necessary for ComboMenu -> must return false if ComboMenu (parentMenu) is not shown
  }

  override recomputeEnabled(parentEnabled?: boolean) {
    if (parentEnabled === undefined) {
      parentEnabled = this._getInheritedAccessibility();
    }

    let enabledComputed;
    let enabledStateForChildren;
    if (this.enabled && this.inheritAccessibility && !parentEnabled && this.childActions.length > 0) {
      // the enabledComputed state here depends on the child actions:
      // - if there are childActions which have inheritAccessibility=false (recursively): this action must be enabledComputed=true so that these children can be reached
      // - otherwise this menu is set to enabledComputed=false
      enabledComputed = this._hasAccessibleChildMenu();
      if (enabledComputed) {
        // this composite menu is only active because it has children with inheritAccessibility=true
        // but child-menus should consider the container parent instead, otherwise all children would be enabled (because this composite menu is enabled now)
        enabledStateForChildren = parentEnabled;
      } else {
        enabledStateForChildren = false;
      }
    } else {
      enabledComputed = this._computeEnabled(this.inheritAccessibility, parentEnabled);
      enabledStateForChildren = enabledComputed;
    }

    this._updateEnabledComputed(enabledComputed, enabledStateForChildren);
  }

  /**
   * Calculates the inherited enabled state of this menu. This is the enabled state of the next relevant parent.
   * A relevant parent is either
   * - the next parent menu with inheritAccessibility=false
   * - or the container of the menu (the parent of the root menu)
   *
   * The enabled state of the container must be used because the parent menu might be a menu which is only enabled because it has children with inheritAccessibility=false.
   * One exception: if a parent menu itself is inheritAccessibility=false. Then the container is not relevant anymore but this parent is taken instead.
   */
  protected _getInheritedAccessibility(): boolean {
    let menu: Menu = this;
    let rootMenu = menu;
    while (menu) {
      if (!menu.inheritAccessibility) {
        // not inherited. no need to check any more parent widgets
        return menu.enabled; /* do not use enabledComputed here because the parents have no effect */
      }
      rootMenu = menu;
      menu = menu.parentMenu;
    }

    let container = rootMenu.parent;
    if (container && container.initialized && container.enabledComputed !== undefined) {
      return container.enabledComputed;
    }
    return true;
  }

  protected _findRootMenu(): Menu {
    let menu: Menu = this;
    let result;
    while (menu) {
      result = menu;
      menu = menu.parentMenu;
    }
    return result;
  }

  protected _hasAccessibleChildMenu(): boolean {
    let childFound = false;
    this.visitChildMenus(child => {
      if (!child.inheritAccessibility && child.enabled /* do not use enabledComputed here */ && child.visible) {
        childFound = true;
        return TreeVisitResult.TERMINATE;
      }
      return TreeVisitResult.CONTINUE;
    });
    return childFound;
  }

  /**
   * cannot use Widget#visitChildren() here because the child actions are not always part of the children collection
   * e.g. for ellipsis menus which declare childActions as 'PreserveOnPropertyChangeProperties'. this means the childActions are not automatically added to the children list even it is a widget property!
   */
  visitChildMenus(visitor: TreeVisitor<Menu>): TreeVisitResult {
    for (let i = 0; i < this.childActions.length; i++) {
      let child = this.childActions[i];
      if (child instanceof Menu) {
        let treeVisitResult = visitor(child);
        if (treeVisitResult === true || treeVisitResult === TreeVisitResult.TERMINATE) {
          // Visitor wants to abort the visiting
          return TreeVisitResult.TERMINATE;
        } else if (treeVisitResult !== TreeVisitResult.SKIP_SUBTREE) {
          treeVisitResult = child.visitChildMenus(visitor);
          if (treeVisitResult === TreeVisitResult.TERMINATE) {
            return TreeVisitResult.TERMINATE;
          }
        }
      }
    }
  }

  protected _hasText(): boolean {
    return strings.hasText(this.text) && this.textVisible;
  }

  protected _updateIconAndTextStyle() {
    let hasText = this._hasText();
    let hasTextAndIcon = !!(hasText && this.iconId);
    let hasIcon = !!this.iconId;
    let hasSubMenuIcon = !!this.$submenuIcon;
    let hasOneIcon = (hasIcon && !hasSubMenuIcon) || (!hasIcon && hasSubMenuIcon);
    this.$container.toggleClass('menu-textandicon', hasTextAndIcon);
    this.$container.toggleClass('menu-icononly', !hasText && hasOneIcon);
  }

  /** @internal */
  _closePopup() {
    if (this.popup && !this.popup.isRemovalPending()) {
      this.popup.close();
    }
  }

  protected _canOpenPopup(): boolean {
    if (this.popup && this.popup.isRemovalPending()) {
      // If the popup should be opened while it is being removed, the popup needs to be removed immediately before it can be opened (the remove animation won't complete).
      // This is necessary to always have a consistent state between menu and popup (e.g. if menu is selected while the popup is removed).
      // The popup will be null afterwards (due to the destroy handler added by openPopup)
      this.popup.removeImmediately();
    }

    if (this.popup) {
      // already open
      return false;
    }

    // Recheck if opening is still possible (maybe destroying the popup changed that, e.g. form of form menu was set to null)
    if (!this._doActionTogglesPopup()) {
      return false;
    }
    return true;
  }

  protected _openPopup() {
    if (!this._canOpenPopup()) {
      return;
    }
    this.popup = this._createPopup();
    this.popup.open();
    aria.linkElementWithControls(this.$container, this.popup.$container);
    this.popup.one('destroy', event => {
      this.popup = null;
      aria.removeControls(this.$container);
    });
    // Unselect on close which comes earlier than destroy (before the animation), to give more immediate feedback
    this.popup.on('close', event => {
      this.setSelected(false);
    });

    if (this.uiCssClass) {
      this.popup.$container.addClass(this.uiCssClass);
    }
  }

  protected _createPopup(): Popup {
    return scout.create(MenuBarPopup, {
      parent: this,
      menu: this,
      menuFilter: this.menuFilter,
      horizontalAlignment: this.popupHorizontalAlignment,
      verticalAlignment: this.popupVerticalAlignment
    });
  }

  protected override _createActionKeyStroke(): ActionKeyStroke {
    return new MenuKeyStroke(this);
  }

  override isToggleAction(): boolean {
    return this.childActions.length > 0 || this.toggleAction;
  }

  isButton(): boolean {
    return Action.ActionStyle.BUTTON === this.actionStyle;
  }

  insertChildAction(actionsToInsert: ObjectOrChildModel<Menu>) {
    this.insertChildActions([actionsToInsert]);
  }

  insertChildActions(actionsToInsert: ObjectOrChildModel<Menu> | ObjectOrChildModel<Menu>[]) {
    actionsToInsert = arrays.ensure(actionsToInsert);
    if (actionsToInsert.length === 0) {
      return;
    }
    let actions = this.childActions as ObjectOrChildModel<Menu>[];
    this.setChildActions(actions.concat(actionsToInsert));
  }

  deleteChildAction(actionToDelete: Menu) {
    this.deleteChildActions([actionToDelete]);
  }

  deleteChildActions(actionsToDelete: Menu | Menu[]) {
    actionsToDelete = arrays.ensure(actionsToDelete);
    if (actionsToDelete.length === 0) {
      return;
    }
    let actions = this.childActions.slice();
    arrays.removeAll(actions, actionsToDelete);
    this.setChildActions(actions);
  }

  setChildActions(childActions: ObjectOrChildModel<Menu>[]) {
    this.setProperty('childActions', childActions);
  }

  protected _setChildActions(childActions: Menu[]) {
    // disconnect existing
    this.childActions.forEach(childAction => {
      childAction.parentMenu = null;
    });

    this._setProperty('childActions', childActions);

    // connect new actions
    this.childActions.forEach(childAction => {
      childAction.parentMenu = this;
    });

    if (this.initialized) {
      this.recomputeEnabled();
    }
  }

  protected override _setInheritAccessibility(inheritAccessibility: boolean) {
    let changed = this._setProperty('inheritAccessibility', inheritAccessibility);
    if (changed) {
      this._recomputeEnabledInMenuHierarchy();
    }
  }

  protected override _setEnabled(enabled: boolean) {
    let changed = this._setProperty('enabled', enabled);
    if (changed) {
      this._recomputeEnabledInMenuHierarchy();
    }
  }

  protected _setVisible(visible: boolean) {
    let changed = this._setProperty('visible', visible);
    if (changed) {
      this._recomputeEnabledInMenuHierarchy();
    }
  }

  protected _recomputeEnabledInMenuHierarchy() {
    if (!this.initialized) {
      return;
    }
    let rootMenu = this._findRootMenu();
    rootMenu.recomputeEnabled();
    if (rootMenu !== this) {
      // necessary in case this menu or a parent menu has inheritAccessibility=false. Because then this menu and its children are skipped in the line above!
      this.recomputeEnabled();
    }
  }

  override setSelected(selected: boolean) {
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

  protected _handleSelectedInEllipsis() {
    // If the selection toggles a popup, open the ellipsis menu as well, otherwise the popup would not be shown
    if (this.selected) {
      this.overflowMenu.setSelected(true);
    }
  }

  setStackable(stackable: boolean) {
    this.setProperty('stackable', stackable);
  }

  protected _renderStackable() {
    this.invalidateLayoutTree();
  }

  setShrinkable(shrinkable: boolean) {
    this.setProperty('shrinkable', shrinkable);
  }

  protected _renderShrinkable() {
    this.invalidateLayoutTree();
  }

  protected override _renderOverflown() {
    super._renderOverflown();
    this._renderActionStyle();
  }

  setMenuTypes(menuTypes: string[]) {
    this.setProperty('menuTypes', menuTypes);
  }

  setMenuStyle(menuStyle: MenuStyle) {
    this.setProperty('menuStyle', menuStyle);
  }

  protected _renderMenuStyle() {
    this.$container.toggleClass('default', this.menuStyle === Menu.MenuStyle.DEFAULT);
  }

  setDefaultMenu(defaultMenu: boolean) {
    this.setProperty('defaultMenu', defaultMenu);
  }

  setMenuFilter(menuFilter: MenuFilter) {
    this.setProperty('menuFilter', menuFilter);
    this.childActions.forEach(child => child.setMenuFilter(menuFilter));
  }

  override clone(model: MenuModel, options: CloneOptions): this {
    let clone = super.clone(model, options) as Menu;
    this._deepCloneProperties(clone, 'childActions', options);
    clone._setChildActions(clone.childActions);
    return clone as this;
  }

  override focus(): boolean {
    let event = this.trigger('focus');
    if (!event.defaultPrevented) {
      return super.focus();
    }
    return false;
  }
}
