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

export class ArrayDoNodeSerializer implements DoNodeSerializer<Array<any>> {

  canSerialize(value: any, metaData: DoValueMetaData): boolean {
    return objects.isArray(value);
  }

  serialize(array: Array<any>, metaData: DoValueMetaData, serializer: DoSerializer): any {
    const arrayValueType = ArrayDoNodeSerializer.getArrayValueType(metaData);
    return array.map(e => serializer.serialize(e, arrayValueType));
  }

  canDeserialize(value: any, metaData: DoValueMetaData): boolean {
    return objects.isArray(value);
  }

  deserialize(array: Array<any>, metaData: DoValueMetaData, deserializer: DoDeserializer): Array<any> {
    const arrayValueType = ArrayDoNodeSerializer.getArrayValueType(metaData);
    return array.map(e => deserializer.deserialize(e, arrayValueType));
  }

  static getArrayValueType(metaData: DoValueMetaData): DoValueMetaData {
    if (metaData?.args?.length) {
      return metaData.args[0];
    }
    return null;
  }
}
