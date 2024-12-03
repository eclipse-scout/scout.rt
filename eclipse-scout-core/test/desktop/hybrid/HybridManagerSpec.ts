/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {Form, FormAdapter, HybridActionContextElements, HybridManager, HybridManagerAdapter, LabelField, scout, StringField, Tree, TreeAdapter, TreeNode, UuidPool, Widget} from '../../../src';
import {FormSpecHelper, TreeSpecHelper} from '../../../src/testing';

describe('HybridManager', () => {
  let session: SandboxSession, formHelper: FormSpecHelper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession({
      desktop: {
        navigationVisible: true,
        headerVisible: true,
        benchVisible: true
      },
      renderDesktop: false
    });
    session.desktop.addOns.push(
      scout.create(HybridManager, {parent: session.desktop}),
      scout.create(UuidPool, {parent: session.desktop})
    );
    linkWidgetAndAdapter(HybridManager.get(session), 'HybridManagerAdapter');
    formHelper = new FormSpecHelper(session);
  });

  describe('widgets', () => {
    it('destroys removed widgets if hybrid manager is the owner', () => {
      UuidPool.get(session).uuids.push('123');
      UuidPool.get(session).uuids.push('234');
      let hybridManager = HybridManager.get(session);
      expect(Object.entries(hybridManager.widgets).length).toBe(0);

      session._processSuccessResponse({
        adapterData: mapAdapterData([{
          id: '111',
          objectType: 'LabelField'
        }, {
          id: '222',
          objectType: 'StringField'
        }]),
        events: [
          {
            target: hybridManager.id,
            type: 'property',
            properties: {
              widgets: {
                123: '111',
                234: '222'
              }
            }
          }
        ]
      });
      let labelField = hybridManager.widgets['123'] as Widget;
      let stringField = hybridManager.widgets['234'] as Widget;
      expect(Object.entries(hybridManager.widgets).length).toBe(2);
      expect(labelField instanceof LabelField);
      expect(labelField.destroyed).toBe(false);
      expect(stringField instanceof StringField);
      expect(stringField.destroyed).toBe(false);

      // Widget '111' is not in the list anymore -> it needs to be destroyed
      session._processSuccessResponse({
        events: [
          {
            target: hybridManager.id,
            type: 'property',
            properties: {
              widgets: {
                234: '222'
              }
            }
          }
        ]
      });
      expect(Object.entries(hybridManager.widgets).length).toBe(1);
      expect(labelField.destroyed).toBe(true);
      expect(stringField.destroyed).toBe(false);
    });
  });

  describe('callActionAndWait', () => {
    it('calls a HybridAction and waits for its completion', done => {
      const id = '42';
      UuidPool.get(session).uuids.push(id);
      HybridManager.get(session).callActionAndWait('Ping').then(() => done());
      session._processSuccessResponse({
        events: [
          {
            target: HybridManager.get(session).id,
            type: 'hybridEvent',
            id,
            eventType: 'hybridActionEnd'
          }
        ]
      });
    });
  });

  describe('openForm', () => {
    it('waits for a form to be opened and listens for form events', done => {
      const id = '42';
      UuidPool.get(session).uuids.push(id);
      HybridManager.get(session).openForm('Dummy').then(form => {
        form.whenClose().then(() => done());
        session._processSuccessResponse({
          events: [
            {
              target: HybridManager.get(session).id,
              type: 'hybridWidgetEvent',
              id,
              eventType: 'close'
            }
          ]
        });
      });
      const form = formHelper.createFormWithOneField();
      session._processSuccessResponse({
        events: [
          {
            target: HybridManager.get(session).id,
            type: 'property',
            properties: {
              widgets: {
                42: form
              }
            }
          }
        ]
      });
    });
  });

  describe('context elements', () => {

    let form: Form;
    let formAdapter: FormAdapter;
    let tree: Tree;
    let treeAdapter: TreeAdapter;
    let treeNode: TreeNode;
    let hybridManager: HybridManager;
    let hybridManagerAdapter: HybridManagerAdapter;

    beforeEach(() => {
      jasmine.Ajax.install();
      jasmine.clock().install();

      let formSpecHelper = new FormSpecHelper(session);
      let treeSpecHelper = new TreeSpecHelper(session);

      form = formSpecHelper.createFormWithOneField();
      tree = treeSpecHelper.createTree(treeSpecHelper.createModelFixture(1));
      treeNode = tree.nodes[0];
      hybridManager = HybridManager.get(session);

      linkWidgetAndAdapter(form, FormAdapter);
      formAdapter = form.modelAdapter as FormAdapter;
      linkWidgetAndAdapter(tree, TreeAdapter);
      treeAdapter = tree.modelAdapter as TreeAdapter;
      hybridManagerAdapter = hybridManager.modelAdapter as HybridManagerAdapter;

      expect(form).toBeTruthy();
      expect(tree).toBeTruthy();
      expect(treeNode).toBeTruthy();
      expect(hybridManager).toBeTruthy();

      expect(formAdapter).toBeTruthy();
      expect(treeAdapter).toBeTruthy();
      expect(hybridManagerAdapter).toBeTruthy();

      expect(jasmine.Ajax.requests.count()).toBe(0);
    });

    afterEach(() => {
      jasmine.Ajax.uninstall();
      jasmine.clock().uninstall();
    });

    it('can send context elements to the server', () => {
      hybridManager.callActionAndWait('foo', {customData: 123}, scout.create(HybridActionContextElements)
        .withElement('form', form)
        .withElement('node', tree, treeNode));

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);
      let requestData = mostRecentJsonRequest();

      expect(requestData).toContainEventTypesExactly(['hybridAction']);
      let hybridActionEvent = requestData.events[0];
      expect(hybridActionEvent.actionType).toBe('foo');
      expect(hybridActionEvent.id).toBeTruthy();
      expect(hybridActionEvent.data).toEqual({customData: 123});
      expect(hybridActionEvent.contextElements).toEqual({
        form: [{widget: formAdapter.id}],
        node: [{widget: treeAdapter.id, element: treeNode.id}]
      });
    });

    it('only sends context elements when needed', () => {
      hybridManager.callActionAndWait('foo', {customData: 123});

      sendQueuedAjaxCalls();
      expect(jasmine.Ajax.requests.count()).toBe(1);
      let requestData = mostRecentJsonRequest();

      expect(requestData).toContainEventTypesExactly(['hybridAction']);
      let hybridActionEvent = requestData.events[0];
      expect(hybridActionEvent.actionType).toBe('foo');
      expect(hybridActionEvent.id).toBeTruthy();
      expect(hybridActionEvent.data).toEqual({customData: 123});
      expect(hybridActionEvent.contextElements).toBeUndefined();
    });

    it('can receive context elements from the server', () => {
      // Uninstall jasmine clock, since it seems to interfere with promises (spec will not complete without this)
      jasmine.clock().uninstall();

      let promise = hybridManager.when('hybridActionEnd:767676767').then(event => {
        expect(event.data).toEqual({customData: 123});
        expect(event.contextElements).toBeInstanceOf(HybridActionContextElements);
        expect(event.contextElements.isEmpty()).toBe(false);
        expect(event.contextElements.getList('form').length).toBe(1);
        expect(event.contextElements.getSingle('form').getWidget()).toBe(form);
        expect(event.contextElements.getSingle('form').optElement()).toBe(null);
        expect(event.contextElements.getList('node').length).toBe(1);
        expect(event.contextElements.getSingle('node').getWidget()).toBe(tree);
        expect(event.contextElements.getSingle('node').getElement()).toBe(treeNode);

        expect(event.contextElements.optList('doesNotExist')).toBe(undefined);
        expect(event.contextElements.optSingle('doesNotExist')).toBe(undefined);
        expect(() => event.contextElements.getList('doesNotExist')).toThrow();
        expect(() => event.contextElements.getSingle('doesNotExist')).toThrow();
      });

      session._processSuccessResponse({
        events: [
          {
            target: hybridManagerAdapter.id,
            type: 'hybridEvent',
            eventType: 'hybridActionEnd',
            id: '767676767',
            data: {customData: 123},
            contextElements: {
              form: [{widget: formAdapter.id}],
              node: [{widget: treeAdapter.id, element: treeNode.id}]
            }
          }
        ]
      });
      return promise;
    });
  });
});
