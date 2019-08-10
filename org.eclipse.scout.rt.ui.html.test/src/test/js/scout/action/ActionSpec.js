/*
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
describe('Action', function() {
  var $sandbox, session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = $('#sandbox');
  });

  describe('defaults', function() {

    it('should be as expected', function() {
      var action = new scout.Action();
      action.init(createSimpleModel('Action', session));
      expect(action.tabbable).toBe(false);
      expect(action.actionStyle).toBe(scout.Action.ActionStyle.DEFAULT);
    });

  });

  describe('setTabbable', function() {

    it('should modify $container tabindex', function() {
      var action = new scout.Action();
      action.init(createSimpleModel('Action', session));
      // because Action is 'abstract' and has no _render method yet
      // but _renderProperties() is called anyway
      action.$container = $sandbox;
      action.render($sandbox);
      expect(action.$container.attr('tabindex')).toBe(undefined);

      action.setTabbable(true);
      expect(action.$container.attr('tabindex')).toBe('0');
    });

  });

  describe('key stroke', function() {

    it('triggers action', function() {
      var action = scout.create('Action', {
        parent: session.desktop,
        keyStroke: 'ctrl-x'
      });
      session.desktop.keyStrokeContext.registerKeyStroke(action);
      action.render();
      var executed = 0;
      action.on('action', function(event) {
        executed++;
      });

      expect(executed).toBe(0);
      session.desktop.$container.triggerKeyInputCapture(scout.keys.X, 'ctrl');
      expect(executed).toBe(1);
    });

    it('is not triggered if another action with the same key stroke handled it first', function() {
      var action = scout.create('Action', {
        parent: session.desktop,
        keyStroke: 'ctrl-x'
      });
      session.desktop.keyStrokeContext.registerKeyStroke(action);
      var actionExecuted = 0;
      action.on('action', function(event) {
        actionExecuted++;
      });
      action.render();

      var action2 = scout.create('Action', {
        parent: session.desktop,
        keyStroke: 'ctrl-x'
      });
      session.desktop.keyStrokeContext.registerKeyStroke(action2);
      var action2Executed = 0;
      action2.on('action', function(event) {
        action2Executed++;
      });
      action2.render();

      expect(actionExecuted).toBe(0);
      expect(action2Executed).toBe(0);
      session.desktop.$container.triggerKeyInputCapture(scout.keys.X, 'ctrl');
      expect(actionExecuted).toBe(1);
      expect(action2Executed).toBe(0);
    });

  });

  describe('action event', function() {

    it('is triggered when doAction is called', function() {
      var action = scout.create('Action', {
        parent: session.desktop
      });
      var executed = 0;
      action.on('action', function(event) {
        executed++;
      });

      expect(executed).toBe(0);
      action.doAction();
      expect(executed).toBe(1);
    });

    it('is fired when doAction is called even if it is a toggle action', function() {
      var action = scout.create('Action', {
        parent: session.desktop,
        toggleAction: true
      });
      var executed = 0;
      var selected = null;
      action.on('action', function(event) {
        // State is already changed so that listener can react on new state
        selected = action.selected;
        executed++;
      });
      expect(executed).toBe(0);
      expect(selected).toBe(null);

      action.doAction();
      expect(executed).toBe(1);
      expect(selected).toBe(true);

      action.doAction();
      expect(executed).toBe(2);
      expect(selected).toBe(false);
    });

  });

});
