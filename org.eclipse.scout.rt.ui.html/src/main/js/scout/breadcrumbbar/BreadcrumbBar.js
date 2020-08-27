/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.BreadcrumbBar = function() {
  scout.BreadcrumbBar.parent.call(this);

  this.breadcrumbItems = [];
  this._ellipsisBreadcrumbItem = null;

  this._addWidgetProperties(['breadcrumbItems']);
};
scout.inherits(scout.BreadcrumbBar, scout.Widget);

scout.BreadcrumbBar.prototype._init = function(model) {
  scout.BreadcrumbBar.parent.prototype._init.call(this, model);
  this._setBreadcrumbItems(this.breadcrumbItems);
};

scout.BreadcrumbBar.prototype._render = function() {
  this.$container = this.$parent.appendDiv('breadcrumb-bar');
  this.htmlComp = scout.HtmlComponent.install(this.$container, this.session);
  this.htmlComp.setLayout(new scout.BreadcrumbBarLayout(this));
};

scout.BreadcrumbBar.prototype._renderProperties = function() {
  scout.BreadcrumbBar.parent.prototype._renderProperties.call(this);
  this._renderBreadcrumbItems();
};

scout.BreadcrumbBar.prototype.setBreadcrumbItems = function(breadcrumbItems) {
  this.setProperty('breadcrumbItems', breadcrumbItems);
};

scout.BreadcrumbBar.prototype._setBreadcrumbItems = function(breadcrumbItems) {
  this._setProperty('breadcrumbItems', scout.arrays.ensure(breadcrumbItems));
};

scout.BreadcrumbBar.prototype._renderBreadcrumbItems = function() {
  this.breadcrumbItems.forEach(function(breadcrumbItem, index) {
    if (index === 1) {
      this._renderEllipsis(); // render ellipsis after first item
    }
    breadcrumbItem.render();
  }, this);
  this._updateMarkers();
  // Invalidate layout because breadcrumb bar may now be longer or shorter
  this.htmlComp.invalidateLayoutTree();
};

scout.BreadcrumbBar.prototype._renderEllipsis = function() {
  if (this._ellipsisBreadcrumbItem) {
    this._ellipsisBreadcrumbItem.destroy();
  }
  if (this.breadcrumbItems.length <= 2) {
    return;
  }

  this._ellipsisBreadcrumbItem = scout.create('BreadcrumbItem', {
    parent: this,
    text: '...',
    enabled: false
  });

  this._ellipsisBreadcrumbItem.render();
};

scout.BreadcrumbBar.prototype._updateMarkers = function() {
  var visibleCrumbs = [];
  this.breadcrumbItems.forEach(function(breadcrumb) {
    if (breadcrumb.rendered) {
      breadcrumb.$container.removeClass('first last');
      if (breadcrumb.isVisible()) {
        visibleCrumbs.push(breadcrumb);
      }
    }
  });
  if (visibleCrumbs.length) {
    visibleCrumbs[0].$container.addClass('first');
    visibleCrumbs[visibleCrumbs.length - 1].$container.addClass('last');
  }
};
