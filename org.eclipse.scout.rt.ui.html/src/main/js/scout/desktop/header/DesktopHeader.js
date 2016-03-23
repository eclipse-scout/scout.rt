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
};
scout.inherits(scout.DesktopHeader, scout.Widget);

scout.DesktopHeader.prototype._init = function(model) {
  scout.DesktopHeader.parent.prototype._init.call(this, model);
  this.desktop = this.session.desktop;
  this.toolBarVisible = scout.nvl(model.toolBarVisible, true);
};

scout.DesktopHeader.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.DesktopHeader.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  // Bound to desktop
  this.desktopKeyStrokeContext = new scout.KeyStrokeContext();
  this.desktopKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
  this.desktopKeyStrokeContext.$bindTarget = this.desktop.$container;
  this.desktopKeyStrokeContext.$scopeTarget = this.$container;
  this.desktopKeyStrokeContext.registerKeyStroke([
    new scout.ViewTabSelectKeyStroke(this),

    new scout.DisableBrowserTabSwitchingKeyStroke(this)
  ].concat(this.desktop.actions));
};

scout.DesktopHeader.prototype._render = function($parent) {
  this.$container = $parent.appendDiv('desktop-header');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.DesktopHeaderLayout(this));
  this._$viewTabBar = this.$container.appendDiv('desktop-view-tabs');
  this._$toolBar = this.$container.appendDiv('header-tools');
  this._renderToolBarVisible();
  this._renderLogoUrl();

  this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
};

scout.DesktopHeader.prototype._remove = function() {
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
  scout.DesktopHeader.parent.prototype._remove.call(this);
};

scout.DesktopHeader.prototype._renderToolBar = function() {
  // we set the menuStyle property to render a menu with a different style
  // depending on where the menu is located (header VS menubar).
  this.desktop.actions.forEach(function(action) {
    action._customCssClasses = "header-tool-item";
    action.popupOpeningDirectionX = 'left';
    action.setParent(this);
    action.render(this._$toolBar);
  }.bind(this));

  if (this.desktop.actions.length) {
    this.desktop.actions[this.desktop.actions.length - 1].$container.addClass('last');
  }
};

scout.DesktopHeader.prototype._removeToolBar = function() {
  if (!this._$toolBar) {
    return;
  }
  this._$toolBar.remove();
  this._$toolBar = null;
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
};

scout.DesktopHeader.prototype.setLogoUrl = function(logoUrl) {
  this.logoUrl = logoUrl;
  if (this.rendered) {
    this._renderLogoUrl();
  }
};

scout.DesktopHeader.prototype.setToolBarVisible = function(toolBarVisible) {
  this.toolBarVisible = toolBarVisible;
  if (this.rendered) {
    this._renderToolBarVisible();
  }
};
