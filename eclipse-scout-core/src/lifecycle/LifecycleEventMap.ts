/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Event, EventMap, Lifecycle} from '../index';

export interface LifecycleEventMap<TValidationResult> extends EventMap {
  'load': Event<Lifecycle<TValidationResult>>;
  'postLoad': Event<Lifecycle<TValidationResult>>;
  'save': Event<Lifecycle<TValidationResult>>;
  'close': Event<Lifecycle<TValidationResult>>;
  'reset': Event<Lifecycle<TValidationResult>>;
}
