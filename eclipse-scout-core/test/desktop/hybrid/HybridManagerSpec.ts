/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {HybridManager, LabelField, scout, StringField, UuidPool, Widget} from '../../../src';
import {FormSpecHelper} from '../../../src/testing';

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
      expect(true).toBeTrue(); // suppress "spec has no expectations" warning
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
      expect(true).toBeTrue(); // suppress "spec has no expectations" warning
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
});
