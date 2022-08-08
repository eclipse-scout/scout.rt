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
import {objects, scout, TypeDescriptor} from './index';

import $ from 'jquery';

/**
 * @singleton
 */
export default class ObjectFactory {

  constructor() {
    // use createUniqueId() to generate a new ID
    this.uniqueIdSeqNo = 0;
    this._registry = new Map();
  }

  static NAMESPACE_SEPARATOR = '.';
  static MODEL_VARIANT_SEPARATOR = ':';

  /**
   * Creates an object from the given objectType. Only the constructor is called.
   *
   * OBJECT TYPE:
   *
   * A string based object type may consist of three parts: [name.space.]Class[:Variant]
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
   *   Object type: Outline                                      -> Constructor: Outline
   *   Object type: myNamespace.Outline                          -> Constructor: myNamespace.Outline
   *   Object type: Outline:MyVariant                            -> Constructor: scout.MyVariantOutline
   *   Object type: myNamespace.Outline:MyVariant                -> Constructor: myNamespace.MyVariantOutline
   *   Object type: Outline:myNamespace.MyVariant                -> Constructor: myNamespace.MyVariantOutline
   *   Object type: myNamespace.Outline:yourNamespace.MyVariant  -> Constructor: yourNamespace.MyVariantOutline
   *
   * RESOLVING THE CONSTRUCTOR:
   *
   * When the object factory contains a create function for the given objectType, this function is called.
   *
   * Otherwise, it tries to find the constructor function by the following logic:
   * If the objectType provides a name space, it is used. Otherwise it takes the default "scout" name space.
   * If the object type provides a variant ("Type:Variant"), the final object type is built by prepending
   * the variant to the type ("VariantType"). If no such type can be found and the option "variantLenient"
   * is set to true, a second attempt is made without the variant.
   *
   * @param {string|{new(): object}} objectType (mandatory) A class reference to the object to be created. Or a string describing the type of the object to be created.
   * @param {object} [options]  (optional)  Options object, currently supporting the following two options:
   *                               - model = Model object to be passed to the constructor or create function
   *                               - variantLenient = Flag to allow a second attempt to resolve the class
   *                                 without variant (see description above).
   */
  _createObjectByType(objectType, options) {
    if (typeof objectType !== 'string' && typeof objectType !== 'function') {
      throw new Error('missing or invalid object type');
    }
    options = options || {};

    let Class = null;
    let typeDescriptor = null;
    if (typeof objectType === 'string') {
      typeDescriptor = TypeDescriptor.parse(objectType);
      Class = typeDescriptor.resolve(options);
    } else if (typeof objectType === 'function') {
      Class = objectType;
    } else {
      throw new Error('Invalid objectType ' + objectType);
    }

    // Check if there is a factory registered for the Class. If yes, use the factory to create the instance.
    // The class may be null if the type could not be resolved. In that case the registry needs to contain a factory for the given objectType, otherwise no instance can be created.
    let createFunc = this.get(Class || objectType);
    if (createFunc) {
      // Use factory function registered for the given objectType
      let scoutObject = createFunc(options.model);
      if (!scoutObject) {
        throw new Error('Failed to create object for objectType "' + objectType + '": Factory function did not return a valid object');
      }
      return scoutObject;
    }

    if (!Class && typeDescriptor) {
      throw typeDescriptor.notFoundError();
    }

    return new Class(options.model);
  }

  /**
   * Creates and initializes a new Scout object. When the created object has an init function, the
   * model object is passed to that function. Otherwise the init call is omitted.
   *
   * @param {string|object|{new(): object}} objectType A class reference to the object to be created. Or a string with the requested objectType.
   *        This argument is optional, but if it is omitted, the argument "model" becomes mandatory and MUST contain a
   *        property named "objectType". If both, objectType and model, are set, the
   *        objectType parameter always wins before the model.objectType property.
   * @param {object} [model] The model object passed to the constructor function and to the init() method.
   *        This argument is mandatory if it is the first argument, otherwise it is
   *        optional (see above). This function may set/overwrite the properties 'id' and
   *        'objectType' on the model object.
   * @param {object} [options] Options object, see table below. This argument is optional.
   * @param {boolean} [options.variantLenient] Controls if the object factory may try to resolve the
   *        scoutClass without the model variant part if the initial objectType could not be resolved. Default is false.
   * @param {boolean} [options.ensureUniqueId] Controls if the resulting object should be assigned the
   *        attribute "id" if it is not defined. If the created object has an
   *        init() function, we also set the property 'id' on the model object
   *        to allow the init() function to copy the attribute from the model
   *        to the scoutObject.
   *        Default is true.
   * @throws Error if the argument list does not match the definition.
   */
  create(objectType, model, options) {
    // Normalize arguments
    if (typeof objectType === 'string' || typeof objectType === 'function') {
      options = options || {};
    } else if (objects.isPlainObject(objectType)) {
      options = model || {};
      model = objectType;
      if (!model.objectType) {
        throw new Error('Missing mandatory property "objectType" on model');
      }
      objectType = model.objectType;
    } else {
      throw new Error('Invalid arguments');
    }
    // noinspection JSUndefinedPropertyAssignment
    options.model = model;

    // Create object
    let scoutObject = this._createObjectByType(objectType, options);
    if (objects.isFunction(scoutObject.init)) {
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
  }

  /**
   * Returns a new unique ID to be used for Widgets/Adapters created by the UI
   * without a model delivered by the server-side client.
   * @return {string} ID with prefix 'ui'
   */
  createUniqueId() {
    return 'ui' + (++this.uniqueIdSeqNo).toString();
  }

  resolveTypedObjectType(objectType) {
    if (typeof objectType !== 'string') {
      return objectType;
    }
    let Class = TypeDescriptor.resolveType(objectType);
    if (Class) {
      return Class;
    }
    // No typed object available -> return string
    return objectType;
  }

  register(objectType, createFunc) {
    objectType = this.resolveTypedObjectType(objectType);
    this._registry.set(objectType, createFunc);
    $.log.isDebugEnabled() && $.log.debug('(ObjectFactory) registered create-function for objectType ' + objectType);
  }

  unregister(objectType) {
    objectType = this.resolveTypedObjectType(objectType);
    this._registry.delete(objectType);
    $.log.isDebugEnabled() && $.log.debug('(ObjectFactory) unregistered objectType ' + objectType);
  }

  get(objectType) {
    objectType = this.resolveTypedObjectType(objectType);
    return this._registry.get(objectType);
  }

  /**
   * Cannot init ObjectFactory until Log4Javascript is initialized.
   * That's why we call this method in the scout._init method.
   */
  init() {
    for (let [objectType, factory] of scout.objectFactories) {
      this.register(objectType, factory);
    }
  }

  static get() {
    return objectFactory;
  }

  static _set(newFactory) {
    objectFactory = newFactory;
  }
}

let objectFactory = new ObjectFactory();
