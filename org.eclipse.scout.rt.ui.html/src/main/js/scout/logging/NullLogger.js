/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.NullLogger = function() {

};

scout.NullLogger.prototype = {
  trace: function () {
  },
  debug: function () {
  },
  info: function () {
  },
  warn: function () {
  },
  error: function () {
  },
  fatal: function () {
  },
  isEnabledFor: function () {
    return false;
  },
  isTraceEnabled: function () {
    return false;
  },
  isDebugEnabled: function () {
    return false;
  },
  isInfoEnabled: function () {
    return false;
  },
  isWarnEnabled: function () {
    return false;
  },
  isErrorEnabled: function () {
    return false;
  },
  isFatalEnabled: function () {
    return false;
  }
};