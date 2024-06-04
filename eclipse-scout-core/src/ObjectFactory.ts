/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FullModelOf, InitModelOf, ModelAdapter, ModelOf, ObjectModel, objects, ObjectUuidProvider, scout, TableRow, TreeNode, TypeDescriptor, TypeDescriptorOptions, Widget} from './index';
import $ from 'jquery';

export type ObjectCreator = (model?: any) => object;
export type ObjectType<TObject = object, TModel = ModelOf<TObject>> = string | (new(model?: TModel) => TObject);

export interface ObjectFactoryOptions extends TypeDescriptorOptions {
  /**
   * Model object to be passed to the constructor or create function.
   */
  model?: object;

  /**
   * Controls if the resulting object should be assigned the attribute "id" if it is not defined.
   * If the created object has an init() function, we also set the property 'id' on the model object to allow the init() function to copy the attribute from the model to the scoutObject.
   * Default is true.
   */
  ensureUniqueId?: boolean;
}

export interface RegisterNamespaceOptions {
  /**
   * List of object names that are allowed to be replaced, see the description for the thrown error. Default is an empty array.
   */
  allowedReplacements?: string[];
}

/**
 * @singleton
 */
export class ObjectFactory {
  initialized: boolean;

  protected _registry: Map<ObjectType, ObjectCreator>;
  protected _objectTypeMap: Map<new() => object, string>;

  constructor() {
    this.initialized = false;
    this._registry = new Map();
    this._objectTypeMap = new Map();
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
   * If the objectType provides a name space, it is used. Otherwise, it takes the default "scout" name space.
   * If the object type provides a variant ("Type:Variant"), the final object type is built by prepending
   * the variant to the type ("VariantType"). If no such type can be found and the option "variantLenient"
   * is set to true, a second attempt is made without the variant.
   *
   * @param objectType A class reference to the object to be created. Or a string describing the type of the object to be created.
   */
  protected _createObjectByType<T>(objectType: ObjectType<T>, options?: ObjectFactoryOptions): any {
    if (typeof objectType !== 'string' && typeof objectType !== 'function') {
      throw new Error('missing or invalid object type');
    }
    options = options || {};

    let Class = null;
    let typeDescriptor: TypeDescriptor = null;
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
   * model object is passed to that function. Otherwise, the init call is omitted.
   *
   * @param objectTypeOrModel A class reference to the object to be created. Or a string with the requested objectType.
   *        This argument is optional, but if it is omitted, the argument "model" becomes mandatory and MUST contain a
   *        property named "objectType". If both, objectType and model, are set, the
   *        objectType parameter always wins before the model.objectType property.
   * @param modelOrOptions The model object passed to the constructor function and to the init() method.
   *        This argument is mandatory if it is the first argument, otherwise it is
   *        optional (see above). This function may set/overwrite the properties 'id' and
   *        'objectType' on the model object.
   * @throws Error if the argument list does not match the definition.
   */
  create<T extends object>(objectTypeOrModel: ObjectType<T> | FullModelOf<T>, modelOrOptions?: InitModelOf<T>, options?: ObjectFactoryOptions): T {
    // Normalize arguments
    let objectType: ObjectType<T>;
    let model: ObjectModel<T>;
    if (typeof objectTypeOrModel === 'string' || typeof objectTypeOrModel === 'function') {
      options = options || {};
      model = modelOrOptions;
      objectType = objectTypeOrModel;
    } else if (objects.isPlainObject(objectTypeOrModel)) {
      options = modelOrOptions || {};
      model = objectTypeOrModel;
      if (!model.objectType) {
        throw new Error('Missing mandatory property "objectType" on model');
      }
      objectType = model.objectType;
    } else {
      throw new Error('Invalid arguments');
    }
    options.model = model;

    // Create object
    let scoutObject = this._createObjectByType(objectType, options);
    // FIXME bsh [js-bookmark] How can be determine whether an ID should be generated? Is this even needed for widgets' (TreeNodes and TableRows seem to need it because of Maps in Tree/Table, but this could probably changed to ES6-Maps)
    let ensureUniqueId = scout.nvl(options.ensureUniqueId, scoutObject instanceof Widget || scoutObject instanceof TreeNode || scoutObject instanceof TableRow || scoutObject instanceof ModelAdapter);

    // Initialize object
    if (objects.isFunction(scoutObject.init)) {
      if (model) {
        if (model.id === undefined && ensureUniqueId) {
          model.id = ObjectUuidProvider.createUiId();
        }
        model.objectType = this.getObjectType(objectType);
      }
      scoutObject.init(model);
    }

    if (scoutObject.id === undefined && ensureUniqueId) {
      scoutObject.id = ObjectUuidProvider.createUiId();
    }
    if (scoutObject.objectType === undefined) {
      scoutObject.objectType = this.getObjectType(objectType);
    }

    return scoutObject;
  }

  /**
   * @deprecated Use {@link ObjectUuidProvider.createUiId} instead.
   */
  createUniqueId(): string {
    return ObjectUuidProvider.createUiId();
  }

  resolveTypedObjectType(objectType: ObjectType): ObjectType {
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

  register(objectType: ObjectType, createFunc: ObjectCreator) {
    objectType = this.resolveTypedObjectType(objectType);
    this._registry.set(objectType, createFunc);
    $.log.isDebugEnabled() && $.log.debug('(ObjectFactory) registered create-function for objectType ' + this._objectTypeToDebugStr(objectType));
  }

  unregister(objectType: ObjectType) {
    objectType = this.resolveTypedObjectType(objectType);
    this._registry.delete(objectType);
    $.log.isDebugEnabled() && $.log.debug('(ObjectFactory) unregistered objectType ' + this._objectTypeToDebugStr(objectType));
  }

  protected _objectTypeToDebugStr(objectType: ObjectType) {
    if (typeof objectType === 'string') {
      return objectType;
    }
    // Name property is obfuscated in production code, only use it for debug purposes
    return objectType.name;
  }

  get(objectType: ObjectType): ObjectCreator {
    objectType = this.resolveTypedObjectType(objectType);
    return this._registry.get(objectType);
  }

  /**
   * Returns the object type as string for the given class.
   */
  getObjectType(Class: ObjectType): string {
    if (typeof Class === 'string') {
      return Class;
    }
    return this._objectTypeMap.get(Class);
  }

  /**
   * Cannot init ObjectFactory until Log4Javascript is initialized.
   * That's why we call this method in the scout._init method.
   */
  init() {
    for (let [objectType, factory] of scout.objectFactories) {
      this.register(objectType, factory);
    }
    this.initialized = true;
  }

  /**
   * The namespace is an object on the window containing object names as keys and object references as values.
   * The type of the object is not restricted, mostly it is a class but may also be a function or a plain object used as enum.
   * <p>
   * Registering classes enables creating an instance of the class by its name using the ObjectFactory (e.g. scout.create(Button, {}) ).
   * This is necessary to import string based models, e.g. if the model is delivered by a server (Scout Classic).
   * Registering objects in general is also necessary, if the application does not use EcmaScript imports or the imports are treated as externals and transpiled to a window lookup (see Webpack root external for details).
   * <p>
   * Registering the namespace also makes it possible to resolve the name of a class including its namespace for any registered class, even if the code is minified.
   * This is used by the ObjectFactory to store the objectType as string on the created object, which maintains backwards compatibility.
   *
   * @param namespace the name of the object on the window
   * @param objects the objects to be put on the namespace
   * @throws Error if the object is already registered on the namespace to avoid accidental replacements.
   *               Such replacements would not work if the object is created using a class reference because in that case the namespace is not used.
   *               If you want to force a replacement, you can allow it by using the option allowedReplacements.
   */
  registerNamespace(namespace: string, objects: object, options?: RegisterNamespaceOptions) {
    options = $.extend({allowedReplacements: []}, options);

    // Ensure namespace object exists on window
    window[namespace] = window[namespace] || {};

    let prefix = namespace === 'scout' ? '' : namespace + '.';
    for (let [name, object] of Object.entries(objects)) {
      if (name === 'default') {
        // Do not register module itself, only imported files
        continue;
      }
      if (!object) {
        // ignore elements which have no value (e.g. exported variables which are null)
        continue;
      }
      if (window[namespace][name] && !options.allowedReplacements.includes(name)) {
        throw new Error(`${name} is already registered on namespace ${namespace || 'scout'}. Use objectFactories if you want to replace the existing obj.`);
      }

      // Register the new objects on the namespace
      window[namespace][name] = object;

      if (!object.prototype || name[0].toUpperCase() !== name[0]) {
        // Ignore elements that are not Classes, because they can't be created with scout.create anyway and therefore just waste space in the map.
        // Since there is no official way to detect a class, we make use of our naming convention that says classes have to start with an uppercase letter.
        continue;
      }

      // Register the new objects for the object type lookup
      this._objectTypeMap.set(object, prefix + name);
    }
  }

  static get(): ObjectFactory {
    return objectFactory;
  }

  protected static _set(newFactory: ObjectFactory) {
    objectFactory = newFactory;
  }
}

let objectFactory = new ObjectFactory();
