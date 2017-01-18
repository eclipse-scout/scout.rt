/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/

/**
 * @singleton
 */
scout.ObjectFactory = function() {
  // use createUniqueId() to generate a new ID
  this.uniqueIdSeqNo = 0;
  this._registry = {};
};

scout.ObjectFactory.NAMESPACE_SEPARATOR = ".";
scout.ObjectFactory.MODEL_VARIANT_SEPARATOR = ":";

/**
 * Creates an object from the given objectType. Only the constructor is called.
 *
 * OBJECT TYPE:
 *
 * An object type may consist of three parts: [name.space.]Class[:Variant]
 * 1. Name spaces (optional)
 *    All name space parts have to end with a dot ('.') character. If this part is omitted, the default
 *    name space "scout." is assumed.
 *    Examples: "scout.", "my.custom.namespace."
 * 2. Scout class name (mandatory)
 *    Examples: "Desktop", "Session", "StringField"
 * 3. Model variant (optional)
 *    Custom variants of a class can be created by adding the custom class prefix after
 *    the Scout class name and a colon character (':'). This prefix is then combined with
 *    the class name.
 *    Examples: ":Offline", ":Horizontal"
 *
 * Full examples:
 *   Object type: Outline                        -> Constructor: scout.Outline
 *   Object type: myNamespace.Outline            -> Constructor: myNamespace.Outline
 *   Object type: Outline:MyVariant              -> Constructor: scout.MyVariantOutline
 *   Object type: myNamespace.Outline:MyVariant  -> Constructor: myNamespace.MyVariantOutline
 *
 * RESOLVING THE CONSTRUCTOR:
 *
 * When scout.objectFactories contains a create function for the given objectType, this function is called.
 *
 * Otherwise it tries to find the constructor function by the following logic:
 * If the objectType provides a name space, it is used. Otherwise it takes the default "scout" name space.
 * If the object type provides a variant ("Type:Variant"), the final object type is built by prepending
 * the variant to the type ("VariantType"). If no such type can be found and the option "variantLenient"
 * is set to true, a second attempt is made without the variant.
 *
 * @param objectType (mandatory) String describing the type of the object to be created.
 * @param options    (optional)  Options object, currently supporting the following two options:
 *                               - model = Model object to be passed to the constructor or create function
 *                               - variantLenient = Flag to allow a second attempt to resolve the class
 *                                 without variant (see description above).
 */
scout.ObjectFactory.prototype._createObjectByType = function(objectType, options) {
  if (typeof objectType !== 'string') {
    throw new Error('missing or invalid object type');
  }
  options = options || {};

  var scoutObject,
    failMsgPrefix = 'Failed to create object for objectType "' + objectType + '": ';

  var createFunc = this._registry[objectType];
  if (createFunc) {
    // 1.
    // Use factory function registered for the given objectType
    scoutObject = createFunc(options.model);
    if (!scoutObject) {
      throw new Error(failMsgPrefix + 'Factory function did not return a valid object');
    }
  } else {
    // 2.
    // Resolve class by name
    var namespaceParts = objectType.split(scout.ObjectFactory.NAMESPACE_SEPARATOR);
    var namespaces = namespaceParts.slice(0, namespaceParts.length - 1);
    var objectTypeParts = namespaceParts[namespaceParts.length - 1]
      .split(scout.ObjectFactory.MODEL_VARIANT_SEPARATOR)
      // Ensure max. two objectType parts (ignore multiple separators)
      .slice(0, 2);
    // Change "Type:Custom" to "CustomType"
    var scoutClass = objectTypeParts.slice().reverse().join('');

    // Find name space
    var namespace = scout; // <-- default
    if (namespaces.length) {
      namespace = window;
      for (var i = 0; i < namespaces.length; i++) {
        namespace = namespace[namespaces[i]];
        if (!namespace) {
          throw new Error(failMsgPrefix + 'Could not resolve namespace "' + namespaces[i] + '"');
        }
      }
    }
    if (!namespace[scoutClass]) {
      if (options.variantLenient && objectTypeParts.length === 2) {
        // Try without variant if variantLenient is true
        return this._createObjectByType(namespaces.concat(objectTypeParts[0]).join(scout.ObjectFactory.NAMESPACE_SEPARATOR), options);
      }
      throw new Error(failMsgPrefix + 'Could not find "' + scoutClass + '" in namespace "' + namespaces.join('.') + '"');
    }
    scoutObject = new namespace[scoutClass](options.model);
  }

  return scoutObject;
};

/**
 * Creates and initializes a new Scout object. When the created object has an init function, the
 * model object is passed to that function. Otherwise the init call is omitted.
 *
 * @param objectType A string with the requested objectType. This argument is optional, but if it
 *                   is omitted, the argument "model" becomes mandatory and MUST contain a
 *                   property named "objectType". If both, objectType and model, are set, the
 *                   objectType parameter always wins before the model.objectType property.
 * @param model      The model object passed to the constructor function and to the init() method.
 *                   This argument is mandatory if it is the first argument, otherwise it is
 *                   optional (see above). This function may set/overwrite the properties 'id' and
 *                   'objectType' on the model object.
 * @param options    Options object, see table below. This argument is optional.
 *
 * An error is thrown if the argument list does not match this definition.
 *
 * List of options:
 *
 * OPTION                   DEFAULT VALUE   DESCRIPTION
 * ------------------------------------------------------------------------------------------------------
 * variantLenient           false           Controls if the object factory may try to resolve the
 *                                          scoutClass without the model variant part if the initial
 *                                          objectType could not be resolved.
 *
 * ensureUniqueId           true            Controls if the resulting object should be assigned the
 *                                          attribute "id" if it is not defined. If the created object has an
 *                                          init() function, we also set the property 'id' on the model object
 *                                          to allow the init() function to copy the attribute from the model
 *                                          to the scoutObject.
 */
scout.ObjectFactory.prototype.create = function(objectType, model, options) {
  // Normalize arguments
  if (typeof objectType === 'string') {
    options = options || {};
  } else if (scout.objects.isPlainObject(objectType)) {
    options = model || {};
    model = objectType;
    if (!model.objectType) {
      throw new Error('Missing mandatory property "objectType" on model');
    }
    objectType = model.objectType;
  } else {
    throw new Error('Invalid arguments');
  }
  options.model = model;

  // Create object
  var scoutObject = this._createObjectByType(objectType, options);
  if (scout.objects.isFunction(scoutObject.init)) {
    if (model) {
      if (model.id === undefined && scout.nvl(options.ensureUniqueId, true)) {
        model.id = this.createUniqueId();
      }
      model.objectType = objectType;
    }
    // Initialize object
    scoutObject.init(model);
  }

  if (scoutObject.id === undefined && scout.nvl(options.ensureUniqueId, true)) {
    scoutObject.id = this.createUniqueId();
  }
  if (scoutObject.objectType === undefined) {
    scoutObject.objectType = objectType;
  }

  return scoutObject;
};

/**
 * Returns a new unique ID to be used for Widgets/Adapters created by the UI
 * without a model delivered by the server-side client.
 * @return string ID with prefix 'ui'
 */
scout.ObjectFactory.prototype.createUniqueId = function() {
  return 'ui' + (++this.uniqueIdSeqNo).toString();
};

scout.ObjectFactory.prototype.register = function(objectType, createFunc) {
  $.log.debug('(ObjectFactory) registered create-function for objectType ' + objectType);
  this._registry[objectType] = createFunc;
};

scout.ObjectFactory.prototype.unregister = function(objectType) {
  $.log.debug('(ObjectFactory) unregistered objectType ' + objectType);
  delete this._registry[objectType];
};

scout.ObjectFactory.prototype.get = function(objectType) {
  return this._registry[objectType];
};

/**
 * Cannot init ObjectFactory until Log4Javascript is initialized.
 * That's why we call this method in the scout._init method.
 */
scout.ObjectFactory.prototype.init = function() {
  for (var objectType in scout.objectFactories) {
    if (scout.objectFactories.hasOwnProperty(objectType)) {
      this.register(objectType, scout.objectFactories[objectType]);
    }
  }
};

scout.objectFactory = new scout.ObjectFactory(scout.objectFactories);
