/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {GridData} from '../../../src';

describe('GridData', () => {
  describe('equals', () => {
    it('returns true if the grid datas are equal', () => {
      let gridData = new GridData();
      expect(gridData.equals(gridData)).toBe(true);
      expect(gridData.equals(new GridData())).toBe(true);

      gridData = new GridData({x: 5, y: 10, w: 3, h: 5});
      expect(gridData.equals(new GridData({x: 5, y: 10, w: 3, h: 5}))).toBe(true);
    });

    it('returns false if the grid datas are not equal', () => {
      let gridData = new GridData();
      expect(gridData.equals(new GridData({w: 3}))).toBe(false);
      expect(gridData.equals(null)).toBe(false);

      gridData = new GridData({x: 5, y: 10, w: 3, h: 5});
      expect(gridData.equals(new GridData({x: 1, y: 10, w: 3, h: 5}))).toBe(false);
    });
  });
});
