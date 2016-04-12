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
/**
 * Shows a list of view buttons with displayStyle=MENU
 * and shows the title of the active outline, if the outline is one
 * of the outline-view-buttons contained in the menu.
 */
scout.ViewMenuTab = function() {
  scout.ViewMenuTab.parent.call(this);
  this.$container;
  this.$arrowIcon; // small "arrow down" icon at the right side of the icon

  this.outlineViewButton = null;
  this.selected = false;
  this.iconId;
  this.inBackground = false;

  this.defaultIconId = scout.icons.OUTLINE;
  this._addEventSupport();
};
scout.inherits(scout.ViewMenuTab, scout.Widget);

scout.ViewMenuTab.prototype._init = function(model) {
  scout.ViewButtons.parent.prototype._init.call(this, model);
  this.viewMenus = model.viewMenus;
  this.viewMenus.forEach(function(viewMenu) {
    viewMenu.setParent(this);
  }, this);
  this._update();
};

/**
 * 1. look for a selected outline-view-button
 * 2. look for any outline-view-button
 * 3. in rare cases there will be no outline-view-button at all
 */
scout.ViewMenuTab.prototype._update = function() {
  var ovb = this._findOutlineViewButton(true);
  if (ovb) {
    this.selected = true;
  } else {
    ovb = this._findOutlineViewButton(false);
    this.selected = false;
  }
  this.outlineViewButton = ovb;

  // Use iconId from outline view button (defaultIconId as fallback)
  this.iconId = (this.outlineViewButton && this.outlineViewButton.iconId) || this.defaultIconId;
};

scout.ViewMenuTab.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('view-button-tab')
    .unfocusable()
    .on('mousedown', this.togglePopup.bind(this));
  this.$arrowIcon = this.$container
    .appendSpan('arrow-icon')
    .on('mousedown', this.togglePopup.bind(this));
};

scout.ViewMenuTab.prototype._renderProperties = function() {
  this._renderIconId();
  this._renderSelected();
  this._renderInBackground();
};

scout.ViewMenuTab.prototype._renderSelected = function() {
  this.$container.select(this.selected);
  this._updateArrowIconVisibility();
};

scout.ViewMenuTab.prototype._renderIconId = function() {
  this.$container.icon(this.iconId);
};

scout.ViewMenuTab.prototype._renderInBackground = function() {
  this.$container.toggleClass('in-background', this.inBackground);
};

scout.ViewMenuTab.prototype._updateArrowIconVisibility = function() {
  this.$arrowIcon.toggleClass('hidden', !this.selected || this.inBackground);
};

/**
 * @param onlySelected when false -> function returns the first viewMenu which is an OutlineViewButton
 *                     when true  -> function returns the first viewMenu which is an OutlineViewButton AND also selected
 */
scout.ViewMenuTab.prototype._findOutlineViewButton = function(onlySelected) {
  var viewMenu;
  for (var i = 0; i < this.viewMenus.length; i++) {
    viewMenu = this.viewMenus[i];
    if (viewMenu instanceof scout.OutlineViewButton) {
      if (!onlySelected ||
        onlySelected && viewMenu.selected) {
        return viewMenu;
      }
    }
  }
  return null;
};

/**
 * Toggles the 'view menu popup', or brings the outline content to the front if in background.
 */
scout.ViewMenuTab.prototype.togglePopup = function() {
  if (this.selected) {
    if (this.inBackground) {
      this.session.desktop.bringOutlineToFront(this.outlineViewButton.outline);
    } else {
      // Open or close the popup.
      if (this.popup) {
        this._closePopup();
      } else {
        this._openPopup();
      }
      return false; // menu won't open if we didn't abort the mousedown-event
    }
  } else {
    this.outlineViewButton.doAction();
  }
};

scout.ViewMenuTab.prototype._openPopup = function() {
  if (this.popup) {
    // already open
    return;
  }
  var naviBounds = scout.graphics.bounds(this.$container.parent(), true);
  this.popup = scout.create('ViewMenuPopup', {
    parent: this,
    $tab: this.$container,
    viewMenus: this._popupViewMenus(),
    naviBounds: naviBounds
  });
  this.popup.headText = this.text;
  this.popup.open();
  this.$container.addClass('popup-open');
  this.popup.on('remove', function(event) {
    this.$container.removeClass('popup-open');
    this.popup = null;
  }.bind(this));
};

scout.ViewMenuTab.prototype._closePopup = function() {
  if (this.popup) {
    this.popup.close();
  }
};

scout.ViewMenuTab.prototype.setSelected = function(selected) {
  if (selected === this.selected) {
    return;
  }
  this._setProperty('selected', selected);
  if (this.rendered) {
    this._renderSelected();
  }
};

scout.ViewMenuTab.prototype.setIconId = function(iconId) {
  if (iconId === this.iconId) {
    return;
  }
  this._setProperty('iconId', iconId);
  if (this.rendered) {
    this._renderIconId();
  }
};

/**
 * An OutlineViewButton for a null-outline shouldn't be added to the menus
 * displayed in the popup-menu. We recognize the null-outline be checking
 * the 'visibleInMenu' property.
 */
scout.ViewMenuTab.prototype._popupViewMenus = function() {
  var popupMenus = [];
  this.viewMenus.forEach(function(viewMenu) {
    if (scout.nvl(viewMenu.visibleInMenu, true)) {
      popupMenus.push(viewMenu);
    }
  });
  return popupMenus;
};

scout.ViewMenuTab.prototype.onOutlineChanged = function(outline) {
  var i, viewMenu, ovb = null;
  for (i = 0; i < this.viewMenus.length; i++) {
    viewMenu = this.viewMenus[i];
    if (viewMenu instanceof scout.OutlineViewButton && viewMenu.outline === outline) {
      ovb = viewMenu;
      break;
    }
  }

  if (ovb) {
    this.outlineViewButton = ovb;
    this.setIconId(ovb.iconId || this.defaultIconId);
    this.setSelected(true);
  } else {
    this.setSelected(false);
  }
  this._closePopup();
};

scout.ViewMenuTab.prototype.sendToBack = function() {
  this.inBackground = true;
  this._renderInBackground();
  this._renderSelected();
  this._closePopup();
};

scout.ViewMenuTab.prototype.bringToFront = function() {
  this.inBackground = false;
  this._renderInBackground();
  this._renderSelected();
};
