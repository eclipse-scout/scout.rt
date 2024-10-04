/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DoDeserializer, DoNodeSerializer, DoSerializer, DoValueMetaData, objects} from '../../index';

export class SetDoNodeSerializer implements DoNodeSerializer<object> {

  canSerialize(value: any, metaData: DoValueMetaData): boolean {
    return value instanceof Set;
  }

  serialize(set: Set<any>, metaData: DoValueMetaData, serializer: DoSerializer): any[] {
    const target = [];
    const setValueType = SetDoNodeSerializer.getSetValueType(metaData);
    set.forEach(v => target.push(serializer.serialize(v, setValueType)));
    return target;
  }

  canDeserialize(value: any, metaData: DoValueMetaData): boolean {
    return Set === metaData?.type && (!value || objects.isArray(value));
  }

  deserialize(set: any[], metaData: DoValueMetaData, deserializer: DoDeserializer): Set<any> {
    const setValueType = SetDoNodeSerializer.getSetValueType(metaData);
    const target = new Set();
    if (!set) {
      return target;
    }
    set.forEach(v => target.add(deserializer.deserialize(v, setValueType)));
    return target;
  }

  static getSetValueType(metaData: DoValueMetaData): DoValueMetaData {
    if (metaData?.args?.length) {
      return metaData.args[0];
    }
    return null;
  }
}
