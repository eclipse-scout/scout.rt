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

import {BreadcrumbBar, BreadcrumbBarFieldEventMap, BreadcrumbBarFieldModel, BreadcrumbBarModel, BreadcrumbItem, BreadcrumbItemModel, FormField, RefModel} from '../../../index';

export default class BreadcrumbBarField extends FormField implements BreadcrumbBarFieldModel {
  declare model: BreadcrumbBarFieldModel;
  declare eventMap: BreadcrumbBarFieldEventMap;

  breadcrumbBar: BreadcrumbBar;

  constructor() {
    super();
    this.breadcrumbBar = null;
    this._addWidgetProperties(['breadcrumbBar']);
  }

  protected _render() {
    this.addContainer(this.$parent, 'breadcrumb-bar-field');
    this.addLabel();
    this.addMandatoryIndicator();
    this.addStatus();
    this._renderBreadcrumbBar();
  }

  setBreadcrumbItems(breadcrumbItems: (BreadcrumbItem | RefModel<BreadcrumbItemModel>) | (BreadcrumbItem | RefModel<BreadcrumbItemModel>)[]) {
    if (!this.breadcrumbBar) {
      return;
    }
    this.breadcrumbBar.setBreadcrumbItems(breadcrumbItems);
  }

  setBreadcrumbBar(breadcrumbBar: BreadcrumbBar | RefModel<BreadcrumbBarModel>) {
    this.setProperty('breadcrumbBar', breadcrumbBar);
  }

  protected _renderBreadcrumbBar() {
    if (!this.breadcrumbBar) {
      return;
    }
    this.breadcrumbBar.render();
    this.addField(this.breadcrumbBar.$container);
    this.invalidateLayoutTree();
  }

  protected _removeBreadcrumbBar() {
    if (!this.breadcrumbBar) {
      return;
    }
    this.breadcrumbBar.remove();
    this._removeField();
    this.invalidateLayoutTree();
  }
}
