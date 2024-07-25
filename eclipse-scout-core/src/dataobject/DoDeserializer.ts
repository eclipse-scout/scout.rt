/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, Constructor, dates, DoRegistry, objects, ObjectType, scout, TypeDescriptor} from '../index';

export class DoDeserializer {

  static META_DATA_KEY = 'scout.m.t';

  parse<T>(json: string, objectType?: ObjectType<T>): T {
    const value = JSON.parse(json); // don't use reviver here as it works bottom-up. But here top-down is required.
    if (objects.isPlainObject(value)) {
      return this.reviveObject(value, objectType);
    }
    return value;
  }

  reviveObject<T>(rawObj: Record<string, any>, objectType?: ObjectType<T>): T {
    let Class: Constructor<T> = this._tryResolveToConstructor(objectType); // convert string to constructor if possible as the datatype metadata would be on the constructor
    if (!Class) {
      Class = this._detectClassFor(rawObj) as Constructor<T>;
    }

    const resultObj = scout.create(Class, null /* must always be possible to create a DO without model */, {ensureUniqueId: false});
    if (BaseDoEntity === Class && rawObj._type) {
      resultObj['_type'] = rawObj._type; // keep _type for BaseDoEntity. This is required for DOs which only exist on the backend.
    }
    delete rawObj._type;
    delete rawObj.objectType;
    // keep _typeVersion in case the DO is sent to the backend again

    const proto = Object.getPrototypeOf(Class).prototype;
    Object.keys(rawObj).forEach(key => {
      resultObj[key] = this._convertValue(proto, rawObj, key, rawObj[key]);
    });
    return resultObj;
  }

  protected _convertValue(proto: object, rawObj: object, key: string, value: any): any {
    if (objects.isNullOrUndefined(value) || !proto) {
      return value; // no value to convert or no data-type info available
    }
    if (Array.isArray(value)) {
      return value.map(e => this._convertValue(proto, rawObj, key, e));
    }

    const objectType = Reflect.getMetadata(DoDeserializer.META_DATA_KEY, proto, key) as ObjectType;
    if (Date === objectType && typeof value === 'string') {
      // Dates are serialized as strings. Convert here.
      return dates.parseJsonDate(value);
    }
    if (objects.isPlainObject(value)) {
      // nested object
      return this.reviveObject(value, this._tryResolveToConstructor(objectType));
    }
    // FIXME mvi [js-bookmark] allow custom (de)serializers here (e.g. for IDs). See IDataObjectSerializerProvider.java and DataObjectSerializers.java
    return value;
  }

  protected _detectClassFor(obj: any): Constructor {
    const jsonType = obj._type;
    if (typeof jsonType === 'string') {
      const result = DoRegistry.get().toConstructor(jsonType);
      if (result) {
        return result;
      }
    }
    return this._tryResolveToConstructor(obj.objectType) || BaseDoEntity;
  }

  protected _tryResolveToConstructor<T>(objectType: ObjectType<T>): Constructor<T> {
    return TypeDescriptor.resolveType(objectType, {variantLenient: true});
  }
}
