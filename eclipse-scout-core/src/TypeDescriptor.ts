/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {arrays, ObjectFactory, scout, strings} from './index';

/**
 * This class contains a structured type description for a Scout class.
 * The model variant is optional.
 *
 * @param {string} typeDescriptor a string in the format '[namespace(s).]objectType[[:namespace(s).]modelVariant]'
 * @param {object} objectType
 * @param {object} [modelVariant]
 * @constructor
 */
export default class TypeDescriptor {

  constructor(typeDescriptor, objectType, modelVariant) {
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

  resolve(options) {
    let namespace = window.scout; // default namespace
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

  error(details) {
    return new Error('Failed to create object for objectType "' + this.typeDescriptor + '": ' + details);
  }

  notFoundError() {
    return this.error('Could not find "' + this.className + '" in namespace "' + this.namespaces.join('.') + '"');
  }

  static resolveType(typeDescriptor, options) {
    let info = TypeDescriptor.parse(typeDescriptor);
    return info.resolve(options);
  }

  /**
   * @param {string} typeDescriptor
   * @returns {TypeDescriptor}
   * @static
   */
  static parse(typeDescriptor) {
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

    function createInfo(name, namespaces) {
      return {
        name: name,
        namespaces: scout.nvl(namespaces, []),
        toString: () => {
          let parts = namespaces.slice();
          parts.push(name);
          return strings.join(ObjectFactory.NAMESPACE_SEPARATOR, parts);
        }
      };
    }

    function parseDescriptorPart(descriptorPart) {
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
