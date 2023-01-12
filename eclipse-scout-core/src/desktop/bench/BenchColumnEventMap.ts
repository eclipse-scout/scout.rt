/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BenchColumn, Event, OutlineContent, WidgetEventMap} from '../../index';

export interface BenchColumnViewActivateEvent<B extends BenchColumn = BenchColumn> extends Event<B> {
  view: OutlineContent;
}

export interface BenchColumnViewAddEvent<B extends BenchColumn = BenchColumn> extends Event<B> {
  view: OutlineContent;
}

export interface BenchColumnViewDeactivateEvent<B extends BenchColumn = BenchColumn> extends Event<B> {
  view: OutlineContent;
}

export interface BenchColumnViewRemoveEvent<B extends BenchColumn = BenchColumn> extends Event<B> {
  view: OutlineContent;
}

export interface BenchColumnEventMap extends WidgetEventMap {
  'viewActivate': BenchColumnViewActivateEvent;
  'viewAdd': BenchColumnViewAddEvent;
  'viewDeactivate': BenchColumnViewDeactivateEvent;
  'viewRemove': BenchColumnViewRemoveEvent;
}
