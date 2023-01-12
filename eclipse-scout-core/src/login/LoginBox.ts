/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Box, Device, InitModelOf, LoginAppModel, strings, TextMap} from '../index';
import $ from 'jquery';

export class LoginBox extends Box {
  declare model: LoginAppModel;

  ajaxOptions: JQuery.AjaxSettings;
  authUrl: string;
  onPostDoneFunc: (data: Record<string, any>) => void;
  redirectUrl: string;
  userDataKey: string;
  passwordDataKey: string;
  additionalData: Record<string, any>;
  prepareRedirectUrlFunc: (url: string) => string;
  messageKey: string;
  texts: TextMap;
  $message: JQuery;
  $form: JQuery;
  $user: JQuery;
  $password: JQuery;
  $button: JQuery;

  constructor() {
    super();

    this.ajaxOptions = {
      type: 'POST'
    };
    this.authUrl = 'auth';
    this.onPostDoneFunc = this.redirect.bind(this);
    this.redirectUrl = null;
    this.logoUrl = 'logo.png';
    this.userDataKey = 'user';
    this.passwordDataKey = 'password';
    this.additionalData = {};
    this.prepareRedirectUrlFunc = LoginBox.prepareRedirectUrl;
    this.messageKey = null;
    this.texts = null;
    this.$message = null;
    this.$form = null;
    this.$user = null;
    this.$password = null;
    this.$button = null;
  }

  init(options: InitModelOf<this>) {
    options = options || {} as InitModelOf<this>;
    let allTexts = $.extend({
      'ui.Login': 'Login',
      'ui.LoginFailed': 'Login failed',
      'ui.User': 'Username',
      'ui.Password': 'Password'
    }, options.texts);
    delete options.texts;
    options.ajaxOptions = $.extend(this.ajaxOptions, options.ajaxOptions);
    $.extend(this, options);
    this.texts = new TextMap(allTexts);
  }

  protected override _render() {
    super._render();

    this.$container.addClass('login-box');
    this.$content.addClass('login-box-content ');
    this.$form = $('<form>')
      .attr('action', this.authUrl)
      .attr('method', 'post')
      .on('submit', this._onLoginFormSubmit.bind(this))
      .appendTo(this.$content);
    if (this.messageKey) {
      this.$message = $('<div>')
        .attr('id', 'message-box')
        .addClass('message-box')
        .html(strings.nl2br(this.texts.get(this.messageKey)))
        .appendTo(this.$form);
    }
    this.$user = $('<input>')
      .attr('type', 'text')
      .attr('autocapitalize', 'off')
      .attr('autocorrect', 'off')
      .placeholder(this.texts.get('ui.User'))
      .appendTo(this.$form);
    this.$password = $('<input>')
      .attr('type', 'password')
      .placeholder(this.texts.get('ui.Password'))
      .appendTo(this.$form);
    this.$button = $('<button>')
      .attr('type', 'submit')
      .addClass('login-button button default')
      .text(this.texts.get('ui.Login'))
      .appendTo(this.$form);

    this.$user.focus();
  }

  protected _resetButtonText() {
    this.$button
      .text(this.texts.get('ui.Login'))
      .removeClass('login-error');
  }

  data(): Record<string, any> {
    let data = {};
    data[this.userDataKey] = this.$user.val();
    data[this.passwordDataKey] = this.$password.val();
    $.extend(data, this.additionalData);
    return data;
  }

  protected _onLoginFormSubmit(event: JQuery.SubmitEvent<HTMLElement>) {
    // Prevent default submit action
    event.preventDefault();

    let url = this.$form.attr('action');
    let data = this.data();

    this.$button
      .removeClass('login-error')
      .setEnabled(false);
    this.$user.off('input.resetLoginError');
    this.$password.off('input.resetLoginError');
    if (Device.get().supportsCssAnimation()) {
      this.$button
        .html('')
        .append($('<div>').addClass('login-button-loading'));
    }

    let options = $.extend({}, this.ajaxOptions, {
      url: url,
      data: data
    });
    $.ajax(options)
      .done(this._onPostDone.bind(this))
      .fail(this._onPostFail.bind(this));
  }

  redirect(data: Record<string, any>) {
    this.$backgroundElements.addClass('box-background-elements-fadeout');
    if (Device.get().supportsCssAnimation()) {
      this.$backgroundElements.oneAnimationEnd(() => {
        this._redirect(data);
      });
    } else {
      // fallback for old browsers that do not support the animation-end event
      this._redirect(data);
    }
  }

  protected _redirect(data: Record<string, any>) {
    // Calculate target URL
    let url = this.redirectUrl;
    if (!url) {
      url = (window.location.href || '').trim();
      // Remove login and everything after it from the URL
      url = this.prepareRedirectUrlFunc(url);
    }

    // Go to target URL
    if (url) {
      window.location.href = url;
    } else {
      window.location.reload();
    }
  }

  protected _onPostDone(data: Record<string, any>) {
    this.remove();
    this.onPostDoneFunc.call(this, data);
  }

  protected _onPostFail(jqXHR: JQuery.jqXHR, textStatus: JQuery.Ajax.ErrorTextStatus, errorThrown: string) {
    // execute delayed to make sure loading animation is visible, otherwise (if it is very fast), it flickers
    setTimeout(this._onPostFailImpl.bind(this, jqXHR, textStatus, errorThrown), 300);
  }

  protected _onPostFailImpl(jqXHR: JQuery.jqXHR, textStatus: JQuery.Ajax.ErrorTextStatus, errorThrown: string) {
    this.$button
      .setEnabled(true)
      .html('')
      .text(this.texts.get('ui.LoginFailed'))
      .addClass('login-error');
    this.$user
      .val('')
      .focus();
    this.$password
      .val('');

    this.$user
      .one('input.resetLoginError', this._resetButtonText.bind(this));
    this.$password
      .one('input.resetLoginError', this._resetButtonText.bind(this));
  }

  // ----- Helper functions -----

  static prepareRedirectUrl(url: string): string {
    let urlParts = /^([^?#]*)(\?[^#]*)?(#.*)?$/.exec(url || ''); // $1 = baseUrl, $2 = queryPart, $3 = hashPart
    let filteredBaseUrl = urlParts[1]
      .replace(/login.html$/, '')
      .replace(/login$/, '')
      .replace(/logout$/, '');
    return filteredBaseUrl + (urlParts[2] ? urlParts[2] : '') + (urlParts[3] ? urlParts[3] : '');
  }
}
