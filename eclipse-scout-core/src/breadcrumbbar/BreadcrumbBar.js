/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import {arrays, HtmlComponent, Widget, scout, BreadcrumbBarLayout} from '../index';

export default class BreadcrumbBar extends Widget {

  constructor() {
    super();
    this.breadcrumbItems = [];
    this._ellipsisBreadcrumbItem = null;

    this._addWidgetProperties(['breadcrumbItems']);
  }

  _init(model) {
    super._init(model);
    this._setBreadcrumbItems(this.breadcrumbItems);
  }

  _render() {
    this.$container = this.$parent.appendDiv('breadcrumb-bar');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new BreadcrumbBarLayout(this));
  }

  _renderProperties() {
    super._renderProperties();
    this._renderBreadcrumbItems();
  }

  setBreadcrumbItems(breadcrumbItems) {
    this.setProperty('breadcrumbItems', breadcrumbItems);
  }

  _setBreadcrumbItems(breadcrumbItems) {
    this._setProperty('breadcrumbItems', arrays.ensure(breadcrumbItems));
  }

  _renderBreadcrumbItems() {
    this.breadcrumbItems.forEach((breadcrumbItem, index) => {
      if (index === 1) {
        this._renderEllipsis(); // render ellipsis after first item
      }
      breadcrumbItem.render();
    }, this);
    this._updateMarkers();
    // Invalidate layout because breadcrumb bar may now be longer or shorter
    this.htmlComp.invalidateLayoutTree();
  }

  _renderEllipsis() {
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
  }

  _updateMarkers() {
    const visibleCrumbs = [];
    this.breadcrumbItems.forEach(breadcrumb => {
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
  }
}
