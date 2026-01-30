/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {dates, LookupRow, scout} from '../../src/index';
import {Fixture06Do} from '../dataobject/DataObjectSerializerSpec';

describe('LookupRow', () => {

  function createLookupRow() {
    return scout.create(LookupRow, {
      key: 123,
      text: 'Foo',
      enabled: false,
      active: false
    });
  }

  it('constructor', () => {
    let lookupRow = scout.create(LookupRow);
    expect(lookupRow.key).toBe(null);
    expect(lookupRow.text).toBe(null);
    expect(lookupRow.enabled).toBe(true);
    expect(lookupRow.active).toBe(true);
  });

  it('can be deep cloned', () => {
    const origDo = new Fixture06Do();
    const origDate = dates.parseJsonDate('2026-02-05 11:51:39.708');
    origDo.propDate = origDate;
    const orig = scout.create(SpecLookupRow, {
      key: 'test',
      text: 'Foo',
      additionalTableRowData: {
        b: true,
        nested: {
          s: 'orig',
          d: origDo
        }
      }
    });
    let copy = orig.clone();
    expect(copy).not.toBe(orig);
    expect(copy).toBeInstanceOf(SpecLookupRow);
    expect(copy.key).toEqual('test');
    expect(copy.text).toEqual('Foo');
    expect(copy.additionalTableRowData.nested.s).toEqual('orig');

    // change copy and expect orig to remain unchanged
    copy.additionalTableRowData.nested.s = 'copy';
    expect(orig.additionalTableRowData.nested.s).toEqual('orig');
    expect(copy.additionalTableRowData.nested.s).toEqual('copy');

    // compare DataObject
    expect(copy.additionalTableRowData.nested.d).toBeInstanceOf(Fixture06Do);
    expect(copy.additionalTableRowData.nested.d.propDate).toBeInstanceOf(Date);
    expect(copy.additionalTableRowData.nested.d).not.toBe(orig.additionalTableRowData.nested.d);
    expect(copy.additionalTableRowData.nested.d.propDate).not.toBe(orig.additionalTableRowData.nested.d.propDate);
    expect(copy.additionalTableRowData.nested.d.propDate).toEqual(origDate);
  });

  class SpecLookupRow extends LookupRow<string> {
  }

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
