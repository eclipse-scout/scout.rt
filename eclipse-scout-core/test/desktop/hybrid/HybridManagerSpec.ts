/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

import {HybridManager, scout, UuidPool} from '../../../src';
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

  describe('triggerActionAndWait', () => {
    it('triggers a HybridAction and waits for its completion', done => {
      const id = '42';
      UuidPool.get(session).uuids.push(id);
      HybridManager.get(session).triggerHybridActionAndWait('Ping').then(() => done());
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
});
