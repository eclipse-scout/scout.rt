/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Box, Device, strings, TextMap} from '../index';
import $ from 'jquery';

export default class LoginBox extends Box {

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
    this.texts = {
      'ui.Login': 'Login',
      'ui.LoginFailed': 'Login failed',
      'ui.User': 'Username',
      'ui.Password': 'Password'
    };
  }

  init(options) {
    options = options || {};
    options.texts = new TextMap($.extend(this.texts, options.texts));
    options.ajaxOptions = $.extend(this.ajaxOptions, options.ajaxOptions);
    $.extend(this, options);
  }

  _render() {
    super._render();

    this.$container.addClass('login-box');
    this.$content.addClass('login-box-content ');
    this.$form = $('<form>')
      .attr('action', this.authUrl)
      .attr('method', 'post')
      .submit(this._onLoginFormSubmit.bind(this))
      .appendTo(this.$content);
    if (this.messageKey) {
      this.$message = $('<div>')
        .attr('id', 'message-box')
        .html(strings.nl2br(this.texts.get(this.messageKey)))
        .appendTo(this.$form);
    }
    this.$user = $('<input>')
      .attr('type', 'text')
      .attr('autocapitalize', 'off')
      .attr('autocorrect', 'off')
      .placeholder(this.texts.get('ui.User'))
      .addClass('alternative')
      .appendTo(this.$form);
    this.$password = $('<input>')
      .attr('type', 'password')
      .placeholder(this.texts.get('ui.Password'))
      .addClass('alternative')
      .appendTo(this.$form);
    this.$button = $('<button>')
      .attr('type', 'submit')
      .addClass('login-button button default')
      .text(this.texts.get('ui.Login'))
      .appendTo(this.$form);

    this.$user.focus();
  }

  _resetButtonText() {
    this.$button
      .text(this.texts.get('ui.Login'))
      .removeClass('login-error');
  }

  data() {
    let data = {};
    data[this.userDataKey] = this.$user.val();
    data[this.passwordDataKey] = this.$password.val();
    $.extend(data, this.additionalData);
    return data;
  }

  _onLoginFormSubmit(event) {
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

  redirect(data) {
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

  _onPostDone(data) {
    this.remove();
    this.onPostDoneFunc.call(this, data);
  }

  _onPostFail(jqXHR, textStatus, errorThrown) {
    // execute delayed to make sure loading animation is visible, otherwise (if it is very fast), it flickers
    setTimeout(this._onPostFailImpl.bind(this, jqXHR, textStatus, errorThrown), 300);
  }

  _onPostFailImpl(jqXHR, textStatus, errorThrown) {
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

    // async bind reset function because focus method on username field (see above) already triggers an input event on IE.
    setTimeout(() => {
      this.$user
        .one('input.resetLoginError', this._resetButtonText.bind(this));
      this.$password
        .one('input.resetLoginError', this._resetButtonText.bind(this));
    });
  }

  // ----- Helper functions -----

  static prepareRedirectUrl(url) {
    let urlParts = /^([^?#]*)(\?[^#]*)?(#.*)?$/.exec(url || ''); // $1 = baseUrl, $2 = queryPart, $3 = hashPart
    let filteredBaseUrl = urlParts[1]
      .replace(/login.html$/, '')
      .replace(/login$/, '')
      .replace(/logout$/, '');
    return filteredBaseUrl + (urlParts[2] ? urlParts[2] : '') + (urlParts[3] ? urlParts[3] : '');
  }
}
