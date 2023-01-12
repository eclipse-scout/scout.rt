/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, PropertyChangeEvent, TagBar, WidgetEventMap} from '../index';

export interface TagBarTagClickEvent<T = TagBar> extends Event<T> {
  tag: string;
}

export interface TagBarTagRemoveEvent<T = TagBar> extends Event<T> {
  tag: string;
  $tag: JQuery;
}

export interface TagBarEventMap extends WidgetEventMap {
  'tagClick': TagBarTagClickEvent;
  'tagRemove': TagBarTagRemoveEvent;
  'propertyChange:clickable': PropertyChangeEvent<boolean>;
  'propertyChange:overflowVisible': PropertyChangeEvent<boolean>;
  'propertyChange:tags': PropertyChangeEvent<string[]>;

}
