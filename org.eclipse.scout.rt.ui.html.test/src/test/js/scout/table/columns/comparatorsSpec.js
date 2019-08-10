/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
describe("scout.comparators", function() {

  it("tests 'compare' method of TEXT comparator", function() {
    var comparator = scout.comparators.TEXT;

    expect(comparator.compare(null, null)).toBe(0);
    expect(comparator.compare(null, 'a')).toBe(-1);
    expect(comparator.compare('a', null)).toBe(1);
    expect(comparator.compare('a', 'a')).toBe(0);
    expect(comparator.compare('a', 'b')).toBe(-1);
    expect(comparator.compare('b', 'a')).toBe(1);
  });

  it("tests 'compareIgnoreCase' method of TEXT comparator", function() {
    var comparator = scout.comparators.TEXT;

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

    expect(comparator.compare('a', 'B')).toBe(1);
    expect(comparator.compare('B', 'a')).toBe(-1);
  });

  it("tests 'compare' method of NUMERIC comparator", function() {
    var comparator = scout.comparators.NUMERIC;

    expect(comparator.compare('1', '1')).toBe(0);
    expect(comparator.compare('1', '2')).toBe(-1);
    expect(comparator.compare('2', '1')).toBe(1);

    expect(comparator.compare('1.0001', '1.0001')).toBe(0);
    expect(comparator.compare('1.9998', '1.9999')).toBe(-1);
    expect(comparator.compare('1.9999', '1.9998')).toBe(1);
  });

  it("tests 'compare' method of ALPHANUMERIC comparator", function() {
    var comparator = scout.comparators.ALPHANUMERIC;

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

  it("tests 'compareIgnoreCase' method of ALPHANUMERIC comparator", function() {
    var comparator = scout.comparators.ALPHANUMERIC;

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

  it("tests 'compareIgnoreCase' method of ALPHANUMERIC comparator with session", function() {
    var comparator = scout.comparators.ALPHANUMERIC;
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

  function createSession(userAgent) {
    setFixtures(sandbox());
    var session = sandboxSession({
      'userAgent': userAgent
    });
    // test request only, don't test response (would require valid session, desktop etc.)
    session._processStartupResponse = function() {};
    return session;
  }
});
