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
  this.htmlComp.pixelBasedSizing = false;
  this._$viewTabBar = this.$container.appendDiv('desktop-view-tabs');
  this._$toolBar = this.$container.appendDiv('header-tools');
  this._renderToolMenus();
  this._renderApplicationLogoUrl();

  this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
};

scout.DesktopHeader.prototype._remove = function() {
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
  scout.DesktopHeader.parent.prototype._remove.call(this);
};

scout.DesktopHeader.prototype._renderToolMenus = function() {
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

scout.DesktopHeader.prototype.setApplicationLogoUrl = function(applicationLogoUrl) {
  this.applicationLogoUrl = applicationLogoUrl;
  if (this.rendered) {
    this._renderApplicationLogoUrl();
  }
};

scout.DesktopHeader.prototype._renderApplicationLogoUrl = function() {
  if (this.applicationLogoUrl) {
    this._renderApplicationLogo();
  } else {
    this._removeApplicationLogo();
  }
  this.invalidateLayoutTree();
};

scout.DesktopHeader.prototype._renderApplicationLogo = function() {
  if (!this.applicationLogo) {
    this.applicationLogo = scout.create('ApplicationLogo', {
      parent: this,
      url: this.applicationLogoUrl
    });
    this.applicationLogo.render(this.$container);
  } else {
    this.applicationLogo.setUrl(this.applicationLogoUrl);
  }
};

scout.DesktopHeader.prototype._removeApplicationLogo = function() {
  if (!this.applicationLogo) {
    return;
  }
  this.applicationLogo.remove();
  this.applicationLogo = null;
};
