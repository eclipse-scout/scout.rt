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

  this.viewButton = null;
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
 * 1. look for a selected view-button
 * 2. look for any view-button
 * 3. in rare cases there will be no view-button at all
 */
scout.ViewMenuTab.prototype._update = function() {
  var viewButton = this._findSelectedViewButton();
  if (viewButton) {
    this.selected = true;
  } else {
    viewButton = this.viewMenus[0];
    this.selected = false;
  }
  this.viewButton = viewButton;

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

scout.ViewMenuTab.prototype._findSelectedViewButton = function() {
  var viewMenu;
  for (var i = 0; i < this.viewMenus.length; i++) {
    viewMenu = this.viewMenus[i];
    if (viewMenu.selected) {
      return viewMenu;
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
      this.session.desktop.bringOutlineToFront(this.viewButton.outline);
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
    this.viewButton.doAction();
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
  // The class needs to be added to the container before the popup gets opened so that the modified style may be copied to the head.
  this.$container.addClass('popup-open');
  this.popup.headText = this.text;
  this.popup.open();
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

scout.ViewMenuTab.prototype.onViewButtonSelected = function() {
  var viewButton = this._findSelectedViewButton();
  if (viewButton) {
    // only change if a new viewMenu was selected, otherwise keep old viewButton in order to reselect it when the viewMenu gets selected again
    this.viewButton = viewButton;
    // Use iconId from selected view button or defaultIconId as fallback
    this.setIconId(this.viewButton.iconId || this.defaultIconId);
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
