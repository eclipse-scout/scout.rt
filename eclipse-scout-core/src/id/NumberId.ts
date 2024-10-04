/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Id, IdParser, numbers, scout} from '../index';

/**
 * LongId
 */
export class NumberId<TTypeName extends string> extends Id<number, TTypeName> {

  protected override _initIdValue(value: number | string[]) {
    if (typeof value === 'number') {
      this.value = value;
    } else {
      const invalidNumberId = () => new Error('Invalid NumberId: ' + arrays.format(value, IdParser.COMPONENT_DELIMITER));
      if (value?.length !== 1) {
        throw invalidNumberId();
      }
      const id = numbers.ensure(value[0]);
      if (!id) {
        throw invalidNumberId();
      }
      this.value = id;
    }
  }

  static of<TTypeName extends string>(value: number, typeName?: TTypeName, signature?: string): NumberId<TTypeName> {
    return scout.create(NumberId<TTypeName>, {value, typeName, signature}, {ensureUniqueId: false});
  }
}
