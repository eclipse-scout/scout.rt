/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Constructor, HybridActionContextElementConverter, scout} from '../../../index';

/**
 * Central registry for all available {@link HybridActionContextElementConverter} instances.
 */
export class HybridActionContextElementConverters {

  static DEFAULT_ORDER = 5000.0;
  protected static _INSTANCE: HybridActionContextElementConverters = null;

  protected _registry: HybridActionContextElementConverterRegistration[] = [];

  register(converter: HybridActionContextElementConverter | Constructor<HybridActionContextElementConverter>, order = HybridActionContextElementConverters.DEFAULT_ORDER): HybridActionContextElementConverterRegistration {
    let registration: HybridActionContextElementConverterRegistration = {
      order: order,
      converter: converter instanceof HybridActionContextElementConverter ? converter : null,
      converterConstructor: converter instanceof HybridActionContextElementConverter ? null : converter
    };
    arrays.insertSorted(this._registry, registration, (r1, r2) => r1.order - r2.order);
    return registration;
  }

  unregister(registration: HybridActionContextElementConverterRegistration): boolean {
    return arrays.remove(this._registry, registration);
  }

  /**
   * @return all registered converters in the order specified during registration
   */
  all(): HybridActionContextElementConverter[] {
    return this._registry.map(registration => {
      registration.converter = registration.converter || scout.create(registration.converterConstructor);
      return registration.converter;
    });
  }

  static get(): HybridActionContextElementConverters {
    if (!HybridActionContextElementConverters._INSTANCE) {
      HybridActionContextElementConverters._INSTANCE = scout.create(HybridActionContextElementConverters);
    }
    return HybridActionContextElementConverters._INSTANCE;
  }

  static all(): HybridActionContextElementConverter[] {
    return HybridActionContextElementConverters.get().all();
  }
}

export interface HybridActionContextElementConverterRegistration {
  converterConstructor: Constructor<HybridActionContextElementConverter>;
  converter: HybridActionContextElementConverter;
  order: number;
}
