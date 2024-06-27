/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Event, HybridActionContextElement, HybridManager, PropertyChangeEvent, Widget, WidgetEventMap} from '../../index';

export interface HybridActionEvent<TObject = object, T = HybridManager> extends Event<T> {
  data: {
    id: string;
    actionType: string;
    contextElement: HybridActionContextElement;
    data: TObject;
  };
}

export interface HybridActionEndEvent<TObject = object, T = HybridManager> extends Event<T> {
  data: TObject;
}

export interface HybridManagerWidgetAddEvent<TWidget = Widget, T = HybridManager> extends Event<T> {
  widget: TWidget;
}

export interface HybridManagerWidgetRemoveEvent<TWidget = Widget, T = HybridManager> extends Event<T> {
  widget: TWidget;
}

export interface HybridManagerEventMap extends WidgetEventMap {
  'hybridAction': HybridActionEvent;
  'hybridActionEnd': HybridActionEndEvent;
  'widgetAdd': HybridManagerWidgetAddEvent;
  'widgetRemove': HybridManagerWidgetRemoveEvent;
  'propertyChange:widgets': PropertyChangeEvent<Record<string, Widget>>;
}
