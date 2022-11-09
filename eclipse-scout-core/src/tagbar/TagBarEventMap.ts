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
