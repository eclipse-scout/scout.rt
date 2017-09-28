/*******************************************************************************
 * Copyright (c) 2014-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe('ButtonAdapterMenu', function() {

  var helper, session, $sandbox, button, adapterMenu;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
    $sandbox = $('#sandbox');
    button = new scout.Button();
    button.init({id:'123', parent: session.desktop});
    adapterMenu = new scout.ButtonAdapterMenu();
    adapterMenu.init({id:'234', button:button, parent: session.desktop});
  });

  describe('initialization / destroy', function() {

    it('should set/delete adaptedBy property on original button instance', function() {
      // init
      expect(button.adaptedBy).toBe(adapterMenu);
      // destroy
      adapterMenu.destroy();
      expect(button.adaptedBy).toBe(undefined);
    });

  });

  describe('focusable element', function() {

    it('button should delegate to adapter menu', function() {
      expect(button.getFocusableElement()).toBe(null);
      expect(adapterMenu.getFocusableElement()).toBe(null);
      adapterMenu.render($sandbox);
      var adapterMenuContainer = adapterMenu.$container[0];
      expect(button.getFocusableElement()).toBe(adapterMenuContainer);
      expect(adapterMenu.getFocusableElement()).toBe(adapterMenuContainer);
    });

  });

});
