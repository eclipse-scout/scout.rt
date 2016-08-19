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
scout.LoginBox = function() {
  scout.LoginBox.parent.call(this);
};
scout.inherits(scout.LoginBox, scout.Box);

scout.LoginBox.prototype.init = function(opts) {
  var defaultOpts = {
    logoUrl: 'res/logo.png'
  };
  this.options = $.extend({}, defaultOpts, opts);
  var defaultTexts = {
    'ui.Login': 'Login',
    'ui.LoginFailed': 'Login failed',
    'ui.User': 'Username',
    'ui.Password': 'Password'
  };
  this.options.texts = $.extend({}, defaultTexts, opts.texts);

  this.texts = new scout.TextMap(this.options.texts);
  this.logoUrl = this.options.logoUrl;
};

scout.LoginBox.prototype.render = function($parent) {
  scout.LoginBox.parent.prototype.render.call(this, $parent);

  this.$container.addClass('login-box');
  this.$content.addClass('login-box-content ');
  this.$form = $('<form>')
    .attr('action', 'auth')
    .attr('method', 'post')
    .submit(this._onLoginFormSubmit.bind(this))
    .appendTo(this.$content);
  if (this.options.messageKey) {
    this.$message = $('<div>')
      .attr('id', 'message-box')
      .text(this.texts.get(this.options.messageKey))
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
};

scout.LoginBox.prototype._resetButtonText = function() {
  this.$button
    .text(this.texts.get('ui.Login'))
    .removeClass('login-error');
};

scout.LoginBox.prototype._onLoginFormSubmit = function(event) {
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
    .done(this._onPostDone.bind(this))
    .fail(this._onPostFail.bind(this));
};

scout.LoginBox.prototype._onPostDone = function(data) {
  // Calculate target URL
  var url = this.options.redirectUrl;
  if (!url) {
    url = (window.location.href || '').trim();
    var prepareRedirectUrlFunc = this.options.prepareRedirectUrlFunc || scout.LoginBox.prepareRedirectUrl;
    // Remove login.html and everything after it from the URL
    url = prepareRedirectUrlFunc(url);
  }

  // Go to target URL
  if (url) {
    window.location.href = url;
  } else {
    window.location.reload();
  }
};

scout.LoginBox.prototype._onPostFail = function(jqXHR, textStatus, errorThrown) {
  // execute delayed to make sure loading animation is visible, otherwise (if it is very fast), it flickers
  setTimeout(function() {
    this.$button
      .setEnabled(true)
      .html('')
      .text(this.texts.get('ui.LoginFailed'))
      .addClass('login-error');
    this.$user
      .val('')
      .focus()
      .one('input.resetLoginError', this._resetButtonText.bind(this));
    this.$password
      .val('')
      .one('input.resetLoginError', this._resetButtonText.bind(this));
  }.bind(this), 300);
};

// ----- Helper functions -----

scout.LoginBox.prepareRedirectUrl = function(url) {
  var urlParts = /^([^?#]*)(\?[^#]*)?(#.*)?$/.exec(url || ''); // $1 = baseUrl, $2 = queryPart, $3 = hashPart
  var filteredBaseUrl = urlParts[1]
    .replace(/login.html$/, '')
    .replace(/login$/, '')
    .replace(/logout$/, '');
  return filteredBaseUrl + (urlParts[2] ? urlParts[2] : '') + (urlParts[3] ? urlParts[3] : '');
};
