/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, Constructor, dataobjects, DoRegistry, ObjectFactory, objects, ObjectType, scout, TypeDescriptor} from '../index';

export class DoDeserializer {

  static TYPE_META_DATA_KEY = 'scout.m.t';

  deserialize<T extends object>(value: any, objectType?: ObjectType<T>): T {
    if (Array.isArray(value)) {
      return value.map(e => this.deserialize(e, objectType)) as T;
    }
    if (objects.isPlainObject(value)) {
      return this._deserializeObject(value, objectType);
    }
    return value;
  }

  protected _deserializeObject<T extends object>(rawObj: Record<string, any>, objectType?: ObjectType<T>): T {
    const detectedClass = this._detectClassFor(rawObj) as Constructor<T>;
    let constructor = DoDeserializer.resolveToConstructor(objectType) as Constructor<T>; // convert string to constructor if possible as the datatype metadata would be on the constructor
    if (constructor) {
      DoDeserializer.assertSame(detectedClass, constructor);
    } else if (detectedClass) {
      constructor = detectedClass;
    } else {
      constructor = BaseDoEntity as Constructor<T>;
    }

    const resultObj = scout.create(constructor, null /* must always be possible to create a DO without model */, {ensureUniqueId: false});
    if (BaseDoEntity === constructor && rawObj._type) {
      resultObj['_type'] = rawObj._type; // keep _type for BaseDoEntity. This is required for DOs which only exist on the backend.
    }

    const proto = Object.getPrototypeOf(constructor).prototype;
    Object.keys(rawObj)
      .filter(key => key !== '_type' && key !== 'objectType') // Ignore _type and objectType from source object as these attributes are already correctly set here. Keep _typeVersion in case the DO is sent to the backend again.
      .forEach(key => {
        resultObj[key] = this._convertValue(proto, rawObj, key, rawObj[key]);
      });
    return resultObj;
  }

  protected _convertValue(proto: object, rawObj: object, key: string, value: any): any {
    const objectType = DoDeserializer.getTypeMetaData(proto, key);
    const deserializer = dataobjects.jsonDeSerializers.find(d => d.canDeserialize(value, objectType));
    if (deserializer) {
      // use custom serializer
      return deserializer.deserialize(value, objectType);
    }
    if (objects.isNullOrUndefined(value)) {
      return value; // no value to convert or no data-type info available
    }
    if (Array.isArray(value)) {
      return value.map(e => this._convertValue(proto, rawObj, key, e));
    }
    if (objects.isPlainObject(value)) {
      // nested object
      return this._deserializeObject(value, objectType);
    }
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
    return DoDeserializer.resolveToConstructor(obj.objectType);
  }

  static getTypeMetaData(objectPrototype: any, fieldName: string): Constructor {
    if (!objectPrototype || !fieldName) {
      return null;
    }
    const objectType = Reflect.getMetadata(DoDeserializer.TYPE_META_DATA_KEY, objectPrototype, fieldName) as ObjectType; // could be a Constructor or string
    return DoDeserializer.resolveToConstructor(objectType);
  }

  static resolveToConstructor<T>(objectType: ObjectType<T>): Constructor<T> {
    return TypeDescriptor.resolveType(objectType, {variantLenient: true});
  }

  static assertSame(detectedFromValue: Constructor, passedFromMeta: Constructor) {
    if (detectedFromValue && passedFromMeta && detectedFromValue !== passedFromMeta) {
      const objectFactory = ObjectFactory.get();
      const inObject = objectFactory.getObjectType(detectedFromValue) || detectedFromValue;
      const inMeta = objectFactory.getObjectType(passedFromMeta) || passedFromMeta;
      throw new Error(`Incompatible types: object contains '${inObject}' but '${inMeta}' was expected.`);
    }
  }
}
