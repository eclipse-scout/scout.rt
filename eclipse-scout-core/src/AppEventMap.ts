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
import {Desktop, Event, EventMap, Session} from './index';
import {AppBootstrapOptions, AppOptions} from './App';

export interface AppInitEvent extends Event {
  options: AppOptions;
}

export interface AppBootstrapEvent extends Event {
  options: AppBootstrapOptions;
}

export interface AppDesktopReadyEvent extends Event {
  desktop: Desktop;
}

export interface AppSessionReadyEvent extends Event {
  session: Session;
}

export default interface AppEventMap extends EventMap {
  'prepare': AppInitEvent;
  'init': AppInitEvent;
  'bootstrap': AppBootstrapEvent;
  'desktopReady': AppDesktopReadyEvent;
  'sessionReady': AppSessionReadyEvent;
}
