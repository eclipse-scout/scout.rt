describe('OfflineSession', function() {

  function createSession(userAgent) {
    setFixtures(sandbox());
    return sandboxSession({'userAgent':userAgent});
  }

  var liveSession, offlineSession;

  beforeEach(function() {
    liveSession = createSession();
  });

  describe('createFromSession', function() {

    it('copies some properties from the real, live session', function() {
      var offlineSession = scout.OfflineSession.createFromSession(liveSession);
      ['uiSessionId', '$entryPoint', 'objectFactory', 'userAgent'].forEach(function(propertyName) {
        expect(offlineSession[propertyName]).toBe(liveSession[propertyName]);
      });
    });

  });

  describe('createUiObject', function() {

    it('sets property \'id\' correctly, session must be set to OfflineSession', function() {
      var offlineSession = scout.OfflineSession.createFromSession(liveSession);
      var expectedSeqNo = scout._uniqueIdSeqNo + 1,
        menu = offlineSession.createUiObject({objectType: 'Menu'});
      expect(menu.id).toBe('ui' + expectedSeqNo.toString());
      expect(scout._uniqueIdSeqNo).toBe(expectedSeqNo);
      expect(menu.session).toBe(offlineSession);
    });

  });

});
