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

  function createModelFixture(nodeCount, depth, expanded) {
    return createModel(createUniqueAdapterId(), createModelNodes(nodeCount, depth, expanded));
  }

  function createModel(id, nodes) {
    var model = {
      "id": id,
    };

    if (nodes) {
      model.nodes = nodes;
    }

    return model;
  }

  function createModelNode(id, text) {
    return {
      "id": id,
      "text": text
    };
  }

  function createModelNodes(nodeCount, depth, expanded) {
    return createModelNodesInternal(nodeCount, depth, expanded);
  }

  function createModelNodesInternal(nodeCount, depth, expanded, parentNode) {
    if (!nodeCount) {
      return;
    }

    var nodes = [],
      nodeId;
    if (!depth) {
      depth = 0;
    }
    for (var i = 0; i < nodeCount; i++) {
      nodeId = i;
      if (parentNode) {
        nodeId = parentNode.id + '_' + nodeId;
      }
      nodes[i] = createModelNode(nodeId, 'node ' + i);
      nodes[i].expanded = expanded;
      if (depth > 0) {
        nodes[i].childNodes = createModelNodesInternal(nodeCount, depth - 1, expanded, nodes[i]);
      }
    }
    return nodes;
  }

  function createTree(model) {
    var tree = new scout.Tree();
    tree.init(model, session);
    return tree;
  }

  function findAllNodes(tree) {
    return tree._$treeScroll.find('.tree-item');
  }

  describe("creation", function() {
    it("adds nodes", function() {

      var model = createModelFixture(1);
      var tree = createTree(model);
      tree.render(session.$entryPoint);

      expect(findAllNodes(tree).length).toBe(1);
    });

    it("does not add notes if no nodes are provided", function() {

      var model = createModelFixture();
      var tree = createTree(model);
      tree.render(session.$entryPoint);

      expect(findAllNodes(tree).length).toBe(0);
    });
  });

  describe("_onNodeClicked", function() {

    it("reacts on node clicks", function() {
      var model = createModelFixture(1);
      var tree = createTree(model);
      spyOn(tree, '_onNodeClicked');
      tree.render(session.$entryPoint);

      var $node = tree._$treeScroll.find('.tree-item:first');
      $node.click();

      expect(tree._onNodeClicked).toHaveBeenCalled();
    });

    it("sends click, selection and expansion events in one call in this order", function() {
      var model = createModelFixture(1);
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
      var model = createModelFixture(1);
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

  describe("onModelAction", function() {

    describe("delete node event", function() {
      var model;
      var tree;
      var node0;
      var node1;
      var node2;

      function createNodesDeletedEvent(model, nodeIds, commonParentNodeId) {
        return {
          id: model.id,
          commonParentNodeId: commonParentNodeId,
          nodeIds: nodeIds,
          type: 'nodesDeleted'
        };
      }

      beforeEach(function() {
        model = createModelFixture(3, 1, true);
        tree = createTree(model);
        node0 = model.nodes[0];
        node1 = model.nodes[1];
        node2 = model.nodes[2];
      });

      describe("deleting a child", function() {

        it("updates model", function() {
          var node2Child0 = node2.childNodes[0];
          var node2Child1 = node2.childNodes[1];
          expect(tree.nodes.length).toBe(3);
          expect(tree.nodes[0]).toBe(node0);
          expect(Object.keys(tree._nodeMap).length).toBe(12);

          var message = {
            events: [createNodesDeletedEvent(model, [node2Child0.id], node2.id)]
          };
          session._processSuccessResponse(message);
          sendQueuedAjaxCalls();

          expect(tree.nodes[2].childNodes.length).toBe(2);
          expect(tree.nodes[2].childNodes[0]).toBe(node2Child1);
          expect(Object.keys(tree._nodeMap).length).toBe(11);
        });

        it("updates html document", function() {
          tree.render(session.$entryPoint);

          var node2Child0 = node2.childNodes[0];
          var node2Child1 = node2.childNodes[1];

          expect(findAllNodes(tree).length).toBe(12);
          expect(tree._findNodeById(node2Child0.id).length).toBe(1);

          //Delete a child node
          var message = {
            events: [createNodesDeletedEvent(model, [node2Child0.id], node2.id)]
          };
          session._processSuccessResponse(message);
          sendQueuedAjaxCalls();

          expect(findAllNodes(tree).length).toBe(11);
          expect(tree._findNodeById(node2Child0.id).length).toBe(0);

          expect(tree._findNodeById(node0.id).length).toBe(1);
          expect(tree._findNodeById(node0.childNodes[0].id).length).toBe(1);
          expect(tree._findNodeById(node0.childNodes[1].id).length).toBe(1);
          expect(tree._findNodeById(node0.childNodes[2].id).length).toBe(1);
        });

      });

      describe("deleting a root node", function() {
        it("updates model", function() {
          var message = {
            events: [createNodesDeletedEvent(model, [node0.id])]
          };
          session._processSuccessResponse(message);
          sendQueuedAjaxCalls();

          expect(tree.nodes.length).toBe(2);
          expect(tree.nodes[0]).toBe(node1);
          expect(Object.keys(tree._nodeMap).length).toBe(8);
        });

        it("updates html document", function() {
          tree.render(session.$entryPoint);

          var message = {
            events: [createNodesDeletedEvent(model, [node0.id])]
          };
          session._processSuccessResponse(message);
          sendQueuedAjaxCalls();

          expect(findAllNodes(tree).length).toBe(8);
          expect(tree._findNodeById(node0.id).length).toBe(0);
          expect(tree._findNodeById(node0.childNodes[0].id).length).toBe(0);
          expect(tree._findNodeById(node0.childNodes[1].id).length).toBe(0);
          expect(tree._findNodeById(node0.childNodes[2].id).length).toBe(0);
        });
      });

      describe("deleting all nodes", function() {
        it("updates model", function() {
          var message = {
            events: [createNodesDeletedEvent(model, [node0.id, node1.id, node2.id])]
          };
          session._processSuccessResponse(message);
          sendQueuedAjaxCalls();

          expect(tree.nodes.length).toBe(0);
          expect(Object.keys(tree._nodeMap).length).toBe(0);
        });

        it("updates html document", function() {
          tree.render(session.$entryPoint);

          var message = {
            events: [createNodesDeletedEvent(model, [node0.id, node1.id, node2.id])]
          };
          session._processSuccessResponse(message);
          sendQueuedAjaxCalls();

          expect(findAllNodes(tree).length).toBe(0);
        });
      });

    });

    describe("delete all node event", function() {
      var model;
      var tree;
      var node0;
      var node1;
      var node2;
      var node1Child0;
      var node1Child1;
      var node1Child2;

      function createAllNodesDeletedEvent(model, commonParentNodeId) {
        return {
          id: model.id,
          commonParentNodeId: commonParentNodeId,
          type: 'allNodesDeleted'
        };
      }


      beforeEach(function() {
        model = createModelFixture(3, 1, true);
        tree = createTree(model);
        node0 = model.nodes[0];
        node1 = model.nodes[1];
        node2 = model.nodes[2];
        node1Child0 = node1.childNodes[0];
        node1Child1 = node1.childNodes[1];
        node1Child2 = node1.childNodes[1];
      });

      it("deletes all nodes from model", function() {
        expect(tree.nodes.length).toBe(3);
        expect(Object.keys(tree._nodeMap).length).toBe(12);

        var message = {
          events: [createAllNodesDeletedEvent(model)]
        };
        session._processSuccessResponse(message);
        sendQueuedAjaxCalls();

        expect(tree.nodes.length).toBe(0);
        expect(Object.keys(tree._nodeMap).length).toBe(0);
      });

      it("deletes all nodes from html document", function() {
        tree.render(session.$entryPoint);

        expect(findAllNodes(tree).length).toBe(12);

        var message = {
          events: [createAllNodesDeletedEvent(model)]
        };
        session._processSuccessResponse(message);
        sendQueuedAjaxCalls();

        expect(findAllNodes(tree).length).toBe(0);
      });

      it("deletes all nodes from model for a given parent", function() {
        expect(tree.nodes.length).toBe(3);
        expect(Object.keys(tree._nodeMap).length).toBe(12);

        var message = {
          events: [createAllNodesDeletedEvent(model, node1.id)]
        };
        session._processSuccessResponse(message);
        sendQueuedAjaxCalls();

        expect(node1.childNodes.length).toBe(0);
        expect(Object.keys(tree._nodeMap).length).toBe(9);
      });

      it("deletes all nodes from html document for a given parent", function() {
        tree.render(session.$entryPoint);

        expect(findAllNodes(tree).length).toBe(12);

        var message = {
          events: [createAllNodesDeletedEvent(model, node1.id)]
        };
        session._processSuccessResponse(message);
        sendQueuedAjaxCalls();

        expect(findAllNodes(tree).length).toBe(9);

        //Check that children are removed, parent must still exist
        expect(tree._findNodeById(node1.id).length).toBe(1);
        expect(tree._findNodeById(node1Child0.id).length).toBe(0);
        expect(tree._findNodeById(node1Child1.id).length).toBe(0);
        expect(tree._findNodeById(node1Child2.id).length).toBe(0);
      });

    });
  });
});
