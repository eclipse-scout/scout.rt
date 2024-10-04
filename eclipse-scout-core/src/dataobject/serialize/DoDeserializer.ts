/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, Constructor, dataObjects, DoRegistry, DoValueMetaData, doValueMetaData, objects, scout} from '../../index';

export class DoDeserializer {

  deserialize<T extends object>(value: any, valueMetaData?: DoValueMetaData<T>): T {
    const deserializer = dataObjects.serializers.find(d => d.canDeserialize(value, valueMetaData));
    if (deserializer) {
      // use custom deserializer
      return deserializer.deserialize(value, valueMetaData, this);
    }
    if (objects.isNullOrUndefined(value)) {
      return value; // no value to convert
    }
    if (objects.isPojo(value)) {
      // nested object
      return this._deserializeObject(value, valueMetaData);
    }
    return value;
  }

  protected _deserializeObject<T extends object>(rawObj: Record<string, any>, metaData?: DoValueMetaData<T>): T {
    const detectedClass = this._detectClass(rawObj) as Constructor<T>;
    let constructor = metaData?.type as Constructor<T>;
    if (constructor) {
      doValueMetaData.assertSame(detectedClass, constructor);
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
        resultObj[key] = this._convertFieldValue(proto, rawObj, key, rawObj[key]);
      });
    return resultObj;
  }

  protected _convertFieldValue(proto: object, rawObj: object, key: string, value: any): any {
    const fieldMetaData = doValueMetaData.getFieldMetaData(proto, key);
    return this.deserialize(value, fieldMetaData);
  }

  protected _detectClass(obj: any): Constructor {
    const jsonType = obj._type;
    if (typeof jsonType === 'string') {
      const result = DoRegistry.get().toConstructor(jsonType);
      if (result) {
        return result;
      }
    }
    return doValueMetaData.resolveToConstructor(obj.objectType);
  }
}

