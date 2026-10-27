/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {FormSpecHelper, JasmineScoutUtil, TestingApp} from '../src/testing/index';
import {App, Deferred, ErrorHandler} from '../src';

describe('RemoteApp', () => {
  let session: SandboxSession;
  let helper: FormSpecHelper;
  let originalApp = App.get();

  beforeEach(() => {
    // @ts-expect-error
    setFixtures(sandbox().addClass('scout'));
    session = sandboxSession({
      renderDesktop: false
    });
    helper = new FormSpecHelper(session);
    jasmine.Ajax.install();
  });

  afterEach(() => {
    jasmine.Ajax.uninstall();
  });

  afterAll(() => {
    // restore original app as creating new App instances automatically overwrites the static App variable
    TestingApp.set(originalApp);
  });

  describe('initDone', () => {

    it('waits for session startup to complete', done => {
      let app = new TestingApp();
      app.init();
      app._createSession = options => session;
      let loaded = false;
      session.start = () => {
        let def = new Deferred<void>();
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

    it('is not executed when session startup fails', async () => {
      let app = new TestingApp();
      let initPromise = app.init();
      app._createSession = options => session;
      let loaded = false;
      session.start = () => {
        let def = new Deferred<void>();
        setTimeout(() => {
          loaded = true;
          def.reject();
        });
        return def.promise();
      };

      // Let the rejected startup promise propagate to the error handler, which renders a message box
      await sleep(10);
      helper.closeMessageBoxes();

      await expectAsync(initPromise).toBeRejected();
      expect(app.initialized).toBe(false);
    });
  });

  describe('_fail', () => {
    it('marks error as consumed if session shows fatal message', async () => {
      let app = new TestingApp();
      app._createSession = options => session;
      let promise = app.init();
      const sessionExpiredResponse = {
        'error': {
          'code': 10,
          'message': 'The session has expired, please reload the page.'
        }
      };
      JasmineScoutUtil.mockRestCall('json?startup', sessionExpiredResponse);
      const error = await promise.catch(err => err);
      expect(error.error).toEqual(sessionExpiredResponse.error);
      expect(ErrorHandler.isConsumed(error)).toBeTrue();
      expect(session.isFatalMessageShown()).toBeTrue();
    });
  });
});
