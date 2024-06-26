/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Constructor, ObjectFactory, scout, strings} from './index';

export interface ObjectTypePart {
  name: string;
  namespaces?: string[];
}

export interface TypeDescriptorOptions {
  /**
   * Controls if the type may be resolved without the model variant part if the initial objectType could not be resolved. Default is false.
   */
  variantLenient?: boolean;
}

/**
 * This class contains a structured type description for a Scout class.
 */
export class TypeDescriptor {
  typeDescriptor: string;
  objectType: ObjectTypePart;
  modelVariant: ObjectTypePart;
  className: string;
  namespaces: string[];

  /**
   * @param typeDescriptor a string in the format '[namespace(s).]objectType[[:namespace(s).]modelVariant]'
   */
  constructor(typeDescriptor: string, objectType: ObjectTypePart, modelVariant?: ObjectTypePart) {
    this.typeDescriptor = typeDescriptor;
    this.objectType = objectType;
    this.modelVariant = modelVariant;

    if (this.modelVariant) {
      this.className = this.modelVariant.name + this.objectType.name;
      this.namespaces = this.modelVariant.namespaces;
    } else {
      this.className = this.objectType.name;
      this.namespaces = this.objectType.namespaces;
    }
  }

  resolve(options?: TypeDescriptorOptions): Constructor {
    let namespace = window['scout']; // default namespace
    options = options || {};

    if (this.namespaces.length) {
      namespace = window;
      for (let i = 0; i < this.namespaces.length; i++) {
        namespace = namespace[this.namespaces[i]];
        if (!namespace) {
          throw this.error('Could not resolve namespace "' + this.namespaces[i] + '"');
        }
      }
    }

    if (!namespace[this.className]) { // Try without variant if variantLenient is true
      if (options.variantLenient && this.modelVariant) {
        let infoWithoutVariant = new TypeDescriptor(this.typeDescriptor, this.objectType, null);
        return infoWithoutVariant.resolve(options);
      }
      return null;
    }

    return namespace[this.className];
  }

  error(details: string): Error {
    return new Error('Failed to create object for objectType "' + this.typeDescriptor + '": ' + details);
  }

  notFoundError(): Error {
    return this.error('Could not find "' + this.className + '" in namespace "' + this.namespaces.join('.') + '"');
  }

  static resolveType(typeDescriptor: string, options?: TypeDescriptorOptions): new() => object {
    let info = TypeDescriptor.parse(typeDescriptor);
    return info.resolve(options);
  }

  static parse(typeDescriptor: string): TypeDescriptor {
    let typePart = null,
      variantPart = null;

    if (strings.contains(typeDescriptor, ObjectFactory.MODEL_VARIANT_SEPARATOR)) {
      let tmp = typeDescriptor.split(ObjectFactory.MODEL_VARIANT_SEPARATOR);
      typePart = parseDescriptorPart(tmp[0]);
      variantPart = parseDescriptorPart(tmp[1]);

      // when type has namespaces but the variant has not, use type namespaces for variant too
      if (arrays.empty(variantPart.namespaces) && !arrays.empty(typePart.namespaces)) {
        variantPart.namespaces = typePart.namespaces;
      }
    } else {
      typePart = parseDescriptorPart(typeDescriptor);
    }

    return new TypeDescriptor(typeDescriptor, typePart, variantPart);

    function createInfo(name: string, namespaces?: string[]) {
      return {
        name: name,
        namespaces: scout.nvl(namespaces, []),
        toString: () => {
          let parts = namespaces.slice();
          parts.push(name);
          return strings.join(ObjectFactory.NAMESPACE_SEPARATOR, ...parts);
        }
      };
    }

    function parseDescriptorPart(descriptorPart: string) {
      let namespaces = [];

      if (strings.contains(descriptorPart, ObjectFactory.NAMESPACE_SEPARATOR)) {
        let namespaceParts = descriptorPart.split(ObjectFactory.NAMESPACE_SEPARATOR);
        namespaces = namespaceParts.slice(0, namespaceParts.length - 1);
        descriptorPart = arrays.last(namespaceParts);
      }

      return createInfo(descriptorPart, namespaces);
    }
  }
}
