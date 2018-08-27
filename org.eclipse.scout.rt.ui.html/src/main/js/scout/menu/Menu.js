/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
   * This property is set if this is a subMenu. The property is set when this submenu is rendered.
   */
  this.parentMenu = null;
  this.popup = null;
  this.preventDoubleClick = false;
  this.stackable = true;
  this.separator = false;

  this._addCloneProperties(['defaultMenu', 'menuTypes', 'overflow', 'stackable', 'separator']);
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

scout.Menu.prototype._init = function(options) {
  scout.Menu.parent.prototype._init.call(this, options);
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
  } else if (this.parent instanceof scout.Menu) {
    this.parent._renderSubMenuItems(parentMenu, menus);
  }
};

scout.Menu.prototype._doActionTogglesSubMenu = function() {
  return this.childActions.length > 0 && (this.parent instanceof scout.ContextMenuPopup || this.parent instanceof scout.Menu);
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
  var shouldBeVisible = this.childActions.length > 0 && this.text;

  if (shouldBeVisible) {
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
  return this.enabled && this.visible && !this.overflown && (this.isButton() || !this.separator);
};

scout.Menu.prototype._updateIconAndTextStyle = function() {
  var hasText = scout.strings.hasText(this.text) && this.textVisible;
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
    ignoreEvent: event,
    openingDirectionX: this.popupOpeningDirectionX,
    openingDirectionY: this.popupOpeningDirectionY
  };

  if (this.parent.menuFilter) {
    options.menuFilter = function(menus, destination, onlyVisible, enableDisableKeyStroke) {
      return this.parent.menuFilter(menus, scout.MenuDestinations.MENU_BAR, onlyVisible, enableDisableKeyStroke);
    }.bind(this);
  }
  if (this.parent._filterMenusHandler) {
    options.menuFilter = function(menus, destination, onlyVisible, enableDisableKeyStroke) {
      return this.parent._filterMenusHandler(menus, scout.MenuDestinations.MENU_BAR, onlyVisible, enableDisableKeyStroke);
    }.bind(this);
  }
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

scout.Menu.prototype.setChildActions = function(childActions) {
  this.setProperty('childActions', childActions);
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

scout.Menu.prototype.clone = function(model, options) {
  var clone = scout.Menu.parent.prototype.clone.call(this, model, options);
  this._deepCloneProperties(clone, 'childActions', options);
  return clone;
};
