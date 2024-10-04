/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Constructor, ObjectFactory, ObjectType, TypeDescriptor} from '../../index';

export const doValueMetaData = {
  TYPE_META_DATA_KEY: 'scout.m.t',

  getFieldMetaData(prototype: object, fieldName: string): DoValueMetaData {
    if (!prototype || !fieldName) {
      return null;
    }
    const meta = Reflect.getMetadata(doValueMetaData.TYPE_META_DATA_KEY, prototype, fieldName) as RawFieldMetaData;
    return doValueMetaData.resolveFieldMetaData(meta);
  },

  resolveFieldMetaData(meta: RawFieldMetaData): DoValueMetaData {
    if (!meta) {
      return null;
    }
    if (typeof meta === 'string' || typeof meta === 'function') {
      // string objectType. E.g. a TypeScript interface or constructor without typeArgs (e.g. Number)
      return {
        ...doValueMetaData.getObjectTypeInfo(meta),
        args: []
      };
    }

    // metadata with nested type arguments
    return {
      ...doValueMetaData.getObjectTypeInfo(meta.objectType),
      args: meta.typeArgs.map(m => doValueMetaData.resolveFieldMetaData(m))
    };
  },

  getObjectTypeInfo(objectType: ObjectType<any>): { type: Constructor<any>; typeName: string } {
    if (typeof objectType === 'string') {
      // string objectType. E.g. a TypeScript interface
      const constructor = doValueMetaData.resolveToConstructor(objectType);
      return {type: constructor, typeName: objectType};
    }
    // constructor without typeArgs (e.g. Number)
    const typeName = ObjectFactory.get().getObjectType(objectType);
    return {type: objectType, typeName};
  },

  resolveToConstructor<T>(objectType: ObjectType<T>): Constructor<T> {
    return TypeDescriptor.resolveType(objectType, {variantLenient: true});
  },

  assertSame(detectedFromValue: Constructor, passedFromMeta: Constructor) {
    if (detectedFromValue && passedFromMeta && detectedFromValue !== passedFromMeta) {
      const objectFactory = ObjectFactory.get();
      const inObject = objectFactory.getObjectType(detectedFromValue) || detectedFromValue;
      const inMeta = objectFactory.getObjectType(passedFromMeta) || passedFromMeta;
      throw new Error(`Incompatible types: object contains '${inObject}' but '${inMeta}' was expected.`);
    }
  }
};

export type RawFieldMetaData = ObjectType | {
  objectType: ObjectType; // could be a Constructor or string
  typeArgs: RawFieldMetaData[];
};

export type DoValueMetaData<T = any> = {
  type: Constructor<T>;
  typeName: string;
  args: DoValueMetaData[];
};
