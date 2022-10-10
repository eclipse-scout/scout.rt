/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
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

  _createErrorHandler(opts) {
    opts = $.extend({
      sendError: true
    }, opts);
    return super._createErrorHandler(opts);
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
}
