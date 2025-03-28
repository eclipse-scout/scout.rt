/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractConstructor, Constructor, FullModelOf, InitModelOf, ModelOf, ObjectModel, ObjectModelWithId, objects, ObjectUuidProvider, scout, TypeDescriptor, TypeDescriptorOptions} from './index';
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
   *
   * @deprecated will be removed in a future release, use {@link objectFactoryHints} instead
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
  protected _objectTypeMap: Map<Constructor, string>;

  constructor() {
    this.initialized = false;
    this._registry = new Map();
    this._objectTypeMap = new Map();
  }

  static NAMESPACE_SEPARATOR = '.';
  static MODEL_VARIANT_SEPARATOR = ':';
  static HINTS_META_DATA_KEY = Symbol('scout.objectFactoryHints');

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
   * Creates and initializes a new Scout object.
   *
   * When the created object has an init function, it will be called.
   * The model object is passed to the constructor and to the init function, if it is available.
   *
   * The class of the object to be created can provide {@link objectFactoryHints} that influence the object creation.
   * If not defined otherwise by the hints, the objectType is written to the created object.
   *
   * @param objectTypeOrModel This argument can be
   *        <ul>
   *          <li>objectType: a class reference to the object to be created or the name of the object as registered with {@link registerNamespace}.</li>
   *          <li>model: an object containing the objectType as property among with other properties that should be passed to the object to be created.</li>
   *        </ul>
   * @param modelOrOptions This argument can be
   *        <ul>
   *          <li>model: an object containing properties that should be passed to the object to be created. The property objectType will be ignored because it is provided by the first argument.</li>
   *          <li>options: see options argument
   *        </ul>
   * @param options optional options to influence the creation of the object
   * @throws Error if the argument list does not match the definition.
   */
  create<T extends object>(objectTypeOrModel: ObjectType<T> | FullModelOf<T>, modelOrOptions?: InitModelOf<T>, options?: ObjectFactoryOptions): T {
    // Normalize arguments
    let objectType: ObjectType<T>;
    let model: ObjectModel<T> & ObjectModelWithId;
    if (typeof objectTypeOrModel === 'string' || typeof objectTypeOrModel === 'function') {
      options = options || {};
      model = modelOrOptions;
      objectType = objectTypeOrModel;
    } else if (objects.isObject(objectTypeOrModel)) {
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
    const scoutObject = this._createObjectByType(objectType, options);
    const ensureId = this._ensureUniqueId(scoutObject, options);
    const ensureObjectType = this._ensureObjectType(scoutObject);
    if (objects.isFunction(scoutObject.init)) {
      if (model) {
        if (model.id === undefined && ensureId) {
          model.id = ObjectUuidProvider.createUiId();
        }
        if (ensureObjectType) {
          model.objectType = this.getObjectType(objectType);
        }
      }
      scoutObject.init(model);
    }

    if (scoutObject.id === undefined && ensureId) {
      scoutObject.id = ObjectUuidProvider.createUiId();
    }
    if (scoutObject.objectType === undefined && ensureObjectType) {
      scoutObject.objectType = this.getObjectType(objectType);
    }

    return scoutObject;
  }

  protected _ensureObjectType(scoutObject: any): boolean {
    let hints = Reflect.getMetadata(ObjectFactory.HINTS_META_DATA_KEY, scoutObject) as ObjectFactoryHints;
    return scout.nvl(hints?.ensureObjectType, true);
  }

  protected _ensureUniqueId(scoutObject: any, options?: ObjectFactoryOptions): boolean {
    let hints = Reflect.getMetadata(ObjectFactory.HINTS_META_DATA_KEY, scoutObject) as ObjectFactoryHints;
    return scout.nvl(options?.ensureUniqueId, scout.nvl(hints?.ensureId, false));
  }

  /**
   * @deprecated Use {@link ObjectUuidProvider.createUiId} instead.
   */
  createUniqueId(): string {
    return ObjectUuidProvider.createUiId();
  }

  resolveTypedObjectType<T>(objectType: ObjectType<T>): ObjectType<T> {
    if (typeof objectType !== 'string') {
      return objectType;
    }
    let Class = TypeDescriptor.resolveType(objectType) as Constructor<T>;
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
  getObjectType(objectType: ObjectType): string {
    if (typeof objectType === 'string') {
      return objectType;
    }
    return this._objectTypeMap.get(objectType);
  }

  /**
   * @param baseClass The base class (exclusive) for which all known subclasses should be returned.
   * @returns All classes that have the given class in their super hierarchy. The given baseClass is not part of the result.
   * More formally: all constructors known to this factory that have the given class in their prototype chain.
   */
  getSubClassesOf<T extends object>(baseClass: Constructor<T> | AbstractConstructor<T>): Constructor<T>[] {
    const result: Constructor<T>[] = [];
    if (!baseClass) {
      return result;
    }
    for (let obj of this._objectTypeMap.keys()) {
      if (baseClass.isPrototypeOf(obj)) {
        result.push(obj as Constructor<T>);
      }
    }
    return result;
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
      let existing = window[namespace][name];
      if (existing && existing !== object && !options.allowedReplacements.includes(name)) {
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

  /**
   * Removes the given object types from the namespace and the object type map.
   * If the namespace is empty after the removal, it will be deleted.
   *
   * @param objectTypes the object types to be removed from the namespace
   */
  removeFromNamespace(objectTypes: Constructor[]) {
    for (const objectType of objectTypes) {
      let name = this.getObjectType(objectType);
      if (name) {
        let [namespace, objectName] = name.split('.');
        if (window[namespace]) {
          delete window[namespace][objectName];

          if (Object.keys(window[namespace]).length === 0) {
            delete window[namespace];
          }
        }
      }
      this._objectTypeMap.delete(objectType);
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

export type ObjectFactoryHints = {
  /**
   * Specifies whether the {@link ObjectFactory} needs to assign a unique id to the object if the object does not already have one.
   *
   * Default is false.
   */
  ensureId?: boolean;
  /**
   * Specifies whether the {@link ObjectFactory} needs to resolve the string based objectType using {@link ObjectType.getObjectType} and assign it to the object.
   *
   * Default is true.
   */
  ensureObjectType?: boolean;
};

/**
 * A class decorator to provide hints for the {@link ObjectFactory} that control the object creation.
 *
 * It is possible to override existing hints by extending from the class having hints and adding the decorator with the customized hints to the subclass.
 */
export function objectFactoryHints(hints: ObjectFactoryHints) {
  return <T extends Constructor | AbstractConstructor>(BaseClass: T) => class extends BaseClass {
    static {
      Reflect.defineMetadata(ObjectFactory.HINTS_META_DATA_KEY, hints, BaseClass.prototype);
    }
  };
}
