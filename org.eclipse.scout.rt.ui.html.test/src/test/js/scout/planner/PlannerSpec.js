/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
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
    var model = createSimpleModel('Planner', session);
    model.resources = [];
    for (var i = 0; i < numResources; i++) {
      model.resources[i] = createResource('resource' + i);
    }
    return model;
  }

  function createResource(text) {
    return {
      id: scout.objectFactory.createUniqueId(),
      resourceCell: {
        text: text
      },
      activities: [ {
          beginTime: '2015-04-01 01:23:45.678Z',
          endTime: '2015-04-31 01:23:45.678Z',
          id: scout.objectFactory.createUniqueId()
        }, {
          beginTime: '2016-02-29 01:23:45.678Z',
          endTime: '2400-02-29 01:23:45.678Z',
          id: scout.objectFactory.createUniqueId()
        } ]
    };
  }

  function createPlanner(model) {
    var planner = new scout.Planner();
    planner.init(model);
    return planner;
  }

  function find$Resources(planner) {
    return planner.$grid.find('.planner-resource');
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
      expect(Object.keys(planner.activityMap).length).toBe(2 * 3);
      expect(planner.resourceMap[resource0.id]).toBe(resource0);
      expect(planner.activityMap[resource0.activities[0].id]).toBe(resource0.activities[0]);

      planner.deleteResources([resource0]);
      expect(planner.resources.length).toBe(2);
      expect(planner.resources[0]).toBe(resource1);
      expect(Object.keys(planner.resourceMap).length).toBe(2);
      expect(Object.keys(planner.activityMap).length).toBe(2 * 2);
      expect(planner.resourceMap[resource0.id]).toBeUndefined();
      expect(planner.activityMap[resource0.activities[0].id]).toBeUndefined();

      planner.deleteResources([resource1, resource2]);
      expect(Object.keys(planner.resourceMap).length).toBe(0);
      expect(Object.keys(planner.activityMap).length).toBe(0);
      expect(planner.resourceMap.length).toBe(0);
      expect(planner.activityMap.length).toBe(0);
    });

    it("deletes resources from html document", function() {
      planner.render(session.$entryPoint);
      expect(find$Resources(planner).length).toBe(3);

      planner.deleteResources([resource0]);
      expect(find$Resources(planner).length).toBe(2);
      expect(find$Resources(planner).eq(0).data('resource')).toBe(resource1);

      planner.deleteResources([resource1, resource2]);
      expect(find$Resources(planner).length).toBe(0);
    });

    it("also adjusts selectedResources and selectionRange if deleted resource was selected", function() {
      planner.selectedResources = [resource0];
      expect(planner.selectedResources.length).toBe(1);
      planner.deleteResources([resource0]);
      expect(planner.selectedResources.length).toBe(0);
      expect(planner.selectionRange.from).toBeUndefined();
      expect(planner.selectionRange.to).toBeUndefined();
    });

  });


  describe("updateResources", function() {
    var model, planner, resource0, resource1, resource2, $resource1;

    beforeEach(function() {
      model = createPlannerModel(3);
      planner = createPlanner(model);
      resource0 = model.resources[0];
      resource1 = model.resources[1];
      resource2 = model.resources[2];
    });

    it("updates resources in model", function() {
      expect(planner.resources[1]).toBe(resource1);
      expect(planner.resources[1].resourceCell.text).toBe('resource1');
      expect(planner.resourceMap[resource1.id]).toBe(planner.resources[1]);

      var updatedResource = createResource('new resource1');
      updatedResource.id = resource1.id;
      planner._updateResources([updatedResource]);
      expect(planner.resources[1]).not.toBe(resource1);
      expect(planner.resources[1].resourceCell.text).toBe('new resource1');
      expect(planner.resourceMap[resource1.id]).toBe(planner.resources[1]);
    });

    it("updates resources in html document", function() {
      planner.render(session.$entryPoint);
      $resource1 = find$Resources(planner).eq(1);
      expect($resource1.children('.resource-title').text()).toBe('resource1');
      expect($resource1[0]).toBe(resource1.$resource[0]);

      var updatedResource = createResource('new resource1');
      updatedResource.id = resource1.id;
      planner._updateResources([updatedResource]);
      $resource1 = find$Resources(planner).eq(1);
      expect($resource1.children('.resource-title').text()).toBe('new resource1');
      expect($resource1[0]).toBe(updatedResource.$resource[0]);
      expect($resource1.data('resource')).toBe(updatedResource);
    });
  });
});
