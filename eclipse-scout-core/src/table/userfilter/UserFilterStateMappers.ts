/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Constructor, scout, UserFilterStateMapper} from '../../index';

export class UserFilterStateMappers {

  static DEFAULT_ORDER = 5000.0;
  protected static _INSTANCE: UserFilterStateMappers = null;

  protected _registry: UserFilterStateMapperRegistration[] = [];

  register(mapper: Constructor<UserFilterStateMapper>, order = UserFilterStateMappers.DEFAULT_ORDER): UserFilterStateMapperRegistration {
    let registration: UserFilterStateMapperRegistration = {
      order: order,
      mapper: mapper instanceof UserFilterStateMapper ? mapper : null,
      mapperConstructor: mapper instanceof UserFilterStateMapper ? null : mapper
    };
    arrays.insertSorted(this._registry, registration, (r1, r2) => r1.order - r2.order);
    return registration;
  }

  unregister(registration: UserFilterStateMapperRegistration): boolean {
    return arrays.remove(this._registry, registration);
  }

  /**
   * @return all registered mappers in the order specified during registration
   */
  all(): UserFilterStateMapper[] {
    return this._registry.map(registration => {
      registration.mapper = registration.mapper || scout.create(registration.mapperConstructor);
      return registration.mapper;
    });
  }

  static get(): UserFilterStateMappers {
    if (!UserFilterStateMappers._INSTANCE) {
      UserFilterStateMappers._INSTANCE = scout.create(UserFilterStateMappers);
    }
    return UserFilterStateMappers._INSTANCE;
  }

  static all(): UserFilterStateMapper[] {
    return UserFilterStateMappers.get().all();
  }
}

export interface UserFilterStateMapperRegistration {
  mapperConstructor: Constructor<UserFilterStateMapper>;
  mapper: UserFilterStateMapper;
  order: number;
}
