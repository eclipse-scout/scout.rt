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
/* global log4javascript */
scout.logging = {

  DEFAULT_LEVEL: 'trace',
  initialized: false,
  _appendersToAdd: [],

  /***
   * Loads log4javascript.min.js if logging is enabled.
   *
   * @returns {$.Deferred}
   */
  bootstrap: function() {
    var location = new scout.URL(),
      enabled = location.getParameter('logging'),
      logLevel = location.getParameter('logLevel');

    $.log = new scout.NullLogger();
    if (!enabled) {
      return $.resolvedPromise();
    }
    if (window.log4javascript) {
      this.initLog4Javascript(logLevel);
      return $.resolvedPromise();
    }

    // If log4javascript is not yet installed, dynamically load the library
    return $.injectScript('res/log4javascript.js')
      .done(this.initLog4Javascript.bind(this, logLevel));
  },

  initLog4Javascript: function(logLevel) {
    logLevel = scout.nvl(logLevel, scout.logging.DEFAULT_LEVEL);
    var defaultLogger = log4javascript.getDefaultLogger();
    defaultLogger.setLevel(this.parseLevel(logLevel));
    $.log = defaultLogger;
    this.initialized = true;

    // Add appenders later
    this._appendersToAdd.forEach(function(obj) {
      this.addAppender(obj.factoryName, obj.options);
    });
    this._appendersToAdd = [];
  },

  parseLevel: function(level) {
    if (!level) {
      return;
    }
    level = level.toLowerCase();
    switch (level) {
      case 'trace':
        return log4javascript.Level.TRACE;
      case 'debug':
        return log4javascript.Level.DEBUG;
      case 'info':
        return log4javascript.Level.INFO;
      case 'warn':
        return log4javascript.Level.WARN;
      case 'error':
        return log4javascript.Level.ERROR;
      case 'fatal':
        return log4javascript.Level.FATAL;
    }
  },

  addAppender: function(factoryName, options) {
    if (!this.initialized) {
      this._appendersToAdd.push({
        factoryName: factoryName,
        options: options
      });
      return;
    }

    var factory = scout.create(factoryName, options);
    $.log.addAppender(factory.create());
  }

};
