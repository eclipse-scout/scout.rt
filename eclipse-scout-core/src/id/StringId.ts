/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Id, IdParser, scout} from '../index';

export class StringId<TTypeName extends string> extends Id<string, TTypeName> {

  protected override _initIdValue(value: string | string[]) {
    if (typeof value === 'string') {
      this.value = value;
    } else {
      const invalidStringId = () => new Error('Invalid StringId: ' + arrays.format(value, IdParser.COMPONENT_DELIMITER));
      if (value?.length !== 1) {
        throw invalidStringId();
      }
      const id = value[0];
      if (!id) {
        throw invalidStringId();
      }
      this.value = id;
    }
  }

  static of<TTypeName extends string>(value: string, typeName?: TTypeName, signature?: string): StringId<TTypeName> {
    return scout.create(StringId<TTypeName>, {value, typeName, signature}, {ensureUniqueId: false});
  }
}
