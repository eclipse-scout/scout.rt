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
scout.ViewButtonBox = function() {
  scout.ViewButtonBox.parent.call(this);
  this.viewMenuTab;
  this.viewTabs;
  this.viewButtons = [];
  this._desktopOutlineChangedHandler = this._onDesktopOutlineChanged.bind(this);
  this._viewButtonPropertyChangeHandler = this._onViewButtonPropertyChange.bind(this);
  this._addAdapterProperties(['viewButtons']);
};
scout.inherits(scout.ViewButtonBox, scout.Widget);

scout.ViewButtonBox.prototype._init = function(model) {
  scout.ViewButtonBox.parent.prototype._init.call(this, model);
  this.desktop = this.session.desktop;
};

scout.ViewButtonBox.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('view-button-box');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.ViewButtonBoxLayout(this));
  this.viewMenuTab = scout.create('ViewMenuTab', {parent: this,
    viewMenus: this._viewButtons('MENU')
  });
  this.viewMenuTab.render(this.$container);

  this.viewTabs = this._viewButtons('TAB');
  this.viewTabs.forEach(function(viewTab, i) {
    viewTab.render(this.$container);
    if (i === this.viewTabs.length - 1) {
      viewTab.last();
    }
  }, this);

  this._onDesktopOutlineChanged();
  this.viewButtons.forEach(function(viewButton) {
    viewButton.on('propertyChange', this._viewButtonPropertyChangeHandler);
  }, this);
  this.desktop.on('outlineChanged', this._desktopOutlineChangedHandler);
};

scout.ViewButtonBox.prototype._remove = function() {
  this.desktop.off('outlineChanged', this._desktopOutlineChangedHandler);
  this.viewButtons.forEach(function(viewButton) {
    viewButton.off('selected', this._viewButtonPropertyChangeHandler);
  }, this);

  scout.ViewButtonBox.parent.prototype._remove.call(this);
};

scout.ViewButtonBox.prototype._viewButtons = function(displayStyle) {
  var viewButtons = [];
  this.viewButtons.forEach(function(viewButton) {
    if (displayStyle === undefined ||
      displayStyle === viewButton.displayStyle) {
      viewButtons.push(viewButton);
    }
  });
  return viewButtons;
};

scout.ViewButtonBox.prototype.sendToBack = function() {
  this.viewMenuTab.sendToBack();
  this.viewTabs.forEach(function(button) {
    button.sendToBack();
  }, this);
};

scout.ViewButtonBox.prototype.bringToFront = function() {
  this.viewMenuTab.bringToFront();
  this.viewTabs.forEach(function(button) {
    button.bringToFront();
  }, this);
};

/**
 * This method updates the state of the view-menu-tab and the selected state of outline-view-button-box.
 * This method must also work in offline mode.
 */
scout.ViewButtonBox.prototype._onDesktopOutlineChanged = function(event) {
  var outline = this.desktop.outline;
  this._viewButtons().forEach(function(viewTab) {
    if (viewTab instanceof scout.OutlineViewButton) {
      viewTab.onOutlineChanged(outline);
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
  if (event.name === 'selected') {
    this._onViewButtonSelected(event);
  }
};
