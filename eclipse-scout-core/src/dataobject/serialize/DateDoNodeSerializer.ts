/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {dates, DoDeserializer, DoNodeSerializer, DoSerializer, DoValueMetaData} from '../../index';

export class DateDoNodeSerializer implements DoNodeSerializer<Date> {

  canSerialize(value: any, metaData: DoValueMetaData): boolean {
    return value instanceof Date;
  }

  serialize(value: Date, metaData: DoValueMetaData, serializer: DoSerializer): string {
    return dates.toJsonDate(value);
  }

  canDeserialize(value: any, metaData: DoValueMetaData): boolean {
    return Date === metaData?.type && typeof value === 'string';
  }

  deserialize(value: string, metaData: DoValueMetaData, deserializer: DoDeserializer): Date {
    return dates.parseJsonDate(value);
  }
}
