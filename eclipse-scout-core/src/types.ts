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

import {Widget} from './index';
import {ObjectType} from './ObjectFactory';

export type Predicate<T> = (obj: T) => boolean;

export type Comparator<T> = (a: T, b: T) => number;

export type RefModel<TModel extends { parent?: any; objectType?: ObjectType<any> }> = Omit<SomeRequired<TModel, 'objectType'>, 'parent'>;

export type Optional<TObject, TKey extends keyof TObject> = Partial<Pick<TObject, TKey>> & Omit<TObject, TKey>;

export type SomeRequired<TObject, TKey extends keyof TObject> = Required<Pick<TObject, TKey>> & TObject;

export type EnumObject<T> = T[keyof T];

export type EmptyObject = Record<string, never>;

export type Primitive = number | string | boolean | symbol | bigint;

export type Closeable = { close(): void };

export type CloseableWidget = Widget & Closeable;

export type Copyable = { copy(): void };

export type CopyableWidget = Widget & Copyable;

export type Abortable = Closeable & { abort(): void };

export type AbortableWidget = Widget & Abortable;

/**
 * @deprecated See https://developer.mozilla.org/en-US/docs/Web/API/Element/mousewheel_event
 */
export type OldWheelEvent = WheelEvent & {
  /**
   * @deprecated Use {@link WheelEvent.deltaX} and {@link WheelEvent.deltaMode} instead. See https://developer.mozilla.org/en-US/docs/Web/API/Element/mousewheel_event
   *
   * Returns an integer (32-bit) representing the distance in pixels.
   */
  wheelDelta?: number;

  /**
   * @deprecated Use {@link WheelEvent.deltaX} and {@link WheelEvent.deltaMode} instead. See https://developer.mozilla.org/en-US/docs/Web/API/Element/mousewheel_event
   *
   * Returns an integer representing the horizontal scroll amount.
   */
  wheelDeltaX?: number;

  /**
   * @deprecated Use {@link WheelEvent.deltaY} and {@link WheelEvent.deltaMode} instead. See https://developer.mozilla.org/en-US/docs/Web/API/Element/mousewheel_event
   *
   * Returns an integer representing the vertical scroll amount.
   */
  wheelDeltaY?: number;
};
