/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Id, numbers, scout} from '../index';

/**
 * Id that consists of a single number. Corresponds to LongId in Java.
 */
export class NumberId<TTypeName extends string> extends Id<number, TTypeName> {

  protected override _initIdValue(value: number | string[]) {
    if (typeof value === 'number') {
      this.value = value;
    } else {
      if (value?.length !== 1) {
        throw this._createInvalidIdError(value);
      }
      const id = numbers.ensure(value[0]);
      if (!id) {
        throw this._createInvalidIdError(value);
      }
      this.value = id;
    }
  }

  static of<TTypeName extends string>(value: number, typeName?: TTypeName, signature?: string): NumberId<TTypeName> {
    if (!value) {
      return null;
    }
    return scout.create(NumberId<TTypeName>, {value, typeName, signature});
  }
}
