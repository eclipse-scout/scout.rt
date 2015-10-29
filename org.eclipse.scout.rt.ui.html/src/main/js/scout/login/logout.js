/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
    var $container,
      defaultOpts = {
        loginUrl: sessionStorage.getItem('scout:loginUrl') || './',
        texts: {
          'ui.logoutSuccessful': 'Good bye!',
          'ui.loginAgain': 'Login again'
        }
      };
    var options = $.extend({}, defaultOpts, opts);
    var texts = new scout.Texts(options.texts);
    var $box = $('<div>')
      .addClass('box-with-logo small centered')
      .html(scout.strings.nl2br(texts.get('ui.logoutSuccessful')))
      .appendTo($('body'));
    var $buttonBar = $('<div>')
      .addClass('button')
      .appendTo($box);
    $('<button>')
      .text(texts.get('ui.loginAgain'))
      .on('click', loginAgain)
      .appendTo($buttonBar);

    $('noscript').remove(); // cleanup DOM

    // ----- Helper functions -----

    function loginAgain() {
      window.location = options.loginUrl;
    }
  }

};
