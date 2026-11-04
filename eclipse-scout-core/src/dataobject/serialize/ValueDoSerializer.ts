/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {BaseDoEntity, DataObjectDeserializer, DataObjectSerializer, DoNodeSerializer, doValueMetaData, DoValueMetaData, ValueDo} from '../../index';

export class ValueDoSerializer implements DoNodeSerializer<object> {

  protected static _INSTANCE: ValueDoSerializer = null;
  protected _typeNames = new Map<string, DoValueMetaData>();

  constructor() {
    this.addTypeName(ValueDo.LOCALE, doValueMetaData.createSimple(String));
    this.addTypeName(ValueDo.LONG, doValueMetaData.createSimple(Number));
    this.addTypeName(ValueDo.STRING, doValueMetaData.createSimple(String));
    this.addTypeName(ValueDo.BOOLEAN, doValueMetaData.createSimple(Boolean));
    this.addTypeName(ValueDo.DATE, doValueMetaData.createSimple(Date));
    this.addTypeName(ValueDo.DATE_TIME, doValueMetaData.createSimple(Date));
    this.addTypeName(ValueDo.DATE_TIME_WITH_TIME_ZONE, doValueMetaData.createSimple(String)); // cannot be parsed to Date by dates.parseJsonDate
    this.addTypeName(ValueDo.INTEGER, doValueMetaData.createSimple(Number));
    this.addTypeName(ValueDo.BYTE_ARRAY, doValueMetaData.createSimple(String)); // Base64 encoded string
    this.addTypeName(ValueDo.BINARY_RESOURCE);
    this.addTypeName(ValueDo.DO_ENTITY_LIST, doValueMetaData.createArray(BaseDoEntity));
    this.addTypeName(ValueDo.OBJECT);
    this.addTypeName(ValueDo.BIG_DECIMAL, doValueMetaData.createSimple(Number));
  }

  addTypeName(typeName: string, valueType?: DoValueMetaData) {
    if (!typeName) {
      return;
    }
    this._typeNames.set(typeName, valueType);
  }

  canSerialize(value: any, metaData: DoValueMetaData): boolean {
    return false; // no special serialize logic necessary
  }

  serialize(value: object, metaData: DoValueMetaData, serializer: DataObjectSerializer) {
    throw new Error('ValueDoSerializer is not implemented');
  }

  canDeserialize(value: any, metaData: DoValueMetaData): boolean {
    return value && this.isValueDo(value._type);
  }

  deserialize(valueDo: any, metaData: DoValueMetaData, deserializer: DataObjectDeserializer): object {
    if (valueDo instanceof BaseDoEntity && !deserializer.deserializeBaseDoEntity) {
      return valueDo;
    }
    const result = new ValueDo<any>();
    result._type = valueDo?._type;
    result.value = deserializer.deserialize(valueDo?.value, doValueMetaData.getFirstTypeArg(metaData) ?? this._typeNames.get(valueDo?._type));
    result._contributions = deserializer.deserialize(valueDo?._contributions);
    if (valueDo?._typeVersion && deserializer.retainTypeVersion(result)) {
      result._typeVersion = valueDo?._typeVersion;
    }
    return result;
  }

  isValueDo(typeName: string): boolean {
    return this._typeNames.has(typeName);
  }

  static get(): ValueDoSerializer {
    if (!ValueDoSerializer._INSTANCE) {
      // Do not create using scout.create as this instance is already used very early during source code parsing.
      // At this time no object factory is available yet.
      ValueDoSerializer._INSTANCE = new ValueDoSerializer();
    }
    return ValueDoSerializer._INSTANCE;
  }
}
