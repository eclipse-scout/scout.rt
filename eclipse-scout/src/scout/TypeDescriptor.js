import Scout from './Scout';
import Arrays from './utils/Arrays';
import * as strings from './utils/strings2';

export const NAMESPACE_SEPARATOR = '.';
export const MODEL_VARIANT_SEPARATOR = ':';

export default class TypeDescriptor {

  constructor(typeDescriptor, objectType, modelVariant) {
    this.typeDescriptor = typeDescriptor;
    this.objectType = objectType;
    this.modelVariant = modelVariant;
  }

  newInstance(options) {
    let cls = this.objectType.name;
    if (this.modelVariant) {
      cls = this.modelVariant.name + cls;
    }
    if (this.modelVariant) {
      className = this.modelVariant.name + this.objectType.name;
    } else {
      className = this.objectType.name;
    }

    /*
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
                throw this.error('Could not resolve namespace '' + namespaces[i] + ''');
            }
        }
    }

    if (!namespace[className]) { // Try without variant if variantLenient is true
        if (options.variantLenient && this.modelVariant) {
            var infoWithoutVariant = new TypeDescriptor(this.typeDescriptor, this.objectType, null);
            return infoWithoutVariant.newInstance(options);
        }
        throw this.error('Could not find '' + className + '' in namespace '' + namespaces.join('.') + ''');
    }

    return new namespace[className](options.model);*/
  };

  error(details) {
    return new Error('Failed to create object for objectType "' + this.typeDescriptor + '": ' + details);
  };

  static newInstance(typeDescriptor, options) {
    var info = TypeDescriptor.parse(typeDescriptor);
    return info.newInstance(options);
  };

  static parse(typeDescriptor) {
    var typePart = null,
      variantPart = null;

    if (strings.contains(typeDescriptor, MODEL_VARIANT_SEPARATOR)) {
      var tmp = typeDescriptor.split(MODEL_VARIANT_SEPARATOR);
      typePart = parseDescriptorPart(tmp[0]);
      variantPart = parseDescriptorPart(tmp[1]);

      // when type has namespaces but the variant has not, use type namespaces for variant too
      if (Arrays.empty(variantPart.namespaces) && !Arrays.empty(typePart.namespaces)) {
        variantPart.namespaces = typePart.namespaces;
      }
    } else {
      typePart = parseDescriptorPart(typeDescriptor);
    }

    return new TypeDescriptor(typeDescriptor, typePart, variantPart);

    function createInfo(name, namespaces) {
      return {
        name: name,
        namespaces: Scout.nvl(namespaces, []),
        toString: function() {
          var parts = namespaces.slice();
          parts.push(name);
          return strings.join(NAMESPACE_SEPARATOR, parts);
        }
      };
    }

    function parseDescriptorPart(descriptorPart) {
      var namespaces = [];

      if (strings.contains(descriptorPart, NAMESPACE_SEPARATOR)) {
        var namespaceParts = descriptorPart.split(NAMESPACE_SEPARATOR);
        namespaces = namespaceParts.slice(0, namespaceParts.length - 1);
        descriptorPart = Arrays.last(namespaceParts);
      }

      return createInfo(descriptorPart, namespaces);
    }
  };

}
