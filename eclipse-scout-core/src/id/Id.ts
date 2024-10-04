/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Constructor, dates, DoValueMetaData, IdParser, InitModelOf, ObjectModel, objects, ObjectWithType, scout} from '../index';

/**
 * Abstract representation of an ID.
 */
export abstract class Id<TIdValueType, TIdTypeName extends string = string> implements ObjectWithType {

  declare model: IdModel<TIdValueType, TIdTypeName>;

  value: TIdValueType;
  typeName: TIdTypeName;
  signature: string;
  objectType: string;

  init(model: InitModelOf<this>) {
    const value = scout.assertParameter('value', model?.value);
    this.typeName = scout.nvl(model.typeName, this.typeName);
    this.signature = model.signature;
    this._initIdValue(value);
  }

  protected abstract _initIdValue(value: TIdValueType | string[]);

  protected _toString(): string {
    return this.value + '';
  }

  toUnqualified(): string {
    const unqualified = this._toString();
    if (this.signature) {
      return unqualified + IdParser.SIGNATURE_DELIMITER + this.signature;
    }
    return unqualified;
  }

  toQualified(): string {
    if (this.typeName) {
      return this.typeName + IdParser.ID_TYPENAME_DELIMITER + this.toUnqualified();
    }
    return this.toUnqualified();
  }

  static parseTypeName(idMetaData: DoValueMetaData): string {
    let firstTypeArg = idMetaData?.args?.length ? idMetaData.args[0] : null;
    if (!firstTypeArg) {
      return null;
    }
    return firstTypeArg.typeName;
  }

  equals(obj: any) {
    if (this === obj) {
      return true;
    }
    if (this.constructor !== obj?.constructor) {
      return false;
    }

    const other = obj as Id<any>;
    return this._equals(this.value, other.value);
  }

  protected _equals(a: any, b: any): boolean {
    if (objects.isArray(a) && objects.isArray(b)) {
      if (a.length !== b.length) {
        return false;
      }
      for (let i = 0; i < a.length; i++) {
        if (!this._equals(a[i], b[i])) {
          return false;
        }
      }
      return true;
    }

    if (a instanceof Date && b instanceof Date) {
      return dates.equals(a, b);
    }
    return objects.equals(a, b);
  }
}

export function idTypeName(idTypeName: string) {
  return <T extends Constructor>(BaseClass: T) => class extends BaseClass {
    constructor(...args: any[]) {
      super(...args);
      Reflect.set(this, 'typeName', idTypeName); // instance
    }
  };
}

export interface IdModel<TIdValueType, TIdTypeName extends string = string> extends ObjectModel<Id<TIdValueType, TIdTypeName>> {
  value: TIdValueType | string[];
  typeName?: TIdTypeName;
  signature?: string;
}
