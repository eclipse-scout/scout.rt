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
describe("BusyIndicator", function() {
  var helper, session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('render', function() {
    it('uses entry point as parent if no $parent is provided', function() {
      var busyIndicator = scout.create('BusyIndicator', {
        parent: session.desktop,
        showTimeout: 0
      });
      busyIndicator.render();
      expect(busyIndicator.$parent[0]).toBe(session.$entryPoint[0]);
      busyIndicator.destroy();
    });

    it('uses $parent as parent if provided', function() {
      var $parent = session.$entryPoint.appendDiv();
      var busyIndicator = scout.create('BusyIndicator', {
        parent: session.desktop,
        showTimeout: 0
      });
      busyIndicator.render($parent);
      expect(busyIndicator.$parent[0]).toBe($parent[0]);
      busyIndicator.destroy();
    });
  });
});
