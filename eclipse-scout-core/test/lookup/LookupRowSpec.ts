/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {LookupRow, scout} from '../../src/index';

describe('LookupRow', () => {

  /** ensureUniqueId=false is important: we don't want a random ID property
   * on the created instance otherwise the equals test would fail.
   */
  function createLookupRow() {
    return scout.create(LookupRow, {
      key: 123,
      text: 'Foo',
      enabled: false,
      active: false
    }, {ensureUniqueId: false});
  }

  it('constructor', () => {
    let lookupRow = scout.create(LookupRow);
    expect(lookupRow.key).toBe(null);
    expect(lookupRow.text).toBe(null);
    expect(lookupRow.enabled).toBe(true);
    expect(lookupRow.active).toBe(true);
  });

  it('sets defaults when created with a plain object', () => {
    let lookupRow = scout.create(LookupRow, {
      key: 123,
      text: 'Foo'
    });

    expect(lookupRow.key).toBe(123);
    expect(lookupRow.text).toBe('Foo');
    expect(lookupRow.enabled).toBe(true);
    expect(lookupRow.active).toBe(true);
  });

  it('uses values from plain object instead of defaults, when set', () => {
    let lookupRow = createLookupRow();

    expect(lookupRow.key).toBe(123);
    expect(lookupRow.text).toBe('Foo');
    expect(lookupRow.enabled).toBe(false);
    expect(lookupRow.active).toBe(false);
  });

  it('two different instances with same properties must be equals', () => {
    let lookupRowA = createLookupRow();
    let lookupRowB = createLookupRow();
    expect(lookupRowA.equals(lookupRowB)).toBe(true);
    expect(lookupRowB.equals(lookupRowA)).toBe(true);
    lookupRowB.enabled = true;
    expect(lookupRowA.equals(lookupRowB)).toBe(false);
  });

});
