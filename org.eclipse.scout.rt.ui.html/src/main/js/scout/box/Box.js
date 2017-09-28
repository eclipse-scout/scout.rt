/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * Used for static pages like login, logout, unsupported-browser and noscript section.
 * Beside custom elements it may contain a header with a logo and a button bar.
 * Note: It does not extend from Widget because widget has too many dependencies which are not needed for this simple use case (login-module does not include these dependencies)
 */
scout.Box = function() {
  this.$parent;
};

scout.Box.prototype.render = function($parent) {
  this.$parent = $parent;
  this._render();
  this.rendered = true;
};

scout.Box.prototype.remove = function() {
  this.$container.remove();
  this.$container = null;
  this.rendered = false;
};

scout.Box.prototype._render = function() {
  this.$container = $('<div>')
    .addClass('box')
    .appendTo(this.$parent);

  this.$wrapper = $('<div>')
    .addClass('wrapper')
    .appendTo(this.$container);

  this.$content = $('<div>')
    .addClass('box-content')
    .appendTo(this.$wrapper);

  if (this.logoUrl) {
    this.$header = this.$content.appendDiv('header');
    this.$logo = $('<img>')
      .addClass('logo')
      .attr('src', this.logoUrl)
      .appendTo(this.$header);
  }
};
