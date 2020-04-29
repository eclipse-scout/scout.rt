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
  }

  createInstance(options) {
    let i, namespaces, className,
      namespace = window.scout; // default namespace

    if (this.modelVariant) {
      className = this.modelVariant.name + this.objectType.name;
      namespaces = this.modelVariant.namespaces;
    } else {
      className = this.objectType.name;
      namespaces = this.objectType.namespaces;
    }

    if (namespaces.length) {
      namespace = window;
      for (i = 0; i < namespaces.length; i++) {
        namespace = namespace[namespaces[i]];
        if (!namespace) {
          throw this.error('Could not resolve namespace "' + namespaces[i] + '"');
        }
      }
    }

    if (!namespace[className]) { // Try without variant if variantLenient is true
      if (options.variantLenient && this.modelVariant) {
        let infoWithoutVariant = new TypeDescriptor(this.typeDescriptor, this.objectType, null);
        return infoWithoutVariant.createInstance(options);
      }
      throw this.error('Could not find "' + className + '" in namespace "' + namespaces.join('.') + '"');
    }

    return new namespace[className](options.model);
  }

  error(details) {
    return new Error('Failed to create object for objectType "' + this.typeDescriptor + '": ' + details);
  }

  static newInstance(typeDescriptor, options) {
    let info = TypeDescriptor.parse(typeDescriptor);
    return info.createInstance(options);
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
