describe('Desktop', function() {

  var session, desktop = new scout.Desktop();

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    desktop.viewButtons = [];
    desktop.session = session;
  });

  describe('_addNullOutline', function() {

    it('should add null-outline when outline of model doesn\'t exist', function() {
      var ovb, outline = null;
      desktop._addNullOutline(outline);
      expect(desktop.viewButtons.length).toBe(1);
      ovb = desktop.viewButtons[0];
      expect(desktop.outline).toBe(ovb.outline);
      expect(ovb.visibleInMenu).toBe(false);
    });

    it('shouldn\'t do anything when model already has an outline', function() {
      var outline = {};
      desktop.outline = outline;
      desktop._addNullOutline(outline);
      expect(desktop.outline).toBe(outline);
      expect(desktop.viewButtons.length).toBe(0);
    });

  });

});
