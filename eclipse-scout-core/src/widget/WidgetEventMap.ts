/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DisabledStyle, Event, GlassPaneContribution, LogicalGrid, PropertyChangeEvent, PropertyEventMap, Widget} from '../index';

export interface HierarchyChangeEvent<T = Widget> extends Event<T> {
  oldParent: Widget;
  parent: Widget;
}

export interface GlassPaneContributionEvent<T = Widget> extends Event<T> {
  contribution: GlassPaneContribution;
}

export interface WidgetEventMap extends PropertyEventMap {
  'init': Event;
  'destroy': Event;
  'render': Event;
  'remove': Event;
  'removing': Event;
  'glassPaneContributionAdded': GlassPaneContributionEvent;
  'glassPaneContributionRemoved': GlassPaneContributionEvent;
  'hierarchyChange': HierarchyChangeEvent;
  'propertyChange:enabled': PropertyChangeEvent<boolean>;
  'propertyChange:enabledComputed': PropertyChangeEvent<boolean>;
  'propertyChange:trackFocus': PropertyChangeEvent<boolean>;
  'propertyChange:scrollTop': PropertyChangeEvent<number>;
  'propertyChange:scrollLeft': PropertyChangeEvent<number>;
  'propertyChange:inheritAccessibility': PropertyChangeEvent<boolean>;
  'propertyChange:disabledStyle': PropertyChangeEvent<DisabledStyle>;
  'propertyChange:visible': PropertyChangeEvent<boolean>;
  'propertyChange:focused': PropertyChangeEvent<boolean>;
  'propertyChange:cssClass': PropertyChangeEvent<string>;
  'propertyChange:loading': PropertyChangeEvent<boolean>;
  'propertyChange:logicalGrid': PropertyChangeEvent<LogicalGrid>;
}
