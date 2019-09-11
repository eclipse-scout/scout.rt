/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
scout.Menu = function() {
  scout.Menu.parent.call(this);

  this.childActions = [];
  this.defaultMenu = null; // null = determined by the menu bar
  this.excludedByFilter = false;
  this.menuTypes = [];
  this.menuStyle = scout.Menu.MenuStyle.NONE;
  /**
   * This property is true when the menu instance was moved into a overflow-menu
   * when there's not enough space on the screen (see MenuBarLayout.js). When set
   * to true, button style menus must be displayed as regular menus.
   */
  this.overflown = false;
  /**
   * This property is set if this is a subMenu
   */
  this.parentMenu = null;
  this.popup = null;
  this.preventDoubleClick = false;
  this.stackable = true;
  this.separator = false;
  this.shrinkable = false;
  this.subMenuVisibility = scout.Menu.SubMenuVisibility.DEFAULT;

  this.menuFilter = null;

  this._addCloneProperties(['defaultMenu', 'menuTypes', 'overflow', 'stackable', 'separator', 'shrinkable', 'parentMenu', 'menuFilter']);
  this._addWidgetProperties('childActions');
};
scout.inherits(scout.Menu, scout.Action);

scout.Menu.SUBMENU_ICON = scout.icons.ANGLE_DOWN_BOLD;

/**
 * Special styles of the menu, calculated by the MenuBar. The default value is MenuStyle.NONE.
 */
scout.Menu.MenuStyle = {
  NONE: 0,
  DEFAULT: 1
};

scout.Menu.SubMenuVisibility = {
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
  ALWAYS: 'always'
};

scout.Menu.prototype._init = function(options) {
  scout.Menu.parent.prototype._init.call(this, options);
  this._setChildActions(this.childActions);
};

/**
 * @override
 */
scout.Menu.prototype._initKeyStrokeContext = function() {
  scout.Menu.parent.prototype._initKeyStrokeContext.call(this);

  this.keyStrokeContext.registerKeyStroke(new scout.MenuExecKeyStroke(this));
};

scout.Menu.prototype._render = function() {
  if (this.separator) {
    this._renderSeparator();
  } else {
    this._renderItem();
  }
  this.$container.unfocusable();
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
};

scout.Menu.prototype._renderProperties = function() {
  scout.Menu.parent.prototype._renderProperties.call(this);
  this._renderOverflown();
  this._renderMenuStyle();
  this._renderMenuButton();
};

scout.Menu.prototype._remove = function() {
  scout.Menu.parent.prototype._remove.call(this);
  this.$submenuIcon = null;
  this.$subMenuBody = null;
};

scout.Menu.prototype._renderSeparator = function() {
  this.$container = this.$parent.appendDiv('menu-separator');
};

scout.Menu.prototype._renderItem = function() {
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
};

scout.Menu.prototype._renderMenuButton = function() {
  this.$container.toggleClass('menu-button', this.isButton() && !this.overflown);
};

scout.Menu.prototype._renderSelected = function() {
  if (!this._doActionTogglesSubMenu()) {
    scout.Menu.parent.prototype._renderSelected.call(this);
    // Cannot be done in ContextMenuPopup,
    // because the property change event is fired before renderSelected is called,
    // and updateNextToSelected depends on the UI state
    if (this.parent instanceof scout.ContextMenuPopup) {
      this.parent.updateNextToSelected();
    }
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
};

scout.Menu.prototype._closeSubMenues = function() {
  this.childActions.forEach(function(menu) {
    if (menu._doActionTogglesPopup()) {
      menu._closeSubMenues();
      menu.setSelected(false);
    }
  });
};

scout.Menu.prototype._removeSubMenuItems = function(parentMenu) {
  if (this.parent instanceof scout.ContextMenuPopup) {
    this.parent.removeSubMenuItems(parentMenu, true);
  } else if (this.parent instanceof scout.Menu) {
    this.parent._removeSubMenuItems(parentMenu);
  }
};

scout.Menu.prototype._renderSubMenuItems = function(parentMenu, menus) {
  if (this.parent instanceof scout.ContextMenuPopup) {
    this.parent.renderSubMenuItems(parentMenu, menus, true);
    var closeHandler = function(event) {
      parentMenu.setSelected(false);
    }.bind(this);
    var propertyChangeHandler = function(event) {
      if (event.propertyName === 'selected' && event.newValue === false) {
        this.parent.off('close', closeHandler);
        parentMenu.off('propertyChange', propertyChangeHandler);
      }
    }.bind(this);
    this.parent.on('close', closeHandler);
    parentMenu.on('propertyChange', propertyChangeHandler);
  } else if (this.parent instanceof scout.Menu) {
    this.parent._renderSubMenuItems(parentMenu, menus);
  }
};

/**
 * Override this method to control the toggles sub-menu behavior when this menu instance is used as parent.
 * Some menu sub-classes like the ComboMenu need to show the popup menu instead.
 * @see: #_doActionTogglesSubMenu
 */
scout.Menu.prototype._togglesSubMenu = function() {
  return true;
};

scout.Menu.prototype._doActionTogglesSubMenu = function() {
  if (!this.childActions.length) {
    return false;
  }
  if (this.parent instanceof scout.ContextMenuPopup) {
    return true;
  }
  if (this.parent instanceof scout.Menu) {
    return this.parent._togglesSubMenu();
  }
  return false;
};

scout.Menu.prototype._getSubMenuLevel = function() {
  if (this.parent instanceof scout.ContextMenuPopup) {
    return 0;
  }
  return scout.Menu.parent.prototype._getSubMenuLevel.call(this) + 1;
};

scout.Menu.prototype._onMouseEvent = function(event) {
  if (!this._allowMouseEvent(event)) {
    return;
  }

  // When the action is clicked the user wants to execute the action and not see the tooltip -> cancel the task
  // If it is already displayed it will stay
  scout.tooltips.cancel(this.$container);

  // If menu has childActions, a popup should be rendered on click. To create
  // the impression of a faster UI, open the popup already on 'mousedown', not
  // on 'click'. All other actions are handled on 'click'.
  if (event.type === 'mousedown' && this._doActionTogglesPopup()) {
    this.doAction();
  } else if ((event.type === 'click' || event.type === 'contextmenu') && !this._doActionTogglesPopup()) {
    this.doAction();
  }
};

/**
 * May be overridden if the criteria to open a popup differs
 */
scout.Menu.prototype._doActionTogglesPopup = function() {
  return this.childActions.length > 0;
};

/**
 * Only render child actions if the sub-menu popup is open.
 */
scout.Menu.prototype._renderChildActions = function() {
  if (scout.objects.optProperty(this.popup, 'rendered')) {
    var $popup = this.popup.$container;
    this.childActions.forEach(function(menu) {
      menu.render($popup);
    });
  }

  this._renderSubMenuIcon();
};

scout.Menu.prototype._renderSubMenuIcon = function() {
  var visible = false;

  // calculate visibility of sub-menu icon
  if (this.childActions.length > 0) {
    switch (this.subMenuVisibility) {
      case scout.Menu.SubMenuVisibility.DEFAULT:
        visible = this._hasText();
        break;
      case scout.Menu.SubMenuVisibility.TEXT_OR_ICON:
        visible = this._hasText() || this.iconId;
        break;
      case scout.Menu.SubMenuVisibility.ALWAYS:
        visible = true;
        break;
    }
  }

  if (visible) {
    if (!this.$submenuIcon) {
      var icon = scout.icons.parseIconId(scout.Menu.SUBMENU_ICON);
      this.$submenuIcon = this.$container
        .appendSpan('submenu-icon')
        .text(icon.iconCharacter);
      this.invalidateLayoutTree();
    }
  } else {
    if (this.$submenuIcon) {
      this.$submenuIcon.remove();
      this.$submenuIcon = null;
      this.invalidateLayoutTree();
    }
  }
};

scout.Menu.prototype._renderText = function(text) {
  scout.Menu.parent.prototype._renderText.call(this, text);
  // Ensure submenu-icon is the last element in the DOM
  if (this.$submenuIcon) {
    this.$submenuIcon.appendTo(this.$container);
  }
  this.$container.toggleClass('has-text', scout.strings.hasText(this.text) && this.textVisible);
  this._updateIconAndTextStyle();
  this.invalidateLayoutTree();
};

scout.Menu.prototype._renderIconId = function() {
  scout.Menu.parent.prototype._renderIconId.call(this);
  this.$container.toggleClass('has-icon', !!this.iconId);
  this._updateIconAndTextStyle();
  this.invalidateLayoutTree();
};

scout.Menu.prototype.isTabTarget = function() {
  return this.enabledComputed && this.visible && !this.overflown && (this.isButton() || !this.separator);
};

/**
 * @override Widget.js
 */
scout.Menu.prototype.recomputeEnabled = function(parentEnabled) {
  if (parentEnabled === undefined) {
    parentEnabled = this._getInheritedAccessibility();
  }

  var enabledComputed;
  var enabledStateForChildren;
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
};

/**
 * Calculates the inherited enabled state of this menu. This is the enabled state of the next relevant parent.
 * A relevant parent is either
 * - the next parent menu with inheritAccessibility=false
 * - or the container of the menu (the parent of the root menu)
 *
 * The enabled state of the container must be used because the parent menu might be a menu which is only enabled because it has children with inheritAccessibility=false.
 * One exception: if a parent menu itself is inheritAccessibility=false. Then the container is not relevant anymore but this parent is taken instead.
 */
scout.Menu.prototype._getInheritedAccessibility = function() {
  var menu = this;
  var rootMenu = menu;
  while (menu) {
    if (!menu.inheritAccessibility) {
      // not inherited. no need to check any more parent widgets
      return menu.enabled; /* do not use enabledComputed here because the parents have no effect */
    }
    rootMenu = menu;
    menu = menu.parentMenu;
  }

  var container = rootMenu.parent;
  if (container && container.initialized && container.enabledComputed !== undefined) {
    return container.enabledComputed;
  }
  return true;
};

scout.Menu.prototype._findRootMenu = function() {
  var menu = this;
  var result;
  while (menu) {
    result = menu;
    menu = menu.parentMenu;
  }
  return result;
};

scout.Menu.prototype._hasAccessibleChildMenu = function() {
  var childFound = false;
  this.visitChildMenus(function(child) {
    if (!child.inheritAccessibility && child.enabled /* do not use enabledComputed here */ && child.visible) {
      childFound = true;
      return scout.TreeVisitResult.TERMINATE;
    }
    return scout.TreeVisitResult.CONTINUE;
  });
  return childFound;
};

/**
 * cannot use Widget#visitChildren() here because the child actions are not always part of the children collection
 * e.g. for ellipsis menus which declare childActions as 'PreserveOnPropertyChangeProperties'. this means the childActions are not automatically added to the children list even it is a widget property!
 */
scout.Menu.prototype.visitChildMenus = function(visitor) {
  for (var i = 0; i < this.childActions.length; i++) {
    var child = this.childActions[i];
    if (child instanceof scout.Menu) {
      var treeVisitResult = visitor(child);
      if (treeVisitResult === true || treeVisitResult === scout.TreeVisitResult.TERMINATE) {
        // Visitor wants to abort the visiting
        return scout.TreeVisitResult.TERMINATE;
      } else if (treeVisitResult !== scout.TreeVisitResult.SKIP_SUBTREE) {
        treeVisitResult = child.visitChildMenus(visitor);
        if (treeVisitResult === true || treeVisitResult === scout.TreeVisitResult.TERMINATE) {
          return scout.TreeVisitResult.TERMINATE;
        }
      }
    }
  }
};

scout.Menu.prototype._hasText = function() {
  return scout.strings.hasText(this.text) && this.textVisible;
};

scout.Menu.prototype._updateIconAndTextStyle = function() {
  var hasText = this._hasText();
  var hasTextAndIcon = !!(hasText && this.iconId);
  this.$container.toggleClass('menu-textandicon', hasTextAndIcon);
  this.$container.toggleClass('menu-icononly', !hasText);
};

scout.Menu.prototype._closePopup = function() {
  if (this.popup) {
    this.popup.close();
  }
};

scout.Menu.prototype._openPopup = function() {
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
};

scout.Menu.prototype._createPopup = function(event) {
  var options = {
    parent: this,
    menu: this,
    menuFilter: this.menuFilter,
    ignoreEvent: event,
    horizontalAlignment: this.popupHorizontalAlignment,
    verticalAlignment: this.popupVerticalAlignment
  };

  return scout.create('MenuBarPopup', options);
};

scout.Menu.prototype._createActionKeyStroke = function() {
  return new scout.MenuKeyStroke(this);
};

scout.Menu.prototype.isToggleAction = function() {
  return this.childActions.length > 0 || this.toggleAction;
};

scout.Menu.prototype.isButton = function() {
  return scout.Action.ActionStyle.BUTTON === this.actionStyle;
};

scout.Menu.prototype.addChildActions = function(childActions) {
  var newActions = this.childActions.slice();
  scout.arrays.pushAll(newActions, scout.arrays.ensure(childActions));
  this.setChildActions(newActions);
};

scout.Menu.prototype.setChildActions = function(childActions) {
  this.setProperty('childActions', childActions);
};

scout.Menu.prototype._setChildActions = function(childActions) {
  // disconnect existing
  this.childActions.forEach(function(childAction) {
    childAction.parentMenu = null;
  });

  this._setProperty('childActions', childActions);

  // connect new actions
  this.childActions.forEach(function(childAction) {
    childAction.parentMenu = this;
  }.bind(this));

  if (this.initialized) {
    this.recomputeEnabled();
  }
};

/**
 * @override Widget.js
 */
scout.Menu.prototype._setInheritAccessibility = function(inheritAccessibility) {
  this._setProperty('inheritAccessibility', inheritAccessibility);
  if (this.initialized) {
    this._findRootMenu().recomputeEnabled();
  }
};

/**
 * @override Widget.js
 */
scout.Menu.prototype._setEnabled = function(enabled) {
  this._setProperty('enabled', enabled);
  if (this.initialized) {
    this._findRootMenu().recomputeEnabled();
  }
};

scout.Menu.prototype._setVisible = function(visible) {
  this._setProperty('visible', visible);
  if (this.initialized) {
    this._findRootMenu().recomputeEnabled();
  }
};

scout.Menu.prototype.setSelected = function(selected) {
  if (selected === this.selected) {
    return;
  }
  scout.Menu.parent.prototype.setSelected.call(this, selected);
  if (!this._doActionTogglesSubMenu() && !this._doActionTogglesPopup()) {
    return;
  }
  // If menu toggles a popup and is in an ellipsis menu which is not selected it needs a special treatment
  if (this.overflowMenu && !this.overflowMenu.selected) {
    this._handleSelectedInEllipsis();
  }
};

scout.Menu.prototype._handleSelectedInEllipsis = function() {
  // If the selection toggles a popup, open the ellipsis menu as well, otherwise the popup would not be shown
  if (this.selected) {
    this.overflowMenu.setSelected(true);
  }
};

scout.Menu.prototype.setStackable = function(stackable) {
  this.setProperty('stackable', stackable);
};

scout.Menu.prototype.setShrinkable = function(shrinkable) {
  this.setProperty('shrinkable', shrinkable);
};

/**
 * For internal usage only.
 * Used by the MenuBarLayout when a menu is moved to the ellipsis drop down.
 */
scout.Menu.prototype._setOverflown = function(overflown) {
  if (this.overflown === overflown) {
    return;
  }
  this._setProperty('overflown', overflown);
  if (this.rendered) {
    this._renderOverflown();
  }
};

scout.Menu.prototype._renderOverflown = function() {
  this.$container.toggleClass('overflown', this.overflown);
  this._renderMenuButton();
};

scout.Menu.prototype.setMenuStyle = function(menuStyle) {
  this.setProperty('menuStyle', menuStyle);
};

scout.Menu.prototype._renderMenuStyle = function() {
  this.$container.toggleClass('default-menu', this.menuStyle === scout.Menu.MenuStyle.DEFAULT);
};

scout.Menu.prototype.setDefaultMenu = function(defaultMenu) {
  this.setProperty('defaultMenu', defaultMenu);
};

scout.Menu.prototype.setMenuFilter = function(menuFilter) {
  this.setProperty('menuFilter', menuFilter);
  this.childActions.forEach(function(child) {
    child.setMenuFilter(menuFilter);
  });
};

scout.Menu.prototype.clone = function(model, options) {
  var clone = scout.Menu.parent.prototype.clone.call(this, model, options);
  this._deepCloneProperties(clone, 'childActions', options);
  clone._setChildActions(clone.childActions);
  return clone;
};

/**
 * @override
 */
scout.Menu.prototype.focus = function() {
  var event = new scout.Event({source: this});
  this.trigger('focus', event);
  if (!event.defaultPrevented) {
    return scout.Menu.parent.prototype.focus.call(this);
  }
  return false;
};
