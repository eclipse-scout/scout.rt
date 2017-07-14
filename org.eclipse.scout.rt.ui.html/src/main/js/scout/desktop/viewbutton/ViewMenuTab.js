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
 * and shows the title of the active view button, if the view button is one
 * of the view buttons contained in the menu.
 */
scout.ViewMenuTab = function() {
  scout.ViewMenuTab.parent.call(this);
  this.$container;
  this.$arrowIcon; // small "arrow down" icon at the right side of the icon

  this.viewButton = null;
  this.viewMenus = [];
  this.selected = false;
  this.iconId;
  this.inBackground = false;
  this.visible = true;

  this.defaultIconId = scout.icons.OUTLINE;
  this._viewMenuPropertyChangeHandler = this._onViewMenuPropertyChange.bind(this);
  this._addWidgetProperties('viewMenus');
};
scout.inherits(scout.ViewMenuTab, scout.Widget);

scout.ViewMenuTab.prototype._init = function(model) {
  scout.ViewMenuTab.parent.prototype._init.call(this, model);
  this.viewMenus.forEach(function(viewMenu) {
    viewMenu.on('propertyChange', this._viewMenuPropertyChangeHandler);
  }, this);
  this._update();
  this.updateVisibility();
};

scout.ViewMenuTab.prototype._initKeyStrokeContext = function() {
  scout.ViewMenuTab.parent.prototype._initKeyStrokeContext.call(this);

  // Bound to desktop
  this.desktopKeyStrokeContext = new scout.KeyStrokeContext();
  this.desktopKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
  this.desktopKeyStrokeContext.$bindTarget = this.session.desktop.$container;
  this.desktopKeyStrokeContext.$scopeTarget = this.session.desktop.$container;
  this.desktopKeyStrokeContext.registerKeyStroke([
    new scout.ViewMenuOpenKeyStroke(this)
  ]);
};

/**
 * 1. look for a selected view-button
 * 2. look for any view-button
 * 3. if there is no view-button menu should not be visible
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

scout.ViewMenuTab.prototype._render = function() {
  this.$container = this.$parent.appendDiv('view-button-tab')
    .unfocusable()
    .on('mousedown', this.togglePopup.bind(this));
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);

  this.$arrowIcon = this.$container
    .appendSpan('arrow-icon')
    .on('mousedown', this.togglePopup.bind(this));

  this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
};

scout.ViewMenuTab.prototype._remove = function() {
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
  scout.ViewMenuTab.parent.prototype._remove.call(this);
};

scout.ViewMenuTab.prototype._renderProperties = function() {
  scout.ViewMenuTab.parent.prototype._renderProperties.call(this);
  this._renderIconId();
  this._renderSelected();
  this._renderInBackground();
};

/**
 * @override
 */
scout.ViewMenuTab.prototype._renderVisible = function() {
  this.$container.setVisible(this.visible);
  this.invalidateLayoutTree();
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
    viewMenus: this.viewMenus,
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
  this.setProperty('selected', selected);
};

scout.ViewMenuTab.prototype.setIconId = function(iconId) {
  this.setProperty('iconId', iconId);
};

scout.ViewMenuTab.prototype.updateVisibility = function() {
  this.setVisible(this.viewMenus.some(function(viewMenu) {
    return viewMenu.visible;
  }));
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

scout.ViewMenuTab.prototype._onViewMenuPropertyChange = function(event) {
  if (event.propertyName === 'visible') {
    this.updateVisibility();
  }
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
