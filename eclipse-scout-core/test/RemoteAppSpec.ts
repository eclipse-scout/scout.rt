/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormSpecHelper, TestingApp} from '../src/testing/index';
import {App} from '../src';

describe('RemoteApp', () => {
  let session: SandboxSession;
  let helper: FormSpecHelper;
  let originalApp = App.get();

  afterAll(() => {
    // restore original app as creating new App instances automatically overwrites the static App variable
    TestingApp.set(originalApp);
  });

  beforeEach(() => {
    // @ts-expect-error
    setFixtures(sandbox().addClass('scout'));
    session = sandboxSession({
      renderDesktop: false
    });
    helper = new FormSpecHelper(session);
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
      jasmine.clock().install();
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
      jasmine.clock().tick(10);
      helper.closeMessageBoxes();
      jasmine.clock().tick(10);
      jasmine.clock().uninstall();
    });
  });
});
