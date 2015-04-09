describe("AbstractSmartField", function() {

  var smartField = new scout.AbstractSmartField();

  function create$Option() {
    return $('<p>').text('FooBar');
  }

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
