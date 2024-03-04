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
        expect(rows[0][1]).toEqual(new Rectangle(85, 0, 0, 30));
      });

      it('scales cell not smaller than minWidth even if prefSize is smaller', () => {
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weightx: 1, minWidth: 80, widthHint: 50});
        let rows = newLogicalGridLayoutInfo([gd1]).layoutCellBounds(new Dimension(10, 10), parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 80, 30));
      });

      it('distributes minWidth to spanned cells', () => {
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 2, gridh: 1, weightx: 1, minWidth: 80});
        let gd2 = new LogicalGridData({gridx: 1, gridy: 0, gridw: 1, gridh: 1, weightx: 1});
        let rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(new Dimension(10, 10), parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 37, 30));
        expect(rows[0][1]).toEqual(new Rectangle(37 + 5, 0, 38, 30));

        // TODO CGU add test for multiple rows
        // TODO CGU add test for weightx = 0
        // TODO CGU add gridData to jswidgets demo and to GridData.java
      });
    });

    describe('maxWidth', () => {
      it('scales cell not bigger than maxWidth', () => {
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weightx: 1, maxWidth: 80});
        let gd2 = new LogicalGridData({gridx: 1, gridy: 0, gridw: 1, gridh: 1, weightx: 1}); // minWidth default is 0
        let rows = newLogicalGridLayoutInfo([gd1, gd2]).layoutCellBounds(parentSize, parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 80, 30));
        expect(rows[0][1]).toEqual(new Rectangle(85, 0, parentSize.width - 85, 30));
      });

      it('scales cell not bigger than maxWidth even if prefSize is bigger', () => {
        let gd1 = new LogicalGridData({gridx: 0, gridy: 0, gridw: 1, gridh: 1, weightx: 1, minWidth: 80, widthHint: 100});
        let rows = newLogicalGridLayoutInfo([gd1]).layoutCellBounds(new Dimension(10, 10), parentInsets);
        expect(rows[0][0]).toEqual(new Rectangle(0, 0, 80, 30));
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
