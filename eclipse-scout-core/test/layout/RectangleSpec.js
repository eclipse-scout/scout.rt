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
});
