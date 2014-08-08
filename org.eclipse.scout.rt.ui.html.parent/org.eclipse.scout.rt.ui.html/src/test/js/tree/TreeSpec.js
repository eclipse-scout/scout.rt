describe("Tree", function() {
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
    if (id === undefined) {
      id = createUniqueAdapterId();
    }

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

  function createTree(model) {
    var tree = new scout.Tree();
    tree.init(model, session);
    session.registerModelAdapter(tree); //FIXME CGU remove after moving to constructor
    return tree;
  }

  describe("constructor", function() {
    it("adds nodes", function() {

      var model = createModel();
      model.nodes = [createModelNode(1)];
      var tree = createTree(model);
      tree.render(session.$entryPoint);

      expect(tree._$treeScroll.find('.tree-item').length).toBe(1);
    });
  });

  describe("_onNodeClicked", function() {

    it("reacts on node clicks", function() {
      var model = createModel();
      model.nodes = [createModelNode(1)];
      var tree = createTree(model);
      spyOn(tree, '_onNodeClicked');
      tree.render(session.$entryPoint);

      var $node = tree._$treeScroll.find('.tree-item:first');
      $node.click();

      expect(tree._onNodeClicked).toHaveBeenCalled();
    });

    it("sends click, selection and expansion events in one call in this order", function() {
      var model = createModel();
      model.nodes = [createModelNode(1)];
      var tree = createTree(model);
      tree.render(session.$entryPoint);

      var $node = tree._$treeScroll.find('.tree-item:first');
      $node.click();

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);

      var requestData = mostRecentJsonRequest();
      expect(requestData).toContainEventTypesExactly(['nodeClicked', 'nodesSelected', 'nodeExpanded']);
    });
  });

  describe("_setNodeSelected", function() {
    it("does not send events if called when processing response", function() {
      var model = createModel();
      model.nodes = [createModelNode(1)];
      var tree = createTree(model);
      tree.render(session.$entryPoint);

      var message = {
        events: [{
          id: model.id,
          nodeIds: [model.nodes[0].id],
          type: 'nodesSelected'
        }]
      };
      session._processSuccessResponse(message);

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(0);
    });
  });

});
