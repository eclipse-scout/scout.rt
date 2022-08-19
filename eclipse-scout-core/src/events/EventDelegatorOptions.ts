/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
export default interface EventDelegatorOptions {
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
