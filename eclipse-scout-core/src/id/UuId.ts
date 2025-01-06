/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Id, scout} from '../index';

/**
 * Id that consist of an universally unique identifier (UUID).
 */
export class UuId<TTypeName extends string> extends Id<string, TTypeName> {

  protected override _initIdValue(value: string | string[]) {
    this.value = this._toSingleString(value);
  }

  static of<TTypeName extends string>(value: string, typeName?: TTypeName, signature?: string): UuId<TTypeName> {
    if (!value) {
      return null;
    }
    return scout.create(UuId<TTypeName>, {value, typeName, signature});
  }
}
