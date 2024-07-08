/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {arrays, Constructor, HybridActionContextElement, JsonHybridActionContextElement, ModelAdapter, scout} from '../../index';

export abstract class HybridActionContextElementConverter {

  static DEFAULT_ORDER = 5000.0;
  protected static _registry: HybridActionContextElementConverterRegistration[] = [];

  static register(converter: HybridActionContextElementConverter | Constructor<HybridActionContextElementConverter>, order = HybridActionContextElementConverter.DEFAULT_ORDER) {
    let registration: HybridActionContextElementConverterRegistration = {
      order: order,
      converter: converter instanceof HybridActionContextElementConverter ? converter : null,
      converterConstructor: converter instanceof HybridActionContextElementConverter ? null : converter
    };
    arrays.insertSorted(HybridActionContextElementConverter._registry, registration, (r1, r2) => r1.order - r2.order);
  }

  static all(): HybridActionContextElementConverter[] {
    return HybridActionContextElementConverter._registry.map(registration => {
      registration.converter = registration.converter || scout.create(registration.converterConstructor);
      return registration.converter;
    });
  }

  static convertFromJson(adapter: ModelAdapter, jsonElement: any): HybridActionContextElement {
    let converters = HybridActionContextElementConverter.all();
    for (let i = 0; i < converters.length; i++) {
      let element = converters[i].jsonToElement(adapter, jsonElement);
      if (element) {
        return element;
      }
    }
    return null;
  }

  static convertToJson(adapter: ModelAdapter, element: any): JsonHybridActionContextElement {
    let converters = HybridActionContextElementConverter.all();
    for (let i = 0; i < converters.length; i++) {
      let jsonElement = converters[i].elementToJson(adapter, element);
      if (jsonElement) {
        return jsonElement;
      }
    }
    return null;
  }

  abstract jsonToElement(adapter: ModelAdapter, jsonElement: any): any;

  abstract elementToJson(adapter: ModelAdapter, element: any): any;
}

export interface HybridActionContextElementConverterRegistration {
  converterConstructor: Constructor<HybridActionContextElementConverter>;
  converter: HybridActionContextElementConverter;
  order: number;
}
