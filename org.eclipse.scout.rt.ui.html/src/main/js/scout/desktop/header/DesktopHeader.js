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
scout.DesktopHeader = function() {
  scout.DesktopHeader.parent.call(this);
  this._desktopPropertyChangeHandler = this._onDesktopPropertyChange.bind(this);
  this._desktopAnimationEndHandler = this._onDesktopAnimationEnd.bind(this);
  this._outlineContentMenuBarPropertyChangeHandler = this._onOutlineContentMenuBarPropertyChange.bind(this);
};
scout.inherits(scout.DesktopHeader, scout.Widget);

scout.DesktopHeader.prototype._init = function(model) {
  scout.DesktopHeader.parent.prototype._init.call(this, model);
  this.desktop = this.session.desktop;
  this.toolBarVisible = scout.nvl(model.toolBarVisible, true);
  this.viewButtonsVisible = scout.nvl(model.viewButtonsVisible, false);
  this.updateViewButtonsVisibility();
};

scout.DesktopHeader.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('desktop-header');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.DesktopHeaderLayout(this));
  this._renderViewButtonsVisible();
  this._renderViewTabs();
  this._renderToolBarVisible();
  this._renderLogoUrl();
  this.desktop.on('propertyChange', this._desktopPropertyChangeHandler);
  this.desktop.on('animationEnd', this._desktopAnimationEndHandler);
  if (this.desktop.bench) {
    this.outlineContent = this.desktop.bench.outlineContent;
  }
  this._attachOutlineContentMenuBarHandler();
};

scout.DesktopHeader.prototype._remove = function() {
  this.desktop.off('propertyChange', this._desktopPropertyChangeHandler);
  this.desktop.off('animationEnd', this._desktopAnimationEndHandler);
  this._detachOutlineContentMenuBarHandler();
  this.outlineContent = null;
  scout.DesktopHeader.parent.prototype._remove.call(this);
};

scout.DesktopHeader.prototype._renderViewTabs = function() {
  if (this.viewTabs) {
    return;
  }
  this.viewTabs = scout.create('DesktopViewTabs', {
    parent: this
  });
  this.viewTabs.render(this.$container);
};

scout.DesktopHeader.prototype._renderToolBar = function() {
  if (this.toolBar) {
    return;
  }
  this.toolBar = scout.create('DesktopToolBox', {
    parent: this,
    menus: this.desktop.menus
  });
  this.toolBar.render(this.$container);
};

scout.DesktopHeader.prototype._removeToolBar = function() {
  if (!this.toolBar) {
    return;
  }
  this.toolBar.remove();
  this.toolBar = null;
};

scout.DesktopHeader.prototype._renderLogoUrl = function() {
  if (this.logoUrl) {
    this._renderLogo();
  } else {
    this._removeLogo();
  }
  this.invalidateLayoutTree();
};

scout.DesktopHeader.prototype._renderLogo = function() {
  if (!this.logo) {
    this.logo = scout.create('DesktopLogo', {
      parent: this,
      url: this.logoUrl
    });
    this.logo.render(this.$container);
  } else {
    this.logo.setUrl(this.logoUrl);
  }
};

scout.DesktopHeader.prototype._removeLogo = function() {
  if (!this.logo) {
    return;
  }
  this.logo.remove();
  this.logo = null;
};

scout.DesktopHeader.prototype._renderToolBarVisible = function() {
  if (this.toolBarVisible) {
    this._renderToolBar();
  } else {
    this._removeToolBar();
  }
  this.invalidateLayoutTree();
};

scout.DesktopHeader.prototype._renderViewButtons = function() {
  if (this.viewButtons) {
    return;
  }
  this.viewButtons = scout.create('ViewButtons', {
    parent: this
  });
  this.viewButtons.render(this.$container);
  this.viewButtons.$container.prependTo(this.$container);
  this.updateViewButtonStyling();
};

scout.DesktopHeader.prototype._removeViewButtons = function() {
  if (!this.viewButtons) {
    return;
  }
  this.viewButtons.remove();
  this.viewButtons = null;
};

scout.DesktopHeader.prototype._renderViewButtonsVisible = function() {
  if (this.viewButtonsVisible) {
    this._renderViewButtons();
  } else {
    this._removeViewButtons();
  }
  this.invalidateLayoutTree();
};

scout.DesktopHeader.prototype.sendToBack = function() {
  if (this.viewButtons) {
    this.viewButtons.sendToBack();
  }
};

scout.DesktopHeader.prototype.bringToFront = function() {
  if (this.viewButtons) {
    this.viewButtons.bringToFront();
  }
};

scout.DesktopHeader.prototype.setLogoUrl = function(logoUrl) {
  if (this.logoUrl === logoUrl) {
    return;
  }
  this.logoUrl = logoUrl;
  if (this.rendered) {
    this._renderLogoUrl();
  }
};

scout.DesktopHeader.prototype.setToolBarVisible = function(visible) {
  if (this.toolBarVisible === visible) {
    return;
  }
  this.toolBarVisible = visible;
  if (this.rendered) {
    this._renderToolBarVisible();
  }
};

scout.DesktopHeader.prototype.setViewButtonsVisible = function(visible) {
  if (this.viewButtonsVisible === visible) {
    return;
  }
  this.viewButtonsVisible = visible;
  if (this.rendered) {
    this._renderViewButtonsVisible();
  }
};

scout.DesktopHeader.prototype.updateViewButtonsVisibility = function() {
  // View buttons are visible in the header if the navigation is not visible
  // With displayStyle is set to compact, the view buttons should never be visible in the header
  this.setViewButtonsVisible(!this.desktop.navigationVisible && this.desktop.displayStyle !== scout.Desktop.DisplayStyle.COMPACT);
};

scout.DesktopHeader.prototype._attachOutlineContentMenuBarHandler = function() {
  if (!this.outlineContent) {
    return;
  }
  var menuBar = this._outlineContentMenuBar(this.outlineContent);
  if (menuBar) {
    menuBar.on('propertyChange', this._outlineContentMenuBarPropertyChangeHandler);
  }
};

scout.DesktopHeader.prototype._detachOutlineContentMenuBarHandler = function() {
  if (!this.outlineContent) {
    return;
  }
  var menuBar = this._outlineContentMenuBar(this.outlineContent);
  if (menuBar) {
    menuBar.off('propertyChange', this._outlineContentMenuBarPropertyChangeHandler);
  }
};

scout.DesktopHeader.prototype._outlineContentMenuBar = function(outlineContent) {
  if (outlineContent instanceof scout.Form) {
    return outlineContent.rootGroupBox.menuBar;
  }
  return outlineContent.menuBar;
};

scout.DesktopHeader.prototype.updateViewButtonStyling = function() {
  if (!this.viewButtonsVisible || !this.desktop.bench || !this.desktop.bench.outlineContentVisible) {
    return;
  }
  var outlineContent = this.desktop.bench.outlineContent;
  if (!outlineContent) {
    // Outline content not available yet (-> needs to be loaded first)
    return;
  }
  var hasMenuBar = false;
  if (outlineContent instanceof scout.Form) {
    var rootGroupBox = outlineContent.rootGroupBox;
    hasMenuBar = rootGroupBox.menuBar && rootGroupBox.menuBarVisible && rootGroupBox.menuBar.visible;
  } else {
    hasMenuBar = outlineContent.menuBar && outlineContent.menuBar.visible;
  }
  this.viewButtons.viewTabs.forEach(function(tab) {
    tab.$container.toggleClass('outline-content-has-menubar', !!hasMenuBar);
  }, this);
  this.viewButtons.viewMenuTab.$container.toggleClass('outline-content-has-menubar', !!hasMenuBar);
};

scout.DesktopHeader.prototype._onDesktopNavigationVisibleChange = function(event) {
  // If navigation gets visible: Hide view buttons immediately
  // If navigation gets hidden using animation: Show view buttons when animation ends
  if (this.desktop.navigationVisible) {
    this.updateViewButtonsVisibility();
  }
};

scout.DesktopHeader.prototype._onDesktopAnimationEnd = function(event) {
  this.updateViewButtonsVisibility();
};

scout.DesktopHeader.prototype.onBenchOutlineContentChange = function(content) {
  this._detachOutlineContentMenuBarHandler();
  this.outlineContent = content;
  this.updateViewButtonStyling();
  this._attachOutlineContentMenuBarHandler();
};

scout.DesktopHeader.prototype._onDesktopPropertyChange = function(event) {
  if (event.changedProperties.indexOf('navigationVisible') !== -1) {
    this._onDesktopNavigationVisibleChange();
  }
};

scout.DesktopHeader.prototype._onOutlineContentMenuBarPropertyChange = function(event) {
  if (event.changedProperties.indexOf('visible') !== -1) {
    this.updateViewButtonStyling();
  }
};
