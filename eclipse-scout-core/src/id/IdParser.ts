/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Id, InitModelOf, ObjectFactory, ObjectType, ObjectWithType, scout, strings} from '../index';

/**
 * Class to parse {@link Id} instances from their string representations.
 * Use IdParser.get() to get the singleton instance.
 *
 * @see Id
 * @see IdCodec.java
 */
export class IdParser implements ObjectWithType {

  static TYPE_NAME_DELIMITER = ':';
  static SIGNATURE_DELIMITER = '###';
  static COMPONENT_DELIMITER = ';';

  protected static _INSTANCE: IdParser; // singleton instance created on first use. Access using IdParser.get()

  objectType: string;
  protected _registry: Map<string, ObjectType<Id<any>>>;

  constructor() {
    this._registry = new Map();

    // initialize registry with known idTypeName -> IdClass mappings
    ObjectFactory.get()
      .getSubClassesOf(Id)
      .forEach(IdClass => {
        const tmpInstance = new IdClass();
        if (tmpInstance.typeName) {
          this.registerIdTypeName(tmpInstance.typeName, IdClass);
        }
      });
  }

  /**
   * Parses the given id value string into an id instance of given objectType. Automatically detects if the id is qualified or unqualified based on content.
   * @param idObjectType The id type to create. Mandatory in case there is no override registered (see {@link register}).
   * @param value The raw string to parse (mandatory).
   * @param typeNameProvider An optional provider for the typeName. Called in case the id is unqualified. Must return the corresponding typeName to use.
   */
  parse<TIdType extends Id<TValueType, TTypeName>, TTypeName extends string, TValueType>(idObjectType: ObjectType<TIdType>, value: string, typeNameProvider?: () => string): TIdType {
    if (strings.empty(value)) {
      throw new Error('Id value is mandatory.');
    }
    const isQualified = strings.contains(value, IdParser.TYPE_NAME_DELIMITER);
    if (isQualified) {
      return this.fromQualified(idObjectType, value);
    }
    const typeName = typeNameProvider ? typeNameProvider() : null;
    return this.fromUnqualified(idObjectType, value, typeName);
  }

  /**
   * Parses the given unqualified raw id string into an id instance of given objectType and typeName.
   * @param idObjectType The id type to create. Mandatory in case there is no override registered (see {@link register}).
   * @param value The raw string to parse (mandatory).
   * @param typeName The typeName of the id.
   * @returns the created id holding the given value.
   */
  fromUnqualified<TIdType extends Id<TValueType, TTypeName>, TTypeName extends string, TValueType>(idObjectType: ObjectType<TIdType>, value: string, typeName: string): TIdType {
    if (strings.empty(value)) {
      throw new Error('Id value is mandatory.');
    }
    const signatureSplit = this._splitSignature(value);
    const rawValue = signatureSplit[0];
    const signature = signatureSplit[1];
    const rawIdComponents = rawValue.split(IdParser.COMPONENT_DELIMITER);
    const idModel = {value: rawIdComponents, typeName, signature} as InitModelOf<TIdType>;
    const objectTypeOverwrite = this.getObjectTypeForTypeName(typeName, idObjectType) as ObjectType<TIdType>;
    return scout.create(objectTypeOverwrite, idModel);
  }

  protected _splitSignature(value: string): string[] {
    let splitPos = value.lastIndexOf(IdParser.SIGNATURE_DELIMITER);
    if (splitPos < 0) {
      // no signature found
      return [value, null];
    }
    return [value.substring(0, splitPos), value.substring(splitPos + IdParser.SIGNATURE_DELIMITER.length)];
  }

  /**
   * Parses the given qualified raw id string into an id instance of given objectType.
   * @param idObjectType The id type to create.
   * @param qualifiedId The raw string to parse.
   * @returns the created id holding the given value.
   */
  fromQualified<TIdType extends Id<TValueType, TTypeName>, TTypeName extends string, TValueType>(idObjectType: ObjectType<TIdType>, qualifiedId: string): TIdType {
    if (!qualifiedId) {
      return null;
    }
    const {id, typeName} = this.splitOnTypeName(qualifiedId);
    return this.fromUnqualified(idObjectType, id, typeName);
  }

  /**
   * Splits the qualified raw id string into typeName and the remaining part (values and signature).
   * @param qualifiedId The id to split.
   * @returns typeName and id part (both may be null).
   */
  splitOnTypeName(qualifiedId: string): { typeName: string; id: string } {
    if (!qualifiedId) {
      return {typeName: null, id: null};
    }
    let firstColonPos = qualifiedId.indexOf(IdParser.TYPE_NAME_DELIMITER);
    if (firstColonPos < 0) {
      return {
        typeName: null,
        id: qualifiedId
      };
    }
    return {
      typeName: qualifiedId.substring(0, firstColonPos),
      id: qualifiedId.substring(firstColonPos + IdParser.TYPE_NAME_DELIMITER.length)
    };
  }

  /**
   * @param typeName The typeName for which the objectType should be returned.
   * @param defaultObjectType The default to use in case nothing is registered for the typeName.
   * @returns the registered objectType to use to create id instances for the typeName given. In case there is nothing registered, the given default is returned.
   * @see register
   */
  getObjectTypeForTypeName<TValueType, TTypeName extends string>(typeName: TTypeName, defaultObjectType?: ObjectType<Id<TValueType, TTypeName>>): ObjectType<Id<TValueType, TTypeName>> {
    if (!typeName) {
      return defaultObjectType;
    }
    const objectType = this._registry.get(typeName);
    if (objectType) {
      return objectType as ObjectType<Id<TValueType, TTypeName>>;
    }
    return defaultObjectType;
  }

  /**
   * Registers an id objectType for a typeName. Can be used to customize which class is used to create id instances for a given typeName.
   *
   * @param typeName The typeName string for which the override should be registered.
   * @param objectType The objectType of the id to create.
   */
  registerIdTypeName<TTypeName extends string>(typeName: TTypeName, objectType: ObjectType<Id<any, TTypeName>>) {
    this._registry.set(typeName, objectType);
  }

  /**
   * Removes a typeName registration from the registry.
   * @param typeName The typeName string for which the override should be registered.
   * @see register
   */
  removeIdTypeName(typeName: string): boolean {
    return this._registry.delete(typeName);
  }

  /**
   * @returns the {@link IdParser} shared instance (created on first use).
   */
  static get() {
    if (!IdParser._INSTANCE) {
      IdParser._INSTANCE = scout.create(IdParser);
    }
    return IdParser._INSTANCE;
  }
}
