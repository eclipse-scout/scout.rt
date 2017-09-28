/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/**
 * This class contains a structured type description for a Scout class.
 * The model variant is optional.
 *
 * @param {string} typeDescriptor a string in the format '[namespace(s).]objectType[[:namespace(s).]modelVariant]'
 * @param {object} objectType
 * @param {object} [modelVariant]
 * @constructor
 */
scout.TypeDescriptor = function(typeDescriptor, objectType, modelVariant) {
  this.typeDescriptor = typeDescriptor;
  this.objectType = objectType;
  this.modelVariant = modelVariant;
};

scout.TypeDescriptor.prototype.newInstance = function(options) {
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
      var infoWithoutVariant = new scout.TypeDescriptor(this.typeDescriptor, this.objectType, null);
      return infoWithoutVariant.newInstance(options);
    }
    throw this.error('Could not find "' + className + '" in namespace "' + namespaces.join('.') + '"');
  }

  return new namespace[className](options.model);
};

scout.TypeDescriptor.prototype.error = function(details) {
  return new Error('Failed to create object for objectType "' + this.typeDescriptor + '": ' + details);
};

scout.TypeDescriptor.newInstance = function(typeDescriptor, options) {
  var info = scout.TypeDescriptor.parse(typeDescriptor);
  return info.newInstance(options);
};

/**
 * @param {string} typeDescriptor
 * @returns {scout.TypeDescriptor}
 * @static
 */
scout.TypeDescriptor.parse = function(typeDescriptor) {
  var typePart = null,
    variantPart = null;

  if (scout.strings.contains(typeDescriptor, scout.ObjectFactory.MODEL_VARIANT_SEPARATOR)) {
    var tmp = typeDescriptor.split(scout.ObjectFactory.MODEL_VARIANT_SEPARATOR);
    typePart = parseDescriptorPart(tmp[0]);
    variantPart = parseDescriptorPart(tmp[1]);

    // when type has namespaces but the variant has not, use type namespaces for variant too
    if (scout.arrays.empty(variantPart.namespaces) && !scout.arrays.empty(typePart.namespaces)) {
      variantPart.namespaces = typePart.namespaces;
    }
  } else {
    typePart = parseDescriptorPart(typeDescriptor);
  }

  return new scout.TypeDescriptor(typeDescriptor, typePart, variantPart);

  function createInfo(name, namespaces) {
    return {
      name: name,
      namespaces: scout.nvl(namespaces, []),
      toString: function() {
        var parts = namespaces.slice();
        parts.push(name);
        return scout.strings.join(scout.ObjectFactory.NAMESPACE_SEPARATOR, parts);
      }
    };
  }

  function parseDescriptorPart(descriptorPart) {
    var namespaces = [];

    if (scout.strings.contains(descriptorPart, scout.ObjectFactory.NAMESPACE_SEPARATOR)) {
      var namespaceParts = descriptorPart.split(scout.ObjectFactory.NAMESPACE_SEPARATOR);
      namespaces = namespaceParts.slice(0, namespaceParts.length - 1);
      descriptorPart = scout.arrays.last(namespaceParts);
    }

    return createInfo(descriptorPart, namespaces);
  }
};





