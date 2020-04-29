/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {App, defaultValues, scout} from './index';
import $ from 'jquery';

export default class RemoteApp extends App {

  constructor() {
    super();
    this.remote = true;
  }

  /**
   * @override
   */
  _doBootstrap(options) {
    return super._doBootstrap(options).concat([
      this._doBootstrapDefaultValues()
    ]);
  }

  _doBootstrapDefaultValues() {
    defaultValues.bootstrap();
  }

  _createErrorHandler() {
    return scout.create('ErrorHandler', {
      sendError: true
    });
  }

  /**
   * @override
   */
  _loadSession($entryPoint, options) {
    options = options || {};
    options.$entryPoint = $entryPoint;
    let session = this._createSession(options);
    App.get().sessions.push(session);
    return session.start();
  }

  _fail(options, error, ...args) {
    $.log.error('App initialization failed', error);
    // Session.js already handled the error -> don't show a message here
    // Reject with original rejection arguments
    return $.rejectedPromise(error, ...args);
  }
}
