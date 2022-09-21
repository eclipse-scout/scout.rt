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

export type Predicate<T> = (obj: T) => boolean;

export type Comparator<T> = (a: T, b: T) => number;

// Type that makes some properties optional and some required.
export type PartialAndRequired<T, OPTIONAL extends keyof T, REQUIRED extends keyof T> = Omit<T, OPTIONAL | REQUIRED> & Partial<Pick<T, OPTIONAL>> & Required<Pick<T, REQUIRED>>;

export type Optional<T, K extends keyof T> = Pick<Partial<T>, K> & Omit<T, K>;

export type EnumObject<TYPE> = TYPE[keyof TYPE];

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
