/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {Dimension, InitModelOf, Insets, LogicalGridData, LogicalGridLayoutInfo, Rectangle} from '../../../src/index';

describe('LogicalGridLayoutInfo', () => {
  function $dummyComps(count: number): JQuery[] {
    let comps = [];
    for (let i = 0; i < count; i++) {
      comps.push($('<div>'));
    }
    return comps;
  }

  function newLogicalGridLayoutInfo(cons: LogicalGridData[], opts?: InitModelOf<LogicalGridLayoutInfo>) {
    return new LogicalGridLayoutInfo($.extend({
      $components: $dummyComps(cons.length),
      cons: cons,
      hgap: 5,
      vgap: 5,
      rowHeight: 30,
      columnWidth: 50
    }, opts));
  }

  function expectWidths(rows: Rectangle[][], rowNum: number, widths: number[], height: number, hgap: number, vgap: number) {
    let row = rows[rowNum];
    expect(row.length).toBe(widths.length);

    let x = 0;
    for (let i = 0; i < widths.length; i++) {
      if (i > 0) {
        x += widths[i - 1] + hgap;
      }
      let y = rowNum * (height + vgap);
      expect(row[i]).withContext(`cell ${i}`).toEqual(new Rectangle(x, y, widths[i], height));
    }
  }

  function expectHeights(cols: Rectangle[][], colNum: number, heights: number[], width: number, hgap: number, vgap: number) {
    let col = cols[colNum];
    expect(col.length).toBe(heights.length);

    let y = 0;
    for (let i = 0; i < heights.length; i++) {
      if (i > 0) {
        y += heights[i - 1] + vgap;
      }
      let x = colNum * (width + hgap);
      expect(col[i]).withContext(`cell ${i}`).toEqual(new Rectangle(x, y, width, heights[i]));
    }
  }

  /**
   * Transposes the given matrix (converts rows to columns).
   */
  function toCols(rows: Rectangle[][]): Rectangle[][] {
    let cols = [];
    for (let i = 0; i < rows.length; i++) {
      for (let j = 0; j < rows[i].length; j++) {
        if (!cols[j]) {
          cols[j] = [];
        }
        cols[j][i] = rows[i][j];
      }
    }
    return cols;
  }

  describe('layoutCellBounds', () => {
    let parentSize = new Dimension(500, 400);
    let parentInsets = new Insets(0, 0, 0, 0);

    it('calculates bounds', () => {
      let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weightx: 0});
      let gd2 = new LogicalGridData({gridx: 1, gridy: 0, gridw: 1, gridh: 1, weightx: 1});
      let rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(parentSize, parentInsets);
      expect(rows.length).toBe(1);

      let cells = rows[0];
      expect(cells.length).toBe(2);

      let cell = cells[0];
      expect(cell).toEqual(new Rectangle(0, 0, 50, 30));

      cell = cells[1];
      expect(cell).toEqual(new Rectangle(55, 0, parentSize.width - 55, 30));
    });

    it('considers widthHint', () => {
      let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weightx: 0, widthHint: 70});
      let gd2 = new LogicalGridData({gridx: 1, gridy: 0, gridw: 1, gridh: 1, weightx: 1});
      let rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(parentSize, parentInsets);
      expect(rows[0][0]).toEqual(new Rectangle(0, 0, 70, 30));
      expect(rows[0][1]).toEqual(new Rectangle(75, 0, parentSize.width - 75, 30));
    });

    describe('maxWidth', () => {
      it('scales cell not bigger than maxWidth', () => {
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weightx: 1, maxWidth: 80});
        let gd2 = new LogicalGridData({gridx: 1, gridy: 0, gridw: 1, gridh: 1, weightx: 1}); // maxWidth default is 0
        let rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(parentSize, parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 80, 30));
        expect(rows[0][1]).toEqual(new Rectangle(85, 0, parentSize.width - 85, 30));
      });

      it('scales cell not bigger than maxWidth even if prefSize is bigger', () => {
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weightx: 1, maxWidth: 80, widthHint: 100});
        let rows = newLogicalGridLayoutInfo([gd1]).layoutCellBounds(parentSize, parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 80, 30));
      });

      it('scales cell not bigger than maxWidth even if weightX is 0', () => {
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weightx: 0, maxWidth: 80});
        let rows = newLogicalGridLayoutInfo([gd1], {columnWidth: 100}).layoutCellBounds(parentSize, parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 80, 30));
      });

      it('distributes maxWidth to spanned cells', () => {
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 2, gridh: 1, weightx: 1, maxWidth: 80});
        let rows = newLogicalGridLayoutInfo([gd1]).layoutCellBounds(parentSize, parentInsets);
        expectWidths(rows, 0, [37, 38], 30, 5, 5);
      });

      it('distributes maxWidth to spanned cells with one row having w=1', () => {
        // w = 2 on first row, w = 1 on second
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 2, gridh: 1, weightx: 1, maxWidth: 80});
        let gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 1, gridh: 1, weightx: 1, maxWidth: 80});
        let rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(parentSize, parentInsets);
        expectWidths(rows, 0, [37, 38], 30, 5, 5);
        expectWidths(rows, 1, [37, 38], 30, 5, 5);

        // w = 1 on first row, w = 2 on second -> should lead to same result
        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weightx: 1, maxWidth: 80});
        gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 2, gridh: 1, weightx: 1, maxWidth: 80});
        rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(parentSize, parentInsets);
        expectWidths(rows, 0, [37, 38], 30, 5, 5);
        expectWidths(rows, 1, [37, 38], 30, 5, 5);
      });

      it('distributes maxWidth to spanned cells with spans on every row', () => {
        // w = 4 on first row, w = 3 on second
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 4, gridh: 1, weightx: 1, maxWidth: 80});
        let gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 3, gridh: 1, weightx: 1, maxWidth: 80});
        let rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(parentSize, parentInsets);
        expectWidths(rows, 0, [16, 16, 17, 17], 30, 5, 5);
        expectWidths(rows, 1, [16, 16, 17, 17], 30, 5, 5);

        // w = 4 on first row, w = 3 on second -> should lead to same result
        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 3, gridh: 1, weightx: 1, maxWidth: 80});
        gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 4, gridh: 1, weightx: 1, maxWidth: 80});
        rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(parentSize, parentInsets);
        expectWidths(rows, 0, [16, 16, 16, 17], 30, 5, 5);
        expectWidths(rows, 1, [16, 16, 16, 17], 30, 5, 5);
      });

      it('scales cell not bigger than maxWidth even if there is a spanned cell on another row', () => {
        // w = 2 and w = 1 on first row, w = 3 on second
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 2, gridh: 1, weightx: 1, maxWidth: 80});
        let gd2 = new LogicalGridData({gridx: 2, gridy: 0, gridw: 1, gridh: 1, weightx: 1, maxWidth: 80});
        let gd3 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 3, gridh: 1, weightx: 1, maxWidth: 80});
        let rows = newLogicalGridLayoutInfo([gd1, gd2, gd3]).layoutCellBounds(parentSize, parentInsets);
        expectWidths(rows, 0, [23, 23, 24], 30, 5, 5);
        expectWidths(rows, 1, [23, 23, 24], 30, 5, 5);

        // w = 3 on first row, w = 2 and w = 1 on second -> should lead to same result
        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 3, gridh: 1, weightx: 1, maxWidth: 80});
        gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 2, gridh: 1, weightx: 1, maxWidth: 80});
        gd3 = new LogicalGridData({gridx: 2, gridy: 1, gridw: 1, gridh: 1, weightx: 1, maxWidth: 80});
        rows = newLogicalGridLayoutInfo([gd1, gd2, gd3]).layoutCellBounds(parentSize, parentInsets);
        expectWidths(rows, 0, [23, 24, 24], 30, 5, 5);
        expectWidths(rows, 1, [23, 24, 24], 30, 5, 5);
      });

      it('scales cell not bigger than maxWidth with multiple spans on multiple rows', () => {
        // w = 3 and w = 2 on first row, w = 5 on second
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 3, gridh: 1, weightx: 1, maxWidth: 80});
        let gd2 = new LogicalGridData({gridx: 3, gridy: 0, gridw: 2, gridh: 1, weightx: 1, maxWidth: 80});
        let gd3 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 5, gridh: 1, weightx: 1, maxWidth: 80});
        let rows = newLogicalGridLayoutInfo([gd1, gd2, gd3]).layoutCellBounds(parentSize, parentInsets);
        expectWidths(rows, 0, [12, 12, 12, 12, 12], 30, 5, 5);
        expectWidths(rows, 1, [12, 12, 12, 12, 12], 30, 5, 5);

        // w = 5 on first row, w = 3 and w = 2 on second -> should lead to same result
        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 5, gridh: 1, weightx: 1, maxWidth: 80});
        gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 3, gridh: 1, weightx: 1, maxWidth: 80});
        gd3 = new LogicalGridData({gridx: 3, gridy: 1, gridw: 2, gridh: 1, weightx: 1, maxWidth: 80});
        rows = newLogicalGridLayoutInfo([gd1, gd2, gd3]).layoutCellBounds(parentSize, parentInsets);
        expectWidths(rows, 0, [12, 12, 13, 12, 13], 30, 5, 5);
        expectWidths(rows, 1, [12, 12, 13, 12, 13], 30, 5, 5);
      });

      it('scales cell not bigger than maxWidth even if on the left and right are used cells', () => {
        // w = 1, w = 2 and w = 1 on first row
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weightx: 1, maxWidth: 80});
        let gd2 = new LogicalGridData({gridx: 1, gridy: 0, gridw: 2, gridh: 1, weightx: 1, maxWidth: 80});
        let gd3 = new LogicalGridData({gridx: 3, gridy: 0, gridw: 1, gridh: 1, weightx: 1, maxWidth: 80});
        let rows = newLogicalGridLayoutInfo([gd1, gd2, gd3]).layoutCellBounds(parentSize, parentInsets);
        expectWidths(rows, 0, [80, 37, 38, 80], 30, 5, 5);

        // w = 2, w = 1 and w = 2 on first row
        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 2, gridh: 1, weightx: 1, maxWidth: 80});
        gd2 = new LogicalGridData({gridx: 2, gridy: 0, gridw: 1, gridh: 1, weightx: 1, maxWidth: 80});
        gd3 = new LogicalGridData({gridx: 3, gridy: 0, gridw: 2, gridh: 1, weightx: 1, maxWidth: 80});
        rows = newLogicalGridLayoutInfo([gd1, gd2, gd3]).layoutCellBounds(parentSize, parentInsets);
        expectWidths(rows, 0, [37, 38, 80, 37, 38], 30, 5, 5);
      });

      it('respects absolute max width', () => {
        let parentSize = new Dimension(50000, 400);
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weightx: 1, maxWidth: 40000});
        let rows = newLogicalGridLayoutInfo([gd1]).layoutCellBounds(parentSize, parentInsets);
        expectWidths(rows, 0, [10240], 30, 5, 5);

        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 2, gridh: 1, weightx: 1, maxWidth: 40000});
        rows = newLogicalGridLayoutInfo([gd1]).layoutCellBounds(parentSize, parentInsets);
        expectWidths(rows, 0, [10240, 10241], 30, 5, 5);

        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weightx: 1, maxWidth: 40000});
        let gd2 = new LogicalGridData({gridx: 1, gridy: 0, gridw: 2, gridh: 1, weightx: 1, maxWidth: 40000});
        rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(parentSize, parentInsets);
        expectWidths(rows, 0, [10240, 10240, 10241], 30, 5, 5);

        // Two rows
        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weightx: 1, maxWidth: 40000});
        gd2 = new LogicalGridData({gridx: 1, gridy: 0, gridw: 1, gridh: 1, weightx: 1, maxWidth: 40000});
        let gd3 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 2, gridh: 1, weightx: 1, maxWidth: 40000});
        rows = newLogicalGridLayoutInfo([gd1, gd2, gd3]).layoutCellBounds(parentSize, parentInsets);
        expectWidths(rows, 0, [10240, 10241], 30, 5, 5);
        expectWidths(rows, 1, [10240, 10241], 30, 5, 5);
      });
    });

    describe('maxHeight', () => {
      it('scales cell not bigger than maxHeight', () => {
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weighty: 1, maxHeight: 80});
        let gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 1, gridh: 1, weighty: 1}); // minHeight default is 0
        let rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(parentSize, parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 50, 80));
        expect(rows[1][0]).toEqual(new Rectangle(0, 85, 50, parentSize.height - 85));
      });

      it('scales cell not bigger than maxHeight even if prefSize is bigger', () => {
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weighty: 1, maxHeight: 80, heightHint: 100});
        let rows = newLogicalGridLayoutInfo([gd1]).layoutCellBounds(parentSize, parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 50, 80));
      });

      it('scales cell not bigger than maxHeight even if weightY is 0', () => {
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weighty: 0, maxHeight: 80});
        let rows = newLogicalGridLayoutInfo([gd1], {rowHeight: 100}).layoutCellBounds(parentSize, parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 50, 80));
      });

      it('distributes maxHeight to spanned cells', () => {
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 2, weighty: 1, maxHeight: 80});
        let cols = toCols(newLogicalGridLayoutInfo([gd1]).layoutCellBounds(parentSize, parentInsets));
        expectHeights(cols, 0, [37, 38], 50, 5, 5);
      });

      it('distributes maxHeight to spanned cells with one col having h=1', () => {
        // h = 2 on first col, h = 1 on second
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 2, weighty: 1, maxHeight: 80});
        let gd2 = new LogicalGridData({gridx: 1, gridy: 0, gridw: 1, gridh: 1, weighty: 1, maxHeight: 80});
        let cols = toCols(newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(parentSize, parentInsets));
        expectHeights(cols, 0, [37, 38], 50, 5, 5);
        expectHeights(cols, 1, [37, 38], 50, 5, 5);

        // h = 1 on first col, h = 2 on second -> should lead to same result
        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weighty: 1, maxHeight: 80});
        gd2 = new LogicalGridData({gridx: 1, gridy: 0, gridw: 0, gridh: 2, weighty: 1, maxHeight: 80});
        expectHeights(cols, 0, [37, 38], 50, 5, 5);
        expectHeights(cols, 1, [37, 38], 50, 5, 5);
      });

      it('distributes maxHeight to spanned cells with spans on every col', () => {
        // h = 4 on first col, h = 3 on second
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 4, weighty: 1, maxHeight: 80});
        let gd2 = new LogicalGridData({gridx: 1, gridy: 0, gridw: 1, gridh: 3, weighty: 1, maxHeight: 80});
        let cols = toCols(newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(parentSize, parentInsets));
        expectHeights(cols, 0, [16, 16, 17, 17], 50, 5, 5);
        expectHeights(cols, 1, [16, 16, 17, 17], 50, 5, 5);

        // h = 3 on first col, h = 4 on second -> should lead to same result
        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 3, weighty: 1, maxHeight: 80});
        gd2 = new LogicalGridData({gridx: 1, gridy: 0, gridw: 1, gridh: 4, weighty: 1, maxHeight: 80});
        cols = toCols(newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(parentSize, parentInsets));
        expectHeights(cols, 0, [16, 16, 16, 17], 50, 5, 5);
        expectHeights(cols, 1, [16, 16, 16, 17], 50, 5, 5);
      });

      it('scales cell not bigger than maxHeight even if there is a spanned cell on another col', () => {
        // h = 2 and h = 1 on first col, h = 3 on second
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 2, weighty: 1, maxHeight: 80});
        let gd2 = new LogicalGridData({gridx: 0, gridy: 2, gridw: 1, gridh: 1, weighty: 1, maxHeight: 80});
        let gd3 = new LogicalGridData({gridx: 1, gridy: 0, gridw: 1, gridh: 3, weighty: 1, maxHeight: 80});
        let cols = toCols(newLogicalGridLayoutInfo([gd1, gd2, gd3]).layoutCellBounds(parentSize, parentInsets));
        expectHeights(cols, 0, [23, 23, 24], 50, 5, 5);
        expectHeights(cols, 1, [23, 23, 24], 50, 5, 5);

        // h = 3 on first col, h = 2 and h = 1 on second -> should lead to same result
        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 3, weighty: 1, maxHeight: 80});
        gd2 = new LogicalGridData({gridx: 1, gridy: 0, gridw: 1, gridh: 2, weighty: 1, maxHeight: 80});
        gd3 = new LogicalGridData({gridx: 1, gridy: 2, gridw: 1, gridh: 1, weighty: 1, maxHeight: 80});
        cols = toCols(newLogicalGridLayoutInfo([gd1, gd2, gd3]).layoutCellBounds(parentSize, parentInsets));
        expectHeights(cols, 0, [23, 24, 24], 50, 5, 5);
        expectHeights(cols, 1, [23, 24, 24], 50, 5, 5);
      });

      it('scales cell not bigger than maxHeight with multiple spans on multiple cols', () => {
        // h = 3 and h = 2 on first col, h = 5 on second
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 3, weighty: 1, maxHeight: 80});
        let gd2 = new LogicalGridData({gridx: 0, gridy: 3, gridw: 1, gridh: 2, weighty: 1, maxHeight: 80});
        let gd3 = new LogicalGridData({gridx: 1, gridy: 0, gridw: 1, gridh: 5, weighty: 1, maxHeight: 80});
        let cols = toCols(newLogicalGridLayoutInfo([gd1, gd2, gd3]).layoutCellBounds(parentSize, parentInsets));
        expectHeights(cols, 0, [12, 12, 12, 12, 12], 50, 5, 5);
        expectHeights(cols, 1, [12, 12, 12, 12, 12], 50, 5, 5);

        // h = 5 on first col, h = 3 and h = 2 on second -> should lead to same result
        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 5, weighty: 1, maxHeight: 80});
        gd2 = new LogicalGridData({gridx: 1, gridy: 0, gridw: 1, gridh: 3, weighty: 1, maxHeight: 80});
        gd3 = new LogicalGridData({gridx: 3, gridy: 3, gridw: 1, gridh: 2, weighty: 1, maxHeight: 80});
        cols = toCols(newLogicalGridLayoutInfo([gd1, gd2, gd3]).layoutCellBounds(parentSize, parentInsets));
        expectHeights(cols, 0, [12, 12, 13, 12, 13], 50, 5, 5);
        expectHeights(cols, 1, [12, 12, 13, 12, 13], 50, 5, 5);
      });

      it('scales cell not bigger than maxHeight even if on the top and bottom are used cells', () => {
        // h = 1, h = 2 and h = 1 on first col
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weighty: 1, maxHeight: 80});
        let gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 1, gridh: 2, weighty: 1, maxHeight: 80});
        let gd3 = new LogicalGridData({gridx: 0, gridy: 3, gridw: 1, gridh: 1, weighty: 1, maxHeight: 80});
        let cols = toCols(newLogicalGridLayoutInfo([gd1, gd2, gd3]).layoutCellBounds(parentSize, parentInsets));
        expectHeights(cols, 0, [80, 37, 38, 80], 50, 5, 5);

        // h = 2, h = 1 and h = 2 on first col
        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 2, weighty: 1, maxHeight: 80});
        gd2 = new LogicalGridData({gridx: 0, gridy: 2, gridw: 1, gridh: 1, weighty: 1, maxHeight: 80});
        gd3 = new LogicalGridData({gridx: 0, gridy: 3, gridw: 1, gridh: 2, weighty: 1, maxHeight: 80});
        cols = toCols(newLogicalGridLayoutInfo([gd1, gd2, gd3]).layoutCellBounds(parentSize, parentInsets));
        expectHeights(cols, 0, [37, 38, 80, 37, 38], 50, 5, 5);
      });

      it('respects absolute max height', () => {
        let parentSize = new Dimension(400, 50000);
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weighty: 1, maxHeight: 40000});
        let cols = toCols(newLogicalGridLayoutInfo([gd1]).layoutCellBounds(parentSize, parentInsets));
        expectHeights(cols, 0, [10240], 50, 5, 5);

        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 2, weighty: 1, maxHeight: 40000});
        cols = toCols(newLogicalGridLayoutInfo([gd1]).layoutCellBounds(parentSize, parentInsets));
        expectHeights(cols, 0, [10240, 10241], 50, 5, 5);

        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weighty: 1, maxHeight: 40000});
        let gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 1, gridh: 2, weighty: 1, maxHeight: 40000});
        cols = toCols(newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(parentSize, parentInsets));
        expectHeights(cols, 0, [10240, 10240, 10241], 50, 5, 5);

        // Two cols
        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weighty: 1, maxHeight: 40000});
        gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 1, gridh: 1, weighty: 1, maxHeight: 40000});
        let gd3 = new LogicalGridData({gridx: 1, gridy: 0, gridw: 1, gridh: 2, weighty: 1, maxHeight: 40000});
        cols = toCols(newLogicalGridLayoutInfo([gd1, gd2, gd3]).layoutCellBounds(parentSize, parentInsets));
        expectHeights(cols, 0, [10240, 10241], 50, 5, 5);
        expectHeights(cols, 1, [10240, 10241], 50, 5, 5);
      });
    });
  });
});
