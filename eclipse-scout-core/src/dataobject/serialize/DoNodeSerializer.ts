/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DataObjectDeserializer, DataObjectSerializer, DoValueMetaData} from '../../index';

/**
 * (De)Serializer for data object attributes (DoNode).
 */
export interface DoNodeSerializer<TType> {

  /**
   * Specifies if this instance can serialize the given value.
   * @param value The value that should be serialized.
   * @param metaData The optional metadata of the attribute the value comes from (as specified in the TypeScript source code).
   * @returns true if this instance can serialize the given value.
   */
  canSerialize(value: any, metaData: DoValueMetaData): boolean;

  /**
   * Converts the given value to its serialized representation.
   * This method is only called for value-metadata-combinations that have passed the {@link canSerialize} check.
   *
   * @param value The value to convert.
   * @param metaData The metaData of the attribute the value comes from (as specified in the TypeScript source code).
   * @param serializer The serializer instance that might be used to perform nested serialization if required. Is never null.
   * @returns The serialized representation of the give value.
   */
  serialize(value: TType, metaData: DoValueMetaData, serializer: DataObjectSerializer): any;

  /**
   * Specifies if this instance can deserialize the given value.
   * @param value The value that should be deserialized.
   * @param metaData The optional metadata of the attribute the value will be written to. Specifies the target data type as declared in the TypeScript source code.
   * @returns true if this instance can convert the given value into the data type specified by the metaData.
   */
  canDeserialize(value: any, metaData: DoValueMetaData): boolean;

  /**
   * Converts the given value to the datatype specified by the metaData.
   * This method is only called for value-metadata-combinations that have passed the {@link canDeserialize} check.
   *
   * @param value The value to convert.
   * @param metaData The metaData of the attribute the value will be written to. Specifies the target data type as declared in the TypeScript source code.
   * @param serializer The serializer instance that might be used to perform nested deserialization if required. Is never null.
   * @returns The deserialized representation of the give value.
   */
  deserialize(value: any, metaData: DoValueMetaData, deserializer: DataObjectDeserializer): TType;
}
