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

import {FormField} from '../../../index';

export default class BreadcrumbBarField extends FormField {
  constructor() {
    super();
    this._addWidgetProperties(['breadcrumbBar']);
  }

  _render() {
    this.addContainer(this.$parent, 'breadcrumb-bar-field');
    if (this.breadcrumbBar) {
      this._renderBreadcrumbBar();
    }
  }

  _renderBreadcrumbBar() {
    this.breadcrumbBar.render();
    this.addLabel();
    this.addMandatoryIndicator();
    this.addField(this.breadcrumbBar.$container);
    this.addStatus();
  }

  setBreadcrumbBar(breadcrumbBar) {
    this._setBreadcrumbBar(breadcrumbBar);
  }

  setBreadcrumbItems(breadcrumbItems) {
    this.breadcrumbBar.setBreadcrumbItems(breadcrumbItems);
  }

  _removeBreadcrumbBar() {
    this.breadcrumbBar.remove();
    this._removeField();
  }
}
