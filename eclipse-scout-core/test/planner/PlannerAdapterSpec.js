/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {defaultValues, ObjectFactory, PlannerAdapter} from '../../src';

describe('PlannerAdapter', () => {
  let session;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.Ajax.install();
  });

  afterEach(() => {
    jasmine.Ajax.uninstall();
  });

  function createPlannerAdapter(model) {
    let plannerAdapter = new PlannerAdapter();
    plannerAdapter.init(model);
    return plannerAdapter;
  }

  function createPlannerModel(numResources) {
    let model = createSimpleModel('Planner', session);
    model.resources = [];
    for (let i = 0; i < numResources; i++) {
      model.resources[i] = createResource('resource' + i);
    }
    return model;
  }

  function createResourcesInsertedEvent(model, resources) {
    return {
      target: model.id,
      resources: resources,
      type: 'resourcesInserted'
    };
  }

  function createResourcesUpdatedEvent(model, resources) {
    return {
      target: model.id,
      resources: resources,
      type: 'resourcesUpdated'
    };
  }

  function createResource(text) {
    return {
      id: ObjectFactory.get().createUniqueId(),
      resourceCell: {
        text: text
      },
      activities: [{
        beginTime: '2015-04-01 01:23:45.678Z',
        endTime: '2015-04-31 01:23:45.678Z',
        id: ObjectFactory.get().createUniqueId()
      }, {
        beginTime: '2016-02-29 01:23:45.678Z',
        endTime: '2400-02-29 01:23:45.678Z',
        id: ObjectFactory.get().createUniqueId()
      }]
    };
  }

  describe('defaultValues', () => {
    let defaults = {
      'defaults': {
        'Planner': {
          'a': 123
        },
        'Resource': {
          'b': 234
        },
        'Activity': {
          'c': 345
        }
      },
      'objectTypeHierarchy': {
        'Widget': {
          'Planner': null
        }
      }
    };

    it('are applied on init', () => {
      defaultValues.init(defaults);
      let model = createPlannerModel(2);
      let adapter = createPlannerAdapter(model);
      let planner = adapter.createWidget(model, session.desktop);
      expect(planner.a).toBe(123);
      expect(planner.resources[0].b).toBe(234);
      expect(planner.resources[0].activities[0].c).toBe(345);
    });

    it('are applied when resources are inserted', () => {
      defaultValues.init(defaults);
      let model = createPlannerModel(0);
      let adapter = createPlannerAdapter(model);
      let planner = adapter.createWidget(model, session.desktop);
      expect(planner.resources.length).toBe(0);

      let event = createResourcesInsertedEvent(model, [createResource()]);
      adapter.onModelAction(event);
      expect(planner.a).toBe(123);
      expect(planner.resources[0].b).toBe(234);
      expect(planner.resources[0].activities[0].c).toBe(345);
    });

    it('are applied when resources are updated', () => {
      defaultValues.init(defaults);
      let model = createPlannerModel(1);
      let adapter = createPlannerAdapter(model);
      let planner = adapter.createWidget(model, session.desktop);

      planner.resources[0].b = 999;
      let resource = {
        id: planner.resources[0].id,
        activities: []
      };
      let event = createResourcesUpdatedEvent(model, [resource]);
      adapter.onModelAction(event);
      expect(planner.resources[0].b).toBe(234);
    });
  });
});
