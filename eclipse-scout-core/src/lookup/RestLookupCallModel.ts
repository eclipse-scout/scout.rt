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
import {LookupCallModel} from '../index';

export default interface RestLookupCallModel<TKey> extends LookupCallModel<TKey> {
  resourceUrl?: string;
  maxTextLength?: number;
  /**
   * for predefined restrictions only (e.g. in JSON or subclasses), don't change this attribute! this instance is shared with all clones!
   */
  restriction?: Record<string, any>;
}
