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
describe('DesktopNavigation', function() {
  var session, desktop, outlineHelper;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession({
      renderDesktop: false,
      desktop: {
        navigationVisible: true
      }
    });
    desktop = session.desktop;
  });

  describe('viewButtonBox', function() {

    it('is rendered if there are view buttons', function() {
      desktop.viewButtons = [scout.create('ViewButton', {parent: desktop})];
      desktop.render(session.$entryPoint);
      expect(desktop.navigation.viewButtonBox.rendered).toBe(true);
    });

    it('is not rendered if there are no view buttons', function() {
      desktop.viewButtons = [];
      desktop.render(session.$entryPoint);
      expect(desktop.navigation.viewButtonBox).toBeFalsy();
    });

  });

});
