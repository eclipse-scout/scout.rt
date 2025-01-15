/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DataObjectDeserializer, DataObjectSerializer, DoNodeSerializer, doValueMetaData, DoValueMetaData, objects} from '../../index';

export class ArrayDoNodeSerializer implements DoNodeSerializer<Array<any>> {

  canSerialize(value: any, metaData: DoValueMetaData): boolean {
    return objects.isArray(value);
  }

  serialize(array: Array<any>, metaData: DoValueMetaData, serializer: DataObjectSerializer): any {
    const arrayValueType = doValueMetaData.getArrayValueType(metaData);
    return array.map(e => serializer.serialize(e, arrayValueType));
  }

  canDeserialize(value: any, metaData: DoValueMetaData): boolean {
    return objects.isArray(value);
  }

  deserialize(array: Array<any>, metaData: DoValueMetaData, deserializer: DataObjectDeserializer): Array<any> {
    const arrayValueType = doValueMetaData.getArrayValueType(metaData);
    return array.map(e => deserializer.deserialize(e, arrayValueType));
  }
}
