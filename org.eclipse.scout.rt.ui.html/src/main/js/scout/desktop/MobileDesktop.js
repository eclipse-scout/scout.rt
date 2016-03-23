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

scout.MobileViewTabsController = function(desktop) {
  scout.MobileViewTabsController.parent.call(this, desktop);
};
scout.inherits(scout.MobileViewTabsController, scout.ViewTabsController);

scout.MobileViewTabsController.prototype.createAndRenderViewTab = function(view, position) {
  this._desktop.setHeaderVisible(true);
  this._desktop.setBenchVisible(true);
  this._desktop.setNavigationVisible(false);

  return scout.MobileViewTabsController.parent.prototype.createAndRenderViewTab.call(this, view, position);
};

scout.MobileViewTabsController.prototype._removeViewTab = function(viewTab, viewId) {
  scout.MobileViewTabsController.parent.prototype._removeViewTab.call(this, viewTab, viewId);
  if (this._viewTabs.length === 0) {
    // Hide bench if no view forms are open -> show navigation
    this._desktop.setNavigationVisible(true);
    this._desktop.setBenchVisible(false);
    this._desktop.setHeaderVisible(false);
  }
};
