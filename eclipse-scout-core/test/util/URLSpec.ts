/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {URL} from '../../src/index';

describe('URL', () => {

  it('can parse super-simple URL', () => {
    let u = new URL(encodeURI('http://www.simple.example/dir/file.html'));
    expect(u).toBeDefined();
    expect(u.baseUrlRaw).toBe('http://www.simple.example/dir/file.html');
    expect(u.queryPartRaw).toBeUndefined();
    expect(u.hashPartRaw).toBeUndefined();
    expect(u.getParameter('sort')).toBeUndefined();
  });

  it('can parse empty hash', () => {
    let u = new URL(encodeURI('http://www.simple.example/dir/file.html#'));
    expect(u.hashPartRaw).toBe('');
  });

  it('can parse a moderately simple URL', () => {
    let u = new URL(encodeURI('http://www.simple.example/dir/file.html?query=search&action=&sort#bottom'));
    expect(u).toBeDefined();
    expect(u.baseUrlRaw).toBe('http://www.simple.example/dir/file.html');
    expect(u.queryPartRaw).toBe('query=search&action=&sort');
    expect(u.hashPartRaw).toBe('bottom');
    expect(u.getParameter('query')).toBe('search');
    expect(u.getParameter('doesNotExist')).toBeUndefined();
    expect(u.getParameter('action')).toBe('');
    expect(u.getParameter('sort')).toBe(null);
    expect(u.getParameter('bottom')).toBeUndefined();
  });

  it('can convert the URL to string (only changed in order of arguments)', () => {
    let u = new URL(encodeURI('http://www.simple.example/dir/file.html?query=search&action=&sort#bottom'));
    expect(u).toBeDefined();
    expect(u.toString()).toBe('http://www.simple.example/dir/file.html?action=&query=search&sort#bottom');
  });

  it('can handle multi-valued parameters', () => {
    let u = new URL(encodeURI('http://www.simple.example/dir/file.html?query=search&action=&query&sort&query=go#bottom'));
    expect(u).toBeDefined();
    expect(u.queryPartRaw).toBe('query=search&action=&query&sort&query=go');
    expect(u.getParameter('query')).toEqual([null, 'go', 'search']);
    expect(u.toString()).toBe('http://www.simple.example/dir/file.html?action=&query&query=go&query=search&sort#bottom');
  });

  it('can add parameters', () => {
    let u = new URL(encodeURI('http://www.simple.example/dir/file.html?user=hugo&submit=yes&debug#66627'));
    u.addParameter('check', 'no');
    try {
      // @ts-expect-error
      u.addParameter();
      // noinspection ExceptionCaughtLocallyJS
      throw new Error('addParameter() should throw an exception');
    } catch (e) {
      // should throw exception
    }
    u.addParameter('', ''); // should do nothing
    u.addParameter('print');
    u.addParameter('slow', null);
    u.addParameter('x', '');
    u.addParameter('user', 'admin');
    expect(u.getParameter('check')).toBe('no');
    expect(u.getParameter('user')).toEqual(['admin', 'hugo']);
    expect(u.toString()).toBe('http://www.simple.example/dir/file.html?check=no&debug&print&slow&submit=yes&user=admin&user=hugo&x=#66627');
  });

  it('can remove parameters', () => {
    let u = new URL(encodeURI('http://www.simple.example/dir/file.html?user=hugo&submit=yes&debug&user=peter#66627'));
    u.removeParameter('user');
    u.removeParameter(''); // should do nothing
    u.removeParameter('submit');
    u.removeParameter('debug');
    expect(u.getParameter('user')).toBeUndefined();
    expect(u.toString()).toBe('http://www.simple.example/dir/file.html#66627');
  });

  it('can create or replace parameters', () => {
    let u = new URL(encodeURI('http://www.simple.example/dir/file.html?user=hugo&submit=yes&debug&user=peter#66627'));
    u.addParameter('user', 'test');
    u.setParameter('user', 'hans');
    u.setParameter(''); // should do nothing
    u.setParameter('submit', '');
    u.setParameter('check', null);
    expect(u.getParameter('user')).toBe('hans');
    expect(u.getParameter('submit')).toBe('');
    expect(u.getParameter('debug')).toBe(null);
    expect(u.getParameter('check')).toBe(null);
    expect(u.toString()).toBe('http://www.simple.example/dir/file.html?check&debug&submit=&user=hans#66627');
  });

  it('can handle non-ascii characters', () => {
    let u = new URL(encodeURI('http://www.menükarte.example/dir/à la carte.php?query=search&wine=château lafite#\u1F603'));
    expect(u).toBeDefined();
    expect(decodeURIComponent(u.baseUrlRaw)).toBe('http://www.menükarte.example/dir/à la carte.php');
    expect(decodeURIComponent(u.queryPartRaw)).toBe('query=search&wine=château lafite');
    expect(u.hashPartRaw).toBe('%E1%BD%A03');
    expect(u.getParameter('query')).toBe('search');
    expect(u.getParameter('doesNotExist')).toBeUndefined();
    expect(u.getParameter('wine')).toBe('château lafite');
  });

  it('can sort parameters', () => {
    let u = new URL(encodeURI('http://www.simple.example/dir/file.html?user=hugo&submit=yes&debug#66627'));
    u.addParameter('check', 'no');
    u.addParameter('print');
    u.addParameter('slow', null);
    u.addParameter('x', '');
    u.addParameter('user', 'admin');
    expect(u.toString()).toBe('http://www.simple.example/dir/file.html?check=no&debug&print&slow&submit=yes&user=admin&user=hugo&x=#66627');
    // @ts-expect-error
    let reverseAlphabetSorter = (a, b) => -1 * URL._sorter(a, b);
    expect(u.toString({
      sorter: reverseAlphabetSorter
    })).toBe('http://www.simple.example/dir/file.html?x=&user=admin&user=hugo&submit=yes&slow&print&debug&check=no#66627');
    expect(u.toString({
      alwaysFirst: 'x'
    })).toBe('http://www.simple.example/dir/file.html?x=&check=no&debug&print&slow&submit=yes&user=admin&user=hugo#66627');
    expect(u.toString({
      alwaysFirst: ['slow', 'debug', 'x']
    })).toBe('http://www.simple.example/dir/file.html?slow&debug&x=&check=no&print&submit=yes&user=admin&user=hugo#66627');
    expect(u.toString({
      alwaysLast: ['print', 'check']
    })).toBe('http://www.simple.example/dir/file.html?debug&slow&submit=yes&user=admin&user=hugo&x=&print&check=no#66627');
    expect(u.toString({
      alwaysLast: ['check', 'print'],
      sorter: reverseAlphabetSorter
    })).toBe('http://www.simple.example/dir/file.html?x=&user=admin&user=hugo&submit=yes&slow&debug&check=no&print#66627');
  });

});
