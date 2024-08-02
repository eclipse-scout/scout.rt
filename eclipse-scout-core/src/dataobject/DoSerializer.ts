/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, Constructor, dataobjects, DoDeserializer, DoRegistry, ObjectFactory, objects} from '../index';

export class DoSerializer {
  serialize(value: any): any {
    if (Array.isArray(value)) {
      return value.map(e => this.serialize(e));
    }
    if (objects.isPlainObject(value)) {
      return this._serializeObject(value);
    }
    return value;
  }

  protected _serializeObject(value: any, objectType?: Constructor): any {
    const detectedObjectType = this._detectObjectType(value);
    if (objectType) {
      // a type is given from data value and from metadata of the parent object: validate they are the same
      DoDeserializer.assertSame(detectedObjectType, objectType);
    } else {
      objectType = detectedObjectType;
    }

    const target = Object.assign({}, value); // shallow copy to keep original object intact
    this._setJsonTypeTo(target, objectType);
    delete target.objectType; // Scout JS internal property

    const proto = objectType ? Object.getPrototypeOf(objectType).prototype : null;
    Object.keys(target).forEach(key => {
      target[key] = this._convertValue(proto, target, key, target[key]);
    });
    return target;
  }

  protected _detectObjectType(obj: any): Constructor {
    if (obj instanceof BaseDoEntity) {
      return obj.constructor as Constructor;
    }
    const constructor = DoDeserializer.resolveToConstructor(obj.objectType) as Constructor;
    if (constructor) {
      return constructor;
    }
    if (typeof obj._type === 'string') {
      return DoRegistry.get().toConstructor(obj._type);
    }
    return null;
  }

  protected _convertValue(proto: object, target: object, key: string, value: any): any {
    const objectType = DoDeserializer.getTypeMetaData(proto, key);
    const serializer = dataobjects.jsonDeSerializers.find(s => s.canSerialize(value, objectType));
    if (serializer) {
      // use custom serializer
      return serializer.serialize(value, objectType);
    }
    if (objects.isNullOrUndefined(value)) {
      return value;
    }
    if (Array.isArray(value)) {
      return value.map(e => this._convertValue(proto, target, key, e));
    }
    if (objects.isPlainObject(value)) {
      // nested object
      return this._serializeObject(value, objectType);
    }
    return value;
  }

  protected _setJsonTypeTo(obj: any, constructor: Constructor) {
    if (obj._type) {
      return; // already present
    }
    const objectFactory = ObjectFactory.get();
    const objectType = objectFactory.getObjectType(constructor) || objectFactory.getObjectType(obj.objectType);
    if (objectType) {
      const jsonType = DoRegistry.get().toJsonType(objectType);
      if (jsonType) {
        obj._type = jsonType;
      }
    }
  }
}
