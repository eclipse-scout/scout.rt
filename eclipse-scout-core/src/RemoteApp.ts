/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, AppBootstrapOptions, config, defaultValues, ErrorHandler, InitModelOf, Session, SessionModel} from './index';
import $ from 'jquery';

export class RemoteApp extends App {

  constructor() {
    super();
    this.remote = true;
  }

  protected override _defaultBootstrappers(options: AppBootstrapOptions): (() => JQuery.Promise<void>)[] {
    return super._defaultBootstrappers(options).concat(
      this._defaultValuesBootstrapper(),
      this._configPropertiesBootstrapper(options)
    );
  }

  protected _configPropertiesBootstrapper(options: AppBootstrapOptions): () => JQuery.Promise<void> {
    if (options.configUrl) {
      return null; // custom URL has been provided. Boostrap using this URL already in queue.
    }
    // no custom URL available: use default bootstrap for main system.
    return config.bootstrapSystem.bind(config);
  }

  protected _defaultValuesBootstrapper(): () => JQuery.Promise<void> {
    return defaultValues.bootstrap.bind(defaultValues);
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
    this.sessions.push(session);
    return session.start();
  }
}
