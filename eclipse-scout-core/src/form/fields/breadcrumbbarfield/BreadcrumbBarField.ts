/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {BreadcrumbBar, BreadcrumbBarFieldEventMap, BreadcrumbBarFieldModel, BreadcrumbItem, FormField, ObjectOrChildModel} from '../../../index';

export class BreadcrumbBarField extends FormField implements BreadcrumbBarFieldModel {
  declare model: BreadcrumbBarFieldModel;
  declare eventMap: BreadcrumbBarFieldEventMap;
  declare self: BreadcrumbBarField;

  breadcrumbBar: BreadcrumbBar;

  constructor() {
    super();
    this.breadcrumbBar = null;
    this._addWidgetProperties(['breadcrumbBar']);
  }

  protected override _render() {
    this.addContainer(this.$parent, 'breadcrumb-bar-field');
    this.addLabel();
    this.addMandatoryIndicator();
    this.addStatus();
    this._renderBreadcrumbBar();
  }

  setBreadcrumbItems(breadcrumbItems: ObjectOrChildModel<BreadcrumbItem> | ObjectOrChildModel<BreadcrumbItem>[]) {
    if (!this.breadcrumbBar) {
      return;
    }
    this.breadcrumbBar.setBreadcrumbItems(breadcrumbItems);
  }

  setBreadcrumbBar(breadcrumbBar: ObjectOrChildModel<BreadcrumbBar>) {
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
