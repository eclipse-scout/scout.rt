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
import {App, defaultValues, ErrorHandler, SessionModel} from './index';
import $ from 'jquery';
import {AppBootstrapOptions} from './App';
import {ErrorHandlerOptions} from './ErrorHandler';

export default class RemoteApp extends App {

  constructor() {
    super();
    this.remote = true;
  }

  protected override _doBootstrap(options: AppBootstrapOptions): Array<JQuery.Promise<any> | JQuery.Promise<any>[]> {
    return super._doBootstrap(options).concat([
      this._doBootstrapDefaultValues()
    ]);
  }

  protected _doBootstrapDefaultValues(): JQuery.Promise<void> {
    return defaultValues.bootstrap();
  }

  protected override _createErrorHandler(opts?: ErrorHandlerOptions): ErrorHandler {
    opts = $.extend({
      sendError: true
    }, opts);
    return super._createErrorHandler(opts);
  }

  protected override _loadSession($entryPoint: JQuery, options: Omit<SessionModel, '$entryPoint'>): JQuery.Promise<any> {
    let model = (options || {}) as SessionModel;
    model.$entryPoint = $entryPoint;
    let session = this._createSession(model);
    App.get().sessions.push(session);
    return session.start();
  }
}
