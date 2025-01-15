/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DataObjectDeserializer, DataObjectSerializer, DoNodeSerializer, DoValueMetaData, numbers} from '../../index';

export class NumberDoNodeSerializer implements DoNodeSerializer<number> {

  canSerialize(value: any, metaData: DoValueMetaData): boolean {
    return false; // not required for serialization: numbers can always directly be serialized
  }

  serialize(array: number, metaData: DoValueMetaData, serializer: DataObjectSerializer): any {
    throw new Error('Unsupported operation');
  }

  canDeserialize(value: any, metaData: DoValueMetaData): boolean {
    return typeof value === 'string' && Number === metaData?.type;
  }

  deserialize(num: string, metaData: DoValueMetaData, deserializer: DataObjectDeserializer): number {
    return numbers.ensure(num);
  }
}
