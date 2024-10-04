/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {DoDeserializer, DoSerializer, DoValueMetaData} from '../../index';

export interface DoNodeSerializer<TType> {

  canSerialize(value: any, metaData: DoValueMetaData): boolean;

  serialize(value: TType, metaData: DoValueMetaData, serializer: DoSerializer): any;

  canDeserialize(value: any, metaData: DoValueMetaData): boolean;

  deserialize(value: any, metaData: DoValueMetaData, deserializer: DoDeserializer): TType;
}
