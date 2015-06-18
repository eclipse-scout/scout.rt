describe("Planner", function() {
  var session;
  var helper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.Ajax.install();
    jasmine.clock().install();
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createPlannerModel(numResources) {
    var model = createSimpleModel('Planner');
    model.resources = [];
    for (var i = 0; i < numResources; i++) {
      model.resources[i] = createResource('resource' + i);
    }
    return model;
  }

  function createResource(text) {
    return {
      id: scout.createUniqueId(),
      resourceCell: {
        text: text
      }
    };
  }

  function createPlanner(model) {
    var planner = new scout.Planner();
    planner.init(model, session);
    return planner;
  }

  function find$Resources(planner) {
    return planner.$grid.find('.resource');
  }

  describe("deleteResources", function() {
    var model, planner, resource0, resource1, resource2;

    beforeEach(function() {
      model = createPlannerModel(3);
      planner = createPlanner(model);
      resource0 = model.resources[0];
      resource1 = model.resources[1];
      resource2 = model.resources[2];
    });

    it("deletes resources from model", function() {
      expect(planner.resources.length).toBe(3);
      expect(planner.resources[0]).toBe(resource0);
      expect(Object.keys(planner.resourceMap).length).toBe(3);
      expect(planner.resourceMap[resource0.id]).toBe(resource0);

      planner._deleteResources([resource0]);
      expect(planner.resources.length).toBe(2);
      expect(planner.resources[0]).toBe(resource1);
      expect(Object.keys(planner.resourceMap).length).toBe(2);
      expect(planner.resourceMap[resource0.id]).toBeUndefined();

      planner._deleteResources([resource1, resource2]);
      expect(Object.keys(planner.resourceMap).length).toBe(0);
      expect(planner.resourceMap.length).toBe(0);
    });

    it("deletes resources from model html document", function() {
      planner.render(session.$entryPoint);
      expect(find$Resources(planner).length).toBe(3);

      planner._deleteResources([resource0]);
      expect(find$Resources(planner).length).toBe(2);
      expect(find$Resources(planner).eq(0).data('resource')).toBe(resource1);

      planner._deleteResources([resource1, resource2]);
      expect(find$Resources(planner).length).toBe(0);
    });

    it("also adjusts selectedResources and selectionRange if deleted resource was selected", function() {
      planner.selectedResources = [resource0];
      expect(planner.selectedResources.length).toBe(1);
      planner._deleteResources([resource0]);
      expect(planner.selectedResources.length).toBe(0);
      expect(planner.selectionRange.from).toBeUndefined();
      expect(planner.selectionRange.to).toBeUndefined();
    });

  });
});
