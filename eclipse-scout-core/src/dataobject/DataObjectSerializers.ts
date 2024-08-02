/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Constructor, dates} from '../index';

export interface JsonDeSerializer<TType> {

  canSerialize(value: any, objectType: Constructor): boolean;

  serialize(value: TType, objectType: Constructor): any;

  canDeserialize(value: any, objectType: Constructor): boolean;

  deserialize(value: any, objectType: Constructor): TType;
}

export class DoDateSerializer implements JsonDeSerializer<Date> {

  canSerialize(value: any, objectType: Constructor): boolean {
    return value instanceof Date;
  }

  serialize(value: Date, objectType: Constructor): string {
    return dates.toJsonDate(value, true);
  }

  canDeserialize(value: any, objectType: Constructor): boolean {
    return Date === objectType && typeof value === 'string';
  }

  deserialize(value: string, objectType: Constructor): Date {
    return dates.parseJsonDate(value);
  }
}

// FIXME mvi [js-bookmark] add DeSerializer for IIds?
