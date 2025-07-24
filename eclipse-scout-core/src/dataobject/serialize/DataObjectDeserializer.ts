/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, Constructor, dataObjects, DoValueMetaData, doValueMetaData, InitModelOf, ObjectModel, objects, ObjectWithType, Predicate, scout} from '../../index';

export class DataObjectDeserializer implements DataObjectDeserializerModel, ObjectWithType {

  declare model: DataObjectDeserializerModel;

  id: string;
  objectType: string;
  createPojoIfDoIsUnknown: boolean;
  retainTypeVersion: Predicate<object>;

  constructor(model?: InitModelOf<DataObjectDeserializer>) {
    this.createPojoIfDoIsUnknown = !!model?.createPojoIfDoIsUnknown;
    this.retainTypeVersion = scout.nvl(model?.retainTypeVersion, (obj: object) => !(obj instanceof BaseDoEntity));
    if (!objects.isFunction(this.retainTypeVersion)) {
      const retain = !!this.retainTypeVersion;
      this.retainTypeVersion = () => retain;
    }
  }

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
    const constructor = this._resolveDataObjectType(rawObj, metaData) as Constructor<T>;
    const resultObj = this._createResultObject(constructor);
    const proto = Object.getPrototypeOf(constructor).prototype;
    Object.keys(rawObj)
      // Ignore _typeVersion as it is handled later on.
      .filter(key => key !== '_typeVersion')
      .forEach(key => {
        resultObj[key] = this._convertFieldValue(proto, rawObj, key, rawObj[key]);
      });

    if (rawObj.hasOwnProperty('_typeVersion') && this.retainTypeVersion(resultObj)) {
      resultObj['_typeVersion'] = this._convertFieldValue(proto, rawObj, '_typeVersion', rawObj['_typeVersion']);
    }

    return resultObj;
  }

  protected _resolveDataObjectType(rawObj: Record<string, any>, metaData?: DoValueMetaData): Constructor {
    for (const resolver of dataObjects.doTypeResolvers) {
      const constructor = resolver.resolve(rawObj, metaData);
      if (constructor) {
        return constructor;
      }
    }
    return BaseDoEntity;
  }

  protected _createResultObject<T extends object>(constructor: Constructor<T>): T {
    if (this.createPojoIfDoIsUnknown && constructor === BaseDoEntity) {
      // DataObject could not be found and missing DOs should be created as Pojo instead of BaseDoEntity (legacy case)
      return {} as T;
    }
    return scout.create(constructor, null /* must always be possible to create a DO without model */);
  }

  protected _convertFieldValue(proto: object, rawObj: object, key: string, value: any): any {
    const fieldMetaData = doValueMetaData.getFieldMetaData(proto, key);
    return this.deserialize(value, fieldMetaData);
  }
}

export interface DataObjectDeserializerModel extends ObjectModel<DataObjectDeserializer> {
  /**
   * Controls the kind of object that will be created when deserializing unknown DataObjects.
   * If `true` a pojo will be created for unknown DOs. If `false`, instances of {@link BaseDoEntity} will be created.
   * Changing the type of the deserialized DataObject may change the deserialization behaviour of the property '_typeVersion' (see {@link retainTypeVersion} for more details).
   * Default is `false`.
   *
   * A DataObject can be unknown e.g. if:
   * <ol>
   *   <li>The _type attribute of the object to deserialize cannot be found in TS.</li>
   *   <li>There is no _type attribute and the TypeScript attribute declaration (metadata) does not provide a class type which could be used as fallback.</li>
   * </ol>
   */
  createPojoIfDoIsUnknown?: boolean;
  /**
   * Controls whether the '_typeVersion' property is retained when deserializing DataObjects.
   * The property can be given as a {@link boolean} or a {@link Predicate}, whose input is the deserialized DataObject.
   * If `true`, the '_typeVersion' will be set on the deserialized DataObject. If `false`, it will be ignored.
   * Default is a {@link Predicate} that ignores the '_typeVersion' if the deserialized DataObject is instance of {@link BaseDoEntity}.
   */
  retainTypeVersion?: boolean | Predicate<object>;
}
