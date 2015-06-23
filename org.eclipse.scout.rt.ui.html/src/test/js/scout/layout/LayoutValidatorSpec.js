describe("LayoutValidator", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe("invalidateTree", function() {

    it("keeps track of invalid html components", function() {
      var $comp = $('<div>').appendTo(session.$entryPoint);
      var htmlComp = new scout.HtmlComponent($comp, session);

      htmlComp.invalidateLayoutTree();
      expect(session.layoutValidator._invalidComponents.length).toBe(1);
      expect(session.layoutValidator._invalidComponents[0]).toBe(htmlComp);
    });

    it("considers only the topmost component", function() {
      var $comp = $('<div>').appendTo(session.$entryPoint);
      var htmlComp = new scout.HtmlComponent($comp, session);

      var $compChild = $('<div>').appendTo($comp);
      var htmlCompChild = new scout.HtmlComponent($compChild, session);

      htmlCompChild.invalidateLayoutTree();
      expect(session.layoutValidator._invalidComponents.length).toBe(1);
      expect(session.layoutValidator._invalidComponents[0]).toBe(htmlComp);
    });

    it("and validate roots", function() {
      var $comp = $('<div>').appendTo(session.$entryPoint);
      new scout.HtmlComponent($comp, session);

      var $compChild = $('<div>').appendTo($comp);
      var htmlCompChild = new scout.HtmlComponent($compChild, session);
      htmlCompChild.validateRoot = true;

      htmlCompChild.invalidateLayoutTree();
      expect(session.layoutValidator._invalidComponents.length).toBe(1);
      expect(session.layoutValidator._invalidComponents[0]).toBe(htmlCompChild);
    });
  });

  describe("layout", function() {

    it("calls validateLayout for each invalid html component", function() {
      var $comp = $('<div>').appendTo(session.$entryPoint);
      var htmlComp = new scout.HtmlComponent($comp, session);
      spyOn(htmlComp, 'validateLayout');

      htmlComp.invalidateLayoutTree();
      session.layoutValidator.validate();
      expect(htmlComp.validateLayout).toHaveBeenCalled();
      expect(htmlComp.validateLayout.calls.count()).toEqual(1);
    });

    it("does not call validateLayout if component has been removed", function() {
      var $comp = $('<div>').appendTo(session.$entryPoint);
      var htmlComp = new scout.HtmlComponent($comp, session);
      spyOn(htmlComp, 'validateLayout');

      htmlComp.invalidateLayoutTree();
      $comp.remove();
      session.layoutValidator.validate();
      expect(htmlComp.validateLayout).not.toHaveBeenCalled();
    });

  });

});
