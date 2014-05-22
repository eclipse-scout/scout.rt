describe("DesktopTree", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = new scout.Session($('#sandbox'), '1.1');
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createModel(id) {
    return {
      "id": id
    };
  }

  function createModelNode(id, text) {
    return {
      "id": id,
      "text": text
    };
  }

  function createDesktopTree(model) {
    var tree = new scout.DesktopTree(model, session);
    tree.attach(session.$entryPoint);
    return tree;
  }

  describe("constructor", function() {
    it("adds nodes", function() {

      var model = createModel();
      model.nodes = [createModelNode(1)];
      var desktopTree = createDesktopTree(model);

      expect(desktopTree._$desktopTreeScroll.children().length).toBe(1);
    });
  });

  describe("_onNodeClicked", function() {

    it("reacts on node clicks", function() {
      var model = createModel();
      model.nodes = [createModelNode(1)];
      var desktopTree = createDesktopTree(model);

      spyOn(desktopTree, '_onNodeClicked');
      var $node = desktopTree._$desktopTreeScroll.children().first();
      $node.click();

      expect(desktopTree._onNodeClicked).toHaveBeenCalled();
    });

    it("sends click, selection and expansion events in one call in this order", function() {
      var model = createModel();
      model.nodes = [createModelNode(1)];
      var desktopTree = createDesktopTree(model);

      var $node = desktopTree._$desktopTreeScroll.children().first();
      $node.click();

      jasmine.clock().tick(0);

      expect(jasmine.Ajax.requests.count()).toBe(1);

      var requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly(['nodeClicked', 'nodesSelected', 'nodeExpanded']);
    });
  });

});
