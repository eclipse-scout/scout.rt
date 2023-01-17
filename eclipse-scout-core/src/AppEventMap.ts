/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
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

export interface AppEventMap extends EventMap {
  'prepare': AppInitEvent;
  'init': AppInitEvent;
  'bootstrap': AppBootstrapEvent;
  'desktopReady': AppDesktopReadyEvent;
  'sessionReady': AppSessionReadyEvent;
}
