describe('LocalSession', function() {

  function createSession(userAgent) {
    setFixtures(sandbox());
    return sandboxSession({'userAgent':userAgent});
  }

  var liveSession, localSession;

  beforeEach(function() {
    liveSession = createSession();
  });

  describe('createFromSession', function() {

    it('copies some properties from the real, live session', function() {
      var localSession = scout.LocalSession.createFromSession(liveSession);
      ['uiSessionId', '$entryPoint', 'objectFactory', 'userAgent'].forEach(function(propertyName) {
        expect(localSession[propertyName]).toBe(liveSession[propertyName]);
      });
    });

  });

  describe('createUiObject', function() {

    it('sets property \'id\' correctly, session must be set to LocalSession', function() {
      var localSession = scout.LocalSession.createFromSession(liveSession);
      var expectedSeqNo = scout._uniqueIdSeqNo + 1,
        menu = localSession.createUiObject({objectType: 'Menu'});
      expect(menu.id).toBe('ui' + expectedSeqNo.toString());
      expect(scout._uniqueIdSeqNo).toBe(expectedSeqNo);
      expect(menu.session).toBe(localSession);
    });

  });

});
