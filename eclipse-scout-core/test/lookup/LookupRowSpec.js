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
import {scout} from '../../src/index';


describe('LookupRow', function() {

  it('constructor', function() {
    var lookupRow = scout.create('LookupRow');
    expect(lookupRow.key).toBe(null);
    expect(lookupRow.text).toBe(null);
    expect(lookupRow.enabled).toBe(true);
    expect(lookupRow.active).toBe(true);
  });

  it('sets defaults when created with a plain object', function() {
    var lookupRow = scout.create('LookupRow', {
      key: 123,
      text: 'Foo'
    });

    expect(lookupRow.key).toBe(123);
    expect(lookupRow.text).toBe('Foo');
    expect(lookupRow.enabled).toBe(true);
    expect(lookupRow.active).toBe(true);
  });

  it('uses values from plain object instead of defaults, when set', function() {
    var lookupRow = scout.create('LookupRow', {
      key: 123,
      text: 'Foo',
      enabled: false,
      active: false
    });

    expect(lookupRow.key).toBe(123);
    expect(lookupRow.text).toBe('Foo');
    expect(lookupRow.enabled).toBe(false);
    expect(lookupRow.active).toBe(false);
  });

});
