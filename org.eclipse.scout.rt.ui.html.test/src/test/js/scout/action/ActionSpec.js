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

});
