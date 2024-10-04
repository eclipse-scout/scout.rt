/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Id, InitModelOf, ObjectType, ObjectWithType, scout, strings} from '../index';

const registry: Map<string, ObjectType<Id<any>>> = new Map();

export class IdParser implements ObjectWithType {

  static ID_TYPENAME_DELIMITER = ':';
  static SIGNATURE_DELIMITER = '###';
  static COMPONENT_DELIMITER = ';';

  objectType: string;

  fromUnQualified<TIdType extends Id<TValueType, TTypeName>, TTypeName extends string, TValueType>(idObjectType: ObjectType<TIdType>, value: string, typeName: string): TIdType {
    if (strings.empty(value)) {
      throw new Error('Id value is mandatory.');
    }
    const signatureSplit = this._splitSignature(value);
    const rawValue = signatureSplit[0];
    const signature = signatureSplit[1];
    const rawIdComponents = rawValue.split(IdParser.COMPONENT_DELIMITER);
    const idModel = {value: rawIdComponents, typeName, signature} as InitModelOf<TIdType>;
    const objectTypeOverwrite = this.getObjectTypeForTypeName(typeName, idObjectType) as ObjectType<TIdType>;
    return scout.create(objectTypeOverwrite, idModel, {ensureUniqueId: false});
  }

  protected _splitSignature(value: string): string[] {
    let splitPos = value.lastIndexOf(IdParser.SIGNATURE_DELIMITER);
    if (splitPos < 0) {
      // no signature found
      return [value, null];
    }
    return [value.substring(0, splitPos), value.substring(splitPos + IdParser.SIGNATURE_DELIMITER.length)];
  }

  fromQualified<TIdType extends Id<TValueType, TTypeName>, TTypeName extends string, TValueType>(idObjectType: ObjectType<TIdType>, qualifiedId: string): TIdType {
    if (!qualifiedId) {
      return null;
    }
    const {id, typeName} = this.stripTypeName(qualifiedId);
    return this.fromUnQualified(idObjectType, id, typeName as TTypeName);
  }

  stripTypeName(qualifiedId: string): { typeName: string; id: string } {
    if (!qualifiedId) {
      return {typeName: null, id: null};
    }
    let firstColonPos = qualifiedId.indexOf(IdParser.ID_TYPENAME_DELIMITER);
    if (firstColonPos < 0) {
      return {
        typeName: null,
        id: qualifiedId
      };
    }
    return {
      typeName: qualifiedId.substring(0, firstColonPos),
      id: qualifiedId.substring(firstColonPos + 1)
    };
  }

  getObjectTypeForTypeName<TValueType, TTypeName extends string>(typeName: TTypeName, defaultObjectType?: ObjectType<Id<TValueType, TTypeName>>): ObjectType<Id<TValueType, TTypeName>> {
    if (!typeName) {
      return defaultObjectType;
    }
    const objectType = registry.get(typeName);
    if (objectType) {
      return objectType as ObjectType<Id<TValueType, TTypeName>>;
    }
    return defaultObjectType;
  }

  static register<TTypeName extends string>(typeName: TTypeName, objectType: ObjectType<Id<any, TTypeName>>) {
    registry.set(typeName, objectType);
  }
}
