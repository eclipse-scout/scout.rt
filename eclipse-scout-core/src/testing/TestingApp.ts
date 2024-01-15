/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, InitModelOf, RemoteApp, Session} from '../index';

export class TestingApp extends RemoteApp {

  protected override _defaultValuesBootrapper(): () => JQuery.Promise<void> {
    // nop for testing
    return null;
  }

  protected override _installErrorHandler() {
    // nop for testing
    // otherwise, it might overwrite the global error handler of Jasmine which will then not be notified about failing specs.
  }

  override _createSession(options: InitModelOf<Session>): Session {
    return super._createSession(options);
  }

  static set(newApp: App) {
    App._set(newApp);
  }
}
