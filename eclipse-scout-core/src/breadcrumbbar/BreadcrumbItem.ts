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

import {Action, HtmlComponent} from '../index';

export default class BreadcrumbItem extends Action {

  constructor() {
    super();
    this.ref = null; // Arbitrary reference value, can be used to find and select modes (see BreadcrumbBar.js)
  }

  _render() {
    super._render();
    this.$container.addClass('breadcrumb-item');
  }

  _renderText() {
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
