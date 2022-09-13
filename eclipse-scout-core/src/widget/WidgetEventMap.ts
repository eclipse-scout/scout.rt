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
import {DisabledStyle, Event, LogicalGrid, PropertyChangeEvent, Widget} from '../index';
import {GlassPaneContribution} from './Widget';
import PropertyEventMap from '../events/PropertyEventMap';

export interface HierarchyChangeEvent<T extends Widget = Widget> extends Event<T> {
  oldParent: Widget;
  parent: Widget;
}

export interface GlassPaneContributionEvent<T extends Widget = Widget> extends Event<T> {
  contribution: GlassPaneContribution;
}

export default interface WidgetEventMap extends PropertyEventMap {
  'init': Event<Widget>;
  'destroy': Event<Widget>;
  'render': Event<Widget>;
  'remove': Event<Widget>;
  'removing': Event<Widget>;
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
