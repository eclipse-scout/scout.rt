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
import {TestingApp} from '../src/testing/index';

describe('RemoteApp', () => {
  let session: SandboxSession;

  beforeEach(() => {
    // @ts-ignore
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
