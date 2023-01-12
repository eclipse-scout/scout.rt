/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {NullLogger, ObjectType, scout, strings, URL} from '../index';
import $ from 'jquery';

declare global {
  let log4javascript: any;
}

export interface Logger {
  trace(...logArgs: any[]): void;

  debug(...logArgs: any[]): void;

  info(...logArgs: any[]): void;

  warn(...logArgs: any[]): void;

  error(...logArgs: any[]): void;

  fatal(...logArgs: any[]): void;

  isEnabledFor(): boolean;

  isTraceEnabled(): boolean;

  isDebugEnabled(): boolean;

  isInfoEnabled(): boolean;

  isWarnEnabled(): boolean;

  isErrorEnabled(): boolean;

  isFatalEnabled(): boolean;

  addAppender?(appender: LogAppender);

  removeAppender?(appender: LogAppender);
}

export enum LogLevel {
  TRACE = 'trace',
  DEBUG = 'debug',
  INFO = 'info',
  WARN = 'warn',
  ERROR = 'error',
  FATAL = 'fatal'
}

export interface LoggingOptions {
  enabled?: boolean;
  showPopup?: boolean;
  resourceUrl?: string;
}

export interface LogAppender {
  append(loggingEvent: LoggingEvent);
}

export interface LoggingEvent {
  logger: Logger;
  timeStamp: Date;
  timeStampInMilliseconds: number;
  timeStampInSeconds: number;
  milliseconds: number;
  level: LogLevel;
  messages: string[];
  exception: any[];

  getThrowableStrRep(): string;

  getCombinedMessages(): string;

  toString(): string;
}

export const logging = {
  /** @internal */
  _appendersToAdd: [] as { factoryName: { new(model?: object) } | string; options?: object }[],
  DEFAULT_LEVEL: LogLevel.TRACE,
  initialized: false,
  showStackTraces: true,

  /***
   * Loads log4javascript.min.js if logging is enabled.
   */
  bootstrap(options?: LoggingOptions): JQuery.Promise<JQuery> {
    let location = new URL(),
      loggingParam = location.getParameter('logging'),
      logLevelParam = location.getParameter('logLevel') as string;

    options = scout.nvl(options, {});

    let enabled = !!(options.enabled || loggingParam),
      showPopup = !!(options.showPopup || loggingParam),
      resourceUrl = strings.nvl(options.resourceUrl);

    $.log = new NullLogger();
    if (!enabled) {
      return $.resolvedPromise();
    }
    if (log4javascript) {
      logging.initLog4Javascript(logLevelParam, showPopup);
      return $.resolvedPromise();
    }

    // If log4javascript is not yet installed, dynamically load the library
    return $.injectScript(resourceUrl + 'log4javascript-1.4.9/log4javascript.js')
      .done(logging.initLog4Javascript.bind(this, logLevelParam, showPopup));
  },

  initLog4Javascript(logLevel?: string, showPopup?: boolean) {
    logLevel = scout.nvl(logLevel, logging.DEFAULT_LEVEL);
    log4javascript.setShowStackTraces(logging.showStackTraces);
    let defaultLogger = log4javascript.getDefaultLogger();
    defaultLogger.setLevel(logging.parseLevel(logLevel));
    $.log = defaultLogger;

    logging.initialized = true;

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
    logging._appendersToAdd.forEach(obj => {
      logging.addAppender(obj.factoryName, obj.options);
    });
    logging._appendersToAdd = [];
  },

  parseLevel(level: string): LogLevel {
    if (!level) {
      return;
    }
    level = level.toLowerCase();
    switch (level) {
      case LogLevel.TRACE:
        return log4javascript.Level.TRACE;
      case LogLevel.DEBUG:
        return log4javascript.Level.DEBUG;
      case LogLevel.INFO:
        return log4javascript.Level.INFO;
      case LogLevel.WARN:
        return log4javascript.Level.WARN;
      case LogLevel.ERROR:
        return log4javascript.Level.ERROR;
      case LogLevel.FATAL:
        return log4javascript.Level.FATAL;
    }
  },

  addAppender(factoryName: ObjectType<any>, options?: object) {
    if (!logging.initialized) {
      logging._appendersToAdd.push({
        factoryName: factoryName,
        options: options
      });
      return;
    }

    let factory = scout.create(factoryName, options);
    $.log.addAppender(factory.create());
  }
};
