/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractConstructor, Constructor, DataObjectDeserializer, DataObjectSerializer, DoNodeSerializer, DoValueMetaData, Id, IdParser} from '../../index';

export class IdDoNodeSerializer implements DoNodeSerializer<Id<any>> {

  canSerialize(value: any, metaData: DoValueMetaData): boolean {
    return value instanceof Id;
  }

  serialize(value: Id<any>, metaData: DoValueMetaData, serializer: DataObjectSerializer): any {
    // It's not possible to perfectly compute if the backend expects a qualified or unqualified id (if the Java DoNode is concrete or not, see ScoutDataObjectSerializerProvider.java).
    // Therefore, always serialize qualified, which might include too much information.
    // If the Java deserializer uses unqualified parsing, the unnecessary extra information is removed (see IdCodec.java#fromUnqualifiedUnchecked).
    return value.toQualified();
  }

  canDeserialize(value: any, metaData: DoValueMetaData): boolean {
    if (!(typeof value === 'string')) {
      return false;
    }
    if (!metaData?.type) {
      return false;
    }
    return this.isIdClass(metaData.type) || Id.isPrototypeOf(metaData.type);
  }

  deserialize(rawId: string, metaData: DoValueMetaData, deserializer: DataObjectDeserializer): Id<any> {
    return IdParser.get().parse(metaData.type, rawId, () => this.detectConcreteTypeName(metaData));
  }

  isIdClass(candidate: Constructor | AbstractConstructor): boolean {
    return candidate === Id;
  }

  detectConcreteTypeName(metaData: DoValueMetaData): string {
    if (!metaData?.type) {
      return null;
    }

    // abstract ID type (E.g. Id<number>): type name cannot be known
    if (this.isIdClass(metaData.type)) {
      return null;
    }

    // TypeName is part of the datatype declaration (E.g. UuId<'scout.MyIdType'>): parse from metadata type argument
    const typeNameFromMetaData = this.extractIdTypeName(metaData);
    if (typeNameFromMetaData) {
      return typeNameFromMetaData;
    }

    // Type name cannot be computed. Check if ID type is fixed on instance (e.g. using the idTypeName annotation or other constructor implementation)
    const IdType = metaData.type;
    const id = new IdType() as Id<any>;
    return id.typeName;
  }

  extractIdTypeName(metaData: DoValueMetaData): string {
    // delegate to static function on ID class of the metaData.
    // required so that IDs may change their TypeName detection based on the generics (it must not always be the first type arg).
    return metaData.type['extractIdTypeName'](metaData);
  }
}
