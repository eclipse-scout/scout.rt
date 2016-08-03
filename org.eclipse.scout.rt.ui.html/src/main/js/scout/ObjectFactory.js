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
 * @param initialRegistry (optional) map object containing the initial values used as object registry.
 *   All entries of the given initialRegistry will be cloned. Key = objectType, value = createFunc which
 *   returns a new Scout object instance.
 * @singleton
 */
scout.ObjectFactory = function() {
  // use createUniqueId() to generate a new ID
  this.uniqueIdSeqNo = 0;
  this._registry = {};
};

/**
 * Creates the object using the given type, or if undefined the objectType of the model. Only the constructor is called, but not the init() method.<p>
 * When scout.objectFactories contains a create function for the given objectType, this function is called.
 * Otherwise it tries to find the constructor function by the following logic:<br>
 * If the objectType provides a namespace, it is used. Otherwise it takes the default 'scout' namespace.
 * If the object type provides a variant, the variant is moved before the actual object type.
 *
 * Examples:
 * Object type is Outline -> constructor scout.Outline is called
 * Object type is myNamespace.Outline -> constructor myNamespace.Outline is called
 * Object type is Outline.MyVariant -> constructor scout.MyVariantOutline is called
 * Object type is myNamespace.Outline.MyVariant -> constructor myNamespace.MyVariantOutline is called
 *
 * @param model with property objectType
 * @param type optional, if defined will be used instead of model.objectType
 */
scout.ObjectFactory.prototype._createObjectByType = function(model, type) {
  // check if requested objectType / variant is registered
  var objectTypeParts, scoutClass, scoutObject,
    variant = '',
    namespaceName = '',
    objectType = type || model.objectType,
    createFunc = this._registry[objectType];

  if (createFunc) {
    // When a factory is registered for the given objectType
    scoutObject = createFunc(model);
  } else {
    // When no factory is registered for the given objectType
    objectTypeParts = objectType.split('.');

    var namespace = scout;

    // Extract namespace
    if (objectTypeParts.length >= 2 && window.hasOwnProperty(objectTypeParts[0])) {
      // FIXME CGU [6.1] maybe it would be better to define the namespace in scout.App to avoid name clashes in the window object (if objectType is named after a var in window)
      namespaceName = objectTypeParts[0];
      namespace = window[namespaceName];
      objectTypeParts = objectTypeParts.slice(1);
    }

    // Extract variant
    variant = '';
    if (objectTypeParts.length === 2) {
      variant = objectTypeParts[1];
    }

    scoutClass = variant + objectTypeParts[0];
    if (!namespace[scoutClass] && model.variantLenient && variant) {
      delete model.variantLenient;

      // Try without variant if variantLenient is true
      return this._createObjectByType(model, namespaceName + '.' + objectTypeParts[0]);
    }
    try {
      scoutObject = new namespace[scoutClass]();
    } catch (e) {
      // NOP - error handling below
    }
  }

  if (!scoutObject) {
    throw new Error('Failed to create Scout object for objectType:' + objectType + '. Either file/class \'scout.' + objectType +
      '\' does not exist, or no factory is registered to create an instance for the given objectType');
  }

  // Put object type used to create the object
  scoutObject.objectType = objectType;

  return scoutObject;
};

/**
 * Creates and initializes a new Scout object. Depending on the type of the vararg parameter the method does this:
 *
 * <ul>
 * <li><code>string</code> objectType. The second parameter must provide the model. A lookup is performed to
 *     find the constructor function.</li>
 * <li><code>object</code> model object. The object must have a property 'objectType'. The second parameter is not
 *     required. A lookup is performed to find the constructor function.</li>
 * </ul>
 *
 * When the provided model does not contain the property '_register', the property is set to false, which means the
 * object is not registered in the adapter registry. Which is the desired default behavior when we create local objects.
 *
 * When the provided model does not contain the property 'id', the property is set to a random, unqiue value
 * having the prefix 'ui'.
 *
 * The returned Scout object is initialized, by calling the init() function.
 *
 * Note: support to pass a constructor-function as vararg has been removed because we cannot determine the name of the
 * function at runtime (ECMA 6 Function.name is not supported by all browsers currently). Thus the objectType would be
 * missing and it makes no sense to pass an additional objectType when we already have the constructor.
 *
 *  @param vararg string or object
 *  @param model (optional) must be set when vararg is a string
 */
scout.ObjectFactory.prototype.create = function(vararg, model) {
  var scoutObject, objectType;
  model = model || {};
  if (typeof vararg === 'string') {
    objectType = vararg;
    scoutObject = this._createObjectByType(model, objectType);
  } else if (typeof vararg === 'object') {
    model = vararg;
    scoutObject = this._createObjectByType(model);
  } else {
    throw new Error('parameter vararg must be an objectType string or an object having an objectType property');
  }

  if (scoutObject instanceof scout.ModelAdapter && model._register === undefined) {
    model._register = false;
  }

  if (model.id === undefined) {
    model.id = this.createUniqueId();
  }

  scoutObject.init(model);
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
    this.register(objectType, scout.objectFactories[objectType]);
  }
};

scout.objectFactory = new scout.ObjectFactory(scout.objectFactories);
