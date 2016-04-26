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
describe("HtmlComponent", function() {
  setFixtures(sandbox());
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  var jqueryMock = {
    data: function(htmlComp) {}
  };

  var LayoutMock = function() {
    LayoutMock.parent.call(this);
  };
  scout.inherits(LayoutMock, scout.AbstractLayout);
  LayoutMock.prototype.layout = function() {};

  var addWidthHeightMock = function(jqueryMock) {
    jqueryMock.width = function(val) {
      if (val !== undefined) {
        return jqueryMock;
      }
    };
    jqueryMock.height = function(val) {
      if (val !== undefined) {
        return jqueryMock;
      }
    };
    jqueryMock.outerWidth = function(withMargins) {
      return 6;
    };
    jqueryMock.outerHeight = function(withMargins) {
      return 7;
    };
  };

  describe("Ctor", function() {

    it("sets data 'htmlComponent' when Ctor is called", function() {
      spyOn(jqueryMock, 'data');
      var htmlComp = new scout.HtmlComponent(jqueryMock, session);
      expect(jqueryMock.data).toHaveBeenCalledWith('htmlComponent', htmlComp);
    });

  });

  describe("getSize", function() {

    addWidthHeightMock(jqueryMock);

    it("returns outerWidth() and outerHeight() of JQuery comp", function() {
      var htmlComp = new scout.HtmlComponent(jqueryMock, session);
      var size = htmlComp.getSize();
      expect(size.width).toBe(6);
      expect(size.height).toBe(7);
    });
  });

  describe("setSize", function() {
    var $comp;
    var htmlComp;

    // return size(6, 7)
    addWidthHeightMock(jqueryMock);

    beforeEach(function() {
      $comp = $('<div>').appendTo(session.$entryPoint);
      htmlComp = new scout.HtmlComponent($comp, session);
      htmlComp.layout = new LayoutMock();
    });

    it("accepts scout.Dimension as single argument", function() {
      spyOn($comp, 'css').and.callThrough();
      htmlComp.setSize(new scout.Dimension(6, 7));
      var size = htmlComp.getSize();
      expect(size.width).toBe(6);
      expect(size.height).toBe(7);
      expect($comp.css).toHaveBeenCalledWith('width', '6px');
      expect($comp.css).toHaveBeenCalledWith('height', '7px');
    });

    it("calls invalidate on layout when size has changed", function() {
      spyOn(htmlComp.layout, 'invalidate');
      htmlComp.setSize(new scout.Dimension(1, 2));
      expect(htmlComp.layout.invalidate).toHaveBeenCalled();
    });

  });

  describe("getInsets", function() {

    it("reads padding, margin and border correctly", function() {
      var jqueryObj = $('<div>').css({
        marginTop: '1px',
        marginRight: '2px',
        marginBottom: '3px',
        marginLeft: '4px',
        paddingTop: '5px',
        paddingRight: '6px',
        paddingBottom: '7px',
        paddingLeft: '8px',
        borderStyle: 'solid',
        borderTopWidth: '9px',
        borderRightWidth: '10px',
        borderBottomWidth: '11px',
        borderLeftWidth: '12px'
      });
      var htmlComp = new scout.HtmlComponent(jqueryObj, session);
      var expected = new scout.Insets(15, 18, 21, 24);
      var actual = htmlComp.getInsets({
        includeMargin: true
      });
      expect(actual).toEqual(expected);
    });

  });

  describe("getBounds", function() {

    var jqueryMock = {
      css: function(key) {
        if (key === 'top') {
          return '5px';
        } else if (key === 'left') {
          return '4px';
        } else {
          throw new Error('unexpected CSS key');
        }
      }
    };

    addWidthHeightMock(jqueryMock);

    it("returns bounds without 'px'", function() {
      spyOn(jqueryMock, 'outerWidth').and.callThrough();
      spyOn(jqueryMock, 'outerHeight').and.callThrough();
      var actual = scout.graphics.getBounds(jqueryMock);
      var expected = new scout.Rectangle(4, 5, 6, 7);
      expect(actual).toEqual(expected);
      expect(jqueryMock.outerWidth).toHaveBeenCalledWith(true);
      expect(jqueryMock.outerHeight).toHaveBeenCalledWith(true);
    });

  });

});
