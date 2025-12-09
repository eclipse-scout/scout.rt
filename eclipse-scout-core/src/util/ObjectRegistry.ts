/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Constructor, ObjectType, scout} from '../index';

/**
 * Represents an ordered collection of instances of the specified object type. These are created automatically
 * on first access and are stored in the registry indefinitely or until explicitly unregistered.
 *
 * The object registry can be used as an extension point for other application modules. It allows callers
 * to apply the _inversion of control_ principle, similar to `BEANS.all()` in Java.
 *
 * @template TObject The type of objects managed by this registry.
 */
export class ObjectRegistry<TObject extends object> {

  static readonly DEFAULT_ORDER = 5000.0;

  protected readonly _registrations: ObjectRegistration<TObject>[] = [];

  /**
   * Registers the given object type with this registry. After registration, the list returned by {@link all}
   * will include an instance of this type in the order it was registered. The object instance ise created
   * using {@link scout.create}. The default ordering can be altered by specifying an explicit order. The
   * default order is {@link DEFAULT_ORDER}.
   *
   * @returns An object that can be used to {@link unregister} the object again.
   */
  register(objectType: Constructor<TObject>, options?: number | ObjectRegistryRegisterOptions): ObjectRegistration<TObject> {
    scout.assertParameter('objectType', objectType);
    let registration = this._createRegistration(objectType, this._prepareOptions(options));
    arrays.insertSorted(this._registrations, registration, (r1, r2) => r1.order - r2.order); // sorted from smallest to largest
    return registration;
  }

  protected _prepareOptions(options?: number | ObjectRegistryRegisterOptions): ObjectRegistryRegisterOptions {
    return typeof options === 'number' ? {order: options} : options;
  }

  protected _createRegistration<T>(objectType: Constructor<TObject>, options: ObjectRegistryRegisterOptions): ObjectRegistration<TObject> {
    return {
      objectType,
      instance: undefined, // created on first use
      order: scout.nvl(options?.order, ObjectRegistry.DEFAULT_ORDER)
    };
  }

  /**
   * Unregisters a previously registered object.
   *
   * @param registration The registration handle that was returned by {@link register}.
   * @return true if the registration was removed, false otherwise.
   */
  unregister(registration: ObjectRegistration<TObject>): boolean {
    return arrays.remove(this._registrations, registration);
  }

  /**
   * @returns All registered objects in the order specified during registration.
   */
  all(): TObject[] {
    return arrays.ensure(this._registrations).map(registration => {
      if (!registration.instance) {
        registration.instance = scout.create(registration.objectType);
      }
      return registration.instance;
    });
  }
}

export interface ObjectRegistryRegisterOptions {
  /**
   * Default is {@link ObjectRegistry.DEFAULT_ORDER}.
   */
  order?: number;
}

/**
 * Registration handle of an object in an {@link ObjectFactory}. Can be used to unregister the object again.
 */
export interface ObjectRegistration<TObject extends object> {
  objectType: ObjectType<TObject>;
  instance: TObject;
  order: number;
}

export class ObjectRegistries {

  protected static readonly _INSTANCES: Map<Constructor<any>, ObjectRegistry<any>> = new Map();

  /**
   * @returns a singleton instance of the given {@link ObjectFactory} class.
   */
  static get<TRegistry extends ObjectRegistry<any>>(registryType: Constructor<TRegistry>): TRegistry {
    if (!ObjectRegistries._INSTANCES.has(registryType)) {
      ObjectRegistries._INSTANCES.set(registryType, scout.create(registryType));
    }
    return ObjectRegistries._INSTANCES.get(registryType) as TRegistry;
  }
}
