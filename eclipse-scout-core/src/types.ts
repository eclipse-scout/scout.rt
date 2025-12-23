/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {BaseDoEntity, scout, Widget} from './index';

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

/**
 * Makes a property required but only if it is not of TType or a super class of TType.
 */
export type RequiredUnlessNotSubclass<TObject, TKey extends keyof TObject, TType> = (TType extends TObject[TKey] ? {} : Pick<TObject, TKey>);

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

/**
 * Marker type to indicate that the value is a {@link JsonValue}.
 *
 * Use this in data object classes ({@link BaseDoEntity}) to prevent the following TS compiler error when creating
 * an instance with {@link scout.create}: `TS2589: Type instantiation is excessively deep and possibly infinite.`
 */
export type AnyJsonValue = any;
