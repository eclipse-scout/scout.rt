/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {HybridActionContextElementConverter, ObjectRegistries, ObjectRegistry} from '../../../index';

/**
 * Central registry for all available {@link HybridActionContextElementConverter} instances.
 */
export class HybridActionContextElementConverters extends ObjectRegistry<HybridActionContextElementConverter> {

  static get(): HybridActionContextElementConverters {
    return ObjectRegistries.get(HybridActionContextElementConverters);
  }

  static all(): HybridActionContextElementConverter[] {
    return HybridActionContextElementConverters.get().all();
  }
}
