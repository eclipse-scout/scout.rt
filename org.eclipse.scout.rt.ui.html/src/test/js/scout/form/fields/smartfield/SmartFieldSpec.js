describe('SmartField', function() {

  var smartField;

  beforeEach(function() {
    smartField = new scout.SmartField();
    smartField.$field = $('<input>');
    smartField.session = {
      send: function() {
      },
      listen: function() {
        return {
          done: function(func) {
          }
        };
      }
    };
  });

  describe('_onKeyUp', function() {

    it('doesn not call _openProposal() when TAB has been pressed', function() {
      smartField._openProposal = function(searchText, selectCurrentValue) {};
      var event = {
        which: scout.keys.TAB
      };
      spyOn(smartField, '_openProposal');
      smartField._onKeyUp(event);
      expect(smartField._openProposal).not.toHaveBeenCalled();
    });

    it('calls _openProposal() when a character key has been pressed', function() {
      smartField._browseOnce = true;
      smartField._popup = {};
      smartField._openProposal = function(searchText, selectCurrentValue) {};
      var event = {
        which: scout.keys.A
      };
      spyOn(smartField, '_openProposal').and.callThrough();
      smartField._onKeyUp(event);
      expect(smartField._openProposal).toHaveBeenCalled();
    });

  });

  describe('_acceptProposal', function() {

    it ('must set displayText', function() {
      smartField.$field.val('foo');
      smartField._acceptProposal();
      expect(smartField.displayText).toBe('foo');
    });

    it ('must call clearTimeout() for pending typedProposal events', function() {
      smartField._sendTimeoutId = null;
      smartField.$field.val('bar');
      smartField._proposalTyped();
      expect(smartField._sendTimeoutId).toBeTruthy();
      smartField._acceptProposal();
      expect(smartField._sendTimeoutId).toBe(null);
    });

    it ('dont send _acceptProposal when searchText has not changed', function() {
      smartField._oldSearchText = 'foo';
      smartField.$field.val('foo');
      spyOn(smartField.session, 'send');
      smartField._acceptProposal();
      expect(smartField.session.send).not.toHaveBeenCalled();
    });

    it ('send _acceptProposal when searchText has changed', function() {
      smartField._oldSearchText = 'foo';
      smartField.$field.val('bar');
      spyOn(smartField.session, 'send');
      smartField._acceptProposal();
      expect(smartField.session.send).toHaveBeenCalled();
    });

  });

});
