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

  describe('open', function() {

    it('opens the form', function() {
      var form = helper.createFormWithOneField();
      form.open();
      expect(form.rendered).toBe(true);
      expect(session.desktop.dialogs.indexOf(form) > -1).toBe(true);
    });

    it('adds it to the display parent', function() {
      var parentForm = helper.createFormWithOneField();
      parentForm.open();
      expect(session.desktop.dialogs.indexOf(parentForm) > -1).toBe(true);

      var form = helper.createFormWithOneField();
      form.displayParent = parentForm;
      form.open();
      expect(form.rendered).toBe(true);
      expect(session.desktop.dialogs.indexOf(form) > -1).toBe(false);
      expect(parentForm.dialogs.indexOf(form) > -1).toBe(true);
    });

  });

  describe('close', function() {

    it('closes the form', function() {
      var form = helper.createFormWithOneField();
      form.open();
      form.close();
      expect(session.desktop.dialogs.indexOf(form) > -1).toBe(false);
      expect(form.rendered).toBe(false);
      expect(form.destroyed).toBe(true);
    });

    it('removes it from the display parent', function() {
      var parentForm = helper.createFormWithOneField();
      parentForm.open();

      var form = helper.createFormWithOneField();
      form.displayParent = parentForm;
      form.open();
      expect(parentForm.dialogs.indexOf(form) > -1).toBe(true);

      form.close();
      expect(parentForm.dialogs.indexOf(form) > -1).toBe(false);
      expect(form.rendered).toBe(false);
      expect(form.destroyed).toBe(true);
    });

  });

  describe('abort', function() {

    it('closes the form if there is a close button', function() {
      var form = scout.create('Form', {
        parent: session.desktop,
        rootGroupBox: {
          objectType: 'GroupBox',
          mainBox: true,
          menus: [{
            objectType: 'CloseMenu',
          }]
        }
      });
      spyOn(form, 'close').and.callThrough();
      spyOn(form, 'cancel').and.callThrough();
      form.open();
      form.abort();
      expect(form.close.calls.count()).toEqual(1);
      expect(form.cancel.calls.count()).toEqual(0);
      expect(form.destroyed).toBe(true);
    });

    it('closes the form by using cancel if there is no close button', function() {
      var form = scout.create('Form', {
        parent: session.desktop,
        rootGroupBox: {
          objectType: 'GroupBox',
          mainBox: true
        }
      });
      spyOn(form, 'close').and.callThrough();
      spyOn(form, 'cancel').and.callThrough();
      form.open();
      form.abort();
      expect(form.close.calls.count()).toEqual(0);
      expect(form.cancel.calls.count()).toEqual(1);
      expect(form.destroyed).toBe(true);
    });

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

  describe('cacheBounds', function() {

    var form;

    beforeEach(function() {
      form = helper.createFormWithOneField();
      form.cacheBounds = true;
      form.cacheBoundsKey = 'FOO';
      form.render();

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
