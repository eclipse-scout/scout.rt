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

  this.viewButtons = [];
  this.selected = false;
  this.inBackground = false;
  this.viewTabVisible = true;
  this.defaultIconId = scout.icons.FOLDER;
  this._addWidgetProperties(['selectedButton']);
};
scout.inherits(scout.ViewMenuTab, scout.Widget);

scout.ViewMenuTab.prototype._init = function(model) {
  scout.ViewMenuTab.parent.prototype._init.call(this, model);
  this.dropdown = scout.create('Menu', {
    parent: this,
    iconId: scout.icons.ANGLE_DOWN,
    tabbable: false,
    cssClass: 'view-menu'
  });
  this.dropdown.on('action', this.togglePopup.bind(this));
  this._setViewButtons(this.viewButtons);
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
  if (this.selectedButton) {
    this.selectedButton.remove();
  }
};

scout.ViewMenuTab.prototype._renderProperties = function() {
  scout.ViewMenuTab.parent.prototype._renderProperties.call(this);
  this._updateSelectedButton();
  this._renderInBackground();
};

scout.ViewMenuTab.prototype.setViewButtons = function(viewButtons) {
  this.setProperty('viewButtons', viewButtons);
};

scout.ViewMenuTab.prototype._setViewButtons = function(viewButtons) {
  this._setProperty('viewButtons', viewButtons);
  this.setVisible(this.viewButtons.length > 0);
  var selectedButton = this._findSelectedViewButton();
  if (selectedButton) {
    this.setSelectedButton(selectedButton);
  } else {
    this.setSelectedButton(this.viewButtons[0]);
  }
  this.setSelected(!!selectedButton);
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
  var outlineParent = null;
  if (viewButton.outline) {
    outlineParent = viewButton.outline.parent;
  }
  viewButton = viewButton.clone({
    parent: this,
    displayStyle: 'TAB'
  }, {
    delegateEventsToOriginal: ['acceptInput', 'action'],
    delegateAllPropertiesToClone: true,
    delegateAllPropertiesToOriginal: true,
    excludePropertiesToOriginal: ['selected']
  });
  if (outlineParent) {
    viewButton.outline.setParent(outlineParent);
  }
  // use default icon if outline does not define one.
  viewButton.iconId = viewButton.iconId || this.defaultIconId;
  this._setProperty('selectedButton', viewButton);
};

scout.ViewMenuTab.prototype._renderSelectedButton = function() {
  this._updateSelectedButton();
};

scout.ViewMenuTab.prototype._updateSelectedButton = function() {
  if (!this.selectedButton) {
    return;
  }
  if (this.viewTabVisible) {
    if (!this.selectedButton.rendered) {
      this.selectedButton.render(this.$container);
      this.invalidateLayoutTree();
    }
  } else {
    if (this.selectedButton.rendered) {
      this.selectedButton.remove();
      this.invalidateLayoutTree();
    }
  }
};

scout.ViewMenuTab.prototype.setViewTabVisible = function(viewTabVisible) {
  this.setProperty('viewTabVisible', viewTabVisible);
  if (this.rendered) {
    this._updateSelectedButton();
  }
};

scout.ViewMenuTab.prototype._renderSelected = function() {
  this.$container.select(this.selected);
};

scout.ViewMenuTab.prototype._renderInBackground = function() {
  this.$container.toggleClass('in-background', this.inBackground);
};

scout.ViewMenuTab.prototype._findSelectedViewButton = function() {
  var viewMenu;
  for (var i = 0; i < this.viewButtons.length; i++) {
    viewMenu = this.viewButtons[i];
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
  if (this.popup) {
    this._closePopup();
  } else {
    this._openPopup();
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
    viewMenus: this.viewButtons,
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

scout.ViewMenuTab.prototype.onViewButtonSelected = function() {
  var viewButton = this._findSelectedViewButton();
  if (viewButton) {
    this.setSelectedButton(this._findSelectedViewButton());
  }
  this.setSelected(!!viewButton);
  this._closePopup();
};
