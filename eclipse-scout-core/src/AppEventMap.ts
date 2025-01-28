/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, AppBootstrapOptions, AppModel, Desktop, Event, EventMap, Session} from './index';

export interface AppInitEvent<T = App> extends Event<T> {
  options: AppModel;
}

export interface AppBootstrapEvent<T = App> extends Event<T> {
  options: AppBootstrapOptions;
}

export interface AppDesktopReadyEvent<T = App> extends Event<T> {
  desktop: Desktop;
}

export interface AppSessionReadyEvent<T = App> extends Event<T> {
  session: Session;
}

export interface AppFailEvent<T = App> extends Event<T> {
  error: any;
}

export interface AppEventMap extends EventMap {
  'prepare': AppInitEvent;
  'init': AppInitEvent;
  /**
   * This event is triggered after the {@link App} has installed its {@link Extension}s (see {@link App#_installExtensions}).
   * It can be used to install additional {@link Extension}s (see {@link Extension#install}), but be aware that there is no control over the order these {@link Extension}s are installed.
   */
  'installExtensions': Event<App>;
  'bootstrap': AppBootstrapEvent;
  'desktopReady': AppDesktopReadyEvent;
  'sessionReady': AppSessionReadyEvent;
  'fail': AppFailEvent;
}
