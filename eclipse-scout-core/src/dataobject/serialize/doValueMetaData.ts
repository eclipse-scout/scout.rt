/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, Constructor, DataObjectInventory, ObjectFactory, ObjectType, TypeDescriptor} from '../../index';

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
    if (typeof objectType === 'string') {
      // ask DO inventory first: is faster and most often has an answer even if the DO is not registered on the window object
      const doConstructor = DataObjectInventory.get().toConstructor(objectType) as Constructor<T>;
      if (doConstructor) {
        return doConstructor;
      }
    }
    // ask TypeDescriptor second
    return TypeDescriptor.resolveType(objectType, {variantLenient: true});
  },

  getArrayValueType(metaData: DoValueMetaData): DoValueMetaData {
    return metaData?.args?.[0];
  },

  chooseDataObjectType(obj: any, metaData: DoValueMetaData): Constructor {
    const detectedClass = doValueMetaData.detectDataObjectClass(obj);
    const metaType = metaData?.type;

    // if both are present: check compatibility and use the detected one (e.g. from _type): it might be a subclass of the one in the source code (more specific).
    if (metaType && detectedClass) {
      doValueMetaData.assertTypesCompatible(detectedClass, metaType);
      return detectedClass;
    }

    if (detectedClass) {
      // no metadata: use detected one
      return detectedClass;
    }

    return metaType; // might also be null
  },

  detectDataObjectClass(obj: any): Constructor {
    if (obj instanceof BaseDoEntity) {
      return obj.constructor as Constructor;
    }
    const typeName = obj._type;
    if (typeof typeName === 'string') {
      const doConstructor = DataObjectInventory.get().toConstructor(typeName);
      if (doConstructor) {
        return doConstructor;
      }
    }
    return doValueMetaData.resolveToConstructor(obj.objectType) as Constructor;
  },

  /**
   * Checks if `actualType` is instanceof `declaredType`.
   */
  assertTypesCompatible(actualType: Constructor, declaredType: Constructor) {
    if (actualType && declaredType && actualType !== declaredType && !declaredType.isPrototypeOf(actualType)) {
      const actual = ObjectFactory.get().getObjectType(actualType) || actualType;
      const declared = ObjectFactory.get().getObjectType(declaredType) || declaredType;
      throw new Error(`Incompatible types: actual type '${actual}' is not assignable to declared type '${declared}'.`);
    }
  }
};

export type RawFieldMetaData = ObjectType | {
  objectType: ObjectType; // could be a Constructor (e.g. Array, Map, String) or string objectType (e.g. 'myApp.MySpecialDo')
  typeArgs: RawFieldMetaData[];
};

export type DoValueMetaData<T = any> = {
  type: Constructor<T>;
  typeName: string;
  args: DoValueMetaData[];
};
