/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DataObjectDeserializer, DataObjectSerializer, DoNodeSerializer, DoValueMetaData, objects} from '../../index';

export class MapDoNodeSerializer implements DoNodeSerializer<object> {

  canSerialize(value: any, metaData: DoValueMetaData): boolean {
    // no special handling required to serialize Records
    return value instanceof Map;
  }

  serialize(map: Map<any, any>, metaData: DoValueMetaData, serializer: DataObjectSerializer): any {
    const target = {};
    const mapKeyType = MapDoNodeSerializer.getMapKeyType(metaData);
    const mapValueType = MapDoNodeSerializer.getMapValueType(metaData);
    for (const [key, value] of map.entries()) {
      const serializedKey = this._throwIfInvalidKey(serializer.serialize(key, mapKeyType));
      target[serializedKey] = serializer.serialize(value, mapValueType);
    }
    return target;
  }

  canDeserialize(value: any, metaData: DoValueMetaData): boolean {
    return 'Record' === metaData?.typeName
      || (Map === metaData?.type && (!value || objects.isPojo(value)));
  }

  deserialize(map: object, metaData: DoValueMetaData, deserializer: DataObjectDeserializer): object {
    const mapKeyType = MapDoNodeSerializer.getMapKeyType(metaData);
    const mapValueType = MapDoNodeSerializer.getMapValueType(metaData);
    if ('Record' === metaData?.typeName) {
      return this._deserializeRecord(map, mapKeyType, mapValueType, deserializer);
    }
    return this._deserializeMap(map as Map<any, any>, mapKeyType, mapValueType, deserializer);
  }

  protected _throwIfInvalidKey(keyCandidate: any): string | number | symbol {
    if (this._isValidKey(keyCandidate)) {
      return keyCandidate;
    }
    throw new Error(`'${keyCandidate}' is no valid map key.`);
  }

  protected _isValidKey(keyCandidate: any): keyCandidate is string | number | symbol {
    const type = typeof keyCandidate;

    // number is actually not allowed according to https://developer.mozilla.org/en-US/docs/Web/JavaScript/Data_structures#properties
    // But it will be automatically converted to a string which is fine as well.
    return type === 'string' || type === 'number' || type === 'symbol';
  }

  protected _deserializeMap(map: Map<any, any>, mapKeyType: DoValueMetaData, mapValueType: DoValueMetaData, deserializer: DataObjectDeserializer): object {
    if (!map) {
      return map;
    }

    const target = new Map();
    for (const [key, value] of Object.entries(map)) {
      const mapKey = deserializer.deserialize(key, mapKeyType);
      target.set(mapKey, deserializer.deserialize(value, mapValueType));
    }
    return target;
  }

  protected _deserializeRecord(record: Record<PropertyKey, any>, recordKeyType: DoValueMetaData, recordValueType: DoValueMetaData, deserializer: DataObjectDeserializer): object {
    if (!record) {
      return record;
    }

    const target = {};
    for (const [key, value] of Object.entries(record)) {
      const mapKey = deserializer.deserialize(key, recordKeyType);
      target[mapKey] = deserializer.deserialize(value, recordValueType);
    }
    return target;
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
