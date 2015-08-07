describe('localObjects', function() {

  function createSession(userAgent) {
    setFixtures(sandbox());
    return sandboxSession();
  }

  var session;

  beforeEach(function() {
    session = createSession();
  });

  describe('createObject', function() {

    it('sets property \'id\' correctly when no ID is provided', function() {
      var expectedSeqNo = scout._uniqueIdSeqNo + 1,
        menu = scout.localObjects.createObject(session, {objectType: 'Menu'});
      expect(menu.id).toBe('ui' + expectedSeqNo.toString());
      expect(scout._uniqueIdSeqNo).toBe(expectedSeqNo);
    });

    it('must create objects with model-object argument and nothing else', function() {
      // must fail
      expect(function() {
        scout.localObjects.createObject(session, 'Menu');
      }).toThrow();
      expect(function() {
        scout.localObjects.createObject(session);
      }).toThrow();
      expect(function() {
        scout.localObjects.createObject();
      }).toThrow();
      expect(function() {
        scout.localObjects.createObject(session, true);
      }).toThrow();

      // must not fail
      var menu = scout.localObjects.createObject(session, {objectType: 'Menu'});
      expect(menu instanceof scout.Menu).toBe(true);
    });

    it('session must be set, but adapter should not be registered', function() {
      var oldNumProperties = scout.objects.countProperties(session.modelAdapterRegistry),
        menu = scout.localObjects.createObject(session, {objectType: 'Menu'});
      expect(menu.session === session).toBe(true);
      expect(menu._register).toBe(false);
      expect(scout.objects.countProperties(session.modelAdapterRegistry)).toBe(oldNumProperties);
    });

  });

});
