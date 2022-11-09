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
