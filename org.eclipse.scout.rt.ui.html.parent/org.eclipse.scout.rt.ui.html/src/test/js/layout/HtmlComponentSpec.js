describe("HtmlComponent", function() {

  var jqueryMock = {
    data:function(htmlComp) {
    }
  };

  var addWidthHeightMock = function(jqueryMock) {
    jqueryMock.width = function(val) {
      if (val !== undefined) {
        return jqueryMock;
      } else {
        return 6;
      }
    };
    jqueryMock.height = function(val) {
      if (val !== undefined) {
        return jqueryMock;
      } else {
        return 7;
      }
    };
  };

  describe("Ctor", function() {

    it("sets data 'htmlComponent' when Ctor is called", function() {
      spyOn(jqueryMock, 'data');
      var htmlComp = new scout.HtmlComponent(jqueryMock);
      expect(jqueryMock.data).toHaveBeenCalledWith('htmlComponent', htmlComp);
    });

  });

  describe("getSize", function() {

    addWidthHeightMock(jqueryMock);

    it("returns width() and height() of JQuery comp", function() {
      var htmlComp = new scout.HtmlComponent(jqueryMock);
      var size = htmlComp.getSize();
      expect(size.width).toBe(6);
      expect(size.height).toBe(7);
    });
  });

  describe("setSize", function() {

    // return size(6, 7)
    addWidthHeightMock(jqueryMock);

    jqueryMock.css = function(key, value) {
      return jqueryMock;
    };

    var htmlComp = new scout.HtmlComponent(jqueryMock);

    htmlComp.layoutManager = {
      invalidate:function() {},
      layout:function() {}
    };

    it("accepts scout.Dimension as single argument", function() {
      spyOn(jqueryMock, 'css').and.callThrough();
      htmlComp.setSize(new scout.Dimension(6, 7));
      var size = htmlComp.getSize();
      expect(size.width).toBe(6);
      expect(size.height).toBe(7);
      expect(jqueryMock.css).toHaveBeenCalledWith('width', '6px');
      expect(jqueryMock.css).toHaveBeenCalledWith('height', '7px');
    });

    it("calls invalidate on layout-manager when size has changed", function() {
      spyOn(htmlComp.layoutManager, 'invalidate');
      htmlComp.setSize(new scout.Dimension(1, 2));
      expect(htmlComp.layoutManager.invalidate).toHaveBeenCalled();
    });

  });

  describe("getInsets", function() {

    it("reads padding, margin and border correctly", function() {
      var jqueryMock = {
        data:function(key, value) {
          // NOP
        },
        css:function(key) {
          var props = {
              'margin-top':'1px',
              'margin-right':'2px',
              'margin-bottom':'3px',
              'margin-left':'4px',
              'padding-top':'5px',
              'padding-right':'6px',
              'padding-bottom':'7px',
              'padding-left':'8px',
              'border-top-width':'9px',
              'border-right-width':'10px',
              'border-bottom-width':'11px',
              'border-left-width':'12px',
          };
          return props[key];
        }
      };

      var htmlComp = new scout.HtmlComponent(jqueryMock);
      var expected = new scout.Insets(15, 18, 21, 24);
      var actual = htmlComp.getInsets();
      expect(actual).toEqual(expected);
    });

  });

  describe("getBounds", function() {

    var jqueryMock = {
      css:function(key) {
        if (key == 'top') {
          return '5px';
        } else if (key == 'left') {
          return '4px';
        } else {
          throw new Error('unexpected CSS key');
        }
      }
    };

    addWidthHeightMock(jqueryMock);

    it("returns bounds without 'px'", function() {
      var actual = scout.HtmlComponent.getBounds(jqueryMock);
      var expected = new scout.Rectangle(4, 5, 6, 7);
      expect(actual).toEqual(expected);
    });

  });

});
