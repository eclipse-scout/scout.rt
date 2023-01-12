/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Point} from '../../src/index';

describe('Point', () => {

  it('equals', () => {
    let p1 = new Point(10, 5);
    let p2 = new Point(20, -1);
    let p3 = new Point(20, -1);
    let p4 = new Point(10.2, 5.9);

    expect(p1.equals(p2)).toBe(false);
    expect(p2.equals(p3)).toBe(true);
    expect(p1.equals(p4)).toBe(false);
    expect(p1.equals(p4.floor())).toBe(true);
    expect(p1.equals(p4.ceil())).toBe(false);
  });
});
