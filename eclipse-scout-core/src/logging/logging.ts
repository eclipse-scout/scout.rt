/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {NullLogger, scout, URL} from '../index';
import $ from 'jquery';
import strings from '../util/strings';

const Level = {
  TRACE: 'trace',
  DEBUG: 'debug',
  INFO: 'info',
  WARN: 'warn',
  ERROR: 'error',
  FATAL: 'fatal'
};

const DEFAULT_LEVEL = Level.TRACE;
let initialized = false;
let _appendersToAdd = [];
let showStackTraces = true;

/***
 * Loads log4javascript.min.js if logging is enabled.
 * @param [options] options
 * @param {boolean} options.enabled
 * @param {boolean} options.showPopup
 * @param options.resourceUrl
 * @returns {Promise}
 */
export function bootstrap(options) {
  let location = new URL(),
    logging = location.getParameter('logging'),
    logLevel = location.getParameter('logLevel');

  options = scout.nvl(options, {});

  let enabled = !!(options.enabled || logging),
    showPopup = !!(options.showPopup || logging),
    resourceUrl = strings.nvl(options.resourceUrl);

  $.log = new NullLogger();
  if (!enabled) {
    return $.resolvedPromise();
  }
  if (window.log4javascript) {
    initLog4Javascript(logLevel, showPopup);
    return $.resolvedPromise();
  }

  // If log4javascript is not yet installed, dynamically load the library
  return $.injectScript(resourceUrl + 'log4javascript-1.4.9/log4javascript.js')
    .done(initLog4Javascript.bind(this, logLevel, showPopup));
}

export function initLog4Javascript(logLevel, showPopup) {
  logLevel = scout.nvl(logLevel, DEFAULT_LEVEL);
  log4javascript.setShowStackTraces(showStackTraces);
  let defaultLogger = log4javascript.getDefaultLogger();
  defaultLogger.setLevel(parseLevel(logLevel));
  $.log = defaultLogger;

  initialized = true;

  if (showPopup) {
    // To avoid problems with our CSP rule which prohibits inline scripts we set the useDocumentWrite
    // flag to false, so the console[_uncompressed].html/js is loaded instead.
    defaultLogger.getEffectiveAppenders().forEach(appender => {
      appender.setUseDocumentWrite(false);
    });
  } else {
    // Remove default PopUpAppender (which is the only appender at this point)
    defaultLogger.removeAllAppenders();
  }

  // Add appenders later
  _appendersToAdd.forEach(obj => {
    addAppender(obj.factoryName, obj.options);
  }, this);
  _appendersToAdd = [];
}

export function parseLevel(level) {
  if (!level) {
    return;
  }
  level = level.toLowerCase();
  switch (level) {
    case Level.TRACE:
      return log4javascript.Level.TRACE;
    case Level.DEBUG:
      return log4javascript.Level.DEBUG;
    case Level.INFO:
      return log4javascript.Level.INFO;
    case Level.WARN:
      return log4javascript.Level.WARN;
    case Level.ERROR:
      return log4javascript.Level.ERROR;
    case Level.FATAL:
      return log4javascript.Level.FATAL;
  }
}

export function addAppender(factoryName, options) {
  if (!initialized) {
    _appendersToAdd.push({
      factoryName: factoryName,
      options: options
    });
    return;
  }

  let factory = scout.create(factoryName, options);
  $.log.addAppender(factory.create());
}

export default {
  DEFAULT_LEVEL,
  Level,
  addAppender,
  bootstrap,
  initLog4Javascript,
  initialized,
  parseLevel,
  showStackTraces
};
