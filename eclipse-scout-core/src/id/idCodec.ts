/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {strings} from '../index';

/**
 * Utility to parse id strings into their components and create qualified or unqualified strings from their components.
 */
export const idCodec = {

  TYPE_NAME_DELIMITER: ':',
  COMPONENT_DELIMITER: ';',
  SIGNATURE_DELIMITER: '_-~SIG~-_',

  /**
   * Parses the given id string into its parts. Automatically detects if the id is qualified or unqualified based on content.
   * @param rawId The raw string to parse (mandatory).
   * @param typeNameProvider An optional provider for the typeName. Called in case the id is unqualified to compute its typeName. Must return the corresponding typeName for the given id.
   * @returns the parsed {@link IdInfo} or null if there is no value.
   */
  parse<TTypeName extends string>(rawId: string, typeNameProvider?: () => TTypeName): IdInfo<TTypeName> {
    if (strings.empty(rawId)) {
      return null;
    }
    const isQualified = strings.contains(rawId, idCodec.TYPE_NAME_DELIMITER);
    if (isQualified) {
      return idCodec.fromQualified(rawId);
    }
    const typeName = typeNameProvider ? typeNameProvider() : null;
    return idCodec.fromUnqualified(rawId, typeName);
  },

  /**
   * Parses the given unqualified id string into its parts.
   * @param value The raw string to parse (mandatory).
   * @param typeName The typeName of the id.
   * @returns the parsed {@link IdInfo} or null if there is no value.
   */
  fromUnqualified<TTypeName extends string>(value: string, typeName: TTypeName): IdInfo<TTypeName> {
    if (strings.empty(value)) {
      return null;
    }
    const {id, signature} = _splitSignature(value);
    const elements = id.split(idCodec.COMPONENT_DELIMITER);
    return {
      typeName,
      value: id,
      elements,
      signature
    };
  },

  /**
   * Parses the given qualified id string into its parts.
   * @param qualifiedId The string to parse including the typeName prefix.
   * @returns the parsed {@link IdInfo} or null if there is no value.
   */
  fromQualified<TTypeName extends string>(qualifiedId: string): IdInfo<TTypeName> {
    if (strings.empty(qualifiedId)) {
      return null;
    }
    const {id, typeName} = _splitTypeName(qualifiedId);
    return idCodec.fromUnqualified(id, typeName as TTypeName);
  },

  /**
   * @returns the given {@link IdInfo} converted to its unqualified string representation. Unqualified means only value and signature are part of the result, the typeName is omitted. Returns null if there is no input.
   */
  toUnqualified(id: IdInfo): string {
    if (!id) {
      return null;
    }
    const unqualified = id.value;
    if (id.signature) {
      return unqualified + idCodec.SIGNATURE_DELIMITER + id.signature;
    }
    return unqualified;
  },

  /**
   * @returns the given {@link IdInfo} converted to its qualified string representation including typeName, value and signature. Returns null if there is no input.
   */
  toQualified(id: IdInfo): string {
    if (!id) {
      return null;
    }
    const unqualified = idCodec.toUnqualified(id);
    if (id.typeName) {
      return id.typeName + idCodec.TYPE_NAME_DELIMITER + unqualified;
    }
    return unqualified;
  }
};

export interface IdInfo<TIdTypeName extends string = string> {
  /**
   * The typeName of the id. Typically, set using the @IdTypeName annotation on an IId instance on the Java backend.
   */
  typeName?: TIdTypeName;
  /**
   * The full value of the id. If it has multiple elements (e.g. for composites) all elements are part of this value including the separators.
   */
  value: string;
  /**
   * The elements of the id. These are the parts of the value split by their separator.
   */
  elements?: string[];
  /**
   * The signature of the id.
   */
  signature?: string;
}

function _splitTypeName(qualifiedId: string): { typeName: string; id: string } {
  if (!qualifiedId) {
    return {typeName: null, id: null};
  }
  let firstColonPos = qualifiedId.indexOf(idCodec.TYPE_NAME_DELIMITER);
  if (firstColonPos < 0) {
    return {
      typeName: null,
      id: qualifiedId
    };
  }
  return {
    typeName: qualifiedId.substring(0, firstColonPos),
    id: qualifiedId.substring(firstColonPos + idCodec.TYPE_NAME_DELIMITER.length)
  };
}

function _splitSignature(value: string): { id: string; signature: string } {
  let splitPos = value.lastIndexOf(idCodec.SIGNATURE_DELIMITER);
  if (splitPos < 0) {
    // no signature found
    return {id: value, signature: null};
  }
  return {
    id: value.substring(0, splitPos),
    signature: value.substring(splitPos + idCodec.SIGNATURE_DELIMITER.length)
  };
}
