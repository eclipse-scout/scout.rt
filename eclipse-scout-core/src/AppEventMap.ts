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
