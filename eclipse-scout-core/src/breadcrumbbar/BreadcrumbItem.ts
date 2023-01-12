/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Action, BreadcrumbItemModel, HtmlComponent} from '../index';

export class BreadcrumbItem extends Action implements BreadcrumbItemModel {
  declare model: BreadcrumbItemModel;

  /** Arbitrary reference value, can be used to find and select modes (see BreadcrumbBar.js) */
  ref: any;

  constructor() {
    super();
    this.ref = null;
  }

  protected override _render() {
    super._render();
    this.$container.addClass('breadcrumb-item');
  }

  protected override _renderText() {
    const text = this.text || '';
    if (text && this.textVisible) {
      if (!this.$text) {
        // Create a separate text element to so that setting the text does not remove the icon
        this.$text = this.$container.appendSpan('content text');
        HtmlComponent.install(this.$text, this.session);
      }
      this.$text.text(text);
    } else {
      this._removeText();
    }
  }
}
