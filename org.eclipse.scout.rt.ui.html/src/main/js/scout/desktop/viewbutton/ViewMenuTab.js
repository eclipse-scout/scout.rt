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
/**
 * Shows a list of view buttons with displayStyle=MENU
 * and shows the title of the active view button, if the view button is one
 * of the view buttons contained in the menu.
 */
scout.ViewMenuTab = function() {
  scout.ViewMenuTab.parent.call(this);

  this.viewMenus = [];
  this.selected = false;
  this.inBackground = false;
  this.defaultIconId = scout.icons.FOLDER_BOLD;

  this._viewMenuPropertyChangeHandler = this._onViewMenuPropertyChange.bind(this);
  this._addWidgetProperties(['viewMenus', 'selectedButton']);
};
scout.inherits(scout.ViewMenuTab, scout.Widget);

scout.ViewMenuTab.prototype._init = function(model) {
  scout.ViewMenuTab.parent.prototype._init.call(this, model);
  var selectedButton = this._findSelectedViewButton();

  this.dropdown = scout.create('Menu', {
    parent: this,
    iconId: scout.icons.ANGLE_DOWN_BOLD,
    tabbable: false,
    cssClass: 'view-menu'
  });
  this.dropdown.on('action', this.togglePopup.bind(this));

  this.viewMenus.forEach(function(viewMenu) {
    viewMenu.on('propertyChange', this._viewMenuPropertyChangeHandler);
  }, this);
  if (selectedButton) {
    this.setSelectedButton(selectedButton);
  } else {
    this.setSelectedButton(this.viewMenus[0]);
  }
  this.setSelected(!!selectedButton);
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

scout.ViewMenuTab.prototype._render = function() {
  this.$container = this.$parent.appendDiv('view-tab');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.dropdown.render(this.$container);
  this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
};

scout.ViewMenuTab.prototype._remove = function() {
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
  scout.ViewMenuTab.parent.prototype._remove.call(this);
};

scout.ViewMenuTab.prototype._renderProperties = function() {
  scout.ViewMenuTab.parent.prototype._renderProperties.call(this);
  this._renderSelectedButton();
  this._renderInBackground();
};

scout.ViewMenuTab.prototype.setSelectedButton = function(viewButton) {
  if (this.selectedButton && this.selectedButton.cloneOf === viewButton) {
    return;
  }
  if (viewButton) {
    this.setProperty('selectedButton', viewButton);
  }
};

scout.ViewMenuTab.prototype._setSelectedButton = function(viewButton) {
  viewButton = viewButton.clone({
    parent: this,
    displayStyle: 'TAB'
  }, {
    delegateEventsToOriginal: ['acceptInput', 'action'],
    delegateAllPropertiesToClone: true,
    delegateAllPropertiesToOriginal: true,
    excludePropertiesToOriginal: ['selected']
  });
  viewButton.iconId = viewButton.iconId || this.defaultIconId;
  this._setProperty('selectedButton', viewButton);
};

scout.ViewMenuTab.prototype._renderSelectedButton = function() {
  if (this.selectedButton) {
    this.selectedButton.render(this.$container);
  }
  this.htmlComp.invalidateLayoutTree();
};

scout.ViewMenuTab.prototype._renderSelected = function() {
  this.$container.select(this.selected);
};

scout.ViewMenuTab.prototype._renderInBackground = function() {
  this.$container.toggleClass('in-background', this.inBackground);
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
  if (this.inBackground) {
    this.session.desktop.bringOutlineToFront(this.selectedButton.outline);
  } else {
    if (this.popup) {
      this._closePopup();
    } else {
      this._openPopup();
    }
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
    $tab: this.dropdown.$container,
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
    this.setSelectedButton(this._findSelectedViewButton());
  }
  this.setSelected(!!viewButton);
  this._closePopup();
};
