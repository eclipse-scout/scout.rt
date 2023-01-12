/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, Splitter, WidgetEventMap} from '../index';

export interface SplitterMoveEvent<S extends Splitter = Splitter> extends Event<S> {
  position: number;
}

export interface SplitterMoveEndEvent<S extends Splitter = Splitter> extends Event<S> {
  position: number;
}

export interface SplitterMoveStartEvent<S extends Splitter = Splitter> extends Event<S> {
  position: number;
}

export interface SplitterPositionChangeEvent<S extends Splitter = Splitter> extends Event<S> {
  position: number;
}

export interface SplitterEventMap extends WidgetEventMap {
  'move': SplitterMoveEvent;
  'moveEnd': SplitterMoveEndEvent;
  'moveStart': SplitterMoveStartEvent;
  'positionChange': SplitterPositionChangeEvent;
}
