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
import {DesktopBench, Event, Outline, OutlineContent, PropertyChangeEvent, WidgetEventMap} from '../../index';

export interface DesktopBenchEventMap extends WidgetEventMap {
  'viewActivate': DesktopBenchViewActivateEvent;
  'viewAdd': DesktopBenchViewAddEvent;
  'viewDeactivate': DesktopBenchViewDeactivateEvent;
  'viewRemove': DesktopBenchViewRemoveEvent;
  'propertyChange:navigationHandleVisible': PropertyChangeEvent<boolean>;
  'propertyChange:outline': PropertyChangeEvent<Outline>;
  'propertyChange:outlineContent': PropertyChangeEvent<OutlineContent>;
  'propertyChange:outlineContentVisible': PropertyChangeEvent<boolean>;
}

export interface DesktopBenchViewActivateEvent<D extends DesktopBench = DesktopBench> extends Event<D> {
  view: OutlineContent;
}

export interface DesktopBenchViewAddEvent<D extends DesktopBench = DesktopBench> extends Event<D> {
  view: OutlineContent;
}

export interface DesktopBenchViewDeactivateEvent<D extends DesktopBench = DesktopBench> extends Event<D> {
  view: OutlineContent;
}

export interface DesktopBenchViewRemoveEvent<D extends DesktopBench = DesktopBench> extends Event<D> {
  view: OutlineContent;
}
