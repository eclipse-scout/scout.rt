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
import {FocusManagerSpecHelper, FormSpecHelper, TableSpecHelper, TreeSpecHelper} from '@eclipse-scout/testing';
import {Device, focusUtils, scout} from '../../src/index';


/* global FocusManagerSpecHelper */
jasmine.DEFAULT_TIMEOUT_INTERVAL = 10000;
describe('scout.FocusManager', function() {
  var session, formHelper, focusHelper, focusManager;

  beforeEach(function() {
    setFixtures(sandbox());
    jasmine.Ajax.install();
    session = sandboxSession();
    focusManager = session.focusManager;
    formHelper = new FormSpecHelper(session);
    focusHelper = new FocusManagerSpecHelper();
    jasmine.clock().install();
    uninstallUnloadHandlers(session);
  });

  afterEach(function() {
    session = null;
    jasmine.Ajax.uninstall();
    jasmine.clock().uninstall();
  });

  function createDivWithTwoInputs() {
    var $container = session.$entryPoint.makeDiv();
    $container.appendElement('<input type="text" val="input1" class="input1">');
    $container.appendElement('<input type="text" val="input2" class="input2">');
    return $container;
  }

  describe('isSelectableText', function() {

    it('must return true for disabled text-fields', function() {
      var $textField = $('<input>')
        .attr('type', 'text')
        .attr('disabled', 'disabled');
      expect(focusUtils.isSelectableText($textField)).toBe(true);
    });

  });

  describe('Focus fixes for Internet Explorer (IE)', function() {

    beforeEach(function() {
      // simulate we are an IE
      Device.get().browser = Device.Browser.INTERNET_EXPLORER;
    });

    it('Click on table-cell, must focus table', function() {
      var tableHelper = new TableSpecHelper(session);
      var tableModel = tableHelper.createModelFixture(2, 1);
      var table = tableHelper.createTable(tableModel);
      table.render();

      // we don't really click - just simulate that the method has been called by the FocusManager
      var event = {
        target: $('.table-cell')[0],
        preventDefault: function() {
        }
      };
      spyOn(event, 'preventDefault');
      focusManager._handleIEEvent(event);
      expect(document.activeElement).toBe(table.$container[0]);
      expect(event.preventDefault).toHaveBeenCalled();
    });

    it('Click on tree-node, must focus tree', function() {
      var treeHelper = new TreeSpecHelper(session);
      var treeModel = treeHelper.createModelFixture(1);
      var tree = treeHelper.createTree(treeModel);
      tree.render();

      // we don't really click - just simulate that the method has been called by the FocusManager
      var event = {
        target: $('.tree-node')[0],
        preventDefault: function() {
        }
      };
      spyOn(event, 'preventDefault');
      focusManager._handleIEEvent(event);
      expect(document.activeElement).toBe(tree.$container[0]);
      expect(event.preventDefault).toHaveBeenCalled();
    });

  });

  describe('validateFocus', function() {

    it('When nothing else is focusable, focus must be on the Desktop (=sandbox)', function() {
      focusManager.validateFocus();
      var sandbox = $('#sandbox')[0];
      expect(document.activeElement).toBe(sandbox);
    });

    describe('with forms:', function() {

      var form;
      beforeEach(function() {
        form = formHelper.createFormWithFields(session.desktop, false, 4);
        form.render();
      });

      afterEach(function() {
        form.destroy();
        form = null;
      });

      /**
       * Because form is not a dialog, it does not install its own focus-context
       * but uses the focus-context of the Desktop (=sandbox) instead.
       */
      it('Focus-context must install listeners on its $container', function() {
        expect(focusHelper.handlersRegistered(session.$entryPoint)).toBe(true);
      });

      it('Focus must be on the 1st form-field when form is rendered', function() {
        var $firstField = form.rootGroupBox.fields[0].$field;
        expect($firstField).toBeFocused();
      });

      it('FocusContext must remember the last focused element', function() {
        var $secondField = form.rootGroupBox.fields[1].$field;
        $secondField.focus();
        expect($secondField).toBeFocused();

        expect(focusManager._findActiveContext().lastValidFocusedElement).toBe($secondField[0]);
      });

      it('A new FocusContext must be created when a form is opened as dialog', function() {
        var $secondField = form.rootGroupBox.fields[1].$field;
        $secondField.focus(); // must be remembered by focus-context

        var sandboxContext = focusManager._findActiveContext();
        expect(sandboxContext.$container).toBe(session.$entryPoint);

        var dialog = formHelper.createFormWithFields(session.desktop, true, 2);
        dialog.render();

        expect(focusManager._focusContexts.length).toBe(2);

        var dialogContext = focusManager._findActiveContext();
        expect(dialogContext.$container).toBe(dialog.$container);

        // focus-context must install handlers on form $container
        expect(focusHelper.handlersRegistered(dialog.$container)).toBe(true);

        // must remember last focused field of first focus-context
        expect(sandboxContext.lastValidFocusedElement).toBe($secondField[0]);
      });

      it('Must focus another valid field if the focused field is removed', function() {
        var $firstField = form.rootGroupBox.fields[0].$field,
          $secondField = form.rootGroupBox.fields[1].$field;

        expect($firstField).toBeFocused();
        $firstField.remove();
        expect($secondField).toBeFocused();
      });


      it('Must focus another valid field if the focused field is hidden', function() {
        var $firstField = form.rootGroupBox.fields[0].$field,
          $secondField = form.rootGroupBox.fields[1].$field;

        expect($firstField).toBeFocused();
        $firstField.setVisible(false);
        expect($secondField).toBeFocused();
      });

    });

  });

  describe('activateFocusContext', function() {

    it('activates the context of the given $container and restores its focus', function() {
      var $container1 = createDivWithTwoInputs().appendTo(session.$entryPoint);
      var $container2 = createDivWithTwoInputs().appendTo(session.$entryPoint);
      focusManager.installFocusContext($container1);
      expect(document.activeElement).toBe($container1.children('.input1')[0]);

      focusManager.installFocusContext($container2);
      focusManager.requestFocus($container2.children('.input2'));
      expect(document.activeElement).toBe($container2.children('.input2')[0]);

      focusManager.activateFocusContext($container1);
      expect(document.activeElement).toBe($container1.children('.input1')[0]);

      focusManager.activateFocusContext($container2);
      expect(document.activeElement).toBe($container2.children('.input2')[0]);

      focusManager.uninstallFocusContext($container1);
      focusManager.uninstallFocusContext($container2);
    });

  });

  describe('requestFocus', function() {
    it('focuses the given element', function() {
      var $container1 = createDivWithTwoInputs().appendTo(session.$entryPoint);
      focusManager.installFocusContext($container1);

      focusManager.requestFocus($container1.children('.input1'));
      expect(document.activeElement).toBe($container1.children('.input1')[0]);

      focusManager.uninstallFocusContext($container1);
    });

    it('activates the context of the element if the element to focus is not in the active context', function() {
      var $container1 = createDivWithTwoInputs().appendTo(session.$entryPoint);
      var $container2 = createDivWithTwoInputs().appendTo(session.$entryPoint);
      focusManager.installFocusContext($container1);
      focusManager.installFocusContext($container2);

      focusManager.requestFocus($container1.children('.input1'));
      expect(document.activeElement).toBe($container1.children('.input1')[0]);
      expect(focusManager._findActiveContext().$container[0]).toBe($container1[0]);

      focusManager.requestFocus($container2.children('.input2'));
      expect(document.activeElement).toBe($container2.children('.input2')[0]);
      expect(focusManager._findActiveContext().$container[0]).toBe($container2[0]);

      focusManager.uninstallFocusContext($container1);
      focusManager.uninstallFocusContext($container2);
    });

    it('does nothing if the element cannot be focused', function() {
      var $container1 = createDivWithTwoInputs().appendTo(session.$entryPoint);
      var $container2 = createDivWithTwoInputs().appendTo(session.$entryPoint);
      focusManager.installFocusContext($container1);
      focusManager.installFocusContext($container2);

      focusManager.requestFocus($container1.children('.input1'));
      expect(document.activeElement).toBe($container1.children('.input1')[0]);
      expect(focusManager._findActiveContext().$container[0]).toBe($container1[0]);

      // Container2 is covered by a glass pane -> requesting focus on a covered element should do nothing
      var glassPane = scout.create('GlassPane', {
        parent: session.desktop
      });
      glassPane.render($container2);
      focusManager.requestFocus($container2.children('.input2'));
      expect(document.activeElement).toBe($container1.children('.input1')[0]);
      expect(focusManager._findActiveContext().$container[0]).toBe($container1[0]);

      focusManager.uninstallFocusContext($container1);
      focusManager.uninstallFocusContext($container2);
    });

    it('activates the correct context', function() {
      var $input0 = session.$entryPoint.appendElement('<input type="text" class="input0">');
      var $container1 = createDivWithTwoInputs().appendTo(session.$entryPoint);
      var $container2 = createDivWithTwoInputs().appendTo(session.$entryPoint);
      focusManager.installFocusContext(session.$entryPoint);
      focusManager.installFocusContext($container1);
      focusManager.installFocusContext($container2);

      focusManager.requestFocus($input0);
      expect(document.activeElement).toBe($input0[0]);
      expect(focusManager._findActiveContext().$container[0]).toBe(session.$entryPoint[0]);

      focusManager.requestFocus($container1.children('.input1'));
      expect(document.activeElement).toBe($container1.children('.input1')[0]);
      expect(focusManager._findActiveContext().$container[0]).toBe($container1[0]);

      focusManager.requestFocus($container2.children('.input2'));
      expect(document.activeElement).toBe($container2.children('.input2')[0]);
      expect(focusManager._findActiveContext().$container[0]).toBe($container2[0]);

      focusManager.uninstallFocusContext(session.$entryPoint);
      focusManager.uninstallFocusContext($container1);
      focusManager.uninstallFocusContext($container2);
    });
  });

  describe('registerGlassPaneTarget', function() {
    it('removes the focus if the active element will be covered by the glass pane', function() {
      var $container1 = createDivWithTwoInputs().appendTo(session.$entryPoint);
      focusManager.installFocusContext($container1);

      focusManager.requestFocus($container1.children('.input1'));
      expect(document.activeElement).toBe($container1.children('.input1')[0]);
      expect(focusManager._findActiveContext().$container[0]).toBe($container1[0]);

      // GlassPane will cover the active element -> blur it and focus desktop
      var glassPane = scout.create('GlassPane', {
        parent: session.desktop
      });
      glassPane.render($container1);
      expect(document.activeElement).toBe(session.$entryPoint[0]);

      // Destroy glass pane -> restore focus
      glassPane.destroy();
      expect(document.activeElement).toBe($container1.children('.input1')[0]);

      focusManager.uninstallFocusContext($container1);
    });
  });
});
