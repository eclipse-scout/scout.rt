/*******************************************************************************
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
describe("TileButton", function() {
  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  function createButton(model) {
    var defaults = {
      parent: session.desktop,
    };
    model = $.extend({}, defaults, model);
    return scout.create('TileButton', model);
  }

  function createFormFieldTile(model) {
    var defaults = {
      parent: session.desktop
    };
    model = $.extend({}, defaults, model);
    return scout.create('FormFieldTile', model);
  }

  describe('init', function() {
    it('creates an enabled tile button', function() {
      var button = createButton({
        enabled: true
      });

      var tile = createFormFieldTile({
        tileWidget: button
     });

      tile.render();

      expect(button.$container.hasClass('disabled')).toBe(false);
      button._setEnabled(false);
      expect(button.$container.hasClass('disabled')).toBe(true);
      button._setEnabled(true);
      expect(button.$container.hasClass('disabled')).toBe(false);
    });

    it('creates a disabled tile button', function() {
      var button = createButton({
        enabled: false
      });

      var tile = createFormFieldTile({
        tileWidget: button
     });

      tile.render();

      expect(button.$container.hasClass('disabled')).toBe(true);
      button._setEnabled(true);
      expect(button.$container.hasClass('disabled')).toBe(false);
      button._setEnabled(false);
      expect(button.$container.hasClass('disabled')).toBe(true);
    });
  });
});
