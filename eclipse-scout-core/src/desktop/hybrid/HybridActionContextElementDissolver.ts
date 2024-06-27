/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HybridActionContextElement} from './HybridActionContextElement';
import {arrays} from '../../util/arrays';
import {Constructor, scout} from '../../scout';

export abstract class HybridActionContextElementDissolver {

  static DEFAULT_ORDER = 5000.0;
  protected static REGISTRY: HybridActionContextElementDissolverRegistration[] = [];

  static register(dissolver: HybridActionContextElementDissolver | Constructor<HybridActionContextElementDissolver>, order = HybridActionContextElementDissolver.DEFAULT_ORDER) {
    let registration: HybridActionContextElementDissolverRegistration = {
      order: order,
      dissolver: dissolver instanceof HybridActionContextElementDissolver ? dissolver : null,
      dissolverConstructor: dissolver instanceof HybridActionContextElementDissolver ? null : dissolver
    };
    arrays.insertSorted(HybridActionContextElementDissolver.REGISTRY, registration, (r1, r2) => r1.order - r2.order);
  }

  static all(): HybridActionContextElementDissolver[] {
    return HybridActionContextElementDissolver.REGISTRY.map(registration => {
      if (!registration.dissolver) {
        registration.dissolver = scout.create(registration.dissolverConstructor);
      }
      return registration.dissolver;
    });
  }

  static dissolve(contextElement: HybridActionContextElement): object {
    let dissolvers = HybridActionContextElementDissolver.all();
    for (let i = 0; i < dissolvers.length; i++) {
      let dissolved = dissolvers[i].dissolve(contextElement);
      if (dissolved) {
        return dissolved;
      }
    }
    return null;
  }

  abstract dissolve(contextElement: HybridActionContextElement): object;
}

export interface HybridActionContextElementDissolverRegistration {
  dissolver: HybridActionContextElementDissolver;
  dissolverConstructor: Constructor<HybridActionContextElementDissolver>;
  order: number;
}
