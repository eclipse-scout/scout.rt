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
  showStackTraces: true,

  /***
   * Loads log4javascript.min.js if logging is enabled.
   *
   * @returns {Promise}
   */
  bootstrap: function(options) {
    var location = new scout.URL(),
      logging = location.getParameter('logging'),
      logLevel = location.getParameter('logLevel');

    options = scout.nvl(options, {});

    var enabled = !!(options.enabled || logging),
      showPopup = !!(options.showPopup || logging);

    $.log = new scout.NullLogger();
    if (!enabled) {
      return $.resolvedPromise();
    }
    if (window.log4javascript) {
      this.initLog4Javascript(logLevel, showPopup);
      return $.resolvedPromise();
    }

    // If log4javascript is not yet installed, dynamically load the library
    return $.injectScript('res/log4javascript.js')
      .done(this.initLog4Javascript.bind(this, logLevel, showPopup));
  },

  initLog4Javascript: function(logLevel, showPopup) {
    logLevel = scout.nvl(logLevel, scout.logging.DEFAULT_LEVEL);
    log4javascript.setShowStackTraces(this.showStackTraces);
    var defaultLogger = log4javascript.getDefaultLogger();
    defaultLogger.setLevel(this.parseLevel(logLevel));
    $.log = defaultLogger;

    this.initialized = true;

    if (showPopup) {
      // To avoid problems with our CSP rule which prohibits inline scripts we set the useDocumentWrite
      // flag to false, so the console_uncompressed.html/js is loaded instead.
      defaultLogger.getEffectiveAppenders().forEach(function(appender) {
        appender.setUseDocumentWrite(false);
      });
    } else {
      // Remove default PopUpAppender (which is the only appender at this point)
      defaultLogger.removeAllAppenders();
    }

    // Add appenders later
    this._appendersToAdd.forEach(function(obj) {
      this.addAppender(obj.factoryName, obj.options);
    }, this);
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
