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
scout.DesktopViewTabs = function() {
  scout.DesktopViewTabs.parent.call(this);
};
scout.inherits(scout.DesktopViewTabs, scout.Widget);

scout.DesktopViewTabs.prototype._initKeyStrokeContext = function(keyStrokeContext) {
  scout.DesktopHeader.parent.prototype._initKeyStrokeContext.call(this, keyStrokeContext);

  // Bound to desktop
  this.desktopKeyStrokeContext = new scout.KeyStrokeContext();
  this.desktopKeyStrokeContext.invokeAcceptInputOnActiveValueField = true;
  this.desktopKeyStrokeContext.$bindTarget = this.session.desktop.$container;
  this.desktopKeyStrokeContext.$scopeTarget = this.$container;
  this.desktopKeyStrokeContext.registerKeyStroke([
    new scout.ViewTabSelectKeyStroke(this.session.desktop),
    new scout.DisableBrowserTabSwitchingKeyStroke(this.session.desktop)
  ]);
};

scout.DesktopViewTabs.prototype._render = function($parent) {
  this.viewTabsController = this.session.desktop.viewTabsController;

  this.$container = $parent.appendDiv('desktop-view-tabs');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.DesktopViewTabsLayout(this));

  // Render the tabs
  this.viewTabs().forEach(function(viewTab) {
    if (!viewTab.rendered) {
      viewTab.render(this.$container);
    }
  }, this);
  this.session.keyStrokeManager.installKeyStrokeContext(this.desktopKeyStrokeContext);
};

scout.DesktopViewTabs.prototype._remove = function() {
  this.session.keyStrokeManager.uninstallKeyStrokeContext(this.desktopKeyStrokeContext);
  scout.DesktopViewTabs.parent.prototype._remove.call(this);
};

scout.DesktopViewTabs.prototype.viewTabs = function() {
  return this.viewTabsController.viewTabs();
};

scout.DesktopViewTabs.prototype.selectViewTab = function(viewTab) {
  this.viewTabsController.selectViewTab(viewTab);
};
