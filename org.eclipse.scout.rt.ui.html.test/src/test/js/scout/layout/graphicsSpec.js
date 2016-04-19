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
describe("scout.Dimension", function() {

  beforeEach(function() {
    setFixtures(sandbox());
  });

  describe("Ctor", function() {

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

  });

  describe("getBounds", function() {

    var $div = $('<div>')
      .css('left', '6px')
      .css('top', '7px')
      .css('width', '8px')
      .css('height', '9px')
      .css('margin', '10px');

    it("returns JQuery.outerWidth/Height(true)", function() {
      var rect = scout.graphics.getBounds($div);
      expect(rect.x).toBe(6);
      expect(rect.y).toBe(7);
      expect(rect.width).toBe(28);
      expect(rect.height).toBe(29);
    });

    it("returns 0 when left/right is set to auto", function() {
      $div
        .css('left', 'auto')
        .css('top', 'auto');
      var rect = scout.graphics.getBounds($div);
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


    it("returns rectangle with position from JQuery.position() including margin if includePosMargin is true", function() {
      var rect = scout.graphics.bounds($div, false, true);
      expect(rect.x).toBe(6 + 10);
      expect(rect.y).toBe(7 + 10);
    });

    it("returns rectangle with size from JQuery.outerWidth/Height", function() {
      var rect = scout.graphics.bounds($div);
      expect(rect.width).toBe(8);
      expect(rect.height).toBe(9);
    });

    it("returns rectangle with size from  JQuery.outerWidth/Height() including margin if includeSizeMargin is true", function() {
      var rect = scout.graphics.bounds($div, true);
      expect(rect.width).toBe(8 + 2 * 10);
      expect(rect.height).toBe(9 + 2 * 10);
    });

  });

});
