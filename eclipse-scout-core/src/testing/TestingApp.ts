/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, AppBootstrapOptions, Device, InitModelOf, RemoteApp, Session} from '../index';

export class TestingApp extends RemoteApp {

  protected override _defaultValuesBootstrapper(): () => Promise<void> {
    // nop for testing
    return null;
  }

  protected override _installErrorHandler() {
    // Don't install the regular error handlers for testing,
    // otherwise, they might overwrite the global error handlers of Jasmine which will then not be notified about failing specs.

    // Jasmine registers its 'unhandledrejection' listener before any spec code runs, so a listener added here cannot stop it.
    // Instead, the listener registered by karma-jasmine-scout (unhandledRejectionFilter.js, loaded before jasmine-core) consults this filter.
    (window as JasmineScoutWindow).jasmineScoutUnhandledRejectionFilter = this._onUnhandledRejection.bind(this);
  }

  override _createSession(options: InitModelOf<Session>): Session {
    return super._createSession(options);
  }

  protected override _defaultBootstrappers(options: AppBootstrapOptions): (() => Promise<void>)[] {
    return [Device.get().bootstrap.bind(Device.get())];
  }

  static set(newApp: App) {
    App._set(newApp);
  }

  protected _onUnhandledRejection(event: PromiseRejectionEvent) {
    const reason = event.reason;
    if (this.errorHandler.isIgnorableRejection(reason)) {
      // Expected cancellation -> must not fail the spec
      return true;
    }
    console.error('Unhandled promise rejection details', event.reason, event.promise);
    return false;
  }
}

type JasmineScoutWindow = Window & {
  /**
   * Called by the 'unhandledrejection' listener of karma-jasmine-scout, which runs before Jasmine's listener.
   * If it returns true, the rejection is not reported to Jasmine.
   */
  jasmineScoutUnhandledRejectionFilter?: (event: PromiseRejectionEvent) => boolean;
};
