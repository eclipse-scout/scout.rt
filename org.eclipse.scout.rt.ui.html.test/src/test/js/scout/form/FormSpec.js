/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/* global linkWidgetAndAdapter */
describe('Form', function() {
  var session, helper;

  beforeEach(function() {
    setFixtures(sandbox());
    jasmine.Ajax.install();
    jasmine.clock().install();
    session = sandboxSession();
    helper = new scout.FormSpecHelper(session);
    uninstallUnloadHandlers(session);
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  describe('destroy', function() {

    it('destroys its children', function() {
      var form = helper.createFormWithOneField();

      expect(form.rootGroupBox).toBeTruthy();
      expect(form.rootGroupBox.fields[0]).toBeTruthy();

      form.destroy();
      expect(form.rootGroupBox.destroyed).toBeTruthy();
      expect(form.rootGroupBox.fields[0].destroyed).toBeTruthy();
    });

  });

  describe('onModelAction', function() {

    describe('formClose', function() {

      function createDisposeAdapterEvent(model) {
        return {
          target: session.uiSessionId,
          type: 'disposeAdapter',
          adapter: model.id
        };
      }

      it('destroys the form', function() {
        var form = helper.createFormWithOneField();
        linkWidgetAndAdapter(form, 'FormAdapter');
        spyOn(form, 'destroy');

        var message = {
          events: [createDisposeAdapterEvent(form)]
        };
        session._processSuccessResponse(message);

        expect(form.destroy).toHaveBeenCalled();
      });

    });

  });

  describe('cacheBounds', function() {

    var form;

    beforeEach(function() {
      form = helper.createFormWithOneField();
      form.cacheBounds = true;
      form.cacheBoundsKey = 'FOO';
      form.render(session.$entryPoint);

      scout.webstorage.removeItem(localStorage, 'scout:formBounds:FOO');
    });

    it('read and store bounds', function() {
      // should return null when local storage not contains the requested key
      expect(form.readCacheBounds()).toBe(null);

      // should return the stored Rectangle
      var storeBounds = new scout.Rectangle(0, 1, 2, 3);
      form.storeCacheBounds(storeBounds);
      var readBounds = form.readCacheBounds();
      expect(readBounds).toEqual(storeBounds);
    });

    it('update bounds - if cacheBounds is true', function() {
      form.updateCacheBounds();
      expect(form.readCacheBounds() instanceof scout.Rectangle).toBe(true);
    });

    it('update bounds - if cacheBounds is false', function() {
      form.cacheBounds = false;
      form.updateCacheBounds();
      expect(form.readCacheBounds()).toBe(null);
    });

  });

});
