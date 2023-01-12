/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, SimpleTabBox, SimpleTabView, WidgetEventMap} from '../index';

export interface SimpleTabBoxViewActivateEvent<TView extends SimpleTabView = SimpleTabView, S extends SimpleTabBox<TView> = SimpleTabBox<TView>> extends Event<S> {
  view: TView;
}

export interface SimpleTabBoxViewAddEvent<TView extends SimpleTabView = SimpleTabView, S extends SimpleTabBox<TView> = SimpleTabBox<TView>> extends Event<S> {
  view: TView;
  siblingView: TView;
}

export interface SimpleTabBoxViewDeactivateEvent<TView extends SimpleTabView = SimpleTabView, S extends SimpleTabBox<TView> = SimpleTabBox<TView>> extends Event<S> {
  view: TView;
}

export interface SimpleTabBoxViewRemoveEvent<TView extends SimpleTabView = SimpleTabView, S extends SimpleTabBox<TView> = SimpleTabBox<TView>> extends Event<S> {
  view: TView;
}

export interface SimpleTabBoxEventMap<TView extends SimpleTabView = SimpleTabView> extends WidgetEventMap {
  'viewActivate': SimpleTabBoxViewActivateEvent<TView>;
  'viewAdd': SimpleTabBoxViewAddEvent<TView>;
  'viewDeactivate': SimpleTabBoxViewDeactivateEvent<TView>;
  'viewRemove': SimpleTabBoxViewRemoveEvent<TView>;
}
