/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Constructor, DataObjectInventory, dataObjects, DoValueMetaData, doValueMetaData, ObjectFactory, objects, ObjectWithType} from '../../index';

export class DataObjectSerializer implements ObjectWithType {

  id: string;
  objectType: string;

  protected _cycleDetector = new DoSerializerCycleDetector();

  serialize(value: any, valueMetaData?: DoValueMetaData): any {
    this._cycleDetector.push(value);
    try {
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
      return value;
    } finally {
      this._cycleDetector.pop(value);
    }
  }

  protected _serializeObject(value: any, metaData?: DoValueMetaData): any {
    const constructor = doValueMetaData.chooseDataObjectType(value, metaData);
    const serialized: any = {};
    const proto = constructor ? Object.getPrototypeOf(constructor).prototype : null;
    Object.keys(value)
      .filter(key => key !== 'objectType' /* Scout JS internal property */)
      .forEach(key => {
        const convertedValue = this._convertFieldValue(proto, key, value[key]);
        if (convertedValue !== undefined) {
          serialized[key] = convertedValue;
        }
      });
    this._writeJsonType(serialized, constructor);
    return serialized;
  }

  protected _convertFieldValue(proto: object, fieldName: string, value: any): any {
    this._cycleDetector.pushAttributeName(fieldName);
    const fieldMetaData = doValueMetaData.getFieldMetaData(proto, fieldName);
    const result = this.serialize(value, fieldMetaData);
    this._cycleDetector.popAttributeName(fieldName);
    return result;
  }

  protected _writeJsonType(target: any, constructor: Constructor) {
    if (target._type) {
      return; // already present
    }
    const objectFactory = ObjectFactory.get();
    const objectType = objectFactory.getObjectType(constructor);
    if (objectType) {
      const typeName = DataObjectInventory.get().toTypeName(objectType);
      if (typeName) {
        target._type = typeName;
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
      const path = '[' + this._attributeNames.join(',') + ']';
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
