/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("Popup", function() {

  var helper, session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession({
      desktop: {
        navigationVisible: true
      }
    });
  });

  afterEach(function() {
    removePopups(session);
  });

  describe('withGlassPane', function() {
    it('shows a glass pane if set to true', function() {
      var popup = scout.create('Popup', {
        parent: session.desktop,
        withGlassPane: true
      });
      popup.render();
      expect(session.desktop.navigation.$container.children('.glasspane').length).toBe(1);
      expect(popup.$container.children('.glasspane').length).toBe(0);
    });

    it('does not show a glass pane if set to false', function() {
      var popup = scout.create('Popup', {
        parent: session.desktop
      });
      popup.render();
      expect(session.desktop.navigation.$container.children('.glasspane').length).toBe(0);
      expect(popup.$container.children('.glasspane').length).toBe(0);
    });
  });

});
