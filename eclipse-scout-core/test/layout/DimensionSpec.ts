/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Dimension} from '../../src/index';

describe('Dimension', () => {

  it('accepts two numbers as width and height arguments', () => {
    let dim = new Dimension(6, 7);
    expect(dim.width).toBe(6);
    expect(dim.height).toBe(7);
  });

  it('accepts a single Dimension argument', () => {
    let dim1 = new Dimension(6, 7);
    let dim2 = new Dimension(dim1);
    expect(dim2.width).toBe(6);
    expect(dim2.height).toBe(7);
    expect(dim1).toEqual(dim2);
  });

  it('equals', () => {
    let d1 = new Dimension(10, 5);
    let d2 = new Dimension(20, 20);
    let d3 = new Dimension(d2);
    let d4 = new Dimension(10.2, 5.9);

    expect(d1.equals(d2)).toBe(false);
    expect(d2.equals(d3)).toBe(true);
    expect(d1.equals(d4)).toBe(false);
    expect(d1.equals(d4.floor())).toBe(true);
    expect(d1.equals(d4.ceil())).toBe(false);
  });
});
