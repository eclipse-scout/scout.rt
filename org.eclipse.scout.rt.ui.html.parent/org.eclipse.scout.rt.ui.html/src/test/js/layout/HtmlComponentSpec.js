describe("HtmlComponent", function() {

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

});
