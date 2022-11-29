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
import {comparators} from '../../../src/index';

describe('comparators', () => {

  beforeEach(() => {
    // ensure before each test that it runs without a collator.
    // comparators.TEXT is a singleton and therefore other tests might have called install already (which creates a collator).
    comparators.TEXT.collator = null;
  });

  it('tests \'compare\' method of TEXT comparator', () => {
    let comparator = comparators.TEXT;

    expect(comparator.compare(null, null)).toBe(0);
    expect(comparator.compare(null, 'a')).toBe(-1);
    expect(comparator.compare('a', null)).toBe(1);
    expect(comparator.compare('a', 'a')).toBe(0);
    expect(comparator.compare('a', 'b')).toBe(-1);
    expect(comparator.compare('b', 'a')).toBe(1);
    expect(comparator.compare('a', 'B')).toBe(1);
    expect(comparator.compare('B', 'a')).toBe(-1);
  });

  it('tests \'compareIgnoreCase\' method of TEXT comparator', () => {
    let comparator = comparators.TEXT;

    expect(comparator.compareIgnoreCase(null, null)).toBe(0);
    expect(comparator.compareIgnoreCase(undefined, undefined)).toBe(0);
    expect(comparator.compareIgnoreCase(undefined, null)).toBe(0);
    expect(comparator.compareIgnoreCase(undefined, '')).toBe(0);
    expect(comparator.compareIgnoreCase('', '')).toBe(0);

    expect(comparator.compareIgnoreCase(null, 'a')).toBe(-1);
    expect(comparator.compareIgnoreCase('a', null)).toBe(1);

    expect(comparator.compareIgnoreCase(undefined, 'a')).toBe(-1);
    expect(comparator.compareIgnoreCase('a', undefined)).toBe(1);

    expect(comparator.compareIgnoreCase('', 'a')).toBe(-1);
    expect(comparator.compareIgnoreCase('a', '')).toBe(1);

    expect(comparator.compareIgnoreCase('A', 'a')).toBe(0);
    expect(comparator.compareIgnoreCase('a', 'a')).toBe(0);
  });

  it('tests \'compare\' method of NUMERIC comparator', () => {
    let comparator = comparators.NUMERIC;

    expect(comparator.compare(undefined, undefined)).toBe(0);
    expect(comparator.compare(undefined, '1')).toBe(-1);
    expect(comparator.compare('1', undefined)).toBe(1);

    expect(comparator.compare(null, null)).toBe(0);
    expect(comparator.compare(null, '1')).toBe(-1);
    expect(comparator.compare('1', null)).toBe(1);

    expect(comparator.compare(null, undefined)).toBe(0);

    expect(comparator.compare(null, null)).toBe(0);
    expect(comparator.compare(null, '-1')).toBe(-1);
    expect(comparator.compare('-1', null)).toBe(1);

    expect(comparator.compare('0', '-1')).toBe(1);
    expect(comparator.compare('-1', '0')).toBe(-1);

    expect(comparator.compare('0', '1')).toBe(-1);
    expect(comparator.compare('1', '0')).toBe(1);

    expect(comparator.compare('1', '1')).toBe(0);
    expect(comparator.compare('1', '2')).toBe(-1);
    expect(comparator.compare('2', '1')).toBe(1);

    expect(comparator.compare('1.0001', '1.0001')).toBe(0);
    expect(comparator.compare('1.9998', '1.9999')).toBe(-1);
    expect(comparator.compare('1.9999', '1.9998')).toBe(1);
  });

  it('tests \'compare\' method of ALPHANUMERIC comparator', () => {
    let comparator = comparators.ALPHANUMERIC;

    expect(comparator.compare(undefined, undefined)).toBe(0);
    expect(comparator.compare(null, null)).toBe(0);
    expect(comparator.compare('', '')).toBe(0);

    expect(comparator.compare('', null)).toBe(0);
    expect(comparator.compare(null, '')).toBe(0);

    expect(comparator.compare('doc8', 'doc9.txt')).toBe(-1);
    expect(comparator.compare('doc9', 'doc9.txt')).toBe(-1);
    expect(comparator.compare('doc9', 'doc10.txt')).toBe(-1);
    expect(comparator.compare('doc9.txt', 'myfile.txt')).toBe(-1);
    expect(comparator.compare('doc 9 .txt 10', 'doc 9')).toBe(1);
  });

  it('tests \'compareIgnoreCase\' method of ALPHANUMERIC comparator', () => {
    let comparator = comparators.ALPHANUMERIC;

    expect(comparator.compareIgnoreCase(undefined, undefined)).toBe(0);
    expect(comparator.compareIgnoreCase(null, null)).toBe(0);
    expect(comparator.compareIgnoreCase('', '')).toBe(0);

    expect(comparator.compareIgnoreCase('', null)).toBe(0);
    expect(comparator.compareIgnoreCase(null, '')).toBe(0);

    expect(comparator.compareIgnoreCase(null, undefined)).toBe(0);
    expect(comparator.compareIgnoreCase('', undefined)).toBe(0);

    expect(comparator.compareIgnoreCase(undefined, 'doc8')).toBe(-1);
    expect(comparator.compareIgnoreCase('doc8', null)).toBe(1);
    expect(comparator.compareIgnoreCase('doc8', '')).toBe(1);

    expect(comparator.compareIgnoreCase('doc8', 'doc8')).toBe(0);
    expect(comparator.compareIgnoreCase('doc8', 'DOC8')).toBe(0);
    expect(comparator.compareIgnoreCase('doc8', 'doc9.txt')).toBe(-1);
    expect(comparator.compareIgnoreCase('Doc8', 'doc9.txt')).toBe(-1);
    expect(comparator.compareIgnoreCase('doc9', 'doc9.txt')).toBe(-1);
    expect(comparator.compareIgnoreCase('doc9', 'Doc9.txt')).toBe(-1);
    expect(comparator.compareIgnoreCase('doc 9 .txt 10', 'doc 9')).toBe(1);
    expect(comparator.compareIgnoreCase('doc 9 .TXT 10', 'doc 9')).toBe(1);
    expect(comparator.compareIgnoreCase('doc9', 'adoc10')).toBe(1);
    expect(comparator.compareIgnoreCase('doc9', 'DOC 9')).toBe(-1);

    expect(comparator.compareIgnoreCase('doc 9', 'DOC-9')).toBe(-1);
    expect(comparator.compareIgnoreCase('doc\n9', 'DOC 9')).toBe(-1);
    expect(comparator.compareIgnoreCase('doc\n9', 'DOC-9')).toBe(-1);
    expect(comparator.compareIgnoreCase('doc\n9', 'DOC\n\n9')).toBe(-1);
  });

  it('tests \'compareIgnoreCase\' method of ALPHANUMERIC comparator with session', () => {
    let comparator = comparators.ALPHANUMERIC;
    comparator.install(createSession());
    expect(comparator.compareIgnoreCase('doc8', 'doc8')).toBe(0);
    expect(comparator.compareIgnoreCase('DoC8', 'dOc8')).toBe(0);
    expect(comparator.compareIgnoreCase('doc8', 'doc9.txt')).toBe(-1);
    expect(comparator.compareIgnoreCase('Doc9', 'doc9.txt')).toBe(-1);
    expect(comparator.compareIgnoreCase('doc 9 .TXT 10', 'doc 9')).toBe(1);
    expect(comparator.compareIgnoreCase('doc9', 'DOC 9')).toBe(-1);
    expect(comparator.compareIgnoreCase('doc 9', 'DOC-9')).toBe(-1);
    expect(comparator.compareIgnoreCase('doc\n9', 'DOC 9')).toBe(-1);
    expect(comparator.compareIgnoreCase('doc\n9', 'DOC-9')).toBe(-1);
    expect(comparator.compareIgnoreCase('doc\n9', 'DOC\n\n9')).toBe(-1);
  });

  describe('compare', () => {
    it('compares each pair until one is not equal', () => {
      let comparator = comparators.TEXT;
      comparator.install(createSession());
      expect(comparators.compare(comparator.compare.bind(comparator),
        ['b', 'a'],
        ['c', 'd'])).toBe(1);

      expect(comparators.compare(comparator.compare.bind(comparator),
        ['a', 'a'],
        ['c', 'd'])).toBe(-1);

      expect(comparators.compare(comparator.compare.bind(comparator),
        ['a', 'a'],
        ['c', 'c'])).toBe(0);
    });
  });

  function createSession(userAgent) {
    setFixtures(sandbox());
    let session = sandboxSession({
      'userAgent': userAgent
    });
    // test request only, don't test response (would require valid session, desktop etc.)
    session._processStartupResponse = () => {
    };
    return session;
  }
});
