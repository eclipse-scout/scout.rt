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

  /**
   * opts:
   * - loginUrl: URL to redirect after login again button click
   * - logoUrl: default points to 'res/logo.png'
   */
  init: function(opts) {
    var deferreds = this._bootstrap();
    $.when.apply($, deferreds)
      .done(this._init.bind(this, opts));
  },

  /**
   * Executes the default bootstrap functions and returns an array of deferred objects.<p>
   * The actual startup begins only when every of these deferred objects are completed.
   * This gives the possibility to dynamically load additional scripts or files which are mandatory for a successful startup.
   * The individual bootstrap functions may return null or undefined, a single deferred or multiple deferreds as an array.
   */
  _bootstrap : function() {
    var deferredValues = [
      scout.logging.bootstrap()
    ];

    var deferreds = [];
    deferredValues.forEach(function(value) {
      if (Array.isArray(value)) {
        deferreds.concat(value);
      } else if (value) {
        deferreds.push(value);
      }
    });
    return deferreds;
  },

  /**
   * Initializes login box
   */
  _init : function(options) {
    options = options || {};
    options.texts = $.extend({}, scout.texts.readFromDOM(), options.texts);

    scout.prepareDOM();
    scout.objectFactory.init();

    var logoutBox = scout.create('LogoutBox', options);
    logoutBox.render($('body'));
  }
};
