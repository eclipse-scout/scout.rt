/*
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {ActionKeyStroke} from '../index';

export default class MenuKeyStroke extends ActionKeyStroke {

  constructor(action) {
    super(action);
  }

  _isEnabled() {
    if (this.field.excludedByFilter) {
      return false;
    }
    return super._isEnabled();
  }
}
