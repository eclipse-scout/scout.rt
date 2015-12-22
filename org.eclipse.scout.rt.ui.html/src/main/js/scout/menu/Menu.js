/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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
  this._addAdapterProperties('childActions');
  this._addModelProperties('overflow');
  this.popup;
  this.excludedByFilter = false;

  this.subMenuExpanded = false;

  /**
   * This property is set if this is a subMenu. The property is set when this submenu is rendered.
   */
  this.parentMenu;

  /**
   * This property is true when the menu instance was moved into a overflow-menu
   * when there's not enough space on the screen (see MenuBarLayout.js). When set
   * to true, button style menus must be displayed as regular menus.
   */
  this.overflow = false;
};
scout.inherits(scout.Menu, scout.Action);

/**
 * @override ModelAdapter
 */
scout.Menu.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.Menu.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  keyStrokeContext.registerKeyStroke(new scout.MenuExecKeyStroke(this));
};

scout.Menu.prototype._render = function($parent) {
  if (this.separator) {
    this._renderSeparator($parent);
  } else {
    this._renderItem($parent);
  }
  this.$container.unfocusable();
};

scout.Menu.prototype._remove = function() {
  scout.Menu.parent.prototype._remove.call(this);
  this.$submenuIcon = null;
  this.$subMenuBody = null;
};

scout.Menu.prototype._renderSeparator = function($parent) {
  this.$container = $parent.appendDiv('menu-separator');
};

scout.Menu.prototype._renderItem = function($parent) {
  this.$container = $parent.appendDiv('menu-item');
  if (this._customCssClasses) {
    this.$container.addClass(this._customCssClasses);
  }

  var mouseEventHandler = this._onMouseEvent.bind(this);
  this.$container
    .on('mousedown', mouseEventHandler)
    .on('contextmenu', mouseEventHandler)
    .on('click', mouseEventHandler);
  if (this.childActions.length > 0 && this.text) {
    this.$submenuIcon = this.$container.appendSpan('submenu-icon');
  }

  // when menus with button style are displayed in a overflow-menu,
  // render as regular menu, ignore button styles.
  if (scout.Action.ActionStyle.BUTTON === this.actionStyle && !this.overflow) {
    this.$container.addClass('menu-button');
  }
};

scout.Menu.prototype._renderSelected = function() {
  if (!this._doActionTogglesPopup()) {
    scout.Menu.parent.prototype._renderSelected.call(this);
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
  this.subMenuExpanded = true;
  if (this.parent instanceof scout.ContextMenuPopup) {
    this.parent.renderSubMenuItems(parentMenu, menus, true);
  } else if (this.parent instanceof scout.Menu) {
    this.parent._renderSubMenuItems(parentMenu, menus);
  }
};

scout.Menu.prototype._doActionTogglesSubMenu = function() {
  return this.parent instanceof scout.ContextMenuPopup || this.parent instanceof scout.Menu;
};

scout.Menu.prototype._getSubMenuLevel = function() {
  if (this.parent instanceof scout.ContextMenuPopup) {
    return 0;
  }
  return scout.Menu.parent.prototype._getSubMenuLevel.call(this) + 1;
};

scout.Menu.prototype._onMouseEvent = function(event) {
  if (event.which !== 1) {
    return; // Other button than left mouse button --> nop
  }

  // If menu has childActions, a popup should be rendered on click. To create
  // the impression of a faster UI, open the popup already on 'mousedown', not
  // on 'click'. All other actions are handled on 'click'.
  if (event.type === 'mousedown' && this._doActionTogglesPopup()) {
    this.doAction(event);
  } else if ((event.type === 'click' || event.type === 'contextmenu') && !this._doActionTogglesPopup()) {
    this.doAction(event);
  }
};

/**
 * May be overridden if the criteria to open a popup differs
 */
scout.Menu.prototype._doActionTogglesPopup = function() {
  return this.childActions.length > 0;
};

scout.Menu.prototype._renderText = function(text) {
  scout.Menu.parent.prototype._renderText.call(this, text);
  // Ensure submenu-icon is the last element in the DOM
  if (this.$submenuIcon) {
    this.$submenuIcon.appendTo(this.$container);
  }
  this._updateIconAndTextStyle();
};

scout.Menu.prototype._renderIconId = function() {
  scout.Menu.parent.prototype._renderIconId.call(this);
  this._updateIconAndTextStyle();
};

scout.Menu.prototype.isTabTarget = function() {
  return this.enabled && this.visible && (this.actionStyle === scout.Action.ActionStyle.BUTTON || !this.separator);
};

scout.Menu.prototype._updateIconAndTextStyle = function() {
  var hasTextAndIcon = !!(scout.strings.hasText(this.text) && this.iconId);
  this.$container.toggleClass('menu-textandicon', hasTextAndIcon);
};

scout.Menu.prototype._closePopup = function() {
  if (this.popup) {
    this.popup.close();
  }
};

/**
 * @param event
 *          UI event that triggered the popup (e.g. 'mouse clicked'). This argument
 *          is passed to the MenuBarPopup  as 'ignoreEvent'. It prevents the popup
 *          from being closed again by the same event that bubbled to other elements.
 */
scout.Menu.prototype._openPopup = function(event) {
  if (this.popup) {
    // already open
    return;
  }
  this.popup = this._createPopup(event);
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
};

scout.Menu.prototype._createPopup = function(event) {
  var options = {
    parent: this,
    menu: this,
    ignoreEvent: event,
    openingDirectionX: this.popupOpeningDirectionX,
    openingDirectionY: this.popupOpeningDirectionY
  };

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
