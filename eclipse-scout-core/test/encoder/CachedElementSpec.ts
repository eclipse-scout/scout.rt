/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {CachedElement} from '../../src/encoder/CachedElement';

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
