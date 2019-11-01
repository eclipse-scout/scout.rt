/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {TestingApp} from '@eclipse-scout/testing';


describe('RemoteApp', function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox().addClass('scout'));
    session = sandboxSession({
      renderDesktop: false
    });
  });

  describe('initDone', function() {

    it('waits for session startup to complete', function(done) {
      var app = new TestingApp();
      app.init();
      app._createSession = function(options) {
        return session;
      };
      var loaded = false;
      session.start = function() {
        var def = $.Deferred();
        setTimeout(function() {
          loaded = true;
          def.resolve();
        });
        return def.promise();
      };
      app.when('init')
        .then(function() {
          expect(loaded).toBe(true);
          expect(app.initialized).toBe(true);
        })
        .then(done)
        .catch(fail);
    });

    it('is not executed when session startup fails', function(done) {
      var app = new TestingApp();
      app.init()
        .catch(function() {
          expect(app.initialized).toBe(false);
          done();
        });
      app._createSession = function(options) {
        return session;
      };
      var loaded = false;
      session.start = function() {
        var def = $.Deferred();
        setTimeout(function() {
          loaded = true;
          def.reject();
        });
        return def.promise();
      };
    });
  });
});
