/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import {BreadcrumbBar, BreadcrumbBarFieldEventMap, BreadcrumbBarFieldModel, BreadcrumbItem, FormField} from '../../../index';
import {ObjectOrChildModel} from '../../../scout';

export default class BreadcrumbBarField extends FormField implements BreadcrumbBarFieldModel {
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
