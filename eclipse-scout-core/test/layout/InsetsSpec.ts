/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Insets} from '../../src/index';

describe('Insets', () => {

  it('equals', () => {
    let i1 = new Insets(0, 0, 10, 5);
    let i2 = new Insets(0, 0, 20, -1);
    let i3 = new Insets(0, 0, 20, -1);
    let i4 = new Insets(0.5, 0.1, 10.2, 5.9);
    let i5 = new Insets(14, 15, 10, 5);
    let i6 = new Insets(14, 15, 20, -1);

    expect(i1.equals(i2)).toBe(false);
    expect(i2.equals(i3)).toBe(true);
    expect(i1.equals(i4)).toBe(false);
    expect(i1.equals(i4.floor())).toBe(true);
    expect(i1.equals(i4.ceil())).toBe(false);
    expect(i1.equals(i5)).toBe(false);
    expect(i2.equals(i6)).toBe(false);
    expect(i5.equals(i6)).toBe(false);
  });
});
