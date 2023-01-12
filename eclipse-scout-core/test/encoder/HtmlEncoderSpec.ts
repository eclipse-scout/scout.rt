/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {HtmlEncoder} from '../../src/encoder/HtmlEncoder';

describe('HtmlEncoder', () => {

  let encoder = new HtmlEncoder();

  it('encodes HTML', () => {
    expect(encoder.encode()).toBeUndefined();
    expect(encoder.encode('')).toBe('');
    expect(encoder.encode('hello')).toBe('hello');
    expect(encoder.encode('<b>hello</b>')).toBe('&lt;b&gt;hello&lt;/b&gt;');
    expect(encoder.encode('123')).toBe('123');
  });

});
