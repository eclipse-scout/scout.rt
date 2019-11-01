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
import {strings} from './index';
import {scout} from './index';
import {arrays} from './index';
import {ObjectFactory} from './index';

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

newInstance(options) {
  var i, namespaces, className,
    namespace = scout; // default namespace

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
      var infoWithoutVariant = new TypeDescriptor(this.typeDescriptor, this.objectType, null);
      return infoWithoutVariant.newInstance(options);
    }
    throw this.error('Could not find "' + className + '" in namespace "' + namespaces.join('.') + '"');
  }

  return new namespace[className](options.model);
}

error(details) {
  return new Error('Failed to create object for objectType "' + this.typeDescriptor + '": ' + details);
}

static newInstance(typeDescriptor, options) {
  var info = TypeDescriptor.parse(typeDescriptor);
  return info.newInstance(options);
}

/**
 * @param {string} typeDescriptor
 * @returns {TypeDescriptor}
 * @static
 */
static parse(typeDescriptor) {
  var typePart = null,
    variantPart = null;

  if (strings.contains(typeDescriptor, ObjectFactory.MODEL_VARIANT_SEPARATOR)) {
    var tmp = typeDescriptor.split(ObjectFactory.MODEL_VARIANT_SEPARATOR);
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
      toString: function() {
        var parts = namespaces.slice();
        parts.push(name);
        return strings.join(ObjectFactory.NAMESPACE_SEPARATOR, parts);
      }
    };
  }

  function parseDescriptorPart(descriptorPart) {
    var namespaces = [];

    if (strings.contains(descriptorPart, ObjectFactory.NAMESPACE_SEPARATOR)) {
      var namespaceParts = descriptorPart.split(ObjectFactory.NAMESPACE_SEPARATOR);
      namespaces = namespaceParts.slice(0, namespaceParts.length - 1);
      descriptorPart = arrays.last(namespaceParts);
    }

    return createInfo(descriptorPart, namespaces);
  }
}
}
