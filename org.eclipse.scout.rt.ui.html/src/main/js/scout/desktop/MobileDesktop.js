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
scout.MobileDesktop = function() {
  scout.MobileDesktop.parent.call(this);
};
scout.inherits(scout.MobileDesktop, scout.Desktop);

scout.MobileDesktop.prototype._init = function(model) {
  scout.MobileDesktop.parent.prototype._init.call(this, model);
  this.viewTabsController = new scout.MobileViewTabsController(this);
  this.navigationVisible = true;
  this.headerVisible = false;
  this.benchVisible = false;
  this._adaptOutline(this.outline);
};

/**
 * @override
 */
scout.MobileDesktop.prototype._render = function($parent) {
  this.$container = $parent;
  this.$container.addClass('desktop');
  this.htmlComp = new scout.HtmlComponent(this.$container, this.session);
  this.htmlComp.setLayout(new scout.DesktopLayout(this));
  // Desktop elements are added before this separator, all overlays are opened after (dialogs, popups, tooltips etc.)
  this.$overlaySeparator = this.$container.appendDiv().setVisible(false);
  this._renderNavigationVisible();
  this._renderHeaderVisible();
  this._renderBenchVisible();

  $parent.window().on('resize', this.onResize.bind(this));
};

scout.MobileDesktop.prototype._renderHeader = function() {
  if (this.header) {
    return;
  }
  this.header = scout.create('DesktopHeader', {
    parent: this,
    toolBarVisible: false
  });
  this.header.render(this.$container);
  this.header.$container.insertBefore(this.$overlaySeparator);
};

scout.MobileDesktop.prototype._renderNavigation = function() {
  if (this.navigation) {
    return;
  }
  this.navigation = scout.create('DesktopNavigation', {
    parent: this,
    outline: this.outline,
    toolBarVisible: true,
    layoutData: {
      fullWidth: true
    }
  });
  this.navigation.render(this.$container);
  this.navigation.$container.prependTo(this.$container);
};

scout.MobileDesktop.prototype._renderBench = function() {
  if (this.bench) {
    return;
  }
  this.bench = scout.create('DesktopBench', {
    parent: this,
    outlineContentVisible: false
  });
  this.bench.render(this.$container);
  this.bench.$container.insertBefore(this.$overlaySeparator);
};

/**
 * @override
 */
scout.MobileDesktop.prototype.setOutline = function(outline) {
  this._adaptOutline(outline);
  scout.MobileDesktop.parent.prototype.setOutline.call(this, outline);
};

scout.MobileDesktop.prototype._adaptOutline = function(outline) {
  if (outline) {
    outline.setEmbedDetailContent(true);
    outline.mobile = true;
  }
};

scout.MobileDesktop.prototype.switchToBench = function() {
  this.setHeaderVisible(true);
  this.setBenchVisible(true);
  this.setNavigationVisible(false);
};

scout.MobileDesktop.prototype.switchToNavigation = function() {
  this.setNavigationVisible(true);
  this.setHeaderVisible(false);
  this.setBenchVisible(false);
};

scout.MobileDesktop.prototype._hideForm = function(form) {
  if (form.isView() && this.viewTabsController._viewTabs.length === 1) {
    // Hide bench and show navigation if this is the last view to be hidden
    this.switchToNavigation();
  }
  scout.MobileDesktop.parent.prototype._hideForm.call(this, form);
};

scout.MobileViewTabsController = function(desktop) {
  scout.MobileViewTabsController.parent.call(this, desktop);
};
scout.inherits(scout.MobileViewTabsController, scout.ViewTabsController);

scout.MobileViewTabsController.prototype.createAndRenderViewTab = function(view, position) {
  if (this._viewTabs.length === 0) {
    // Show bench and hide navigation if this is the first view to be shown
    this._desktop.switchToBench();
  }

  return scout.MobileViewTabsController.parent.prototype.createAndRenderViewTab.call(this, view, position);
};
