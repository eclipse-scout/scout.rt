/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("graphics", function() {

  beforeEach(function() {
    setFixtures(sandbox());
  });

  describe("cssBounds", function() {

    var $div = $('<div>')
      .css('left', '6px')
      .css('top', '7px')
      .css('width', '8px')
      .css('height', '9px')
      .css('margin', '10px');

    it("returns width, height, x and y", function() {
      var rect = scout.graphics.cssBounds($div);
      expect(rect.x).toBe(6);
      expect(rect.y).toBe(7);
      expect(rect.width).toBe(8);
      expect(rect.height).toBe(9);
    });

    it("returns width+margin, height+margin, x and y with includeMargin=true", function() {
      var rect = scout.graphics.cssBounds($div, {
        includeMargin: true
      });
      expect(rect.x).toBe(6);
      expect(rect.y).toBe(7);
      expect(rect.width).toBe(28);
      expect(rect.height).toBe(29);
    });

    it("returns 0 when left/right is set to auto", function() {
      $div
        .css('left', 'auto')
        .css('top', 'auto');
      var rect = scout.graphics.cssBounds($div);
      expect(rect.x).toBe(0);
      expect(rect.y).toBe(0);
    });

  });

  describe("bounds", function() {
    var $div;

    beforeEach(function() {
      $div = $('<div>')
        .css('position', 'absolute')
        .css('left', '6px')
        .css('top', '7px')
        .css('width', '8px')
        .css('height', '9px')
        .css('margin', '10px')
        .appendTo($('#sandbox'));
    });

    it("returns rectangle with position from JQuery.position()", function() {
      var rect = scout.graphics.bounds($div);
      expect(rect.x).toBe(6);
      expect(rect.y).toBe(7);
    });

    it("returns rectangle with size from JQuery.outerWidth/Height", function() {
      var rect = scout.graphics.bounds($div);
      expect(rect.width).toBe(8);
      expect(rect.height).toBe(9);
    });

    it("returns rectangle with size from  JQuery.outerWidth/Height() including margin if includeMargin is true", function() {
      var rect = scout.graphics.bounds($div, {
        includeMargin: true
      });
      expect(rect.width).toBe(8 + 2 * 10);
      expect(rect.height).toBe(9 + 2 * 10);
    });

  });

  describe("Point", function() {

    it("equals", function() {
      var p1 = new scout.Point(10, 5);
      var p2 = new scout.Point(20, -1);
      var p3 = new scout.Point(20, -1);
      var p4 = new scout.Point(10.2, 5.9);

      expect(p1.equals(p2)).toBe(false);
      expect(p2.equals(p3)).toBe(true);
      expect(p1.equals(p4)).toBe(false);
      expect(p1.equals(p4.floor())).toBe(true);
      expect(p1.equals(p4.ceil())).toBe(false);
    });

  });

  describe("Dimension", function() {

    it("accepts two numbers as width and height arguments", function() {
      var dim = new scout.Dimension(6, 7);
      expect(dim.width).toBe(6);
      expect(dim.height).toBe(7);
    });

    it("accepts a single scout.Dimension argument", function() {
      var dim1 = new scout.Dimension(6, 7);
      var dim2 = new scout.Dimension(dim1);
      expect(dim2.width).toBe(6);
      expect(dim2.height).toBe(7);
      expect(dim1).toEqual(dim2);
    });

    it("equals", function() {
      var d1 = new scout.Dimension(10, 5);
      var d2 = new scout.Dimension(20, 20);
      var d3 = new scout.Dimension(d2);
      var d4 = new scout.Dimension(10.2, 5.9);

      expect(d1.equals(d2)).toBe(false);
      expect(d2.equals(d3)).toBe(true);
      expect(d1.equals(d4)).toBe(false);
      expect(d1.equals(d4.floor())).toBe(true);
      expect(d1.equals(d4.ceil())).toBe(false);
    });

  });

  describe("Rectangle", function() {

    it("equals", function() {
      var r1 = new scout.Rectangle(0, 0, 10, 5);
      var r2 = new scout.Rectangle(0, 0, 20, -1);
      var r3 = new scout.Rectangle(0, 0, 20, -1);
      var r4 = new scout.Rectangle(0.5, 0.1, 10.2, 5.9);
      var r5 = new scout.Rectangle(14, 15, 10, 5);
      var r6 = new scout.Rectangle(14, 15, 20, -1);

      expect(r1.equals(r2)).toBe(false);
      expect(r2.equals(r3)).toBe(true);
      expect(r1.equals(r4)).toBe(false);
      expect(r1.equals(r4.floor())).toBe(true);
      expect(r1.equals(r4.ceil())).toBe(false);
      expect(r1.equals(r5)).toBe(false);
      expect(r2.equals(r6)).toBe(false);
      expect(r5.equals(r6)).toBe(false);
    });

  });

  describe("Insets", function() {

    it("equals", function() {
      var i1 = new scout.Insets(0, 0, 10, 5);
      var i2 = new scout.Insets(0, 0, 20, -1);
      var i3 = new scout.Insets(0, 0, 20, -1);
      var i4 = new scout.Insets(0.5, 0.1, 10.2, 5.9);
      var i5 = new scout.Insets(14, 15, 10, 5);
      var i6 = new scout.Insets(14, 15, 20, -1);

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

});
