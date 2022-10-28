/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Dimension, Insets, LogicalGridData, LogicalGridLayoutInfo, Rectangle} from '../../../src/index';

/* This test also exists as Java code, to make sure Java and JS code produces the same results */
describe('LogicalGridLayoutInfo', () => {

  describe('Rectangle.union', () => {

    let r1 = new Rectangle(0, 0, 675, 558);
    let r2 = new Rectangle(687, 0, 674, 558);

    it('produces same results as java.awt.Rectangle', () => {
      let r = r1.union(r2);
      let expected = new Rectangle(0, 0, 1361, 558);
      expect(expected.equals(r)).toBe(true);
    });

  });

  describe('layoutCellBounds', () => {
    // Create some mock-objects for JQuery selector- and HtmlComponent instances.
    let mockJquery = function(compName) {
      let jquery = this;
      return {
        data: dataKey => {
          if ('htmlComponent' === dataKey) {
            return mockHtmlComp(jquery);
          }
        },
        attr: attrKey => attrKey === 'id' ? compName : undefined
      };
    };

    function mockHtmlComp(jquery) {
      return {
        prefSize: () => new Dimension(1, 1)
      };
    }

    let components = [
      mockJquery('DateField'),
      mockJquery('StringField')
    ] as JQuery[];

    let gd1 = new LogicalGridData();
    gd1.gridx = 0;
    gd1.gridy = 0;
    gd1.gridw = 1;
    gd1.gridh = 1;
    gd1.weightx = 0.0;
    gd1.widthHint = 70;

    let gd2 = new LogicalGridData();
    gd2.gridx = 1;
    gd2.gridy = 0;
    gd2.gridw = 1;
    gd2.gridh = 1;
    gd2.weightx = 1.0;

    let cons = [gd1, gd2];
    let lgli = new LogicalGridLayoutInfo({
      $components: components,
      cons: cons,
      hgap: 5,
      vgap: 5,
      rowHeight: 30
    });
    let parentSize = new Dimension(500, 23);
    let parentInsets = new Insets(0, 0, 0, 0);

    it('calculates bounds', () => {
      lgli.layoutCellBounds(parentSize, parentInsets);

      let rows = lgli.layoutCellBounds(parentSize, parentInsets);
      expect(rows.length).toBe(1);
      let cells = rows[0];
      expect(cells.length).toBe(2);

      let cell = cells[0];
      expect(cell.x).toBe(0);
      expect(cell.y).toBe(0);
      expect(cell.width).toBe(70);
      expect(cell.height).toBe(30);

      cell = cells[1];
      expect(cell.x).toBe(75);
      expect(cell.y).toBe(0);
      expect(cell.width).toBe(425);
      expect(cell.height).toBe(30);
    });

  });

});
