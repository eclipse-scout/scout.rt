/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, Constructor, dataObjects, DoRegistry, DoValueMetaData, doValueMetaData, ObjectFactory, objects, strings} from '../../index';

export class DoSerializer {

  cycleDetector = new DoSerializerCycleDetector();

  serialize(value: any, valueMetaData?: DoValueMetaData): any {
    this.cycleDetector.push(value);
    const serializer = dataObjects.serializers.find(s => s.canSerialize(value, valueMetaData));
    if (serializer) {
      // use custom serializer
      return serializer.serialize(value, valueMetaData, this);
    }
    if (objects.isNullOrUndefined(value)) {
      return value;
    }
    if (objects.isObject(value)) {
      // nested object
      return this._serializeObject(value, valueMetaData);
    }
    this.cycleDetector.pop(value);
    return value;
  }

  protected _serializeObject(value: any, metaData?: DoValueMetaData): any {
    const detectedObjectType = this._detectObjectType(value);
    let objectType = metaData?.type;
    if (objectType) {
      // a type is given from data value and from metadata of the parent object: validate they are the same
      doValueMetaData.assertSame(detectedObjectType, objectType);
    } else {
      objectType = detectedObjectType;
    }

    const target = Object.assign({}, value); // shallow copy to keep original object intact
    this._setJsonTypeTo(target, objectType);
    delete target.objectType; // Scout JS internal property

    const proto = objectType ? Object.getPrototypeOf(objectType).prototype : null;
    Object.keys(target)
      .filter(key => key !== '_type')
      .forEach(key => {
        target[key] = this._convertFieldValue(proto, key, target[key]);
      });
    return target;
  }

  protected _detectObjectType(obj: any): Constructor {
    if (obj instanceof BaseDoEntity) {
      return obj.constructor as Constructor;
    }
    const constructor = doValueMetaData.resolveToConstructor(obj.objectType) as Constructor;
    if (constructor) {
      return constructor;
    }
    if (typeof obj._type === 'string') {
      return DoRegistry.get().toConstructor(obj._type);
    }
    return null;
  }

  protected _convertFieldValue(proto: object, fieldName: string, value: any): any {
    this.cycleDetector.pushAttributeName(fieldName);
    const fieldMetaData = doValueMetaData.getFieldMetaData(proto, fieldName);
    const result = this.serialize(value, fieldMetaData);
    this.cycleDetector.popAttributeName(fieldName);
    return result;
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

export class DoSerializerCycleDetector {
  protected _stack = [];
  protected _attributeNames: string[] = [];

  push(value: any) {
    if (!this._canHaveChildren(value)) {
      return;
    }
    if (this.contains(value)) {
      const path = '[' + strings.join(',', ...this._attributeNames) + ']';
      throw new Error(`Unable to serialize object. Reference cycle detected. Attribute path: ${path}`);
    }
    this._stack.push(value);
  }

  pop(value: any) {
    if (!this._canHaveChildren(value)) {
      return;
    }
    const removed = this._stack.pop();
    if (removed !== value) {
      throw new Error(`Asymmetric use of cycle detector. Expected pop for '${removed}' but was '${value}'.`);
    }
  }

  contains(value: any) {
    return this._stack.indexOf(value) >= 0;
  }

  pushAttributeName(attributeName: string) {
    this._attributeNames.push(attributeName);
  }

  popAttributeName(attributeName: string) {
    const removed = this._attributeNames.pop();
    if (removed !== attributeName) {
      throw new Error(`Asymmetric use of cycle detector. Expected pop for '${removed}' but was '${attributeName}'.`);
    }
  }

  protected _canHaveChildren(value: any): boolean {
    return objects.isObject(value) || objects.isArray(value);
  }
}
