/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Id, IdParser, scout} from '../index';

/**
 * Id that consists of a single string value.
 * The value must not contain one of the delimiters defined in {@link IdParser}:
 * * {@link IdParser.TYPE_NAME_DELIMITER}
 * * {@link IdParser.COMPONENT_DELIMITER}
 * * {@link IdParser.SIGNATURE_DELIMITER}
 */
export class StringId<TTypeName extends string> extends Id<string, TTypeName> {

  protected override _initIdValue(value: string | string[]) {
    this.value = this._toSingleString(value);
  }

  static of<TTypeName extends string>(value: string, typeName?: TTypeName, signature?: string): StringId<TTypeName> {
    if (!value) {
      return null;
    }
    return scout.create(StringId<TTypeName>, {value, typeName, signature});
  }
}
