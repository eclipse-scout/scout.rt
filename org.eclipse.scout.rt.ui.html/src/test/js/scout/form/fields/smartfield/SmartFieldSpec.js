describe("SmartField", function() {

  var smartField = new scout.SmartField();

  describe("_onKeyUp", function() {

    it("doesn't call _openProposal() when TAB has been pressed", function() {
      smartField._openProposal = function(searchText, selectCurrentValue) {};
      var event = {
        which: scout.keys.TAB
      };
      spyOn(smartField, '_openProposal');
      smartField._onKeyUp(event);
      expect(smartField._openProposal).not.toHaveBeenCalled();
    });

    it("calls _openProposal() when a character key has been pressed", function() {
      smartField._browseOnce = true;
      smartField._popup = {};
      smartField._openProposal = function(searchText, selectCurrentValue) {};
      smartField.$field = $('<input>');
      var event = {
        which: scout.keys.A
      };
      spyOn(smartField, '_openProposal').and.callThrough();
      smartField._onKeyUp(event);
      expect(smartField._openProposal).toHaveBeenCalled();
    });

  });

});
