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
import {BenchColumn, Event, WidgetEventMap} from '../../index';
import {OutlineContent} from './DesktopBench';

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

export default interface BenchColumnEventMap extends WidgetEventMap {
  'viewActivate': BenchColumnViewActivateEvent;
  'viewAdd': BenchColumnViewAddEvent;
  'viewDeactivate': BenchColumnViewDeactivateEvent;
  'viewRemove': BenchColumnViewRemoveEvent;
}
