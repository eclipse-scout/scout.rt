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
scout.logout = {

  init: function(opts) {
    var logoUrl, options, texts, $container,
      $parent = $('body'),
      defaultOpts = {
        loginUrl: sessionStorage.getItem('scout:loginUrl') || './',
        logoUrl: 'res/logo.png',
        texts: {
          'ui.LogoutSuccessful': 'Good bye!',
          'ui.LoginAgain': 'Login again'
        }
      };
    options = $.extend({}, defaultOpts, opts);
    texts = new scout.Texts(options.texts);
    logoUrl = options.logoUrl;
    this.$container = $('<div>')
      .addClass('login-box box-with-logo')
      .appendTo($parent);

    this.$wrapper = $('<div>')
      .addClass('wrapper')
      .appendTo(this.$container);

    this.$content = $('<div>')
      .addClass('login-box-content box-with-logo-content small centered')
      .appendTo(this.$wrapper);

    if (logoUrl) {
      this.$header = this.$content.appendDiv('header');
      this.$logo = $('<img>')
        .addClass('logo')
        .attr('src', logoUrl)
        .appendTo(this.$header);
    }

    this.$content.appendDiv().html(scout.strings.nl2br(texts.get('ui.LogoutSuccessful')));

    this.$buttonBar = $('<div>')
      .addClass('button-bar')
      .appendTo(this.$content);
    $('<button>')
      .addClass('button')
      .text(texts.get('ui.LoginAgain'))
      .on('click', loginAgain)
      .appendTo(this.$buttonBar);

    scout.prepareDOM();

    // ----- Helper functions -----

    function loginAgain() {
      window.location = options.loginUrl;
    }
  }

};
