/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, AppBootstrapOptions, defaultValues, ErrorHandler, InitModelOf, Session, SessionModel} from './index';
import $ from 'jquery';

export class RemoteApp extends App {

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

  protected override _createErrorHandler(opts?: InitModelOf<ErrorHandler>): ErrorHandler {
    opts = $.extend({
      sendError: true
    }, opts);
    return super._createErrorHandler(opts);
  }

  protected override _loadSession($entryPoint: JQuery, options: SessionModel): JQuery.Promise<any> {
    let model = (options || {}) as InitModelOf<Session>;
    model.$entryPoint = $entryPoint;
    let session = this._createSession(model);
    App.get().sessions.push(session);
    return session.start();
  }
}
