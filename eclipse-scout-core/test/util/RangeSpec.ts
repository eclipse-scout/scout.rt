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
import {Range} from '../../src/index';

describe('Range', () => {

  it('equals', () => {
    expect(new Range(10, 20).equals(new Range(10, 20))).toBe(true);
    expect(new Range(20, 10).equals(new Range(10, 20))).toBe(false);
    expect(new Range(10, 20).equals(new Range(20, 10))).toBe(false);
  });

  describe('add', () => {
    it('returns a new range with the sum of both ranges', () => {
      let range1 = new Range(0, 10);
      let range2 = new Range(5, 20);
      expect(range1.add(range2)).toEqual(new Range(0, 20));

      range1 = new Range(5, 20);
      range2 = new Range(0, 10);
      expect(range1.add(range2)).toEqual(new Range(0, 20));

      range1 = new Range(0, 10);
      range2 = new Range(10, 20);
      expect(range1.add(range2)).toEqual(new Range(0, 20));
    });

    it('fails if the new range does not border on the existing', () => {
      let range1 = new Range(0, 10);
      let range2 = new Range(11, 20);
      expect(() => {
        range1.add(range2);
      }).toThrow(new Error('Range to add has to border on the existing range. scout.Range[from=0 to=10], scout.Range[from=11 to=20]'));
    });

    it('returns a copy of the non empty range if one range is empty', () => {
      let range1 = new Range(0, 10);
      let range2 = new Range(0, 0);
      expect(range1.add(range2)).toEqual(new Range(0, 10));

      range1 = new Range(0, 0);
      range2 = new Range(0, 10);
      expect(range1.add(range2)).toEqual(new Range(0, 10));
    });
  });

  describe('union', () => {
    it('returns a new range with the sum of both ranges', () => {
      let range1 = new Range(0, 10);
      let range2 = new Range(5, 20);
      expect(range1.union(range2)).toEqual([new Range(0, 20)]);

      range1 = new Range(5, 20);
      range2 = new Range(0, 10);
      expect(range1.union(range2)).toEqual([new Range(0, 20)]);

      range1 = new Range(0, 10);
      range2 = new Range(10, 20);
      expect(range1.union(range2)).toEqual([new Range(0, 20)]);
    });

    it('returns a copy of both ranges if the ranges don\'t overlap', () => {
      let range1 = new Range(0, 10);
      let range2 = new Range(11, 20);
      expect(range1.union(range2)).toEqual([new Range(0, 10), new Range(11, 20)]);
    });

    it('returns a copy of the non empty range if one range is empty', () => {
      let range1 = new Range(0, 10);
      let range2 = new Range(0, 0);
      expect(range1.union(range2)).toEqual([new Range(0, 10)]);

      range1 = new Range(0, 0);
      range2 = new Range(0, 10);
      expect(range1.union(range2)).toEqual([new Range(0, 10)]);
    });
  });

  describe('subtract', () => {
    it('returns a new range where the second range is removed from the first', () => {
      let range1 = new Range(0, 10);
      let range2 = new Range(5, 20);
      expect(range1.subtract(range2)).toEqual([new Range(0, 5)]);

      range1 = new Range(5, 15);
      range2 = new Range(0, 10);
      expect(range1.subtract(range2)).toEqual([new Range(10, 15)]);
    });

    it('returns a copy of the first range if the second does not overlap the first', () => {
      let range1 = new Range(0, 10);
      let range2 = new Range(11, 20);
      expect(range1.subtract(range2)).toEqual([new Range(0, 10)]);

      range1 = new Range(0, 10);
      range2 = new Range(10, 20);
      expect(range1.subtract(range2)).toEqual([new Range(0, 10)]);

      range1 = new Range(5, 15);
      range2 = new Range(0, 5);
      expect(range1.subtract(range2)).toEqual([new Range(5, 15)]);

      range1 = new Range(5, 15);
      range2 = new Range(0, 4);
      expect(range1.subtract(range2)).toEqual([new Range(5, 15)]);
    });

    it('returns an empty range if second range completely covers the first', () => {
      let range1 = new Range(5, 15);
      let range2 = new Range(0, 20);
      expect(range1.subtract(range2)).toEqual([new Range(0, 0)]);

      range1 = new Range(5, 15);
      range2 = new Range(0, 15);
      expect(range1.subtract(range2)).toEqual([new Range(0, 0)]);

      range1 = new Range(5, 15);
      range2 = new Range(5, 20);
      expect(range1.subtract(range2)).toEqual([new Range(0, 0)]);

      range1 = new Range(5, 15);
      range2 = new Range(5, 15);
      expect(range1.subtract(range2)).toEqual([new Range(0, 0)]);
    });

    it('returns a new range if second range is inside the first and touches a border', () => {
      let range1 = new Range(0, 10);
      let range2 = new Range(0, 2);
      expect(range1.subtract(range2)).toEqual([new Range(2, 10)]);

      range1 = new Range(0, 10);
      range2 = new Range(8, 10);
      expect(range1.subtract(range2)).toEqual([new Range(0, 8)]);
    });

    it('returns an array of two ranges if second range is inside the first but does not touch a border', () => {
      let range1 = new Range(0, 20);
      let range2 = new Range(5, 15);
      expect(range1.subtract(range2)).toEqual([new Range(0, 5), new Range(15, 20)]);
    });

    it('returns a copy of the first range if the second range is empty', () => {
      let range1 = new Range(0, 10);
      let range2 = new Range(0, 0);
      expect(range1.subtract(range2)).toEqual([new Range(0, 10)]);
    });

    it('returns an empty range if the first range is empty', () => {
      let range1 = new Range(0, 0);
      let range2 = new Range(0, 10);
      expect(range1.subtract(range2)).toEqual([new Range(0, 0)]);
    });

  });

  describe('subtractAll', () => {
    it('subtracts all given ranges', () => {
      let range1 = new Range(0, 10);
      let range2 = new Range(5, 20);
      let range3 = new Range(0, 2);
      expect(range1.subtractAll([range2, range3])).toEqual([new Range(2, 5)]);

      range1 = new Range(5, 15);
      range2 = new Range(0, 10);
      range3 = new Range(10, 14);
      expect(range1.subtractAll([range2, range3])).toEqual([new Range(14, 15)]);
    });

    it('may return multiple ranges', () => {
      let range1 = new Range(0, 20);
      let range2 = new Range(5, 10);
      let range3 = new Range(12, 15);
      expect(range1.subtractAll([range2, range3])).toEqual([new Range(0, 5), new Range(10, 12), new Range(15, 20)]);
    });
  });

  describe('intersect', () => {
    it('returns a new range with the part where both ranges overlap', () => {
      let range1 = new Range(0, 10);
      let range2 = new Range(5, 20);
      expect(range1.intersect(range2)).toEqual(new Range(5, 10));
    });

    it('returns an empty range if the ranges don\'t overlap', () => {
      let range1 = new Range(0, 10);
      let range2 = new Range(10, 20);
      expect(range1.intersect(range2)).toEqual(new Range(0, 0));

      range1 = new Range(10, 20);
      range2 = new Range(0, 10);
      expect(range1.intersect(range2)).toEqual(new Range(0, 0));
    });

    it('returns an empty range if one range is empty', () => {
      let range1 = new Range(0, 0);
      let range2 = new Range(0, 20);
      expect(range1.intersect(range2)).toEqual(new Range(0, 0));

      range1 = new Range(10, 10);
      range2 = new Range(10, 20);
      expect(range1.intersect(range2)).toEqual(new Range(0, 0));

      range1 = new Range(11, 11);
      range2 = new Range(10, 20);
      expect(range1.intersect(range2)).toEqual(new Range(11, 11));
      expect(range1.intersect(range2).size()).toEqual(0);
    });
  });
});
