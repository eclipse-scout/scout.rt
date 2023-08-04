/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, scout} from '../src/index';
import {AppModel} from '../src/App';
import {TestingApp} from '../src/testing';

describe('App', () => {

  let originalApp = App.get();

  afterAll(() => {
    // restore original app as creating new App instances automatically overwrites the static App variable
    TestingApp.set(originalApp);
  });

  beforeEach(() => {
    // @ts-expect-error
    setFixtures(sandbox().addClass('scout'));
  });

  afterEach(() => {
    $('.startup-error').remove();
  });

  describe('initDone', () => {
    it('is executed after desktop is rendered', done => {
      let app = new App();
      app.init();

      let desktop;
      app.when('desktopReady').then(event => {
        desktop = event.desktop;
      });
      app.when('init')
        .then(() => {
          expect(desktop.rendered).toBe(true);
          expect(app.initialized).toBe(true);
        })
        .then(done)
        .catch(fail);
    });

    class SpecApp extends App {
      override _load(options: AppModel): JQuery.Promise<any> {
        return super._load(options);
      }
    }

    it('waits for load to complete', done => {
      let app = new SpecApp();
      app.init();

      let loaded = false;
      app._load = () => {
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

    it('is not executed when loading fails', done => {
      let app = new SpecApp();
      app.init()
        .catch(() => {
          expect(app.initialized).toBe(false);
          done();
        });

      let loaded = false;
      app._load = () => {
        let def = $.Deferred();
        setTimeout(() => {
          loaded = true;
          def.reject();
        });
        return def.promise();
      };
    });
  });

  describe('aria properties', () => {

    it('has lang attribute set', done => {
      let app = new App();
      app.init();
      app.when('init')
        .then(() => {
          expect(document.documentElement.lang).not.toBeEmpty();
        })
        .then(done)
        .catch(fail);
    });
  });
});
