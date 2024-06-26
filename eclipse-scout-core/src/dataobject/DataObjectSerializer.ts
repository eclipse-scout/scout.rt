/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, Constructor, dates, DoRegistry, ObjectFactory, objects, TypeDescriptor} from '../index';

export class DataObjectSerializer {
  serialize(key: string, value: any): any {
    if (!objects.isPlainObject(value)) {
      return value;
    }
    return this._handleObjectValue(value);
  }

  protected _handleObjectValue(value: any): any {
    const target = Object.assign({}, value); // shallow copy to keep original object intact
    const objectType = this._detectObjectType(value);
    this._setJsonType(target, objectType);
    delete target.objectType; // Scout JS internal property

    if (objectType) {
      const proto = Object.getPrototypeOf(objectType).prototype;
      Object.keys(target)
        .filter(key => !objects.isNullOrUndefined(target[key])) // properties not specified (no need to convert anything)
        .forEach(key => {
          target[key] = this._convertValue(proto, target, key, target[key]);
        });
    }
    return target;
  }

  protected _detectObjectType(obj): Constructor {
    if (obj instanceof BaseDoEntity) {
      return obj.constructor as Constructor;
    }
    const objectType = obj.objectType;
    if (typeof objectType === 'function') {
      return objectType;
    }
    if (typeof objectType === 'string') {
      const typeDescriptor = TypeDescriptor.parse(objectType);
      const result = typeDescriptor.resolve({variantLenient: true});
      if (result) {
        return result;
      }
    }
    if (typeof obj._type === 'string') {
      return DoRegistry.get().toConstructor(obj._type);
    }
    return null;
  }

  protected _convertValue(proto: object, target: object, key: string, value: any): any {
    // const propertyDataType = Reflect.getMetadata('scout.meta.t', proto, key);
    // const arrayDim = Reflect.getMetadata('scout.meta.a', proto, key);
    // if (typeof arrayDim === 'number' && arrayDim > 0) {
    //   console.log('array of dimension' + arrayDim);
    // }
    if (Array.isArray(value)) {
      return value.map(e => this._convertValue(proto, target, key, e));
    }
    if (value instanceof Date) {
      // JS Date must be converted to a string as expected by the Scout backends
      return dates.toJsonDate(value, true);
    }
    if (objects.isPlainObject(value)) {
      // set _type of child object based on meta-data of this field
      this._setJsonTypeByMetaData(proto, key, value);
    }
    return value;
  }

  protected _setJsonTypeByMetaData(proto: object, key: string, target: any) {
    if (target._type) {
      return; // already present
    }
    const objectType = Reflect.getMetadata('scout.meta.t', proto, key);
    const jsonType = DoRegistry.get().toJsonType(objectType);
    if (jsonType) {
      target._type = jsonType;
    }
  }

  protected _setJsonType(obj: any, constructor: Constructor) {
    if (obj._type) {
      return; // already present
    }
    const objectFactory = ObjectFactory.get();
    const objectType = objectFactory.getObjectType(obj.objectType) || objectFactory.getObjectType(constructor);
    if (objectType) {
      const jsonType = DoRegistry.get().toJsonType(objectType);
      if (jsonType) {
        obj._type = jsonType;
      }
    }
  }
}
