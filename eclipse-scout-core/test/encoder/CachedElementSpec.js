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

import CachedElement from '../../src/encoder/CachedElement';

describe('CachedElement', () => {

  it('should create element only once', () => {
    let cache = new CachedElement('div');
    expect(cache.cachedElement).toBe(null);

    // subsequent calls must always return the same element
    let elem1 = cache.get();
    expect(elem1).not.toBe(null);

    let elem2 = cache.get();
    expect(elem2).toBe(elem1);
  });

});
