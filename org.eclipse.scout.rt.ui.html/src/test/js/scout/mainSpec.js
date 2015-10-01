describe('main', function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('create', function() {

    it('accepts string or functions as first argument', function() {
      // must fail
      expect(function() {
        scout.create(1);
      }).toThrow();
      expect(function() {
        scout.create();
      }).toThrow();
      expect(function() {
        scout.create(true);
      }).toThrow();

      // must not fail
      var menu = scout.create('Menu', {
        parent: new scout.NullWidget(),
        session: session
      });
      expect(menu instanceof scout.Menu).toBe(true);
    });

    it('creates a new widget if the first parameter is a constructor function', function() {
      var parent = new scout.NullWidget();
      var widget = scout.create(scout.Tooltip, {
        parent: parent,
        session: session
      });
      expect(widget).toBeTruthy();
      expect(widget instanceof scout.Tooltip).toBe(true);
      expect(widget.parent).toBe(parent);
      expect(widget.session).toBe(session);
    });

    describe('creates local model adapter if first parameter is the objectType', function() {

      it('sets property \'id\' correctly when no ID is provided', function() {
        var expectedSeqNo = scout._uniqueIdSeqNo + 1,
          menu = scout.create('Menu', {
            parent: new scout.NullWidget(),
            session: session
          });
        expect(menu.id).toBe('ui' + expectedSeqNo.toString());
        expect(scout._uniqueIdSeqNo).toBe(expectedSeqNo);
      });

      it('session must be set, but adapter should not be registered', function() {
        var oldNumProperties = scout.objects.countProperties(session.modelAdapterRegistry),
          menu = scout.create('Menu', {
            parent: new scout.NullWidget(),
            session: session
          });
        expect(menu.session === session).toBe(true);
        expect(menu._register).toBe(false);
        expect(scout.objects.countProperties(session.modelAdapterRegistry)).toBe(oldNumProperties);
      });

    });

  });

});
