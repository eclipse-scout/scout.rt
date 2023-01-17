/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {scout} from '../../src/index';
import {ActiveDummyLookupCall} from '../../src/testing';

describe('StaticLookupCall', () => {

  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
    jasmine.Ajax.uninstall();
  });

  it('filter: active only (default)', done => {
    let lookupCall = scout.create(ActiveDummyLookupCall, {
      session: session
    });

    expect(lookupCall.active).toBe(true);
    lookupCall.getAll().then(result => {
      expect(result.lookupRows.length).toBe(2);
      expect(result.lookupRows[0].text).toBe('Foo');
      expect(result.lookupRows[0].active).toBe(true);
      expect(result.lookupRows[1].text).toBe('Baz');
      expect(result.lookupRows[1].active).toBe(null); // active null is treated as true
      done();
    });
    jasmine.clock().tick(500);
  });

  it('filter: inactive only', done => {
    let lookupCall = scout.create(ActiveDummyLookupCall, {
      session: session,
      active: false
    });
    lookupCall.getAll().then(result => {
      expect(result.lookupRows.length).toBe(1);
      expect(result.lookupRows[0].text).toBe('Bar');
      expect(result.lookupRows[0].active).toBe(false);
      done();
    });
    jasmine.clock().tick(500);
  });

  it('filter: all', done => {
    let lookupCall = scout.create(ActiveDummyLookupCall, {
      session: session,
      active: null // = all
    });
    lookupCall.getAll().then(result => {
      expect(result.lookupRows.length).toBe(3);
      done();
    });
    jasmine.clock().tick(500);
  });

  it('filter: text', () => {
    let lookupCall = scout.create('DummyLookupCall', {
      session: session
    });

    const expectLookupRows = result => expect(result.lookupRows.map(row => row.text));

    lookupCall.getByText().then(result => expectLookupRows(result).toEqual(['Foo', 'Bar', 'Baz']));
    jasmine.clock().tick(500);

    lookupCall.getByText('').then(result => expectLookupRows(result).toEqual(['Foo', 'Bar', 'Baz']));
    jasmine.clock().tick(500);

    lookupCall.getByText('x').then(result => expectLookupRows(result).toEqual([]));
    jasmine.clock().tick(500);

    lookupCall.getByText('f').then(result => expectLookupRows(result).toEqual(['Foo']));
    jasmine.clock().tick(500);

    lookupCall.getByText('a').then(result => expectLookupRows(result).toEqual([]));
    jasmine.clock().tick(500);

    lookupCall.getByText('*a').then(result => expectLookupRows(result).toEqual(['Bar', 'Baz']));
    jasmine.clock().tick(500);

    lookupCall.getByText('*Ar').then(result => expectLookupRows(result).toEqual(['Bar']));
    jasmine.clock().tick(500);

    lookupCall.getByText('*a*z*').then(result => expectLookupRows(result).toEqual(['Baz']));
    jasmine.clock().tick(500);

    lookupCall.getByText('f*a').then(result => expectLookupRows(result).toEqual([]));
    jasmine.clock().tick(500);

    lookupCall.getByText('******************').then(result => expectLookupRows(result).toEqual(['Foo', 'Bar', 'Baz']));
    jasmine.clock().tick(500);

    lookupCall.getByText('.*').then(result => expectLookupRows(result).toEqual([]));
    jasmine.clock().tick(500);

    lookupCall.getByText('[').then(result => expectLookupRows(result).toEqual([]));
    jasmine.clock().tick(500);
  });

});
