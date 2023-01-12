/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Box, InitModelOf, LogoutAppModel, strings, TextMap, webstorage} from '../index';
import $ from 'jquery';

export class LogoutBox extends Box {
  declare model: LogoutAppModel;

  texts: TextMap;
  loginUrl: string;
  options: LogoutAppModel;
  $buttonBar: JQuery;

  constructor() {
    super();

    this.texts = null;
    this.loginUrl = null;
    this.options = null;
    this.$buttonBar = null;
  }

  init(opts: InitModelOf<this>) {
    let defaultOpts: LogoutAppModel = {
      loginUrl: webstorage.getItemFromSessionStorage('scout:loginUrl') || './',
      logoUrl: 'logo.png'
    };
    this.options = $.extend({}, defaultOpts, opts);
    let defaultTexts = {
      'ui.LogoutSuccessful': 'Good bye!',
      'ui.LoginAgain': 'Login again'
    };
    this.options.texts = $.extend({}, defaultTexts, opts.texts);

    this.texts = new TextMap(this.options.texts);
    this.loginUrl = this.options.loginUrl;
    this.logoUrl = this.options.logoUrl;
  }

  protected override _render() {
    super._render();

    this.$content.addClass('small centered')
      .appendDiv().html(strings.nl2br(this.texts.get('ui.LogoutSuccessful')));

    this.$buttonBar = $('<div>')
      .addClass('button-bar')
      .appendTo(this.$content);
    $('<button>')
      .addClass('button')
      .text(this.texts.get('ui.LoginAgain'))
      .on('click', this._loginAgain.bind(this))
      .appendTo(this.$buttonBar);
  }

  protected _loginAgain() {
    window.location.href = this.loginUrl;
  }
}
