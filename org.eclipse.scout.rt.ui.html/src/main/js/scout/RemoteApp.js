/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.RemoteApp = function() {
  scout.RemoteApp.parent.call(this);
  this.remote = true;
};
scout.inherits(scout.RemoteApp, scout.App);

/**
 * @override
 */
scout.RemoteApp.prototype._doBootstrap = function(options) {
  return [
    scout.device.bootstrap(),
    scout.defaultValues.bootstrap(),
    scout.fonts.bootstrap(options.fonts)
  ];
};

scout.RemoteApp.prototype._createErrorHandler = function() {
  return scout.create('ErrorHandler', {
    sendError: true
  });
};

/**
 * @override
 */
scout.RemoteApp.prototype._loadSession = function($entryPoint, options) {
  options = options || {};
  options.$entryPoint = $entryPoint;
  var session = this._createSession(options);
  session.start();
  return session;
};
