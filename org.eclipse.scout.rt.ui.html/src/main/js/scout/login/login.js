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
    var $buttonDiv, texts, defaultOpts = {
      texts: {
        'ui.Login': 'Login',
        'ui.LoginFailed': 'Login failed',
        'ui.User': 'User',
        'ui.Password': 'Password'
      }
    };
    this.options = $.extend({}, defaultOpts, opts);
    texts = new scout.Texts(this.options.texts);
    this.$form = $('<form>')
      .attr('action', 'auth')
      .attr('method', 'post')
      .submit(onLoginFormSubmit.bind(this))
      .appendTo($('body'));
    this.$container = $('<div>')
      .attr('id', 'login-box')
      .addClass('box-with-logo')
      .appendTo(this.$form);
    this.$user = $('<input>')
      .attr('type', 'text')
      .attr('autocapitalize', 'off')
      .attr('autocorrect', 'off')
      .placeholder(texts.get('ui.User'))
      .appendTo(this.$container);
    this.$password = $('<input>')
      .attr('type', 'password')
      .placeholder(texts.get('ui.Password'))
      .appendTo(this.$container);
    $buttonDiv = $('<div>')
      .attr('id', 'login-button')
      .addClass('button')
      .appendTo(this.$container);
    this.$button = $('<button>')
      .addClass('default')
      .attr('type', 'submit')
      .text(texts.get('ui.Login'))
      .appendTo($buttonDiv);

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
          .append($('<div>').attr('id', 'login-button-loading'));
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
