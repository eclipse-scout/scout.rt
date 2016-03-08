/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.login = {

  /**
   * opts:
   * - redirectUrl: URL to redirect to after successful login
   * - prepareRedirectUrl: function(s) that is called on the redirectUrl before opening it
   */
  init: function(opts) {
    var logoUrl, $buttonDiv, texts,
    $parent = $('body'),
    defaultOpts = {
      logoUrl: 'res/logo.png',
      texts: {
        'ui.Login': 'Login',
        'ui.LoginFailed': 'Login failed',
        'ui.User': 'User',
        'ui.Password': 'Password'
      }
    };
    this.options = $.extend({}, defaultOpts, opts);
    texts = new scout.Texts(this.options.texts);
    logoUrl = this.options.logoUrl;
    this.$container = $('<div>')
      .addClass('login-box box-with-logo')
      .appendTo($parent);

    this.$wrapper = $('<div>')
      .addClass('wrapper')
      .appendTo(this.$container);

    this.$content = $('<div>')
      .addClass('login-box-content box-with-logo-content')
      .appendTo(this.$wrapper);

    if (logoUrl) {
      this.$header = this.$content.appendDiv('header');
      this.$logo = $('<img>')
        .addClass('logo')
        .attr('src', logoUrl)
        .appendTo(this.$header);
    }
    this.$form = $('<form>')
      .attr('action', 'auth')
      .attr('method', 'post')
      .submit(onLoginFormSubmit.bind(this))
      .appendTo(this.$content);
    this.$user = $('<input>')
      .attr('type', 'text')
      .attr('autocapitalize', 'off')
      .attr('autocorrect', 'off')
      .placeholder(texts.get('ui.User'))
      .appendTo(this.$form);
    this.$password = $('<input>')
      .attr('type', 'password')
      .placeholder(texts.get('ui.Password'))
      .appendTo(this.$form);
    this.$button = $('<button>')
      .attr('type', 'submit')
      .addClass('login-button button default')
      .text(texts.get('ui.Login'))
      .appendTo(this.$form);

    this.$user.focus();

    scout.prepareDOM();

    // ----- Helper functions -----

    function onLoginFormSubmit(event) {
      // Prevent default submit action
      event.preventDefault();

      var url = this.$form.attr('action');
      var data = {
        user: this.$user.val(),
        password: this.$password.val()
      };

      this.$button
        .removeClass('login-error')
        .setEnabled(false);
      this.$user.off('input.resetLoginError');
      this.$password.off('input.resetLoginError');
      if (scout.device.supportsCssAnimation()) {
        this.$button
          .html('')
          .append($('<div>').addClass('login-button-loading'));
      }

      $.post(url, data)
        .done(onPostDone.bind(this))
        .fail(onPostFail.bind(this));
    }

    function onPostDone(data) {
      // Calculate target URL
      var url = this.options.redirectUrl;
      if (!url) {
        url = (window.location.href || '').trim();
        var prepareRedirectUrlFunc = this.options.prepareRedirectUrlFunc || prepareRedirectUrl;
        // Remove login.html and everything after it from the URL
        url = prepareRedirectUrlFunc(url);
      }

      // Go to target URL
      if (url) {
        window.location.href = url;
      } else {
        window.location.reload();
      }
    }

    function onPostFail(jqXHR, textStatus, errorThrown) {
      // execute delayed to make sure loading animation is visible, otherwise (if it is very fast), it flickers
      setTimeout(function() {
        this.$button
          .setEnabled(true)
          .html('')
          .text(texts.get('ui.LoginFailed'))
          .addClass('login-error');
        this.$user.focus();
        this.$user.one('input.resetLoginError', resetButtonText.bind(this));
        this.$password.one('input.resetLoginError', resetButtonText.bind(this));
      }.bind(this), 300);
    }

    function prepareRedirectUrl(url) {
      var urlParts = /^([^?#]*)(\?[^#]*)?(#.*)?$/.exec(url || ''); // $1 = baseUrl, $2 = queryPart, $3 = hashPart
      var filteredBaseUrl = urlParts[1]
        .replace(/login.html$/, '')
        .replace(/login$/, '')
        .replace(/logout$/, '');
      return filteredBaseUrl + (urlParts[2] ? urlParts[2] : '') + (urlParts[3] ? urlParts[3] : '');
    }

    function resetButtonText() {
      this.$button
        .text(texts.get('ui.Login'))
        .removeClass('login-error');
    }
  }

};
