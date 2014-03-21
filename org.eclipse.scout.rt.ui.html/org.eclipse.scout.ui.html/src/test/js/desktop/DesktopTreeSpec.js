describe("DesktopTree", function() {
  var $parent;
  var scout;

  beforeEach(function() {
    setFixtures(sandbox());
    scout = new Scout.Session($('#sandbox'), '1.1');
    jasmine.Ajax.installMock();
    jasmine.clock().install();
  });

  afterEach(function() {
    scout = null;
    jasmine.Ajax.uninstallMock();
    clearAjaxRequests();
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
    return new Scout.DesktopTree(scout, scout.$entryPoint, model);
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

      expect(ajaxRequests.length).toBe(1);

      var requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly(['nodeClicked', 'nodesSelected', 'nodeExpanded']);
    });
  });

});
