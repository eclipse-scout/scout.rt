/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {AbstractConstructor, arrays, Constructor, DoValueMetaData, IdParser, InitModelOf, objects, scout} from '../index';

/**
 * Abstract representation of an ID.
 */
export abstract class Id<TIdValueType, TIdTypeName extends string = string> implements IdModel<TIdValueType, TIdTypeName> {

  declare model: IdModel<TIdValueType, TIdTypeName>;

  value: TIdValueType;
  typeName: TIdTypeName;
  signature: string;

  init(model: InitModelOf<this>) {
    const value = scout.assertParameter('value', model?.value);
    this.typeName = scout.nvl(model.typeName, this.typeName);
    this.signature = model.signature;
    this._initIdValue(value);
  }

  /**
   * Initializes the value of this id based on given input.
   * Implementors should write this.value based on the argument.
   * @param value The typed or raw value of this id.
   */
  protected abstract _initIdValue(value: TIdValueType | string[]);

  protected _toSingleString(value: string | string[]): string {
    if (typeof value === 'string') {
      return value;
    }
    if (value?.length !== 1) {
      throw this._createInvalidIdError(value);
    }
    const first = value[0];
    if (!first) {
      throw this._createInvalidIdError(value);
    }
    return first;
  }

  /**
   * Converts this id's value to its string representation to be used in {@link toUnqualified}.
   * If the id consists of several values, use IdParser.COMPONENT_DELIMITER to separate the values.
   * @returns the string representation of this id.
   */
  protected _toString(): string {
    return this.value + '';
  }

  /**
   * @returns this id converted to its unqualified string representation. Unqualified means only value and signature are part of the result, the typeName is omitted.
   */
  toUnqualified(): string {
    const unqualified = this._toString();
    if (this.signature) {
      return unqualified + IdParser.SIGNATURE_DELIMITER + this.signature;
    }
    return unqualified;
  }

  /**
   * @returns this id converted to its qualified string representation including typeName, value and signature.
   */
  toQualified(): string {
    if (this.typeName) {
      return this.typeName + IdParser.TYPE_NAME_DELIMITER + this.toUnqualified();
    }
    return this.toUnqualified();
  }

  /**
   * Extracts the typeName from the id metadata.
   * Called by reflection e.g. from IdDoNodeSerializer.
   * May be overridden in subclasses to modify the typeName extraction in case it is not the first type argument.
   * @param idMetaData The metadata from which the typeName should be extracted.
   */
  static extractIdTypeName(idMetaData: DoValueMetaData): string {
    return idMetaData?.args?.[0]?.typeName;
  }

  /**
   * Compares this id to the other object. It is considered to be equal if the class, typeName and value are equal (recursively).
   * @see objects.equalsRecursive
   */
  equals(obj: any) {
    if (this === obj) {
      return true;
    }
    if (this.constructor !== obj?.constructor) {
      return false;
    }

    const other = obj as Id<any>;
    return this.typeName === other.typeName
      && objects.equalsRecursive(this.value, other.value);
  }

  protected _createInvalidIdError(value: string[]): Error {
    let rawId = arrays.format(value, IdParser.COMPONENT_DELIMITER);
    return new Error(`Invalid Id value: ${rawId}.`);
  }
}

/**
 * Class decorator for ids. It applies the given id typeName to the instance during constructor time.
 * The typeName value is written to the 'typeName' attribute.
 *
 * @param idTypeName The typeName value to set to the instance
 */
export function idTypeName(idTypeName: string) {
  return <T extends Constructor | AbstractConstructor>(BaseClass: T) => class extends BaseClass {
    constructor(...args: any[]) {
      super(...args);
      Reflect.set(this, 'typeName', idTypeName); // instance
    }
  };
}

export interface IdModel<TIdValueType, TIdTypeName extends string = string> {
  /**
   * The value of the id. May consist of multiple segments.
   */
  value: TIdValueType | string[];
  /**
   * The typeName of the id. May also be set statically using @idTypeName().
   */
  typeName?: TIdTypeName;
  /**
   * Id signature if available.
   */
  signature?: string;
}
