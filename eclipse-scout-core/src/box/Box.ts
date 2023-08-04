/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import $ from 'jquery';

/**
 * Used for static pages like login, logout, unsupported-browser and noscript section.
 * Beside custom elements it may contain a header with a logo and a button bar.
 * Note: It does not extend from Widget because widget has too many dependencies which are not needed for this simple use case (login-module does not include these dependencies)
 */
export class Box {
  rendered: boolean;
  logoUrl: string;

  $parent: JQuery;
  $container: JQuery;
  $backgroundElements: JQuery;
  $wrapper: JQuery;
  $content: JQuery;
  $header: JQuery;
  $logo: JQuery;

  constructor() {
    this.rendered = false;
    this.logoUrl = null;

    this.$parent = null;
    this.$container = null;
    this.$backgroundElements = null;
    this.$wrapper = null;
    this.$content = null;
    this.$header = null;
    this.$logo = null;
  }

  render($parent: JQuery) {
    this.$parent = $parent;
    this._render();
    this.rendered = true;
  }

  remove() {
    this.$container.remove();
    this.$container = null;
    this.rendered = false;
  }

  protected _render() {
    // add background-elements which can be styled individually
    this.$backgroundElements = $('<div>')
      .addClass('box-background-elements')
      .appendTo(this.$parent);

    $('<div>').addClass('box-background-element-1').appendTo(this.$backgroundElements);
    $('<div>').addClass('box-background-element-2').appendTo(this.$backgroundElements);
    $('<div>').addClass('box-background-element-3').appendTo(this.$backgroundElements);

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
        .attr('alt', '')
        .appendTo(this.$header);
    }
  }
}
