/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {arrays, BreadcrumbBarEventMap, BreadcrumbBarLayout, BreadcrumbBarModel, BreadcrumbItem, HtmlComponent, InitModelOf, ObjectOrChildModel, scout, Widget} from '../index';

export class BreadcrumbBar extends Widget implements BreadcrumbBarModel {
  declare model: BreadcrumbBarModel;
  declare eventMap: BreadcrumbBarEventMap;
  declare self: BreadcrumbBar;

  breadcrumbItems: BreadcrumbItem[];
  ellipsisBreadcrumbItem: BreadcrumbItem;

  constructor() {
    super();
    this.breadcrumbItems = [];
    this.ellipsisBreadcrumbItem = null;

    this._addWidgetProperties(['breadcrumbItems']);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._setBreadcrumbItems(this.breadcrumbItems);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('breadcrumb-bar');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new BreadcrumbBarLayout(this));
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderBreadcrumbItems();
  }

  setBreadcrumbItems(breadcrumbItems: ObjectOrChildModel<BreadcrumbItem> | ObjectOrChildModel<BreadcrumbItem>[]) {
    this.setProperty('breadcrumbItems', breadcrumbItems);
  }

  protected _setBreadcrumbItems(breadcrumbItems: BreadcrumbItem | BreadcrumbItem[]) {
    this._setProperty('breadcrumbItems', arrays.ensure(breadcrumbItems));
  }

  protected _renderBreadcrumbItems() {
    this.breadcrumbItems.forEach((breadcrumbItem: BreadcrumbItem, index: number) => {
      if (index === 1) {
        this._renderEllipsis(); // render ellipsis after first item
      }
      breadcrumbItem.render();
    });
    this._updateMarkers();
    // Invalidate layout because breadcrumb bar may now be longer or shorter
    this.htmlComp.invalidateLayoutTree();
  }

  protected _renderEllipsis() {
    if (this.ellipsisBreadcrumbItem) {
      this.ellipsisBreadcrumbItem.destroy();
    }
    if (this.breadcrumbItems.length <= 2) {
      return;
    }

    this.ellipsisBreadcrumbItem = scout.create(BreadcrumbItem, {
      parent: this,
      text: '...',
      enabled: false
    });

    this.ellipsisBreadcrumbItem.render();
  }

  protected _updateMarkers() {
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
