/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
export interface EventDelegatorOptions {
  /** True, to call the setter on the target on a property change event. */
  callSetter?: boolean;
  /** An array of all properties to be delegated from the source to the target when changed on the source. Default is []; */
  delegateProperties?: string[];
  /** An array of all properties to be excluded from delegating to the target in all cases. Default is []. */
  excludeProperties?: string[];
  /** An array of all events to be delegated from the source to the target when triggered on the source. Default is []. */
  delegateEvents?: string[];
  /** True, to delegate all property changes from the source to the target. Default is false. */
  delegateAllProperties?: boolean;
  /** True, to delegate all events from the source to the target. Default is false. */
  delegateAllEvents?: boolean;
}
