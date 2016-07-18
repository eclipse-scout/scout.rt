/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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

});
