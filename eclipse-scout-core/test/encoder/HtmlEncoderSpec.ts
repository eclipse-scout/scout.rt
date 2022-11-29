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

import HtmlEncoder from '../../src/encoder/HtmlEncoder';

describe('HtmlEncoder', () => {

  let encoder = new HtmlEncoder();

  it('encodes HTML', () => {
    expect(encoder.encode()).toBeUndefined();
    expect(encoder.encode('')).toBe('');
    expect(encoder.encode('hello')).toBe('hello');
    expect(encoder.encode('<b>hello</b>')).toBe('&lt;b&gt;hello&lt;/b&gt;');
    expect(encoder.encode(123)).toBe('123');
  });

});
