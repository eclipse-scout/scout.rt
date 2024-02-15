/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Rectangle} from '../../src/index';

describe('Rectangle', () => {

  it('equals', () => {
    let r1 = new Rectangle(0, 0, 10, 5);
    let r2 = new Rectangle(0, 0, 20, -1);
    let r3 = new Rectangle(0, 0, 20, -1);
    let r4 = new Rectangle(0.5, 0.1, 10.2, 5.9);
    let r5 = new Rectangle(14, 15, 10, 5);
    let r6 = new Rectangle(14, 15, 20, -1);

    expect(r1.equals(r2)).toBe(false);
    expect(r2.equals(r3)).toBe(true);
    expect(r1.equals(r4)).toBe(false);
    expect(r1.equals(r4.floor())).toBe(true);
    expect(r1.equals(r4.ceil())).toBe(false);
    expect(r1.equals(r5)).toBe(false);
    expect(r2.equals(r6)).toBe(false);
    expect(r5.equals(r6)).toBe(false);
  });

  it('intersects', () => {
    let r1 = new Rectangle(0, 0, 10, 5);
    let r2 = new Rectangle(0, 0, 20, -1);
    let r3 = new Rectangle(9, 0, 10, 5);
    let r4 = new Rectangle(10, 0, 10, 5);
    let r5 = new Rectangle(0, 4, 10, 5);
    let r6 = new Rectangle(0, 5, 10, 5);
    let r7 = new Rectangle(4, 7, 10, 5);
    let r8 = new Rectangle(7, 4, 10, 5);
    let r9 = new Rectangle(10, 5, 10, 5);
    let r10 = new Rectangle(12, 8, 10, 5);

    expect(r1.intersects(r1)).toBe(true);
    expect(r1.intersects(r2)).toBe(false);
    expect(r2.intersects(r2)).toBe(false);
    expect(r1.intersects(r5)).toBe(true);
    expect(r1.intersects(r3)).toBe(true);
    expect(r1.intersects(r4)).toBe(false);
    expect(r1.intersects(r6)).toBe(false);
    expect(r1.intersects(r7)).toBe(false);
    expect(r1.intersects(r8)).toBe(true);
    expect(r1.intersects(r9)).toBe(false);
    expect(r1.intersects(r10)).toBe(false);
  });

  describe('intersect', () => {
    it('computes the intersection between two rectangles', () => {
      let rect = new Rectangle(2, 3, 10, 8);

      // Second rectangle is as big as first
      expect(rect.intersect(new Rectangle(rect))).toEqual(rect);

      // Second rectangle completely covers first
      expect(rect.intersect(new Rectangle(0, 1, 20, 30))).toEqual(rect);

      // Second rectangle is completely inside first
      expect(rect.intersect(new Rectangle(3, 4, 3, 2))).toEqual(new Rectangle(3, 4, 3, 2));

      // Second rectangle starts inside and overlaps right and bottom
      expect(rect.intersect(new Rectangle(4, 5, 20, 20))).toEqual(new Rectangle(4, 5, 8, 6));

      // Second rectangle starts further left and top and ends inside first
      expect(rect.intersect(new Rectangle(0, 1, 3, 4))).toEqual(new Rectangle(2, 3, 1, 2));

      // Second rectangle starts further left and top and touches right border of first
      expect(rect.intersect(new Rectangle(-4, -5, 17, 10))).toEqual(new Rectangle(2, 3, 10, 2));
    });

    it('returns an empty rectangle if the rectangles don\'t intersect', () => {
      let rect = new Rectangle(2, 3, 10, 8);
      // Second rectangle touches first on the bottom right edge
      expect(rect.intersect(new Rectangle(12, 11, 5, 5))).toEqual(new Rectangle());

      // Second rectangle touches first on the top left edge
      expect(rect.intersect(new Rectangle(0, 0, 2, 8))).toEqual(new Rectangle());
    });

    it('returns an empty rectangle if the second rectangle is null or empty', () => {
      expect(new Rectangle(2, 3, 10, 8).intersect(null)).toEqual(new Rectangle());
      expect(new Rectangle().intersect(new Rectangle())).toEqual(new Rectangle());
    });
  });
});
