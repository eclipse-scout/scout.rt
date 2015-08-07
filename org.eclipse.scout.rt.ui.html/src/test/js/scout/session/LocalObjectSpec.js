describe('LocalObject', function() {

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
        menu = scout.LocalObject.createObject(session, 'Menu');
      expect(menu.id).toBe('ui' + expectedSeqNo.toString());
      expect(scout._uniqueIdSeqNo).toBe(expectedSeqNo);
    });

    it('must create objects with plain string and model-object arguments', function() {
      var menu = scout.LocalObject.createObject(session, 'Menu');
      expect(menu instanceof scout.Menu).toBe(true);
      menu = scout.LocalObject.createObject(session, {objectType: 'Menu'});
      expect(menu instanceof scout.Menu).toBe(true);
    });

    it('session must be set, but adapter should not be registered', function() {
      var oldNumProperties = scout.objects.countProperties(session.modelAdapterRegistry),
        menu = scout.LocalObject.createObject(session, 'Menu');
      expect(menu.session === session).toBe(true);
      expect(menu._register).toBe(false);
      expect(scout.objects.countProperties(session.modelAdapterRegistry)).toBe(oldNumProperties);
    });

  });

});
