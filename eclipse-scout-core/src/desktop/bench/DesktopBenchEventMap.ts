/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
