describe('Action', function() {
  var $sandbox, session, action;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = $('#sandbox');
    action = new scout.Action();
    action.init(createSimpleModel('Action', session));
  });

  describe('defaults', function() {

    it('should be as expected', function() {
      expect(action.tabbable).toBe(false);
      expect(action.actionStyle).toBe(scout.Action.ActionStyle.DEFAULT);
    });

  });

  describe('setTabbable', function() {

    it('should modify $container tabindex', function() {
      // because Action is 'abstract' and has no _render method yet
      // but _renderProperties() is called anyway
      action.$container = $sandbox;
      action.render($sandbox);
      expect(action.$container.attr('tabindex')).toBe(undefined);

      action.setTabbable(true);
      expect(action.$container.attr('tabindex')).toBe('0');
    });

  });


});
