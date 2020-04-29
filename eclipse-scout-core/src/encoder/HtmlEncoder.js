/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */

import {CachedElement} from '../index';

export default class HtmlEncoder {
  constructor() {
    this.cache = new CachedElement('div');
  }

  encode(text) {
    if (!text) {
      return text;
    }
    let div = this.cache.get();
    div.textContent = text;
    return div.innerHTML;
  }
}
