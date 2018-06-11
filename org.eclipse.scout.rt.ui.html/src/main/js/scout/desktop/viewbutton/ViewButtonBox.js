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
scout.ViewButtonBox = function() {
  scout.ViewButtonBox.parent.call(this);
  this.viewMenuTab;
  this.viewButtons = [];
  this.menuButtons = [];
  this.tabButtons = [];
  this._desktopOutlineChangeHandler = this._onDesktopOutlineChange.bind(this);
  this._viewButtonPropertyChangeHandler = this._onViewButtonPropertyChange.bind(this);
  this._addWidgetProperties(['tabButtons']);
};
scout.inherits(scout.ViewButtonBox, scout.Widget);

scout.ViewButtonBox.prototype._init = function(model) {
  scout.ViewButtonBox.parent.prototype._init.call(this, model);
  this.desktop = this.session.desktop;
  this.viewMenuTab = scout.create('ViewMenuTab', {
    parent: this
  });
  this._setViewButtons(this.viewButtons);
  this.desktop.on('outlineChange', this._desktopOutlineChangeHandler);
};

scout.ViewButtonBox.prototype._render = function() {
  this.$container = this.$parent.appendDiv('view-button-box');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.ViewButtonBoxLayout(this));

  this.viewMenuTab.render();
  this._onDesktopOutlineChange();
};

scout.ViewButtonBox.prototype._renderProperties = function() {
  scout.ViewButtonBox.parent.prototype._renderProperties.call(this);
  this._renderTabButtons();
};

scout.ViewButtonBox.prototype._remove = function() {
  this.desktop.off('outlineChange', this._desktopOutlineChangeHandler);
  this.viewButtons.forEach(function(viewButton) {
    viewButton.off('selected', this._viewButtonPropertyChangeHandler);
  }, this);

  scout.ViewButtonBox.parent.prototype._remove.call(this);
};

scout.ViewButtonBox.prototype.setMenuTabVisible = function(menuTabVisible) {
  this.viewMenuTab.setViewTabVisible(menuTabVisible);
  this.invalidateLayoutTree();
};

scout.ViewButtonBox.prototype.setViewButtons = function(viewButtons) {
  this.setProperty('viewButtons', viewButtons);
};

scout.ViewButtonBox.prototype._setViewButtons = function(viewButtons) {
  if (this.viewButtons) {
    this.viewButtons.forEach(function(viewButton) {
      viewButton.off('propertyChange', this._viewButtonPropertyChangeHandler);
    }, this);
  }
  this._setProperty('viewButtons', viewButtons);
  this.viewButtons.forEach(function(viewButton) {
    viewButton.on('propertyChange', this._viewButtonPropertyChangeHandler);
  }, this);
  this._updateViewButtons();
};

scout.ViewButtonBox.prototype.setTabButtons = function(tabButtons) {
  this.setProperty('tabButtons', tabButtons);
};

scout.ViewButtonBox.prototype._renderTabButtons = function() {
  this.tabButtons.forEach(function(viewTab, i) {
    viewTab.renderAsTab();
    viewTab.tab();
    if (i === this.tabButtons.length - 1) {
      viewTab.last();
    }
  }, this);
};

scout.ViewButtonBox.prototype._updateViewButtons = function() {
  var viewButtons = this.viewButtons.filter(function(b) {
      return b.visible;
    }),
    menuButtons = viewButtons.filter(function(b) {
      return b.displayStyle === 'MENU';
    }),
    tabButtons = null;
  // render as tab if length is < 1
  if (menuButtons.length > 1) {
    tabButtons = viewButtons.filter(function(b) {
      return b.displayStyle === 'TAB';
    });
  } else {
    // all visible view buttons are rendered as tab
    tabButtons = viewButtons;
    menuButtons = [];
  }

  this._setMenuButtons(menuButtons);

  this.setTabButtons(tabButtons);
  this._updateVisibility();
};

scout.ViewButtonBox.prototype._updateVisibility = function(menuButtons) {
  this.setVisible((this.tabButtons.length + this.menuButtons.length) > 1);
};

scout.ViewButtonBox.prototype.setMenuButtons = function(menuButtons) {
  this.setProperty('menuButtons', menuButtons);
  this._updateVisibility();
};

scout.ViewButtonBox.prototype._setMenuButtons = function(menuButtons) {
  this._setProperty('menuButtons', menuButtons);
  this.viewMenuTab.setViewButtons(this.menuButtons);
};

scout.ViewButtonBox.prototype.sendToBack = function() {
  this.viewMenuTab.sendToBack();
};

scout.ViewButtonBox.prototype.bringToFront = function() {
  this.viewMenuTab.bringToFront();
};

/**
 * This method updates the state of the view-menu-tab and the selected state of outline-view-button-box.
 * This method must also work in offline mode.
 */
scout.ViewButtonBox.prototype._onDesktopOutlineChange = function(event) {
  var outline = this.desktop.outline;
  this.viewButtons.forEach(function(viewTab) {
    if (viewTab instanceof scout.OutlineViewButton) {
      viewTab.onOutlineChange(outline);
    }
  });
};

scout.ViewButtonBox.prototype._onViewButtonSelected = function(event) {
  // Deselect other togglable view buttons
  this.viewButtons.forEach(function(viewButton) {
    if (viewButton !== event.source && viewButton.isToggleAction()) {
      viewButton.setSelected(false);
    }
  }, this);

  // Inform viewMenu tab about new selection
  this.viewMenuTab.onViewButtonSelected();
};

scout.ViewButtonBox.prototype._onViewButtonPropertyChange = function(event) {
  if (event.propertyName === 'selected') {
    this._onViewButtonSelected(event);
  } else if (event.propertyName === 'visible' ||
    event.propertyName === 'displayStyle') {
    this._updateViewButtons();
  }
};
