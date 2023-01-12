/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LookupCallModel} from '../index';

export interface RestLookupCallModel<TKey> extends LookupCallModel<TKey> {
  resourceUrl?: string;
  maxTextLength?: number;
  /**
   * for predefined restrictions only (e.g. in JSON or subclasses), don't change this attribute! this instance is shared with all clones!
   */
  restriction?: Record<string, any>;
}
