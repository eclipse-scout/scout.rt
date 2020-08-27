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
scout.BreadcrumbItem = function() {
  scout.BreadcrumbItem.parent.call(this);

  this.ref = null; // Arbitrary reference value, can be used to find and select modes (see BreadcrumbBar.js)
};
scout.inherits(scout.BreadcrumbItem, scout.Action);

scout.BreadcrumbItem.prototype._render = function() {
  scout.BreadcrumbItem.parent.prototype._render.call(this);
  this.$container.addClass('breadcrumb-item');
};

scout.BreadcrumbItem.prototype._renderText = function() {
  var text = this.text || '';
  if (text && this.textVisible) {
    if (!this.$text) {
      // Create a separate text element to so that setting the text does not remove the icon
      this.$text = this.$container.appendSpan('content text');
      scout.HtmlComponent.install(this.$text, this.session);
    }
    this.$text.text(text);
  } else {
    this._removeText();
  }
};
