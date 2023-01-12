/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {TestingApp} from '../src/testing/index';

describe('RemoteApp', () => {
  let session: SandboxSession;

  beforeEach(() => {
    // @ts-expect-error
    setFixtures(sandbox().addClass('scout'));
    session = sandboxSession({
      renderDesktop: false
    });
  });

  describe('initDone', () => {

    it('waits for session startup to complete', done => {
      let app = new TestingApp();
      app.init();
      app._createSession = options => session;
      let loaded = false;
      session.start = () => {
        let def = $.Deferred();
        setTimeout(() => {
          loaded = true;
          def.resolve();
        });
        return def.promise();
      };
      app.when('init')
        .then(() => {
          expect(loaded).toBe(true);
          expect(app.initialized).toBe(true);
        })
        .then(done)
        .catch(fail);
    });

    it('is not executed when session startup fails', done => {
      let app = new TestingApp();
      app.init()
        .catch(() => {
          expect(app.initialized).toBe(false);
          done();
        });
      app._createSession = options => session;
      let loaded = false;
      session.start = () => {
        let def = $.Deferred();
        setTimeout(() => {
          loaded = true;
          def.reject();
        });
        return def.promise();
      };
    });
  });
});
