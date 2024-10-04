/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DoDeserializer, DoNodeSerializer, DoSerializer, DoValueMetaData, numbers, objects} from '../../index';

export class MapDoNodeSerializer implements DoNodeSerializer<object> {

  canSerialize(value: any, metaData: DoValueMetaData): boolean {
    // no special handling required to serialize Records
    return value instanceof Map;
  }

  serialize(map: Map<any, any>, metaData: DoValueMetaData, serializer: DoSerializer): any {
    const target = {};
    const mapValueType = MapDoNodeSerializer.getMapValueType(metaData);
    for (const [key, value] of map.entries()) {
      target[key] = serializer.serialize(value, mapValueType);
    }
    return target;
  }

  canDeserialize(value: any, metaData: DoValueMetaData): boolean {
    return 'Record' === metaData?.typeName
      || (Map === metaData?.type && (!value || objects.isPojo(value)));
  }

  deserialize(map: object, metaData: DoValueMetaData, deserializer: DoDeserializer): object {
    const mapKeyType = MapDoNodeSerializer.getMapKeyType(metaData);
    const mapValueType = MapDoNodeSerializer.getMapValueType(metaData);
    if ('Record' === metaData?.typeName) {
      return this._deserializeRecord(map, mapKeyType, mapValueType, deserializer);
    }
    return this._deserializeMap(map as Map<any, any>, mapKeyType, mapValueType, deserializer);
  }

  protected _deserializeMap(map: Map<any, any>, mapKeyType: DoValueMetaData, mapValueType: DoValueMetaData, deserializer: DoDeserializer): object {
    const target = new Map();
    if (!map) {
      return target;
    }

    for (const [key, value] of Object.entries(map)) {
      const mapKey = this._convertKey(key, mapKeyType);
      target.set(mapKey, deserializer.deserialize(value, mapValueType));
    }
    return target;
  }

  protected _deserializeRecord(record: Record<PropertyKey, any>, recordKeyType: DoValueMetaData, recordValueType: DoValueMetaData, deserializer: DoDeserializer): object {
    const target = {};
    if (!record) {
      return target;
    }
    for (const [key, value] of Object.entries(record)) {
      const mapKey = this._convertKey(key, recordKeyType);
      target[mapKey] = deserializer.deserialize(value, recordValueType);
    }
    return target;
  }

  protected _convertKey(key: string, keyMetaData: DoValueMetaData): PropertyKey {
    // keys are always strings when serialized: convert to supported target types here
    if (Number === keyMetaData?.type) {
      return numbers.ensure(key);
    }
    return key;
  }

  static getMapKeyType(metaData: DoValueMetaData): DoValueMetaData {
    return MapDoNodeSerializer._getMapType(metaData, 0);
  }

  static getMapValueType(metaData: DoValueMetaData): DoValueMetaData {
    return MapDoNodeSerializer._getMapType(metaData, 1);
  }

  protected static _getMapType(metaData: DoValueMetaData, index: number): DoValueMetaData {
    if (metaData?.args?.length > 1) {
      return metaData.args[index];
    }
    return null;
  }
}
