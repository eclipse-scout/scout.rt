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
import {icons, scout} from '../../src/index';


describe('Icon', function() {

  var session;

  beforeEach(function() {
    setFixtures(sandbox());
    session = sandboxSession();
  });

  describe('render', function() {

    it('creates a span if it is a font icon', function() {
      var icon = scout.create('Icon', {
        parent: session.desktop,
        iconDesc: icons.INFO
      });
      icon.render();
      expect(icon.$container[0].tagName).toBe('SPAN');
    });

    it('creates an img if it is an image icon', function() {
      var icon = scout.create('Icon', {
        parent: session.desktop,
        iconDesc: 'icon/image.png'
      });
      icon.render();
      expect(icon.$container[0].tagName).toBe('IMG');
    });

  });

  describe('setIconDesc', function() {

    it('accepts a string representing the iconId', function() {
      var icon = scout.create('Icon', {
        parent: session.desktop,
        iconDesc: 'icon/image.png'
      });
      expect(icon.iconDesc.iconUrl).toBe('icon/image.png');

      icon.setIconDesc('icon/image2.png');
      expect(icon.iconDesc.iconUrl).toBe('icon/image2.png');
    });

    it('accepts a scout.IconDesc', function() {
      var icon = scout.create('Icon', {
        parent: session.desktop,
        iconDesc: icons.parseIconId('icon/image.png')
      });
      expect(icon.iconDesc.iconUrl).toBe('icon/image.png');

      icon.setIconDesc(icons.parseIconId('icon/image2.png'));
      expect(icon.iconDesc.iconUrl).toBe('icon/image2.png');
    });

  });

});
