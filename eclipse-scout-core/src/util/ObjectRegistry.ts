/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractConstructor, arrays, Constructor, scout} from '../index';

export class ObjectRegistry {

  static readonly DEFAULT_ORDER = 5000.0;

  // Note: We cannot use objects.createSingletonProxy(), because that requires the ObjectFactory to be fully
  // initialized, which is only the case after the App has been started (but registrations happen earlier).
  protected static _INSTANCE: ObjectRegistry = null;

  static get(): ObjectRegistry {
    if (!ObjectRegistry._INSTANCE) {
      ObjectRegistry._INSTANCE = scout.create(ObjectRegistry);
    }
    return ObjectRegistry._INSTANCE;
  }

  // --------------------------------------------

  protected readonly _registry: Map<Constructor | AbstractConstructor, ObjectRegistration<any>[]> = new Map();

  register<TObject extends object>(
    objectType: Constructor<TObject> | AbstractConstructor<TObject>,
    instanceOrConstructor: TObject | Constructor<TObject>,
    options?: ObjectRegistryRegisterOptions
  ): ObjectRegistration<TObject> {
    let objectInstance = typeof instanceOrConstructor === 'function' ? null : instanceOrConstructor as TObject;
    let objectConstructor = typeof instanceOrConstructor === 'function' ? instanceOrConstructor as Constructor<TObject> : null;
    let registration: ObjectRegistration<TObject> = {
      order: scout.nvl(options?.order, ObjectRegistry.DEFAULT_ORDER),
      reuse: objectInstance ? false : scout.nvl(options?.reuse, true),
      objectInstance: objectInstance,
      objectConstructor: objectConstructor
    };
    let registrations = this._registry.get(objectType);
    if (!registrations) {
      registrations = [];
      this._registry.set(objectType, registrations);
    }
    arrays.insertSorted(registrations, registration, (r1, r2) => r1.order - r2.order);
    return registration;
  }

  unregister<TObject extends object>(
    objectType: Constructor<TObject> | AbstractConstructor<TObject>,
    registration: ObjectRegistration<TObject>
  ): boolean {
    let registrations = this._registry.get(objectType);
    return arrays.remove(registrations, registration);
  }

  /**
   * @returns all registered objects in the order specified during registration
   */
  all<TObject extends object>(
    objectType: Constructor<TObject> | AbstractConstructor<TObject>
  ): TObject[] {
    let registrations = this._registry.get(objectType);
    return arrays.ensure(registrations).map(registration => {
      if (registration.objectInstance) {
        return registration.objectInstance as TObject;
      }
      let instance = scout.create(registration.objectConstructor) as TObject;
      if (registration.reuse) {
        registration.objectInstance = instance;
      }
      return instance;
    });
  }
}

export interface ObjectRegistryRegisterOptions {
  /**
   * Default is {@link ObjectRegistry.DEFAULT_ORDER}.
   */
  order?: number;
  /**
   * Default is true.
   */
  reuse?: boolean;
}

export interface ObjectRegistration<TObject extends object> {
  objectConstructor: Constructor<TObject>;
  objectInstance: TObject;
  order: number;
  reuse: boolean;
}

export function registerAs(objectType: Constructor | AbstractConstructor, options?: ObjectRegistryRegisterOptions) {
  return <T extends Constructor | AbstractConstructor>(BaseClass: T) => {
    ObjectRegistry.get().register(objectType, BaseClass, options);
  };
}
