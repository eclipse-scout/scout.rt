/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {defaultValues, FullModelOf, ObjectUuidProvider, Planner, PlannerAdapter, PlannerResourceModel} from '../../src/index';

describe('PlannerAdapter', () => {
  let session: SandboxSession;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    jasmine.Ajax.install();
  });

  afterEach(() => {
    jasmine.Ajax.uninstall();
  });

  function createPlannerAdapter(model: FullModelOf<Planner> & { id: string; objectType: string; session: SandboxSession }): PlannerAdapter {
    let plannerAdapter = new PlannerAdapter();
    plannerAdapter.init(model);
    return plannerAdapter;
  }

  function createPlannerModel(numResources): FullModelOf<Planner> & { id: string; objectType: string; session: SandboxSession } {
    let model = createSimpleModel('Planner', session) as FullModelOf<Planner> & { id: string; objectType: string; session: SandboxSession };
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

  function createResource(text?: string): PlannerResourceModel {
    return {
      id: ObjectUuidProvider.createUiId(),
      resourceCell: {
        text: text
      },
      activities: [{
        beginTime: '2015-04-01 01:23:45.678Z',
        endTime: '2015-04-31 01:23:45.678Z',
        id: ObjectUuidProvider.createUiId()
      }, {
        beginTime: '2016-02-29 01:23:45.678Z',
        endTime: '2400-02-29 01:23:45.678Z',
        id: ObjectUuidProvider.createUiId()
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
      let planner = adapter.createWidget(model, session.desktop) as Planner;
      expect(planner['a']).toBe(123);
      expect(planner.resources[0]['b']).toBe(234);
      expect(planner.resources[0].activities[0]['c']).toBe(345);
    });

    it('are applied when resources are inserted', () => {
      defaultValues.init(defaults);
      let model = createPlannerModel(0);
      let adapter = createPlannerAdapter(model);
      let planner = adapter.createWidget(model, session.desktop) as Planner;
      expect(planner.resources.length).toBe(0);

      let event = createResourcesInsertedEvent(model, [createResource()]);
      adapter.onModelAction(event);
      expect(planner['a']).toBe(123);
      expect(planner.resources[0]['b']).toBe(234);
      expect(planner.resources[0].activities[0]['c']).toBe(345);
    });

    it('are applied when resources are updated', () => {
      defaultValues.init(defaults);
      let model = createPlannerModel(1);
      let adapter = createPlannerAdapter(model);
      let planner = adapter.createWidget(model, session.desktop) as Planner;

      planner.resources[0]['b'] = 999;
      let resource = {
        id: planner.resources[0].id,
        activities: []
      };
      let event = createResourcesUpdatedEvent(model, [resource]);
      adapter.onModelAction(event);
      expect(planner.resources[0]['b']).toBe(234);
    });
  });
});
