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
scout.ViewButtons = function() {
  scout.ViewButtons.parent.call(this);
  this.viewMenuTab;
  this.viewTabs;
  this._desktopOutlineChangedHandler = this._onDesktopOutlineChanged.bind(this);
  this._viewButtonPropertyChangeHandler = this._onViewButtonPropertyChange.bind(this);
};
scout.inherits(scout.ViewButtons, scout.Widget);

scout.ViewButtons.prototype._init = function(model) {
  scout.ViewButtons.parent.prototype._init.call(this, model);
  this.desktop = this.session.desktop;
};

scout.ViewButtons.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.DesktopHeader.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  // Bound to desktop
  this.desktopKeyStrokeContext = new scout.KeyStrokeContext();
  this.desktopKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
  this.desktopKeyStrokeContext.$bindTarget = this.desktop.$container;
  this.desktopKeyStrokeContext.$scopeTarget = this.$container;
  this.desktopKeyStrokeContext.registerKeyStroke([
    new scout.ViewMenuOpenKeyStroke(this)
  ]);
};

scout.ViewButtons.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('view-buttons');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.ViewButtonsLayout(this));
  this.viewMenuTab = scout.create('ViewMenuTab', {parent: this,
    viewMenus: this._viewButtons('MENU')
  });
  this.viewMenuTab.render(this.$container);

  this.viewTabs = this._viewButtons('TAB');
  this.viewTabs.forEach(function(viewTab, i) {
    viewTab.setParent(this);
    viewTab.render(this.$container);
    if (i === this.viewTabs.length - 1) {
      viewTab.last();
    }
  }, this);
  this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);

  this._onDesktopOutlineChanged();
  this.desktop.viewButtons.forEach(function(viewButton) {
    viewButton.on('propertyChange', this._viewButtonPropertyChangeHandler);
  }, this);
  this.desktop.on('outlineChanged', this._desktopOutlineChangedHandler);
};

scout.ViewButtons.prototype._remove = function() {
  this.desktop.off('outlineChanged', this._desktopOutlineChangedHandler);
  this.desktop.viewButtons.forEach(function(viewButton) {
    viewButton.off('selected', this._viewButtonPropertyChangeHandler);
  }, this);
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
  scout.ViewButtons.parent.prototype._remove.call(this);
};

scout.ViewButtons.prototype._viewButtons = function(displayStyle) {
  var viewButtons = [];
  this.desktop.viewButtons.forEach(function(viewButton) {
    if (displayStyle === undefined ||
      displayStyle === viewButton.displayStyle) {
      viewButtons.push(viewButton);
    }
  });
  return viewButtons;
};

scout.ViewButtons.prototype.doViewMenuAction = function() {
  this.viewMenuTab.togglePopup();
};

scout.ViewButtons.prototype.sendToBack = function() {
  this.viewMenuTab.sendToBack();
  this.viewTabs.forEach(function(button) {
    button.sendToBack();
  }, this);
};

scout.ViewButtons.prototype.bringToFront = function() {
  this.viewMenuTab.bringToFront();
  this.viewTabs.forEach(function(button) {
    button.bringToFront();
  }, this);
};

/**
 * This method updates the state of the view-menu-tab and the selected state of outline-view-buttons.
 * This method must also work in offline mode.
 */
scout.ViewButtons.prototype._onDesktopOutlineChanged = function(event) {
  var outline = this.desktop.outline;
  this._viewButtons().forEach(function(viewTab) {
    if (viewTab instanceof scout.OutlineViewButton) {
      viewTab.onOutlineChanged(outline);
    }
  });
};

scout.ViewButtons.prototype._onViewButtonSelected = function(event) {
  // Deselect other togglable view buttons
  this.desktop.viewButtons.forEach(function(viewButton) {
    if (viewButton !== event.source && viewButton.isToggleAction()) {
      viewButton.setSelected(false);
    }
  }, this);

  // Inform viewMenu tab about new selection
  this.viewMenuTab.onViewButtonSelected();
};

scout.ViewButtons.prototype._onViewButtonPropertyChange = function(event) {
  if (event.changedProperties.indexOf('selected') !== -1) {
    this._onViewButtonSelected(event);
  }
};
