/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Widget} from './index';

export type Predicate<T> = (obj: T) => boolean;

export type Comparator<T> = (a: T, b: T) => number;

/**
 * Makes some properties in TObject optional.
 */
export type SomePartial<TObject, TKey extends keyof TObject> = Partial<Pick<TObject, TKey>> & Omit<TObject, TKey>;

/**
 * Makes some properties in TObject required.
 */
export type SomeRequired<TObject, TKey extends keyof TObject> = Required<Pick<TObject, TKey>> & TObject;

export type EnumObject<T> = T[keyof T];

export type EmptyObject = Record<string, never>;

export type Primitive = number | string | boolean | symbol | bigint;

export interface Closeable {
  close(): void;
}

export type CloseableWidget = Widget & Closeable;

export interface Copyable {
  copy(): void;
}

export type CopyableWidget = Widget & Copyable;

export interface Abortable {
  abort(): void;
}

export type AbortableWidget = Widget & Abortable;

export type JsonValue = string | number | boolean | JsonObject | JsonValue[];

export interface JsonObject {
  [x: string]: JsonValue;
}

export type JsonValueMapper = (key: string, value: any) => any;
