describe("AbstractSmartField", function() {

  var smartField = new scout.AbstractSmartField();

  function create$Option() {
    return $('<p>').text('FooBar');
  }

  beforeEach(function() {
    var $scrollBar = $('<div>').addClass('scrollbar');
    smartField._$optionsDiv = $('<div>')
      .append($scrollBar)
      .append(create$Option())
      .append(create$Option());
  });

  describe("_emptyOptions", function() {

    it("must remove all P elements (=options), but not the scrollbar DIV", function() {
      smartField._emptyOptions();
      expect(smartField._$optionsDiv.children().length).toBe(1);
      expect(smartField._$optionsDiv.eq(0).is('div')).toBe(true);
    });

  });

  describe("_get$Options", function() {

    it("must return all P elements (=options), but not the scrollbar DIV", function() {
      var $options = smartField._get$Options();
      expect($options.length).toBe(2);
      $options.each(function() {
        expect($(this).is('p')).toBe(true);
      });
    });

  });

  describe("_onKeyUp", function() {

    it("doesn't call _openPopup() when TAB has been pressed", function() {
      smartField._openPopup = function() {};
      var event = {
        which: scout.keys.TAB
      };
      spyOn(smartField, '_openPopup');
      smartField._onKeyUp(event);
      expect(smartField._openPopup).not.toHaveBeenCalled();
    });

    it("calls _openPopup() when a character key has been pressed", function() {
      smartField._openPopup = function() { return true; };
      var event = {
        which: scout.keys.A
      };
      spyOn(smartField, '_openPopup').and.callThrough();
      smartField._onKeyUp(event);
      expect(smartField._openPopup).toHaveBeenCalled();
    });

  });

});
