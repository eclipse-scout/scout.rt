/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/* This test also exists as Java code, to make sure Java and JS code produces the same results */
describe("LogicalGridLayoutInfo", function() {

  describe("Rectangle.union", function() {

    var r1 = new scout.Rectangle(0, 0, 675, 558);
    var r2 = new scout.Rectangle(687, 0, 674, 558);

    it("produces same results as java.awt.Rectangle", function() {
      var r = r1.union(r2);
      var expected = new scout.Rectangle(0, 0, 1361, 558);
      expect(expected.equals(r)).toBe(true);
    });

  });

  describe("layoutCellBounds", function() {
    // Create some mock-objects for JQuery selector- and HtmlComponent instances.
    var mockJquery = function(compName) {
      var jquery = this;
      return {
        data:function(dataKey) {
          if ('htmlComponent' === dataKey) {
            return mockHtmlComp(jquery);
          }
        },
        attr:function(attrKey) {
          return attrKey === 'id' ? compName : undefined;
        }
      };
    };

    function mockHtmlComp(jquery) {
      return {
        prefSize:function() {
          return new scout.Dimension(1, 1);
        }
      };
    }

    var components = [
      mockJquery('DateField'),
      mockJquery('StringField')
    ];

    var gd1 = new scout.LogicalGridData();
    gd1.gridx = 0;
    gd1.gridy = 0;
    gd1.gridw = 1;
    gd1.gridh = 1;
    gd1.weightx = 0.0;
    gd1.widthHint = 70;

    var gd2 = new scout.LogicalGridData();
    gd2.gridx = 1;
    gd2.gridy = 0;
    gd2.gridw = 1;
    gd2.gridh = 1;
    gd2.weightx = 1.0;

    var cons = [gd1, gd2];
    var lgli = new scout.LogicalGridLayoutInfo(components, cons, 5, 5);
    var parentSize = new scout.Dimension(500, 23);
    var parentInsets = new scout.Insets(0, 0, 0, 0);

    it("calculates bounds", function() {
      lgli.layoutCellBounds(parentSize, parentInsets);

      var rows = lgli.layoutCellBounds(parentSize, parentInsets);
      expect(rows.length).toBe(1);
      var cells = rows[0];
      expect(cells.length).toBe(2);

      var cell = cells[0];
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
