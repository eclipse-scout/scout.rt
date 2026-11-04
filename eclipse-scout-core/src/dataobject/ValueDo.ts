/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, BinaryResource, scout} from '../index';

/**
 * Wrapper data object for a generic value.
 */
export class ValueDo<T> extends BaseDoEntity {

  static STRING = 'scout.StringValue';
  static INTEGER = 'scout.IntegerValue';
  static LONG = 'scout.LongValue';
  static BOOLEAN = 'scout.BooleanValue';
  static LOCALE = 'scout.LocaleValue';
  static BIG_DECIMAL = 'scout.BigDecimalValue';
  static DATE_TIME_WITH_TIME_ZONE = 'scout.DateTimeWithTimeZoneValue';
  static DATE = 'scout.DateValue';
  static DATE_TIME = 'scout.DateTimeValue';
  static BYTE_ARRAY = 'scout.ByteArrayValue';
  /**
   * See {@link BinaryResource}
   */
  static BINARY_RESOURCE = 'scout.BinaryResourceValue';
  static DO_ENTITY_LIST = 'scout.DoEntityListValue';
  static OBJECT = 'scout.ObjectValue';

  value: T;

  static ofString(value: string): ValueDo<string> {
    return ValueDo.of(ValueDo.STRING, value);
  }

  static ofLong(value: number): ValueDo<number> {
    return ValueDo.of(ValueDo.LONG, value);
  }

  static ofBoolean(value: boolean): ValueDo<boolean> {
    return ValueDo.of(ValueDo.BOOLEAN, value);
  }

  static ofDate(value: Date): ValueDo<Date> {
    return ValueDo.of(ValueDo.DATE, value);
  }

  static ofDateTime(value: Date): ValueDo<Date> {
    return ValueDo.of(ValueDo.DATE_TIME, value);
  }

  static of<T>(typeName: string, value: T): ValueDo<T> {
    return scout.create(ValueDo<T>, {_type: typeName, value: value as any});
  }
}
