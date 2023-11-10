/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {TileGridLayoutConfig} from '../../src/index';

describe('TileGridLayoutConfig', () => {
  it('can be copied using copy constructor or clone', () => {
    let config = new TileGridLayoutConfig({
      hgap: 3,
      vgap: 4,
      columnWidth: 5,
      rowHeight: 6,
      minWidth: 7,
      maxWidth: 8
    });
    expect(config.hgap).toBe(3);
    expect(config.vgap).toBe(4);
    expect(config.columnWidth).toBe(5);
    expect(config.rowHeight).toBe(6);
    expect(config.minWidth).toBe(7);
    expect(config.maxWidth).toBe(8);

    let config2 = new TileGridLayoutConfig(config);
    expect(config2).toEqual(config);

    let config3 = config.clone();
    expect(config3).toEqual(config);
    expect(config3).not.toBe(config);
  });

  it('can be enriched using clone', () => {
    let config = new TileGridLayoutConfig({
      hgap: 3,
      vgap: 4,
      columnWidth: 5,
      rowHeight: 6,
      minWidth: 7,
      maxWidth: 8
    });

    expect(config.clone({
      rowHeight: 99,
      minWidth: 77,
      maxWidth: 88
    })).toEqual(new TileGridLayoutConfig({
      hgap: 3,
      vgap: 4,
      columnWidth: 5,
      rowHeight: 99,
      minWidth: 77,
      maxWidth: 88
    }));
  });
});
