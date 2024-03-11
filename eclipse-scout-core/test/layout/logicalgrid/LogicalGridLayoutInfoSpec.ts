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

/* This test also exists as Java code, to make sure Java and JS code produces the same results */
describe('LogicalGridLayoutInfo', () => {
  function $dummyComps(count: number) {
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

    describe('minWidth', () => {
      it('scales cell not smaller than minWidth', () => {
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weightx: 1, minWidth: 80});
        let gd2 = new LogicalGridData({gridx: 1, gridy: 0, gridw: 1, gridh: 1, weightx: 1}); // minWidth default is 0
        let rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(new Dimension(10, 10), parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 80, 30));
        expect(rows[0][1]).toEqual(new Rectangle(80, 0, 0, 30));
      });

      it('scales cell not smaller than minWidth even if prefSize is smaller', () => {
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weightx: 1, minWidth: 80, widthHint: 50});
        let rows = newLogicalGridLayoutInfo([gd1]).layoutCellBounds(new Dimension(10, 10), parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 80, 30));
      });

      it('scales cell not smaller than minWidth even if weightX is 0', () => {
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weightx: 0, minWidth: 80});
        let rows = newLogicalGridLayoutInfo([gd1]).layoutCellBounds(new Dimension(10, 10), parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 80, 30));
      });

      it('distributes minWidth to spanned cells', () => {
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 2, gridh: 1, weightx: 1, minWidth: 80});
        let rows = newLogicalGridLayoutInfo([gd1]).layoutCellBounds(new Dimension(10, 10), parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 37, 30));
        expect(rows[0][1]).toEqual(new Rectangle(37 + 5, 0, 38, 30));
      });

      it('distributes minWidth to spanned cells with one row having w=1', () => {
        // w = 2 on first row, w = 1 on second
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 2, gridh: 1, weightx: 1, minWidth: 80});
        let gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 1, gridh: 1, weightx: 1, minWidth: 80});
        let rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(new Dimension(10, 10), parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 80, 30));
        expect(rows[0][1]).toEqual(new Rectangle(80, 0, 0, 30));
        expect(rows[1][0]).toEqual(new Rectangle(0, 35, 80, 30));

        // w = 1 on first row, w = 2 on second -> should lead to same result
        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weightx: 1, minWidth: 80});
        gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 2, gridh: 1, weightx: 1, minWidth: 80});
        rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(new Dimension(10, 10), parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 80, 30));
        expect(rows[0][1]).toEqual(new Rectangle(80, 0, 0, 30));
        expect(rows[1][0]).toEqual(new Rectangle(0, 35, 80, 30));
      });

      it('distributes minWidth to spanned cells with spans on every row', () => {
        // w = 4 on first row, w = 3 on second
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 4, gridh: 1, weightx: 1, minWidth: 80});
        let gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 3, gridh: 1, weightx: 1, minWidth: 80});
        let rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(new Dimension(10, 10), parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 23, 30));
        expect(rows[0][1]).toEqual(new Rectangle(28, 0, 23, 30));
        expect(rows[0][2]).toEqual(new Rectangle(56, 0, 24, 30));
        expect(rows[0][3]).toEqual(new Rectangle(80, 0, 0, 30));
        expect(rows[1][0]).toEqual(new Rectangle(0, 35, 23, 30));
        expect(rows[1][1]).toEqual(new Rectangle(28, 35, 23, 30));
        expect(rows[1][2]).toEqual(new Rectangle(56, 35, 24, 30));
        expect(rows[1][3]).toEqual(new Rectangle(80, 35, 0, 30));

        // w = 4 on first row, w = 3 on second -> should lead to same result
        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 3, gridh: 1, weightx: 1, minWidth: 80});
        gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 4, gridh: 1, weightx: 1, minWidth: 80});
        rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(new Dimension(10, 10), parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 23, 30));
        expect(rows[0][1]).toEqual(new Rectangle(28, 0, 23, 30));
        expect(rows[0][2]).toEqual(new Rectangle(56, 0, 24, 30));
        expect(rows[0][3]).toEqual(new Rectangle(80, 0, 0, 30));
        expect(rows[1][0]).toEqual(new Rectangle(0, 35, 23, 30));
        expect(rows[1][1]).toEqual(new Rectangle(28, 35, 23, 30));
        expect(rows[1][2]).toEqual(new Rectangle(56, 35, 24, 30));
        expect(rows[1][3]).toEqual(new Rectangle(80, 35, 0, 30));
      });

      it('scales cell not smaller than minWidth even if there is a spanned cell on another row', () => {
        // w = 2 and w = 1 on first row, w = 3 on second
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 2, gridh: 1, weightx: 1, minWidth: 80});
        let gd2 = new LogicalGridData({gridx: 2, gridy: 0, gridw: 1, gridh: 1, weightx: 1, minWidth: 80});
        let gd3 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 3, gridh: 1, weightx: 1, minWidth: 80});
        let rows = newLogicalGridLayoutInfo([gd1, gd2, gd3]).layoutCellBounds(new Dimension(10, 10), parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 37, 30));
        expect(rows[0][1]).toEqual(new Rectangle(42, 0, 38, 30));
        expect(rows[0][2]).toEqual(new Rectangle(85, 0, 80, 30));
        expect(rows[1][0]).toEqual(new Rectangle(0, 35, 37, 30));
        expect(rows[1][1]).toEqual(new Rectangle(42, 35, 38, 30));
        expect(rows[1][2]).toEqual(new Rectangle(85, 35, 80, 30));

        // w = 3 on first row, w = 2 and w = 1 on second -> should lead to same result
        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 3, gridh: 1, weightx: 1, minWidth: 80});
        gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 2, gridh: 1, weightx: 1, minWidth: 80});
        gd3 = new LogicalGridData({gridx: 2, gridy: 1, gridw: 1, gridh: 1, weightx: 1, minWidth: 80});
        rows = newLogicalGridLayoutInfo([gd1, gd2, gd3]).layoutCellBounds(new Dimension(10, 10), parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 37, 30));
        expect(rows[0][1]).toEqual(new Rectangle(42, 0, 38, 30));
        expect(rows[0][2]).toEqual(new Rectangle(85, 0, 80, 30));
        expect(rows[1][0]).toEqual(new Rectangle(0, 35, 37, 30));
        expect(rows[1][1]).toEqual(new Rectangle(42, 35, 38, 30));
        expect(rows[1][2]).toEqual(new Rectangle(85, 35, 80, 30));
      });

      it('scales cell not smaller than minWidth with multiple spans on multiple rows', () => {
        // w = 3 and w = 2 on first row, w = 5 on second
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 3, gridh: 1, weightx: 1, minWidth: 80});
        let gd2 = new LogicalGridData({gridx: 3, gridy: 0, gridw: 2, gridh: 1, weightx: 1, minWidth: 80});
        let gd3 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 5, gridh: 1, weightx: 1, minWidth: 80});
        let rows = newLogicalGridLayoutInfo([gd1, gd2, gd3]).layoutCellBounds(new Dimension(10, 10), parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 23, 30));
        expect(rows[0][1]).toEqual(new Rectangle(28, 0, 23, 30));
        expect(rows[0][2]).toEqual(new Rectangle(56, 0, 24, 30));
        expect(rows[0][3]).toEqual(new Rectangle(85, 0, 37, 30));
        expect(rows[0][4]).toEqual(new Rectangle(127, 0, 38, 30));
        expect(rows[1][0]).toEqual(new Rectangle(0, 35, 23, 30));
        expect(rows[1][1]).toEqual(new Rectangle(28, 35, 23, 30));
        expect(rows[1][2]).toEqual(new Rectangle(56, 35, 24, 30));
        expect(rows[1][3]).toEqual(new Rectangle(85, 35, 37, 30));
        expect(rows[1][4]).toEqual(new Rectangle(127, 35, 38, 30));

        // w = 5 on first row, w = 3 and w = 2 on second -> should lead to same result
        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 5, gridh: 1, weightx: 1, minWidth: 80});
        gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 3, gridh: 1, weightx: 1, minWidth: 80});
        gd3 = new LogicalGridData({gridx: 3, gridy: 1, gridw: 2, gridh: 1, weightx: 1, minWidth: 80});
        rows = newLogicalGridLayoutInfo([gd1, gd2, gd3]).layoutCellBounds(new Dimension(10, 10), parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 23, 30));
        expect(rows[0][1]).toEqual(new Rectangle(28, 0, 23, 30));
        expect(rows[0][2]).toEqual(new Rectangle(56, 0, 24, 30));
        expect(rows[0][3]).toEqual(new Rectangle(85, 0, 37, 30));
        expect(rows[0][4]).toEqual(new Rectangle(127, 0, 38, 30));
        expect(rows[1][0]).toEqual(new Rectangle(0, 35, 23, 30));
        expect(rows[1][1]).toEqual(new Rectangle(28, 35, 23, 30));
        expect(rows[1][2]).toEqual(new Rectangle(56, 35, 24, 30));
        expect(rows[1][3]).toEqual(new Rectangle(85, 35, 37, 30));
        expect(rows[1][4]).toEqual(new Rectangle(127, 35, 38, 30));
      });

      it('scales cell not smaller than minWidth even if on the left and right are used cells', () => {
        // w = 1, w = 2 and w = 1 on first row
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weightx: 1, minWidth: 80});
        let gd2 = new LogicalGridData({gridx: 1, gridy: 0, gridw: 2, gridh: 1, weightx: 1, minWidth: 80});
        let gd3 = new LogicalGridData({gridx: 3, gridy: 0, gridw: 1, gridh: 1, weightx: 1, minWidth: 80});
        let rows = newLogicalGridLayoutInfo([gd1, gd2, gd3]).layoutCellBounds(new Dimension(10, 10), parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 37, 30));
        expect(rows[0][1]).toEqual(new Rectangle(42, 0, 38, 30));
        expect(rows[0][2]).toEqual(new Rectangle(85, 0, 80, 30));
        expect(rows[1][0]).toEqual(new Rectangle(0, 35, 37, 30));
        expect(rows[1][1]).toEqual(new Rectangle(42, 35, 38, 30));
        expect(rows[1][2]).toEqual(new Rectangle(85, 35, 80, 30));

        // w = 2, w = 1 and w = 2 on first row
        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 2, gridh: 1, weightx: 1, minWidth: 80});
        gd2 = new LogicalGridData({gridx: 1, gridy: 0, gridw: 1, gridh: 1, weightx: 1, minWidth: 80});
        gd3 = new LogicalGridData({gridx: 3, gridy: 0, gridw: 2, gridh: 1, weightx: 1, minWidth: 80});
        rows = newLogicalGridLayoutInfo([gd1, gd2, gd3]).layoutCellBounds(new Dimension(10, 10), parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 37, 30));
        expect(rows[0][1]).toEqual(new Rectangle(42, 0, 38, 30));
        expect(rows[0][2]).toEqual(new Rectangle(85, 0, 80, 30));
        expect(rows[1][0]).toEqual(new Rectangle(0, 35, 37, 30));
        expect(rows[1][1]).toEqual(new Rectangle(42, 35, 38, 30));
        expect(rows[1][2]).toEqual(new Rectangle(85, 35, 80, 30));
      });
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
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 37, 30));
        expect(rows[0][1]).toEqual(new Rectangle(37 + 5, 0, 38, 30));
      });

      it('distributes maxWidth to spanned cells with one row having w=1', () => {
        // w = 2 on first row, w = 1 on second
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 2, gridh: 1, weightx: 1, maxWidth: 80});
        let gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 1, gridh: 1, weightx: 1, maxWidth: 80});
        let rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(parentSize, parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 37, 30));
        expect(rows[0][1]).toEqual(new Rectangle(42, 0, 38, 30));
        expect(rows[1][0]).toEqual(new Rectangle(0, 35, 37, 30));

        // w = 1 on first row, w = 2 on second -> should lead to same result
        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weightx: 1, maxWidth: 80});
        gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 2, gridh: 1, weightx: 1, maxWidth: 80});
        rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(parentSize, parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 37, 30));
        expect(rows[0][1]).toEqual(new Rectangle(42, 0, 38, 30));
        expect(rows[1][0]).toEqual(new Rectangle(0, 35, 37, 30));
      });

      it('distributes maxWidth to spanned cells with spans on every row', () => {
        // w = 4 on first row, w = 3 on second
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 4, gridh: 1, weightx: 1, maxWidth: 80});
        let gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 3, gridh: 1, weightx: 1, maxWidth: 80});
        let rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(parentSize, parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 16, 30));
        expect(rows[0][1]).toEqual(new Rectangle(21, 0, 16, 30));
        expect(rows[0][2]).toEqual(new Rectangle(42, 0, 17, 30));
        expect(rows[0][3]).toEqual(new Rectangle(64, 0, 17, 30));
        expect(rows[1][0]).toEqual(new Rectangle(0, 35, 16, 30));
        expect(rows[1][1]).toEqual(new Rectangle(21, 35, 16, 30));
        expect(rows[1][2]).toEqual(new Rectangle(42, 35, 17, 30));
        expect(rows[1][3]).toEqual(new Rectangle(64, 35, 17, 30));

        // w = 4 on first row, w = 3 on second -> should lead to same result
        gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 3, gridh: 1, weightx: 1, maxWidth: 80});
        gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 4, gridh: 1, weightx: 1, maxWidth: 80});
        rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(parentSize, parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 16, 30));
        expect(rows[0][1]).toEqual(new Rectangle(21, 0, 16, 30));
        expect(rows[0][2]).toEqual(new Rectangle(42, 0, 16, 30));
        expect(rows[0][3]).toEqual(new Rectangle(63, 0, 17, 30));
        expect(rows[1][0]).toEqual(new Rectangle(0, 35, 16, 30));
        expect(rows[1][1]).toEqual(new Rectangle(21, 35, 16, 30));
        expect(rows[1][2]).toEqual(new Rectangle(42, 35, 16, 30));
        expect(rows[1][3]).toEqual(new Rectangle(63, 35, 17, 30));
      });
    });

    describe('minHeight', () => {
      it('scales cell not smaller than minHeight', () => {
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weighty: 1, minHeight: 80});
        let gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 1, gridh: 1, weighty: 1}); // minHeight default is 0
        let rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(new Dimension(10, 10), parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 50, 80));
        expect(rows[1][0]).toEqual(new Rectangle(0, 85, 50, 0));
      });

      it('scales cell not smaller than minHeight even if prefSize is smaller', () => {
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weighty: 1, minHeight: 80, heightHint: 50});
        let rows = newLogicalGridLayoutInfo([gd1]).layoutCellBounds(new Dimension(10, 10), parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 50, 80));
      });

      it('distributes minHeight to spanned cells', () => {
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 2, weighty: 1, minHeight: 80});
        let gd2 = new LogicalGridData({gridx: 0, gridy: 1, gridw: 1, gridh: 1, weighty: 1});
        let rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(new Dimension(10, 10), parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 50, 37));
        expect(rows[1][0]).toEqual(new Rectangle(0, 37 + 5, 50, 38));
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
    });
  });
});
