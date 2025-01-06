/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, Constructor, dataObjects, DoValueMetaData, doValueMetaData, objects, scout} from '../../index';

export class DataObjectDeserializer {

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
    const constructor = (doValueMetaData.chooseDataObjectType(rawObj, metaData) || BaseDoEntity) as Constructor<T>;
    const resultObj = scout.create(constructor, null /* must always be possible to create a DO without model */);
    const proto = Object.getPrototypeOf(constructor).prototype;
    Object.keys(rawObj)
      .filter(key => key !== '_typeVersion') // Ignore _typeVersion as this is not required on the client. At runtime only the latest version can exist anyway and the backend could re-create this information from its Java annotation.
      .forEach(key => {
        resultObj[key] = this._convertFieldValue(proto, rawObj, key, rawObj[key]);
      });
    return resultObj;
  }

  protected _convertFieldValue(proto: object, rawObj: object, key: string, value: any): any {
    const fieldMetaData = doValueMetaData.getFieldMetaData(proto, key);
    return this.deserialize(value, fieldMetaData);
  }
}

